package com.SmartHire.userAuthService.service;

/**
 * 验证码服务接口
 */
public interface VerificationCodeService {

    /**
     * 发送验证码到指定邮箱
     *
     * @param email 邮箱地址
     */
    void sendVerificationCode(String email);

    /**
     * 验证用户输入的验证码
     *
     * @param email 邮箱地址
     * @param code  用户输入的验证码
     */
    void verifyCode(String email, String code);
}