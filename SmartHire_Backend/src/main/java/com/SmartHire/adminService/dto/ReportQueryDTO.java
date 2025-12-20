package com.SmartHire.adminService.dto;

import lombok.Data;
import jakarta.validation.constraints.Min;

/**
 * 举报查询DTO
 */
@Data
public class ReportQueryDTO {

    /**
     * 页码，从1开始
     */
    @Min(value = 1, message = "页码最小为1")
    private Integer page = 1;

    /**
     * 当前页码（前端分页组件使用）
     */
    @Min(value = 1, message = "页码最小为1")
    private Integer current = 1;

    /**
     * 每页大小
     */
    @Min(value = 1, message = "每页大小最小为1")
    private Integer size = 20;

    /**
     * 举报对象类型：1-用户, 2-职位
     */
    private Integer targetType;

    /**
     * 举报类型：1-垃圾信息, 2-不当内容, 3-虚假职位, 4-欺诈行为, 5-骚扰行为, 6-其他
     */
    private Integer reportType;

    /**
     * 处理状态：0-待处理, 1-已处理
     */
    private Integer status;

    /**
     * 搜索关键词（搜索举报原因、举报人、被举报对象等）
     */
    private String keyword;
}