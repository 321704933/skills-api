package ai.skills.api.weather.collector;

import ai.skills.api.common.redis.RedisUtils;
import ai.skills.api.weather.model.WeatherResponse;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 天气数据采集器
 * 从中国天气网 (weather.com.cn) 采集天气预报数据
 * 采集内容包括：实时天气、空气质量、7天预报、逐小时预报、24小时观测、生活指数
 *
 * @author Devil
 * @since 2025-03-16
 */
@Slf4j
@Component
public class WeatherCollector {

    /**
     * 7天天气预报页面地址
     */
    private static final String WEATHER_7D_URL = "https://www.weather.com.cn/weathern/{}.shtml";

    /**
     * 今日详情页面地址（包含24小时观测）
     */
    private static final String WEATHER_1D_URL = "https://www.weather.com.cn/weather1dn/{}.shtml";

    /**
     * 实时天气数据接口（JSONP 格式，需要 Referer 头）
     */
    private static final String SK_API_URL = "http://d1.weather.com.cn/sk_2d/{}.html";



    /**
     * d1 接口所需的 Referer 头
     */
    private static final String REFERER = "http://www.weather.com.cn/";

    /**
     * 请求超时时间（毫秒）
     */
    private static final int TIMEOUT_MS = 10000;

    /**
     * 天气数据缓存键前缀
     */
    private static final String CACHE_KEY_PREFIX = "weather:data:";

    /**
     * 天气数据缓存时长（10分钟）
     */
    private static final Duration CACHE_TTL = Duration.ofMinutes(10);

    /**
     * 城市编码映射（城市名 -> 城市编码）
     */
    private final Map<String, String> cityCodeMap = new ConcurrentHashMap<>();

    /**
     * 构造函数，加载城市编码数据
     */
    public WeatherCollector() {
        loadCityCodes();
    }

    /**
     * 从 city-code.json 加载城市编码数据
     */
    private void loadCityCodes() {
        try {
            String json = ResourceUtil.readUtf8Str("weather/city-code.json");
            JSONObject obj = JSONUtil.parseObj(json);
            for (String key : obj.keySet()) {
                cityCodeMap.put(key, obj.getStr(key));
            }
            log.info("加载城市编码数据成功，共 {} 个城市", cityCodeMap.size());
        } catch (Exception e) {
            log.error("加载城市编码数据失败", e);
        }
    }

    /**
     * 根据城市名称获取城市编码
     */
    public String getCityCode(String cityName) {
        return cityCodeMap.get(cityName);
    }

    /**
     * 搜索城市编码（模糊匹配）
     */
    public List<String> searchCity(String keyword) {
        List<String> result = new ArrayList<>();
        String lowerKeyword = keyword.toLowerCase();
        for (String city : cityCodeMap.keySet()) {
            if (city.toLowerCase().contains(lowerKeyword)) {
                result.add(city);
                if (result.size() >= 10) {
                    break;
                }
            }
        }
        return result;
    }

    /**
     * 采集天气数据（优先读取缓存）
     */
    public WeatherResponse collect(String cityName) {
        String cityCode = getCityCode(cityName);
        if (cityCode == null) {
            log.warn("未找到城市编码: {}", cityName);
            return null;
        }
        return collectByCode(cityName, cityCode);
    }

    /**
     * 根据城市编码采集天气数据
     * 优先从 Redis 缓存读取，缓存未命中时采集并写入缓存
     */
    public WeatherResponse collectByCode(String cityName, String cityCode) {
        // 1. 尝试从缓存获取
        String cacheKey = CACHE_KEY_PREFIX + cityCode;
        WeatherResponse cached = RedisUtils.getCacheObject(cacheKey);
        if (cached != null) {
            log.debug("命中天气缓存: {} ({})", cityName, cityCode);
            return cached;
        }

        // 2. 缓存未命中，执行采集
        try {
            String url7d = StrUtil.format(WEATHER_7D_URL, cityCode);
            String url1d = StrUtil.format(WEATHER_1D_URL, cityCode);

            Document doc7d = Jsoup.connect(url7d).timeout(TIMEOUT_MS).get();
            Document doc1d = fetchPageSilently(url1d);

            WeatherResponse result = buildWeatherResponse(cityName, cityCode, doc7d, doc1d);

            // 3. 写入缓存
            if (result != null) {
                RedisUtils.setCacheObject(cacheKey, result, CACHE_TTL);
                log.debug("天气数据已缓存: {} ({})，TTL={}", cityName, cityCode, CACHE_TTL);
            }

            return result;
        } catch (Exception e) {
            log.error("采集天气数据失败: {} ({})", cityName, cityCode, e);
            return null;
        }
    }

    /**
     * 静默获取页面，失败不抛异常
     */
    private Document fetchPageSilently(String url) {
        try {
            return Jsoup.connect(url).timeout(TIMEOUT_MS).get();
        } catch (Exception e) {
            log.warn("获取页面失败: {}", url, e);
            return null;
        }
    }

    // ==================== 核心组装 ====================

    /**
     * 组装完整天气响应数据
     */
    private WeatherResponse buildWeatherResponse(String cityName, String cityCode,
                                                  Document doc7d, Document doc1d) {
        // 1. 提取更新时间
        String updateTime = extractUpdateTime(doc7d);

        // 2. 解析实时天气（从 d1 接口获取）
        WeatherResponse.CurrentWeather current = parseCurrentWeather(cityCode);

        // 3. 解析7天预报
        List<WeatherResponse.DailyWeather> forecast = parseDailyForecast(doc7d);

        // 4. 解析24小时观测数据（从今日详情页）
        List<WeatherResponse.Observation> observations = parseObservations(doc1d);

        return new WeatherResponse(cityName, cityCode, updateTime, current, forecast, observations);
    }

    // ==================== 更新时间 ====================

    /**
     * 提取数据更新时间
     */
    private String extractUpdateTime(Document doc) {
        Element updateTimeEl = doc.selectFirst("#update_time");
        if (updateTimeEl != null) {
            return updateTimeEl.attr("value");
        }
        return "";
    }

    // ==================== 实时天气 ====================

    /**
     * 从 d1.weather.com.cn 接口获取实时天气数据
     * 接口返回 JSONP 格式：var dataSK = {...};
     *
     * @param cityCode 城市编码
     */
    private WeatherResponse.CurrentWeather parseCurrentWeather(String cityCode) {
        try {
            String url = StrUtil.format(SK_API_URL, cityCode);
            String response = cn.hutool.http.HttpRequest.get(url)
                    .header("Referer", REFERER)
                    .timeout(TIMEOUT_MS)
                    .execute()
                    .body();

            if (StrUtil.isBlank(response)) return null;

            // 解析 JSONP 响应：var dataSK = {...};
            JSONObject dataSK = parseJsonpResponse(response);
            if (dataSK == null) return null;

            return new WeatherResponse.CurrentWeather(
                    dataSK.getStr("temp", ""),
                    dataSK.getStr("weather", ""),
                    dataSK.getStr("WD", ""),
                    dataSK.getStr("WS", ""),
                    dataSK.getStr("SD", ""),
                    dataSK.getStr("rain", dataSK.getStr("rain24h", "0")),
                    dataSK.getStr("qy", ""),
                    dataSK.getStr("time", "")
            );
        } catch (Exception e) {
            log.warn("获取实时天气失败: {}", cityCode, e);
            return null;
        }
    }



    /**
     * 解析 JSONP 格式的响应
     * 支持格式：var xxx = {...}; 或直接 {...}
     */
    private JSONObject parseJsonpResponse(String response) {
        try {
            String json = response.trim();

            // 去掉 var xxx = 前缀
            if (json.contains("=")) {
                json = json.substring(json.indexOf("=") + 1).trim();
            }

            // 去掉末尾分号
            if (json.endsWith(";")) {
                json = json.substring(0, json.length() - 1).trim();
            }

            // 去掉 JSONP 回调包装 callback({...})
            if (json.contains("(") && json.endsWith(")")) {
                json = json.substring(json.indexOf("(") + 1, json.lastIndexOf(")"));
            }

            return JSONUtil.parseObj(json);
        } catch (Exception e) {
            log.debug("JSONP 响应解析失败: {}", response.substring(0, Math.min(200, response.length())), e);
            return null;
        }
    }

    // ==================== 7天预报 ====================

    /**
     * 解析7天预报数据
     * 注意：页面第一天是"昨天"，需要跳过
     */
    private List<WeatherResponse.DailyWeather> parseDailyForecast(Document doc) {
        List<WeatherResponse.DailyWeather> result = new ArrayList<>();

        // 1. 提取日期信息（跳过第一个，即昨天）
        List<String> dates = extractDates(doc);

        // 2. 提取天气现象、风向风力信息（跳过第一个，即昨天）
        List<String[]> weatherPairs = extractWeatherPairs(doc);
        List<String[]> windDirectionPairs = extractWindDirectionPairs(doc);
        List<String[]> windPowerPairs = extractWindPowerPairs(doc);

        // 3. 从 JavaScript 变量中提取温度和日出日落数据（跳过第一个，即昨天）
        String scriptContent = getScriptContent(doc);
        List<String> tempHighs = extractJsArrayVariable(scriptContent, "eventDay", true);
        List<String> tempLows = extractJsArrayVariable(scriptContent, "eventNight", true);
        List<String> sunrises = extractJsArrayVariable(scriptContent, "sunup", true);
        List<String> sunsets = extractJsArrayVariable(scriptContent, "sunset", true);

        // 4. 组装7天预报数据
        int days = Math.min(dates.size(),
                Math.min(tempHighs.size(), tempLows.size()));

        for (int i = 0; i < days; i++) {
            String date = safeGet(dates, i);

            // 天气：白天/夜间
            String[] wp = safeGetArray(weatherPairs, i);
            String dayWeather = wp[0];
            String nightWeather = wp[1];

            // 风向：白天/夜间
            String[] wdp = safeGetArray(windDirectionPairs, i);
            String dayWindDir = wdp[0];
            String nightWindDir = wdp[1];

            // 风力：白天/夜间
            String[] wpp = safeGetArray(windPowerPairs, i);
            String dayWindPower = wpp[0];
            String nightWindPower = wpp[1];

            String tempHigh = safeGet(tempHighs, i);
            String tempLow = safeGet(tempLows, i);
            String sunrise = safeGet(sunrises, i);
            String sunset = safeGet(sunsets, i);

            // 逐小时预报（hour3data 不包含昨天，索引从0开始对应今天）
            List<WeatherResponse.HourlyWeather> hourly = parseHourlyForecast(doc, i);

            // 生活指数
            List<WeatherResponse.LifeIndex> lifeIndices = parseLifeIndices(doc, i);

            result.add(new WeatherResponse.DailyWeather(
                    date, dayWeather, nightWeather,
                    tempHigh, tempLow,
                    dayWindDir, dayWindPower, nightWindDir, nightWindPower,
                    sunrise, sunset, hourly, lifeIndices
            ));
        }

        return result;
    }

    /**
     * 提取日期列表（跳过昨天）
     */
    private List<String> extractDates(Document doc) {
        List<String> dates = new ArrayList<>();
        Elements dateItems = doc.select("ul.date-container > li.date-item");
        for (int i = 1; i < dateItems.size(); i++) {
            Element dateItem = dateItems.get(i);
            String date = dateItem.selectFirst("p.date") != null ?
                    dateItem.selectFirst("p.date").text() : "";
            dates.add(date);
        }
        return dates;
    }

    /**
     * 提取天气现象（白天/夜间对）
     * 页面结构中包含两个天气容器：sky（白天）和另一个（夜间）
     */
    private List<String[]> extractWeatherPairs(Document doc) {
        List<String[]> result = new ArrayList<>();

        // 白天天气
        Elements dayItems = doc.select("ul.blue-container.sky > li.blue-item");
        // 夜间天气
        Elements nightItems = doc.select("ul.blue-container.land > li.blue-item");

        int count = Math.max(dayItems.size(), nightItems.size());
        for (int i = 1; i < count; i++) {
            String dayWeather = "";
            String nightWeather = "";

            if (i < dayItems.size()) {
                Element item = dayItems.get(i);
                Element weatherInfo = item.selectFirst("p.weather-info");
                dayWeather = weatherInfo != null ? weatherInfo.attr("title") : "";
            }

            if (i < nightItems.size()) {
                Element item = nightItems.get(i);
                Element weatherInfo = item.selectFirst("p.weather-info");
                nightWeather = weatherInfo != null ? weatherInfo.attr("title") : "";
            }

            // 如果没有分开的白天/夜间，尝试解析"晴转多云"格式
            if (StrUtil.isBlank(nightWeather) && dayWeather.contains("转")) {
                String[] parts = dayWeather.split("转", 2);
                dayWeather = parts[0].trim();
                nightWeather = parts[1].trim();
            } else if (StrUtil.isBlank(nightWeather)) {
                nightWeather = dayWeather;
            }

            result.add(new String[]{dayWeather, nightWeather});
        }

        return result;
    }

    /**
     * 提取风向信息对（白天/夜间）
     */
    private List<String[]> extractWindDirectionPairs(Document doc) {
        List<String[]> result = new ArrayList<>();
        Elements weatherItems = doc.select("ul.blue-container.sky > li.blue-item");

        for (int i = 1; i < weatherItems.size(); i++) {
            Element item = weatherItems.get(i);
            Elements windIcons = item.select("div.wind-container > i.wind-icon");

            String dayWind = "";
            String nightWind = "";

            if (windIcons.size() >= 2) {
                dayWind = windIcons.get(0).attr("title");
                nightWind = windIcons.get(1).attr("title");
            } else if (windIcons.size() == 1) {
                dayWind = windIcons.get(0).attr("title");
                nightWind = dayWind;
            }

            result.add(new String[]{dayWind, nightWind});
        }

        return result;
    }

    /**
     * 提取风力信息对（白天/夜间）
     */
    private List<String[]> extractWindPowerPairs(Document doc) {
        List<String[]> result = new ArrayList<>();
        Elements weatherItems = doc.select("ul.blue-container.sky > li.blue-item");

        for (int i = 1; i < weatherItems.size(); i++) {
            Element item = weatherItems.get(i);
            Element windInfo = item.selectFirst("p.wind-info");
            String windPowerText = windInfo != null ? windInfo.text() : "";

            String dayPower = windPowerText;
            String nightPower = windPowerText;

            // 解析"3-4级转<3级"格式
            if (windPowerText.contains("转")) {
                String[] parts = windPowerText.split("转", 2);
                dayPower = parts[0].trim();
                nightPower = parts[1].trim();
            }

            result.add(new String[]{dayPower, nightPower});
        }

        return result;
    }

    // ==================== 逐小时预报 ====================

    /**
     * 解析指定日期的逐小时预报
     * <p>
     * 新版页面的 hour3data 格式：
     * hour3data={"cityCode":[[时间,温度,天气,图标,风向,风力], ...], ...}
     * 旧版格式：hour3data=[[{ja:...,jb:...}, ...], ...]
     *
     * @param dayIndex 日期索引（0=今天，1=明天，2=后天...）
     */
    private List<WeatherResponse.HourlyWeather> parseHourlyForecast(Document doc, int dayIndex) {
        List<WeatherResponse.HourlyWeather> result = new ArrayList<>();

        // 风向映射表（来自页面 JS 代码）
        String[] windDirNames = {"无持续风向", "东北风", "东风", "东南风", "南风",
                "西南风", "西风", "西北风", "北风", "旋转风"};
        // 风力等级映射表（来自页面 JS 代码）
        String[] windPowerNames = {"<3级", "3-4级", "4-5级", "5-6级", "6-7级",
                "7-8级", "8-9级", "9-10级", "10-11级", "11-12级"};

        String scriptContent = getScriptContent(doc);
        if (scriptContent == null) return result;

        try {
            // 尝试新版格式：hour3data={"101010100": [[...], ...]}
            if (scriptContent.contains("var hour3data=")) {
                result = parseHourlyNewFormat(scriptContent, dayIndex);
                if (!result.isEmpty()) return result;
            }

            // 尝试旧版格式：hour3data=[[{...}, ...], ...]
            if (scriptContent.contains("var hour3data=")) {
                result = parseHourlyOldFormat(scriptContent, dayIndex, windDirNames, windPowerNames);
            }
        } catch (Exception e) {
            log.warn("解析逐小时预报失败", e);
        }

        return result;
    }

    /**
     * 解析新版逐小时预报格式
     * 格式: hour3data={"101010100": [["20260316200000","10","晴","n00","南风","3级"], ...]}
     */
    private List<WeatherResponse.HourlyWeather> parseHourlyNewFormat(String scriptContent, int dayIndex) {
        List<WeatherResponse.HourlyWeather> result = new ArrayList<>();

        try {
            int startIdx = scriptContent.indexOf("var hour3data=") + "var hour3data=".length();
            String data = scriptContent.substring(startIdx);

            // 找到 JSON 对象的边界
            int braceStart = data.indexOf("{");
            if (braceStart == -1) return result;

            int depth = 0;
            int braceEnd = -1;
            for (int i = braceStart; i < data.length(); i++) {
                char c = data.charAt(i);
                if (c == '{') depth++;
                else if (c == '}') {
                    depth--;
                    if (depth == 0) {
                        braceEnd = i + 1;
                        break;
                    }
                }
            }

            if (braceEnd <= braceStart) return result;

            String json = data.substring(braceStart, braceEnd);
            JSONObject hourData = JSONUtil.parseObj(json);

            // 获取第一个城市的数据
            for (String key : hourData.keySet()) {
                JSONArray allHours = hourData.getJSONArray(key);
                if (allHours == null) continue;

                // 根据 dayIndex 过滤对应日期的数据
                // 所有数据在一个扁平数组中，需要按日期归组
                String targetDatePrefix = "";
                List<JSONArray> dayHours = new ArrayList<>();

                for (int i = 0; i < allHours.size(); i++) {
                    JSONArray hourItem = allHours.getJSONArray(i);
                    if (hourItem == null || hourItem.isEmpty()) continue;

                    String dateTimeStr = hourItem.getStr(0);
                    if (dateTimeStr != null && dateTimeStr.length() >= 8) {
                        String dateStr = dateTimeStr.substring(0, 8);
                        if (targetDatePrefix.isEmpty()) {
                            targetDatePrefix = dateStr;
                        }
                        dayHours.add(hourItem);
                    }
                }

                // 按日期分组
                Map<String, List<JSONArray>> grouped = new java.util.LinkedHashMap<>();
                for (JSONArray hourItem : dayHours) {
                    String dateStr = hourItem.getStr(0).substring(0, 8);
                    grouped.computeIfAbsent(dateStr, k -> new ArrayList<>()).add(hourItem);
                }

                // 获取指定日期的数据
                List<String> dateKeys = new ArrayList<>(grouped.keySet());
                // dayIndex: 0=今天, 1=明天, 2=后天...
                // hour3data 从今天开始，索引直接对应
                int targetIdx = dayIndex;
                if (targetIdx >= 0 && targetIdx < dateKeys.size()) {
                    List<JSONArray> hours = grouped.get(dateKeys.get(targetIdx));
                    for (JSONArray hourItem : hours) {
                        if (hourItem.size() >= 6) {
                            String dateTime = hourItem.getStr(0);
                            String time = "";
                            if (dateTime != null && dateTime.length() >= 12) {
                                time = dateTime.substring(8, 10) + ":" + dateTime.substring(10, 12);
                            } else if (dateTime != null && dateTime.length() >= 10) {
                                time = dateTime.substring(8, 10) + ":00";
                            }
                            String temp = hourItem.getStr(1);
                            String weather = hourItem.getStr(2);
                            String windDir = hourItem.getStr(4);
                            String windPower = hourItem.getStr(5);

                            result.add(new WeatherResponse.HourlyWeather(
                                    time, weather, temp, windDir, windPower
                            ));
                        }
                    }
                }
                break; // 只取第一个城市
            }
        } catch (Exception e) {
            log.debug("新版逐小时格式解析失败，尝试旧版格式", e);
        }

        return result;
    }

    /**
     * 解析旧版逐小时预报格式
     * 格式: hour3data=[[{ja:"00",jb:"7",jc:"0",jd:"4",je:"0",jf:"2026031608"}, ...], ...]
     */
    private List<WeatherResponse.HourlyWeather> parseHourlyOldFormat(String scriptContent, int dayIndex,
                                                                      String[] windDirNames, String[] windPowerNames) {
        List<WeatherResponse.HourlyWeather> result = new ArrayList<>();

        try {
            int startIdx = scriptContent.indexOf("var hour3data=") + "var hour3data=".length();
            String data = scriptContent.substring(startIdx);

            int start = data.indexOf("[");
            if (start == -1) return result;

            // 手动匹配括号
            int depth = 0;
            int end = -1;
            for (int i = start; i < data.length(); i++) {
                char c = data.charAt(i);
                if (c == '[') depth++;
                else if (c == ']') {
                    depth--;
                    if (depth == 0) {
                        end = i + 1;
                        break;
                    }
                }
            }

            if (end > start) {
                String json = data.substring(start, end);
                JSONArray dayArray = JSONUtil.parseArray(json);
                if (dayIndex < dayArray.size()) {
                    JSONArray hourArray = dayArray.getJSONArray(dayIndex);
                    for (int i = 0; i < hourArray.size() && i < 8; i++) {
                        JSONObject hour = hourArray.getJSONObject(i);
                        String weatherCode = hour.getStr("ja");
                        String temp = hour.getStr("jb");
                        String windPowerIndex = hour.getStr("jc");
                        String windDirIndex = hour.getStr("jd");
                        String dateTime = hour.getStr("jf");

                        String time = "";
                        if (dateTime != null && dateTime.length() >= 10) {
                            time = dateTime.substring(8, 10) + ":00";
                        }

                        String weather = getWeatherName(weatherCode);
                        String windDirection = resolveMapping(windDirIndex, windDirNames);
                        String windPower = resolveMapping(windPowerIndex, windPowerNames);

                        result.add(new WeatherResponse.HourlyWeather(
                                time, weather, temp, windDirection, windPower
                        ));
                    }
                }
            }
        } catch (Exception e) {
            log.debug("旧版逐小时格式解析失败", e);
        }

        return result;
    }

    // ==================== 生活指数 ====================

    /**
     * 解析指定日期的生活指数
     * 注意：生活指数只有今天起7天有数据，昨天没有
     *
     * @param dayIndex 日期索引（0=今天，1=明天...）
     */
    private List<WeatherResponse.LifeIndex> parseLifeIndices(Document doc, int dayIndex) {
        List<WeatherResponse.LifeIndex> result = new ArrayList<>();

        // 生活指数在新版页面中用 div.lv 显示，每个 div.lv 是一天
        Elements lvDivs = doc.select("div.lv");
        if (dayIndex < lvDivs.size()) {
            Element lvDiv = lvDivs.get(dayIndex);

            // 提取所有指数名称
            Elements nameItems = doc.select("div.livezs ul > li > h2, div.livezs-item h2, ul > li > h2");
            List<String> names = new ArrayList<>();
            for (Element nameItem : nameItems) {
                String text = nameItem.text().trim();
                if (!text.isEmpty()) {
                    names.add(text);
                }
            }

            // 如果没找到名称，使用默认名称列表
            if (names.isEmpty()) {
                names = List.of("紫外线指数", "运动指数", "过敏指数", "穿衣指数", "洗车指数", "空气污染扩散指数");
            }

            // 提取当前天的所有指数详情
            Elements dlItems = lvDiv.select("dl");
            for (int i = 0; i < dlItems.size(); i++) {
                Element dlItem = dlItems.get(i);

                // 等级：dt > em
                Element emEl = dlItem.selectFirst("dt > em");
                String level = emEl != null ? emEl.text() : "";

                // 描述：dd
                Element ddEl = dlItem.selectFirst("dd");
                String desc = ddEl != null ? ddEl.text() : "";

                // 名称
                String name = i < names.size() ? names.get(i) : "";

                result.add(new WeatherResponse.LifeIndex(name, level, desc));
            }
        }

        return result;
    }

    // ==================== 24小时观测 ====================

    /**
     * 解析过去24小时整点观测数据
     * 数据来源：今日详情页的 observe24h JavaScript 变量
     */
    private List<WeatherResponse.Observation> parseObservations(Document doc) {
        List<WeatherResponse.Observation> result = new ArrayList<>();

        if (doc == null) return result;

        try {
            String scriptContent = getScriptContent(doc);
            if (scriptContent == null) return result;

            // 提取 observe24h 变量
            JSONObject observe24h = extractJsonVariable(scriptContent, "observe24h_data");
            if (observe24h == null) {
                observe24h = extractJsonVariable(scriptContent, "observe24h");
            }

            if (observe24h != null) {
                // 结构：{ od: { od0: "时间", od1: "城市", od2: [{od21:时, od22:温度, ...}, ...] } }
                JSONObject od = observe24h.getJSONObject("od");
                if (od == null) {
                    // 可能直接就是嵌套在城市编码下
                    for (String key : observe24h.keySet()) {
                        Object val = observe24h.get(key);
                        if (val instanceof JSONObject) {
                            od = ((JSONObject) val).getJSONObject("od");
                            if (od != null) break;
                        }
                    }
                }

                if (od != null) {
                    JSONArray od2 = od.getJSONArray("od2");
                    if (od2 != null) {
                        for (int i = 0; i < od2.size(); i++) {
                            JSONObject item = od2.getJSONObject(i);
                            result.add(new WeatherResponse.Observation(
                                    item.getStr("od21", ""),   // 时间（整点）
                                    item.getStr("od22", ""),   // 温度
                                    item.getStr("od24", ""),   // 风向
                                    item.getStr("od25", ""),   // 风力
                                    item.getStr("od27", "")    // 湿度
                            ));
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("解析24小时观测数据失败", e);
        }

        return result;
    }

    // ==================== 工具方法 ====================

    /**
     * 获取页面所有 script 标签的内容拼接
     */
    private String getScriptContent(Document doc) {
        if (doc == null) return null;
        StringBuilder sb = new StringBuilder();
        Elements scripts = doc.select("script");
        for (Element script : scripts) {
            if (script.html().length() > 50) {
                sb.append(script.html()).append("\n");
            }
        }
        return sb.length() > 0 ? sb.toString() : null;
    }

    /**
     * 从 JavaScript 代码中提取 JSON 对象变量
     * 支持格式：var name = {...}; 或 var name={...};
     */
    private JSONObject extractJsonVariable(String scriptContent, String varName) {
        if (scriptContent == null) return null;

        try {
            // 匹配 var varName = {...} 或 var varName={...}
            String pattern1 = "var " + varName + "\\s*=\\s*";
            int idx = -1;
            Pattern p = Pattern.compile(pattern1);
            Matcher m = p.matcher(scriptContent);
            if (m.find()) {
                idx = m.end();
            }

            if (idx == -1) return null;

            // 找到 { 开始
            int braceStart = scriptContent.indexOf("{", idx);
            if (braceStart == -1 || braceStart - idx > 10) return null;

            // 手动匹配括号
            int depth = 0;
            int braceEnd = -1;
            for (int i = braceStart; i < scriptContent.length(); i++) {
                char c = scriptContent.charAt(i);
                if (c == '{') depth++;
                else if (c == '}') {
                    depth--;
                    if (depth == 0) {
                        braceEnd = i + 1;
                        break;
                    }
                }
            }

            if (braceEnd > braceStart) {
                String json = scriptContent.substring(braceStart, braceEnd);
                return JSONUtil.parseObj(json);
            }
        } catch (Exception e) {
            log.debug("提取 JS 变量 {} 失败", varName, e);
        }

        return null;
    }

    /**
     * 从 JavaScript 代码中提取数组变量
     * 支持格式：var name = [1,2,3]; 或 var name=["a","b"];
     *
     * @param skipFirst 是否跳过第一个元素（昨天的数据）
     */
    private List<String> extractJsArrayVariable(String scriptContent, String varName, boolean skipFirst) {
        List<String> result = new ArrayList<>();
        if (scriptContent == null) return result;

        try {
            String searchKey = "var " + varName + " =";
            if (!scriptContent.contains(searchKey)) {
                searchKey = "var " + varName + "=";
            }
            if (!scriptContent.contains(searchKey)) return result;

            String[] parts = scriptContent.split(Pattern.quote(searchKey));
            if (parts.length > 1) {
                String data = parts[1];
                int semicolonIdx = data.indexOf(";");
                if (semicolonIdx > 0) {
                    data = data.substring(0, semicolonIdx);
                }
                data = data.replace("[", "").replace("]", "").replace("\"", "").trim();
                String[] values = data.split(",");

                int startIdx = skipFirst ? 1 : 0;
                for (int i = startIdx; i < values.length; i++) {
                    result.add(values[i].trim());
                }
            }
        } catch (Exception e) {
            log.debug("提取 JS 数组变量 {} 失败", varName, e);
        }

        return result;
    }

    /**
     * 安全获取列表元素
     */
    private String safeGet(List<String> list, int index) {
        return (index >= 0 && index < list.size()) ? list.get(index) : "";
    }

    /**
     * 安全获取数组对列表元素
     */
    private String[] safeGetArray(List<String[]> list, int index) {
        return (index >= 0 && index < list.size()) ? list.get(index) : new String[]{"", ""};
    }

    /**
     * 安全获取 HTML 元素文本
     */
    private String getElementText(Document doc, String cssSelector) {
        if (doc == null) return "";
        // 支持多个选择器（逗号分隔）
        String[] selectors = cssSelector.split(",");
        for (String selector : selectors) {
            Element el = doc.selectFirst(selector.trim());
            if (el != null && !el.text().isBlank()) {
                return el.text().trim();
            }
        }
        return "";
    }

    /**
     * 解析索引到映射表值
     */
    private String resolveMapping(String indexStr, String[] mappings) {
        try {
            int idx = Integer.parseInt(indexStr);
            if (idx >= 0 && idx < mappings.length) {
                return mappings[idx];
            }
        } catch (NumberFormatException ignored) {
        }
        return "";
    }

    /**
     * 根据天气代码获取天气名称
     */
    private String getWeatherName(String code) {
        if (code == null) return "";
        return switch (code) {
            case "00", "0", "92", "100" -> "晴";
            case "01", "1", "93", "101" -> "多云";
            case "02", "2", "49", "94", "95", "102" -> "阴";
            case "03", "3", "50", "103" -> "阵雨";
            case "04", "4", "51", "104" -> "雷阵雨";
            case "05", "5", "105" -> "雷阵雨伴有冰雹";
            case "06", "6", "53", "106" -> "雨夹雪";
            case "07", "7", "54", "107" -> "小雨";
            case "08", "8", "55", "108" -> "中雨";
            case "09", "9", "56", "109" -> "大雨";
            case "10", "57", "110" -> "暴雨";
            case "11", "58", "111" -> "大暴雨";
            case "12", "59", "112" -> "特大暴雨";
            case "13", "60", "113" -> "阵雪";
            case "14", "61", "114" -> "小雪";
            case "15", "62", "115" -> "中雪";
            case "16", "63", "116" -> "大雪";
            case "17", "64", "117" -> "暴雪";
            case "18", "118" -> "雾";
            case "19", "65", "119" -> "冻雨";
            case "20", "66", "120" -> "沙尘暴";
            case "21", "67", "121" -> "浮尘";
            case "22", "68", "122" -> "扬沙";
            case "23", "69", "123" -> "强沙尘暴";
            case "24", "70", "124" -> "霾";
            case "25", "72", "125" -> "浓雾";
            case "26", "74", "126" -> "强浓雾";
            case "27", "75", "127" -> "中度霾";
            case "28", "76", "128" -> "重度霾";
            case "29", "77", "129" -> "严重霾";
            case "30", "78", "130" -> "大雾";
            case "31", "79", "131" -> "特强浓雾";
            case "32" -> "风";
            case "52" -> "冰雹";
            case "71", "73" -> "雾";
            case "80" -> "热雷暴";
            case "81" -> "干雷暴";
            case "82" -> "龙卷风";
            case "83" -> "飑";
            case "84" -> "飓";
            case "85" -> "台风";
            case "86" -> "沙尘";
            case "87" -> "尘卷风";
            case "88" -> "强沙尘暴";
            case "89" -> "烟雾";
            case "90" -> "冻雾";
            case "91", "99" -> "未知";
            default -> code;
        };
    }
}
