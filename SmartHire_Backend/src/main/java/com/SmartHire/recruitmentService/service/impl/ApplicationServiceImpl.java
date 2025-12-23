package com.SmartHire.recruitmentService.service.impl;

import com.SmartHire.common.api.HrApi;
import com.SmartHire.common.api.SeekerApi;
import com.SmartHire.common.api.UserAuthApi;
import com.SmartHire.common.auth.UserContext;
import com.SmartHire.common.exception.enums.ErrorCode;
import com.SmartHire.common.exception.exception.BusinessException;
import com.SmartHire.common.event.ApplicationCreatedEvent;
import com.SmartHire.recruitmentService.dto.ApplicationListDTO;
import com.SmartHire.recruitmentService.dto.ApplicationQueryDTO;
import com.SmartHire.recruitmentService.dto.SubmitResumeDTO;
import com.SmartHire.recruitmentService.dto.RecommendRequest;
import com.SmartHire.recruitmentService.dto.SeekerApplicationListDTO;
import com.SmartHire.recruitmentService.dto.SeekerApplicationDTO;
import com.SmartHire.recruitmentService.service.ApplicationEventProducer;
import com.SmartHire.recruitmentService.mapper.ApplicationMapper;
import com.SmartHire.recruitmentService.model.Application;
import com.SmartHire.recruitmentService.service.ApplicationService;
import com.SmartHire.userAuthService.model.User;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.util.Date;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 投递/推荐记录表 服务实现类
 *
 * @author SmartHire Team
 * @since 2025-11-30
 */
@Slf4j
@Service
public class ApplicationServiceImpl extends ServiceImpl<ApplicationMapper, Application>
        implements ApplicationService {

    @Autowired
    private SeekerApi seekerApi;

    @Autowired
    private HrApi hrApi;

    @Autowired
    private UserAuthApi userAuthApi;

    @Autowired
    private UserContext userContext;

    @Autowired
    private ApplicationMapper applicationMapper;

    @Autowired
    private ApplicationEventProducer applicationEventProducer;
    @Autowired
    private com.SmartHire.recruitmentService.service.ApplicationRejectedEventProducer applicationRejectedEventProducer;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long recommend(RecommendRequest request) {
        // 参数校验
        if (request == null || request.getJobId() == null || request.getSeekerUserId() == null) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }

        Long jobId = request.getJobId();
        Long resumeId = request.getResumeId();

        // 获取当前HR用户ID
        Long hrUserId = userContext.getCurrentUserId();

        // 获取当前 HR id（hr_info 表的 id），并校验是 HR
        Long hrId = hrApi.getHrIdByUserId(hrUserId);
        if (hrId == null) {
            throw new BusinessException(ErrorCode.HR_NOT_EXIST);
        }

        // 获取目标求职者的 seekerId（jobSeekerId）
        Long seekerId = seekerApi.getJobSeekerIdByUserId(request.getSeekerUserId());
        if (seekerId == null) {
            throw new BusinessException(ErrorCode.SEEKER_NOT_EXIST);
        }

        // 如果提供了简历ID，则校验附件简历是否存在且属于目标求职者
        if (resumeId != null && !seekerApi.validateResumeOwnership(resumeId, seekerId)) {
            throw new BusinessException(ErrorCode.RESUME_NOT_BELONG_TO_USER);
        }

        // 校验岗位是否存在并且属于当前 HR（或 HR 有权限推荐此岗位）
        Long jobHrId = hrApi.getHrIdByJobId(jobId);
        if (jobHrId == null) {
            throw new BusinessException(ErrorCode.JOB_NOT_EXIST);
        }
        if (!jobHrId.equals(hrId)) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED);
        }

        // 检查是否已投递/已推荐过该职位（避免重复）
        long existingCount = lambdaQuery()
                .eq(Application::getJobId, jobId)
                .eq(Application::getJobSeekerId, seekerId)
                .count();
        if (existingCount > 0) {
            throw new BusinessException(ErrorCode.RECOMMEND_ALREADY_EXISTS);
        }

        // 创建推荐记录（作为 application）
        Application application = new Application();
        application.setJobId(jobId);
        application.setJobSeekerId(seekerId);
        application.setResumeId(resumeId); // 可能为null（在线简历）
        application.setInitiator((byte) 1); // 1-HR推荐
        application.setStatus((byte) 0); // 0-已投递/已推荐

        Date now = new Date();
        application.setCreatedAt(now);
        application.setUpdatedAt(now);

        boolean saved = this.save(application);
        if (!saved) {
            log.error("保存推荐记录失败, jobId={}, seekerId={}, resumeId={}", jobId, seekerId, resumeId);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }

        // 发布投递/推荐岗位创建事件，由 messageService 异步发送通知
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
        log.info("推荐岗位创建事件已发布: applicationId={}, jobId={}, seekerId={}", application.getId(), jobId, seekerId);

        return application.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long submitResume(SubmitResumeDTO request) {
        // 参数校验
        if (request == null || request.getJobId() == null) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }

        Long jobId = request.getJobId();
        Long resumeId = request.getResumeId();

        // 获取当前用户ID
        Long userId = userContext.getCurrentUserId();

        // 获取当前求职者ID
        Long seekerId = seekerApi.getJobSeekerIdByUserId(userId);

        // 如果提供了简历ID，则校验附件简历是否存在且属于当前用户
        if (resumeId != null && !seekerApi.validateResumeOwnership(resumeId, seekerId)) {
            throw new BusinessException(ErrorCode.RESUME_NOT_BELONG_TO_USER);
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

        // 发送投递/推荐岗位创建事件到消息队列，由消息服务异步处理
        // 这样可以解耦 recruitmentService 和 messageService，避免循环依赖
        Long hrId = hrApi.getHrIdByJobId(jobId);
        Long hrUserId = hrApi.getHrUserIdByHrId(hrId); // 获取HR的用户ID
        ApplicationCreatedEvent event = new ApplicationCreatedEvent();
        event.setApplicationId(application.getId());
        event.setJobId(jobId);
        event.setJobSeekerId(seekerId);
        event.setSeekerUserId(userId);
        event.setHrId(hrId);
        event.setHrUserId(hrUserId); // 设置HR用户ID
        event.setMessageContent("您好，我对这个岗位感兴趣");
        event.setInitiator((byte) 0);

        applicationEventProducer.publishApplicationCreated(event);
        log.info("投递/推荐岗位创建事件已发布: applicationId={}, jobId={}, seekerId={}", application.getId(), jobId, seekerId);
        return application.getId();
    }

    /**
     * 获取当前登录HR的ID（hr_info表的id）
     */
    private Long getCurrentHrId() {
        Long userId = userContext.getCurrentUserId();
        User user = userAuthApi.getUserById(userId);
        if (user.getUserType() != 2) {
            throw new BusinessException(ErrorCode.USER_NOT_HR);
        }

        Long hrId = hrApi.getHrIdByUserId(userId);
        if (hrId == null) {
            throw new BusinessException(ErrorCode.HR_NOT_EXIST);
        }

        return hrId;
    }

    /**
     * 校验投递是否属于当前HR
     */
    private Application validateApplicationOwnership(Long applicationId, Long hrId) {
        Application application = getById(applicationId);
        if (application == null) {
            throw new BusinessException(ErrorCode.APPLICATION_NOT_EXIST);
        }

        Long jobHrId = hrApi.getHrIdByJobId(application.getJobId());
        if (jobHrId == null) {
            throw new BusinessException(ErrorCode.JOB_NOT_EXIST);
        }

        if (!jobHrId.equals(hrId)) {
            throw new BusinessException(ErrorCode.APPLICATION_NOT_BELONG_TO_HR);
        }

        return application;
    }

    @Override
    public Page<ApplicationListDTO> getApplicationList(ApplicationQueryDTO queryDTO) {
        Long hrId = getCurrentHrId();
        int pageNum = queryDTO.getPageNum() == null ? 1 : queryDTO.getPageNum();
        int pageSize = queryDTO.getPageSize() == null ? 10 : queryDTO.getPageSize();

        Page<ApplicationListDTO> page = new Page<>(pageNum, pageSize);
        String keyword = StringUtils.hasText(queryDTO.getKeyword()) ? queryDTO.getKeyword().trim() : null;

        return baseMapper.selectApplicationList(
                page, hrId, queryDTO.getJobId(), queryDTO.getStatus(), keyword);
    }

    @Override
    public ApplicationListDTO getApplicationDetail(Long applicationId) {
        Long hrId = getCurrentHrId();
        ApplicationListDTO detail = baseMapper.selectApplicationDetail(applicationId, hrId);
        if (detail == null) {
            throw new BusinessException(ErrorCode.APPLICATION_NOT_EXIST);
        }
        return detail;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateApplicationStatus(Long applicationId, Byte status) {
        if (status == null || status < 0 || status > 6) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }

        Long hrId = getCurrentHrId();
        Application application = validateApplicationOwnership(applicationId, hrId);

        Date now = new Date();
        application.setStatus(status);
        application.setUpdatedAt(now);

        updateById(application);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectApplication(Long applicationId, String reason, Boolean sendNotification, String templateId) {
        Long hrId = getCurrentHrId();
        Application application = validateApplicationOwnership(applicationId, hrId);

        // 如果已被拒绝，直接返回（幂等）
        if (application.getStatus() != null && application.getStatus() == 5) {
            log.info("application {} already rejected", applicationId);
            return;
        }

        Date now = new Date();
        application.setStatus((byte) 5); // 5 = 已拒绝
        // 将拒绝原因写入 matchAnalysis 字段作为最小可行审计（建议后续迁移到专门的审计表）
        String prev = application.getMatchAnalysis();
        String note = String.format("rejected_by_hr:%s reason:%s at:%s", hrId, reason == null ? "" : reason,
                now.toString());
        application.setMatchAnalysis((prev == null ? "" : prev + " | ") + note);
        application.setUpdatedAt(now);
        updateById(application);

        // 发布被拒事件（通知候选人）
        Long jobSeekerId = application.getJobSeekerId();
        Long seekerUserId = seekerApi.getJobSeekerById(jobSeekerId).getUserId();
        Long hrUserId = hrApi.getHrUserIdByHrId(hrId);

        com.SmartHire.common.event.ApplicationRejectedEvent event = new com.SmartHire.common.event.ApplicationRejectedEvent();
        event.setApplicationId(applicationId);
        event.setSeekerUserId(seekerUserId);
        event.setHrUserId(hrUserId);
        event.setReason(reason);
        event.setTemplateId(templateId);

        // 使用应用已有的 ApplicationEventProducer 发布到 MQ（添加新方法）
        applicationRejectedEventProducer.publishApplicationRejected(event);
        log.info("Application rejected and event published: applicationId={}, reason={}", applicationId, reason);
    }

    @Override
    public SeekerApplicationListDTO getSeekerApplicationList(Integer page, Integer size) {
        // 设置默认值
        if (page == null || page < 1) {
            page = 1;
        }
        if (size == null || size < 1) {
            size = 20;
        }

        Long userId = userContext.getCurrentUserId();
        Long seekerId = seekerApi.getJobSeekerIdByUserId(userId);

        // 计算偏移量
        int offset = (page - 1) * size;

        // 查询投递记录列表
        List<SeekerApplicationDTO> applicationList = applicationMapper.selectSeekerApplicationList(
                seekerId, page, size, offset);

        // 查询总记录数
        Long total = applicationMapper.countSeekerApplicationList(seekerId);

        // 构建返回结果
        SeekerApplicationListDTO result = new SeekerApplicationListDTO();
        result.setTotal(total);
        result.setPage(page);
        result.setSize(size);
        result.setList(applicationList);

        return result;
    }

    @Override
    public boolean existsBySeekerIdAndJobId(Long seekerId, Long jobId) {
        if (seekerId == null) {
            throw new BusinessException(ErrorCode.SEEKER_NOT_EXIST);
        }
        if (jobId == null) {
            throw new BusinessException(ErrorCode.JOB_NOT_EXIST);
        }
        return lambdaQuery()
                .eq(Application::getJobId, jobId)
                .eq(Application::getJobSeekerId, seekerId)
                .exists();
    }
}
