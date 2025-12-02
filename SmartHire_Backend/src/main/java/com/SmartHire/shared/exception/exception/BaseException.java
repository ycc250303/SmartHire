package com.SmartHire.shared.exception.exception;

import com.SmartHire.shared.exception.enums.ErrorCode;

public class BaseException extends RuntimeException {
  private final Integer code;
  private final String message;

  // 使用错误码枚举的构造方法
  public BaseException(ErrorCode errorCode) {
    this.code = errorCode.getCode();
    this.message = errorCode.getMessage();
  }

  // 使用错误码枚举 + 自定义消息的构造方法
  public BaseException(ErrorCode errorCode, String customMessage) {
    this.code = errorCode.getCode();
    this.message = customMessage;
  }

  public Integer getCode() {
    return code;
  }

  public String getMessage() {
    return message;
  }
}
