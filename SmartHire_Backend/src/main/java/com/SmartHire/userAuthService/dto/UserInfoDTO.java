package com.SmartHire.userAuthService.dto;

import lombok.Data;

/**
 * 用户信息DTO
 *
 * @author SmartHire Team
 */
@Data
public class UserInfoDTO {
    /**
     * 用户名
     */
    private String username;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 性别：女、男、不愿透露
     */
    private Integer gender;

    /**
     * 用户类型：1-求职者 2-HR
     */
    private Integer userType;

    /**
     * 头像URL
     */
    private String avatarUrl;
}
