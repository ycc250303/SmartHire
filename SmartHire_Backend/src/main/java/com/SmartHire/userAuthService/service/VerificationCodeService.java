package com.SmartHire.userAuthService.service;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * 验证码服务接口
 */
public interface VerificationCodeService {

    /**
     * 发送验证码到指定邮箱
     *
     * @param email 邮箱地址（使用 @Email 注解自动验证格式）
     */
    void sendVerificationCode(@Email(message = "邮箱格式不正确") String email);

    /**
     * 验证用户输入的验证码（验证成功后立即删除验证码，防止重复使用）
     *
     * @param email 邮箱地址（使用 @Email 注解自动验证格式）
     * @param code  用户输入的验证码（使用 @NotBlank 注解自动验证非空）
     */
    void verifyCode(@Email(message = "邮箱格式不正确") String email,
            @NotBlank(message = "验证码不能为空") String code);

    /**
     * 验证用户输入的验证码（验证成功后不删除验证码，用于注册等场景，注册成功后再删除）
     *
     * @param email 邮箱地址（使用 @Email 注解自动验证格式）
     * @param code  用户输入的验证码（使用 @NotBlank 注解自动验证非空）
     */
    void verifyCodeWithoutDelete(@Email(message = "邮箱格式不正确") String email,
            @NotBlank(message = "验证码不能为空") String code);

    /**
     * 删除指定邮箱的验证码
     *
     * @param email 邮箱地址
     */
    void deleteCode(String email);
}