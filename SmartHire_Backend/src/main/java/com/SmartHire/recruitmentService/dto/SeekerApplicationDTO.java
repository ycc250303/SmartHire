package com.SmartHire.recruitmentService.dto;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/** 求职者投递记录DTO */
@Data
public class SeekerApplicationDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 投递ID */
    private Long applicationId;

    /** 职位ID */
    private Long jobId;

    /** 职位名称 */
    private String jobTitle;

    /** 公司名称 */
    private String companyName;

    /** 公司Logo */
    private String companyLogo;

    /** 状态：0-已投递/已推荐 1-已查看 2-待面试 3-已面试 4-已录用 5-已拒绝 6-已撤回 */
    private Integer status;

    /** 投递时间 */
    private Date appliedAt;

    /** 会话ID */
    private Long conversationId;

    /** HR姓名 */
    private String hrName;

    /** HR头像 */
    private String hrAvatar;
}
