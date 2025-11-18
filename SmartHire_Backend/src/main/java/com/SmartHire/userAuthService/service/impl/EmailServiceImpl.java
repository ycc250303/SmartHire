package com.SmartHire.userAuthService.service.impl;
import com.SmartHire.userAuthService.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * 邮件服务实现类
 */
@Service
@Slf4j
public class EmailServiceImpl implements EmailService {
    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.from}")
    private String fromEmail;

    @Override
    public void sendVerificationCode(String toEmail, String code) throws Exception {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // 设置邮件信息
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("SmartHire 邮箱验证码");

            // 构建邮件内容（HTML格式）
            String content = buildEmailContent(code);
            helper.setText(content, true); // true 表示支持HTML

            // 发送邮件
            mailSender.send(message);
            log.info("验证码邮件发送成功，接收邮箱: {}", toEmail);

        } catch (Exception e) {
            log.error("验证码邮件发送失败，接收邮箱: {}, 错误信息: {}", toEmail, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 构建邮件内容（HTML格式）
     *
     * @param code 验证码
     * @return HTML格式的邮件内容
     */
    private String buildEmailContent(String code) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<meta charset='UTF-8'>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
                ".container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
                ".code-box { background-color: #f4f4f4; padding: 20px; text-align: center; " +
                "border-radius: 5px; margin: 20px 0; }" +
                ".code { font-size: 32px; font-weight: bold; color: #007bff; letter-spacing: 5px; }" +
                ".footer { margin-top: 30px; font-size: 12px; color: #666; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "<h2>SmartHire 邮箱验证码</h2>" +
                "<p>您好，</p>" +
                "<p>您正在使用此邮箱进行验证，验证码如下：</p>" +
                "<div class='code-box'>" +
                "<div class='code'>" + code + "</div>" +
                "</div>" +
                "<p>验证码有效期为 <strong>10分钟</strong>，请勿泄露给他人。</p>" +
                "<p>如非本人操作，请忽略此邮件。</p>" +
                "<div class='footer'>" +
                "<p>此邮件由系统自动发送，请勿回复。</p>" +
                "<p>© SmartHire Team</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }
}
