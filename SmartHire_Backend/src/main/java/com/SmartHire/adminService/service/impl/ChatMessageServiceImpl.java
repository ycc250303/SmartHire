package com.SmartHire.adminService.service.impl;

import com.SmartHire.adminService.model.ChatMessage;
import com.SmartHire.adminService.mapper.ChatMessageMapper;
import com.SmartHire.adminService.service.ChatMessageService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 聊天消息表 服务实现类
 * </p>
 *
 * @author SmartHire Team
 * @since 2025-12-02
 */
@Service
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage> implements ChatMessageService {

}
