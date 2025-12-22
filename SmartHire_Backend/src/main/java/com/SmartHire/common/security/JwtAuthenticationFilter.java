package com.SmartHire.common.security;

import com.SmartHire.common.auth.JwtTokenExtractor;
import com.SmartHire.common.exception.enums.ErrorCode;
import com.SmartHire.common.exception.exception.BusinessException;
import com.SmartHire.common.utils.JwtUtil;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * JWT 认证过滤器
 * - 支持公共路径放行
 * - 支持前缀匹配
 * - 支持 context-path 自动处理
 * - 支持 Redis 黑名单检查
 */
@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private static final String ACCESS_BLACKLIST_PREFIX = "token:blacklist:access:";

  // 公共路径（支持前缀匹配）
  private static final Set<String> PUBLIC_PATHS = Set.of(
      "/health",
      "/swagger-ui",
      "/v3/api-docs",
      "/swagger-resources",
      "/configuration",
      "/user-auth/login",
      "/user-auth/register",
      "/user-auth/send-verification-code",
      "/user-auth/verify-code",
      "/user-auth/refresh-token",
      "/webjars");

  @Autowired
  private JwtUtil jwtUtil;

  @Autowired
  private JwtTokenExtractor tokenExtractor;

  @Autowired
  private RedisTemplate<String, String> redisTemplate;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    String servletPath = request.getServletPath(); // 不含 context-path
    String contextPath = request.getContextPath(); // /smarthire/api
    String fullPath = contextPath + servletPath;
    String method = request.getMethod();

    log.info("请求路径: contextPath={}, servletPath={}, fullPath={}, method={}",
        contextPath, servletPath, fullPath, method);

    // OPTIONS 请求放行
    if ("OPTIONS".equalsIgnoreCase(method)) {
      log.debug("OPTIONS 请求放行");
      filterChain.doFilter(request, response);
      return;
    }

    // 公共路径放行（前缀匹配）
    boolean isPublic = PUBLIC_PATHS.stream().anyMatch(servletPath::startsWith);
    if (isPublic) {
      log.debug("路径匹配公共路径，直接放行: {}", servletPath);
      filterChain.doFilter(request, response);
      return;
    }

    try {
      // 提取 token
      String token = tokenExtractor.extractToken(request);
      ensureNotBlacklisted(token);
      DecodedJWT decoded = jwtUtil.verifyToken(token);

      if (!jwtUtil.isAccessToken(decoded)) {
        log.warn("Refresh token 访问受保护接口, path={}", servletPath);
        throw new BusinessException(ErrorCode.TOKEN_IS_REFRESH_TOKEN);
      }

      // 设置 SecurityContext
      Map<String, Object> claims = jwtUtil.getClaims(decoded);
      UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(claims, null,
          Collections.emptyList());
      SecurityContextHolder.getContext().setAuthentication(authentication);

      filterChain.doFilter(request, response);
    } catch (BusinessException ex) {
      SecurityContextHolder.clearContext();
      log.warn("业务异常: {}", ex.getMessage());
      throw new BusinessAuthenticationException(ex.getMessage());
    } catch (Exception ex) {
      SecurityContextHolder.clearContext();
      log.error("认证失败: {}", ex.getMessage(), ex);
      throw new GeneralAuthenticationException("认证失败: " + ex.getMessage());
    }
  }

  /** 检查 token 是否在黑名单 */
  private void ensureNotBlacklisted(String token) {
    if (token == null || token.isBlank()) {
      throw new BusinessException(ErrorCode.TOKEN_IS_NULL);
    }
    if (Boolean.TRUE.equals(redisTemplate.hasKey(ACCESS_BLACKLIST_PREFIX + token))) {
      throw new BusinessException(ErrorCode.TOKEN_IS_IN_BLACKLIST);
    }
  }

  /** 业务异常转换的认证异常 */
  private static class BusinessAuthenticationException
      extends org.springframework.security.core.AuthenticationException {
    public BusinessAuthenticationException(String message) {
      super(message);
    }
  }

  /** 通用认证异常 */
  private static class GeneralAuthenticationException
      extends org.springframework.security.core.AuthenticationException {
    public GeneralAuthenticationException(String message) {
      super(message);
    }
  }
}
