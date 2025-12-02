package com.SmartHire.hrService.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 岗位技能DTO
 */
@Data
public class JobSkillDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 技能名称
     */
    private String skillName;

    /**
     * 是否必需：0-加分项 1-必须
     */
    private Integer isRequired;
}
