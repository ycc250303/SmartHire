package com.SmartHire.adminService.service.impl;

import com.SmartHire.adminService.dto.UserStatusUpdateDTO;
import com.SmartHire.adminService.dto.UserManagementDTO;
import com.SmartHire.adminService.exception.AdminServiceException;
import com.SmartHire.userAuthService.model.User;
import com.SmartHire.adminService.mapper.UserMapper;
import com.SmartHire.adminService.service.BanRecordService;
import com.SmartHire.adminService.service.UserService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 用户基础表 服务实现类
 * </p>
 *
 * @author SmartHire Team
 * @since 2025-12-02
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private BanRecordService banRecordService;

    @Override
    public Page<UserManagementDTO> getUserManagementList(Page<UserManagementDTO> page, String userType, String status, String keyword) {
        return userMapper.selectUserManagementList(page, userType, status, keyword);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUserStatus(UserStatusUpdateDTO userStatusUpdateDTO) {
        log.info("开始更新用户状态，用户ID: {}, 目标状态: {}, 操作管理员: {}",
                userStatusUpdateDTO.getUserId(), userStatusUpdateDTO.getTargetStatus(), userStatusUpdateDTO.getAdminUsername());

        // 1. 检查用户是否存在
        User user = userMapper.selectById(userStatusUpdateDTO.getUserId());
        if (user == null) {
            throw AdminServiceException.userNotFound(userStatusUpdateDTO.getUserId());
        }

        // 2. 验证用户类型（不能修改管理员状态）
        if (user.getUserType() == 3) {
            throw AdminServiceException.cannotModifyAdminStatus();
        }

        // 3. 验证状态转换
        Integer currentStatus = user.getStatus();
        Integer targetStatus = userStatusUpdateDTO.getTargetStatus();

        // 如果是从正常状态改为禁用状态，需要检查是否已有封禁记录
        if (currentStatus == 1 && userStatusUpdateDTO.getTargetStatus() == 0) {
            if (!banRecordService.isUserBanned(userStatusUpdateDTO.getUserId())) {
                throw AdminServiceException.operationFailed("封禁用户请使用专门的封禁接口");
            }
        }

        // 4. 更新用户状态
        user.setStatus(targetStatus);
        boolean updated = userMapper.updateById(user) > 0;

        if (updated) {
            log.info("用户状态更新成功，用户ID: {}, 原状态: {}, 新状态: {}",
                    userStatusUpdateDTO.getUserId(), currentStatus, targetStatus);
        } else {
            log.error("用户状态更新失败，用户ID: {}", userStatusUpdateDTO.getUserId());
        }

        return updated;
    }

    @Override
    public UserManagementDTO getUserManagementInfo(Long userId) {
        UserManagementDTO userInfo = userMapper.selectUserManagementInfo(userId);
        if (userInfo == null) {
            throw AdminServiceException.userNotFound(userId);
        }

        // 检查是否被封禁
        boolean isBanned = banRecordService.isUserBanned(userId);
        if (isBanned && userInfo.getStatus() == 1) {
            // 如果用户状态是正常但实际被封禁，更新状态
            userInfo.setStatus(0);
            User user = new User();
            user.setId(userId);
            user.setStatus(0);
            userMapper.updateById(user);
        }

        return userInfo;
    }

    @Override
    public boolean userExists(Long userId) {
        if (userId == null) {
            return false;
        }
        return userMapper.selectById(userId) != null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchUpdateUserStatus(List<Long> userIds, Byte status, Long operatorId, String operatorName, String reason) {
        log.info("开始批量更新用户状态，用户数量: {}, 目标状态: {}, 操作管理员: {}",
                userIds.size(), status, operatorName);

        // 验证所有用户是否存在且非管理员
        for (Long userId : userIds) {
            User user = userMapper.selectById(userId);
            if (user == null) {
                throw AdminServiceException.userNotFound(userId);
            }
            if (user.getUserType() == 3) {
                throw AdminServiceException.cannotModifyAdminStatus();
            }
        }

        // 批量更新状态
        int affectedRows = userMapper.batchUpdateStatus(userIds, status);

        log.info("批量更新用户状态完成，影响行数: {}", affectedRows);
        return affectedRows;
    }

    @Override
    public int countUsersByTypeAndStatus(String userType, String status) {
        return userMapper.countByTypeAndStatus(userType, status);
    }

    @Override
    public Map<String, Object> getUserStatistics() {
        Map<String, Object> statistics = new HashMap<>();

        // 总用户数
        int totalCount = Math.toIntExact(this.count());
        statistics.put("total", totalCount);

        // 按类型统计
        int jobSeekerCount = countUsersByTypeAndStatus("jobseeker", "");
        int hrCount = countUsersByTypeAndStatus("hr", "");
        int adminCount = countUsersByTypeAndStatus("admin", "");

        Map<String, Object> typeStats = new HashMap<>();
        typeStats.put("jobseeker", jobSeekerCount);
        typeStats.put("hr", hrCount);
        typeStats.put("admin", adminCount);
        statistics.put("byType", typeStats);

        // 按状态统计
        int activeCount = countUsersByTypeAndStatus("", "active");
        int bannedCount = countUsersByTypeAndStatus("", "banned");

        Map<String, Object> statusStats = new HashMap<>();
        statusStats.put("active", activeCount);
        statusStats.put("banned", bannedCount);
        statistics.put("byStatus", statusStats);

        // 封禁统计
        Map<String, Object> banStats = banRecordService.getBanStatistics();
        statistics.put("banStatistics", banStats);

        return statistics;
    }
}
