package com.SmartHire.adminService.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 简历基础表
 * </p>
 *
 * @author SmartHire Team
 * @since 2025-12-02
 */
@Getter
@Setter
public class Resume implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 简历ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 求职者ID
     */
    private Long jobSeekerId;

    /**
     * 简历名称
     */
    private String resumeName;

    /**
     * 隐私级别：1-公开 2-仅投递可见
     */
    private Byte privacyLevel;

    /**
     * 简历文件URL
     */
    private String fileUrl;

    /**
     * 完整度（0-100）(AI评估）
     */
    private Integer completeness;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;
}
