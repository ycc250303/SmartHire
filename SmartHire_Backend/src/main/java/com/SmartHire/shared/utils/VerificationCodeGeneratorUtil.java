package com.SmartHire.shared.utils;

import java.security.SecureRandom;

/**
 * 验证码生成工具类
 */
public class VerificationCodeGeneratorUtil {
    private static final String DIGITS = "0123456789";
    private static final SecureRandom random = new SecureRandom();

    /**
     * 生成指定位数的数字验证码
     *
     * @param length 验证码长度（默认6位）
     * @return 验证码字符串
     */
    public static String generate(int length){
        if(length<=0){
            length = 6;
        }

        StringBuilder code = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            code.append(DIGITS.charAt(random.nextInt(DIGITS.length())));
        }
        return code.toString();
    }

    /**
     * 默认生成6位数字验证码
     *
     * @return 验证码字符串
     */
    public static String generate(){
        return generate(6);
    }
}