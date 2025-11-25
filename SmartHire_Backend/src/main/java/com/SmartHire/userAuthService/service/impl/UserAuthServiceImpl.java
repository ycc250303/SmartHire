package com.SmartHire.userAuthService.service.impl;

import com.SmartHire.shared.exception.enums.ErrorCode;
import com.SmartHire.shared.exception.exception.BusinessException;
import com.SmartHire.shared.utils.AliOssUtil;
import com.SmartHire.shared.utils.JwtUtil;
import com.SmartHire.shared.utils.ThreadLocalUtil;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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

    @Autowired
    private AliOssUtil aliOssUtil;

    private static final String AVATAR_DIRECTORY_KEY = "avatar";
    private static final String DEFAULT_AVATAR_URL = "https://smart-hire.oss-cn-shanghai.aliyuncs.com/default-avatar.png";

    @Override
    public User findByUserName(String username) {
        return userMapper.findByUserName(username);
    }

    /**
     * 注册
     */
    @Override
    public void register(RegisterDTO request) {
        // 统一业务校验（验证码验证成功但不删除）
        validateRegisterRequest(request);

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setGender(request.getGender());

        user.setUserType(request.getUserType());
        user.setStatus(1);
        user.setAvatarUrl(DEFAULT_AVATAR_URL);

        Date now = new Date();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        userMapper.insert(user);

        // 注册成功后删除验证码
        verificationCodeService.deleteCode(request.getEmail());

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
        // 验证验证码（验证成功后不删除，等注册成功后再删除）
        verificationCodeService.verifyCodeWithoutDelete(request.getEmail(), request.getVerifyCode());

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

        if (loginUser.getStatus() == null || loginUser.getStatus() == 0) {
            throw new BusinessException(ErrorCode.ACCOUNT_DISABLED);
        }

        return loginUser;
    }

    /**
     * 获取用户信息
     *
     * @return 用户信息DTO
     */
    @Override
    public UserInfoDTO getUserInfo() {
        Map<String, Object> map = ThreadLocalUtil.get();
        Long userId = JwtUtil.getUserIdFromToken(map);
        if (userId == null) {
            throw new BusinessException(ErrorCode.USER_ID_NOT_EXIST);
        }
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

    @Override
    public String updateUserAvatar(MultipartFile avatarFile) throws IOException {
        Map<String, Object> map = ThreadLocalUtil.get();
        Long userId = JwtUtil.getUserIdFromToken(map);
        if (userId == null) {
            throw new BusinessException(ErrorCode.USER_ID_NOT_EXIST);
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_ID_NOT_EXIST);
        }
        String oldAvatarUrl = user.getAvatarUrl();

        // 生成文件名
        String originalFileName = avatarFile.getOriginalFilename();
        log.info("originalFileName:" + originalFileName);
        String fileExtension = "";

        // 安全地获取文件扩展名
        if (originalFileName != null && originalFileName.contains(".")) {
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }

        String fileName = UUID.randomUUID().toString() + fileExtension;
        try {
            String avatarUrl = aliOssUtil.uploadFile(AVATAR_DIRECTORY_KEY, fileName, avatarFile.getInputStream());
            userMapper.updateUserAvator(avatarUrl, userId);
            removeOldAvatar(oldAvatarUrl, avatarUrl);
            return avatarUrl;
        } catch (RuntimeException ex) {
            log.error("用户头像上传失败, userId={}, fileName={}", userId, fileName, ex);
            throw new BusinessException(ErrorCode.USER_AVATAR_UPLOAD_FAILED);
        }
    }

    private void removeOldAvatar(String oldAvatarUrl, String newAvatarUrl) {
        if (oldAvatarUrl == null || oldAvatarUrl.isBlank()) {
            return;
        }
        if (oldAvatarUrl.equals(newAvatarUrl)) {
            return;
        }
        if (DEFAULT_AVATAR_URL.equals(oldAvatarUrl)) {
            return;
        }

        String objectName = aliOssUtil.extractObjectName(oldAvatarUrl);
        if (objectName == null || objectName.isBlank()) {
            return;
        }
        boolean deleted = aliOssUtil.deleteFile(objectName);
        if (deleted) {
            log.info("旧头像删除成功, objectName={}", objectName);
        } else {
            log.warn("旧头像删除失败, objectName={}", objectName);
        }
    }
}
