package com.SmartHire.userAuthService.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

/**
 * 登录响应数据
 */
@Data
public class LoginResponseDTO {
    private String accessToken;
    private String refreshToken;
    private long expiresIn; // accessToken 剩余秒数
}