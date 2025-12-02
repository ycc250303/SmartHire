package com.SmartHire.shared.entity;

import com.SmartHire.shared.exception.enums.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Result<T> {
  private Integer code;
  private String message;
  private T data;

  public static Result success() {
    return new Result<>(0, "操作成功", null);
  }

  public static Result success(String message) {
    return new Result<>(0, message, null);
  }

  public static <E> Result<E> success(E data) {
    return new Result<>(0, "操作成功", data);
  }

  public static <E> Result<E> success(String message, E data) {
    return new Result<>(0, message, data);
  }

  public static <E> Result<E> error(Integer code, String message) {
    return new Result<>(code, message, null);
  }

  public static <E> Result<E> error(ErrorCode errorCode) {
    return new Result<>(errorCode.getCode(), errorCode.getMessage(), null);
  }

  public static <E> Result<E> error(ErrorCode errorCode, String message) {
    return new Result<>(errorCode.getCode(), message, null);
  }
}
