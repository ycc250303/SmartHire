package com.SmartHire.shared.interceptors;

import com.SmartHire.shared.exception.enums.ErrorCode;
import com.SmartHire.shared.exception.exception.BusinessException;
import com.SmartHire.shared.utils.JwtUtil;
import com.SmartHire.shared.utils.ThreadLocalUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;

@Slf4j
@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String requestURI = request.getRequestURI();
        String token = request.getHeader("Authorization");
        log.debug("LoginInterceptor拦截请求: URI={}, Token存在={}", requestURI, token != null);

        try {
            Map<String, Object> claims = JwtUtil.parseToken(token);
            ThreadLocalUtil.set(claims);
            log.debug("Token验证成功，用户ID: {}", claims.get("id"));
            return true;
        } catch (BusinessException e) {
            // 如果是业务异常，直接抛出
            log.warn("Token验证失败: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            // 其他异常统一转换为用户未登录异常
            log.warn("Token解析异常: {}", e.getMessage());
            throw new BusinessException(ErrorCode.USER_NOT_LOGIN);
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
        // 请求完成后清理ThreadLocal，防止内存泄漏
        ThreadLocalUtil.remove();
    }
}
