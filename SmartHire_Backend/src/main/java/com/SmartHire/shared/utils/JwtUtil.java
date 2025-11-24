package com.SmartHire.shared.utils;

import com.SmartHire.shared.exception.enums.ErrorCode;
import com.SmartHire.shared.exception.exception.BusinessException;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;

import java.util.Date;
import java.util.Map;

public class JwtUtil {

    private static final String SECRET_KEY = "SmartHire";

    private static final Integer TOKEN_VALID_TIME = 7 * 24 * 60 * 60 * 1000;

    public static String generateToken(Map<String, Object> claims) {
        return JWT.create()
                .withClaim("claims", claims)
                .withExpiresAt(new Date(System.currentTimeMillis() + TOKEN_VALID_TIME))
                .sign(Algorithm.HMAC256(SECRET_KEY));
    }

    public static Map<String, Object> parseToken(String token) {
        // 检查 token 是否为 null
        if (token == null || token.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.TOKEN_IS_NULL);
        }

        try {
            // JWT.verify() 在 token 无效时会抛出异常，不会返回 null
            Map<String, Object> res = JWT.require(Algorithm.HMAC256(SECRET_KEY))
                    .build()
                    .verify(token)
                    .getClaim("claims")
                    .asMap();

            // 虽然 verify() 不会返回 null，但 getClaim().asMap() 可能返回 null
            if (res == null) {
                throw new BusinessException(ErrorCode.TOKEN_IS_INVALID);
            }

            return res;
        } catch (JWTVerificationException e) {
            // 捕获 JWT 验证异常（token 无效、过期、签名错误等）
            throw new BusinessException(ErrorCode.TOKEN_IS_INVALID);
        } catch (Exception e) {
            // 捕获其他异常（如 token 格式错误等）
            throw new BusinessException(ErrorCode.TOKEN_IS_INVALID);
        }
    }

    /**
     * 从 JWT token 解析的 Map 中安全地获取用户ID（Long类型）
     * 处理 Integer 和 Long 类型兼容问题
     *
     * @param map JWT token 解析后的 Map
     * @return 用户ID（Long类型）
     */
    public static Long getUserIdFromToken(Map<String, Object> map) {
        Object idObj = map.get("id");
        if (idObj == null) {
            return null;
        }
        if (idObj instanceof Long) {
            return (Long) idObj;
        } else if (idObj instanceof Integer) {
            return ((Integer) idObj).longValue();
        } else if (idObj instanceof Number) {
            return ((Number) idObj).longValue();
        } else {
            throw new IllegalArgumentException("用户ID格式不正确，期望数字类型");
        }
    }
}