package com.SmartHire.adminService.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import com.SmartHire.adminService.mapper.ReportMapper;
import com.SmartHire.adminService.model.Report;
import com.SmartHire.adminService.service.ReportService;
import com.SmartHire.adminService.service.BanRecordService;
import com.SmartHire.adminService.service.JobAuditService;
import com.SmartHire.adminService.service.NotificationService;
import com.SmartHire.hrService.model.HrInfo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.SmartHire.adminService.dto.ReportDetailDTO;
import com.SmartHire.adminService.dto.ReportQueryDTO;
import com.SmartHire.adminService.dto.ReportHandleDTO;
import com.SmartHire.adminService.dto.UserBanDTO;
import com.SmartHire.common.auth.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.BeanUtils;

import java.util.Map;

/**
 * 举报服务实现类
 */
@Slf4j
@Service
public class ReportServiceImpl extends ServiceImpl<ReportMapper, Report> implements ReportService {

    @Autowired
    private BanRecordService banRecordService;

    @Autowired
    private JobAuditService jobAuditService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserContext userContext;

    @Autowired
    private com.SmartHire.hrService.mapper.HrInfoMapper hrInfoMapper;

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

        // 获取举报详情（用于通知）
        ReportDetailDTO detail = getReportDetail(id);

        // 获取当前管理员信息
        String auditorName = userContext.getCurrentUsername();

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
                // 封禁用户 - 只能对用户举报执行封禁操作
                if (!report.getTargetType().equals(Report.TargetType.USER)) {
                    throw new RuntimeException("封禁用户操作只能针对用户举报，当前举报对象为职位");
                }

                try {
                    // 集成用户封禁服务
                    UserBanDTO userBanDTO = new UserBanDTO();
                    userBanDTO.setBanDurationType(handleDTO.getBanInfo().getBanType());
                    userBanDTO.setBanDays(handleDTO.getBanInfo().getBanDays());
                    userBanDTO.setBanReason(handleDTO.getHandleReason());
                    userBanDTO.setAdminId(handlerId);
                    userBanDTO.setAdminUsername(auditorName);

                    banRecordService.banUser(report.getTargetId(), userBanDTO);

                    // 发送封禁通知给被举报用户
                    notificationService.sendUserBanNotification(
                        report.getTargetId(),
                        handleDTO.getBanInfo().getBanType(),
                        handleDTO.getBanInfo().getBanDays(),
                        handleDTO.getHandleReason()
                    );

                } catch (Exception e) {
                    throw new RuntimeException("封禁用户失败：" + e.getMessage());
                }
                break;

            case Report.HandleResult.OFFLINE:
                // 下线职位 - 只能对职位举报执行下线操作
                if (!report.getTargetType().equals(Report.TargetType.JOB)) {
                    throw new RuntimeException("下线职位操作只能针对职位举报，当前举报对象为用户");
                }

                try {
                    // 集成职位下线服务
                    jobAuditService.forceOfflineJob(
                        report.getTargetId(),
                        handleDTO.getHandleReason(),
                        handlerId,
                        auditorName
                    );

                    // 发送职位下线通知给HR
                    if (detail.getTargetJob() != null) {
                        // 需要获取HR的user_id，而不是hr_info_id
                        Long hrUserId = getHrUserIdByJobId(detail.getTargetJob().getHrId());
                        if (hrUserId != null) {
                            notificationService.sendJobOfflineNotification(
                                hrUserId,
                                detail.getTargetJob().getJobTitle(),
                                handleDTO.getHandleReason()
                            );
                        }
                    }

                } catch (Exception e) {
                    throw new RuntimeException("下线职位失败：" + e.getMessage());
                }
                break;

            case Report.HandleResult.WARN:
                // 发送警告通知
                if (report.getTargetType().equals(Report.TargetType.USER)) {
                    String targetType = "用户";
                    if (detail.getTargetUser() != null) {
                        targetType = detail.getTargetUser().getUserType() == 2 ? "HR账号" : "求职者账号";
                    }
                    notificationService.sendUserWarningNotification(
                        report.getTargetId(),
                        targetType,
                        handleDTO.getHandleReason()
                    );
                } else if (report.getTargetType().equals(Report.TargetType.JOB)) {
                    // 警告HR（针对职位）
                    if (detail.getTargetJob() != null) {
                        // 需要获取HR的user_id，而不是hr_info_id
                        Long hrUserId = getHrUserIdByJobId(detail.getTargetJob().getHrId());
                        if (hrUserId != null) {
                            notificationService.sendUserWarningNotification(
                                hrUserId,
                                "职位内容",
                                handleDTO.getHandleReason()
                            );
                        }
                    }
                }
                break;

            case Report.HandleResult.NO_ACTION:
                // 无需处理，不做额外操作
                break;
        }

        // 发送通知给举报方和被举报方
        String resultLabel = getHandleResultLabel(handleDTO.getHandleResult());
        String targetTypeLabel = report.getTargetType().equals(Report.TargetType.USER) ? "用户" : "职位";
        String targetTitle = detail.getTargetTitle();

        // 发送通知给举报方
        notificationService.sendReportResultToReporter(
            id,
            report.getReporterId(),
            targetTitle,
            resultLabel,
            handleDTO.getHandleReason()
        );

        // 发送通知给被举报方（除了无需处理的情况）
        if (handleDTO.getHandleResult() != Report.HandleResult.NO_ACTION) {
            if (report.getTargetType().equals(Report.TargetType.USER)) {
                notificationService.sendReportResultToTarget(
                    id,
                    report.getTargetId(),
                    targetTypeLabel,
                    resultLabel,
                    handleDTO.getHandleReason()
                );
            } else if (report.getTargetType().equals(Report.TargetType.JOB) && detail.getTargetJob() != null) {
                // 需要获取HR的user_id，而不是hr_info_id
                Long hrUserId = getHrUserIdByJobId(detail.getTargetJob().getHrId());
                if (hrUserId != null) {
                    notificationService.sendReportResultToTarget(
                        id,
                        hrUserId,
                        targetTypeLabel,
                        resultLabel,
                        handleDTO.getHandleReason()
                    );
                }
            }
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

    /**
     * 根据hr_info_id获取对应的user_id
     * @param hrInfoId hr_info表的主键ID
     * @return 对应的用户ID
     */
    private Long getHrUserIdByJobId(Long hrInfoId) {
        try {
            // 查询hr_info表获取user_id
            LambdaQueryWrapper<HrInfo> wrapper = new LambdaQueryWrapper<>();
            wrapper.select(HrInfo::getUserId).eq(HrInfo::getId, hrInfoId);
            HrInfo hrInfo = hrInfoMapper.selectOne(wrapper);
            return hrInfo != null ? hrInfo.getUserId() : null;
        } catch (Exception e) {
            log.error("获取HR用户ID失败，hrInfoId: {}", hrInfoId, e);
            return null;
        }
    }

    /**
     * 获取处理结果标签
     */
    private String getHandleResultLabel(Integer handleResult) {
        switch (handleResult) {
            case Report.HandleResult.NO_ACTION:
                return "无需处理";
            case Report.HandleResult.WARN:
                return "警告";
            case Report.HandleResult.BAN:
                return "封禁";
            case Report.HandleResult.OFFLINE:
                return "下线";
            default:
                return "未知";
        }
    }
}