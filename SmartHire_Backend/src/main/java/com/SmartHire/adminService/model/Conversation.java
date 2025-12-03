package com.SmartHire.adminService.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 一对一会话
 * </p>
 *
 * @author SmartHire Team
 * @since 2025-12-02
 */
@Getter
@Setter
public class Conversation implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 会话ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户A
     */
    private Long user1Id;

    /**
     * 用户B
     */
    private Long user2Id;

    /**
     * 最近一条消息内容预览
     */
    private String lastMessage;

    /**
     * 最近消息时间
     */
    private Date lastMessageTime;

    private Integer unreadCountUser1;

    private Integer unreadCountUser2;

    private Byte pinnedByUser1;

    private Byte pinnedByUser2;

    /**
     * 用户1是否有未读通知
     */
    private Byte hasNotificationUser1;

    /**
     * 用户2是否有未读通知
     */
    private Byte hasNotificationUser2;

    private Date createdAt;

    private Byte deletedByUser1;

    private Byte deletedByUser2;
}
