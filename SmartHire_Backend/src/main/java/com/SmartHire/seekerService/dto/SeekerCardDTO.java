package com.SmartHire.seekerService.dto;

import lombok.Data;

/** 求职卡片展示 DTO。 */
@Data
public class SeekerCardDTO {
    /** 用户ID */
    private Integer userId;

  /** 用户名 */
  private String username;

  /** 毕业年份描述，例如：2025年应届生 */
  private String graduationYear;

  /** 年龄 */
  private Integer age;

  /** 最高学历（本科/硕士/博士） */
  private String highestEducation;

  /** 专业 */
  private String major;

  /** 求职状态 */
  private Integer jobStatus;

  /** 工作经验（年） */
  private Integer workExperienceYear;

  /** 是否有实习经历 */
  private Boolean internshipExperience;

  /** 最高等级教育经历的大学 */
  private String university;

  /** 所在城市 */
  private String city;

  /** 用户头像url */
  private String avatarUrl;
}
