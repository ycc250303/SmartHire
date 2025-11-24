package com.SmartHire.seekerService.service;

import com.SmartHire.seekerService.dto.JobSeekerExpectationDTO;
import com.SmartHire.seekerService.model.JobSeekerExpectation;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.validation.Valid;

import java.util.List;

/**
 * <p>
 * 求职者期望表 服务类
 * </p>
 *
 * @author SmartHire Team
 * @since 2025-11-19
 */
public interface JobSeekerExpectationService extends IService<JobSeekerExpectation> {

    /**
     * 添加求职期望
     *
     * @param request 添加求职期望请求参数
     */
    void addJobSeekerExpectation(@Valid JobSeekerExpectationDTO request);

    /**
     * 修改求职期望
     *
     * @param id      求职期望ID
     * @param request 修改求职期望请求参数（部分字段，至少一项不为空）
     */
    void updateJobSeekerExpectation(Long id, JobSeekerExpectationDTO request);

    /**
     * 获取当前用户的所有求职期望
     *
     * @return 求职期望列表
     */
    List<JobSeekerExpectation> getJobSeekerExpectations();

    /**
     * 删除求职期望
     *
     * @param id 求职期望ID
     */
    void deleteJobSeekerExpectation(Long id);
}
