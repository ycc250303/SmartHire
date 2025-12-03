package com.SmartHire.common.api;

import com.SmartHire.userAuthService.model.User;

/**
 * 用户认证服务API接口
 * 用于模块间通信，避免直接访问数据库
 */
public interface UserAuthApi {
    
    /**
     * 根据用户ID获取用户信息
     * 
     * @param userId 用户ID
     * @return 用户信息
     */
    User getUserById(Long userId);
    
    /**
     * 根据用户ID验证用户是否存在
     * 
     * @param userId 用户ID
     * @return 用户是否存在
     */
    boolean existsUser(Long userId);
    
    /**
     * 验证用户类型
     * 
     * @param userId 用户ID
     * @param userType 用户类型（1-求职者，2-HR）
     * @return 是否符合类型
     */
    boolean validateUserType(Long userId, Integer userType);
}

