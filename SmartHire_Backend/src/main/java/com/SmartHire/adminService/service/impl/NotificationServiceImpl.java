package com.SmartHire.adminService.service.impl;

import com.SmartHire.adminService.mapper.NotificationMapper;
import com.SmartHire.adminService.mapper.UserMapper;
import com.SmartHire.adminService.model.Notification;
import com.SmartHire.adminService.service.NotificationService;
import com.SmartHire.common.exception.exception.AdminServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 通知服务实现类 - 管理员端专用
 * 功能：管理员发送各种通知给用户
 *
 * @author SmartHire Team
 * @since 2025-12-15
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;
    private final UserMapper userMapper;

    @Override
    public void sendNotification(Long userId, Integer type, String title, String content) {
        log.info("管理员发送通知给用户{}, 类型: {}, 标题: {}", userId, type, title);

        // 验证用户是否存在
        validateUserExistence(userId);

        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setIsRead(Notification.ReadStatus.UNREAD);

        notificationMapper.insert(notification);
    }

    @Override
    public void sendNotification(Long userId, Integer type, String title, String content,
                                Long relatedId, String relatedType) {
        log.info("管理员发送关联通知给用户{}, 类型: {}, 关联: {}-{}",
                userId, type, relatedType, relatedId);

        // 验证用户是否存在
        validateUserExistence(userId);

        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setRelatedId(relatedId);
        notification.setRelatedType(relatedType);
        notification.setIsRead(Notification.ReadStatus.UNREAD);

        notificationMapper.insert(notification);
    }

    @Override
    public void sendBatchNotification(List<Long> userIds, Integer type, String title, String content) {
        log.info("管理员批量发送通知给{}个用户, 类型: {}", userIds.size(), type);

        // 验证所有用户是否存在
        validateUsersExistence(userIds);

        List<Notification> notifications = new ArrayList<>();
        for (Long userId : userIds) {
            Notification notification = new Notification();
            notification.setUserId(userId);
            notification.setType(type);
            notification.setTitle(title);
            notification.setContent(content);
            notification.setIsRead(Notification.ReadStatus.UNREAD);
            notifications.add(notification);
        }

        notificationMapper.insertBatch(notifications);
    }

    @Override
    public void sendReportResultToReporter(Long reportId, Long reporterId, String targetTitle,
                                          String result, String reason) {
        String title = "举报处理结果通知";
        String content = String.format(
            "您好，您举报的《%s》已处理完成。处理结果：%s。处理原因：%s。感谢您的监督。",
            targetTitle, result, reason
        );

        sendNotification(reporterId, Notification.Type.REPORT_RESULT, title, content,
                        reportId, Notification.RelatedType.REPORT);
    }

    @Override
    public void sendReportResultToTarget(Long reportId, Long targetUserId, String targetType,
                                        String result, String reason) {
        String title = "举报通知";
        String content;

        // 根据被举报对象类型生成不同的通知内容
        if ("职位".equals(targetType)) {
            content = String.format(
                "您好，您的职位被用户举报。经管理员审核，处理结果：%s。处理原因：%s。如有疑问请联系客服。",
                result, reason
            );
        } else {
            content = String.format(
                "您好，您被用户举报。经管理员审核，处理结果：%s。处理原因：%s。如有疑问请联系客服。",
                result, reason
            );
        }

        sendNotification(targetUserId, Notification.Type.REPORT_RESULT, title, content,
                        reportId, Notification.RelatedType.REPORT);
    }

    @Override
    public void sendUserBanNotification(Long userId, String banType, Integer banDays, String reason) {
        String title = "账户封禁通知";
        String duration = "永久封禁";
        if (banDays != null && banDays > 0) {
            duration = banDays + "天";
        }

        String content = String.format(
            "您的账户因违反社区规定已被封禁。封禁类型：%s。封禁原因：%s。封禁时间：%s。如有疑问请联系客服。",
            "temporary".equals(banType) ? "临时封禁" : "永久封禁", reason, duration
        );

        sendNotification(userId, Notification.Type.USER_BAN, title, content,
                        userId, Notification.RelatedType.USER);
    }

    @Override
    public void sendUserWarningNotification(Long userId, String contentType, String reason) {
        String title = "警告通知";
        String content = String.format(
            "您的%s存在违规行为，已被管理员警告。警告原因：%s。请遵守社区规范，如有疑问请联系客服。",
            contentType, reason
        );

        sendNotification(userId, Notification.Type.USER_WARNING, title, content,
                        userId, Notification.RelatedType.USER);
    }

    @Override
    public void sendJobOfflineNotification(Long hrId, String jobTitle, String reason) {
        String title = "职位下线通知";
        String content = String.format(
            "您发布的职位《%s》因违反规定已被下线。下线原因：%s。请修改后重新发布。如有疑问请联系客服。",
            jobTitle, reason
        );

        sendNotification(hrId, Notification.Type.JOB_OFFLINE, title, content,
                        null, Notification.RelatedType.JOB);
    }

    /**
     * 验证单个用户是否存在
     *
     * @param userId 用户ID
     */
    private void validateUserExistence(Long userId) {
        if (userMapper.selectById(userId) == null) {
            throw AdminServiceException.userNotFound(userId);
        }
    }

    /**
     * 验证多个用户是否都存在
     *
     * @param userIds 用户ID列表
     */
    private void validateUsersExistence(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            throw AdminServiceException.operationFailed("用户ID列表不能为空");
        }

        // 检查是否有重复的用户ID
        long uniqueCount = userIds.stream().distinct().count();
        if (uniqueCount != userIds.size()) {
            throw AdminServiceException.operationFailed("用户ID列表包含重复项");
        }

        // 验证每个用户是否存在
        for (Long userId : userIds) {
            if (userMapper.selectById(userId) == null) {
                throw AdminServiceException.userNotFound(userId);
            }
        }
    }
}