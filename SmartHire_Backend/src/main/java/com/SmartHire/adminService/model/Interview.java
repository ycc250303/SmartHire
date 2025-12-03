package com.SmartHire.adminService.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 面试安排表
 * </p>
 *
 * @author SmartHire Team
 * @since 2025-12-02
 */
@Getter
@Setter
public class Interview implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 面试ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 投递记录ID
     */
    private Long applicationId;

    /**
     * 面试类型：1-电话 2-视频 3-现场
     */
    private Byte interviewType;

    /**
     * 面试轮次
     */
    private Integer interviewRound;

    /**
     * 面试时间
     */
    private Date interviewTime;

    /**
     * 时长（分钟）
     */
    private Integer duration;

    /**
     * 面试地点
     */
    private String location;

    /**
     * 视频链接
     */
    private String meetingLink;

    /**
     * 面试官
     */
    private String interviewer;

    /**
     * 状态：0-待确认 1-已确认 2-已完成 3-已取消
     */
    private Byte status;

    /**
     * 面试反馈
     */
    private String feedback;

    /**
     * 面试结果：0-未通过 1-通过
     */
    private Byte result;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;
}
