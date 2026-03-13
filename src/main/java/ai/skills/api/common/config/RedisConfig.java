package ai.skills.api.common.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.extern.slf4j.Slf4j;
import org.redisson.client.codec.StringCodec;
import org.redisson.codec.CompositeCodec;
import org.redisson.codec.TypedJsonJacksonCodec;
import org.redisson.spring.starter.RedissonAutoConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

/**
 * Redis 配置类
 * <p>
 * 创建时间：2026/03/13
 * 功能：统一配置 Redisson 序列化器，确保存储到 Redis 的数据为可读的 JSON 格式
 * 作者：Devil
 * </p>
 */
@Slf4j
@Configuration
public class RedisConfig {

    /**
     * Redisson 自定义配置
     * <p>
     * 核心配置说明：
     * 1. 使用 CompositeCodec 组合编解码器
     *    - Key 使用 StringCodec：保证 Redis 中的 key 为普通字符串，便于查看和调试
     *    - Value 使用 TypedJsonJacksonCodec：保证 value 为可读的 JSON 格式
     * 2. 支持 Java 8 时间类型（LocalDateTime）的序列化和反序列化
     * 3. 启用 Lua 脚本缓存，减少网络传输（Redisson 大部分功能基于 Lua 脚本实现）
     * </p>
     *
     * @return RedissonAutoConfigurationCustomizer 自定义配置器
     */
    @Bean
    public RedissonAutoConfigurationCustomizer redissonCustomizer() {
        return config -> {
            // ========== Jackson 配置 ==========

            // 创建 JavaTimeModule 用于处理 Java 8 时间类型
            JavaTimeModule javaTimeModule = new JavaTimeModule();
            // 设置 LocalDateTime 的日期时间格式
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            // 添加 LocalDateTime 序列化器
            javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(formatter));
            // 添加 LocalDateTime 反序列化器
            javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(formatter));

            // 创建 ObjectMapper 并进行配置
            ObjectMapper om = new ObjectMapper();
            // 注册 JavaTimeModule 以支持 Java 8 时间类型
            om.registerModule(javaTimeModule);
            // 设置时区为系统默认时区
            om.setTimeZone(TimeZone.getDefault());
            // 设置所有属性的可见性（包括 private 属性）
            om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
            /*
             * 自定义类型解析器：让 record 类型也包含类型信息
             *
             * 原因：Java record 是隐式 final 的，NON_FINAL 策略默认不会为 final 类型
             *       添加类名包装，导致 record 序列化后无类型信息，反序列化时报错：
             *       "expected START_ARRAY, need WRAPPER_ARRAY type information"
             *
             * 解决：重写 useForType()，对 record 类型返回 true，强制包含类型信息
             */
            ObjectMapper.DefaultTypeResolverBuilder typer = new ObjectMapper.DefaultTypeResolverBuilder(
                    ObjectMapper.DefaultTyping.NON_FINAL,
                    LaissezFaireSubTypeValidator.instance
            ) {
                @Override
                public boolean useForType(JavaType t) {
                    if (t.isRecordType()) {
                        return true;
                    }
                    return super.useForType(t);
                }
            };
            typer.init(JsonTypeInfo.Id.CLASS, null);
            typer.inclusion(JsonTypeInfo.As.WRAPPER_ARRAY);
            om.setDefaultTyping(typer);

            // ========== 编解码器配置 ==========

            // 创建 JSON 编解码器，用于 value 的序列化和反序列化
            TypedJsonJacksonCodec jsonCodec = new TypedJsonJacksonCodec(Object.class, om);

            /*
             * 组合编解码器配置
             * - StringCodec.INSTANCE：用于 key 的编解码，保证 key 为普通字符串
             * - jsonCodec：用于 value 的编解码，保证 value 为可读的 JSON 格式
             * 这样在 Redis 中存储的数据就是可读的 JSON 格式，而不是二进制数据
             */
            CompositeCodec codec = new CompositeCodec(StringCodec.INSTANCE, jsonCodec, jsonCodec);

            // ========== Redisson 客户端配置 ==========

            config
                    // 启用 Lua 脚本缓存，减少网络传输（Redisson 大部分功能基于 Lua 脚本实现）
                    .setUseScriptCache(true)
                    // 设置组合编解码器
                    .setCodec(codec);

            log.info("初始化 Redis 配置完成，使用 CompositeCodec（Key: String, Value: JSON）");
        };
    }
}
