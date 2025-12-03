package com.SmartHire.adminService.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 用户基础表
 * </p>
 *
 * @author SmartHire Team
 * @since 2025-12-02
 */
@Getter
@Setter
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 加密密码
     */
    private String password;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 用户类型：1-求职者 2-HR
     */
    private Byte userType;

    /**
     * 账户状态：0-禁用 1-正常
     */
    private Byte status;

    /**
     * 头像URL
     */
    private String avatarUrl;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

    /**
     * 最后登录时间
     */
    private Date lastLoginAt;

    /**
     * 性别 0-男 1-女 2-不愿透露
     */
    private Byte gender;
}
