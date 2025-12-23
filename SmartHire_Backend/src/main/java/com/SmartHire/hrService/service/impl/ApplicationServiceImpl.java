package com.SmartHire.hrService.service.impl;

import com.SmartHire.recruitmentService.dto.ApplicationListDTO;
import com.SmartHire.recruitmentService.dto.ApplicationQueryDTO;
import com.SmartHire.recruitmentService.dto.SubmitResumeDTO;
import com.SmartHire.recruitmentService.service.ApplicationService;
import com.SmartHire.hrService.mapper.HrApplicationMapper;
import com.SmartHire.hrService.mapper.HrInfoMapper;
import com.SmartHire.hrService.mapper.JobInfoMapper;
import com.SmartHire.recruitmentService.model.Application;
import com.SmartHire.hrService.model.HrInfo;
import com.SmartHire.hrService.model.JobInfo;
import com.SmartHire.common.auth.UserContext;
import com.SmartHire.common.exception.enums.ErrorCode;
import com.SmartHire.common.exception.exception.BusinessException;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;

/**
 * 简历服务实现类
 */
@Service("hrApplicationServiceImpl")
public class ApplicationServiceImpl extends ServiceImpl<HrApplicationMapper, Application>
        implements ApplicationService {

    @Autowired
    private UserContext userContext;

    @Autowired
    private HrInfoMapper hrInfoMapper;

    @Autowired
    private JobInfoMapper jobInfoMapper;

    /**
     * 获取当前登录HR的ID（hr_info表的id）
     * 注意：用户身份验证已由AOP在Controller层统一处理，此处无需再次验证
     */
    private Long getCurrentHrId() {
        Long userId = userContext.getCurrentUserId();

        HrInfo hrInfo = hrInfoMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<HrInfo>()
                        .eq(HrInfo::getUserId, userId));

        if (hrInfo == null) {
            throw new BusinessException(ErrorCode.HR_NOT_EXIST);
        }

        return hrInfo.getId();
    }

    /**
     * 校验投递是否属于当前HR
     */
    private Application validateApplicationOwnership(Long applicationId, Long hrId) {
        Application application = getById(applicationId);
        if (application == null) {
            throw new BusinessException(ErrorCode.APPLICATION_NOT_EXIST);
        }

        JobInfo jobInfo = jobInfoMapper.selectById(application.getJobId());
        if (jobInfo == null) {
            throw new BusinessException(ErrorCode.JOB_NOT_EXIST);
        }

        if (!jobInfo.getHrId().equals(hrId)) {
            throw new BusinessException(ErrorCode.APPLICATION_NOT_BELONG_TO_HR);
        }

        return application;
    }

    @Override
    @Transactional
    public Long submitResume(SubmitResumeDTO request) {
        // HR服务中不支持投递简历功能，此方法应由求职者端调用
        throw new BusinessException(ErrorCode.SYSTEM_ERROR, "HR服务不支持投递简历功能，请使用求职者端接口");
    }

    @Override
    public Page<ApplicationListDTO> getApplicationList(ApplicationQueryDTO queryDTO) {
        Long hrId = getCurrentHrId();
        int pageNum = queryDTO.getPageNum() == null ? 1 : queryDTO.getPageNum();
        int pageSize = queryDTO.getPageSize() == null ? 10 : queryDTO.getPageSize();

        Page<ApplicationListDTO> page = new Page<>(pageNum, pageSize);
        String keyword = StringUtils.hasText(queryDTO.getKeyword()) ? queryDTO.getKeyword().trim() : null;

        // 将 Byte 转换为 Integer（因为 ApplicationMapper.selectApplicationList 方法期望 Integer
        // 类型）
        Integer status = queryDTO.getStatus() != null ? queryDTO.getStatus().intValue() : null;

        return baseMapper.selectApplicationList(page, hrId, queryDTO.getJobId(), status, keyword);
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
    @Transactional
    public void updateApplicationStatus(Long applicationId, Byte status) {
        if (status == null || status < 0 || status > 6) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }

        Long hrId = getCurrentHrId();
        Application application = validateApplicationOwnership(applicationId, hrId);

        Date now = new Date();
        // Application 模型的 status 是 Byte 类型，直接使用
        application.setStatus(status);
        application.setUpdatedAt(now);

        updateById(application);
    }

    @Transactional
    public void updateApplicationComment(Long applicationId, String comment) {
        // 注意：由于数据库中没有hr_comment字段，此功能暂时不可用
        // Controller层会直接返回错误，这里不需要做任何操作
    }

    @Override
    @Transactional
    public Long recommend(com.SmartHire.recruitmentService.dto.RecommendRequest request) {
        // 这个方法应该在recruitmentservice模块中实现，而不是hrservice模块
        // 暂时抛出异常，说明这个实现位置不正确
        throw new UnsupportedOperationException(
                "This method should be implemented in recruitmentService module, not hrService module");
    }

    @Override
    @Transactional
    public void rejectApplication(Long applicationId, String reason, Boolean sendNotification, String templateId) {
        // 这个方法应该在recruitmentservice模块中实现，而不是hrservice模块
        // 暂时抛出异常，说明这个实现位置不正确
        throw new UnsupportedOperationException(
                "This method should be implemented in recruitmentService module, not hrService module");
    }

    @Override
    public boolean existsBySeekerIdAndJobId(Long seekerId, Long jobId) {
        throw new BusinessException(ErrorCode.SYSTEM_ERROR, "HR服务不支持获取职位列表功能，请使用求职者端接口");
    }

    @Override
    public List<Long> getJobIdListBySeekerId() {
        throw new BusinessException(ErrorCode.SYSTEM_ERROR, "HR服务不支持获取职位列表功能，请使用求职者端接口");
    }
}
