package com.SmartHire.adminService.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.SmartHire.adminService.mapper.ReportMapper;
import com.SmartHire.adminService.model.Report;
import com.SmartHire.adminService.service.ReportService;
import com.SmartHire.adminService.service.BanRecordService;
import com.SmartHire.adminService.dto.ReportDetailDTO;
import com.SmartHire.adminService.dto.ReportQueryDTO;
import com.SmartHire.adminService.dto.ReportHandleDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.BeanUtils;

import java.util.Map;

/**
 * 举报服务实现类
 */
@Service
public class ReportServiceImpl extends ServiceImpl<ReportMapper, Report> implements ReportService {

    @Autowired
    private BanRecordService banRecordService;

    @Override
    public Page<ReportDetailDTO> getReportList(ReportQueryDTO queryDTO) {
        // 优先使用current，如果没有则使用page
        Integer pageNum = queryDTO.getCurrent() != null ? queryDTO.getCurrent() : queryDTO.getPage();
        Page<ReportDetailDTO> page = new Page<>(pageNum, queryDTO.getSize());

        Page<ReportDetailDTO> result = baseMapper.selectReportListWithDetails(
                page,
                queryDTO.getTargetType(),
                queryDTO.getReportType(),
                queryDTO.getStatus(),
                queryDTO.getKeyword()
        );

        return result;
    }

    @Override
    public ReportDetailDTO getReportDetail(Long id) {
        ReportDetailDTO detail = baseMapper.selectReportDetailById(id);
        if (detail == null) {
            return null;
        }

        // 如果是举报用户，查询用户详细信息
        if (detail.getTargetType() != null && detail.getTargetType().equals(Report.TargetType.USER)) {
            ReportDetailDTO.UserInfoDTO targetUser = baseMapper.selectTargetUserById(detail.getTargetId());
            detail.setTargetUser(targetUser);
        }

        // 如果是举报职位，查询职位详细信息
        if (detail.getTargetType() != null && detail.getTargetType().equals(Report.TargetType.JOB)) {
            ReportDetailDTO.JobInfoDTO targetJob = baseMapper.selectTargetJobById(detail.getTargetId());
            detail.setTargetJob(targetJob);
        }

        return detail;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean handleReport(Long id, ReportHandleDTO handleDTO, Long handlerId) {
        // 查询举报信息
        Report report = getById(id);
        if (report == null) {
            throw new RuntimeException("举报记录不存在");
        }

        if (report.getStatus().equals(Report.Status.RESOLVED)) {
            throw new RuntimeException("该举报已处理");
        }

        // 更新举报处理状态
        int updateCount = baseMapper.updateReportHandle(
                id, handlerId, handleDTO.getHandleResult(), handleDTO.getHandleReason()
        );

        if (updateCount <= 0) {
            throw new RuntimeException("更新举报状态失败");
        }

        // 根据处理结果执行相应操作
        switch (handleDTO.getHandleResult()) {
            case Report.HandleResult.BAN:
                // 封禁用户
                if (report.getTargetType().equals(Report.TargetType.USER)) {
                    try {
                        // TODO: 调用用户封禁接口
                        // banRecordService.banUser(report.getTargetId(), ...);
                    } catch (Exception e) {
                        throw new RuntimeException("封禁用户失败：" + e.getMessage());
                    }
                }
                break;

            case Report.HandleResult.OFFLINE:
                // 下线职位
                if (report.getTargetType().equals(Report.TargetType.JOB)) {
                    try {
                        // TODO: 调用职位审核模块的下线接口
                        // jobAuditService.offlineJob(report.getTargetId());
                    } catch (Exception e) {
                        throw new RuntimeException("下线职位失败：" + e.getMessage());
                    }
                }
                break;

            case Report.HandleResult.WARN:
                // 发送警告通知
                // TODO: 实现通知功能
                break;

            case Report.HandleResult.NO_ACTION:
                // 无需处理，不做额外操作
                break;
        }

        // 发送通知给被举报方
        if (handleDTO.getNotifyTarget() != null && handleDTO.getNotifyTarget()) {
            // TODO: 实现通知发送功能
        }

        return true;
    }

    @Override
    public Map<String, Object> getReportStats() {
        return baseMapper.getReportStats();
    }

    @Override
    public String getEvidenceImage(Long id) {
        Report report = getById(id);
        if (report == null) {
            throw new RuntimeException("举报记录不存在");
        }
        return report.getEvidenceImage();
    }
}