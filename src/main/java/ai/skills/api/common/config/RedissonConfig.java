package ai.skills.api.common.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.spring.starter.RedissonAutoConfigurationCustomizer;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 创建时间：2026/03/12
 * 功能：Redisson 配置类，统一配置 JSON 序列化器，确保存储到 Redis 的数据可读。
 * 作者：Devil
 */
@Configuration
public class RedissonConfig {

    /**
     * 功能：自定义 Redisson 自动配置，使用 JSON 编解码器。
     * 说明：替换默认的 FST 编解码器，使 Redis 中存储的数据为可读的 JSON 格式。
     *
     * @return Redisson 自动配置定制器
     */
    @Bean
    public RedissonAutoConfigurationCustomizer redissonCustomizer() {
        return config -> {
            // 使用 Jackson JSON 编解码器
            config.setCodec(createJsonCodec());
        };
    }

    /**
     * 功能：创建 JSON 编解码器。
     *
     * @return JsonJacksonCodec 实例
     */
    private JsonJacksonCodec createJsonCodec() {
        return new JsonJacksonCodec(createObjectMapper());
    }

    /**
     * 功能：创建配置好的 ObjectMapper 实例。
     * 说明：支持 Java 8 时间类型，启用类型信息以确保反序列化正确。
     *
     * @return ObjectMapper 实例
     */
    private ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // 注册 Java 8 时间模块
        mapper.registerModule(new JavaTimeModule());

        // 禁用日期时间作为时间戳序列化
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 启用默认类型信息，确保多态反序列化正确
        mapper.activateDefaultTyping(
                mapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );

        return mapper;
    }
}
