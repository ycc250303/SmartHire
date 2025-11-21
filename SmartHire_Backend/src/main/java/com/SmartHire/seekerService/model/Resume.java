package com.SmartHire.seekerService.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import java.io.Serial;
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
 * @since 2025-11-19
 */
@Getter
@Setter
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
     * 是否默认简历
     */
    private Byte isDefault;

    /**
     * 隐私级别：1-完全公开 2-部分公开 3-仅投递可见
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
     * 状态：0-草稿 1-已发布
     */
    private Byte status;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;
}
