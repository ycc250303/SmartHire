package com.SmartHire.common.exception.handler;

import com.SmartHire.common.exception.exception.AdminServiceException;
import com.SmartHire.common.entity.Result;
import com.SmartHire.common.exception.enums.ErrorCode;
import com.SmartHire.common.exception.exception.AuthenticationException;
import com.SmartHire.common.exception.exception.BusinessException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 全局异常处理器 统一处理所有异常，只返回 ErrorCode 中的 message 信息
 *
 * @author SmartHire Team
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e) {
        log.warn("业务异常: code={}, message={}", e.getCode(), e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理认证异常
     */
    @ExceptionHandler(AuthenticationException.class)
    public Result<?> handleAuthenticationException(AuthenticationException e) {
        log.warn("认证异常: code={}, message={}", e.getCode(), e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理管理员服务异常
     */
    @ExceptionHandler(AdminServiceException.class)
    public Result<?> handleAdminServiceException(AdminServiceException e) {
        log.warn("管理员服务异常: code={}, message={}", e.getCode(), e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理参数校验异常（@RequestBody 和 @ModelAttribute 的验证失败） 统一转换为 BusinessException，使用统一的错误码
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleValidationException(MethodArgumentNotValidException e) {
        // 合并所有错误消息
        String message =
                e.getBindingResult().getFieldErrors().stream()
                        .map(FieldError::getDefaultMessage)
                        .collect(Collectors.joining("；"));

        log.warn("参数校验异常: {}", message);

        // 统一转换为 BusinessException，使用 VALIDATION_ERROR 错误码
        // 这样前端只需要处理 BusinessException 一种异常类型
        return Result.error(ErrorCode.VALIDATION_ERROR.getCode(), message);
    }

    /**
     * 处理方法参数校验异常（@RequestParam、@PathVariable 等参数的验证失败） 统一转换为 BusinessException，使用统一的错误码
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<?> handleConstraintViolationException(ConstraintViolationException e) {
        // 合并所有错误消息
        String message =
                e.getConstraintViolations().stream()
                        .map(ConstraintViolation::getMessage)
                        .collect(Collectors.joining("；"));

        log.warn("方法参数校验异常: {}", message);

        // 统一转换为 BusinessException，使用 VALIDATION_ERROR 错误码
        return Result.error(ErrorCode.VALIDATION_ERROR.getCode(), message);
    }

    /**
     * 处理请求体缺失或格式错误异常
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<?> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        String message = "请求参数格式错误，请检查请求体是否为有效的JSON格式";

        // 如果是请求体缺失
        if (e.getMessage() != null && e.getMessage().contains("Required request body is missing")) {
            message = "请求体不能为空，请提供JSON格式的请求参数";
        }
        // 如果是JSON格式错误
        else if (e.getMessage() != null && e.getMessage().contains("JSON parse error")) {
            message = "JSON格式错误，请检查请求体格式";
        }

        log.warn("请求体异常: {}", e.getMessage());
        return Result.error(ErrorCode.VALIDATION_ERROR.getCode(), message);
    }

    /**
     * 处理系统异常（兜底）
     */
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.error(ErrorCode.SYSTEM_ERROR.getCode(), "系统内部错误，请联系管理员");
    }

    @ExceptionHandler({
            NoHandlerFoundException.class,
            NoResourceFoundException.class
    })
    public Result<?> handleNotFound(Exception e) {
        log.debug("接口不存在: {}", e.getMessage());

        return Result.error(ErrorCode.NOT_FOUND.getCode(), "接口不存在");
    }


    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Result<?> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {

        log.warn("请求方式不支持: {}", e.getMethod());

        return Result.error(
                ErrorCode.METHOD_NOT_ALLOWED.getCode(),
                "不支持的请求方式: " + e.getMethod()
        );
    }


    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result<?> handleMissingParam(MissingServletRequestParameterException e) {

        log.warn("缺少请求参数: {}", e.getParameterName());
        return Result.error(
                ErrorCode.VALIDATION_ERROR.getCode(),
                "缺少请求参数: " + e.getParameterName()
        );
    }

}
