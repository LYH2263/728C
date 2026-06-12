package com.steam.exception;

import com.steam.enums.ErrorCode;

public class SystemException extends BaseException {

    public SystemException(ErrorCode errorCode) {
        super(errorCode);
    }

    public SystemException(ErrorCode errorCode, String customMessage) {
        super(errorCode, customMessage);
    }

    public SystemException(String message, Throwable cause) {
        super(ErrorCode.SYSTEM_ERROR.getCode(), message, ErrorCode.SYSTEM_ERROR.getHttpStatus());
        initCause(cause);
    }

    public SystemException(Throwable cause) {
        super(ErrorCode.SYSTEM_ERROR.getCode(), ErrorCode.SYSTEM_ERROR.getMessage(), ErrorCode.SYSTEM_ERROR.getHttpStatus());
        initCause(cause);
    }

    public static SystemException of(ErrorCode errorCode) {
        return new SystemException(errorCode);
    }

    public static SystemException of(String message, Throwable cause) {
        return new SystemException(message, cause);
    }

    public static SystemException of(Throwable cause) {
        return new SystemException(cause);
    }
}
