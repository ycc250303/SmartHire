package com.SmartHire.seekerService.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

/**
 * 技能表
 *
 * @author SmartHire
 * @since 2025-11-21
 */
@Getter
@Setter
@TableName("skill")
public class Skill implements Serializable {
  @Serial private static final long serialVersionUID = 1L;

  @TableId(value = "id", type = IdType.AUTO)
  private Long id;

  @JsonIgnore private Long jobSeekerId;

  private String skillName;

  private Byte level;

  @JsonIgnore private Date createdAt;

  @JsonIgnore private Date updatedAt;
}
