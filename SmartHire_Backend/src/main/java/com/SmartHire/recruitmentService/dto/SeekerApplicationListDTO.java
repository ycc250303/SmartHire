package com.SmartHire.recruitmentService.dto;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import lombok.Data;

/** 求职者投递记录列表DTO */
@Data
public class SeekerApplicationListDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 总记录数 */
    private Long total;

    /** 当前页码 */
    private Integer page;

    /** 每页大小 */
    private Integer size;

    /** 投递记录列表 */
    private List<SeekerApplicationDTO> list;
}
