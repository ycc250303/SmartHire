package com.SmartHire.userAuthService.utils;

import java.util.regex.Pattern;

/**
 * 邮箱验证工具类
 */
public class EmailValidatorUtil {

    // 邮箱格式正则表达式（支持主流邮箱）
    private static final String EMAIL_PATTERN =
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";

    private static final Pattern pattern = Pattern.compile(EMAIL_PATTERN);

    /**
     * 验证邮箱格式是否正确
     *
     * @param email 邮箱地址
     * @return true-格式正确，false-格式错误
     */
    public static boolean isValid(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return pattern.matcher(email.trim()).matches();
    }
}