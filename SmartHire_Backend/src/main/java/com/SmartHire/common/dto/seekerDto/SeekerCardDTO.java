package com.SmartHire.common.dto.seekerDto;

import lombok.Data;
import java.io.Serializable;

/**
 * 模块间通用的求职者卡片信息
 */
@Data
public class SeekerCardDTO implements Serializable {
    private Integer userId;
    private String username;
    private String graduationYear;
    private Integer age;
    private String highestEducation;
    private String major;
    private Integer jobStatus;
    private Integer workExperienceYear;
    private Boolean internshipExperience;
    private String university;
    private String city;
    private String avatarUrl;
}
