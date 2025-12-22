package com.SmartHire.seekerService.controller;

import com.SmartHire.common.entity.Result;
import com.SmartHire.seekerService.dto.SeekerCardDTO;
import com.SmartHire.seekerService.service.OnlineResumeService;
import com.SmartHire.seekerService.service.SeekerCardService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.SmartHire.common.auth.RequireUserType;
import com.SmartHire.common.auth.UserType;

/**
 * 求职者公开服务控制器（对外） 供HR或其他系统使用，用于查看求职者的公开信息
 * 注意：此Controller的接口不需要认证，可以匿名访问
 *
 * @author SmartHire Team
 * @since 2025-11-19
 */
@Slf4j
@RestController
@RequestMapping("/public-seeker")
@RequireUserType({ UserType.SEEKER, UserType.HR, UserType.ADMIN })
public class SeekerPublicController {

  @Autowired
  private OnlineResumeService onlineResumeService;

  @Autowired
  private SeekerCardService seekerCardService;

  /**
   * 获取指定用户的在线简历
   *
   * @param userId 用户ID
   * @return 在线简历聚合数据
   */
  @GetMapping("/online-resume")
  @Operation(summary = "获取在线简历", description = "HR查看指定用户的在线简历（无需认证）")
  public Result<?> getOnlineResume(
      @RequestParam("userId") @NotNull(message = "用户ID不能为空") @Positive(message = "用户ID必须为正整数") Long userId) {
    return Result.success("获取在线简历成功", onlineResumeService.getOnlineResumeByUserId(userId));
  }

  /**
   * 获取所有求职者卡片信息（分页）
   *
   * @param pageNum  页码，默认为1
   * @param pageSize 每页大小，默认为20
   * @return 求职者卡片信息列表
   */
  @GetMapping("/cards")
  @Operation(summary = "获取所有求职者卡片", description = "HR查看所有求职者的基本信息卡片")
  public Result<List<SeekerCardDTO>> getAllSeekerCards(
      @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
      @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize) {
    log.info("getAllSeekerCards被调用，pageNum={}, pageSize={}", pageNum, pageSize);
    List<SeekerCardDTO> seekerCards = seekerCardService.getAllSeekerCards(pageNum, pageSize);
    return Result.success("获取所有求职者卡片成功", seekerCards);
  }

  /**
   * 综合筛选求职者卡片信息
   *
   * @param city          城市名称（可选）
   * @param education     学历级别（2-本科，3-硕士，4-博士）（可选）
   * @param salaryMin     最低期望薪资（可选）
   * @param salaryMax     最高期望薪资（可选）
   * @param isInternship  是否实习（0-全职，1-实习）（可选）
   * @param jobStatus     求职状态（0-离校-尽快到岗，1-在校-尽快到岗，2-在校-考虑机会，3-在校-暂不考虑）（可选）
   * @param hasInternship 是否有实习经历（0-无，1-有）（可选）
   * @return 求职者卡片信息列表
   */
  @GetMapping("/cards/filter")
  @Operation(summary = "综合筛选求职者卡片", description = "根据多个条件综合筛选求职者的基本信息卡片")
  public Result<List<SeekerCardDTO>> getSeekersByMultipleConditions(
      @RequestParam(value = "city", required = false) String city,
      @RequestParam(value = "education", required = false) Integer education,
      @RequestParam(value = "salaryMin", required = false) Double salaryMin,
      @RequestParam(value = "salaryMax", required = false) Double salaryMax,
      @RequestParam(value = "isInternship", required = false) Integer isInternship,
      @RequestParam(value = "jobStatus", required = false) Integer jobStatus,
      @RequestParam(value = "hasInternship", required = false) Integer hasInternship) {

    // 调用综合筛选方法
    List<SeekerCardDTO> seekerCards = seekerCardService.getSeekersByMultipleConditions(
        city, education, salaryMin, salaryMax, isInternship, jobStatus, hasInternship);

    return Result.success("综合筛选求职者卡片成功", seekerCards);
  }
}
