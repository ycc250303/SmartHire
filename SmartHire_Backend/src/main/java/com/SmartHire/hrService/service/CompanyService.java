package com.SmartHire.hrService.service;

import com.SmartHire.hrService.dto.CompanyNameDTO;
import com.SmartHire.hrService.dto.HrInfoDTO;
import com.SmartHire.hrService.dto.CompanyDTO;
import com.SmartHire.hrService.model.Company;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

/**
 * 公司管理员服务接口
 *
 * @author SmartHire Team
 * @since 2025-12-13
 */
public interface CompanyService {

    /**
     * 创建公司
     *
     * @param createDTO 公司创建信息
     */
    Long createCompany(CompanyDTO createDTO);

    /**
     * 获取公司信息
     *
     * @param companyId 公司ID
     * @return 公司信息
     */
    CompanyDTO getCompanyById(Long companyId);

    /**
     * 获取公司列表
     *
     * @param current  当前页
     * @param size     每页大小
     * @param keyword  搜索关键词（可选）
     * @return 公司列表
     */
    List<CompanyNameDTO> getCompanies(Long current, Long size, String keyword);

    /**
     * 更新公司信息
     *
     * @param companyId 公司ID
     * @param companyDTO   公司信息
     */
    void updateCompanyInfo(Long companyId, CompanyDTO companyDTO);

    /**
     * 获取本公司HR列表
     *
     * @param page    分页对象
     * @param keyword 搜索关键词（可选）
     * @return HR列表
     */
    Page<HrInfoDTO> getCompanyHrList(Page<HrInfoDTO> page, String keyword);

    /**
     * 审核通过HR
     *
     * @param hrId HR ID
     */
    void approveHr(Long hrId);

    /**
     * 审核拒绝HR
     *
     * @param hrId         HR ID
     * @param rejectReason 拒绝原因
     */
    void rejectHr(Long hrId, String rejectReason);

    /**
     * 获取本公司待审核岗位列表
     *
     * @param page    分页对象
     * @param status  审核状态（可选）
     * @param keyword 搜索关键词（可选）
     * @return 岗位审核列表
     */
    Page<com.SmartHire.adminService.dto.JobAuditListDTO> getCompanyJobAuditList(
            Page<com.SmartHire.adminService.dto.JobAuditListDTO> page, String status, String keyword);

    /**
     * 审核通过岗位（公司审核）
     *
     * @param jobId 岗位ID
     */
    void approveCompanyJob(Long jobId);

    /**
     * 审核拒绝岗位（公司审核）
     *
     * @param jobId        岗位ID
     * @param rejectReason 拒绝原因
     */
    void rejectCompanyJob(Long jobId, String rejectReason);

    /**
     * 要求修改岗位（公司审核）
     *
     * @param jobId        岗位ID
     * @param modifyReason 修改意见
     */
    void modifyCompanyJob(Long jobId, String modifyReason);
}
