package com.SmartHire.hrService.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * 投诉实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("complaint")
public class Complaint implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 投诉ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 投诉人ID（用户ID）
     */
    private Long complainantId;

    /**
     * 投诉人类型：1-求职者 2-HR
     */
    private Integer complainantType;

    /**
     * 被投诉对象类型：1-职位 2-HR 3-求职者 4-公司
     */
    private Integer targetType;

    /**
     * 被投诉对象ID
     */
    private Long targetId;

    /**
     * 投诉类型：1-虚假信息 2-骚扰行为 3-不当言论 4-其他
     */
    private Integer complaintType;

    /**
     * 投诉标题
     */
    private String title;

    /**
     * 投诉内容
     */
    private String content;

    /**
     * 投诉证据（JSON格式，可存储图片URL等）
     */
    private String evidence;

    /**
     * 投诉状态：0-待处理 1-处理中 2-已处理 3-已驳回
     */
    private Integer status;

    /**
     * 处理人ID（管理员ID）
     */
    private Long handlerId;

    /**
     * 处理结果
     */
    private String handleResult;

    /**
     * 处理时间
     */
    private Date handledAt;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

}

