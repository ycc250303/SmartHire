package com.SmartHire.common.dto.userDto;

import lombok.Data;
import java.io.Serializable;

/**
 * 模块间通用的用户管理信息
 */
@Data
public class UserManagementDTO implements Serializable {
    private Long userId;
    private String username;
    private String email;
    private String phone;
    private String userType;
    private Integer status;
    private String avatarUrl;
    private String createTime;
    private String lastLoginTime;
    private String company;
    private String position;
}
