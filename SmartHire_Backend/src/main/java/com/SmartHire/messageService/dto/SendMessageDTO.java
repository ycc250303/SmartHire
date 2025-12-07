package com.SmartHire.messageService.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SendMessageDTO {
  /** 接收者用户ID */
  @NotNull(message = "接收者ID不能为空")
  private Long receiverId;

  /** 投递记录ID（可选，用于关联到具体的投递） */
  private Long applicationId;

  /** 消息类型：1-文本 2-图片 3-文件 4-语音 5-视频 */
  @NotNull(message = "消息类型不能为空")
  private Byte messageType;

  /** 消息内容 */
  @NotNull(message = "消息内容不能为空")
  @Size(max = 1000, message = "消息内容不能超过1000字符")
  private String content;

  /** 文件URL（图片、文件、语音、视频时使用） */
  private String fileUrl;

  /** 引用的消息ID（回复消息时使用） */
  private Long replyTo;
}
