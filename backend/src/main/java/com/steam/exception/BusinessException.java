package com.steam.exception;

import com.steam.enums.ErrorCode;

public class BusinessException extends BaseException {

    public BusinessException(ErrorCode errorCode) {
        super(errorCode);
    }

    public BusinessException(ErrorCode errorCode, String customMessage) {
        super(errorCode, customMessage);
    }

    public BusinessException(ErrorCode errorCode, Object... args) {
        super(errorCode, errorCode.formatMessage(args));
    }

    public static BusinessException of(ErrorCode errorCode) {
        return new BusinessException(errorCode);
    }

    public static BusinessException of(ErrorCode errorCode, String customMessage) {
        return new BusinessException(errorCode, customMessage);
    }

    public static BusinessException of(ErrorCode errorCode, Object... args) {
        return new BusinessException(errorCode, args);
    }
}
