package com.SmartHire.hrService.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 简历列表查询参数
 */
@Data
public class ApplicationQueryDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 页码（从1开始）
     */
    @Min(value = 1, message = "页码至少为1")
    private Integer pageNum = 1;

    /**
     * 每页数量
     */
    @Min(value = 1, message = "每页数量至少为1")
    @Max(value = 100, message = "每页数量不能超过100")
    private Integer pageSize = 10;

    /**
     * 岗位ID
     */
    private Long jobId;

    /**
     * 状态：0-已投递 1-已查看 2-待面试 3-已面试 4-已录用 5-已拒绝 6-已撤回
     */
    @Min(value = 0, message = "状态值必须在0-6之间")
    @Max(value = 6, message = "状态值必须在0-6之间")
    private Integer status;

    /**
     * 关键词（岗位名称或求职者姓名）
     */
    @Size(max = 50, message = "关键词长度不能超过50个字符")
    private String keyword;
}


