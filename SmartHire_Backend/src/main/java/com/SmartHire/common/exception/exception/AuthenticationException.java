package com.SmartHire.common.exception.exception;

import com.SmartHire.common.exception.enums.ErrorCode;

public class AuthenticationException extends BaseException {
  public AuthenticationException(ErrorCode errorCode) {
    super(errorCode);
  }

  public AuthenticationException(ErrorCode errorCode, String customMessage) {
    super(errorCode, customMessage);
  }
}
