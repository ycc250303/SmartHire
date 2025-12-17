package com.SmartHire.seekerService.dto;

import java.util.Date;
import lombok.Data;

/**
 * 求职者信息响应DTO
 *
 * @author SmartHire Team
 */
@Data
public class SeekerInfoDTO {

  /** 真实姓名 */
  private String realName;

  /** 出生日期 */
  private Date birthDate;

  /** 当前城市 */
  private String currentCity;

  /** 最高学历 */
  private Integer education;

  /** 求职状态 */
  private Integer jobStatus;

  /** 毕业年份 */
  private String graduationYear;

  /** 工作经验（年） */
  private Integer workExperienceYear;

  /** 是否有实习经验 */
  private Boolean internshipExperience;

  /** 获取出生日期（返回防御性拷贝） */
  public Date getBirthDate() {
    return birthDate == null ? null : new Date(birthDate.getTime());
  }

  /** 设置出生日期（存储防御性拷贝） */
  public void setBirthDate(Date birthDate) {
    this.birthDate = birthDate == null ? null : new Date(birthDate.getTime());
  }
}
