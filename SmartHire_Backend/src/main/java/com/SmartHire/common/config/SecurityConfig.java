package com.SmartHire.common.config;

import com.SmartHire.common.security.JwtAuthenticationFilter;
import com.SmartHire.common.security.RestAuthenticationEntryPoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@ConditionalOnWebApplication
public class SecurityConfig {

        @Bean
        public SecurityFilterChain securityFilterChain(
                        HttpSecurity http,
                        JwtAuthenticationFilter jwtAuthenticationFilter,
                        RestAuthenticationEntryPoint authenticationEntryPoint,
                        CorsConfigurationSource corsConfigurationSource)
                        throws Exception {
                http.csrf(csrf -> csrf.disable())
                                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                                .sessionManagement(
                                                session -> session
                                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authorizeHttpRequests(
                                                auth -> auth.requestMatchers(HttpMethod.OPTIONS, "/**")
                                                                .permitAll()
                                                                .requestMatchers(
                                                                                "/health",
                                                                                "/user-auth/login",
                                                                                "/user-auth/register",
                                                                                "/user-auth/send-verification-code",
                                                                                "/user-auth/verify-code",
                                                                                "/user-auth/refresh-token",
                                                                                "/seeker/public/**",
                                                                                "/swagger-ui/**",
                                                                                "/swagger-ui.html",
                                                                                "/swagger-resources/**",
                                                                                "/configuration/**",
                                                                                "/configuration/security",
                                                                                "/configuration/ui",
                                                                                "/v3/api-docs/**",
                                                                                "/v3/api-docs",
                                                                                "/webjars/**",
                                                                                "/doc.html")
                                                                .permitAll()
                                                                .anyRequest()
                                                                .authenticated())
                                .exceptionHandling(
                                                exception -> exception
                                                                .authenticationEntryPoint(authenticationEntryPoint));

                http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
                return http.build();
        }
}
