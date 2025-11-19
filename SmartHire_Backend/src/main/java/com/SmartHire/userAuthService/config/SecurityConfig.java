package com.SmartHire.userAuthService.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        // 默认强度 ，如需更高安全性可以使用 new BCryptPasswordEncoder(12)
        return new BCryptPasswordEncoder();
    }
}