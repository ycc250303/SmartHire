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
    /** 消息类型：1-文本 2-图片 3-文件 4-语音 5-视频 6-系统通知 7-卡片消息 8-面试邀请 9-Offer 10-拒绝通知 */
    private Integer messageType;
    private String content;
    private String fileUrl;
    private Long replyTo;
    private Byte isRead;
    private Date createdAt;
    private String replyContent;
    private Integer replyMessageType;
}
