package com.SmartHire.userAuthService.service;

import com.SmartHire.userAuthService.dto.LoginDTO;
import com.SmartHire.userAuthService.dto.PublicUserInfoDTO;
import com.SmartHire.userAuthService.dto.RegisterDTO;
import com.SmartHire.userAuthService.dto.UserInfoDTO;
import com.SmartHire.userAuthService.model.User;
import com.baomidou.mybatisplus.extension.service.IService;

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
     * @return token
     */
    String login(LoginDTO request);

    /**
     * 获取用户信息（完整信息，仅自己可查看）
     *
     * @param userId 用户ID
     * @return 用户信息DTO
     */
    UserInfoDTO getUserInfo(Long userId);

    /**
     * 获取公开用户信息（他人可查看，不包含隐私信息）
     *
     * @param userId 用户ID
     * @return 公开用户信息DTO
     */
    PublicUserInfoDTO getPublicUserInfo(Long userId);
}
