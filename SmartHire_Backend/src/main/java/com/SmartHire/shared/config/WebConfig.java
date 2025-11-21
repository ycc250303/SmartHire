package com.SmartHire.shared.config;

import com.SmartHire.shared.interceptors.LoginInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Slf4j
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Autowired
    private LoginInterceptor loginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 登录和注册接口不拦截（不需要包含 context-path 前缀）
        registry.addInterceptor(loginInterceptor)
                .excludePathPatterns(
                        "/user-auth/login",
                        "/user-auth/register",
                        "/user-auth/send-verification-code",
                        "/user-auth/verify-code");

        log.info(
                "LoginInterceptor 已注册，排除路径: /user-auth/login, /user-auth/register, /user-auth/send-verification-code, /user-auth/verify-code");
    }
}
