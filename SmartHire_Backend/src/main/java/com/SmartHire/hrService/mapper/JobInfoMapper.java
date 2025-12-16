package com.SmartHire.hrService.mapper;

import com.SmartHire.hrService.dto.JobCardDTO;
import com.SmartHire.hrService.model.JobInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import java.math.BigDecimal;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** 岗位 Mapper 接口 */
@Mapper
public interface JobInfoMapper extends BaseMapper<JobInfo> {

  /**
   * 求职者端岗位筛选
   *
   * @param city 城市
   * @param jobType 职位类型
   * @param educationRequired 学历要求
   * @param minSalary 最低薪资
   * @param maxSalary 最高薪资
   * @param keyword 关键字
   * @param skills 技能列表
   * @param companyId 公司ID
   * @return 岗位卡片列表
   */
  List<JobCardDTO> searchPublicJobCards(
      @Param("city") String city,
      @Param("jobType") Integer jobType,
      @Param("educationRequired") Integer educationRequired,
      @Param("minSalary") BigDecimal minSalary,
      @Param("maxSalary") BigDecimal maxSalary,
      @Param("keyword") String keyword,
      @Param("skills") List<String> skills,
      @Param("companyId") Long companyId,
      @Param("offset") Integer offset,
      @Param("limit") Integer limit);
}
