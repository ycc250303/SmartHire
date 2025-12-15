package com.SmartHire.adminService.dto;

import lombok.Data;
import java.util.Date;

/**
 * 举报详情DTO
 */
@Data
public class ReportDetailDTO {

    /** 举报ID */
    private Long id;

    /** 举报人ID */
    private Long reporterId;

    /** 举报人名称 */
    private String reporterName;

    /** 举报人类型（1-求职者，2-HR） */
    private Integer reporterType;

    /** 举报对象类型：1-用户, 2-职位 */
    private Integer targetType;

    /** 举报对象ID */
    private Long targetId;

    /** 举报对象名称/标题 */
    private String targetTitle;

    /** 举报类型：1-垃圾信息, 2-不当内容, 3-虚假职位, 4-欺诈行为, 5-骚扰行为, 6-其他 */
    private Integer reportType;

    /** 举报原因/描述 */
    private String reason;

    /** 处理状态：0-待处理, 1-已处理 */
    private Integer status;

    /** 处理人ID */
    private Long handlerId;

    /** 处理结果：0-无需处理, 1-警告, 2-封禁, 3-下线 */
    private Integer handleResult;

    /** 处理原因 */
    private String handleReason;

    /** 处理时间 */
    private Date handleTime;

    /** 证据图片URL */
    private String evidenceImage;

    /** 创建时间 */
    private Date createdAt;

    /** 更新时间 */
    private Date updatedAt;

    /** 被举报用户的详细信息（当targetType=1时使用） */
    private UserInfoDTO targetUser;

    /** 被举报职位的详细信息（当targetType=2时使用） */
    private JobInfoDTO targetJob;

    /**
     * 用户信息DTO
     */
    @Data
    public static class UserInfoDTO {
        private Long id;
        private String username;
        private String email;
        private String phone;
        private Integer userType;
        private Integer status;
        private String avatarUrl;
        private Date createdAt;
    }

    /**
     * 职位信息DTO
     */
    @Data
    public static class JobInfoDTO {
        private Long id;
        private Long hrId;
        private String jobTitle;
        private String jobCategory;
        private String city;
        private String salaryMin;
        private String salaryMax;
        private Integer status;
        private Date createdAt;
        private Long companyId;
        private String companyName;
    }
}