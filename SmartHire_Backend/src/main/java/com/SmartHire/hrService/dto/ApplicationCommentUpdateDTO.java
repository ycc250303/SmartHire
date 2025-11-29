package com.SmartHire.hrService.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 简历备注更新DTO
 */
@Data
public class ApplicationCommentUpdateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * HR备注
     */
    @NotBlank(message = "备注内容不能为空")
    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String comment;
}


