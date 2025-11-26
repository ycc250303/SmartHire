package com.SmartHire.shared.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.Map;

/**
 * 从 Spring Security 上下文中获取当前请求的认证信息
 */
public final class SecurityContextUtil {

    private SecurityContextUtil() {
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> getCurrentClaims() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return Collections.emptyMap();
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return Collections.emptyMap();
    }
}
