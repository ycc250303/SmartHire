package com.SmartHire.adminService.mapper;

import com.SmartHire.adminService.model.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 通知Mapper接口 - 管理员端专用
 * 功能：管理员发送各种通知给用户（举报处理、封禁、警告等）
 * 注意：不包含用户端的已读标记等功能
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
}