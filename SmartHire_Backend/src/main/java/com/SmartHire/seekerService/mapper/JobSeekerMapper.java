package com.SmartHire.seekerService.mapper;

import com.SmartHire.seekerService.dto.SeekerCardDTO;
import com.SmartHire.seekerService.model.JobSeeker;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 求职者信息表 Mapper 接口
 *
 * @author SmartHire Team
 * @since 2025-11-19
 */
public interface JobSeekerMapper extends BaseMapper<JobSeeker> {

  /**
   * 根据城市查询求职者卡片信息
   *
   * @param city 城市名称
   * @return 求职者卡片信息列表
   */
  List<SeekerCardDTO> getSeekerCardsByCity(@Param("city") String city);

  /**
   * 根据学历查询求职者卡片信息
   *
   * @param education 学历级别
   * @return 求职者卡片信息列表
   */
  List<SeekerCardDTO> getSeekerCardsByEducation(@Param("education") Integer education);

  /**
   * 根据期望薪资范围查询求职者卡片信息
   *
   * @param salaryMin 最低期望薪资
   * @param salaryMax 最高期望薪资
   * @param isInternship 是否实习
   * @return 求职者卡片信息列表
   */
  List<SeekerCardDTO> getSeekerCardsBySalaryRange(
      @Param("salaryMin") Double salaryMin,
      @Param("salaryMax") Double salaryMax,
      @Param("isInternship") Integer isInternship);

  /**
   * 根据求职状态查询求职者卡片信息
   *
   * @param jobStatus 求职状态
   * @return 求职者卡片信息列表
   */
  List<SeekerCardDTO> getSeekerCardsByJobStatus(@Param("jobStatus") Integer jobStatus);

  /**
   * 根据是否有实习经历查询求职者卡片信息
   *
   * @param hasInternship 是否有实习经历
   * @return 求职者卡片信息列表
   */
  List<SeekerCardDTO> getSeekerCardsByInternshipExperience(
      @Param("hasInternship") Integer hasInternship);
}
