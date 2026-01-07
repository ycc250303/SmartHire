package com.SmartHire.recruitmentService.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 求职者对面试邀请的响应DTO
 */
@Data
@Schema(description = "求职者对面试邀请的响应DTO")
public class InterviewResponseDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "面试记录ID")
    @NotNull(message = "面试ID不能为空")
    private Long interviewId;

    @Schema(description = "响应状态：1-接受并确认面试 2-拒绝面试邀请")
    @NotNull(message = "状态不能为空")
    private Integer response; // 1: Accept, 2: Reject
}
