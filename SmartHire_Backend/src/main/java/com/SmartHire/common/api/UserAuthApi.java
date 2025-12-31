package com.SmartHire.common.api;

import com.SmartHire.common.dto.userDto.UserCommonDTO;
import com.SmartHire.common.dto.userDto.UserManagementDTO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.List;

/** 用户认证服务API接口 用于模块间通信，避免直接访问数据库 */
public interface UserAuthApi {

  /**
   * 根据用户ID获取用户信息
   *
   * @param userId 用户ID
   * @return 用户信息
   */
  UserCommonDTO getUserById(Long userId);

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
   * @param userId   用户ID
   * @param userType 用户类型（1-求职者，2-HR）
   * @return 是否符合类型
   */
  boolean validateUserType(Long userId, Integer userType);

  /**
   * 分页查询用户管理列表
   *
   * @param page     分页参数
   * @param userType 用户类型筛选
   * @param status   状态筛选
   * @param keyword  关键词搜索
   * @return 用户管理DTO列表
   */
  Page<UserManagementDTO> getUserManagementList(
      Page<UserManagementDTO> page,
      String userType,
      String status,
      String keyword);

  /**
   * 根据用户ID获取用户管理信息
   *
   * @param userId 用户ID
   * @return 用户管理DTO
   */
  UserManagementDTO getUserManagementInfo(Long userId);

  /**
   * 批量更新用户状态
   *
   * @param userIds 用户ID列表
   * @param status  目标状态
   * @return 影响行数
   */
  int batchUpdateUserStatus(List<Long> userIds, Byte status);

  /**
   * 根据用户类型和状态统计用户数量
   *
   * @param userType 用户类型
   * @param status   状态
   * @return 用户数量
   */
  int countUsersByTypeAndStatus(String userType, String status);

  /**
   * 更新用户信息
   *
   * @param user 用户信息
   * @return 是否更新成功
   */
  boolean updateUser(UserCommonDTO user);
}
