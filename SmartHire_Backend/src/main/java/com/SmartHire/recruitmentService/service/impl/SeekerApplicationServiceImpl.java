package com.SmartHire.recruitmentService.service.impl;

import com.SmartHire.common.api.HrApi;
import com.SmartHire.common.api.SeekerApi;
import com.SmartHire.common.auth.UserContext;
import com.SmartHire.common.event.ApplicationCreatedEvent;
import com.SmartHire.common.exception.enums.ErrorCode;
import com.SmartHire.common.exception.exception.BusinessException;
import com.SmartHire.recruitmentService.dto.SeekerApplicationDTO;
import com.SmartHire.recruitmentService.dto.SeekerApplicationListDTO;
import com.SmartHire.recruitmentService.dto.SubmitResumeDTO;
import com.SmartHire.recruitmentService.mapper.ApplicationMapper;
import com.SmartHire.recruitmentService.model.Application;
import com.SmartHire.recruitmentService.service.ApplicationEventProducer;
import com.SmartHire.recruitmentService.service.SeekerApplicationService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 求职者侧投递服务实现类
 */
@Slf4j
@Service
public class SeekerApplicationServiceImpl extends ServiceImpl<ApplicationMapper, Application>
        implements SeekerApplicationService {

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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long submitResume(SubmitResumeDTO request) {
        if (request == null || request.getJobId() == null) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }

        Long jobId = request.getJobId();
        Long resumeId = request.getResumeId();
        Long userId = userContext.getCurrentUserId();
        Long seekerId = seekerApi.getJobSeekerIdByUserId(userId);

        if (resumeId != null && !seekerApi.validateResumeOwnership(resumeId, seekerId)) {
            throw new BusinessException(ErrorCode.RESUME_NOT_BELONG_TO_USER);
        }

        long existingCount = lambdaQuery()
                .eq(Application::getJobId, jobId)
                .eq(Application::getJobSeekerId, seekerId)
                .count();
        if (existingCount > 0) {
            throw new BusinessException(ErrorCode.APPLICATION_ALREADY_EXISTS);
        }

        Application application = new Application();
        application.setJobId(jobId);
        application.setJobSeekerId(seekerId);
        application.setInitiator((byte) 0);
        application.setStatus((byte) 0);

        Date now = new Date();
        application.setCreatedAt(now);
        application.setUpdatedAt(now);

        boolean saved = this.save(application);
        if (!saved) {
            log.error("保存投递记录失败, jobId={}, seekerId={}, resumeId={}", jobId, seekerId, resumeId);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }

        String resumeType = resumeId != null ? "附件简历" : "在线简历";
        log.info("投递{}成功, jobId={}, seekerId={}, resumeId={}", resumeType, jobId, seekerId, resumeId);

        Long hrId = hrApi.getHrIdByJobId(jobId);
        Long hrUserId = hrApi.getHrUserIdByHrId(hrId);
        ApplicationCreatedEvent event = new ApplicationCreatedEvent();
        event.setApplicationId(application.getId());
        event.setJobId(jobId);
        event.setJobSeekerId(seekerId);
        event.setSeekerUserId(userId);
        event.setHrId(hrId);
        event.setHrUserId(hrUserId);
        event.setMessageContent("您好，我对这个岗位感兴趣");
        event.setInitiator((byte) 0);

        applicationEventProducer.publishApplicationCreated(event);
        log.info("投递/推荐岗位创建事件已发布: applicationId={}, jobId={}, seekerId={}", application.getId(), jobId, seekerId);
        return application.getId();
    }

    @Override
    public SeekerApplicationListDTO getSeekerApplicationList(Integer page, Integer size) {
        if (page == null || page < 1) page = 1;
        if (size == null || size < 1) size = 20;

        Long userId = userContext.getCurrentUserId();
        Long seekerId = seekerApi.getJobSeekerIdByUserId(userId);
        int offset = (page - 1) * size;

        List<SeekerApplicationDTO> applicationList = applicationMapper.selectSeekerApplicationList(
                seekerId, page, size, offset);
        Long total = applicationMapper.countSeekerApplicationList(seekerId);

        SeekerApplicationListDTO result = new SeekerApplicationListDTO();
        result.setTotal(total);
        result.setPage(page);
        result.setSize(size);
        result.setList(applicationList);

        return result;
    }

    @Override
    public boolean existsBySeekerIdAndJobId(Long seekerUserId, Long jobId) {
        log.info("check application exists: seekerUserId={}, jobId={}", seekerUserId, jobId);
        if (seekerUserId == null) throw new BusinessException(ErrorCode.SEEKER_NOT_EXIST);
        if (jobId == null) throw new BusinessException(ErrorCode.JOB_NOT_EXIST);
        
        Long seekerId = seekerApi.getJobSeekerIdByUserId(seekerUserId);
        return lambdaQuery()
                .eq(Application::getJobId, jobId)
                .eq(Application::getJobSeekerId, seekerId)
                .exists();
    }
}

