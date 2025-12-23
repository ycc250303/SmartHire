package com.SmartHire.messageService.controller;

import com.SmartHire.common.auth.UserContext;
import com.SmartHire.common.entity.Result;
import com.SmartHire.messageService.dto.ConversationDTO;
import com.SmartHire.messageService.dto.MessageDTO;
import com.SmartHire.messageService.dto.SendMessageDTO;
import com.SmartHire.messageService.service.ChatMessageService;
import com.SmartHire.messageService.service.ConversationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import java.util.List;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 沟通消息服务统一控制器
 *
 * @author SmartHire Team
 * @since 2025-11-30
 */
@Slf4j
@RestController
@RequestMapping("/message")
public class MessageController {
  @Autowired
  private ChatMessageService chatMessageService;

  @Autowired
  private ConversationService conversationService;

  @Autowired
  private UserContext userContext;

  /** 获取会话列表 */
  @GetMapping("/get-conversations")
  public Result<List<ConversationDTO>> getConversations() {
    Long userId = userContext.getCurrentUserId();
    List<ConversationDTO> conversations = conversationService.getConversationList(userId);
    return Result.success("获取会话列表成功", conversations);
  }

  /** 发送文本消息 */
  @PostMapping(value = "/send-text", consumes = "application/json")
  public Result<MessageDTO> sendMessageText(@Valid @RequestBody SendMessageDTO dto) {
    Long userId = userContext.getCurrentUserId();
    MessageDTO messageDTO = chatMessageService.sendMessage(userId, dto);
    return Result.success("消息发送成功", messageDTO);
  }

  /** 发送媒体消息（图片、文件、语音、视频） */
  @PostMapping(value = "/send-media", consumes = "multipart/form-data")
  public Result<MessageDTO> sendMessageMedia(
      @RequestParam("dto") String dtoJson,
      @RequestPart(value = "file", required = false) MultipartFile file) {

    // 解析JSON
    SendMessageDTO dto;
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      dto = objectMapper.readValue(dtoJson, SendMessageDTO.class);
    } catch (JsonProcessingException e) {
      log.error("JSON解析失败: {}", e.getMessage());
      return Result.error(1, "参数格式错误");
    } catch (Exception e) {
      log.error("解析消息参数失败: {}", e.getMessage());
      return Result.error(1, "参数格式错误");
    }

    // 设置文件
    dto.setFile(file);

    Long userId = userContext.getCurrentUserId();
    MessageDTO messageDTO = chatMessageService.sendMessage(userId, dto);
    return Result.success("消息发送成功", messageDTO);
  }

  /** 获取聊天记录 */
  @GetMapping("/get-chat-history")
  public Result<List<MessageDTO>> getChatHistory(
      @RequestParam @NotNull @Min(value = 1, message = "会话ID不能小于1") Long conversationId,
      @RequestParam(defaultValue = "1") Integer page,
      @RequestParam(defaultValue = "20") Integer size) {
    Long userId = userContext.getCurrentUserId();
    List<MessageDTO> messages = chatMessageService.getChatHistory(conversationId, userId, page, size);
    return Result.success("获取聊天记录成功", messages);
  }

  /** 标记消息为已读 */
  @PatchMapping("/read")
  public Result<?> markAsRead(@RequestParam Long conversationId) {
    Long userId = userContext.getCurrentUserId();
    chatMessageService.markAsRead(conversationId, userId);
    return Result.success("标记消息为已读成功");
  }

  /** 获取未读消息总数 */
  @GetMapping("/get-unread-count")
  public Result<Integer> getUnreadCount() {
    Long userId = userContext.getCurrentUserId();
    Integer count = chatMessageService.getUnreadCount(userId);
    return Result.success("获取未读消息总数成功", count);
  }

  /** 置顶/取消置顶会话 */
  @PatchMapping("/pin-conversation/{id}")
  public Result<?> pinConversation(@PathVariable Long id, @RequestParam Boolean pinned) {
    Long userId = userContext.getCurrentUserId();
    conversationService.pinConversation(id, userId, pinned);
    return Result.success(pinned ? "置顶会话成功" : "取消置顶会话成功");
  }

  /** 删除会话 */
  @DeleteMapping("/delete-conversation/{id}")
  public Result<?> deleteConversation(@PathVariable Long id) {
    Long userId = userContext.getCurrentUserId();
    conversationService.deleteConversation(id, userId);
    return Result.success("删除会话成功");
  }
}
