package com.SmartHire.adminService.mapper;

import com.SmartHire.adminService.dto.UserManagementDTO;
import com.SmartHire.userAuthService.model.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户基础表 Mapper 接口
 *
 * @author SmartHire Team
 * @since 2025-12-02
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

  /**
   * 分页查询用户管理列表
   *
   * @param page 分页参数
   * @param userType 用户类型筛选
   * @param status 状态筛选
   * @param keyword 关键词搜索
   * @return 用户管理DTO列表
   */
  Page<UserManagementDTO> selectUserManagementList(
      Page<UserManagementDTO> page,
      @Param("userType") String userType,
      @Param("status") String status,
      @Param("keyword") String keyword);

  /**
   * 根据用户ID获取用户管理信息
   *
   * @param userId 用户ID
   * @return 用户管理DTO
   */
  UserManagementDTO selectUserManagementInfo(@Param("userId") Long userId);

  /**
   * 批量更新用户状态
   *
   * @param userIds 用户ID列表
   * @param status 目标状态
   * @return 影响行数
   */
  int batchUpdateStatus(@Param("userIds") List<Long> userIds, @Param("status") Byte status);

  /**
   * 根据用户类型和状态统计用户数量
   *
   * @param userType 用户类型
   * @param status 状态
   * @return 用户数量
   */
  int countByTypeAndStatus(@Param("userType") String userType, @Param("status") String status);
}
