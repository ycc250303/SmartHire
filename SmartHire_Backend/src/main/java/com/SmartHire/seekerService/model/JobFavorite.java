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
 * 职位收藏表
 *
 * @author SmartHire Team
 * @since 2025-11-19
 */
@Getter
@Setter
@TableName("job_favorite")
public class JobFavorite implements Serializable {
  @Serial private static final long serialVersionUID = 1L;

  /** 收藏ID */
  @TableId(value = "id", type = IdType.AUTO)
  @JsonIgnore
  private Long id;

  /** 求职者ID */
  @JsonIgnore private Long jobSeekerId;

  /** 职位ID */
  private Long jobId;

  /** 收藏时间 */
  @JsonIgnore private Date createdAt;

  /**
   * 获取收藏时间（返回防御性拷贝）
   */
  public Date getCreatedAt() {
    return createdAt == null ? null : new Date(createdAt.getTime());
  }

  /**
   * 设置收藏时间（存储防御性拷贝）
   */
  public void setCreatedAt(Date createdAt) {
    this.createdAt = createdAt == null ? null : new Date(createdAt.getTime());
  }
}
