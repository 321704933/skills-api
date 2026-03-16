package ai.skills.api.image.service;

import ai.skills.api.common.api.ResponseCode;
import ai.skills.api.common.exception.BizException;
import ai.skills.api.image.config.ImageConvertProperties;
import ai.skills.api.image.model.ConvertResult;
import ai.skills.api.image.model.OutputFormat;
import ai.skills.api.image.model.OutputMode;
import lombok.extern.slf4j.Slf4j;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.transcoder.image.JPEGTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;
import java.util.Set;

/**
 * 创建时间：2026/03/16
 * 功能：图片转换服务，支持 SVG/光栅图片格式转换、尺寸调整、质量控制。
 * 作者：Devil
 */
@Slf4j
@Service
public class ImageConvertService {

    private static final Set<String> SVG_CONTENT_TYPES = Set.of(
            "image/svg+xml",
            "application/svg+xml",
            "text/xml",
            "application/xml"
    );

    private static final Set<String> SVG_EXTENSIONS = Set.of(".svg", ".svgz");

    private final ImageConvertProperties properties;
    private final HttpClient httpClient;

    public ImageConvertService(ImageConvertProperties properties) {
        this.properties = properties;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    /**
     * 功能：执行图片转换。
     *
     * @param file    上传的文件
     * @param base64  Base64 编码的图片内容
     * @param url     图片 URL
     * @param format  目标格式
     * @param width   输出宽度
     * @param height  输出高度
     * @param quality 压缩质量（1-100）
     * @param output  输出方式
     * @return 转换结果（Base64 模式）或 null（二进制模式需通过 getOutputBytes 获取）
     */
    public ConversionContext convert(MultipartFile file, String base64, String url,
                                     OutputFormat format, Integer width, Integer height,
                                     Integer quality, OutputMode output) {
        // 获取输入数据
        byte[] inputData = getInputData(file, base64, url);
        String sourceFormat = detectFormat(inputData, file, url);

        // 验证输入大小
        if (inputData.length > properties.maxFileSize()) {
            throw new BizException(ResponseCode.BIZ_ERROR, "输入文件大小超出限制，最大允许 " +
                    (properties.maxFileSize() / 1024 / 1024) + "MB");
        }

        // 获取有效质量值
        int effectiveQuality = quality != null ? quality : properties.defaultQuality();
        effectiveQuality = Math.clamp(effectiveQuality, 1, 100);

        // 执行转换
        byte[] outputBytes;
        int outputWidth = 0;
        int outputHeight = 0;

        if (isSvg(sourceFormat, inputData)) {
            // SVG 转换
            var svgResult = convertSvg(inputData, format, width, height, effectiveQuality);
            outputBytes = svgResult.bytes();
            outputWidth = svgResult.width();
            outputHeight = svgResult.height();
        } else {
            // 光栅图片转换
            var rasterResult = convertRaster(inputData, format, width, height, effectiveQuality);
            outputBytes = rasterResult.bytes();
            outputWidth = rasterResult.width();
            outputHeight = rasterResult.height();
        }

        // 验证输出大小
        if (outputBytes.length > properties.maxOutputSize()) {
            throw new BizException(ResponseCode.BIZ_ERROR, "输出文件大小超出限制，最大允许 " +
                    (properties.maxOutputSize() / 1024 / 1024) + "MB");
        }

        return new ConversionContext(outputBytes, format, outputWidth, outputHeight, output);
    }

    /**
     * 功能：从转换上下文获取 Base64 结果。
     */
    public ConvertResult toBase64Result(ConversionContext context) {
        String base64 = Base64.getEncoder().encodeToString(context.bytes());
        return new ConvertResult(
                base64,
                context.format().name(),
                context.width(),
                context.height(),
                context.bytes().length
        );
    }

    /**
     * 功能：获取输入数据。
     */
    private byte[] getInputData(MultipartFile file, String base64, String url) {
        int sourceCount = (file != null && !file.isEmpty() ? 1 : 0)
                + (base64 != null && !base64.isBlank() ? 1 : 0)
                + (url != null && !url.isBlank() ? 1 : 0);

        if (sourceCount == 0) {
            throw new BizException(ResponseCode.INVALID_REQUEST, "必须提供 file、base64 或 url 参数之一");
        }
        if (sourceCount > 1) {
            throw new BizException(ResponseCode.INVALID_REQUEST, "file、base64、url 参数只能提供一个");
        }

        try {
            if (file != null && !file.isEmpty()) {
                return file.getBytes();
            }
            if (base64 != null && !base64.isBlank()) {
                // 移除可能存在的 data URL 前缀
                String cleanBase64 = base64;
                if (base64.contains(",")) {
                    cleanBase64 = base64.substring(base64.indexOf(",") + 1);
                }
                return Base64.getDecoder().decode(cleanBase64);
            }
            if (url != null && !url.isBlank()) {
                return downloadFromUrl(url);
            }
        } catch (IOException e) {
            throw new BizException(ResponseCode.BIZ_ERROR, "读取输入数据失败: " + e.getMessage());
        }

        throw new BizException(ResponseCode.BIZ_ERROR, "无法获取输入数据");
    }

    /**
     * 功能：从 URL 下载图片。
     */
    private byte[] downloadFromUrl(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(30))
                    .GET()
                    .build();

            HttpResponse<byte[]> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofByteArray());

            if (response.statusCode() >= 400) {
                throw new BizException(ResponseCode.BIZ_ERROR, "下载图片失败，HTTP 状态码: " + response.statusCode());
            }

            return response.body();
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            throw new BizException(ResponseCode.BIZ_ERROR, "下载图片失败: " + e.getMessage());
        }
    }

    /**
     * 功能：检测输入格式。
     */
    private String detectFormat(byte[] data, MultipartFile file, String url) {
        // 检查文件扩展名
        String filename = null;
        if (file != null && file.getOriginalFilename() != null) {
            filename = file.getOriginalFilename();
        } else if (url != null) {
            int queryIndex = url.indexOf('?');
            String path = queryIndex > 0 ? url.substring(0, queryIndex) : url;
            int slashIndex = path.lastIndexOf('/');
            if (slashIndex >= 0) {
                filename = path.substring(slashIndex + 1);
            }
        }

        if (filename != null) {
            String lowerName = filename.toLowerCase();
            for (String ext : SVG_EXTENSIONS) {
                if (lowerName.endsWith(ext)) {
                    return "svg";
                }
            }
        }

        // 检查 Content-Type
        if (file != null && file.getContentType() != null) {
            if (SVG_CONTENT_TYPES.contains(file.getContentType().toLowerCase())) {
                return "svg";
            }
        }

        // 检查文件内容（SVG 通常以 <svg 或 <?xml 开头）
        String header = new String(data, 0, Math.min(100, data.length)).trim();
        if (header.startsWith("<svg") || header.startsWith("<?xml") && header.contains("<svg")) {
            return "svg";
        }

        // 检查图片格式的魔数
        if (data.length >= 8) {
            // PNG: 89 50 4E 47
            if (data[0] == (byte) 0x89 && data[1] == 0x50 && data[2] == 0x4E && data[3] == 0x47) {
                return "png";
            }
            // JPEG: FF D8 FF
            if (data[0] == (byte) 0xFF && data[1] == (byte) 0xD8 && data[2] == (byte) 0xFF) {
                return "jpg";
            }
            // WebP: 52 49 46 46 ... 57 45 42 50
            if (data.length >= 12 && data[0] == 0x52 && data[1] == 0x49 && data[2] == 0x46 && data[3] == 0x46
                    && data[8] == 0x57 && data[9] == 0x45 && data[10] == 0x42 && data[11] == 0x50) {
                return "webp";
            }
        }

        return "unknown";
    }

    /**
     * 功能：判断是否为 SVG。
     */
    private boolean isSvg(String format, byte[] data) {
        return "svg".equals(format);
    }

    /**
     * 功能：转换 SVG 图片。
     */
    private SvgConversionResult convertSvg(byte[] svgData, OutputFormat format,
                                           Integer width, Integer height, int quality) {
        try {
            ByteArrayInputStream input = new ByteArrayInputStream(svgData);
            TranscoderInput transcoderInput = new TranscoderInput(input);

            // 首先获取 SVG 的原始尺寸（用于计算等比缩放）
            Dimension originalSize = getSvgDimension(svgData);
            int targetWidth = width != null ? width : (int) originalSize.getWidth();
            int targetHeight = height != null ? height : (int) originalSize.getHeight();

            // 如果只指定了一个维度，按比例计算另一个
            if (width != null && height == null) {
                double scale = width / originalSize.getWidth();
                targetHeight = (int) (originalSize.getHeight() * scale);
            } else if (height != null && width == null) {
                double scale = height / originalSize.getHeight();
                targetWidth = (int) (originalSize.getWidth() * scale);
            }

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            TranscoderOutput transcoderOutput = new TranscoderOutput(output);

            // 创建合适的 Transcoder
            ImageTranscoder transcoder = createTranscoder(format, quality, targetWidth, targetHeight);
            transcoder.transcode(transcoderInput, transcoderOutput);

            return new SvgConversionResult(output.toByteArray(), targetWidth, targetHeight);
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.error("SVG 转换失败: {}", e.getMessage(), e);
            throw new BizException(ResponseCode.BIZ_ERROR, "SVG 转换失败: " + e.getMessage());
        }
    }

    /**
     * 功能：获取 SVG 原始尺寸。
     */
    private Dimension getSvgDimension(byte[] svgData) {
        // 默认尺寸
        int defaultWidth = 800;
        int defaultHeight = 600;

        try {
            String svgContent = new String(svgData);
            // 尝试解析 width 和 height 属性
            int width = extractNumericAttribute(svgContent, "width", defaultWidth);
            int height = extractNumericAttribute(svgContent, "height", defaultHeight);
            return new Dimension(width, height);
        } catch (Exception e) {
            return new Dimension(defaultWidth, defaultHeight);
        }
    }

    /**
     * 功能：从 SVG 内容中提取数值属性。
     */
    private int extractNumericAttribute(String svgContent, String attrName, int defaultValue) {
        // 匹配 width="100" 或 width='100' 或 width=100
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
                attrName + "\\s*=\\s*[\"']?(\\d+(?:\\.\\d+)?)");
        java.util.regex.Matcher matcher = pattern.matcher(svgContent);
        if (matcher.find()) {
            try {
                return (int) Double.parseDouble(matcher.group(1));
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    /**
     * 功能：创建对应格式的 Transcoder。
     */
    private ImageTranscoder createTranscoder(OutputFormat format, int quality,
                                             int width, int height) {
        return switch (format) {
            case PNG -> {
                PNGTranscoder transcoder = new PNGTranscoder();
                transcoder.addTranscodingHint(PNGTranscoder.KEY_WIDTH, (float) width);
                transcoder.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, (float) height);
                yield transcoder;
            }
            case JPG -> {
                JPEGTranscoder transcoder = new JPEGTranscoder();
                transcoder.addTranscodingHint(JPEGTranscoder.KEY_WIDTH, (float) width);
                transcoder.addTranscodingHint(JPEGTranscoder.KEY_HEIGHT, (float) height);
                transcoder.addTranscodingHint(JPEGTranscoder.KEY_QUALITY, quality / 100f);
                yield transcoder;
            }
            case WEBP -> {
                // Batik 不原生支持 WebP，先转 PNG 再转 WebP
                // 这里先使用 PNG，后续在光栅转换中处理
                PNGTranscoder transcoder = new PNGTranscoder();
                transcoder.addTranscodingHint(PNGTranscoder.KEY_WIDTH, (float) width);
                transcoder.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, (float) height);
                yield transcoder;
            }
        };
    }

    /**
     * 功能：转换光栅图片（PNG/JPG/WEBP）。
     */
    private RasterConversionResult convertRaster(byte[] inputData, OutputFormat format,
                                                 Integer width, Integer height, int quality) {
        try {
            BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(inputData));
            if (originalImage == null) {
                throw new BizException(ResponseCode.BIZ_ERROR, "无法解析图片，可能是不支持的格式");
            }

            int originalWidth = originalImage.getWidth();
            int originalHeight = originalImage.getHeight();

            // 计算目标尺寸
            int targetWidth = width != null ? width : originalWidth;
            int targetHeight = height != null ? height : originalHeight;

            // 如果只指定了一个维度，按比例计算另一个
            if (width != null && height == null) {
                double scale = (double) width / originalWidth;
                targetHeight = (int) (originalHeight * scale);
            } else if (height != null && width == null) {
                double scale = (double) height / originalHeight;
                targetWidth = (int) (originalWidth * scale);
            }

            // 缩放图片
            BufferedImage scaledImage = scaleImage(originalImage, targetWidth, targetHeight, format);

            // 转换为目标格式
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            String formatName = format.getExtension();

            // WebP 需要特殊处理（JDK 原生不支持，使用 PNG 代替或提示）
            if (format == OutputFormat.WEBP) {
                // 由于 JDK ImageIO 不原生支持 WebP，这里先输出 PNG
                // 如果需要 WebP 支持，需要添加 webp-imageio 依赖
                ImageIO.write(scaledImage, "png", output);
                log.warn("WebP 输出暂不支持，已转换为 PNG。如需 WebP 支持请添加 webp-imageio 依赖");
                formatName = "png";
            } else if (format == OutputFormat.JPG) {
                // JPG 不支持透明，需要转换为 RGB
                BufferedImage rgbImage = scaledImage;
                if (scaledImage.getType() != BufferedImage.TYPE_INT_RGB) {
                    rgbImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
                    Graphics2D g2d = rgbImage.createGraphics();
                    g2d.setColor(Color.WHITE);
                    g2d.fillRect(0, 0, targetWidth, targetHeight);
                    g2d.drawImage(scaledImage, 0, 0, null);
                    g2d.dispose();
                }
                // 使用自定义质量压缩
                writeJpegWithQuality(rgbImage, output, quality);
            } else {
                ImageIO.write(scaledImage, formatName, output);
            }

            return new RasterConversionResult(output.toByteArray(), targetWidth, targetHeight);
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.error("图片转换失败: {}", e.getMessage(), e);
            throw new BizException(ResponseCode.BIZ_ERROR, "图片转换失败: " + e.getMessage());
        }
    }

    /**
     * 功能：缩放图片。
     */
    private BufferedImage scaleImage(BufferedImage original, int width, int height, OutputFormat format) {
        int imageType = format == OutputFormat.JPG ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
        BufferedImage scaled = new BufferedImage(width, height, imageType);
        Graphics2D g2d = scaled.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (format == OutputFormat.JPG) {
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, width, height);
        }

        g2d.drawImage(original, 0, 0, width, height, null);
        g2d.dispose();
        return scaled;
    }

    /**
     * 功能：以指定质量写入 JPEG。
     */
    private void writeJpegWithQuality(BufferedImage image, ByteArrayOutputStream output, int quality) throws IOException {
        // 使用 ImageWriter 设置压缩质量
        var writers = ImageIO.getImageWritersByFormatName("jpg");
        if (writers.hasNext()) {
            var writer = writers.next();
            var writeParam = writer.getDefaultWriteParam();
            writeParam.setCompressionMode(javax.imageio.ImageWriteParam.MODE_EXPLICIT);
            writeParam.setCompressionQuality(quality / 100f);

            try (var ios = ImageIO.createImageOutputStream(output)) {
                writer.setOutput(ios);
                writer.write(null, new javax.imageio.IIOImage(image, null, null), writeParam);
            } finally {
                writer.dispose();
            }
        } else {
            ImageIO.write(image, "jpg", output);
        }
    }

    /**
     * 功能：转换上下文，保存转换结果和元数据。
     */
    public record ConversionContext(
            byte[] bytes,
            OutputFormat format,
            int width,
            int height,
            OutputMode outputMode
    ) {
    }

    private record SvgConversionResult(byte[] bytes, int width, int height) {
    }

    private record RasterConversionResult(byte[] bytes, int width, int height) {
    }
}
