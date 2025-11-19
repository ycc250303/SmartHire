package com.SmartHire.userAuthService.service.impl;

import com.SmartHire.shared.exception.enums.ErrorCode;
import com.SmartHire.shared.exception.exception.BusinessException;
import com.SmartHire.userAuthService.utils.EmailValidatorUtil;
import com.SmartHire.shared.utils.VerificationCodeGeneratorUtil;
import com.SmartHire.userAuthService.service.EmailService;
import com.SmartHire.userAuthService.service.VerificationCodeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 验证码服务实现类
 */
@Service
@Slf4j
public class VerificationCodeServiceImpl implements VerificationCodeService {
    // Redis Key 前缀
    private static final String CODE_KEY_PREFIX = "email:code:";
    private static final String SEND_TIME_KEY_PREFIX = "email:send:time:";

    @Autowired
    private EmailService emailService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final long CODE_EXPIRE_TIME = 30 * 60 * 1000; // 验证码有效期30分钟

    public static final long SEND_INTERVAL = 5 * 1000; // 发送验证码的间隔时间5秒（毫秒）

    /**
     * 发送验证码到指定邮箱
     *
     * @param email 邮箱地址
     */
    @Override
    public void sendVerificationCode(String email) {
        // 验证邮箱格式
        if (!EmailValidatorUtil.isValid(email)) {
            throw new BusinessException(ErrorCode.EMAIL_FORMAT_INVALID);
        }

        // 检查发送频率
        String sendTimeKey = SEND_TIME_KEY_PREFIX + email;
        String lastSendTime = redisTemplate.opsForValue().get(sendTimeKey);
        if (lastSendTime != null) {
            // 如果键存在，说明还在限制期内
            // 为了安全，即使键存在但可能已过期（Redis的过期是异步的），也直接抛出异常
            // 或者可以检查时间差，但为了简单起见，直接检查键是否存在即可
            log.warn("邮箱 {} 发送验证码过于频繁，上次发送时间: {}", email, lastSendTime);
            throw new BusinessException(ErrorCode.EMAIL_CODE_SEND_TOO_FREQUENT);
        }

        // 生成验证码
        String code = VerificationCodeGeneratorUtil.generate();
        log.info("为邮箱 {} 生成验证码: {}", email, code);

        // 发送验证码
        try {
            emailService.sendVerificationCode(email, code);
        } catch (Exception e) {
            log.error("发送验证码邮件失败，邮箱：{}", email, e);
            throw new BusinessException(ErrorCode.EMAIL_CODE_SEND_FAILED);
        }

        // 存储验证码到Redis
        String codeKey = CODE_KEY_PREFIX + email;
        redisTemplate.opsForValue().set(codeKey, code, CODE_EXPIRE_TIME, TimeUnit.MILLISECONDS);

        // 存储发送时间
        redisTemplate.opsForValue().set(sendTimeKey, String.valueOf(System.currentTimeMillis()),
                SEND_INTERVAL, TimeUnit.MILLISECONDS);

        log.info("验证码已发送并存储到Redis，邮箱: {}", email);
    }

    /**
     * 验证用户输入的验证码
     *
     * @param email 邮箱地址
     * @param code  用户输入的验证码
     */
    @Override
    public void verifyCode(String email, String code) {
        // 1. 参数校验
        if (email == null || email.trim().isEmpty() ||
                code == null || code.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "邮箱或验证码不能为空");
        }

        // 2. 从 Redis 获取验证码
        String codeKey = CODE_KEY_PREFIX + email;
        String storedCode = redisTemplate.opsForValue().get(codeKey);

        // 3. 验证码不存在或已过期
        if (storedCode == null) {
            log.warn("验证码验证失败：验证码不存在或已过期，邮箱: {}", email);
            throw new BusinessException(ErrorCode.EMAIL_CODE_INVALID);
        }

        // 4. 比对验证码
        boolean isValid = storedCode.trim().equals(code.trim());
        if (!isValid) {
            log.warn("验证码验证失败：验证码不匹配，邮箱: {}", email);
            throw new BusinessException(ErrorCode.EMAIL_CODE_INVALID);
        }

        // 5. 验证成功后删除验证码（防止重复使用）
        redisTemplate.delete(codeKey);
        log.info("验证码验证成功，邮箱: {}", email);
    }
}