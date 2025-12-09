package com.SmartHire.adminService.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * 职位审核操作DTO
 *
 * @author SmartHire Team
 * @since 2025-12-09
 */
@Data
public class JobAuditDTO {

    /**
     * 审核原因（拒绝原因或修改意见）
     */
    @NotBlank(message = "审核原因不能为空")
    private String reason;
}