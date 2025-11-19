package com.SmartHire.shared.exception.exception;

import com.SmartHire.shared.exception.enums.ErrorCode;

public class AuthenticationException extends BaseException {
    public AuthenticationException(ErrorCode errorCode) {
        super(errorCode);
    }

    public AuthenticationException(ErrorCode errorCode, String customMessage) {
        super(errorCode, customMessage);
    }
}
