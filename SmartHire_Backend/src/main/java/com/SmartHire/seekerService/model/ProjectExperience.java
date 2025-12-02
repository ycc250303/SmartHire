package com.SmartHire.seekerService.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

/**
 * 项目经历表
 *
 * @author SmartHire Team
 * @since 2025-11-19
 */
@Getter
@Setter
@TableName("project_experience")
public class ProjectExperience implements Serializable {

  private static final long serialVersionUID = 1L;

  /** 记录ID */
  @TableId(value = "id", type = IdType.AUTO)
  private Long id;

  /** 求职者ID */
  @JsonIgnore private Long jobSeekerId;

  /** 项目名称 */
  private String projectName;

  /** 项目角色 */
  private String projectRole;

  /** 开始月份 */
  @TableField("start_date")
  private LocalDate startDate;

  /** 结束月份 */
  @TableField("end_date")
  private LocalDate endDate;

  /** 项目描述 */
  private String description;

  /** 职责描述 */
  private String responsibility;

  /** 项目成果 */
  private String achievement;

  /** 项目链接 */
  private String projectUrl;

  /** 创建时间 */
  @JsonIgnore private Date createdAt;

  /** 更新时间 */
  @JsonIgnore private Date updatedAt;
}
