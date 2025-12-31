package com.SmartHire.recruitmentService.service;

import com.SmartHire.recruitmentService.dto.SeekerApplicationListDTO;
import com.SmartHire.recruitmentService.dto.SubmitResumeDTO;
import com.SmartHire.recruitmentService.model.Application;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 求职者侧投递服务接口
 */
public interface SeekerApplicationService extends IService<Application> {

    /**
     * 求职者投递简历
     *
     * @param request 投递请求
     * @return 投递记录ID
     */
    Long submitResume(SubmitResumeDTO request);

    /**
     * 分页查询当前求职者的投递记录
     *
     * @param page 页码
     * @param size 每页大小
     * @return 投递记录列表
     */
    SeekerApplicationListDTO getSeekerApplicationList(Integer page, Integer size);

    /**
     * 检查求职者是否已投递过该岗位
     *
     * @param seekerUserId 求职者用户ID
     * @param jobId        岗位ID
     * @return 是否已投递
     */
    boolean existsBySeekerIdAndJobId(Long seekerUserId, Long jobId);
}

