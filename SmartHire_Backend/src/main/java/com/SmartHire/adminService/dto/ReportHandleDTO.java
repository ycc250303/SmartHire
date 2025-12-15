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
     * 处理原因（内部记录）
     */
    @NotBlank(message = "处理原因不能为空")
    private String handleReason;

    /**
     * 封禁信息DTO（当处理结果为封禁时必填）
     */
    private BanInfoDTO banInfo;

    /**
     * 举报方通知内容（必填）
     */
    @NotBlank(message = "举报方通知内容不能为空")
    private String reporterNotificationContent;

    /**
     * 被举报方通知内容（必填）
     */
    @NotBlank(message = "被举报方通知内容不能为空")
    private String targetNotificationContent;

    /**
     * 封禁信息内部类
     */
    @Data
    public static class BanInfoDTO {
        /**
         * 封禁类型：permanent-永久封禁, temporary-临时封禁
         */
        @NotBlank(message = "封禁类型不能为空")
        private String banType;

        /**
         * 封禁天数（临时封禁时必填，1-365天）
         */
        @Min(value = 1, message = "封禁天数最少1天")
        @Max(value = 365, message = "封禁天数最多365天")
        private Integer banDays;
    }
}