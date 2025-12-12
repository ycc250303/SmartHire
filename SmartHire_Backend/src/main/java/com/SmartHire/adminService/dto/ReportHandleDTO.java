package com.SmartHire.adminService.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

/**
 * 举报处理DTO
 */
@Data
public class ReportHandleDTO {

    /**
     * 处理结果：0-无需处理, 1-警告, 2-封禁, 3-下线
     */
    @NotNull(message = "处理结果不能为空")
    @Min(value = 0, message = "处理结果值无效")
    @Max(value = 3, message = "处理结果值无效")
    private Integer handleResult;

    /**
     * 处理原因
     */
    @NotBlank(message = "处理原因不能为空")
    private String handleReason;

    /**
     * 是否通知被举报方
     */
    private Boolean notifyTarget = false;

    /**
     * 通知内容
     */
    private String notificationContent;
}