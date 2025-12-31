package com.SmartHire.common.dto.userDto;

import lombok.Data;
import java.io.Serializable;

/**
 * 模块间通用的用户信息
 */
@Data
public class UserCommonDTO implements Serializable {
    private Long id;
    private String username;
    private String email;
    private String phone;
    private Integer gender;
    private Integer userType;
    private Integer status;
    private String avatarUrl;
}
