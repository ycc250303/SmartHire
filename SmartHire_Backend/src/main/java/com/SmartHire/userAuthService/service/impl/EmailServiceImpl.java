package com.SmartHire.userAuthService.service.impl;

import com.SmartHire.userAuthService.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** 邮件服务实现类 - 简单实现，仅记录日志 */
@Service
@Slf4j
public class EmailServiceImpl implements EmailService {

  @Override
  public void sendVerificationCode(String toEmail, String code) throws Exception {
    // TODO: 实现真实的邮件发送功能
    // 目前仅记录日志，供开发测试使用
    log.info("【模拟邮件发送】发送验证码到邮箱: {}, 验证码: {}", toEmail, code);
    log.warn("当前为Mock实现，未发送真实邮件。生产环境请配置SMTP服务器");
  }
}