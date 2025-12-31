package com.SmartHire.common.api;

import com.SmartHire.common.dto.messageDto.SystemNotificationCommonDTO;

import java.util.List;

/**
 * 通知服务API接口
 * 用于模块间通信，避免直接访问数据库
 *
 * @author SmartHire Team
 * @since 2025-12-22
 */
public interface NotificationApi {

    /**
     * 获取用户的系统通知列表
     *
     * @param userId 用户ID
     * @param page   页码
     * @param size   每页大小
     * @return 系统通知列表
     */
    List<SystemNotificationCommonDTO> getUserNotifications(Long userId, Integer page, Integer size);

    /**
     * 获取用户未读通知数量
     *
     * @param userId 用户ID
     * @return 未读通知数量
     */
    Integer getUnreadNotificationCount(Long userId);

    /**
     * 标记通知为已读
     *
     * @param notificationId 通知ID
     * @param userId         用户ID
     * @return 是否成功
     */
    boolean markNotificationAsRead(Long notificationId, Long userId);

    /**
     * 标记所有通知为已读
     *
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean markAllNotificationsAsRead(Long userId);

    /**
     * 删除通知
     *
     * @param notificationId 通知ID
     * @param userId         用户ID
     * @return 是否成功
     */
    boolean deleteNotification(Long notificationId, Long userId);
}