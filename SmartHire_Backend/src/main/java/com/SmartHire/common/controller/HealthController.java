package com.SmartHire.common.controller;

import com.SmartHire.common.entity.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 健康检查控制器
 *
 * <p>用于 CD 流程中的服务健康检查，无需认证即可访问
 *
 * @author SmartHire Team
 * @since 2025-12-03
 */
@Tag(name = "健康检查", description = "服务健康检查接口")
@RestController
@RequestMapping("/health")
public class HealthController {

  /**
   * 健康检查接口
   *
   * <p>返回服务状态信息，用于 CD 流程中的健康检查
   *
   * @return 健康状态信息
   */
  @GetMapping
  @Operation(summary = "健康检查", description = "检查服务是否正常运行，无需认证")
  public Result<Map<String, Object>> health() {
    Map<String, Object> healthInfo = new HashMap<>();
    healthInfo.put("status", "UP");
    healthInfo.put("timestamp", LocalDateTime.now());
    healthInfo.put("service", "SmartHire Backend");
    return Result.success("服务运行正常", healthInfo);
  }
}
