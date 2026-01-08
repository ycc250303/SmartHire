package com.SmartHire.common.dto.messageDto;

import lombok.Data;
import java.io.Serializable;

/**
 * 模块间通用的发送消息请求对象
 */
@Data
public class SendMessageCommonDTO implements Serializable {
    /** 接收者用户ID */
    private Long receiverId;

    /** 消息类型：1-文本 2-图片 3-文件 4-语音 5-视频 6-系统通知 7-卡片消息 8-面试邀请 9-Offer 10-拒绝通知 */
    private Integer messageType;

    /** 消息内容 */
    private String content;

    /** 文件URL（图片、文件、语音、视频时使用） */
    private String fileUrl;

    /** 引用的消息ID（回复消息时使用） */
    private Long replyTo;
}

