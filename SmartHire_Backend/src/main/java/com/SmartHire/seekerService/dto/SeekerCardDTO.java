package com.SmartHire.seekerService.dto;

import lombok.Data;

/** 求职卡片展示 DTO。 */
@Data
public class SeekerCardDTO {
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
}
