package com.SmartHire.seekerService.service;

import com.SmartHire.seekerService.dto.RegisterSeekerDTO;
import com.SmartHire.seekerService.model.JobSeeker;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 求职者信息表 服务类
 * </p>
 *
 * @author SmartHire Team
 * @since 2025-11-19
 */
public interface JobSeekerService extends IService<JobSeeker> {
    /**
     * 注册求职者
     *
     * @param registerSeekerDTO 注册求职者信息
     */
    void registerSeeker(RegisterSeekerDTO registerSeekerDTO);

    /**
     * 获取求职者信息
     *
     * @return 求职者信息
     */
    JobSeeker getSeekerInfo();

    /**
     * 获取求职者ID
     *
     * @return 求职者ID
     */
    Long getJobSeekerId();
}
