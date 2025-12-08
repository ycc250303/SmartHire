package com.SmartHire.hrService.mapper;

import com.SmartHire.recruitmentService.dto.ApplicationListDTO;
import com.SmartHire.recruitmentService.model.Application;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 简历 Mapper 接口
 */
@Mapper
public interface HrApplicationMapper extends BaseMapper<Application> {

    /**
     * 分页查询投递列表
     *
     * @param page   分页对象
     * @param hrId   HR ID
     * @param jobId  岗位ID
     * @param status 状态
     * @param keyword 关键词
     * @return 分页结果
     */
    Page<ApplicationListDTO> selectApplicationList(Page<ApplicationListDTO> page,
                                                   @Param("hrId") Long hrId,
                                                   @Param("jobId") Long jobId,
                                                   @Param("status") Integer status,
                                                   @Param("keyword") String keyword);

    /**
     * 查询岗位下所有投递
     *
     * @param hrId  HR ID
     * @param jobId 岗位ID
     * @return 投递列表
     */
    List<ApplicationListDTO> selectApplicationsByJob(@Param("hrId") Long hrId,
                                                     @Param("jobId") Long jobId);

    /**
     * 查询投递详情
     *
     * @param applicationId 投递ID
     * @param hrId          HR ID
     * @return 投递详情
     */
    ApplicationListDTO selectApplicationDetail(@Param("applicationId") Long applicationId,
                                               @Param("hrId") Long hrId);

    /**
     * 更新匹配结果
     *
     * @param applicationId 投递ID
     * @param score         匹配分
     * @param analysis      匹配分析
     * @param updatedAt     更新时间
     */
    void updateMatchResult(@Param("applicationId") Long applicationId,
                           @Param("score") java.math.BigDecimal score,
                           @Param("analysis") String analysis,
                           @Param("updatedAt") java.util.Date updatedAt);
}

