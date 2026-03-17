package ai.skills.api.image.controller;

import ai.skills.api.image.model.ConvertResult;
import ai.skills.api.image.model.OutputFormat;
import ai.skills.api.image.model.OutputMode;
import ai.skills.api.image.service.ImageConvertService;
import ai.skills.api.image.service.ImageConvertService.ConversionContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 创建时间：2026/03/16
 * 功能：图片转换接口，支持格式转换、尺寸调整、质量控制。
 * 作者：Devil
 */
@Tag(name = "图片转换")
@RestController
@RequestMapping("/api/v1/image")
public class ImageConvertController {

    private static final DateTimeFormatter FILENAME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private final ImageConvertService imageConvertService;

    public ImageConvertController(ImageConvertService imageConvertService) {
        this.imageConvertService = imageConvertService;
    }

    /**
     * 功能：转换图片格式，支持 SVG 转 PNG/JPG/WEBP 以及光栅图片之间的互转。
     *
     * @param file    上传的图片文件
     * @param base64  Base64 编码的图片内容
     * @param url     图片 URL 地址
     * @param format  目标格式（PNG/JPG/WEBP）
     * @param width   输出宽度（像素）
     * @param height  输出高度（像素）
     * @param quality 压缩质量（1-100）
     * @param output  输出方式（binary/base64）
     * @return 转换后的图片
     */
    @Operation(
            summary = "转换图片格式",
            description = "支持 SVG 转 PNG/JPG/WEBP，以及 PNG/JPG/WEBP 之间的互转。" +
                    "可通过 file、base64 或 url 参数提供图片（三选一）。" +
                    "支持调整输出尺寸和质量。"
    )
    @PostMapping(value = "/convert", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> convert(
            @Parameter(description = "上传的图片文件")
            @RequestParam(required = false) MultipartFile file,

            @Parameter(description = "Base64 编码的图片内容")
            @RequestParam(required = false) String base64,

            @Parameter(description = "图片 URL 地址")
            @RequestParam(required = false) String url,

            @Parameter(description = "目标格式（PNG/JPG/WEBP）", required = true)
            @RequestParam String format,

            @Parameter(description = "输出宽度（像素）")
            @RequestParam(required = false) Integer width,

            @Parameter(description = "输出高度（像素）")
            @RequestParam(required = false) Integer height,

            @Parameter(description = "压缩质量（1-100），默认 85")
            @RequestParam(required = false) Integer quality,

            @Parameter(description = "输出方式（binary/base64），默认 binary")
            @RequestParam(required = false, defaultValue = "binary") String output
    ) {
        // 解析目标格式
        OutputFormat outputFormat = OutputFormat.fromString(format);
        if (outputFormat == null) {
            return ResponseEntity.badRequest()
                    .body("不支持的目标格式: " + format + "，支持的格式: PNG, JPG, WEBP");
        }

        // 解析输出方式
        OutputMode outputMode = OutputMode.fromString(output);

        // 执行转换
        ConversionContext context = imageConvertService.convert(
                file, base64, url, outputFormat, width, height, quality, outputMode
        );

        // 根据输出模式返回结果
        if (outputMode == OutputMode.BASE64) {
            ConvertResult result = imageConvertService.toBase64Result(context);
            return ResponseEntity.ok(result);
        } else {
            // 二进制输出，文件名包含时间戳
            String timestamp = LocalDateTime.now().format(FILENAME_FORMATTER);
            String filename = "converted_" + timestamp + "." + outputFormat.getExtension();
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(outputFormat.getContentType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .body(context.bytes());
        }
    }
}
