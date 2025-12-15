package com.SmartHire.adminService.service;

import java.util.List;

/**
 * 通知服务接口
 *
 * @author SmartHire Team
 * @since 2025-12-15
 */
public interface NotificationService {

    /**
     * 发送系统通知给用户
     *
     * @param userId 接收通知的用户ID
     * @param type 通知类型：1-系统消息 2-举报处理 3-封禁通知 4-职位下线 5-警告通知
     * @param title 通知标题（可选）
     * @param content 通知内容
     */
    void sendNotification(Long userId, Integer type, String title, String content);

    /**
     * 发送带关联信息的通知
     *
     * @param userId 接收通知的用户ID
     * @param type 通知类型
     * @param title 通知标题
     * @param content 通知内容
     * @param relatedId 关联业务ID
     * @param relatedType 关联业务类型
     */
    void sendNotification(Long userId, Integer type, String title, String content,
                         Long relatedId, String relatedType);

    /**
     * 批量发送通知
     *
     * @param userIds 接收通知的用户ID列表
     * @param type 通知类型
     * @param title 通知标题
     * @param content 通知内容
     */
    void sendBatchNotification(List<Long> userIds, Integer type, String title, String content);

    /**
     * 发送举报处理通知给举报方
     *
     * @param reportId 举报ID
     * @param reporterId 举报人ID
     * @param targetTitle 被举报对象标题
     * @param result 处理结果
     * @param reason 处理原因
     */
    void sendReportResultToReporter(Long reportId, Long reporterId, String targetTitle,
                                   String result, String reason);

    /**
     * 发送举报处理通知给被举报方
     *
     * @param reportId 举报ID
     * @param targetUserId 被举报用户ID
     * @param targetType 被举报对象类型
     * @param result 处理结果
     * @param reason 处理原因
     */
    void sendReportResultToTarget(Long reportId, Long targetUserId, String targetType,
                                 String result, String reason);

    /**
     * 发送用户封禁通知
     *
     * @param userId 被封禁用户ID
     * @param banType 封禁类型
     * @param banDays 封禁天数
     * @param reason 封禁原因
     */
    void sendUserBanNotification(Long userId, String banType, Integer banDays, String reason);

    /**
     * 发送用户警告通知
     *
     * @param userId 被警告用户ID
     * @param contentType 内容类型
     * @param reason 警告原因
     */
    void sendUserWarningNotification(Long userId, String contentType, String reason);

    /**
     * 发送职位下线通知
     *
     * @param hrId HR用户ID
     * @param jobTitle 职位标题
     * @param reason 下线原因
     */
    void sendJobOfflineNotification(Long hrId, String jobTitle, String reason);
}