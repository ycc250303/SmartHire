package com.SmartHire.recruitmentService.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 投递简历响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "投递简历响应")
public class SubmitResumeResponseDTO implements Serializable {

    @Schema(description = "投递记录ID")
    private Long applicationId;

    @Schema(description = "关联的会话ID")
    private Long conversationId;
}

