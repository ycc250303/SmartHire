package com.SmartHire.messageService.mapper;

import com.SmartHire.messageService.model.ChatMessage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 聊天消息表 Mapper 接口
 *
 * @author SmartHire Team
 * @since 2025-11-30
 */
@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {}
