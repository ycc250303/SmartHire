package com.SmartHire.adminService.service.impl;

import com.SmartHire.adminService.mapper.NotificationMapper;
import com.SmartHire.adminService.model.Notification;
import com.SmartHire.common.api.NotificationApi;
import com.SmartHire.messageService.dto.SystemNotificationDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 通知API实现类
 * 为其他模块提供通知相关的服务
 *
 * @author SmartHire Team
 * @since 2025-12-22
 */
@Slf4j
@Service
public class NotificationApiImpl implements NotificationApi {

    @Autowired
    private NotificationMapper notificationMapper;

    @Override
    public List<SystemNotificationDTO> getUserNotifications(Long userId, Integer page, Integer size) {
        log.info("获取用户 {} 的系统通知列表，页码: {}, 每页大小: {}", userId, page, size);

        // 计算偏移量
        int offset = (page - 1) * size;

        // 查询通知列表
        List<Notification> notifications = notificationMapper.selectByUserId(userId, offset, size);

        // 转换为DTO
        List<SystemNotificationDTO> result = new ArrayList<>();
        for (Notification notification : notifications) {
            SystemNotificationDTO dto = new SystemNotificationDTO();
            BeanUtils.copyProperties(notification, dto);
            result.add(dto);
        }

        log.info("查询到 {} 条系统通知", result.size());
        return result;
    }

    @Override
    public Integer getUnreadNotificationCount(Long userId) {
        log.info("获取用户 {} 的未读通知数量", userId);

        Integer count = notificationMapper.countUnreadByUserId(userId);

        log.info("用户 {} 有 {} 条未读通知", userId, count);
        return count;
    }

    @Override
    public boolean markNotificationAsRead(Long notificationId, Long userId) {
        log.info("标记通知 {} 为已读，用户: {}", notificationId, userId);

        int affectedRows = notificationMapper.markAsRead(notificationId, userId);
        boolean success = affectedRows > 0;

        if (success) {
            log.info("成功标记通知 {} 为已读", notificationId);
        } else {
            log.warn("标记通知 {} 为已读失败，可能通知不存在或不属于该用户", notificationId);
        }

        return success;
    }

    @Override
    public boolean markAllNotificationsAsRead(Long userId) {
        log.info("标记用户 {} 的所有通知为已读", userId);

        int affectedRows = notificationMapper.markAllAsRead(userId);
        boolean success = affectedRows >= 0; // 即使没有未读通知也算成功

        log.info("用户 {} 标记了 {} 条通知为已读", userId, affectedRows);
        return success;
    }

    @Override
    public boolean deleteNotification(Long notificationId, Long userId) {
        log.info("删除通知 {}，用户: {}", notificationId, userId);

        int affectedRows = notificationMapper.deleteById(notificationId, userId);
        boolean success = affectedRows > 0;

        if (success) {
            log.info("成功删除通知 {}", notificationId);
        } else {
            log.warn("删除通知 {} 失败，可能通知不存在或不属于该用户", notificationId);
        }

        return success;
    }
}