package com.SmartHire.shared.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 要求用户具有特定角色
 * 用于Controller方法或类上，统一进行身份验证
 * 
 * @author SmartHire Team
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {
    /**
     * 允许的用户类型
     * 1 = 求职者, 2 = HR, 3 = 管理员
     */
    int[] value();
}

