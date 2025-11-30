package com.SmartHire.recruitmentService.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 投递/推荐记录表
 * </p>
 *
 * @author SmartHire Team
 * @since 2025-11-30
 */
@Getter
@Setter
public class Application implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 记录ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 职位ID
     */
    private Long jobId;

    /**
     * 求职者ID
     */
    private Long jobSeekerId;

    /**
     * 简历ID（附件简历ID）
     * 如果为null，表示投递的是在线简历；如果不为null，表示投递的是指定的附件简历
     * 推荐时可为空
     */
    private Long resumeId;

    /**
     * 发起方：0-求职者投递 1-HR推荐
     */
    private Byte initiator;

    /**
     * 状态： 0-已投递/已推荐 1-已查看 2-待面试 3-已面试 4-已录用 5-已拒绝 6-已撤回
     */
    private Byte status;

    /**
     * 匹配度分数（0-100）
     */
    private BigDecimal matchScore;

    /**
     * 匹配分析（JSON格式）
     */
    private String matchAnalysis;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;
}
