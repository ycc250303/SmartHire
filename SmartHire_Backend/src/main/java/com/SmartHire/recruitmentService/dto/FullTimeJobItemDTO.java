package com.SmartHire.recruitmentService.dto;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import lombok.Data;

/**
 * 全职岗位 DTO
 */
@Data
public class FullTimeJobItemDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long jobId;
    private String title;
    private Integer salary_min;
    private Integer salary_max;
    private String companyName;
    private String city;
    private String address;
    private List<String> skills;
    private Integer experienceRequired;
    /**
     * 匹配分数（0-100）。向量搜索未就绪时使用关键词匹配代替。
     */
    private Integer matchScore;
}
