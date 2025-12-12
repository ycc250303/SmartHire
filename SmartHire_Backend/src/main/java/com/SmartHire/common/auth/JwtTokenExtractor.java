package com.SmartHire.common.auth;

import com.SmartHire.common.exception.enums.ErrorCode;
import com.SmartHire.common.exception.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/** JWT Token提取工具类 统一从HTTP请求中提取JWT Token */
@Slf4j
@Component
public class JwtTokenExtractor {

    private static final String AUTHORIZATION_HEADER = "Authorization";

    /**
     * 从当前HTTP请求中提取JWT Token
     *
     * @return JWT Token字符串
     * @throws BusinessException 如果请求上下文不存在或Token为空
     */
    public String extractToken() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new BusinessException(ErrorCode.USER_NOT_LOGIN);
        }

        HttpServletRequest request = attributes.getRequest();
        String token = request.getHeader(AUTHORIZATION_HEADER);

        if (token == null || token.isBlank()) {
            throw new BusinessException(ErrorCode.TOKEN_IS_NULL);
        }

        // 处理 "Bearer " 前缀
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        return token;
    }

    /**
     * 从当前HTTP请求中提取JWT Token（可为空）
     *
     * @return JWT Token字符串，如果不存在则返回null
     */
    public String extractTokenNullable() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }

        HttpServletRequest request = attributes.getRequest();
        String token = request.getHeader(AUTHORIZATION_HEADER);

        if (token == null || token.isBlank()) {
            return null;
        }

        // 处理 "Bearer " 前缀
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        return token;
    }

    /**
     * 从指定的HTTP请求中提取JWT Token
     *
     * @param request HTTP请求对象
     * @return JWT Token字符串
     * @throws BusinessException 如果Token为空
     */
    public String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);
        log.info("📥 提取Token - Authorization header: {}",
                authHeader != null ? authHeader.substring(0, Math.min(50, authHeader.length())) + "..." : "null");

        if (authHeader == null || authHeader.isBlank()) {
            log.warn("❌ Authorization header为空");
            throw new BusinessException(ErrorCode.TOKEN_IS_NULL);
        }

        // 处理 "Bearer " 前缀
        String token = authHeader;
        if (authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            log.info("✅ 已移除Bearer前缀，token长度: {}", token.length());
        } else {
            log.warn("⚠️ Authorization header不包含Bearer前缀");
        }

        return token;
    }

    /**
     * 从指定的HTTP请求中提取JWT Token（可为空）
     *
     * @param request HTTP请求对象
     * @return JWT Token字符串，如果不存在则返回null
     */
    public String extractTokenNullable(HttpServletRequest request) {
        String token = request.getHeader(AUTHORIZATION_HEADER);
        if (token == null || token.isBlank()) {
            return null;
        }
        // 处理 "Bearer " 前缀
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return token;
    }
}d