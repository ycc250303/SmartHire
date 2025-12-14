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

  @Select(
      "SELECT h.company_id "
          + "FROM hr_info h "
          + "JOIN conversation c ON (h.user_id = c.user1_id OR h.user_id = c.user2_id) "
          + "WHERE c.id = #{conversationId} "
          + "AND h.user_id <> #{currentUserId} "
          + "LIMIT 1")
  Long getOtherCompanyId(
      @Param("conversationId") Long conversationId, @Param("currentUserId") Long currentUserId);

  @Select(
      "SELECT co.company_name "
          + "FROM company co "
          + "JOIN hr_info h ON co.id = h.company_id "
          + "JOIN conversation c ON (h.user_id = c.user1_id OR h.user_id = c.user2_id) "
          + "WHERE c.id = #{conversationId} "
          + "AND h.user_id <> #{currentUserId} "
          + "LIMIT 1")
  String getOtherCompanyName(
      @Param("conversationId") Long conversationId, @Param("currentUserId") Long currentUserId);

  @Select(
      "SELECT co.logo_url "
          + "FROM company co "
          + "JOIN hr_info h ON co.id = h.company_id "
          + "JOIN conversation c ON (h.user_id = c.user1_id OR h.user_id = c.user2_id) "
          + "WHERE c.id = #{conversationId} "
          + "AND h.user_id <> #{currentUserId} "
          + "LIMIT 1")
  String getOtherCompanyLogo(
      @Param("conversationId") Long conversationId, @Param("currentUserId") Long currentUserId);
}
