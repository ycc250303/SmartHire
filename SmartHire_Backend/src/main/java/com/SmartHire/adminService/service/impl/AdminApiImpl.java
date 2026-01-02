package com.SmartHire.adminService.service.impl;

import com.SmartHire.adminService.mapper.ReportMapper;
import com.SmartHire.adminService.model.Report;
import com.SmartHire.common.api.AdminApi;
import com.SmartHire.common.dto.adminDto.ReportSubmitDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminApiImpl implements AdminApi {

    @Autowired
    private ReportMapper reportMapper;

    @Override
    public boolean submitReport(ReportSubmitDTO submitDTO, Long reporterId) {
        Report report = new Report();
        // 将 common DTO 转换为 admin 内部的实体类
        BeanUtils.copyProperties(submitDTO, report);
        report.setReporterId(reporterId);
        report.setStatus(0); // 待处理
        return reportMapper.insert(report) > 0;
    }
}
