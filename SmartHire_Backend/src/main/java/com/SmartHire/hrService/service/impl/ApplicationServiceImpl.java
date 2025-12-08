package com.SmartHire.hrService.service.impl;

import com.SmartHire.hrService.dto.ApplicationListDTO;
import com.SmartHire.hrService.dto.ApplicationQueryDTO;
import com.SmartHire.hrService.mapper.ApplicationMapper;
import com.SmartHire.hrService.mapper.HrInfoMapper;
import com.SmartHire.hrService.mapper.JobPositionMapper;
import com.SmartHire.hrService.model.Application;
import com.SmartHire.hrService.model.HrInfo;
import com.SmartHire.hrService.model.JobPosition;
import com.SmartHire.hrService.service.ApplicationService;
import com.SmartHire.common.api.UserAuthApi;
import com.SmartHire.common.auth.UserContext;
import com.SmartHire.common.exception.enums.ErrorCode;
import com.SmartHire.common.exception.exception.BusinessException;
import com.SmartHire.userAuthService.model.User;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;

/**
 * 简历服务实现类
 */
@Service("hrApplicationServiceImpl")
public class ApplicationServiceImpl extends ServiceImpl<ApplicationMapper, Application> implements ApplicationService {

    @Autowired
    private UserAuthApi userAuthApi;

    @Autowired
    private UserContext userContext;

    @Autowired
    private HrInfoMapper hrInfoMapper;

    @Autowired
    private JobPositionMapper jobPositionMapper;

    /**
     * 获取当前登录HR的ID（hr_info表的id）
     */
    private Long getCurrentHrId() {
        Long userId = userContext.getCurrentUserId();
        User user = userAuthApi.getUserById(userId);
        if (user.getUserType() != 2) {
            throw new BusinessException(ErrorCode.USER_NOT_HR);
        }

        HrInfo hrInfo = hrInfoMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<HrInfo>()
                        .eq(HrInfo::getUserId, userId)
        );

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

        JobPosition jobPosition = jobPositionMapper.selectById(application.getJobId());
        if (jobPosition == null) {
            throw new BusinessException(ErrorCode.JOB_NOT_EXIST);
        }

        if (!jobPosition.getHrId().equals(hrId)) {
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

        return baseMapper.selectApplicationList(page, hrId, queryDTO.getJobId(), queryDTO.getStatus(), keyword);
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
    public void updateApplicationStatus(Long applicationId, Integer status) {
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
    @Transactional
    public void updateApplicationComment(Long applicationId, String comment) {
        // 注意：由于数据库中没有hr_comment字段，此功能暂时不可用
        // Controller层会直接返回错误，这里不需要做任何操作
    }
}

