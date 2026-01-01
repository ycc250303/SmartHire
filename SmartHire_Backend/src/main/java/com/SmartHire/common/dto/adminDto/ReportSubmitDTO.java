package com.SmartHire.common.dto.adminDto;

import lombok.Data;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.Range;

@Data
public class ReportSubmitDTO {
    @NotNull(message = "举报对象不能为空")
    private Long targetId;

    @NotNull(message = "举报对象类型不能为空")
    @Range(min = 1, max = 2, message = "类型错误：1-用户, 2-职位")
    private Integer targetType;

    @NotNull(message = "举报类型不能为空")
    private Integer reportType;

    @NotBlank(message = "举报原因不能为空")
    private String reason;

    private String evidenceImage; // Base64
}