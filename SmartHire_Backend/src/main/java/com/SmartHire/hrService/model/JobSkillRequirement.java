package com.SmartHire.hrService.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 职位技能要求实体类 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("job_skill_requirement")
public class JobSkillRequirement implements Serializable {

  private static final long serialVersionUID = 1L;

  /** 记录ID */
  @TableId(value = "id", type = IdType.AUTO)
  private Long id;

  /** 职位ID */
  private Long jobId;

  /** 技能名称 */
  private String skillName;

  /** 是否必需：0-加分项 1-必须 */
  private Integer isRequired;

  /** 创建时间 */
  private Date createdAt;
}
