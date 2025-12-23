package com.SmartHire.common.event;

import java.io.Serial;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 会话创建事件 DTO
 * 用于在微服务间传递会话创建事件，将会话ID更新回Application记录
 *
 * @author SmartHire Team
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversationCreatedEvent implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 会话ID */
    private Long conversationId;

    /** 用户1ID */
    private Long user1Id;

    /** 用户2ID */
    private Long user2Id;

    /** 创建时间 */
    private java.util.Date createdAt;
    
    /** 关联的申请ID列表（可选，用于精确更新Application记录） */
    private java.util.List<Long> relatedApplicationIds;
}
