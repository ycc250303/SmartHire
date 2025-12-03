package com.SmartHire.hrService.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.io.Serial;
import java.io.Serializable;
import lombok.Data;

/** HR信息更新DTO */
@Data
public class HrInfoUpdateDTO implements Serializable {

  @Serial private static final long serialVersionUID = 1L;

  /** 真实姓名 */
  @Size(max = 50, message = "真实姓名长度不能超过50个字符")
  private String realName;

  /** 职位 */
  @Size(max = 50, message = "职位长度不能超过50个字符")
  private String position;

  /** 工作电话 */
  @Pattern(
      regexp = "^1[3-9]\\d{9}$|^\\d{3}-\\d{4}-\\d{4}$|^\\d{4}-\\d{7,8}$",
      message = "工作电话格式不正确")
  @Size(max = 20, message = "工作电话长度不能超过20个字符")
  private String workPhone;
}
