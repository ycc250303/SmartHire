package com.SmartHire.userAuthService.mapper;

import com.SmartHire.userAuthService.model.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * <p>
 * 用户基础表 Mapper 接口
 * </p>
 *
 * @author SmartHire Team
 * @since 2025-11-15
 */
@Mapper
public interface UserAuthMapper extends BaseMapper<User> {

    // 根据用户名查询用户信息
    @Select("select * from user where username=#{username}")
    public User findByUserName(String username);

    // 检查用户名是否存在
    @Select("select id from user where username= #{username} limit 1")
    public Long checkUsernameExist(String username);

    // 检查邮箱是否存在
    @Select("select id from user where email= #{email} limit 1")
    public Long checkEmailExist(String email);

    // 检查手机号是否存在
    @Select("select id from user where phone= #{phone} limit 1")
    public Long checkPhoneExist(String phone);

    // 根据ID查询用户信息
    @Select("select * from user where id = #{id}")
    public User findById(Long id);

    // 更新用户头像
    @Update("update user set avatar_url = #{avatarUrl},updated_at=now() where id = #{id}")
    void updateUserAvator(String avatarUrl,Long id);
}
