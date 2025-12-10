package com.SmartHire.userAuthService.service.impl;

import com.SmartHire.common.exception.enums.ErrorCode;
import com.SmartHire.common.exception.exception.BusinessException;
import com.SmartHire.common.utils.VerificationCodeGeneratorUtil;
import com.SmartHire.userAuthService.service.EmailService;
import com.SmartHire.userAuthService.service.VerificationCodeService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/** 验证码服务实现类 */
@Service
@Slf4j
@Validated // 启用方法参数验证
public class VerificationCodeServiceImpl implements VerificationCodeService {
  // Redis Key 前缀
  private static final String CODE_KEY_PREFIX = "email:code:";
  private static final String SEND_TIME_KEY_PREFIX = "email:send:time:";

  @Autowired private EmailService emailService;

  @Autowired private RedisTemplate<String, String> redisTemplate;

  private static final long CODE_EXPIRE_TIME = 30 * 60 * 1000; // 验证码有效期30分钟

  public static final long SEND_INTERVAL = 5 * 1000; // 发送验证码的间隔时间5秒（毫秒）

  /**
   * 发送验证码到指定邮箱
   *
   * @param email 邮箱地址（使用 @Email 注解自动验证格式）
   */
  @Override
  public void sendVerificationCode(@Email(message = "邮箱格式不正确") String email) {
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
    // 注意：直接使用 set 覆盖，允许用户重新请求验证码时覆盖旧的验证码
    String codeKey = CODE_KEY_PREFIX + email;
    try {
      // 直接设置验证码（如果键已存在则覆盖）
      redisTemplate.opsForValue().set(codeKey, code, CODE_EXPIRE_TIME, TimeUnit.MILLISECONDS);

      // 验证存储是否成功（稍等片刻确保 Redis 操作完成）
      String storedCode = redisTemplate.opsForValue().get(codeKey);
      if (storedCode == null || !storedCode.equals(code)) {
        log.error("验证码存储验证失败，邮箱: {}, 存储的验证码: {}", email, storedCode);
        throw new BusinessException(ErrorCode.EMAIL_CODE_SEND_FAILED);
      }

      log.info("验证码已成功存储到Redis，邮箱: {}, 有效期: {} 分钟", email, CODE_EXPIRE_TIME / (60 * 1000));
    } catch (BusinessException e) {
      // 重新抛出业务异常
      throw e;
    } catch (Exception e) {
      log.error("存储验证码到Redis失败，邮箱: {}", email, e);
      throw new BusinessException(ErrorCode.EMAIL_CODE_SEND_FAILED);
    }

    // 存储发送时间
    try {
      redisTemplate
          .opsForValue()
          .set(
              sendTimeKey,
              String.valueOf(System.currentTimeMillis()),
              SEND_INTERVAL,
              TimeUnit.MILLISECONDS);
    } catch (Exception e) {
      log.warn("存储发送时间失败，邮箱: {}，但不影响验证码使用", email, e);
      // 发送时间存储失败不影响主流程，只记录警告
    }

    log.info("验证码已发送并存储到Redis，邮箱: {}", email);
  }

  /**
   * 验证用户输入的验证码（验证成功后立即删除验证码，防止重复使用）
   *
   * @param email 邮箱地址（使用 @Email 注解自动验证格式）
   * @param code 用户输入的验证码（使用 @NotBlank 注解自动验证非空）
   */
  @Override
  public void verifyCode(
      @Email(message = "邮箱格式不正确") String email, @NotBlank(message = "验证码不能为空") String code) {
    // 1.从 Redis 获取验证码
    String codeKey = CODE_KEY_PREFIX + email;
    String storedCode = null;
    try {
      storedCode = redisTemplate.opsForValue().get(codeKey);
      log.debug("从Redis获取验证码，邮箱: {}, 是否存在: {}", email, storedCode != null);
    } catch (Exception e) {
      log.error("从Redis获取验证码失败，邮箱: {}", email, e);
      throw new BusinessException(ErrorCode.EMAIL_CODE_INVALID);
    }

    // 2. 验证码不存在或已过期
    if (storedCode == null || storedCode.isBlank()) {
      log.warn("验证码验证失败：验证码不存在或已过期，邮箱: {}, Redis键: {}", email, codeKey);
      throw new BusinessException(ErrorCode.EMAIL_CODE_INVALID);
    }

    // 3. 比对验证码（去除空格，不区分大小写）
    String trimmedStoredCode = storedCode.trim();
    String trimmedInputCode = code.trim();
    boolean isValid = trimmedStoredCode.equals(trimmedInputCode);

    if (!isValid) {
      log.warn(
          "验证码验证失败：验证码不匹配，邮箱: {}, 输入的验证码: {}, 存储的验证码: {}",
          email,
          trimmedInputCode,
          trimmedStoredCode);
      throw new BusinessException(ErrorCode.EMAIL_CODE_INVALID);
    }

    // 4. 验证成功后删除验证码（防止重复使用）
    try {
      redisTemplate.delete(codeKey);
      log.info("验证码验证成功并已删除，邮箱: {}", email);
    } catch (Exception e) {
      log.warn("删除验证码失败，邮箱: {}，但不影响验证结果", email, e);
      // 删除失败不影响验证结果，只记录警告
    }
  }

  /**
   * 验证用户输入的验证码（验证成功后不删除验证码，用于注册等场景，注册成功后再删除）
   *
   * @param email 邮箱地址（使用 @Email 注解自动验证格式）
   * @param code 用户输入的验证码（使用 @NotBlank 注解自动验证非空）
   */
  @Override
  public void verifyCodeWithoutDelete(
      @Email(message = "邮箱格式不正确") String email, @NotBlank(message = "验证码不能为空") String code) {
    // 1.从 Redis 获取验证码
    String codeKey = CODE_KEY_PREFIX + email;
    String storedCode = redisTemplate.opsForValue().get(codeKey);

    // 2. 验证码不存在或已过期
    if (storedCode == null) {
      log.warn("验证码验证失败：验证码不存在或已过期，邮箱: {}", email);
      throw new BusinessException(ErrorCode.EMAIL_CODE_INVALID);
    }

    // 3. 比对验证码
    boolean isValid = storedCode.trim().equals(code.trim());
    if (!isValid) {
      log.warn("验证码验证失败：验证码不匹配，邮箱: {}", email);
      throw new BusinessException(ErrorCode.EMAIL_CODE_INVALID);
    }

    // 4. 验证成功但不删除验证码（等待业务成功后删除）
    log.info("验证码验证成功（未删除，等待业务成功后删除），邮箱: {}", email);
  }

  /**
   * 删除指定邮箱的验证码
   *
   * @param email 邮箱地址
   */
  @Override
  public void deleteCode(String email) {
    String codeKey = CODE_KEY_PREFIX + email;
    redisTemplate.delete(codeKey);
    log.info("验证码已删除，邮箱: {}", email);
  }
}
