package com.SmartHire.userAuthService.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.SmartHire.userAuthService.service.impl.VerificationCodeServiceImpl;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

/**
 * VerificationCodeService 单元测试
 *
 * <p>测试目标：防止验证码发送逻辑回归
 *
 * <p>测试原则：
 *
 * <ul>
 *   <li>使用 Mockito mock 外部依赖（邮件服务、Redis）
 *   <li>保留真实业务逻辑测试（验证码生成、发送逻辑）
 *   <li>使用 Arrange / Act / Assert 三段式结构
 *   <li>严格遵循测试用例矩阵，不添加额外测试
 * </ul>
 *
 * <p>测试范围说明：
 *
 * <ul>
 *   <li>✅ TC-VERIFY-001: 成功发送验证码（本测试文件）
 * </ul>
 *
 * <p>注意：本测试基于 API 文档和测试用例矩阵推导，严格遵循矩阵定义的 10 个测试用例
 *
 * @author SmartHire Test Team
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("验证码服务单元测试")
class VerificationCodeServiceTest {

  @Mock private EmailService emailService;

  @Mock private RedisTemplate<String, String> redisTemplate;

  @Mock private ValueOperations<String, String> valueOperations;

  @InjectMocks private VerificationCodeServiceImpl verificationCodeService;

  private static final String VALID_EMAIL = "test@example.com";

  // 用于模拟 Redis 存储的 Map
  private final Map<String, String> mockRedisStore = new HashMap<>();

  @BeforeEach
  void setUp() {
    // Arrange: 设置 RedisTemplate mock 行为
    // 当调用 redisTemplate.opsForValue() 时返回 valueOperations mock
    // 使用 lenient 避免不必要的 stubbing 警告
    lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    // 清空模拟的 Redis 存储
    mockRedisStore.clear();
  }

  @Test
  @DisplayName("TC-VERIFY-001: 成功发送验证码")
  void testSendVerificationCode_Success() {
    // Arrange: 准备测试数据
    // 1. Mock Redis get 方法：使用 Map 模拟 Redis 存储
    // 第一次调用：检查发送频率限制（返回 null 表示未在限制期内）
    // 后续调用：从模拟存储中返回值
    when(valueOperations.get(anyString()))
        .thenAnswer(
            invocation -> {
              String key = invocation.getArgument(0);
              // 第一次调用是检查发送频率，返回 null
              // 后续调用从模拟存储中获取值
              return mockRedisStore.get(key);
            });

    // 2. Mock set 方法：将值存储到模拟的 Map 中
    doAnswer(
            invocation -> {
              String key = invocation.getArgument(0);
              String value = invocation.getArgument(1);
              mockRedisStore.put(key, value);
              return null;
            })
        .when(valueOperations)
        .set(anyString(), anyString(), anyLong(), any(TimeUnit.class));

    // 3. Mock 邮件服务成功发送（不抛出异常）
    try {
      doNothing().when(emailService).sendVerificationCode(anyString(), anyString());
    } catch (Exception e) {
      fail("Mock 邮件服务不应该抛出异常: " + e.getMessage());
    }

    // Act: 执行发送验证码操作
    assertDoesNotThrow(
        () -> {
          verificationCodeService.sendVerificationCode(VALID_EMAIL);
        },
        "发送验证码应该成功，不抛出异常");

    // Assert: 验证结果
    // 1. 验证邮件服务被调用（说明验证码已生成并发送）
    try {
      verify(emailService, times(1)).sendVerificationCode(eq(VALID_EMAIL), anyString());
    } catch (Exception e) {
      fail("验证邮件服务调用不应该抛出异常: " + e.getMessage());
    }
    // 2. 验证验证码已保存到 Redis（用于后续验证）
    verify(valueOperations, atLeastOnce())
        .set(anyString(), anyString(), anyLong(), any(TimeUnit.class));
    // 3. 验证存储的验证码不为空（从模拟存储中检查）
    String codeKey = "email:code:" + VALID_EMAIL;
    assertTrue(mockRedisStore.containsKey(codeKey), "验证码应该已存储到 Redis");
    assertNotNull(mockRedisStore.get(codeKey), "存储的验证码不应该为空");
  }
}
