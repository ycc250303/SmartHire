package com.SmartHire.hrService.mapper;

import com.SmartHire.hrService.model.JobSkillRequirement;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** 职位技能要求 Mapper 接口 */
@Mapper
public interface JobSkillRequirementMapper extends BaseMapper<JobSkillRequirement> {

  /**
   * 根据职位ID查询技能名称列表
   *
   * @param jobId 职位ID
   * @return 技能名称列表
   */
  List<String> selectSkillNamesByJobId(@Param("jobId") Long jobId);

  /**
   * 根据职位ID删除所有技能要求
   *
   * @param jobId 职位ID
   */
  void deleteByJobId(@Param("jobId") Long jobId);
}
