package com.SmartHire.messageService.mapper;

import com.SmartHire.messageService.model.Conversation;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 一对一会话 Mapper 接口
 *
 * @author SmartHire Team
 * @since 2025-11-30
 */
@Mapper
public interface ConversationMapper extends BaseMapper<Conversation> {
  @Select(
      "SELECT u.username "
          + "FROM user u "
          + "JOIN conversation c ON (u.id = c.user1_id OR u.id = c.user2_id) "
          + "WHERE c.id = #{conversationId} "
          + "AND u.id <> #{currentUserId} "
          + "LIMIT 1")
  String getOtherUserName(
      @Param("conversationId") Long conversationId, @Param("currentUserId") Long currentUserId);

  @Select(
      "SELECT u.avatar_url "
          + "FROM user u "
          + "JOIN conversation c ON (u.id = c.user1_id OR u.id = c.user2_id) "
          + "WHERE c.id = #{conversationId} "
          + "AND u.id <> #{currentUserId} "
          + "LIMIT 1")
  String getOtherUserAvatar(
      @Param("conversationId") Long conversationId, @Param("currentUserId") Long currentUserId);
}
