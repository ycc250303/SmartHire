package com.SmartHire.adminService.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 求职者信息表
 * </p>
 *
 * @author SmartHire Team
 * @since 2025-12-02
 */
@Getter
@Setter
@TableName("job_seeker")
public class JobSeeker implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 求职者ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 出生日期
     */
    private Date birthDate;

    /**
     * 当前城市
     */
    private String currentCity;

    /**
     * 最高学历 0-高中及以下 1-专科 2-本科 3-硕士 4-博士'
     */
    private Byte education;

    /**
     * 求职状态 0-离校-尽快到岗 1-在校-尽快到岗 2-在校-考虑机会 3-在校-暂不考虑
     */
    private Byte jobStatus;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

    /**
     * 毕业年份
     */
    private String graduationYear;
}
