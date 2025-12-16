package com.SmartHire.adminService.controller;

import com.SmartHire.adminService.dto.UserBanDTO;
import com.SmartHire.adminService.dto.UserManagementDTO;
import com.SmartHire.adminService.dto.UserStatusUpdateDTO;
import com.SmartHire.adminService.model.BanRecord;
import com.SmartHire.adminService.service.BanRecordService;
import com.SmartHire.adminService.service.NotificationService;
import com.SmartHire.adminService.service.UserService;
import com.SmartHire.common.auth.RequireUserType;
import com.SmartHire.common.entity.Result;
import com.SmartHire.common.enums.UserType;
import com.SmartHire.common.exception.enums.ErrorCode;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员服务统一控制器
 *
 * @author SmartHire Team
 * @since 2025-12-02
 */
@Slf4j
@RestController
@RequestMapping("/admin")
@RequireUserType(UserType.ADMIN) // 只有管理员可以访问所有admin接口
@Validated
public class AdminController {

  @Autowired private UserService userService;

  @Autowired private BanRecordService banRecordService;

  @Autowired private NotificationService notificationService;

  // ==================== 用户管理接口 ====================

  /** 分页查询用户列表 */
  @GetMapping("/users")
  public Result<Page<UserManagementDTO>> getUserList(
      @RequestParam(defaultValue = "1") @Min(1) Integer current,
      @RequestParam(defaultValue = "20") @Min(1) Integer size,
      @RequestParam(required = false) String userType,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String keyword) {

    Page<UserManagementDTO> page = new Page<>(current, size);
    Page<UserManagementDTO> result =
        userService.getUserManagementList(page, userType, status, keyword);

    return Result.success(result);
  }

  /** 根据用户ID获取用户详情 */
  @GetMapping("/users/{userId}")
  public Result<UserManagementDTO> getUserDetail(@PathVariable @NotNull Long userId) {
    UserManagementDTO userInfo = userService.getUserManagementInfo(userId);
    return Result.success(userInfo);
  }

  /** 更新用户状态 */
  @PutMapping("/users/{userId}/status")
  public Result<Boolean> updateUserStatus(
      @PathVariable @NotNull Long userId,
      @Valid @RequestBody UserStatusUpdateDTO userStatusUpdateDTO) {

    // 确保用户ID匹配
    userStatusUpdateDTO.setUserId(userId);
    boolean result = userService.updateUserStatus(userStatusUpdateDTO);

    if (result) {
      return Result.success("用户状态更新成功", true);
    } else {
      return Result.error(ErrorCode.ADMIN_OPERATION_FAILED);
    }
  }

  /** 批量更新用户状态 */
  @PutMapping("/users/batch-status")
  public Result<Integer> batchUpdateUserStatus(
      @RequestParam @NotNull List<Long> userIds,
      @RequestParam @NotNull Byte status,
      @RequestParam @NotNull Long operatorId,
      @RequestParam String operatorName,
      @RequestParam String reason) {

    int affectedRows =
        userService.batchUpdateUserStatus(userIds, status, operatorId, operatorName, reason);
    return Result.success("批量更新用户状态成功", affectedRows);
  }

  /** 获取用户统计信息 */
  @GetMapping("/users/statistics")
  public Result<Map<String, Object>> getUserStatistics() {
    Map<String, Object> statistics = userService.getUserStatistics();
    return Result.success(statistics);
  }

  // ==================== 用户封禁接口 ====================

  /** 封禁用户 */
  @PostMapping("/users/{userId}/ban")
  public Result<BanRecord> banUser(
      @PathVariable @NotNull Long userId, @Valid @RequestBody UserBanDTO userBanDTO) {

        BanRecord banRecord = banRecordService.banUser(userId, userBanDTO);

    return Result.success("用户封禁成功", banRecord);
  }

  /** 解除用户封禁 */
  @PostMapping("/users/{userId}/unban")
  public Result<Boolean> liftUserBan(
      @PathVariable @NotNull Long userId,
      @RequestParam @NotNull Long operatorId,
      @RequestParam String operatorName,
      @RequestParam String liftReason) {

    log.info(
        "解封请求参数: userId={}, operatorId={}, operatorName={}, liftReason={}",
        userId,
        operatorId,
        operatorName,
        liftReason);

    boolean result = banRecordService.liftUserBan(userId, operatorId, operatorName, liftReason);

    if (result) {
      return Result.success("解除封禁成功", true);
    } else {
      return Result.error(ErrorCode.ADMIN_UNBAN_FAILED);
    }
  }

  /** 检查用户是否被封禁 */
  @GetMapping("/users/{userId}/ban-status")
  public Result<Boolean> checkUserBanStatus(@PathVariable @NotNull Long userId) {
    boolean isBanned = banRecordService.isUserBanned(userId);
    return Result.success(isBanned);
  }

  /** 获取用户封禁记录 */
  @GetMapping("/users/{userId}/ban-history")
  public Result<List<BanRecord>> getUserBanHistory(@PathVariable @NotNull Long userId) {
    List<BanRecord> banHistory = banRecordService.findBanHistoryByUserId(userId);
    return Result.success(banHistory);
  }

  /** 处理过期封禁记录 */
  @PostMapping("/bans/process-expired")
  public Result<Integer> processExpiredBans() {
    int processedCount = banRecordService.processExpiredBans();
    return Result.success("处理过期封禁记录完成", processedCount);
  }

  /** 获取封禁统计信息 */
  @GetMapping("/bans/statistics")
  public Result<Map<String, Object>> getBanStatistics() {
    Map<String, Object> statistics = banRecordService.getBanStatistics();
    return Result.success("获取封禁统计信息成功", statistics);
  }

  /** 获取管理员的封禁记录 */
  @GetMapping("/bans/operator/{operatorId}")
  public Result<List<BanRecord>> getBansByOperatorId(@PathVariable @NotNull Long operatorId) {
    List<BanRecord> banRecords = banRecordService.findBansByOperatorId(operatorId);
    return Result.success("获取管理员封禁记录成功", banRecords);
  }

  // ==================== 系统管理接口 ====================

  /** 获取系统概览信息 */
  @GetMapping("/overview")
  public Result<Map<String, Object>> getSystemOverview() {
    Map<String, Object> overview = new java.util.HashMap<>();

    // 用户统计
    Map<String, Object> userStats = userService.getUserStatistics();
    overview.put("users", userStats);

    // 封禁统计
    Map<String, Object> banStats = banRecordService.getBanStatistics();
    overview.put("bans", banStats);

    return Result.success(overview);
  }

  // ==================== 通知管理接口 ====================

  /** 发送通知给指定用户 */
  @PostMapping("/notifications/send")
  public Result<Boolean> sendNotification(
      @RequestParam @NotNull Long userId,
      @RequestParam @NotNull Integer type,
      @RequestParam String title,
      @RequestParam @NotNull String content) {

    try {
      notificationService.sendNotification(userId, type, title, content);
      return Result.success("通知发送成功", true);
    } catch (Exception e) {
      log.error("发送通知失败: {}", e.getMessage(), e);
      return Result.error(ErrorCode.ADMIN_NOTIFICATION_SEND_FAILED.getCode(),
                        "通知发送失败: " + e.getMessage());
    }
  }

  /** 发送带关联信息的通知 */
  @PostMapping("/notifications/send-with-related")
  public Result<Boolean> sendNotificationWithRelated(
      @RequestParam @NotNull Long userId,
      @RequestParam @NotNull Integer type,
      @RequestParam String title,
      @RequestParam @NotNull String content,
      @RequestParam(required = false) Long relatedId,
      @RequestParam(required = false) String relatedType) {

    try {
      if (relatedId != null && relatedType != null) {
        notificationService.sendNotification(userId, type, title, content, relatedId, relatedType);
      } else {
        notificationService.sendNotification(userId, type, title, content);
      }
      return Result.success("通知发送成功", true);
    } catch (Exception e) {
      log.error("发送通知失败: {}", e.getMessage(), e);
      return Result.error(ErrorCode.ADMIN_NOTIFICATION_SEND_FAILED.getCode(),
                        "通知发送失败: " + e.getMessage());
    }
  }
}
