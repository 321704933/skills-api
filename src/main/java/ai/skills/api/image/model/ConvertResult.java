package ai.skills.api.image.model;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 创建时间：2026/03/16
 * 功能：图片转换结果（Base64 输出模式）。
 * 作者：Devil
 */
@Schema(name = "图片转换结果", description = "Base64 模式下的转换结果")
public record ConvertResult(

        @Schema(description = "Base64 编码的图片内容", example = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAAB...")
        String base64,

        @Schema(description = "输出格式", example = "PNG")
        String format,

        @Schema(description = "输出宽度（像素）", example = "800")
        Integer width,

        @Schema(description = "输出高度（像素）", example = "600")
        Integer height,

        @Schema(description = "文件大小（字节）", example = "12345")
        long size

) {
}
