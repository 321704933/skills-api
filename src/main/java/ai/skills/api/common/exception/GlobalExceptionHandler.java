package ai.skills.api.common.exception;

import ai.skills.api.common.api.ApiErrorResponse;
import ai.skills.api.common.api.ResponseCode;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * 创建时间：2026/03/12
 * 功能：集中处理控制器抛出的异常，输出统一错误响应。
 * 作者：Devil
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 功能：处理自定义业务异常。
     *
     * @param ex      业务异常
     * @param request 当前请求
     * @return 统一错误响应
     */
    @ExceptionHandler(BizException.class)
    public ResponseEntity<ApiErrorResponse> handleBizException(BizException ex, HttpServletRequest request) {
        return ResponseEntity
                .status(ex.getHttpStatus())
                .body(ApiErrorResponse.of(
                        ex.getErrorCode(),
                        ex.getMessage(),
                        request.getRequestURI(),
                        List.of(),
                        ex.getDetails()
                ));
    }

    /**
     * 功能：处理请求体参数校验失败异常。
     *
     * @param ex      参数校验异常
     * @param request 当前请求
     * @return 统一错误响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        return ResponseEntity
                .status(ResponseCode.INVALID_REQUEST.httpStatus())
                .body(ApiErrorResponse.of(
                        ResponseCode.INVALID_REQUEST,
                        ResponseCode.INVALID_REQUEST.defaultMessage(),
                        request.getRequestURI(),
                        ex.getBindingResult().getFieldErrors().stream().map(this::toValidationError).toList(),
                        null
                ));
    }

    /**
     * 功能：处理绑定参数时的校验异常。
     *
     * @param ex      绑定异常
     * @param request 当前请求
     * @return 统一错误响应
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiErrorResponse> handleBindException(BindException ex, HttpServletRequest request) {
        return ResponseEntity
                .status(ResponseCode.INVALID_REQUEST.httpStatus())
                .body(ApiErrorResponse.of(
                        ResponseCode.INVALID_REQUEST,
                        ResponseCode.INVALID_REQUEST.defaultMessage(),
                        request.getRequestURI(),
                        ex.getBindingResult().getFieldErrors().stream().map(this::toValidationError).toList(),
                        null
                ));
    }

    /**
     * 功能：处理请求体不可读异常，例如 JSON（JavaScript Object Notation，对象表示法）结构非法。
     *
     * @param ex      请求体读取异常
     * @param request 当前请求
     * @return 统一错误响应
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex,
            HttpServletRequest request
    ) {
        return ResponseEntity
                .status(ResponseCode.MESSAGE_NOT_READABLE.httpStatus())
                .body(ApiErrorResponse.of(
                        ResponseCode.MESSAGE_NOT_READABLE,
                        ResponseCode.MESSAGE_NOT_READABLE.defaultMessage(),
                        request.getRequestURI(),
                        List.of(),
                        null
                ));
    }

    /**
     * 功能：兜底处理未显式捕获的异常。
     *
     * @param ex      未知异常
     * @param request 当前请求
     * @return 统一错误响应
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleException(Exception ex, HttpServletRequest request) {
        return ResponseEntity
                .status(ResponseCode.INTERNAL_ERROR.httpStatus())
                .body(ApiErrorResponse.of(
                        ResponseCode.INTERNAL_ERROR,
                        ResponseCode.INTERNAL_ERROR.defaultMessage(),
                        request.getRequestURI(),
                        List.of(),
                        null
                ));
    }

    /**
     * 功能：把 Spring 字段错误转换为统一校验错误结构。
     *
     * @param fieldError 字段错误对象
     * @return 统一字段错误信息
     */
    private ApiErrorResponse.ValidationError toValidationError(FieldError fieldError) {
        return new ApiErrorResponse.ValidationError(fieldError.getField(), fieldError.getDefaultMessage());
    }
}
