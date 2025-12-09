package com.SmartHire.messageService.controller;

import com.SmartHire.common.auth.UserContext;
import com.SmartHire.common.entity.Result;
import com.SmartHire.messageService.dto.ConversationDTO;
import com.SmartHire.messageService.dto.MessageDTO;
import com.SmartHire.messageService.dto.SendMessageDTO;
import com.SmartHire.messageService.service.ChatMessageService;
import com.SmartHire.messageService.service.ConversationService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.*;

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
  @Autowired private ChatMessageService chatMessageService;

  @Autowired private ConversationService conversationService;

  @Autowired private UserContext userContext;
  @Autowired private MessageSource messageSource;

  /** 获取会话列表 */
  @GetMapping("/get-conversations")
  public Result<List<ConversationDTO>> getConversations() {
    Long userId = userContext.getCurrentUserId();
    List<ConversationDTO> conversations = conversationService.getConversationList(userId);
    return Result.success("获取会话列表成功", conversations);
  }

  /** 发送消息 */
  @PostMapping("/send-message")
  public Result<MessageDTO> sendMessage(@Valid @RequestBody SendMessageDTO dto) {
    Long userId = userContext.getCurrentUserId();
    MessageDTO messageDTO = chatMessageService.sendMessage(userId, dto);
    return Result.success("消息发送成功", messageDTO);
  }

  /** 获取聊天记录 */
  @GetMapping("/get-chat-history")
  public Result<List<MessageDTO>> getChatHistory(
      @RequestParam Long conversationId,
      @RequestParam(defaultValue = "1") Integer page,
      @RequestParam(defaultValue = "20") Integer size) {
    Long userId = userContext.getCurrentUserId();
    List<MessageDTO> messages =
        chatMessageService.getChatHistory(conversationId, userId, page, size);
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
