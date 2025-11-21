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
     * 验证用户输入的验证码（验证成功后立即删除验证码，防止重复使用）
     *
     * @param email 邮箱地址
     * @param code  用户输入的验证码
     */
    void verifyCode(String email, String code);

    /**
     * 验证用户输入的验证码（验证成功后不删除验证码，用于注册等场景，注册成功后再删除）
     *
     * @param email 邮箱地址
     * @param code  用户输入的验证码
     */
    void verifyCodeWithoutDelete(String email, String code);

    /**
     * 删除指定邮箱的验证码
     *
     * @param email 邮箱地址
     */
    void deleteCode(String email);
}