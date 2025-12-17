package com.SmartHire.common.utils;

import com.SmartHire.common.exception.enums.ErrorCode;
import com.SmartHire.common.exception.exception.BusinessException;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import java.util.Date;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtUtil {

  private final Algorithm algorithm;
  private final long refreshTokenValidTime;
  private final long accessTokenValidTime;

  public JwtUtil(
      @Value("${jwt.secret-key}") String secretKey,
      @Value("${jwt.refresh-token-valid-time}") long refreshTokenValidTime,
      @Value("${jwt.access-token-valid-time}") long accessTokenValidTime) {
    this.algorithm = Algorithm.HMAC256(secretKey);
    this.refreshTokenValidTime = refreshTokenValidTime;
    this.accessTokenValidTime = accessTokenValidTime;
  }

  public String generateAccessToken(Map<String, Object> claims) {
    return JWT.create()
        .withClaim("claims", claims)
        .withClaim("type", "access")
        .withExpiresAt(new Date(System.currentTimeMillis() + accessTokenValidTime))
        .sign(algorithm);
  }

  public String generateRefreshToken(Map<String, Object> claims) {
    return JWT.create()
        .withClaim("claims", claims)
        .withClaim("type", "refresh")
        .withExpiresAt(new Date(System.currentTimeMillis() + refreshTokenValidTime))
        .sign(algorithm);
  }

  /**
   * 验证令牌（基础方法） 验证 token 的有效性（签名、过期时间等），返回完整的 DecodedJWT 适用于需要检查 token 类型、过期时间等完整信息的场景
   *
   * @param token JWT token 字符串
   * @return DecodedJWT 解码后的 JWT 对象
   */
  public DecodedJWT verifyToken(String token) {
    if (token == null || token.isBlank()) {
      log.error("Token验证失败: token为空");
      throw new BusinessException(ErrorCode.TOKEN_IS_NULL);
    }

    log.info(
        "开始验证token，长度: {}, 前缀: {}",
        token.length(),
        token.substring(0, Math.min(20, token.length())) + "...");

    try {
      DecodedJWT decoded = JWT.require(algorithm).build().verify(token);
      log.info("Token验证成功");
      log.info("- Token类型: {}", decoded.getClaim("type").asString());
      log.info("- 过期时间: {}", decoded.getExpiresAt());
      return decoded;
    } catch (JWTVerificationException e) {
      log.error("Token验证失败: {}", e.getMessage());
      log.error("失败的token前缀: {}", token.substring(0, Math.min(30, token.length())));
      throw new BusinessException(ErrorCode.TOKEN_IS_INVALID);
    }
  }

  /**
   * 解析令牌并提取 claims 内部调用 verifyToken 进行验证，然后提取 claims 信息 适用于只需要用户信息（claims）的场景
   *
   * @param token JWT token 字符串
   * @return claims Map，包含用户信息
   */
  public Map<String, Object> parseToken(String token) {
    DecodedJWT decodedJWT = verifyToken(token);
    return getClaims(decodedJWT);
  }

  /**
   * 从 DecodedJWT 中提取 claims 辅助方法，用于从已验证的 DecodedJWT 中提取 claims
   *
   * @param decodedJWT 已验证的 DecodedJWT 对象
   * @return claims Map
   */
  public Map<String, Object> getClaims(DecodedJWT decodedJWT) {
    Map<String, Object> claims = decodedJWT.getClaim("claims").asMap();
    if (claims == null) {
      throw new BusinessException(ErrorCode.TOKEN_IS_INVALID);
    }
    return claims;
  }

  /**
   * 从 JWT token 解析的 Map 中安全地获取用户ID（Long类型） 处理 Integer 和 Long 类型兼容问题
   *
   * @param map JWT token 解析后的 Map
   * @return 用户ID（Long类型）
   */
  public Long getUserIdFromToken(Map<String, Object> map) {
    Object idObj = map.get("id");
    if (idObj == null) {
      return null;
    }
    if (idObj instanceof Long) {
      return (Long) idObj;
    } else if (idObj instanceof Integer) {
      return ((Integer) idObj).longValue();
    } else if (idObj instanceof Number) {
      return ((Number) idObj).longValue();
    } else {
      throw new IllegalArgumentException("用户ID格式不正确，期望数字类型");
    }
  }

  /**
   * 判断 token 是否为 access token
   *
   * @param decodedJWT
   * @return
   */
  public boolean isAccessToken(DecodedJWT decodedJWT) {
    return "access".equals(decodedJWT.getClaim("type").asString());
  }

  /**
   * 判断 token 是否为 refresh token
   *
   * @param decodedJWT
   * @return
   */
  public boolean isRefreshToken(DecodedJWT decodedJWT) {
    return "refresh".equals(decodedJWT.getClaim("type").asString());
  }

  /**
   * 获取 token 的剩余有效时间
   *
   * @param decodedJWT
   * @return
   */
  public long getExpiresInSeconds(DecodedJWT decodedJWT) {
    long remaining = decodedJWT.getExpiresAt().getTime() - System.currentTimeMillis();
    return Math.max(remaining / 1000, 0);
  }
}
