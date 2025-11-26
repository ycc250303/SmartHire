package com.SmartHire.userAuthService.service;

import com.SmartHire.userAuthService.dto.*;
import com.SmartHire.userAuthService.model.User;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * <p>
 * 用户基础表 服务类
 * </p>
 *
 * @author SmartHire Team
 * @since 2025-11-15
 */
public interface UserAuthService extends IService<User> {

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户
     */
    User findByUserName(String username);

    /**
     * 注册
     *
     * @param request 注册请求
     */
    void register(RegisterDTO request);

    /**
     * 登录
     *
     * @param request 登录请求
     * @return LoginResponseDTO
     */
    LoginResponseDTO login(LoginDTO request);

    /**
     * 获取用户信息（完整信息，仅自己可查看）
     *
     * @return 用户信息DTO
     */
    UserInfoDTO getUserInfo();

    /**
     * 获取公开用户信息（他人可查看，不包含隐私信息）
     *
     * @param userId 用户ID
     * @return 公开用户信息DTO
     */
    PublicUserInfoDTO getPublicUserInfo(Long userId);

    /**
     * 更新用户头像
     *
     * @param avatarFile 头像文件
     * @return 用户头像URL
     */
    String updateUserAvatar(MultipartFile avatarFile) throws IOException;

    /**
     * 登出
     */
    void logout();

    /**
     * 刷新令牌
     *
     * @param refreshToken 刷新令牌
     * @return 新的令牌
     */
    LoginResponseDTO refreshToken(String refreshToken);
}
