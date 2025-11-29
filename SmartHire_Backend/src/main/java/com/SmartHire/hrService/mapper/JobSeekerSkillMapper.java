package com.SmartHire.hrService.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 求职者技能 Mapper
 */
@Mapper
public interface JobSeekerSkillMapper {

    /**
     * 根据求职者ID查询技能名称
     *
     * @param jobSeekerId 求职者ID
     * @return 技能名称列表
     */
    List<String> selectSkillNamesByJobSeekerId(@Param("jobSeekerId") Long jobSeekerId);
}


