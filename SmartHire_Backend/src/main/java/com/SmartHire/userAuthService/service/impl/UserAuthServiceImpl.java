package com.SmartHire.userAuthService.service.impl;

import com.SmartHire.common.api.MessageApi;
import com.SmartHire.common.api.SeekerApi;
import com.SmartHire.common.auth.JwtTokenExtractor;
import com.SmartHire.common.auth.UserContext;
import com.SmartHire.common.exception.enums.ErrorCode;
import com.SmartHire.common.exception.exception.BusinessException;
import com.SmartHire.common.utils.AliOssUtil;
import com.SmartHire.common.utils.JwtUtil;
import com.SmartHire.userAuthService.dto.*;
import com.SmartHire.userAuthService.mapper.UserAuthMapper;
import com.SmartHire.userAuthService.model.User;
import com.SmartHire.userAuthService.service.UserAuthService;
import com.SmartHire.userAuthService.service.VerificationCodeService;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户基础表 服务实现类
 *
 * @author SmartHire Team
 * @since 2025-11-15
 */
@Slf4j
@Service
public class UserAuthServiceImpl extends ServiceImpl<UserAuthMapper, User>
    implements UserAuthService {

  private static final String AVATAR_DIRECTORY_KEY = "avatar";
  private static final String DEFAULT_AVATAR_URL =
      "https://smart-hire.oss-cn-shanghai.aliyuncs.com/default-avatar.png";

  private static final String ACCESS_BLACKLIST_PREFIX = "token:blacklist:access:";
  private static final String REFRESH_BLACKLIST_PREFIX = "token:blacklist:refresh:";
  private static final String REFRESH_SINGLE_LOGIN_PREFIX = "token:refresh:single:";

  @Autowired private UserAuthMapper userMapper;

  @Autowired private VerificationCodeService verificationCodeService;

  @Autowired private PasswordEncoder passwordEncoder;

  @Autowired private AliOssUtil aliOssUtil;

  @Autowired private JwtUtil jwtUtil;

  @Autowired private UserContext userContext;

  @Autowired private JwtTokenExtractor tokenExtractor;

  @Autowired private RedisTemplate<String, String> redisTemplate;

  @Autowired(required = false)
  private SeekerApi seekerApi;

  @Autowired(required = false)
  private MessageApi messageApi;

  @Value("${jwt.access-token-valid-time}")
  private long accessTokenValidTime;

  @Value("${jwt.refresh-token-valid-time}")
  private long refreshTokenValidTime;

  @Value("${jwt.refresh-token-renew-threshold:600000}")
  private long refreshTokenRenewThreshold;

  @Override
  public User findByUserName(String username) {
    return userMapper.findByUserName(username);
  }

  /** 注册 */
  @Override
  public void register(RegisterDTO request) {
    // 统一业务校验（验证码验证成功但不删除）
    validateRegisterRequest(request);

    User user = new User();
    user.setUsername(request.getUsername());
    user.setPassword(passwordEncoder.encode(request.getPassword()));
    user.setEmail(request.getEmail());
    user.setPhone(request.getPhone());
    user.setGender(request.getGender());

    user.setUserType(request.getUserType());
    user.setStatus(1);
    user.setAvatarUrl(DEFAULT_AVATAR_URL);

    Date now = new Date();
    user.setCreatedAt(now);
    user.setUpdatedAt(now);

    userMapper.insert(user);

    // 注册成功后删除验证码
    verificationCodeService.deleteCode(request.getEmail());

    log.info("用户注册成功，用户ID: {}", user.getId());
  }

  /** 登录 */
  @Override
  public LoginResponseDTO login(LoginDTO request) {
    User user = validateLoginRequest(request);

    user.setLastLoginAt(new Date());
    userMapper.updateById(user);

    Map<String, Object> claims =
        Map.of(
            "id", user.getId(),
            "username", user.getUsername(),
            "userType", user.getUserType());

    String accessToken = jwtUtil.generateAccessToken(claims);
    String refreshToken = jwtUtil.generateRefreshToken(claims);

    cacheRefreshTokenForSingleLogin(user.getId(), refreshToken);

    LoginResponseDTO resp = new LoginResponseDTO();
    resp.setAccessToken(accessToken);
    resp.setRefreshToken(refreshToken);
    resp.setExpiresIn(accessTokenValidTime / 1000);
    return resp;
  }

  /**
   * 获取用户信息
   *
   * @return 用户信息DTO
   */
  @Override
  public UserInfoDTO getUserInfo() {
    Long userId = userContext.getCurrentUserId();
    User user = userMapper.selectById(userId);
    if (user == null) {
      throw new BusinessException(ErrorCode.USER_ID_NOT_EXIST);
    }

    UserInfoDTO userInfoDTO = new UserInfoDTO();
    BeanUtils.copyProperties(user, userInfoDTO);
    return userInfoDTO;
  }

  /**
   * 获取公开用户信息（他人可查看，不包含隐私信息）
   *
   * @param userId 用户ID
   * @return 公开用户信息DTO
   */
  @Override
  public PublicUserInfoDTO getPublicUserInfo(Long userId) {
    User user = userMapper.selectById(userId);

    if (user == null) {
      throw new BusinessException(ErrorCode.USER_ID_NOT_EXIST);
    }

    PublicUserInfoDTO publicUserInfoDTO = new PublicUserInfoDTO();
    publicUserInfoDTO.setId(user.getId());
    publicUserInfoDTO.setUsername(user.getUsername());
    publicUserInfoDTO.setUserType(user.getUserType());
    publicUserInfoDTO.setAvatarUrl(user.getAvatarUrl());
    return publicUserInfoDTO;
  }

  /**
   * 更新用户头像
   *
   * @param avatarFile 头像文件
   * @return 用户头像URL
   */
  @Override
  public String updateUserAvatar(MultipartFile avatarFile) throws IOException {
    Long userId = userContext.getCurrentUserId();
    User user = userMapper.selectById(userId);
    if (user == null) {
      throw new BusinessException(ErrorCode.USER_ID_NOT_EXIST);
    }
    String oldAvatarUrl = user.getAvatarUrl();

    // 生成文件名
    String originalFileName = avatarFile.getOriginalFilename();
    log.info("originalFileName:" + originalFileName);
    String fileExtension = "";

    // 安全地获取文件扩展名
    if (originalFileName != null && originalFileName.contains(".")) {
      fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
    }

    String fileName = UUID.randomUUID().toString() + fileExtension;
    try {
      String avatarUrl =
          aliOssUtil.uploadFile(AVATAR_DIRECTORY_KEY, fileName, avatarFile.getInputStream());
      userMapper.updateUserAvator(avatarUrl, userId);
      removeOldAvatar(oldAvatarUrl, avatarUrl);
      return avatarUrl;
    } catch (RuntimeException ex) {
      log.error("用户头像上传失败, userId={}, fileName={}", userId, fileName, ex);
      throw new BusinessException(ErrorCode.USER_AVATAR_UPLOAD_FAILED);
    }
  }

  /**
   * 登出
   *
   * @return
   */
  @Override
  public void logout() {
    Long userId = userContext.getCurrentUserId();
    String accessToken = tokenExtractor.extractToken();
    String refreshToken = getCurrentRefreshToken(userId);

    blacklistToken(accessToken, ACCESS_BLACKLIST_PREFIX, accessTokenValidTime);
    if (refreshToken != null) {
      blacklistToken(refreshToken, REFRESH_BLACKLIST_PREFIX, refreshTokenValidTime);
    }
    redisTemplate.delete(REFRESH_SINGLE_LOGIN_PREFIX + userId);
  }

  /**
   * 刷新令牌
   *
   * @param refreshToken 刷新令牌
   * @return 新的令牌
   */
  @Override
  public LoginResponseDTO refreshToken(String refreshToken) {
    // 验证 refresh token 并获取完整信息（需要检查类型）
    DecodedJWT decoded = jwtUtil.verifyToken(refreshToken);

    // 确保是 refresh token 类型
    if (!jwtUtil.isRefreshToken(decoded)) {
      throw new BusinessException(ErrorCode.TOKEN_IS_INVALID);
    }

    // 检查黑名单
    ensureNotBlacklisted(refreshToken, REFRESH_BLACKLIST_PREFIX);

    // 提取 claims 信息
    Map<String, Object> claims = jwtUtil.getClaims(decoded);
    Long userId = jwtUtil.getUserIdFromToken(claims);

    // 确保单点登录
    ensureSingleLogin(refreshToken, userId);

    // 旧 access token 加入黑名单，防止旧 token 继续使用
    String oldAccessToken = tokenExtractor.extractTokenNullable();
    if (oldAccessToken != null) {
      blacklistToken(oldAccessToken, ACCESS_BLACKLIST_PREFIX, accessTokenValidTime);
    }

    long refreshExpiresInSeconds = jwtUtil.getExpiresInSeconds(decoded);
    boolean needRenewRefreshToken =
        TimeUnit.SECONDS.toMillis(refreshExpiresInSeconds) <= refreshTokenRenewThreshold;

    String effectiveRefreshToken = refreshToken;
    if (needRenewRefreshToken) {
      String newRefreshToken = jwtUtil.generateRefreshToken(claims);
      cacheRefreshTokenForSingleLogin(userId, newRefreshToken);
      blacklistToken(
          refreshToken,
          REFRESH_BLACKLIST_PREFIX,
          TimeUnit.SECONDS.toMillis(refreshExpiresInSeconds));
      effectiveRefreshToken = newRefreshToken;
    }

    // 生成新的 access token
    String newAccessToken = jwtUtil.generateAccessToken(claims);

    // 计算新 access token 的过期时间
    DecodedJWT newAccessDecoded = jwtUtil.verifyToken(newAccessToken);
    long expiresIn = jwtUtil.getExpiresInSeconds(newAccessDecoded);

    LoginResponseDTO resp = new LoginResponseDTO();
    resp.setAccessToken(newAccessToken);
    resp.setRefreshToken(effectiveRefreshToken);
    resp.setExpiresIn(expiresIn);
    return resp;
  }

  /** 注册业务校验 */
  private void validateRegisterRequest(RegisterDTO request) {
    // 验证验证码（验证成功后不删除，等注册成功后再删除）
    verificationCodeService.verifyCodeWithoutDelete(request.getEmail(), request.getVerifyCode());

    if (userMapper.checkEmailExist(request.getEmail()) != null) {
      throw new BusinessException(ErrorCode.EMAIL_ALREADY_REGISTERED);
    }

    if (userMapper.checkUsernameExist(request.getUsername()) != null) {
      throw new BusinessException(ErrorCode.USER_AUTH_USER_HAS_EXISTED);
    }

    if (userMapper.checkPhoneExist(request.getPhone()) != null) {
      throw new BusinessException(ErrorCode.USER_AUTH_PHONE_HAS_EXISTED);
    }
  }

  /** 登录业务校验 */
  private User validateLoginRequest(LoginDTO request) {
    User loginUser = userMapper.findByUserName(request.getUsername());
    if (loginUser == null) {
      throw new BusinessException(ErrorCode.USER_AUTH_USER_NOT_EXIST);
    }

    if (!passwordEncoder.matches(request.getPassword(), loginUser.getPassword())) {
      throw new BusinessException(ErrorCode.USER_AUTH_USER_PASSWORD_ERROR);
    }

    if (loginUser.getStatus() == null || loginUser.getStatus() == 0) {
      throw new BusinessException(ErrorCode.ACCOUNT_DISABLED);
    }

    return loginUser;
  }

  /**
   * 删除旧头像
   *
   * @param oldAvatarUrl 旧头像URL
   * @param newAvatarUrl 新头像URL
   */
  private void removeOldAvatar(String oldAvatarUrl, String newAvatarUrl) {
    if (oldAvatarUrl == null || oldAvatarUrl.isBlank()) {
      return;
    }
    if (oldAvatarUrl.equals(newAvatarUrl)) {
      return;
    }
    if (DEFAULT_AVATAR_URL.equals(oldAvatarUrl)) {
      return;
    }

    String objectName = aliOssUtil.extractObjectName(oldAvatarUrl);
    if (objectName == null || objectName.isBlank()) {
      return;
    }
    boolean deleted = aliOssUtil.deleteFile(objectName);
    if (deleted) {
      log.info("旧头像删除成功, objectName={}", objectName);
    } else {
      log.warn("旧头像删除失败, objectName={}", objectName);
    }
  }

  /**
   * 缓存刷新令牌
   *
   * @param userId
   * @param refreshToken
   */
  private void cacheRefreshTokenForSingleLogin(Long userId, String refreshToken) {
    redisTemplate
        .opsForValue()
        .set(
            REFRESH_SINGLE_LOGIN_PREFIX + userId,
            refreshToken,
            refreshTokenValidTime,
            TimeUnit.MILLISECONDS);
  }

  /**
   * 确保单点登录
   *
   * @param refreshToken
   * @param userId
   */
  private void ensureSingleLogin(String refreshToken, Long userId) {
    String key = REFRESH_SINGLE_LOGIN_PREFIX + userId;
    String latestRefresh = redisTemplate.opsForValue().get(key);
    if (latestRefresh == null || !latestRefresh.equals(refreshToken)) {
      throw new BusinessException(ErrorCode.TOKEN_IS_INVALID);
    }
  }

  /**
   * 获取当前刷新令牌
   *
   * @param userId
   * @return
   */
  private String getCurrentRefreshToken(Long userId) {
    return redisTemplate.opsForValue().get(REFRESH_SINGLE_LOGIN_PREFIX + userId);
  }

  /**
   * 加入黑名单
   *
   * @param token
   * @param prefix
   * @param ttl
   */
  private void blacklistToken(String token, String prefix, long ttl) {
    if (token == null) {
      return;
    }
    redisTemplate.opsForValue().set(prefix + token, "blacklisted", ttl, TimeUnit.MILLISECONDS);
  }

  /**
   * 确保不在黑名单中
   *
   * @param token
   * @param prefix
   */
  private void ensureNotBlacklisted(String token, String prefix) {
    if (Boolean.TRUE.equals(redisTemplate.hasKey(prefix + token))) {
      throw new BusinessException(ErrorCode.TOKEN_IS_INVALID);
    }
  }

  /**
   * 删除用户（管理员可删除任何用户，普通用户只能删除自己的账户） 如果用户是求职者，则同时删除求职者信息
   *
   * @param userId 要删除的用户ID
   */
  @Override
  @Transactional(rollbackFor = Exception.class)
  public void deleteUser(Long userId) {
    // 1. 获取当前登录用户信息
    Long currentUserId = userContext.getCurrentUserId();
    User currentUser = userMapper.selectById(currentUserId);
    if (currentUser == null) {
      throw new BusinessException(ErrorCode.USER_ID_NOT_EXIST);
    }

    // 2. 查询要删除的用户
    User targetUser = userMapper.selectById(userId);
    if (targetUser == null) {
      throw new BusinessException(ErrorCode.USER_ID_NOT_EXIST);
    }

    // 3. 权限检查：管理员可以删除任何用户，普通用户只能删除自己的账户
    boolean isAdmin = currentUser.getUserType() != null && currentUser.getUserType() == 3;
    boolean isSelf = currentUserId.equals(userId);

    if (!isAdmin && !isSelf) {
      throw new BusinessException(ErrorCode.CANNOT_DELETE_OTHER_USER);
    }

    if (isAdmin) {
      log.info("管理员 {} 开始删除用户 {}", currentUserId, userId);
    } else {
      log.info("用户 {} 开始删除自己的账户", currentUserId);
    }

    // 4. 如果用户是求职者，删除求职者相关信息
    if (targetUser.getUserType() != null && targetUser.getUserType() == 1) {
      if (seekerApi != null) {
        seekerApi.deleteJobSeekerByUserId(userId);
      } else {
        log.warn("SeekerApi 未注入，跳过删除求职者数据");
      }
    }

    // 5. 删除用户相关的消息和会话（共同部分，HR和求职者都需要删除）
    if (messageApi != null) {
      messageApi.deleteUserMessages(userId);
    } else {
      log.warn("MessageApi 未注入，跳过删除消息数据");
    }

    // 6. 删除用户记录
    userMapper.deleteById(userId);
    log.info("删除用户成功，用户ID：{}", userId);
  }
}
