package com.SmartHire.userAuthService.dto;

import com.SmartHire.userAuthService.utils.ValidPasswordUtil;
import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * 用户注册请求DTO
 *
 * @author SmartHire Team
 */
@Data
public class RegisterDTO {

  /** 用户名 */
  @NotBlank(message = "用户名不能为空")
  @Pattern(regexp = "^[a-zA-Z0-9_]{3,20}$", message = "用户名只能包含字母、数字和下划线，长度3-20位")
  private String username;

  /** 密码 要求：不小于6位，至少包含数字、小写字母、大写字母的两项，不能包含特殊字符 */
  @NotBlank(message = "密码不能为空")
  @ValidPasswordUtil
  private String password;

  /** 邮箱 */
  @NotBlank(message = "邮箱不能为空")
  @Email(message = "邮箱格式不正确")
  private String email;

  /** 手机号 */
  @NotBlank(message = "手机号不能为空")
  @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
  private String phone;

  /** 性别 */
  @NotNull(message = "性别不能为空")
  @Min(value = 0, message = "性别类型格式不正确")
  @Max(value = 2, message = "性别类型格式不正确")
  private int gender;

  /** 用户类型 */
  @NotNull(message = "用户类型不能为空")
  @Min(value = 1, message = "用户类型格式不正确")
  @Max(value = 2, message = "用户类型格式不正确")
  private Integer userType;

  /** 验证码 */
  @NotBlank(message = "验证码不能为空")
  private String verifyCode;
}
