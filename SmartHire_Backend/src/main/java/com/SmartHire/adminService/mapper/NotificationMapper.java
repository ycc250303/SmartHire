package com.SmartHire.adminService.mapper;

import com.SmartHire.adminService.model.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 通知Mapper接口
 * 功能：通知相关的数据库操作
 *
 * @author SmartHire Team
 * @since 2025-12-15
 */
@Mapper
public interface NotificationMapper {

    /**
     * 管理员发送单条通知给指定用户
     * 使用场景：举报处理通知、封禁通知、警告通知等
     *
     * @param notification 通知对象（包含接收用户ID、通知类型、内容等）
     * @return 影响行数
     */
    int insert(Notification notification);

    /**
     * 管理员批量发送通知给多个用户
     * 使用场景：系统公告、批量通知等
     *
     * @param notifications 通知列表
     * @return 影响行数
     */
    int insertBatch(@Param("notifications") List<Notification> notifications);

    /**
     * 获取用户的系统通知列表
     *
     * @param userId 用户ID
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 通知列表
     */
    List<Notification> selectByUserId(@Param("userId") Long userId,
                                     @Param("offset") Integer offset,
                                     @Param("limit") Integer limit);

    /**
     * 获取用户未读通知数量
     *
     * @param userId 用户ID
     * @return 未读通知数量
     */
    Integer countUnreadByUserId(@Param("userId") Long userId);

    /**
     * 标记通知为已读
     *
     * @param notificationId 通知ID
     * @param userId 用户ID
     * @return 影响行数
     */
    int markAsRead(@Param("notificationId") Long notificationId, @Param("userId") Long userId);

    /**
     * 标记用户所有通知为已读
     *
     * @param userId 用户ID
     * @return 影响行数
     */
    int markAllAsRead(@Param("userId") Long userId);

    /**
     * 删除通知
     *
     * @param notificationId 通知ID
     * @param userId 用户ID
     * @return 影响行数
     */
    int deleteById(@Param("notificationId") Long notificationId, @Param("userId") Long userId);
}