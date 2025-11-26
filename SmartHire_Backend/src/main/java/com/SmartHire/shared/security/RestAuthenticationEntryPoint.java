package com.SmartHire.shared.security;

import com.SmartHire.shared.entity.Result;
import com.SmartHire.shared.exception.enums.ErrorCode;
import com.SmartHire.shared.exception.exception.BusinessException;
import com.SmartHire.shared.utils.JwtUtil;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/**
 * 未登录时返回的错误信息
 */
@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final JwtUtil jwtUtil;

    public RestAuthenticationEntryPoint(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void commence(HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        Result<?> result = determineErrorResult(request);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }

    private Result<?> determineErrorResult(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && !token.isBlank()) {
            try {
                DecodedJWT decodedJWT = jwtUtil.verifyToken(token);
                if (jwtUtil.isRefreshToken(decodedJWT)) {
                    return Result.error(ErrorCode.TOKEN_IS_REFRESH_TOKEN);
                }
            } catch (BusinessException ex) {
                return Result.error(ex.getCode(), ex.getMessage());
            } catch (Exception ignored) {
            }
        }
        return Result.error(ErrorCode.USER_NOT_LOGIN);
    }
}
