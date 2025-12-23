package com.SmartHire.recruitmentService.dto;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import lombok.Data;

@Data
public class SeekerJobPositionDTO implements Serializable {
  @Serial private static final long serialVersionUID = 1L;

  private Long jobId;
  private String jobTitle;
  private String jobCategory;
  private String department;
  private String city;
  private String address;
  private Integer salaryMin;
  private Integer salaryMax;
  private Integer salaryMonths;
  private Integer educationRequired;
  private Integer jobType;
  private Integer experienceRequired;
  private String description;
  private String responsibilities;
  private String requirements;
  private List<String> skills;
  private Integer viewCount;
  private Integer applicationCount;
  private Date publishedAt;

  private CompanyDTO company;
  private HrDTO hr;
  private ApplicationDTO application;

  @Data
  public static class CompanyDTO implements Serializable {
    private Long companyId;
    private String companyName;
    private String companyLogo;
    private Integer companyScale;
    private Integer financingStage;
    private String industry;
    private String description;
    private String mainBusiness;
    private String website;
  }

  @Data
  public static class HrDTO implements Serializable {
    private Long hrId;
    private String realName;
    private String position;
    private String avatarUrl;
  }

  @Data
  public static class ApplicationDTO implements Serializable {
    private Boolean hasApplied;
    private Long applicationId;
    private Integer status;
    private Date appliedAt;
    private Long conversationId;
  }
}


