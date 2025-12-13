package com.SmartHire.adminService.service;

import com.SmartHire.adminService.dto.UserManagementDTO;
import com.SmartHire.adminService.dto.UserStatusUpdateDTO;
import com.SmartHire.userAuthService.model.User;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

/**
 * 用户基础表 服务类
 *
 * @author SmartHire Team
 * @since 2025-12-02
 */
public interface UserService extends IService<User> {

  /**
   * 分页查询用户管理列表
   *
   * @param page 分页参数
   * @param userType 用户类型筛选
   * @param status 状态筛选
   * @param keyword 关键词搜索
   * @return 用户管理DTO列表
   */
  Page<UserManagementDTO> getUserManagementList(
      Page<UserManagementDTO> page, String userType, String status, String keyword);

  /**
   * 更新用户状态
   *
   * @param userStatusUpdateDTO 状态更新DTO
   * @return 是否成功
   */
  boolean updateUserStatus(UserStatusUpdateDTO userStatusUpdateDTO);

  /**
   * 根据用户ID获取用户管理信息
   *
   * @param userId 用户ID
   * @return 用户管理DTO
   */
  UserManagementDTO getUserManagementInfo(Long userId);

  /**
   * 检查用户是否存在
   *
   * @param userId 用户ID
   * @return 是否存在
   */
  boolean userExists(Long userId);

  /**
   * 批量更新用户状态
   *
   * @param userIds 用户ID列表
   * @param status 目标状态
   * @param operatorId 操作管理员ID
   * @param operatorName 操作管理员姓名
   * @param reason 操作原因
   * @return 成功数量
   */
  int batchUpdateUserStatus(
      List<Long> userIds, Byte status, Long operatorId, String operatorName, String reason);

  /**
   * 根据用户类型和状态获取用户数量
   *
   * @param userType 用户类型
   * @param status 状态
   * @return 用户数量
   */
  int countUsersByTypeAndStatus(String userType, String status);

  /**
   * 获取用户统计信息
   *
   * @return 统计信息Map
   */
  java.util.Map<String, Object> getUserStatistics();
}
