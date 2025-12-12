package com.SmartHire.adminService.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.SmartHire.adminService.model.Report;
import com.SmartHire.adminService.dto.ReportDetailDTO;
import com.SmartHire.adminService.dto.ReportQueryDTO;
import com.SmartHire.adminService.dto.ReportHandleDTO;

import java.util.Map;

/**
 * 举报服务接口
 */
public interface ReportService extends IService<Report> {

    /**
     * 分页查询举报列表
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    Page<ReportDetailDTO> getReportList(ReportQueryDTO queryDTO);

    /**
     * 获取举报详情
     * @param id 举报ID
     * @return 举报详情
     */
    ReportDetailDTO getReportDetail(Long id);

    /**
     * 处理举报
     * @param id 举报ID
     * @param handleDTO 处理信息
     * @param handlerId 处理人ID
     * @return 是否成功
     */
    boolean handleReport(Long id, ReportHandleDTO handleDTO, Long handlerId);

    /**
     * 获取举报统计信息
     * @return 统计信息
     */
    Map<String, Object> getReportStats();

    /**
     * 获取证据图片Base64数据
     * @param id 举报ID
     * @return Base64图片数据
     */
    String getEvidenceImage(Long id);
}