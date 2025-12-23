package com.SmartHire.messageService.mapper;

import com.SmartHire.messageService.model.Conversation;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 一对一会话 Mapper 接口
 *
 * @author SmartHire Team
 * @since 2025-11-30
 */
@Mapper
public interface ConversationMapper extends BaseMapper<Conversation> {

    /**
     * 获取对方用户名
     */
    String getOtherUserName(
            @Param("conversationId") Long conversationId, @Param("currentUserId") Long currentUserId);

    /**
     * 获取对方用户头像
     */
    String getOtherUserAvatar(
            @Param("conversationId") Long conversationId, @Param("currentUserId") Long currentUserId);

    /**
     * 获取对方公司ID
     */
    Long getOtherCompanyId(
            @Param("conversationId") Long conversationId, @Param("currentUserId") Long currentUserId);

    /**
     * 获取对方公司名称
     */
    String getOtherCompanyName(
            @Param("conversationId") Long conversationId, @Param("currentUserId") Long currentUserId);

    /**
     * 获取对方公司Logo
     */
    String getOtherCompanyLogo(
            @Param("conversationId") Long conversationId, @Param("currentUserId") Long currentUserId);

    /**
     * 获取投递记录ID
     */
    Long getApplicationIdByConversationId(
            @Param("conversationId") Long conversationId);
}
