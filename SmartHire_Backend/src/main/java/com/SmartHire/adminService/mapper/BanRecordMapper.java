package com.SmartHire.adminService.mapper;

import com.SmartHire.adminService.model.BanRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 用户封禁记录表 Mapper 接口
 *
 * @author SmartHire Team
 * @since 2025-12-06
 */
@Mapper
public interface BanRecordMapper extends BaseMapper<BanRecord> {

    /**
     * 根据用户ID查询当前生效的封禁记录
     * @param userId 用户ID
     * @return 封禁记录
     */
    BanRecord findActiveBanByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID查询所有封禁记录
     * @param userId 用户ID
     * @return 封禁记录列表
     */
    List<BanRecord> findAllBansByUserId(@Param("userId") Long userId);

    /**
     * 查询已过期需要自动解除的封禁记录
     * @return 过期的封禁记录列表
     */
    List<BanRecord> findExpiredBans();

    /**
     * 解除用户的所有生效封禁
     * @param userId 用户ID
     * @param operatorId 操作管理员ID
     * @param operatorName 操作管理员姓名
     * @param liftReason 解除原因
     * @return 影响行数
     */
    int liftUserBans(@Param("userId") Long userId,
                     @Param("operatorId") Long operatorId,
                     @Param("operatorName") String operatorName,
                     @Param("liftReason") String liftReason);

    /**
     * 根据管理员ID查询封禁记录
     * @param operatorId 管理员ID
     * @return 封禁记录列表
     */
    List<BanRecord> findBansByOperatorId(@Param("operatorId") Long operatorId);

    /**
     * 统计指定时间范围内的封禁数量
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 封禁数量
     */
    int countBansByTimeRange(@Param("startTime") Date startTime,
                             @Param("endTime") Date endTime);
}