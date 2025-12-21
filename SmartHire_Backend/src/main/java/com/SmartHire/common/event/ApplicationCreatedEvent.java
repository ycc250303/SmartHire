package com.SmartHire.common.event;

import java.io.Serial;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 投递/推荐岗位创建事件 DTO
 * 用于在微服务间传递投递/推荐岗位创建事件，解耦 recruitmentService 和 messageService
 *
 * @author SmartHire Team
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationCreatedEvent implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 投递ID */
    private Long applicationId;

    /** 岗位ID */
    private Long jobId;

    /** 求职者ID */
    private Long jobSeekerId;

    /** 求职者用户ID */
    private Long seekerUserId;

    /** HR ID */
    private Long hrId;

    /** HR用户ID */
    private Long hrUserId;

    /** 消息内容 */
    private String messageContent;

    /** 发送者角色 */
    private Byte initiator; // 0-求职者投递 1-HR推荐
}
