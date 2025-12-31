package com.SmartHire.common.dto.messageDto;

import lombok.Data;
import java.io.Serializable;
import java.util.Date;

/**
 * 模块间通用的消息传输对象
 */
@Data
public class MessageCommonDTO implements Serializable {
    private Long id;
    private Long conversationId;
    private Long senderId;
    private Long receiverId;
    private Integer messageType;
    private String content;
    private String fileUrl;
    private Long replyTo;
    private Byte isRead;
    private Date createdAt;
    private String replyContent;
    private Integer replyMessageType;
}

