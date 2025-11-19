package com.SmartHire.userAuthService.service.impl;

import com.SmartHire.shared.exception.enums.ErrorCode;
import com.SmartHire.shared.exception.exception.BusinessException;
import com.SmartHire.shared.utils.JwtUtil;
import com.SmartHire.userAuthService.dto.LoginDTO;
import com.SmartHire.userAuthService.dto.PublicUserInfoDTO;
import com.SmartHire.userAuthService.dto.RegisterDTO;
import com.SmartHire.userAuthService.dto.UserInfoDTO;
import com.SmartHire.userAuthService.model.User;
import com.SmartHire.userAuthService.mapper.UserAuthMapper;
import com.SmartHire.userAuthService.service.UserAuthService;
import com.SmartHire.userAuthService.service.VerificationCodeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 用户基础表 服务实现类
 * </p>
 *
 * @author SmartHire Team
 * @since 2025-11-15
 */
@Slf4j
@Service
public class UserAuthServiceImpl extends ServiceImpl<UserAuthMapper, User> implements UserAuthService {
    @Autowired
    private UserAuthMapper userMapper;

    @Autowired
    private VerificationCodeService verificationCodeService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public User findByUserName(String username) {
        return userMapper.findByUserName(username);
    }

    /**
     * 注册
     */
    @Override
    public void register(RegisterDTO request) {
        // 统一业务校验
        validateRegisterRequest(request);

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setGender(request.getGender());

        user.setUserType(request.getUserType());
        user.setStatus(1);
        user.setAvatarUrl("https://wanx.alicdn.com/material/20250318/first_frame.png");

        Date now = new Date();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        userMapper.insert(user);
        log.info("用户注册成功，用户ID: {}, 用户名: {}, 邮箱: {}, 手机号: {}", user.getId(), user.getUsername(), user.getEmail(),
                user.getPhone());
    }

    /**
     * 登录
     */
    @Override
    public String login(LoginDTO request) {
        User loginUser = validateLoginRequest(request);

        loginUser.setLastLoginAt(new Date());
        userMapper.updateById(loginUser);

        Map<String, Object> claims = new HashMap<>();
        claims.put("id", loginUser.getId());
        claims.put("username", loginUser.getUsername());
        return JwtUtil.generateToken(claims);
    }

    /**
     * 注册业务校验
     */
    private void validateRegisterRequest(RegisterDTO request) {
        verificationCodeService.verifyCode(request.getEmail(), request.getVerifyCode());

        if (userMapper.checkEmailExist(request.getEmail()) != null) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_REGISTERED);
        }

        if (userMapper.checkUsernameExist(request.getUsername()) != null) {
            throw new BusinessException(ErrorCode.USER_AUTH_USER_HAS_EXISTED);
        }

        if (userMapper.checkPhoneExist(request.getPhone()) != null) {
            throw new BusinessException(ErrorCode.USER_AUTH_PHONE_HAS_EXISTED);
        }
    }

    /**
     * 登录业务校验
     */
    private User validateLoginRequest(LoginDTO request) {
        User loginUser = userMapper.findByUserName(request.getUsername());
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.USER_AUTH_USER_NOT_EXIST);
        }

        if (!passwordEncoder.matches(request.getPassword(), loginUser.getPassword())) {
            throw new BusinessException(ErrorCode.USER_AUTH_USER_PASSWORD_ERROR);
        }

        return loginUser;
    }

    /**
     * 获取用户信息
     *
     * @param userId 用户ID
     * @return 用户信息DTO
     */
    @Override
    public UserInfoDTO getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_ID_NOT_EXIST);
        }

        UserInfoDTO userInfoDTO = new UserInfoDTO();
        BeanUtils.copyProperties(user, userInfoDTO);
        return userInfoDTO;
    }

    /**
     * 获取公开用户信息（他人可查看，不包含隐私信息）
     *
     * @param userId 用户ID
     * @return 公开用户信息DTO
     */
    @Override
    public PublicUserInfoDTO getPublicUserInfo(Long userId) {
        User user = userMapper.selectById(userId);

        if (user == null) {
            throw new BusinessException(ErrorCode.USER_ID_NOT_EXIST);
        }

        PublicUserInfoDTO publicUserInfoDTO = new PublicUserInfoDTO();
        publicUserInfoDTO.setId(user.getId());
        publicUserInfoDTO.setUsername(user.getUsername());
        publicUserInfoDTO.setUserType(user.getUserType());
        publicUserInfoDTO.setAvatarUrl(user.getAvatarUrl());
        return publicUserInfoDTO;
    }
}
