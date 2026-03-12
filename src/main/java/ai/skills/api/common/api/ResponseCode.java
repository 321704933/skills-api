package ai.skills.api.common.api;

import org.springframework.http.HttpStatus;

/**
 * 创建时间：2026/03/12
 * 功能：定义系统级统一错误码及其默认 HTTP（超文本传输协议）状态。
 * 规范：统一使用三位整形错误码，对齐 HTTP 语义：200 成功，400 请求错误，409 业务冲突，429 请求过频，500 服务异常。
 * 作者：Devil
 */
public enum ResponseCode {
    SUCCESS(200, "请求成功", HttpStatus.OK),
    INVALID_REQUEST(400, "请求参数校验失败", HttpStatus.BAD_REQUEST),
    MESSAGE_NOT_READABLE(400, "请求体格式错误", HttpStatus.BAD_REQUEST),
    IDEMPOTENCY_KEY_REQUIRED(400, "请求缺少幂等键", HttpStatus.BAD_REQUEST),
    IDEMPOTENT_CONFLICT(409, "请求重复提交", HttpStatus.CONFLICT),
    BIZ_ERROR(409, "业务处理失败", HttpStatus.CONFLICT),
    RATE_LIMITED(429, "请求过于频繁", HttpStatus.TOO_MANY_REQUESTS),
    INTERNAL_ERROR(500, "系统内部错误", HttpStatus.INTERNAL_SERVER_ERROR);

    private final Integer code;
    private final String defaultMessage;
    private final HttpStatus httpStatus;

    /**
     * 功能：初始化统一错误码定义。
     *
     * @param code           错误码
     * @param defaultMessage 默认错误说明
     * @param httpStatus     HTTP（超文本传输协议）状态码
     */
    ResponseCode(Integer code, String defaultMessage, HttpStatus httpStatus) {
        this.code = code;
        this.defaultMessage = defaultMessage;
        this.httpStatus = httpStatus;
    }

    /**
     * 功能：获取整形错误码。
     *
     * @return 错误码
     */
    public Integer code() {
        return code;
    }

    /**
     * 功能：获取默认错误说明。
     *
     * @return 默认错误说明
     */
    public String defaultMessage() {
        return defaultMessage;
    }

    /**
     * 功能：获取默认 HTTP（超文本传输协议）状态。
     *
     * @return HTTP 状态
     */
    public HttpStatus httpStatus() {
        return httpStatus;
    }
}
