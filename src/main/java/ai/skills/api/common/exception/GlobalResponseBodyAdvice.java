package ai.skills.api.common.exception;

import ai.skills.api.common.api.ApiErrorResponse;
import ai.skills.api.common.api.ApiResponse;
import ai.skills.api.common.config.WebProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 创建时间：2026/03/12
 * 功能：统一包装控制器成功响应，确保 API（应用程序编程接口）返回结构一致。
 * 作者：Devil
 */
@ControllerAdvice(annotations = {RestController.class, Controller.class})
public class GlobalResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    private final WebProperties webProperties;
    private final ObjectMapper objectMapper;

    public GlobalResponseBodyAdvice(WebProperties webProperties, ObjectMapper objectMapper) {
        this.webProperties = webProperties;
        this.objectMapper = objectMapper;
    }

    /**
     * 功能：判断当前返回值是否需要进行统一响应包装。
     *
     * @param returnType    控制器返回值描述
     * @param converterType 消息转换器类型
     * @return 是否需要包装
     */
    @Override
    public boolean supports(MethodParameter returnType, @NonNull Class<? extends HttpMessageConverter<?>> converterType) {
        boolean methodProducesBody = returnType.hasMethodAnnotation(ResponseBody.class);
        boolean classIsRestController = returnType.getContainingClass().isAnnotationPresent(RestController.class);
        return webProperties.isWrapSuccessResponse() && (methodProducesBody || classIsRestController);
    }

    /**
     * 功能：在响应写出前统一包装为标准成功结构。
     *
     * @param body                  原始响应体
     * @param returnType            控制器返回值描述
     * @param selectedContentType   已选择的内容类型
     * @param selectedConverterType 已选择的消息转换器
     * @param request               当前请求
     * @param response              当前响应
     * @return 包装后的响应体
     */
    @Override
    public Object beforeBodyWrite(
            Object body,
            @NonNull MethodParameter returnType,
            @NonNull MediaType selectedContentType,
            @NonNull Class<? extends HttpMessageConverter<?>> selectedConverterType,
            @NonNull ServerHttpRequest request,
            @NonNull ServerHttpResponse response
    ) {
        if (body instanceof ApiResponse<?> || body instanceof ApiErrorResponse) {
            return body;
        }

        if (body instanceof byte[] || body instanceof org.springframework.core.io.Resource) {
            return body;
        }

        ApiResponse<Object> wrapped = ApiResponse.success(body);
        if (StringHttpMessageConverter.class.isAssignableFrom(selectedConverterType)) {
            try {
                return objectMapper.writeValueAsString(wrapped);
            } catch (JsonProcessingException ex) {
                throw new IllegalStateException("响应序列化失败", ex);
            }
        }
        return wrapped;
    }
}
