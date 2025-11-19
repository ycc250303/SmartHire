package com.SmartHire.userAuthService.dto;

import lombok.Data;

/**
 * 公开用户信息DTO（用于他人查看，不包含隐私信息）
 *
 * @author SmartHire Team
 */
@Data
public class PublicUserInfoDTO {
    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 用户类型：1-求职者 2-HR
     */
    private Integer userType;

    /**
     * 头像URL
     */
    private String avatarUrl;
}
