package com.SmartHire.adminService.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.SmartHire.adminService.model.Report;
import com.SmartHire.adminService.dto.ReportDetailDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 举报Mapper接口
 */
@Mapper
public interface ReportMapper extends BaseMapper<Report> {

    /**
     * 分页查询举报列表（包含关联信息）
     * @param page 分页参数
     * @param targetType 举报对象类型
     * @param reportType 举报类型
     * @param status 处理状态
     * @param keyword 关键词
     * @return 分页结果
     */
    Page<ReportDetailDTO> selectReportListWithDetails(
            Page<ReportDetailDTO> page,
            @Param("targetType") Integer targetType,
            @Param("reportType") Integer reportType,
            @Param("status") Integer status,
            @Param("keyword") String keyword
    );

    /**
     * 根据ID查询举报详情
     * @param id 举报ID
     * @return 举报详情
     */
    ReportDetailDTO selectReportDetailById(@Param("id") Long id);

    /**
     * 更新举报处理状态
     * @param id 举报ID
     * @param handlerId 处理人ID
     * @param handleResult 处理结果
     * @param handleReason 处理原因
     * @return 影响行数
     */
    @Update("UPDATE reports SET status = 1, handler_id = #{handlerId}, " +
            "handle_result = #{handleResult}, handle_reason = #{handleReason}, " +
            "handle_time = NOW() WHERE id = #{id}")
    int updateReportHandle(@Param("id") Long id,
                          @Param("handlerId") Long handlerId,
                          @Param("handleResult") Integer handleResult,
                          @Param("handleReason") String handleReason);

    /**
     * 获取举报统计信息
     * @return 统计结果
     */
    @Select("SELECT " +
            "COUNT(*) as total, " +
            "SUM(CASE WHEN status = 0 THEN 1 ELSE 0 END) as pendingCount, " +
            "SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) as resolvedCount " +
            "FROM reports")
    java.util.Map<String, Object> getReportStats();

    /**
     * 批量查询举报详情
     * @param ids 举报ID列表
     * @return 举报详情列表
     */
    List<ReportDetailDTO> selectReportDetailsByIds(@Param("ids") List<Long> ids);

    /**
     * 查询被举报用户详情
     * @param userId 用户ID
     * @return 用户详情
     */
    ReportDetailDTO.UserInfoDTO selectTargetUserById(@Param("userId") Long userId);

    /**
     * 查询被举报职位详情
     * @param jobId 职位ID
     * @return 职位详情
     */
    ReportDetailDTO.JobInfoDTO selectTargetJobById(@Param("jobId") Long jobId);
}