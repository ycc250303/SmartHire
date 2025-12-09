package com.SmartHire.seekerService.controller;

import com.SmartHire.common.auth.RequireUserType;
import com.SmartHire.common.entity.Result;
import com.SmartHire.common.enums.UserType;
import com.SmartHire.seekerService.service.OnlineResumeService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 求职者公开服务控制器（对外） 供HR或其他系统使用，用于查看求职者的公开信息
 *
 * @author SmartHire Team
 * @since 2025-11-19
 */
@Slf4j
@RestController
@RequestMapping("/seeker")
@RequireUserType(UserType.HR)
public class SeekerPublicController {

  @Autowired private OnlineResumeService onlineResumeService;

  /**
   * 获取指定用户的在线简历
   *
   * @param userId 用户ID
   * @return 在线简历聚合数据
   */
  @GetMapping("/online-resume")
  @Operation(summary = "获取在线简历", description = "HR查看指定用户的在线简历")
  public Result<?> getOnlineResume(
      @RequestParam("userId") @NotNull(message = "用户ID不能为空") @Positive(message = "用户ID必须为正整数")
          Long userId) {
    return Result.success("获取在线简历成功", onlineResumeService.getOnlineResumeByUserId(userId));
  }
}
