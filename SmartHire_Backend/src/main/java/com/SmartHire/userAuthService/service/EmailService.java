package com.SmartHire.userAuthService.service;

/**
 * 邮件服务接口
 */
public interface EmailService {

    /**
     * 发送验证码邮件
     *
     * @param toEmail 接收邮箱地址
     * @param code 验证码
     * @throws Exception 发送失败时抛出异常
     */
    void sendVerificationCode(String toEmail, String code) throws Exception;
}