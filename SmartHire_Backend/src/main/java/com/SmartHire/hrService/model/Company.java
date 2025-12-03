package com.SmartHire.hrService.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 公司信息实体类 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("company")
public class Company implements Serializable {

  private static final long serialVersionUID = 1L;

  /** 公司ID */
  @TableId(value = "id", type = IdType.AUTO)
  private Long id;

  /** 公司名称 */
  private String companyName;

  /** 公司简介 */
  private String description;

  /** 公司规模 */
  private String scale;

  /** 公司行业 */
  private String industry;

  /** 公司地址 */
  private String address;

  /** 公司城市 */
  private String city;

  /** 创建时间 */
  private Date createdAt;

  /** 更新时间 */
  private Date updatedAt;
}
