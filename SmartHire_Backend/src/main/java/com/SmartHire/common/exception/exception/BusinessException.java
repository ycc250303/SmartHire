package com.SmartHire.common.exception.exception;

import com.SmartHire.common.exception.enums.ErrorCode;

public class BusinessException extends BaseException {
  public BusinessException(ErrorCode errorCode) {
    super(errorCode);
  }

  public BusinessException(ErrorCode errorCode, String customMessage) {
    super(errorCode, customMessage);
  }
}
