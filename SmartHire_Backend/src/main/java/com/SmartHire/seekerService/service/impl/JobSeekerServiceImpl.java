package com.SmartHire.seekerService.service.impl;

import com.SmartHire.seekerService.dto.RegisterSeekerDTO;
import com.SmartHire.seekerService.model.JobSeeker;
import com.SmartHire.seekerService.mapper.JobSeekerMapper;
import com.SmartHire.seekerService.service.JobSeekerService;
import com.SmartHire.shared.exception.enums.ErrorCode;
import com.SmartHire.shared.exception.exception.BusinessException;
import com.SmartHire.shared.utils.JwtUtil;
import com.SmartHire.shared.utils.SecurityContextUtil;
import com.SmartHire.userAuthService.mapper.UserAuthMapper;
import com.SmartHire.userAuthService.model.User;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

/**
 * <p>
 * 求职者信息表 服务实现类
 * </p>
 *
 * @author SmartHire Team
 * @since 2025-11-19
 */
@Slf4j
@Service
public class JobSeekerServiceImpl extends ServiceImpl<JobSeekerMapper, JobSeeker> implements JobSeekerService {
    @Autowired
    private JobSeekerMapper jobSeekerMapper;

    @Autowired
    private UserAuthMapper userAuthMapper;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 注册求职者
     *
     * @param request 注册求职者信息
     */
    @Override
    public void registerSeeker(RegisterSeekerDTO request) {
        Map<String, Object> map = SecurityContextUtil.getCurrentClaims();
        Long userId = jwtUtil.getUserIdFromToken(map);
        log.info("用户ID：{}", userId);
        // 验证用户是否存在
        User user = userAuthMapper.findById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_ID_NOT_EXIST);
        }

        // 检查该用户是否已经注册过求职者信息
        JobSeeker existingSeeker = lambdaQuery()
                .eq(JobSeeker::getUserId, userId)
                .one();
        if (existingSeeker != null) {
            throw new BusinessException(ErrorCode.SEEKER_ALREADY_REGISTERED);
        }

        JobSeeker jobSeeker = new JobSeeker();
        jobSeeker.setUserId(userId);
        jobSeeker.setRealName(request.getRealName());
        jobSeeker.setBirthDate(request.getBirthDate());
        jobSeeker.setCurrentCity(request.getCurrentCity());
        jobSeeker.setWorkYears(request.getWorkYears());
        jobSeeker.setEducation(request.getEducation());
        jobSeeker.setJobStatus(0);

        jobSeeker.setCreatedAt(new Date());
        jobSeeker.setUpdatedAt(new Date());

        jobSeekerMapper.insert(jobSeeker);
    }

    /**
     * 获取求职者信息
     *
     * @return 求职者信息
     */
    @Override
    public JobSeeker getSeekerInfo() {
        Map<String, Object> map = SecurityContextUtil.getCurrentClaims();
        Long userId = jwtUtil.getUserIdFromToken(map);

        // 验证用户是否存在
        User user = userAuthMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_ID_NOT_EXIST);
        }

        // 验证用户身份是否为求职者
        if (user.getUserType() != 1) {
            throw new BusinessException(ErrorCode.USER_NOT_SEEKER);
        }

        // 通过用户ID获取（job_seeker表有user_id字段，且是唯一索引）
        JobSeeker jobSeeker = lambdaQuery()
                .eq(JobSeeker::getUserId, userId)
                .one();

        if (jobSeeker == null) {
            throw new BusinessException(ErrorCode.SEEKER_NOT_EXIST);
        }

        return jobSeeker;
    }

    /**
     * 获取求职者ID
     *
     * @return 求职者ID
     */
    @Override
    public Long getJobSeekerId() {
        Map<String, Object> map = SecurityContextUtil.getCurrentClaims();
        Long userId = jwtUtil.getUserIdFromToken(map);
        if (userId == null) {
            throw new BusinessException(ErrorCode.USER_ID_NOT_EXIST);
        }
        JobSeeker jobSeeker = lambdaQuery()
                .eq(JobSeeker::getUserId, userId)
                .one();

        if (jobSeeker == null) {
            throw new BusinessException(ErrorCode.SEEKER_NOT_EXIST);
        }

        return jobSeeker.getId();
    }
}
