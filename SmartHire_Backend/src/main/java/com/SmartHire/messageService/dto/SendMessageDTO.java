package com.SmartHire.messageService.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class SendMessageDTO {
  /** 接收者用户ID */
  @NotNull(message = "接收者ID不能为空")
  private Long receiverId;

  /** 消息类型：1-文本 2-图片 3-文件 4-语音 5-视频 */
  @NotNull(message = "消息类型不能为空")
  private Integer messageType;

  /** 消息内容 */
  @Size(max = 1000, message = "消息内容不能超过1000字符")
  private String content;

  /** 文件URL（图片、文件、语音、视频时使用） */
  private String fileUrl;

  /** 引用的消息ID（回复消息时使用） */
  private Long replyTo;

  /**
   * 消息附件文件（仅 multipart/form-data 提交时使用）
   *
   * <p>文本消息可忽略；图片/文件/语音/视频等媒体消息可通过该字段上传后由后端生成访问 URL。
   */
  private MultipartFile file;
}
