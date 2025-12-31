package com.SmartHire.recruitmentService.service.impl;

import com.SmartHire.common.api.HrApi;
import com.SmartHire.common.api.SeekerApi;
import com.SmartHire.common.auth.UserContext;
import com.SmartHire.common.dto.seekerDto.SeekerCardDTO;
import com.SmartHire.common.event.ApplicationCreatedEvent;
import com.SmartHire.common.event.ApplicationRejectedEvent;
import com.SmartHire.common.exception.enums.ErrorCode;
import com.SmartHire.common.exception.exception.BusinessException;
import com.SmartHire.recruitmentService.dto.ApplicationListDTO;
import com.SmartHire.recruitmentService.dto.ApplicationQueryDTO;
import com.SmartHire.recruitmentService.dto.RecommendRequest;
import com.SmartHire.recruitmentService.mapper.ApplicationMapper;
import com.SmartHire.recruitmentService.model.Application;
import com.SmartHire.recruitmentService.service.ApplicationEventProducer;
import com.SmartHire.recruitmentService.service.ApplicationRejectedEventProducer;
import com.SmartHire.recruitmentService.service.HrApplicationService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;

/**
 * HR侧招聘投递管理服务实现类
 */
@Slf4j
@Service
public class HrApplicationServiceImpl extends ServiceImpl<ApplicationMapper, Application>
        implements HrApplicationService {

    @Autowired
    private SeekerApi seekerApi;

    @Autowired
    private HrApi hrApi;

    @Autowired
    private UserContext userContext;

    @Autowired
    private ApplicationMapper applicationMapper;

    @Autowired
    private ApplicationEventProducer applicationEventProducer;

    @Autowired
    private ApplicationRejectedEventProducer applicationRejectedEventProducer;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long recommend(RecommendRequest request) {
        // ... (existing code same)
        if (request == null || request.getJobId() == null || request.getSeekerUserId() == null) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }

        Long jobId = request.getJobId();
        Long hrUserId = userContext.getCurrentUserId();
        Long hrId = hrApi.getHrIdByUserId(hrUserId);
        if (hrId == null)
            throw new BusinessException(ErrorCode.HR_NOT_EXIST);

        Long seekerId = seekerApi.getJobSeekerIdByUserId(request.getSeekerUserId());
        if (seekerId == null)
            throw new BusinessException(ErrorCode.SEEKER_NOT_EXIST);

        if (!hrApi.isHrAuthorizedForJob(hrId, jobId)) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED);
        }

        long existingCount = lambdaQuery()
                .eq(Application::getJobId, jobId)
                .eq(Application::getJobSeekerId, seekerId)
                .count();
        if (existingCount > 0) {
            throw new BusinessException(ErrorCode.RECOMMEND_ALREADY_EXISTS);
        }

        Application application = new Application();
        application.setJobId(jobId);
        application.setJobSeekerId(seekerId);
        application.setInitiator((byte) 1);
        application.setStatus((byte) 0);

        Date now = new Date();
        application.setCreatedAt(now);
        application.setUpdatedAt(now);

        if (!this.save(application)) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }

        Long targetHrId = hrApi.getHrIdByJobId(jobId);
        Long targetHrUserId = hrApi.getHrUserIdByHrId(targetHrId);
        ApplicationCreatedEvent event = new ApplicationCreatedEvent();
        event.setApplicationId(application.getId());
        event.setJobId(jobId);
        event.setJobSeekerId(seekerId);
        event.setSeekerUserId(request.getSeekerUserId());
        event.setHrId(targetHrId);
        event.setHrUserId(targetHrUserId);
        event.setMessageContent(request.getNote() != null ? request.getNote() : "HR 推荐岗位给您");
        event.setInitiator((byte) 1);

        applicationEventProducer.publishApplicationCreated(event);
        return application.getId();
    }

    @Override
    public Page<ApplicationListDTO> getApplicationList(ApplicationQueryDTO queryDTO) {
        Long hrId = hrApi.getCurrentHrId();
        int pageNum = queryDTO.getPageNum() == null ? 1 : queryDTO.getPageNum();
        int pageSize = queryDTO.getPageSize() == null ? 10 : queryDTO.getPageSize();

        Page<ApplicationListDTO> page = new Page<>(pageNum, pageSize);
        String keyword = StringUtils.hasText(queryDTO.getKeyword()) ? queryDTO.getKeyword().trim() : null;

        return applicationMapper.selectApplicationList(page, hrId, queryDTO.getJobId(), queryDTO.getStatus(), keyword);
    }

    @Override
    public ApplicationListDTO getApplicationDetail(Long applicationId) {
        Long hrId = hrApi.getCurrentHrId();
        ApplicationListDTO detail = applicationMapper.selectApplicationDetail(applicationId, hrId);
        if (detail == null)
            throw new BusinessException(ErrorCode.APPLICATION_NOT_EXIST);
        return detail;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateApplicationStatus(Long applicationId, Byte status) {
        if (status == null || status < 0 || status > 6)
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        Long hrId = hrApi.getCurrentHrId();
        Application application = validateApplicationOwnership(applicationId, hrId);
        application.setStatus(status);
        application.setUpdatedAt(new Date());
        updateById(application);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectApplication(Long applicationId, String reason, Boolean sendNotification, String templateId) {
        Long hrId = hrApi.getCurrentHrId();
        Application application = validateApplicationOwnership(applicationId, hrId);

        if (application.getStatus() != null && application.getStatus() == 5)
            return;

        Date now = new Date();
        application.setStatus((byte) 5);
        String prev = application.getMatchAnalysis();
        String note = String.format("rejected_by_hr:%s reason:%s at:%s", hrId, reason == null ? "" : reason, now);
        application.setMatchAnalysis((prev == null ? "" : prev + " | ") + note);
        application.setUpdatedAt(now);
        updateById(application);

        Long jobSeekerId = application.getJobSeekerId();
        Long seekerUserId = seekerApi.getJobSeekerById(jobSeekerId).getUserId();
        Long hrUserId = hrApi.getHrUserIdByHrId(hrId);

        ApplicationRejectedEvent event = new ApplicationRejectedEvent();
        event.setApplicationId(applicationId);
        event.setSeekerUserId(seekerUserId);
        event.setHrUserId(hrUserId);
        event.setReason(reason);
        event.setTemplateId(templateId);

        applicationRejectedEventProducer.publishApplicationRejected(event);
    }

    @Override
    public SeekerCardDTO getSeekerCard(Long userId) {
        SeekerCardDTO seekerCard = seekerApi.getSeekerCard(userId);
        if (seekerCard == null) {
            throw new BusinessException(ErrorCode.SEEKER_NOT_EXIST);
        }
        return seekerCard;
    }

    private Application validateApplicationOwnership(Long applicationId, Long hrId) {
        Application application = getById(applicationId);
        if (application == null)
            throw new BusinessException(ErrorCode.APPLICATION_NOT_EXIST);
        if (!hrApi.isHrAuthorizedForJob(hrId, application.getJobId())) {
            throw new BusinessException(ErrorCode.APPLICATION_NOT_BELONG_TO_HR);
        }
        return application;
    }
}
