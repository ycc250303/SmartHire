package com.SmartHire.recruitmentService.service.impl;

import com.SmartHire.recruitmentService.dto.SubmitResumeDTO;
import com.SmartHire.recruitmentService.model.Application;
import com.SmartHire.recruitmentService.mapper.ApplicationMapper;
import com.SmartHire.recruitmentService.service.ApplicationService;
import com.SmartHire.seekerService.model.Resume;
import com.SmartHire.seekerService.service.JobSeekerService;
import com.SmartHire.seekerService.service.seekerTableService.ResumeService;
import com.SmartHire.shared.exception.enums.ErrorCode;
import com.SmartHire.shared.exception.exception.BusinessException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * <p>
 * 投递/推荐记录表 服务实现类
 * </p>
 *
 * @author SmartHire Team
 * @since 2025-11-30
 */
@Slf4j
@Service
public class ApplicationServiceImpl extends ServiceImpl<ApplicationMapper, Application> implements ApplicationService {

    @Autowired
    private JobSeekerService jobSeekerService;

    @Autowired
    private ResumeService resumeService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitResume(SubmitResumeDTO request) {
        // 参数校验
        if (request == null || request.getJobId() == null) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }

        Long jobId = request.getJobId();
        Long resumeId = request.getResumeId();

        // 获取当前求职者ID
        Long seekerId = jobSeekerService.getJobSeekerId();
        if (seekerId == null) {
            throw new BusinessException(ErrorCode.SEEKER_NOT_EXIST);
        }

        // 如果提供了简历ID，则校验附件简历是否存在且属于当前用户
        if (resumeId != null) {
            Resume resume = resumeService.getById(resumeId);
            if (resume == null) {
                throw new BusinessException(ErrorCode.RESUME_NOT_EXIST);
            }
            if (!resume.getJobSeekerId().equals(seekerId)) {
                throw new BusinessException(ErrorCode.RESUME_NOT_BELONG_TO_USER);
            }
        }
        // 如果 resumeId 为 null，则投递在线简历（不需要额外校验）

        // 检查是否已投递过该职位（避免重复投递）
        long existingCount = lambdaQuery()
                .eq(Application::getJobId, jobId)
                .eq(Application::getJobSeekerId, seekerId)
                .count();
        if (existingCount > 0) {
            throw new BusinessException(ErrorCode.APPLICATION_ALREADY_EXISTS);
        }

        // 创建投递记录
        Application application = new Application();
        application.setJobId(jobId);
        application.setJobSeekerId(seekerId);
        application.setResumeId(resumeId); // 可能为null（在线简历）
        application.setInitiator((byte) 0); // 0-求职者投递
        application.setStatus((byte) 0); // 0-已投递/已推荐

        Date now = new Date();
        application.setCreatedAt(now);
        application.setUpdatedAt(now);

        // 保存投递记录
        boolean saved = this.save(application);
        if (!saved) {
            log.error("保存投递记录失败, jobId={}, seekerId={}, resumeId={}", jobId, seekerId, resumeId);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }

        String resumeType = resumeId != null ? "附件简历" : "在线简历";
        log.info("投递{}成功, jobId={}, seekerId={}, resumeId={}", resumeType, jobId, seekerId, resumeId);

        // TODO：添加消息通知并发送给HR
    }
}
