package com.SmartHire.userAuthService.controller;

import com.SmartHire.common.entity.Result;
import com.SmartHire.userAuthService.dto.*;
import com.SmartHire.userAuthService.service.UserAuthService;
import com.SmartHire.userAuthService.service.VerificationCodeService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户认证服务用户认证控制器
 *
 * @author SmartHire Team
 * @since 2025-11-15
 */
@RestController
@RequestMapping("/user-auth")
@Validated // 启用方法参数验证
public class UserAuthController {

  @Autowired private UserAuthService userService;

  @Autowired private VerificationCodeService verificationCodeService;

  /**
   * 发送邮箱验证码
   *
   * @param email 邮箱地址
   * @return 操作结果
   */
  @PostMapping("/send-verification-code")
  @Operation(summary = "发送邮箱验证码", description = "向指定邮箱发送验证码")
  public Result<?> sendVerificationCode(
      @RequestParam @jakarta.validation.constraints.Email(message = "邮箱格式不正确") String email) {
    // 如果 Service 抛出 BusinessException，会被 GlobalExceptionHandler 自动处理
    verificationCodeService.sendVerificationCode(email);
    return Result.success("验证码发送成功");
  }

  /**
   * 验证邮箱验证码
   *
   * @param email 邮箱地址
   * @param code 验证码
   * @return 操作结果
   */
  @PostMapping("/verify-code")
  @Operation(summary = "验证邮箱验证码", description = "验证指定邮箱的验证码")
  public Result<?> verifyCode(
      @RequestParam @jakarta.validation.constraints.Email(message = "邮箱格式不正确") String email,
      @RequestParam @jakarta.validation.constraints.NotBlank(message = "验证码不能为空") String code) {
    verificationCodeService.verifyCode(email, code);
    return Result.success("验证码验证成功");
  }

  /**
   * 用户注册
   *
   * @param request 注册请求参数（JSON格式）
   * @return 操作结果
   */
  @PostMapping("/register")
  @Operation(summary = "用户注册", description = "新用户注册接口")
  public Result<?> register(@Valid @RequestBody RegisterDTO request) {
    userService.register(request);
    return Result.success("注册成功");
  }

  /**
   * 用户登录
   *
   * @param request 登录请求参数（JSON格式）
   * @return 操作结果
   */
  @PostMapping("/login")
  @Operation(summary = "用户登录")
  public Result<LoginResponseDTO> login(@Valid @RequestBody LoginDTO request) {
    LoginResponseDTO resp = userService.login(request);
    return Result.success("登录成功", resp);
  }

  /**
   * 获取当前登录用户信息（完整信息，仅自己可查看）
   *
   * @return 用户信息
   */
  @GetMapping("/user-info")
  @Operation(summary = "获取当前登录用户信息", description = "获取当前登录用户的完整信息（仅自己可查看）")
  public Result<UserInfoDTO> getUserInfo() {

    UserInfoDTO userInfo = userService.getUserInfo();
    return Result.success("获取用户信息成功", userInfo);
  }

  /**
   * 获取其他用户公开信息（他人可查看，不包含隐私信息）
   *
   * @param userId 用户ID
   * @return 公开用户信息
   */
  @GetMapping("/public-user-info/{userId}")
  @Operation(summary = "获取用户公开信息", description = "通过用户ID获取其他用户的公开信息（不包含邮箱、手机号等隐私信息）")
  public Result<PublicUserInfoDTO> getPublicUserInfo(@PathVariable Long userId) {
    PublicUserInfoDTO publicUserInfo = userService.getPublicUserInfo(userId);
    return Result.success("获取用户公开信息成功", publicUserInfo);
  }

  @PatchMapping("/update-user-avatar")
  @Operation(summary = "更新用户头像", description = "更新用户头像")
  public Result<?> updateUserAvatar(@RequestBody MultipartFile avatarFile) throws IOException {
    String url = userService.updateUserAvatar(avatarFile);
    return Result.success("更新用户头像成功", url);
  }

  @PostMapping("/logout")
  @Operation(summary = "用户登出", description = "用户登出接口")
  public Result<?> logout() {
    userService.logout();
    return Result.success("登出成功");
  }

  @PostMapping("/refresh-token")
  @Operation(summary = "刷新 Access Token")
  public Result<LoginResponseDTO> refreshToken(@Valid @RequestBody RefreshTokenDTO request) {
    LoginResponseDTO resp = userService.refreshToken(request.getRefreshToken());
    return Result.success("刷新成功", resp);
  }

  /**
   * 删除用户（管理员可删除任何用户，普通用户只能删除自己的账户） 如果用户是求职者，则同时删除求职者信息
   *
   * @param userId 要删除的用户ID
   * @return 操作结果
   */
  @DeleteMapping("/user/{userId}")
  @Operation(summary = "删除用户", description = "删除指定用户。管理员可删除任何用户，普通用户只能删除自己的账户。如果用户是求职者，则同时删除求职者信息")
  public Result<?> deleteUser(@PathVariable Long userId) {
    userService.deleteUser(userId);
    return Result.success("删除用户成功");
  }
}
