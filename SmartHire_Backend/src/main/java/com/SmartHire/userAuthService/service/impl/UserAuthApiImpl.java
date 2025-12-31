package com.SmartHire.userAuthService.service.impl;

import com.SmartHire.common.api.UserAuthApi;
import com.SmartHire.common.dto.userDto.UserCommonDTO;
import com.SmartHire.common.dto.userDto.UserManagementDTO;
import com.SmartHire.common.exception.enums.ErrorCode;
import com.SmartHire.common.exception.exception.BusinessException;
import com.SmartHire.userAuthService.mapper.UserAuthMapper;
import com.SmartHire.userAuthService.model.User;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.List;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** 用户认证服务API实现类 用于模块间通信 */
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
  public UserCommonDTO getUserById(Long userId) {
    if (userId == null) {
      throw new BusinessException(ErrorCode.VALIDATION_ERROR);
    }
    User user = userAuthMapper.selectById(userId);
    if (user == null) {
      throw new BusinessException(ErrorCode.USER_ID_NOT_EXIST);
    }
    UserCommonDTO dto = new UserCommonDTO();
    BeanUtils.copyProperties(user, dto);
    return dto;
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

  @Override
  public Page<UserManagementDTO> getUserManagementList(
      Page<UserManagementDTO> page,
      String userType,
      String status,
      String keyword) {
    // 调用 Mapper 进行分页查询，结果会自动映射到 UserManagementDTO
    return userAuthMapper.selectUserManagementList(page, userType, status, keyword);
  }

  @Override
  public UserManagementDTO getUserManagementInfo(Long userId) {
    if (userId == null) {
      return null;
    }
    return userAuthMapper.selectUserManagementInfo(userId);
  }

  @Override
  public int batchUpdateUserStatus(List<Long> userIds, Byte status) {
    if (userIds == null || userIds.isEmpty() || status == null) {
      return 0;
    }
    return userAuthMapper.batchUpdateStatus(userIds, status);
  }

  @Override
  public int countUsersByTypeAndStatus(String userType, String status) {
    return userAuthMapper.countByTypeAndStatus(userType, status);
  }

  @Override
  public boolean updateUser(UserCommonDTO userDto) {
    if (userDto == null || userDto.getId() == null) {
      return false;
    }
    User user = new User();
    BeanUtils.copyProperties(userDto, user);
    return userAuthMapper.updateById(user) > 0;
  }
}
