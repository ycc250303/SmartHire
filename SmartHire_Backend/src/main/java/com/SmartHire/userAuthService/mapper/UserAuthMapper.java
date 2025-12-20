package com.SmartHire.userAuthService.mapper;

import com.SmartHire.userAuthService.model.User;
import com.SmartHire.adminService.dto.UserManagementDTO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 用户基础表 Mapper 接口
 *
 * @author SmartHire Team
 * @since 2025-11-15
 */
@Mapper
public interface UserAuthMapper extends BaseMapper<User> {

  // 根据用户名查询用户信息
  @Select("select * from user where username=#{username}")
  User findByUserName(String username);

  // 检查用户名是否存在
  @Select("select id from user where username= #{username} limit 1")
  Long checkUsernameExist(String username);

  // 检查邮箱是否存在
  @Select("select id from user where email= #{email} limit 1")
  Long checkEmailExist(String email);

  // 检查手机号是否存在
  @Select("select id from user where phone= #{phone} limit 1")
  Long checkPhoneExist(String phone);

  // 根据ID查询用户信息
  @Select("select * from user where id = #{id}")
  User findById(Long id);

  // 更新用户头像
  @Update("update user set avatar_url = #{avatarUrl},updated_at=now() where id = #{id}")
  void updateUserAvator(String avatarUrl, Long id);

  /**
   * 分页查询用户管理列表
   */
  Page<UserManagementDTO> selectUserManagementList(
      Page<UserManagementDTO> page,
      String userType,
      String status,
      String keyword);

  /**
   * 根据用户ID获取用户管理信息
   */
  UserManagementDTO selectUserManagementInfo(Long userId);

  /**
   * 批量更新用户状态
   */
  int batchUpdateStatus(List<Long> userIds, Byte status);

  /**
   * 根据用户类型和状态统计用户数量
   */
  int countByTypeAndStatus(String userType, String status);
}
