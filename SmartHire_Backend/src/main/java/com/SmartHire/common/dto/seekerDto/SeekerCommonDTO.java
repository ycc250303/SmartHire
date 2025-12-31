package com.SmartHire.common.dto.seekerDto;

import lombok.Data;
import java.io.Serializable;

/**
 * 模块间通用的求职者基础信息
 */
@Data
public class SeekerCommonDTO implements Serializable {
    private Long id;
    private Long userId;
    private String realName;
    private String currentCity;
    private Integer education;
    private Integer jobStatus;
    private String graduationYear;
    private Boolean internshipExperience;
    private Integer workExperienceYear;
}


