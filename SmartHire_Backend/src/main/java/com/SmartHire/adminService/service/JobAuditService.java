package com.SmartHire.adminService.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.SmartHire.adminService.dto.JobAuditListDTO;
import com.SmartHire.adminService.dto.JobAuditQueryDTO;
import com.SmartHire.adminService.model.JobAuditRecord;

/**
 * 职位审核Service接口
 *
 * @author SmartHire Team
 * @since 2025-12-09
 */
public interface JobAuditService extends IService<JobAuditRecord> {

    /**
     * 分页查询审核列表（完全支持前端功能）
     *
     * @param page 分页对象
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    Page<JobAuditListDTO> getAuditList(Page<JobAuditListDTO> page,
                                     JobAuditQueryDTO queryDTO);

    /**
     * 审核通过职位
     *
     * @param jobId 职位ID
     * @param auditorId 审核员ID
     * @param auditorName 审核员姓名
     */
    void approveJob(Long jobId, Long auditorId, String auditorName);

    /**
     * 拒绝职位
     *
     * @param jobId 职位ID
     * @param rejectReason 拒绝原因
     * @param auditorId 审核员ID
     * @param auditorName 审核员姓名
     */
    void rejectJob(Long jobId, String rejectReason, Long auditorId, String auditorName);

    /**
     * 要求修改职位
     *
     * @param jobId 职位ID
     * @param modifyReason 修改意见
     * @param auditorId 审核员ID
     * @param auditorName 审核员姓名
     */
    void modifyJob(Long jobId, String modifyReason, Long auditorId, String auditorName);

    /**
     * 根据状态统计数量
     *
     * @param status 审核状态
     * @return 数量
     */
    Integer countByStatus(String status);

    /**
     * 获取审核详情
     *
     * @param jobId 职位ID
     * @return 审核记录
     */
    JobAuditRecord getAuditDetail(Long jobId);
}