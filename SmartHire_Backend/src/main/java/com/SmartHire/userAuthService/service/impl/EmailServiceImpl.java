package com.SmartHire.userAuthService.service.impl;

import com.SmartHire.userAuthService.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** 邮件服务实现类（空实现，用于开发环境） */
@Service
@Slf4j
public class EmailServiceImpl implements EmailService {

  @Override
  public void sendVerificationCode(String toEmail, String code) throws Exception {
    // 空实现：仅记录日志，不实际发送邮件
    // 这样可以避免邮件配置问题导致应用无法启动
    log.warn("邮件服务未配置，验证码发送功能已禁用。邮箱: {}, 验证码: {}", toEmail, code);
    log.info("如需启用邮件功能，请配置 application.yml 中的 spring.mail 相关配置");
    // 不抛出异常，允许应用继续运行
  }
}

