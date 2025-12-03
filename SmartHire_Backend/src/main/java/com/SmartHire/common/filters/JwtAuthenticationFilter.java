package com.SmartHire.common.filters;

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

/** JWT 认证过滤器 */
@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private static final String ACCESS_BLACKLIST_PREFIX = "token:blacklist:access:";
  private static final Set<String> PUBLIC_PATHS =
      Set.of(
          "/user-auth/login",
          "/user-auth/register",
          "/user-auth/send-verification-code",
          "/user-auth/verify-code",
          "/user-auth/refresh-token");

  @Autowired private JwtUtil jwtUtil;

  @Autowired private RedisTemplate<String, String> redisTemplate;

  /** 过滤器内部逻辑 */
  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String path = request.getServletPath();
    if (PUBLIC_PATHS.contains(path)) {
      filterChain.doFilter(request, response);
      return;
    }

    String token = request.getHeader("Authorization");
    try {
      ensureNotBlacklisted(token);
      DecodedJWT decoded = jwtUtil.verifyToken(token);
      if (!jwtUtil.isAccessToken(decoded)) {
        log.warn("refresh token 访问受保护接口, path={} token={}", path, token);
        throw new BusinessException(ErrorCode.TOKEN_IS_REFRESH_TOKEN);
      }
      Map<String, Object> claims = jwtUtil.getClaims(decoded);

      UsernamePasswordAuthenticationToken authentication =
          new UsernamePasswordAuthenticationToken(claims, null, Collections.emptyList());
      SecurityContextHolder.getContext().setAuthentication(authentication);
      filterChain.doFilter(request, response);
    } catch (BusinessException ex) {
      SecurityContextHolder.clearContext();
      throw ex;
    } finally {
      SecurityContextHolder.clearContext();
    }
  }

  /** 确保 token 不在黑名单中 */
  private void ensureNotBlacklisted(String token) {
    if (token == null || token.isBlank()) {
      throw new BusinessException(ErrorCode.TOKEN_IS_NULL);
    }
    if (Boolean.TRUE.equals(redisTemplate.hasKey(ACCESS_BLACKLIST_PREFIX + token))) {
      throw new BusinessException(ErrorCode.TOKEN_IS_INVALID);
    }
  }
}
