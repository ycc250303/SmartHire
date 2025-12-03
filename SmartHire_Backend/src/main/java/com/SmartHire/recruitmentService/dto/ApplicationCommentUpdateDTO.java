package com.SmartHire.recruitmentService.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serial;
import java.io.Serializable;
import lombok.Data;

/** 简历备注更新DTO 注意：数据库表中没有hr_comment字段，此DTO保留用于未来扩展 */
@Data
public class ApplicationCommentUpdateDTO implements Serializable {

  @Serial private static final long serialVersionUID = 1L;

  /** HR备注 */
  @NotBlank(message = "备注内容不能为空")
  @Size(max = 500, message = "备注长度不能超过500个字符")
  private String comment;
}
