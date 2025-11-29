package com.SmartHire.seekerService.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 简历基础表（用于存储用户上传的文件简历）
 * </p>
 *
 * @author SmartHire Team
 * @since 2025-11-19
 */
@Getter
@Setter
@TableName("resume")
public class Resume implements Serializable {

    @Serial
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
     * 完整度（0-100）
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
