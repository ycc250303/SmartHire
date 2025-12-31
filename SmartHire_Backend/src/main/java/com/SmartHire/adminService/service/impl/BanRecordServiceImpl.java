package com.SmartHire.adminService.service.impl;

import com.SmartHire.adminService.dto.UserBanDTO;
import com.SmartHire.common.exception.exception.AdminServiceException;
import com.SmartHire.adminService.mapper.BanRecordMapper;
import com.SmartHire.adminService.model.BanRecord;
import com.SmartHire.adminService.service.BanRecordService;
import com.SmartHire.common.api.UserAuthApi;
import com.SmartHire.common.dto.userDto.UserCommonDTO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户封禁记录表 服务实现类
 *
 * @author SmartHire Team
 * @since 2025-12-06
 */
@Slf4j
@Service
public class BanRecordServiceImpl extends ServiceImpl<BanRecordMapper, BanRecord>
    implements BanRecordService {

  @Autowired private BanRecordMapper banRecordMapper;

  @Autowired private UserAuthApi userAuthApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BanRecord banUser(Long userId, UserBanDTO userBanDTO) {
        log.info("开始封禁用户，用户ID: {}, 操作管理员: {}", userId, userBanDTO.getAdminUsername());

        // 1. 验证用户是否存在
        UserCommonDTO user = userAuthApi.getUserById(userId);
        if (user == null) {
            throw AdminServiceException.userNotFound(userId);
        }

    // 2. 验证用户类型（不能封禁管理员）
    if (user.getUserType() == 3) {
      throw AdminServiceException.cannotBanAdmin();
    }

        // 3. 检查用户是否已经被封禁
        BanRecord existingBan = banRecordMapper.findActiveBanByUserId(userId);
        if (existingBan != null) {
            throw AdminServiceException.userAlreadyBanned(userId);
        }

        // 4. 创建封禁记录
        BanRecord banRecord = new BanRecord();
        banRecord.setUserId(userId);
        banRecord.setUsername(user.getUsername());
        banRecord.setEmail(user.getEmail());
        banRecord.setUserType(user.getUserType().byteValue());
        banRecord.setBanReason(userBanDTO.getBanReason());
        banRecord.setBanType(userBanDTO.getBanDurationType());
        banRecord.setBanStartTime(new Date());
        banRecord.setBanStatus("active");
        banRecord.setOperatorId(userBanDTO.getAdminId());
        banRecord.setOperatorName(userBanDTO.getAdminUsername());

    // 计算封禁结束时间
    if ("temporary".equals(userBanDTO.getBanDurationType()) && userBanDTO.getBanDays() != null) {
      Date endTime =
          new Date(
              System.currentTimeMillis()
                  + (long) userBanDTO.getBanDays() * 24L * 60L * 60L * 1000L);
      banRecord.setBanEndTime(endTime);
      banRecord.setBanDays(userBanDTO.getBanDays());
    } else if ("permanent".equals(userBanDTO.getBanDurationType())) {
      banRecord.setBanEndTime(null); // 永久封禁
      banRecord.setBanDays(null);
    }

    // 5. 保存封禁记录
    banRecordMapper.insert(banRecord);

    // 6. 更新用户状态为禁用
    user.setStatus(0); // 0-禁用
    userAuthApi.updateUser(user);

        log.info("用户封禁成功，用户ID: {}, 封禁类型: {}", userId, userBanDTO.getBanDurationType());
        return banRecord;
    }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public boolean liftUserBan(Long userId, Long operatorId, String operatorName, String liftReason) {
    log.info("开始解除用户封禁，用户ID: {}, 操作管理员: {}", userId, operatorName);

    try {
      // 1. 检查用户是否存在
      log.info("步骤1: 检查用户是否存在, userId={}", userId);
      UserCommonDTO user = userAuthApi.getUserById(userId);
      if (user == null) {
        log.error("用户不存在: userId={}", userId);
        throw AdminServiceException.userNotFound(userId);
      }
      log.info("用户存在: username={}", user.getUsername());

      // 2. 检查是否有生效的封禁记录
      log.info("步骤2: 检查是否有生效的封禁记录");
      BanRecord activeBan = banRecordMapper.findActiveBanByUserId(userId);
      if (activeBan == null) {
        log.error("没有找到生效的封禁记录: userId={}", userId);
        throw AdminServiceException.userNotBanned(userId);
      }
      log.info("找到生效封禁记录: banId={}, banStatus={}", activeBan.getId(), activeBan.getBanStatus());

      // 3. 解除封禁
      log.info("步骤3: 解除封禁");
      int affectedRows = banRecordMapper.liftUserBans(userId, operatorId, operatorName, liftReason);
      log.info("解除封禁影响行数: {}", affectedRows);
      if (affectedRows == 0) {
        log.error("解除封禁失败，影响行数为0");
        throw AdminServiceException.operationFailed("解除封禁");
      }

      // 4. 恢复用户状态
      log.info("步骤4: 恢复用户状态");
      user.setStatus(1); // 1-正常
      userAuthApi.updateUser(user);
      log.info("用户状态已更新为正常");

      log.info("用户封禁解除成功，用户ID: {}", userId);
      return true;
    } catch (Exception e) {
      log.error("解除用户封禁过程中发生异常: userId={}, error={}", userId, e.getMessage(), e);
      throw e;
    }
  }

  @Override
  public BanRecord findActiveBanByUserId(Long userId) {
    return banRecordMapper.findActiveBanByUserId(userId);
  }

  @Override
  public boolean isUserBanned(Long userId) {
    BanRecord banRecord = findActiveBanByUserId(userId);
    return banRecord != null;
  }

  @Override
  public List<BanRecord> findBanHistoryByUserId(Long userId) {
    return banRecordMapper.findAllBansByUserId(userId);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public int processExpiredBans() {
    log.info("开始处理过期的封禁记录");

    List<BanRecord> expiredBans = banRecordMapper.findExpiredBans();
    if (expiredBans.isEmpty()) {
      log.info("没有过期的封禁记录");
      return 0;
    }

    int processedCount = 0;
    for (BanRecord ban : expiredBans) {
      try {
        // 更新封禁记录状态
        ban.setBanStatus("expired");
        ban.setUpdatedAt(new Date());
        banRecordMapper.updateById(ban);

        // 恢复用户状态
        UserCommonDTO user = userAuthApi.getUserById(ban.getUserId());
        if (user != null) {
          user.setStatus(1); // 1-正常
          userAuthApi.updateUser(user);
        }

        processedCount++;
      } catch (Exception e) {
        log.error("处理过期封禁记录失败，记录ID: {}", ban.getId(), e);
      }
    }

    log.info("处理过期封禁记录完成，处理数量: {}", processedCount);
    return processedCount;
  }

  @Override
  public List<BanRecord> findBansByOperatorId(Long operatorId) {
    return banRecordMapper.findBansByOperatorId(operatorId);
  }

  @Override
  public int countBansByTimeRange(Date startTime, Date endTime) {
    return banRecordMapper.countBansByTimeRange(startTime, endTime);
  }

  @Override
  public Map<String, Object> getBanStatistics() {
    Map<String, Object> statistics = new HashMap<>();

    // 总数
    int totalCount = Math.toIntExact(this.count());
    statistics.put("total", totalCount);

    // 生效中
    int activeCount =
        Math.toIntExact(this.lambdaQuery().eq(BanRecord::getBanStatus, "active").count());
    statistics.put("active", activeCount);

    // 已过期
    int expiredCount =
        Math.toIntExact(this.lambdaQuery().eq(BanRecord::getBanStatus, "expired").count());
    statistics.put("expired", expiredCount);

    // 已解除
    int liftedCount =
        Math.toIntExact(this.lambdaQuery().eq(BanRecord::getBanStatus, "lifted").count());
    statistics.put("lifted", liftedCount);

    return statistics;
  }
}
