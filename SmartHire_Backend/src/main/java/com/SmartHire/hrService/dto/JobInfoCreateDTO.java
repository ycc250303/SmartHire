package com.SmartHire.hrService.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

/** 岗位创建DTO */
@Data
public class JobInfoCreateDTO implements Serializable {

  @Serial private static final long serialVersionUID = 1L;

  /** 公司ID */
  @NotNull(message = "公司ID不能为空")
  private Long companyId;

  /** 职位名称 */
  @NotBlank(message = "职位名称不能为空")
  private String jobTitle;

  /** 职位类别 */
  private String jobCategory;

  /** 部门 */
  private String department;

  /** 工作城市 */
  @NotBlank(message = "工作城市不能为空")
  private String city;

  /** 详细地址 */
  private String address;

  /** 薪资最低 */
  @Min(value = 0, message = "最低薪资不能小于0")
  private BigDecimal salaryMin;

  /** 薪资最高 */
  @Min(value = 0, message = "最高薪资不能小于0")
  private BigDecimal salaryMax;

  /** 薪资月数 */
  @Min(value = 1, message = "薪资月数不能小于1")
  @Max(value = 24, message = "薪资月数不能大于24")
  private Integer salaryMonths;

  /** 学历要求：0-不限 1-专科 2-本科 3-硕士 4-博士 */
  private Integer educationRequired;

  /** 工作类型：0-全职 1-实习 */
  @NotNull(message = "工作类型不能为空")
  private Integer jobType;

  /** 经验要求（仅全职类型需要）0-应届生 1-1年以内 2-1-3年 3-3-5年 4-5-10年 5-10年以上 */
  @Min(value = 0, message = "经验要求值必须在0-5之间")
  @Max(value = 5, message = "经验要求值必须在0-5之间")
  private Integer experienceRequired;

  /** 每周实习天数（仅实习类型需要） */
  @Min(value = 1, message = "每周实习天数不能小于1")
  @Max(value = 7, message = "每周实习天数不能大于7")
  private Integer internshipDaysPerWeek;

  /** 实习时长（月为单位，仅实习类型需要） */
  @Min(value = 1, message = "实习时长不能小于1个月")
  @Max(value = 24, message = "实习时长不能大于24个月")
  private Integer internshipDurationMonths;

  /** 职位描述 */
  @NotBlank(message = "职位描述不能为空")
  private String description;

  /** 岗位职责 */
  private String responsibilities;

  /** 任职要求 */
  private String requirements;

  /** 状态：0-已下线 1-招聘中 2-已暂停（默认为1-招聘中） */
  private Integer status;

  /** 技能要求列表 */
  private List<JobSkillDTO> skills;
}
