package com.SmartHire.adminService.service.impl;

import com.SmartHire.adminService.model.User;
import com.SmartHire.adminService.mapper.UserMapper;
import com.SmartHire.adminService.service.UserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户基础表 服务实现类
 * </p>
 *
 * @author SmartHire Team
 * @since 2025-12-02
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

}
