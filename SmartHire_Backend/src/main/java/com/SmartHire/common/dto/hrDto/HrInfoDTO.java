package com.SmartHire.common.dto.hrDto;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/** HR信息DTO */
@Data
public class HrInfoDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** HR ID */
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 公司ID */
    private Long companyId;

    /** 真实姓名 */
    private String realName;

    /** 职位 */
    private String position;

    /** 工作电话 */
    private String workPhone;

    /** 是否为公司管理员：0-否 1-是 */
    private Integer isCompanyAdmin;

    /** 创建时间 */
    private Date createdAt;

    /** 更新时间 */
    private Date updatedAt;
}
