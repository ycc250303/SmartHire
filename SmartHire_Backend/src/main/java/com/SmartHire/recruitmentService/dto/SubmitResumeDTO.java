package com.SmartHire.recruitmentService.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * 投递简历请求DTO
 *
 * @author SmartHire Team
 */
@Data
public class SubmitResumeDTO {

    /**
     * 职位ID
     */
    @NotNull(message = "职位ID不能为空")
    @Positive(message = "职位ID必须大于0")
    private Long jobId;

    /**
     * 简历ID
     */
    @NotNull(message = "简历ID不能为空")
    @Positive(message = "简历ID必须大于0")
    private Long resumeId;
}
