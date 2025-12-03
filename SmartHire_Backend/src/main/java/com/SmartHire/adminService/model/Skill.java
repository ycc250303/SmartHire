package com.SmartHire.adminService.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 技能表（在线简历信息）
 * </p>
 *
 * @author SmartHire Team
 * @since 2025-12-02
 */
@Getter
@Setter
public class Skill implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 技能ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 求职者ID
     */
    private Long jobSeekerId;

    /**
     * 技能名称
     */
    private String skillName;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 技能熟练度 0-了解 1-熟悉 2-掌握
     */
    private Byte level;

    /**
     * 更新时间
     */
    private Date updatedAt;
}
