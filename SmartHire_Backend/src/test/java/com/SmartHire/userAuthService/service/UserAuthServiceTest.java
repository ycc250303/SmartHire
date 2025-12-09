package com.SmartHire.userAuthService.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.SmartHire.common.auth.JwtTokenExtractor;
import com.SmartHire.common.auth.UserContext;
import com.SmartHire.common.exception.enums.ErrorCode;
import com.SmartHire.common.exception.exception.BusinessException;
import com.SmartHire.common.utils.JwtUtil;
import com.SmartHire.userAuthService.dto.*;
import com.SmartHire.userAuthService.mapper.UserAuthMapper;
import com.SmartHire.userAuthService.model.User;
import com.SmartHire.userAuthService.service.impl.UserAuthServiceImpl;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.lang.reflect.Field;
import org.mockito.ArgumentCaptor;

/**
 * UserAuthService 单元测试
 *
 * <p>
 * 测试目标：防止用户注册、登录、Token刷新等核心业务逻辑回归
 *
 * <p>
 * 测试原则：
 * <ul>
 * <li>使用 Mockito mock 外部依赖（验证码服务、数据库、JWT工具、Redis）</li>
 * <li>保留真实业务逻辑测试（密码加密、Token生成、用户信息查询）</li>
 * <li>使用 Arrange / Act / Assert 三段式结构</li>
 * </ul>
 *
 * <p>
 * 注意：本测试基于 API 文档和测试用例矩阵推导，不依赖具体实现细节
 *
 * @author SmartHire Test Team
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("用户认证服务单元测试")
@SuppressWarnings("unchecked")
class UserAuthServiceTest {

    @Mock
    private VerificationCodeService verificationCodeService;

    @Mock
    private UserAuthMapper userAuthMapper;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private UserContext userContext;

    @Mock
    private JwtTokenExtractor tokenExtractor;

    @InjectMocks
    private UserAuthServiceImpl userAuthService;

    // ==================== 测试数据常量 ====================
    // 说明：使用常量定义测试数据，避免硬编码，提高可维护性

    /** 有效的用户名（用于测试） */
    private static final String VALID_USERNAME = "testuser123";

    /** 有效的密码（用于测试） */
    private static final String VALID_PASSWORD = "Password123";

    /** 有效的邮箱（用于测试） */
    private static final String VALID_EMAIL = "test@example.com";

    /** 有效的手机号（用于测试） */
    private static final String VALID_PHONE = "13800138000";

    /** 有效的验证码（用于测试） */
    private static final String VALID_CODE = "123456";

    /** 无效的验证码（用于测试验证码校验失败场景） */
    private static final String INVALID_CODE = "999999";

    /** 加密后的密码（模拟 PasswordEncoder 加密结果） */
    private static final String ENCRYPTED_PASSWORD = "$2a$10$encryptedPasswordHash";

    /** 测试用户ID */
    private static final Long USER_ID = 1L;

    /** 默认头像URL（业务代码中定义的默认值） */
    private static final String DEFAULT_AVATAR_URL = "https://smart-hire.oss-cn-shanghai.aliyuncs.com/default-avatar.png";

    @BeforeEach
    void setUp() throws Exception {
        // Arrange: 设置通用 mock 行为
        // Mock RedisTemplate 返回 ValueOperations（使用 lenient 避免不必要的 stubbing 警告）
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // 设置 @Value 注入的字段值（@InjectMocks 不会自动注入 @Value 字段）
        // accessTokenValidTime: 7200 秒 = 2 小时（毫秒：7200000）
        setFieldValue(userAuthService, "accessTokenValidTime", 7200000L);
        // refreshTokenValidTime: 604800 秒 = 7 天（毫秒：604800000）
        setFieldValue(userAuthService, "refreshTokenValidTime", 604800000L);
        // refreshTokenRenewThreshold: 600 秒 = 10 分钟（毫秒：600000）
        setFieldValue(userAuthService, "refreshTokenRenewThreshold", 600000L);
    }

    /**
     * 使用反射设置私有字段值（用于设置 @Value 注入的字段）
     *
     * @param target    目标对象
     * @param fieldName 字段名
     * @param value     字段值
     */
    private void setFieldValue(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    // ==================== P0 关键业务测试用例 ====================

    @Test
    @DisplayName("TC-REG-001: 成功注册流程")
    void testRegister_Success() {
        // Arrange: 准备测试数据
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setUsername(VALID_USERNAME);
        registerDTO.setPassword(VALID_PASSWORD);
        registerDTO.setEmail(VALID_EMAIL);
        registerDTO.setPhone(VALID_PHONE);
        registerDTO.setGender(1);
        registerDTO.setUserType(1);
        registerDTO.setVerifyCode(VALID_CODE);

        // Mock 验证码服务：验证码有效
        doNothing().when(verificationCodeService).verifyCodeWithoutDelete(eq(VALID_EMAIL), eq(VALID_CODE));

        // Mock 数据库查询：用户名、邮箱、手机号都不存在
        when(userAuthMapper.checkUsernameExist(VALID_USERNAME)).thenReturn(null);
        when(userAuthMapper.checkEmailExist(VALID_EMAIL)).thenReturn(null);
        when(userAuthMapper.checkPhoneExist(VALID_PHONE)).thenReturn(null);

        // Mock 密码加密
        when(passwordEncoder.encode(VALID_PASSWORD)).thenReturn(ENCRYPTED_PASSWORD);

        // Mock 数据库插入：返回插入的行数
        when(userAuthMapper.insert(any(User.class))).thenReturn(1);

        // Mock 验证码删除：注册成功后删除验证码
        doNothing().when(verificationCodeService).deleteCode(VALID_EMAIL);

        // Act: 执行注册操作
        assertDoesNotThrow(
                () -> {
                    userAuthService.register(registerDTO);
                },
                "注册应该成功，不抛出异常");

        // Assert: 验证业务流程
        // 1. 验证验证码校验被调用
        verify(verificationCodeService, times(1)).verifyCodeWithoutDelete(eq(VALID_EMAIL), eq(VALID_CODE));
        // 2. 验证用户名唯一性检查
        verify(userAuthMapper, times(1)).checkUsernameExist(VALID_USERNAME);
        // 3. 验证邮箱唯一性检查
        verify(userAuthMapper, times(1)).checkEmailExist(VALID_EMAIL);
        // 4. 验证手机号唯一性检查
        verify(userAuthMapper, times(1)).checkPhoneExist(VALID_PHONE);
        // 5. 验证密码被加密
        verify(passwordEncoder, times(1)).encode(VALID_PASSWORD);
        // 6. 验证验证码被删除
        verify(verificationCodeService, times(1)).deleteCode(VALID_EMAIL);

        // Assert: 验证业务数据完整性（防止字段缺失或错误）
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userAuthMapper, times(1)).insert(userCaptor.capture());
        User insertedUser = userCaptor.getValue();

        // 验证所有字段都正确设置
        assertEquals(VALID_USERNAME, insertedUser.getUsername(), "用户名应该正确设置");
        assertEquals(ENCRYPTED_PASSWORD, insertedUser.getPassword(), "密码应该被加密存储");
        assertEquals(VALID_EMAIL, insertedUser.getEmail(), "邮箱应该正确设置");
        assertEquals(VALID_PHONE, insertedUser.getPhone(), "手机号应该正确设置");
        assertEquals(1, insertedUser.getGender(), "性别应该正确设置");
        assertEquals(1, insertedUser.getUserType(), "用户类型应该正确设置");
        assertEquals(1, insertedUser.getStatus(), "账户状态应该设置为正常(1)");
        assertEquals(DEFAULT_AVATAR_URL, insertedUser.getAvatarUrl(), "头像URL应该设置为默认值");
        assertNotNull(insertedUser.getCreatedAt(), "创建时间应该被设置");
        assertNotNull(insertedUser.getUpdatedAt(), "更新时间应该被设置");
    }

    @Test
    @DisplayName("TC-REG-002: 验证码无效或过期")
    void testRegister_InvalidVerificationCode() {
        // Arrange: 准备测试数据
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setUsername(VALID_USERNAME);
        registerDTO.setPassword(VALID_PASSWORD);
        registerDTO.setEmail(VALID_EMAIL);
        registerDTO.setPhone(VALID_PHONE);
        registerDTO.setGender(1);
        registerDTO.setUserType(1);
        registerDTO.setVerifyCode(INVALID_CODE);

        // Mock 验证码服务：验证码无效，抛出异常
        doThrow(new BusinessException(ErrorCode.EMAIL_CODE_INVALID))
                .when(verificationCodeService)
                .verifyCodeWithoutDelete(eq(VALID_EMAIL), eq(INVALID_CODE));

        // Act & Assert: 执行并验证抛出验证码无效异常
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> {
                    userAuthService.register(registerDTO);
                },
                "验证码无效应该抛出 BusinessException");

        // Assert: 验证异常
        assertEquals(
                ErrorCode.EMAIL_CODE_INVALID.getCode(),
                exception.getCode(),
                "异常错误码应该是 EMAIL_CODE_INVALID(1003)");

        // 验证用户未被保存到数据库
        verify(userAuthMapper, never()).insert(any(User.class));
    }

    @Test
    @DisplayName("TC-REG-003: 用户名已存在")
    void testRegister_UsernameExists() {
        // Arrange: 准备测试数据
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setUsername("existinguser");
        registerDTO.setPassword(VALID_PASSWORD);
        registerDTO.setEmail(VALID_EMAIL);
        registerDTO.setPhone(VALID_PHONE);
        registerDTO.setGender(1);
        registerDTO.setUserType(1);
        registerDTO.setVerifyCode(VALID_CODE);

        // Mock 验证码服务：验证码有效
        doNothing().when(verificationCodeService).verifyCodeWithoutDelete(eq(VALID_EMAIL), eq(VALID_CODE));

        // Mock 数据库查询：邮箱不存在，但用户名已存在
        // 注意：业务代码先检查邮箱，再检查用户名，所以需要确保邮箱不存在
        when(userAuthMapper.checkEmailExist(VALID_EMAIL)).thenReturn(null);
        when(userAuthMapper.checkUsernameExist("existinguser")).thenReturn(1L);

        // Act & Assert: 执行并验证抛出用户名已存在异常
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> {
                    userAuthService.register(registerDTO);
                },
                "用户名已存在应该抛出 BusinessException");

        // Assert: 验证异常
        assertEquals(
                ErrorCode.USER_AUTH_USER_HAS_EXISTED.getCode(),
                exception.getCode(),
                "异常错误码应该是 USER_AUTH_USER_HAS_EXISTED(1001)");

        // 验证用户未被保存到数据库
        verify(userAuthMapper, never()).insert(any(User.class));
    }

    @Test
    @DisplayName("TC-REG-003: 邮箱已被注册")
    void testRegister_EmailExists() {
        // Arrange: 准备测试数据
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setUsername(VALID_USERNAME);
        registerDTO.setPassword(VALID_PASSWORD);
        registerDTO.setEmail("existing@example.com");
        registerDTO.setPhone(VALID_PHONE);
        registerDTO.setGender(1);
        registerDTO.setUserType(1);
        registerDTO.setVerifyCode(VALID_CODE);

        // Mock 验证码服务：验证码有效
        doNothing().when(verificationCodeService).verifyCodeWithoutDelete(eq("existing@example.com"), eq(VALID_CODE));

        // Mock 数据库查询：邮箱已存在（业务代码先检查邮箱）
        when(userAuthMapper.checkEmailExist("existing@example.com")).thenReturn(1L);

        // Act & Assert: 执行并验证抛出邮箱已被注册异常
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> {
                    userAuthService.register(registerDTO);
                },
                "邮箱已被注册应该抛出 BusinessException");

        // Assert: 验证异常
        assertEquals(
                ErrorCode.EMAIL_ALREADY_REGISTERED.getCode(),
                exception.getCode(),
                "异常错误码应该是 EMAIL_ALREADY_REGISTERED(1006)");

        // 验证用户未被保存到数据库
        verify(userAuthMapper, never()).insert(any(User.class));
    }

    @Test
    @DisplayName("TC-LOGIN-001: 成功登录流程")
    void testLogin_Success() {
        // Arrange: 准备测试数据
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername(VALID_USERNAME);
        loginDTO.setPassword(VALID_PASSWORD);

        // Mock 数据库查询：用户存在
        User user = new User();
        user.setId(USER_ID);
        user.setUsername(VALID_USERNAME);
        user.setPassword(ENCRYPTED_PASSWORD);
        user.setEmail(VALID_EMAIL);
        user.setUserType(1);
        user.setStatus(1); // 账户状态：1-正常（重要：业务代码会检查状态）
        when(userAuthMapper.findByUserName(VALID_USERNAME)).thenReturn(user);

        // Mock 密码校验：密码正确
        when(passwordEncoder.matches(VALID_PASSWORD, ENCRYPTED_PASSWORD)).thenReturn(true);

        // Mock JWT Token 生成
        String accessToken = "eyJhbGci.accessToken";
        String refreshToken = "eyJhbGci.refreshToken";
        when(jwtUtil.generateAccessToken(any(Map.class))).thenReturn(accessToken);
        when(jwtUtil.generateRefreshToken(any(Map.class))).thenReturn(refreshToken);

        // Mock 数据库更新：更新最后登录时间
        // 注意：MyBatis-Plus 的 updateById 是 ServiceImpl 提供的方法，不需要 mock

        // Mock Redis：保存 refreshToken（用于单点登录）
        doNothing().when(valueOperations).set(anyString(), anyString(), anyLong(), any());

        // Act: 执行登录操作
        LoginResponseDTO response = assertDoesNotThrow(
                () -> {
                    return userAuthService.login(loginDTO);
                },
                "登录应该成功，不抛出异常");

        // Assert: 验证响应数据
        assertNotNull(response, "登录响应不应该为 null");
        assertNotNull(response.getAccessToken(), "AccessToken 不应该为 null");
        assertNotNull(response.getRefreshToken(), "RefreshToken 不应该为 null");
        assertEquals(7200, response.getExpiresIn(), "Token 过期时间应该是 7200 秒（2小时）");

        // Assert: 验证业务流程
        // 1. 验证密码校验被调用
        verify(passwordEncoder, times(1)).matches(VALID_PASSWORD, ENCRYPTED_PASSWORD);
        // 2. 验证 Token 生成被调用，并验证 claims 内容正确
        ArgumentCaptor<Map<String, Object>> accessTokenClaimsCaptor = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Map<String, Object>> refreshTokenClaimsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(jwtUtil, times(1)).generateAccessToken(accessTokenClaimsCaptor.capture());
        verify(jwtUtil, times(1)).generateRefreshToken(refreshTokenClaimsCaptor.capture());

        // 验证 accessToken claims 包含正确的用户信息（防止 Token 内容错误）
        Map<String, Object> accessTokenClaims = accessTokenClaimsCaptor.getValue();
        assertEquals(USER_ID, accessTokenClaims.get("id"), "AccessToken claims 应该包含正确的用户ID");
        assertEquals(VALID_USERNAME, accessTokenClaims.get("username"), "AccessToken claims 应该包含正确的用户名");
        assertEquals(1, accessTokenClaims.get("userType"), "AccessToken claims 应该包含正确的用户类型");

        // 验证 refreshToken claims 包含正确的用户信息
        Map<String, Object> refreshTokenClaims = refreshTokenClaimsCaptor.getValue();
        assertEquals(USER_ID, refreshTokenClaims.get("id"), "RefreshToken claims 应该包含正确的用户ID");
        assertEquals(VALID_USERNAME, refreshTokenClaims.get("username"), "RefreshToken claims 应该包含正确的用户名");
        assertEquals(1, refreshTokenClaims.get("userType"), "RefreshToken claims 应该包含正确的用户类型");
    }

    @Test
    @DisplayName("TC-LOGIN-002: 用户名不存在")
    void testLogin_UsernameNotExists() {
        // Arrange: 准备测试数据
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("nonexist");
        loginDTO.setPassword(VALID_PASSWORD);

        // Mock 数据库查询：用户不存在
        when(userAuthMapper.findByUserName("nonexist")).thenReturn(null);

        // Act & Assert: 执行并验证抛出用户名不存在异常
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> {
                    userAuthService.login(loginDTO);
                },
                "用户名不存在应该抛出 BusinessException");

        // Assert: 验证异常
        assertEquals(
                ErrorCode.USER_AUTH_USER_NOT_EXIST.getCode(),
                exception.getCode(),
                "异常错误码应该是 USER_AUTH_USER_NOT_EXIST(1007)");

        // 验证密码校验未被调用
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        // 验证 Token 生成未被调用
        verify(jwtUtil, never()).generateAccessToken(any(Map.class));
    }

    @Test
    @DisplayName("TC-LOGIN-002: 密码错误")
    void testLogin_PasswordError() {
        // Arrange: 准备测试数据
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername(VALID_USERNAME);
        loginDTO.setPassword("WrongPassword");

        // Mock 数据库查询：用户存在
        User user = new User();
        user.setId(USER_ID);
        user.setUsername(VALID_USERNAME);
        user.setPassword(ENCRYPTED_PASSWORD);
        when(userAuthMapper.findByUserName(VALID_USERNAME)).thenReturn(user);

        // Mock 密码校验：密码错误
        when(passwordEncoder.matches("WrongPassword", ENCRYPTED_PASSWORD)).thenReturn(false);

        // Act & Assert: 执行并验证抛出密码错误异常
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> {
                    userAuthService.login(loginDTO);
                },
                "密码错误应该抛出 BusinessException");

        // Assert: 验证异常
        assertEquals(
                ErrorCode.USER_AUTH_USER_PASSWORD_ERROR.getCode(),
                exception.getCode(),
                "异常错误码应该是 USER_AUTH_USER_PASSWORD_ERROR(1008)");

        // 验证 Token 生成未被调用
        verify(jwtUtil, never()).generateAccessToken(any(Map.class));
    }

    @Test
    @DisplayName("TC-TOKEN-001: 成功刷新 Token")
    void testRefreshToken_Success() {
        // Arrange: 准备测试数据
        String validRefreshToken = "eyJhbGci.validRefreshToken";

        // Mock JWT 验证：Token 有效且未过期（剩余时间 > 续期阈值）
        com.auth0.jwt.interfaces.DecodedJWT decodedJWT = mock(com.auth0.jwt.interfaces.DecodedJWT.class);
        when(jwtUtil.verifyToken(validRefreshToken)).thenReturn(decodedJWT);
        when(jwtUtil.isRefreshToken(decodedJWT)).thenReturn(true);
        // 剩余时间 7200 秒（大于阈值 600 秒），不需要续期
        when(jwtUtil.getExpiresInSeconds(decodedJWT)).thenReturn(7200L);

        Map<String, Object> claims = new HashMap<String, Object>();
        claims.put("id", USER_ID);
        claims.put("username", VALID_USERNAME);
        claims.put("userType", 1);
        when(jwtUtil.getClaims(decodedJWT)).thenReturn(claims);
        when(jwtUtil.getUserIdFromToken(claims)).thenReturn(USER_ID);

        // Mock Redis：检查 refreshToken 是否在单点登录缓存中
        when(valueOperations.get(contains("token:refresh:single:"))).thenReturn(validRefreshToken);

        // Mock TokenExtractor：获取旧的 accessToken（可能为 null）
        when(tokenExtractor.extractTokenNullable()).thenReturn(null);

        // Mock 新 Token 生成
        String newAccessToken = "eyJhbGci.newAccessToken";
        when(jwtUtil.generateAccessToken(any(Map.class))).thenReturn(newAccessToken);

        // Mock 新 accessToken 验证（用于计算过期时间）
        com.auth0.jwt.interfaces.DecodedJWT newAccessDecoded = mock(com.auth0.jwt.interfaces.DecodedJWT.class);
        when(jwtUtil.verifyToken(newAccessToken)).thenReturn(newAccessDecoded);
        when(jwtUtil.getExpiresInSeconds(newAccessDecoded)).thenReturn(7200L);

        // Act: 执行刷新 Token 操作
        LoginResponseDTO response = assertDoesNotThrow(
                () -> {
                    return userAuthService.refreshToken(validRefreshToken);
                },
                "刷新 Token 应该成功，不抛出异常");

        // Assert: 验证响应数据
        assertNotNull(response, "刷新 Token 响应不应该为 null");
        assertNotNull(response.getAccessToken(), "新的 AccessToken 不应该为 null");
        assertEquals(7200, response.getExpiresIn(), "Token 过期时间应该是 7200 秒");
        // 验证 refreshToken 未续期（因为剩余时间 > 阈值）
        assertEquals(validRefreshToken, response.getRefreshToken(), "refreshToken 应该保持不变（未续期）");

        // Assert: 验证业务流程
        // 1. 验证 Token 验证被调用
        verify(jwtUtil, times(1)).verifyToken(validRefreshToken);
        // 2. 验证新 Token 生成被调用
        verify(jwtUtil, times(1)).generateAccessToken(any(Map.class));
        // 3. 验证 refreshToken 未续期（未调用 generateRefreshToken）
        verify(jwtUtil, never()).generateRefreshToken(any(Map.class));
    }

    @Test
    @DisplayName("TC-USER-001: 成功获取当前用户信息")
    void testGetUserInfo_Success() {
        // Arrange: 准备测试数据
        // Mock UserContext：获取当前用户ID
        when(userContext.getCurrentUserId()).thenReturn(USER_ID);

        // Mock 数据库查询：用户存在
        // 注意：业务代码使用 MyBatis-Plus 的 selectById 方法
        User user = new User();
        user.setId(USER_ID);
        user.setUsername(VALID_USERNAME);
        user.setEmail(VALID_EMAIL);
        user.setPhone(VALID_PHONE);
        user.setUserType(1);
        user.setAvatarUrl("https://example.com/avatar.png");
        when(userAuthMapper.selectById(USER_ID)).thenReturn(user);

        // Act: 执行获取用户信息操作
        UserInfoDTO userInfo = assertDoesNotThrow(
                () -> {
                    return userAuthService.getUserInfo();
                },
                "获取用户信息应该成功，不抛出异常");

        // Assert: 验证结果
        assertNotNull(userInfo, "用户信息不应该为 null");
        assertEquals(VALID_USERNAME, userInfo.getUsername(), "用户名应该匹配");
        assertEquals(VALID_EMAIL, userInfo.getEmail(), "邮箱应该匹配");
        assertEquals(VALID_PHONE, userInfo.getPhone(), "手机号应该匹配");
        assertEquals(1, userInfo.getUserType(), "用户类型应该匹配");

        // 验证数据库查询被调用
        verify(userAuthMapper, times(1)).selectById(USER_ID);
    }

    // ==================== P1 次要业务测试用例 ====================

    @Test
    @DisplayName("TC-PUBLIC-001: 成功获取用户公开信息")
    void testGetPublicUserInfo_Success() {
        // Arrange: 准备测试数据
        Long targetUserId = 123L;

        // Mock 数据库查询：用户存在
        // 注意：业务代码使用 MyBatis-Plus 的 selectById 方法
        User user = new User();
        user.setId(targetUserId);
        user.setUsername("otheruser");
        user.setUserType(1);
        user.setAvatarUrl("https://example.com/avatar.png");
        // 注意：公开信息不应该包含 email 和 phone
        when(userAuthMapper.selectById(targetUserId)).thenReturn(user);

        // Act: 执行获取公开信息操作
        PublicUserInfoDTO publicInfo = assertDoesNotThrow(
                () -> {
                    return userAuthService.getPublicUserInfo(targetUserId);
                },
                "获取公开信息应该成功，不抛出异常");

        // Assert: 验证结果
        assertNotNull(publicInfo, "公开信息不应该为 null");
        assertEquals(targetUserId, publicInfo.getId(), "用户ID应该匹配");
        assertEquals("otheruser", publicInfo.getUsername(), "用户名应该匹配");
        assertEquals(1, publicInfo.getUserType(), "用户类型应该匹配");
        // 验证不包含隐私信息
        // 注意：实际实现中 PublicUserInfoDTO 不应该有 email 和 phone 字段

        // 验证数据库查询被调用
        verify(userAuthMapper, times(1)).selectById(targetUserId);
    }

    @Test
    @DisplayName("TC-LOGOUT-001: 成功登出")
    void testLogout_Success() {
        // Arrange: 准备测试数据
        String accessToken = "eyJhbGci.currentAccessToken";
        String refreshToken = "eyJhbGci.currentRefreshToken";

        // Mock UserContext：获取当前用户ID
        when(userContext.getCurrentUserId()).thenReturn(USER_ID);

        // Mock TokenExtractor：提取 accessToken
        when(tokenExtractor.extractToken()).thenReturn(accessToken);

        // Mock Redis：获取当前 refreshToken
        when(valueOperations.get(contains("token:refresh:single:"))).thenReturn(refreshToken);

        // Mock Redis：将 Token 加入黑名单
        doNothing().when(valueOperations).set(anyString(), anyString(), anyLong(), any());

        // Mock Redis：删除单点登录缓存
        when(redisTemplate.delete(anyString())).thenReturn(true);

        // Act: 执行登出操作
        assertDoesNotThrow(
                () -> {
                    userAuthService.logout();
                },
                "登出应该成功，不抛出异常");

        // Assert: 验证业务流程
        // 1. 验证 accessToken 被加入黑名单（使用 ArgumentCaptor 验证过期时间）
        ArgumentCaptor<String> blacklistKeyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> blacklistValueCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Long> expireTimeCaptor = ArgumentCaptor.forClass(Long.class);
        verify(valueOperations, atLeastOnce()).set(
                blacklistKeyCaptor.capture(),
                blacklistValueCaptor.capture(),
                expireTimeCaptor.capture(),
                any());

        // 验证 accessToken 和 refreshToken 都被加入黑名单
        assertTrue(
                blacklistKeyCaptor.getAllValues().stream()
                        .anyMatch(key -> key.contains("token:blacklist:access:")),
                "accessToken 应该被加入黑名单");
        assertTrue(
                blacklistKeyCaptor.getAllValues().stream()
                        .anyMatch(key -> key.contains("token:blacklist:refresh:")),
                "refreshToken 应该被加入黑名单");

        // 验证黑名单过期时间设置正确（应该大于 0）
        assertTrue(
                expireTimeCaptor.getAllValues().stream().allMatch(time -> time > 0),
                "黑名单过期时间应该大于 0");

        // 2. 验证单点登录缓存被删除
        verify(redisTemplate, times(1)).delete(contains("token:refresh:single:"));
    }
}
