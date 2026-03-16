package ai.skills.api.image.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 创建时间：2026/03/16
 * 功能：图片转换配置属性。
 * 作者：Devil
 */
@ConfigurationProperties(prefix = "skills-api.image")
public record ImageConvertProperties(

        /*
          最大输入文件大小（字节），默认 10MB
         */
        Long maxFileSize,

        /*
          最大输出文件大小（字节），默认 50MB
         */
        Long maxOutputSize,

        /*
          默认压缩质量（1-100），默认 85
         */
        Integer defaultQuality

) {

    /**
     * 功能：提供默认配置值。
     */
    public ImageConvertProperties {
        if (maxFileSize == null || maxFileSize <= 0) {
            maxFileSize = 10L * 1024 * 1024;
        }
        if (maxOutputSize == null || maxOutputSize <= 0) {
            maxOutputSize = 50L * 1024 * 1024;
        }
        if (defaultQuality == null || defaultQuality <= 0 || defaultQuality > 100) {
            defaultQuality = 85;
        }
    }
}
