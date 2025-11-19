package com.SmartHire.shared.exception.exception;

import com.SmartHire.shared.exception.enums.ErrorCode;

public class BusinessException extends BaseException {
    public BusinessException(ErrorCode errorCode) {
        super(errorCode);
    }

    public BusinessException(ErrorCode errorCode, String customMessage) {
        super(errorCode, customMessage);
    }
}