package com.SmartHire.common.dto.seekerDto;

import lombok.Data;
import java.io.Serializable;

/**
 * 模块间通用的简历信息
 */
@Data
public class ResumeCommonDTO implements Serializable {
    private Long id;
    private Long jobSeekerId;
    private String resumeName;
    private String fileUrl;
    private Byte privacyLevel;
    private Integer completeness;
}


