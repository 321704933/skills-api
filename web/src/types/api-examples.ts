export interface ApiExample {
  id: string
  name: string
  method: 'GET' | 'POST'
  endpoint: string
  description: string
  response: object
}

export const apiExamples: ApiExample[] = [
  {
    id: 'hotsearch',
    name: '热搜数据',
    method: 'GET',
    endpoint: '/api/v1/hot-search/baidu/latest',
    description: '获取百度最新热搜数据',
    response: {
      success: true,
      code: 200,
      message: "请求成功",
      status: "SUCCESS",
      timestamp: "2026-03-17T13:24:22.303783200Z",
      traceId: "091cd0a289404980a74b43c55d16253d",
      data: {
        platform: "baidu",
        items: [
          {
            rank: 1,
            title: "北大攻克160余年化学难题",
            hotScore: 7809071,
            url: "https://www.baidu.com/s?wd=北大攻克160余年化学难题",
            hotTag: "热"
          },
          {
            rank: 2,
            title: "韩国首尔市内地铁全线接入微信支付",
            hotScore: 7712204,
            url: "https://www.baidu.com/s?wd=韩国首尔市内地铁全线接入微信支付",
            hotTag: "热"
          },
          {
            rank: 3,
            title: "第二艘国产大型邮轮有哪些新突破",
            hotScore: 7617825,
            url: "https://www.baidu.com/s?wd=第二艘国产大型邮轮有哪些新突破",
            hotTag: ""
          }
        ],
        collectedAt: "2026-03-17T21:00:00"
      }
    }
  },
  {
    id: 'stockindex',
    name: '股票指数',
    method: 'GET',
    endpoint: '/api/v1/stock-index/all',
    description: '获取A股、港股、美股实时指数行情',
    response: {
      success: true,
      code: 200,
      message: "请求成功",
      status: "SUCCESS",
      timestamp: "2026-03-18T10:30:00.000Z",
      traceId: "stock123abc456def",
      data: {
        aShare: [
          { code: "sh000001", name: "上证指数", price: 3426.43, change: 23.56, changePercent: "0.69%" },
          { code: "sz399001", name: "深证成指", price: 10957.85, change: -45.23, changePercent: "-0.41%" },
          { code: "sz399006", name: "创业板指", price: 2256.78, change: 12.34, changePercent: "0.55%" }
        ],
        hk: [
          { code: "hkHSI", name: "恒生指数", price: 24215.32, change: 156.78, changePercent: "0.65%" },
          { code: "hkHSTECH", name: "恒生科技指数", price: 5832.45, change: -28.90, changePercent: "-0.49%" }
        ],
        us: [
          { code: "usDJI", name: "道琼斯指数", price: 42987.55, change: 356.28, changePercent: "0.84%" },
          { code: "usIXIC", name: "纳斯达克指数", price: 19167.33, change: -45.67, changePercent: "-0.24%" },
          { code: "usSPX", name: "标普500", price: 5930.85, change: 18.92, changePercent: "0.32%" }
        ],
        updateTime: "2026-03-18 10:30:00"
      }
    }
  },
  {
    id: 'morningnews',
    name: '每日早报',
    method: 'GET',
    endpoint: '/api/v1/morning-news/general/latest',
    description: '获取综合分类早报',
    response: {
      success: true,
      code: 200,
      message: "请求成功",
      status: "SUCCESS",
      timestamp: "2026-03-17T13:24:33.496944200Z",
      traceId: "00d19187d1444bcab39eb75770e7f6a3",
      data: {
        category: "general",
        categoryName: "综合早报",
        items: [
          {
            rank: 1,
            title: "中美在法国巴黎举行经贸磋商",
            summary: "当地时间3月15日－16日，中美经贸中方牵头人、国务院副总理何立峰与美方代表在巴黎举行经贸磋商。",
            source: "新华社新闻",
            publishTime: "2026-03-16 23:41:07",
            url: "https://view.inews.qq.com/a/20260316A08ISM00",
            coverImage: "https://inews.gtimg.com/news_ls/ONkXifq3ithBR8rmVcvYwYaAraH1_i4FIY23zaHDx4nnMAA_870492/0"
          },
          {
            rank: 2,
            title: "还要打多久？白宫、以军回应",
            summary: "3月15日，特朗普的一名高级助手透露，五角大楼估计，已进入第三周的伊朗战争会持续四到六周。",
            source: "中国新闻网",
            publishTime: "2026-03-16 23:33:12",
            url: "https://view.inews.qq.com/a/20260316A07KRW00",
            coverImage: "https://inews.gtimg.com/news_ls/OGlTQcLFg8cMNS7aIbRBHopWONhmBRyJ7-beLer7QaVgUAA_870492/0"
          }
        ],
        collectedAt: "2026-03-17T21:24:33.4848136"
      }
    }
  },
  {
    id: 'weather',
    name: '天气预报',
    method: 'GET',
    endpoint: '/api/v1/weather/北京',
    description: '获取指定城市的天气数据',
    response: {
      success: true,
      code: 200,
      message: "请求成功",
      status: "SUCCESS",
      timestamp: "2026-03-17T13:24:35.513465900Z",
      traceId: "dac37016537f4a908c3e08e7039172a2",
      data: {
        city: "北京",
        cityCode: "101010100",
        updateTime: "18:00",
        current: {
          temp: "11.4",
          weather: "中度霾",
          windDirection: "东北风",
          windPower: "1级",
          humidity: "56%",
          rain: "0",
          pressure: "1011",
          time: "21:10"
        },
        forecast: [
          {
            date: "17日",
            dayWeather: "多云",
            nightWeather: "小雨",
            tempHigh: "13",
            tempLow: "3",
            dayWindDirection: "东风",
            dayWindPower: "<3级",
            nightWindDirection: "北风",
            nightWindPower: "3-4级",
            sunrise: "06:21",
            sunset: "18:23",
            hourly: [
              { time: "20:00", weather: "小雨", temp: "11", windDirection: "北风", windPower: "<3级" },
              { time: "21:00", weather: "多云", temp: "10", windDirection: "东风", windPower: "<3级" }
            ],
            lifeIndices: [
              { name: "紫外线", level: "最弱", description: "辐射弱，涂擦SPF8-12防晒护肤品。" },
              { name: "运动", level: "较不宜", description: "有降水，推荐您在室内进行休闲运动。" },
              { name: "穿衣", level: "较冷", description: "建议着厚外套加毛衣等服装。" }
            ]
          }
        ],
        observations: [
          { hour: "20", temp: "11.5", windDirection: "东风", windPower: "1", humidity: "56" },
          { hour: "19", temp: "12", windDirection: "东北风", windPower: "1", humidity: "54" }
        ]
      }
    }
  },
  {
    id: 'almanac',
    name: '今日黄历',
    method: 'GET',
    endpoint: '/api/v1/almanac/almanac',
    description: '获取今日黄历信息',
    response: {
      success: true,
      code: 200,
      message: "请求成功",
      status: "SUCCESS",
      timestamp: "2026-03-17T13:24:37.047210100Z",
      traceId: "3ed3159c9cb54711b424485ddac384e5",
      data: {
        date: "2026-03-17",
        lunarDate: "丙午年正月廿九",
        yearGanZhi: "丙午",
        monthGanZhi: "辛卯",
        dayGanZhi: "庚寅",
        zodiac: "马",
        suitable: ["安床", "伐木", "拆卸", "修造", "动土", "上梁", "立券", "交易", "栽种", "纳畜", "牧养", "入殓", "安葬"],
        avoid: ["嫁娶", "祭祀", "开光", "出行", "出火", "移徙", "入宅", "安门"],
        festivals: [],
        jieQi: "",
        week: "二",
        xiu: "室",
        xiuLuck: "吉",
        pengZuGan: "庚不经络织机虚张",
        pengZuZhi: "寅不祭祀神鬼不尝",
        positionXi: "西北",
        positionFu: "西南",
        positionCai: "正东",
        dayChong: "(甲申)猴",
        daySha: "北",
        yearNaYin: "天河水",
        monthNaYin: "松柏木",
        dayNaYin: "松柏木",
        yueXiang: "晓"
      }
    }
  },
  {
    id: 'ip',
    name: 'IP查询',
    method: 'GET',
    endpoint: '/api/v1/ip/query?ip=8.8.8.8',
    description: '查询IP地址的地理位置',
    response: {
      success: true,
      code: 200,
      message: "请求成功",
      status: "SUCCESS",
      timestamp: "2026-03-17T13:24:38.640936700Z",
      traceId: "d16a82577bfd4c3fbf5c2a354b557feb",
      data: {
        ip: "8.8.8.8",
        country: "United States",
        province: "",
        city: "Google LLC",
        isp: "US"
      }
    }
  },
  {
    id: 'prose',
    name: '散文句子',
    method: 'GET',
    endpoint: '/api/v1/prose/random',
    description: '获取随机散文句子',
    response: {
      success: true,
      code: 200,
      message: "请求成功",
      status: "SUCCESS",
      timestamp: "2026-03-17T13:24:40.084030600Z",
      traceId: "254c9acffc394e5396b2c9e9db52c816",
      data: {
        content: "我到底经历了什么，才能收起暴躁的脾气和骄傲",
        source: "我在人间凑数的日子"
      }
    }
  },
  {
    id: 'sensitive',
    name: '敏感词检测',
    method: 'GET',
    endpoint: '/api/v1/sensitive/check?text=需要检测的文本',
    description: '检测文本中的敏感词',
    response: {
      success: true,
      code: 200,
      message: "请求成功",
      status: "SUCCESS",
      timestamp: "2026-03-17T13:24:53.180995900Z",
      traceId: "7b9273d77c8f4e0e9c8e3bf741b5cae3",
      data: {
        hasSensitive: false,
        foundWords: [],
        filteredText: "这是一个测试"
      }
    }
  },
  {
    id: 'captcha',
    name: '验证码生成',
    method: 'POST',
    endpoint: '/api/v1/captcha/generate',
    description: '生成图形验证码',
    response: {
      success: true,
      code: 200,
      message: "请求成功",
      status: "SUCCESS",
      timestamp: "2026-03-17T13:25:00.000Z",
      traceId: "cap789def012",
      data: {
        captchaId: "cap_abc123xyz",
        captchaImage: "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA...",
        expireSeconds: 300
      }
    }
  },
  {
    id: 'image',
    name: '图片转换',
    method: 'POST',
    endpoint: '/api/v1/image/convert',
    description: '转换图片格式',
    response: {
      success: true,
      code: 200,
      message: "请求成功",
      status: "SUCCESS",
      timestamp: "2026-03-17T13:25:00.000Z",
      traceId: "img345ghi678",
      data: {
        originalFormat: "svg",
        targetFormat: "png",
        convertedImage: "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA...",
        fileSize: "15.2KB"
      }
    }
  }
]
