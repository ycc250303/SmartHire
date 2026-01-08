package com.SmartHire.recruitmentService.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 求职者对 Offer 的响应DTO
 */
@Data
@Schema(description = "求职者对 Offer 的响应DTO")
public class OfferResponseDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "消息ID")
    @NotNull(message = "消息ID不能为空")
    private Long messageId;

    @Schema(description = "响应状态：1-接受Offer 2-拒绝Offer")
    @NotNull(message = "状态不能为空")
    private Integer response; // 1: Accept, 2: Reject
}
