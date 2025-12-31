package com.SmartHire.common.dto.hrDto;

import java.io.Serial;
import java.io.Serializable;
import lombok.Data;

/** 岗位完整详情DTO - 包含岗位、公司及HR信息 */
@Data
public class JobFullDetailDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 岗位基本信息 */
    private JobInfoDTO jobInfo;

    /** 公司信息 */
    private CompanyDTO company;

    /** HR信息 */
    private HrInfoDTO hrInfo;
}
