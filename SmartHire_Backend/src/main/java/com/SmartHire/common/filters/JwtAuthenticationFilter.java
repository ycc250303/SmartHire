package com.SmartHire.common.filters;

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

/** JWT 认证过滤器 统一处理JWT Token的验证和用户信息提取 */
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

  @Autowired private JwtTokenExtractor tokenExtractor;

  @Autowired private RedisTemplate<String, String> redisTemplate;

  /** 过滤器内部处理 */
  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String path = request.getServletPath();

    // 公开路径直接放行
    if (PUBLIC_PATHS.contains(path)) {
      filterChain.doFilter(request, response);
      return;
    }

    try {
      // 从HTTP请求中提取Token
      String token = tokenExtractor.extractToken(request);

      // 检查黑名单
      ensureNotBlacklisted(token);

      // 验证Token
      DecodedJWT decoded = jwtUtil.verifyToken(token);

      // 确保是Access Token
      if (!jwtUtil.isAccessToken(decoded)) {
        log.warn("refresh token 访问受保护接口, path={}", path);
        throw new BusinessException(ErrorCode.TOKEN_IS_REFRESH_TOKEN);
      }

      // 提取Claims并设置到SecurityContext
      Map<String, Object> claims = jwtUtil.getClaims(decoded);
      UsernamePasswordAuthenticationToken authentication =
          new UsernamePasswordAuthenticationToken(claims, null, Collections.emptyList());
      SecurityContextHolder.getContext().setAuthentication(authentication);

      filterChain.doFilter(request, response);
    } catch (BusinessException ex) {
      SecurityContextHolder.clearContext();
      // 将BusinessException转换为AuthenticationException，让Spring Security处理
      throw new org.springframework.security.core.AuthenticationException(ex.getMessage()) {
        @Override
        public String getMessage() {
          return ex.getMessage();
        }
      };
    } catch (Exception ex) {
      SecurityContextHolder.clearContext();
      // 其他异常也转换为AuthenticationException
      throw new org.springframework.security.core.AuthenticationException(
          "认证失败: " + ex.getMessage()) {
        @Override
        public String getMessage() {
          return ex.getMessage();
        }
      };
    }
  }

  /** 确保Token不在黑名单中 */
  private void ensureNotBlacklisted(String token) {
    if (token == null || token.isBlank()) {
      throw new BusinessException(ErrorCode.TOKEN_IS_NULL);
    }
    if (Boolean.TRUE.equals(redisTemplate.hasKey(ACCESS_BLACKLIST_PREFIX + token))) {
      throw new BusinessException(ErrorCode.TOKEN_IS_INVALID);
    }
  }
}
