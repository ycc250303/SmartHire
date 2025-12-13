package com.SmartHire.adminService.mapper;

import com.SmartHire.adminService.dto.JobAuditListDTO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 职位审核记录Mapper接口
 *
 * @author SmartHire Team
 * @since 2025-12-09
 */
@Mapper
public interface JobAuditMapper
    extends BaseMapper<com.SmartHire.adminService.model.JobAuditRecord> {

  /**
   * 分页查询审核列表（支持前端所有功能）
   *
   * @param page 分页对象
   * @param status 审核状态
   * @param keyword 搜索关键词
   * @return 分页结果
   */
  Page<JobAuditListDTO> selectAuditList(
      Page<JobAuditListDTO> page, @Param("status") String status, @Param("keyword") String keyword);

  /**
   * 根据状态统计数量
   *
   * @param status 审核状态
   * @return 数量
   */
  Integer countByStatus(@Param("status") String status);

  /**
   * 分页查询系统审核列表（只查询已通过公司审核的岗位）
   *
   * @param page 分页对象
   * @param status 系统审核状态
   * @param keyword 搜索关键词
   * @return 分页结果
   */
  Page<JobAuditListDTO> selectSystemAuditList(Page<JobAuditListDTO> page,
                                              @Param("status") String status,
                                              @Param("keyword") String keyword);

  /**
   * 根据系统审核状态统计数量（只统计已通过公司审核的）
   *
   * @param status 系统审核状态
   * @return 数量
   */
  Integer countBySystemStatus(@Param("status") String status);
}
