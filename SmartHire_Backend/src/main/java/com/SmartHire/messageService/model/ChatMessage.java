package com.SmartHire.messageService.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

/**
 * 聊天消息表
 *
 * @author SmartHire Team
 * @since 2025-11-30
 */
@Getter
@Setter
@TableName("chat_message")
public class ChatMessage implements Serializable {

  private static final long serialVersionUID = 1L;

  /** 消息ID */
  @TableId(value = "id", type = IdType.AUTO)
  private Long id;

  /** 投递/推荐记录ID */
  private Long applicationId;

  /** 发送者用户ID */
  private Long senderId;

  /** 接收者用户ID */
  private Long receiverId;

  /** 消息类型：1-文本 2-图片 3-文件 4-语音 5-视频 6-系统通知 7-卡片消息 */
  private Byte messageType;

  /** 消息内容 */
  private String content;

  /** 文件/图片/语音/视频URL */
  private String fileUrl;

  /** 引用的消息ID */
  private Long replyTo;

  /** 是否已读 */
  private Byte isRead;

  /** 是否被标记为敏感 */
  private Byte isFlagged;

  /** 是否被逻辑删除/撤回 */
  private Byte isDeleted;

  /** 发送时间 */
  private Date createdAt;

  /** 会话ID */
  private Long conversationId;
}
