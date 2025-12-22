package com.SmartHire.messageService.controller;

import com.SmartHire.common.api.NotificationApi;
import com.SmartHire.common.auth.UserContext;
import com.SmartHire.common.entity.Result;
import com.SmartHire.messageService.dto.SystemNotificationDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统消息控制器
 * 提供给求职者和HR使用的系统消息展示接口
 *
 * @author SmartHire Team
 * @since 2025-12-22
 */
@Slf4j
@RestController
@RequestMapping("/system-notification")
public class SystemNotificationController {

    @Autowired
    private NotificationApi notificationApi;

    @Autowired
    private UserContext userContext;

    /**
     * 获取系统消息列表
     */
    @GetMapping("/list")
    public Result<List<SystemNotificationDTO>> getNotificationList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {

        Long userId = userContext.getCurrentUserId();
        log.info("用户 {} 获取系统消息列表，页码: {}, 每页大小: {}", userId, page, size);

        List<SystemNotificationDTO> notifications = notificationApi.getUserNotifications(userId, page, size);

        // 设置通知类型名称
        notifications.forEach(this::setTypeName);

        return Result.success("获取系统消息列表成功", notifications);
    }

    /**
     * 获取未读消息数量
     */
    @GetMapping("/unread-count")
    public Result<Integer> getUnreadCount() {
        Long userId = userContext.getCurrentUserId();
        log.info("用户 {} 获取未读系统消息数量", userId);

        Integer count = notificationApi.getUnreadNotificationCount(userId);
        return Result.success("获取未读消息数量成功", count);
    }

    /**
     * 标记消息为已读
     */
    @PatchMapping("/read/{notificationId}")
    public Result<?> markAsRead(@PathVariable Long notificationId) {
        Long userId = userContext.getCurrentUserId();
        log.info("用户 {} 标记系统消息 {} 为已读", userId, notificationId);

        boolean success = notificationApi.markNotificationAsRead(notificationId, userId);
        if (success) {
            return Result.success("标记消息已读成功");
        } else {
            return Result.error(1, "标记消息已读失败");
        }
    }

    /**
     * 标记所有消息为已读
     */
    @PatchMapping("/read-all")
    public Result<?> markAllAsRead() {
        Long userId = userContext.getCurrentUserId();
        log.info("用户 {} 标记所有系统消息为已读", userId);

        boolean success = notificationApi.markAllNotificationsAsRead(userId);
        if (success) {
            return Result.success("标记所有消息已读成功");
        } else {
            return Result.error(1, "标记所有消息已读失败");
        }
    }

    /**
     * 删除系统消息
     */
    @DeleteMapping("/{notificationId}")
    public Result<?> deleteNotification(@PathVariable Long notificationId) {
        Long userId = userContext.getCurrentUserId();
        log.info("用户 {} 删除系统消息 {}", userId, notificationId);

        boolean success = notificationApi.deleteNotification(notificationId, userId);
        if (success) {
            return Result.success("删除系统消息成功");
        } else {
            return Result.error(1, "删除系统消息失败");
        }
    }

    /**
     * 设置通知类型名称
     */
    private void setTypeName(SystemNotificationDTO notification) {
        switch (notification.getType()) {
            case SystemNotificationDTO.Type.SYSTEM_MESSAGE:
                notification.setTypeName("系统消息");
                break;
            case SystemNotificationDTO.Type.REPORT_RESULT:
                notification.setTypeName("举报处理");
                break;
            case SystemNotificationDTO.Type.USER_BAN:
                notification.setTypeName("封禁通知");
                break;
            case SystemNotificationDTO.Type.JOB_OFFLINE:
                notification.setTypeName("职位下线");
                break;
            case SystemNotificationDTO.Type.USER_WARNING:
                notification.setTypeName("警告通知");
                break;
            default:
                notification.setTypeName("未知类型");
                break;
        }
    }
}