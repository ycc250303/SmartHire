package com.SmartHire.common.security;

import com.SmartHire.common.entity.Result;
import com.SmartHire.common.exception.enums.ErrorCode;
import com.SmartHire.common.exception.exception.BusinessException;
import com.SmartHire.common.utils.JwtUtil;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 未登录时返回的错误信息
 */
@Slf4j
@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final JwtUtil jwtUtil;

    public RestAuthenticationEntryPoint(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException)
            throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        Result<?> result = determineErrorResult(request);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }

    private Result<?> determineErrorResult(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (!StringUtils.hasText(authHeader)) {
            return Result.error(ErrorCode.USER_NOT_LOGIN);
        }

        String token = authHeader.startsWith("Bearer ")
                ? authHeader.substring(7)
                : authHeader;

        try {
            DecodedJWT decodedJWT = jwtUtil.verifyToken(token);

            if (jwtUtil.isRefreshToken(decodedJWT)) {
                return Result.error(ErrorCode.TOKEN_IS_REFRESH_TOKEN);
            }
        } catch (BusinessException ex) {
            return Result.error(ex.getCode(), ex.getMessage());
        } catch (Exception e) {
            log.debug("JWT verification failed", e);
        }
        return Result.error(ErrorCode.USER_NOT_LOGIN);
    }

}
