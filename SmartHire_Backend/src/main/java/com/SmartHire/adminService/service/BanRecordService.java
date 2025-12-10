package com.SmartHire.adminService.service;

import com.SmartHire.adminService.dto.UserBanDTO;
import com.SmartHire.adminService.model.BanRecord;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Date;
import java.util.List;

/**
 * 用户封禁记录表 服务类
 *
 * @author SmartHire Team
 * @since 2025-12-06
 */
public interface BanRecordService extends IService<BanRecord> {

    /**
     * 封禁用户
     * @param userBanDTO 封禁信息
     * @return 封禁记录
     */
    BanRecord banUser(UserBanDTO userBanDTO);

    /**
     * 解除用户封禁
     * @param userId 用户ID
     * @param operatorId 操作管理员ID
     * @param operatorName 操作管理员姓名
     * @param liftReason 解除原因
     * @return 是否成功
     */
    boolean liftUserBan(Long userId, Long operatorId, String operatorName, String liftReason);

    /**
     * 根据用户ID查询当前生效的封禁记录
     * @param userId 用户ID
     * @return 封禁记录，如果没有则返回null
     */
    BanRecord findActiveBanByUserId(Long userId);

    /**
     * 检查用户是否被封禁
     * @param userId 用户ID
     * @return true-被封禁, false-未封禁
     */
    boolean isUserBanned(Long userId);

    /**
     * 根据用户ID查询所有封禁记录
     * @param userId 用户ID
     * @return 封禁记录列表
     */
    List<BanRecord> findBanHistoryByUserId(Long userId);

    /**
     * 自动处理过期的封禁记录
     * @return 处理的记录数量
     */
    int processExpiredBans();

    /**
     * 根据管理员ID查询封禁记录
     * @param operatorId 管理员ID
     * @return 封禁记录列表
     */
    List<BanRecord> findBansByOperatorId(Long operatorId);

    /**
     * 统计指定时间范围内的封禁数量
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 封禁数量
     */
    int countBansByTimeRange(Date startTime, Date endTime);

    /**
     * 获取封禁统计信息
     * @return 统计信息Map：total总数, active生效中, expired已过期, lifted已解除
     */
    java.util.Map<String, Object> getBanStatistics();
}