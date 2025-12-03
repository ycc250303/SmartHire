package com.SmartHire.userAuthService.service.impl;

import com.SmartHire.common.api.UserAuthApi;
import com.SmartHire.common.exception.enums.ErrorCode;
import com.SmartHire.common.exception.exception.BusinessException;
import com.SmartHire.userAuthService.mapper.UserAuthMapper;
import com.SmartHire.userAuthService.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 用户认证服务API实现类
 * 用于模块间通信
 */
@Service
public class UserAuthApiImpl implements UserAuthApi {
    
    @Autowired
    private UserAuthMapper userAuthMapper;

    /**
     * 根据用户ID获取用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    @Override
    public User getUserById(Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        User user = userAuthMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_ID_NOT_EXIST);
        }
        return user;
    }
    
    @Override
    public boolean existsUser(Long userId) {
        if (userId == null) {
            return false;
        }
        return userAuthMapper.selectById(userId) != null;
    }
    
    @Override
    public boolean validateUserType(Long userId, Integer userType) {
        if (userId == null || userType == null) {
            return false;
        }
        User user = userAuthMapper.selectById(userId);
        return user != null && userType.equals(user.getUserType());
    }
}

