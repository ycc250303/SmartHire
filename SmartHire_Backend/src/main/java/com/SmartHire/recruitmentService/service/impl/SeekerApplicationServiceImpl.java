package com.SmartHire.recruitmentService.service.impl;

import com.SmartHire.common.api.HrApi;
import com.SmartHire.common.api.MessageApi;
import com.SmartHire.common.api.SeekerApi;
import com.SmartHire.common.api.UserAuthApi;
import com.SmartHire.common.auth.UserContext;
import com.SmartHire.common.dto.hrDto.CompanyDTO;
import com.SmartHire.common.dto.hrDto.HrInfoDTO;
import com.SmartHire.common.dto.hrDto.JobCardDTO;
import com.SmartHire.common.dto.hrDto.JobFullDetailDTO;
import com.SmartHire.common.dto.hrDto.JobInfoDTO;
import com.SmartHire.common.dto.hrDto.JobSearchDTO;
import com.SmartHire.common.dto.seekerDto.SeekerCardDTO;
import com.SmartHire.common.dto.seekerDto.SeekerCommonDTO;
import com.SmartHire.common.dto.userDto.UserCommonDTO;
import com.SmartHire.common.event.ApplicationCreatedEvent;
import com.SmartHire.common.exception.enums.ErrorCode;
import com.SmartHire.common.exception.exception.BusinessException;
import com.SmartHire.recruitmentService.dto.FullTimeJobItemDTO;
import com.SmartHire.recruitmentService.dto.FullTimeJobRecommendationsDTO;
import com.SmartHire.recruitmentService.dto.InternJobItemDTO;
import com.SmartHire.recruitmentService.dto.InternJobRecommendationsDTO;
import com.SmartHire.recruitmentService.dto.SeekerApplicationDTO;
import com.SmartHire.recruitmentService.dto.SeekerApplicationListDTO;
import com.SmartHire.recruitmentService.dto.SeekerJobPositionDTO;
import com.SmartHire.recruitmentService.dto.SubmitResumeDTO;
import com.SmartHire.recruitmentService.mapper.ApplicationMapper;
import com.SmartHire.recruitmentService.model.Application;
import com.SmartHire.recruitmentService.service.ApplicationEventProducer;
import com.SmartHire.recruitmentService.service.SeekerApplicationService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

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
    private UserAuthApi userAuthApi;

    @Autowired
    private MessageApi messageApi;

    @Autowired
    private UserContext userContext;

    @Autowired
    private ApplicationMapper applicationMapper;

    @Autowired
    private ApplicationEventProducer applicationEventProducer;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long submitResume(SubmitResumeDTO request) {
        // ... (existing code same)
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
        // ... (existing code same)
        if (page == null || page < 1)
            page = 1;
        if (size == null || size < 1)
            size = 20;

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
        if (seekerUserId == null)
            throw new BusinessException(ErrorCode.SEEKER_NOT_EXIST);
        if (jobId == null)
            throw new BusinessException(ErrorCode.JOB_NOT_EXIST);

        Long seekerId = seekerApi.getJobSeekerIdByUserId(seekerUserId);
        return lambdaQuery()
                .eq(Application::getJobId, jobId)
                .eq(Application::getJobSeekerId, seekerId)
                .exists();
    }

    @Override
    public JobCardDTO getJobCard(Long jobId) {
        JobCardDTO jobCard = hrApi.getJobCardByJobId(jobId);
        if (jobCard == null) {
            throw new BusinessException(ErrorCode.JOB_NOT_EXIST);
        }
        return jobCard;
    }

    /**
     * 岗位卡片和匹配分数的包装类
     */
    private static class JobCardWithScore {
        private final JobCardDTO jobCard;
        private final Integer matchScore;

        public JobCardWithScore(JobCardDTO jobCard, Integer matchScore) {
            this.jobCard = jobCard;
            this.matchScore = matchScore;
        }

        public JobCardDTO getJobCard() {
            return jobCard;
        }

        public Integer getMatchScore() {
            return matchScore;
        }
    }

    /**
     * 获取岗位推荐并计算匹配分数的公共逻辑
     *
     * @param jobType 岗位类型：0-全职，1-实习
     * @return 带匹配分数的岗位卡片列表，按匹配分数降序排序
     */
    private List<JobCardWithScore> getJobRecommendationsWithScore(Integer jobType) {
        JobSearchDTO searchDTO = new JobSearchDTO();
        searchDTO.setJobType(jobType);
        searchDTO.setPage(1);
        searchDTO.setSize(20);

        List<JobCardDTO> jobCards = hrApi.searchPublicJobs(searchDTO);

        Long targetUserId = userContext.getCurrentUserId();
        Long jobSeekerId = seekerApi.getJobSeekerIdByUserId(targetUserId);
        SeekerCommonDTO seeker = null;
        SeekerCardDTO seekerCard = null;

        if (jobSeekerId != null) {
            seeker = seekerApi.getJobSeekerById(jobSeekerId);
            seekerCard = seekerApi.getSeekerCard(targetUserId);
        }

        StringBuilder kw = new StringBuilder();
        if (seekerCard != null) {
            if (seekerCard.getMajor() != null)
                kw.append(seekerCard.getMajor()).append(" ");
            if (seekerCard.getHighestEducation() != null)
                kw.append(seekerCard.getHighestEducation()).append(" ");
            if (seekerCard.getUniversity() != null)
                kw.append(seekerCard.getUniversity()).append(" ");
        }
        String keyword = kw.toString().trim();
        String lowerKeyword = keyword.toLowerCase(Locale.ROOT);

        List<JobCardWithScore> jobCardsWithScore = new ArrayList<>();
        for (JobCardDTO jc : jobCards) {
            int score = calculateMatchScore(jc, seeker, lowerKeyword);
            jobCardsWithScore.add(new JobCardWithScore(jc, score));
        }

        jobCardsWithScore.sort(
                Comparator.comparing(JobCardWithScore::getMatchScore, Comparator.nullsLast(Comparator.naturalOrder()))
                        .reversed());

        return jobCardsWithScore;
    }

    /**
     * 计算岗位匹配分数
     *
     * @param jobCard      岗位卡片
     * @param seeker       求职者信息
     * @param lowerKeyword 小写关键词
     * @return 匹配分数（0-100）
     */
    private int calculateMatchScore(JobCardDTO jobCard, SeekerCommonDTO seeker, String lowerKeyword) {
        int score = 50;
        String combined = (Objects.toString(jobCard.getJobTitle(), "") + " "
                + Objects.toString(jobCard.getCompanyName(), "")
                + " "
                + Objects.toString(jobCard.getCity(), "")).toLowerCase(Locale.ROOT);
        if (!lowerKeyword.isEmpty()) {
            if (combined.contains(lowerKeyword)) {
                score += 30;
            } else {
                String[] toks = lowerKeyword.split("\\s+");
                for (String t : toks) {
                    if (!t.isEmpty() && combined.contains(t)) {
                        score += 10;
                    }
                }
            }
        }
        if (seeker != null && seeker.getEducation() != null && jobCard.getEducationRequired() != null
                && seeker.getEducation().equals(jobCard.getEducationRequired())) {
            score += 10;
        }
        if (seeker != null && seeker.getCurrentCity() != null
                && seeker.getCurrentCity().equalsIgnoreCase(jobCard.getCity())) {
            score += 5;
        }
        if (score > 100)
            score = 100;
        if (score < 0)
            score = 0;
        return score;
    }

    @Override
    public InternJobRecommendationsDTO getInternJobRecommendations() {
        List<JobCardWithScore> jobCardsWithScore = getJobRecommendationsWithScore(1); // 1 == internship

        List<InternJobItemDTO> items = new ArrayList<>();
        for (JobCardWithScore jcws : jobCardsWithScore) {
            JobCardDTO jc = jcws.getJobCard();
            InternJobItemDTO item = new InternJobItemDTO();
            item.setJobId(jc.getJobId());
            item.setTitle(jc.getJobTitle());
            item.setCompanyName(jc.getCompanyName());
            item.setCity(jc.getCity());
            item.setAddress(jc.getAddress());
            item.setSalary_min(jc.getSalaryMin() == null ? 0 : jc.getSalaryMin().intValue());
            item.setSalary_max(jc.getSalaryMax() == null ? 0 : jc.getSalaryMax().intValue());
            item.setSkills(jc.getSkills());
            item.setMatchScore(jcws.getMatchScore());
            items.add(item);
        }

        InternJobRecommendationsDTO resp = new InternJobRecommendationsDTO();
        resp.setJobs(items);
        return resp;
    }

    @Override
    public FullTimeJobRecommendationsDTO getFullTimeJobRecommendations() {
        List<JobCardWithScore> jobCardsWithScore = getJobRecommendationsWithScore(0); // 0 == full-time

        List<FullTimeJobItemDTO> items = new ArrayList<>();
        for (JobCardWithScore jcws : jobCardsWithScore) {
            JobCardDTO jc = jcws.getJobCard();
            FullTimeJobItemDTO item = new FullTimeJobItemDTO();
            item.setJobId(jc.getJobId());
            item.setTitle(jc.getJobTitle());
            item.setCompanyName(jc.getCompanyName());
            item.setCity(jc.getCity());
            item.setAddress(jc.getAddress());
            item.setSalary_min(jc.getSalaryMin() == null ? 0 : jc.getSalaryMin().intValue());
            item.setSalary_max(jc.getSalaryMax() == null ? 0 : jc.getSalaryMax().intValue());
            item.setSkills(jc.getSkills());
            item.setExperienceRequired(jc.getExperienceRequired());
            item.setMatchScore(jcws.getMatchScore());
            items.add(item);
        }

        FullTimeJobRecommendationsDTO resp = new FullTimeJobRecommendationsDTO();
        resp.setJobs(items);
        return resp;
    }

    @Override
    public SeekerJobPositionDTO getJobPosition(Long jobId) {
        JobFullDetailDTO fullDetail = hrApi.getJobFullDetail(jobId);
        if (fullDetail == null) {
            throw new BusinessException(ErrorCode.JOB_NOT_EXIST);
        }

        JobInfoDTO jobModel = fullDetail.getJobInfo();
        CompanyDTO company = fullDetail.getCompany();
        HrInfoDTO hrInfo = fullDetail.getHrInfo();

        UserCommonDTO hrUser = null;
        if (hrInfo != null) {
            hrUser = userAuthApi.getUserById(hrInfo.getUserId());
        }

        JobInfoDTO jobDto = hrApi.getJobInfoWithSkills(jobId);

        SeekerJobPositionDTO resp = new SeekerJobPositionDTO();
        resp.setJobId(jobModel.getId());
        resp.setJobTitle(jobModel.getJobTitle());
        resp.setJobCategory(jobModel.getJobCategory());
        resp.setDepartment(jobModel.getDepartment());
        resp.setCity(jobModel.getCity());
        resp.setAddress(jobModel.getAddress());
        resp.setSalaryMin(jobModel.getSalaryMin() == null ? 0 : jobModel.getSalaryMin().intValue());
        resp.setSalaryMax(jobModel.getSalaryMax() == null ? 0 : jobModel.getSalaryMax().intValue());
        resp.setSalaryMonths(jobModel.getSalaryMonths());
        resp.setEducationRequired(jobModel.getEducationRequired());
        resp.setJobType(jobModel.getJobType());
        resp.setExperienceRequired(jobModel.getExperienceRequired());
        resp.setDescription(jobModel.getDescription());
        resp.setResponsibilities(jobModel.getResponsibilities());
        resp.setRequirements(jobModel.getRequirements());
        resp.setSkills(jobDto != null ? jobDto.getSkills() : null);
        resp.setViewCount(jobModel.getViewCount());
        resp.setApplicationCount(jobModel.getApplicationCount());
        resp.setPublishedAt(jobModel.getPublishedAt());

        if (company != null) {
            SeekerJobPositionDTO.CompanyDTO c = new SeekerJobPositionDTO.CompanyDTO();
            c.setCompanyId(company.getId());
            c.setCompanyName(company.getCompanyName());
            c.setCompanyLogo(company.getLogoUrl());
            c.setCompanyScale(company.getCompanyScale());
            c.setFinancingStage(company.getFinancingStage());
            c.setIndustry(company.getIndustry());
            c.setDescription(company.getDescription());
            c.setWebsite(company.getWebsite());
            resp.setCompany(c);
        }

        if (hrInfo != null) {
            SeekerJobPositionDTO.HrDTO h = new SeekerJobPositionDTO.HrDTO();
            h.setHrUserId(hrInfo.getUserId());
            h.setRealName(hrInfo.getRealName());
            h.setPosition(hrInfo.getPosition());
            h.setAvatarUrl(hrUser == null ? null : hrUser.getAvatarUrl());
            resp.setHr(h);
        }

        Long currentUserId = userContext.getCurrentUserId();
        Long seekerId = null;
        if (currentUserId != null) {
            seekerId = seekerApi.getJobSeekerIdByUserId(currentUserId);
        }
        SeekerJobPositionDTO.ApplicationDTO appDto = new SeekerJobPositionDTO.ApplicationDTO();
        if (seekerId != null) {
            Application application = applicationMapper.selectOne(
                    new LambdaQueryWrapper<Application>()
                            .eq(Application::getJobId, jobId)
                            .eq(Application::getJobSeekerId, seekerId));
            if (application != null) {
                appDto.setHasApplied(true);
                appDto.setApplicationId(application.getId());
                appDto.setStatus(application.getStatus() == null ? null : application.getStatus().intValue());
                appDto.setAppliedAt(application.getCreatedAt());

                // 解耦：通过 messageApi 检查会话状态，不再直接访问 chatMessageMapper
                if (application.getConversationId() != null
                        && messageApi.hasMessages(application.getConversationId())) {
                    appDto.setConversationId(application.getConversationId());
                }
            } else {
                appDto.setHasApplied(false);
            }
        } else {
            appDto.setHasApplied(false);
        }
        resp.setApplication(appDto);

        return resp;
    }
}
