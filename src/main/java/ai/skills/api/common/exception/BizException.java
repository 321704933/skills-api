package ai.skills.api.common.exception;

import ai.skills.api.common.api.ResponseCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * 创建时间：2026/03/12
 * 功能：定义业务异常，统一承载错误码、HTTP（超文本传输协议）状态和扩展信息。
 * 作者：Devil
 */
public class BizException extends RuntimeException {

    private final ResponseCode responseCode;
    /**
     * -- GETTER --
     *  功能：获取 HTTP（超文本传输协议）状态。
     *
     * @return HTTP 状态
     */
    @Getter
    private final HttpStatus httpStatus;
    /**
     * -- GETTER --
     *  功能：获取扩展错误详情。
     *
     * @return 扩展详情
     */
    @Getter
    private final Map<String, Object> details;

    /**
     * 功能：使用默认错误信息创建业务异常。
     *
     * @param responseCode 统一错误码
     */
    public BizException(ResponseCode responseCode) {
        this(responseCode, responseCode.defaultMessage(), responseCode.httpStatus(), Map.of());
    }

    /**
     * 功能：使用自定义错误信息创建业务异常。
     *
     * @param responseCode 统一错误码
     * @param message      自定义错误说明
     */
    public BizException(ResponseCode responseCode, String message) {
        this(responseCode, message, responseCode.httpStatus(), Map.of());
    }

    /**
     * 功能：使用扩展详情创建业务异常。
     *
     * @param responseCode 统一错误码
     * @param message      自定义错误说明
     * @param details      扩展明细
     */
    public BizException(ResponseCode responseCode, String message, Map<String, Object> details) {
        this(responseCode, message, responseCode.httpStatus(), details);
    }

    /**
     * 功能：完整创建业务异常。
     *
     * @param responseCode 统一错误码
     * @param message      自定义错误说明
     * @param httpStatus   HTTP（超文本传输协议）状态
     * @param details      扩展明细
     */
    public BizException(ResponseCode responseCode, String message, HttpStatus httpStatus, Map<String, Object> details) {
        super(message);
        this.responseCode = responseCode;
        this.httpStatus = httpStatus;
        this.details = details == null ? Map.of() : Map.copyOf(details);
    }

    /**
     * 功能：获取统一错误码。
     *
     * @return 错误码
     */
    public ResponseCode getErrorCode() {
        return responseCode;
    }

}
