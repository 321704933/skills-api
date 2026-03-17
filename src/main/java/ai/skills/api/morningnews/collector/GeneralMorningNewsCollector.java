package ai.skills.api.morningnews.collector;

import ai.skills.api.morningnews.MorningNewsCollector;
import ai.skills.api.morningnews.MorningNewsItem;
import ai.skills.api.morningnews.MorningNewsResult;
import ai.skills.api.morningnews.NewsCategory;
import ai.skills.api.morningnews.collector.model.TencentNewsResponse;
import ai.skills.api.morningnews.collector.model.TencentNewsResponse.NewsItem;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 创建时间：2026/03/17
 * 功能：综合早报采集器，通过腾讯新闻 API 获取综合早报数据。
 * <p>
 * Bean 名称 {@code "general"} 与 YAML 中 {@code skills-api.morning-news.platforms.general} 对应。
 * 作者：Devil
 */
@Slf4j
@Component("general")
public class GeneralMorningNewsCollector extends AbstractTencentNewsCollector {

    @Override
    protected NewsCategory getCategory() {
        return NewsCategory.GENERAL;
    }
}
