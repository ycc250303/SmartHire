package com.SmartHire.recruitmentService.service;

import com.SmartHire.common.dto.hrDto.JobCardDTO;
import com.SmartHire.recruitmentService.dto.FullTimeJobRecommendationsDTO;
import com.SmartHire.recruitmentService.dto.InternJobRecommendationsDTO;
import com.SmartHire.recruitmentService.dto.InterviewResponseDTO;
import com.SmartHire.recruitmentService.dto.SeekerApplicationListDTO;
import com.SmartHire.recruitmentService.dto.SeekerJobPositionDTO;
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

    /**
     * 获取岗位卡片信息
     *
     * @param jobId 岗位ID
     * @return 岗位卡片
     */
    JobCardDTO getJobCard(Long jobId);

    /**
     * 获取实习岗位推荐
     *
     * @return 实习岗位推荐DTO
     */
    InternJobRecommendationsDTO getInternJobRecommendations();

    /**
     * 获取全职岗位推荐
     *
     * @return 全职岗位推荐DTO
     */
    FullTimeJobRecommendationsDTO getFullTimeJobRecommendations();
    
    /**
     * 获取最新的实习岗位列表（按时间排序）
     *
     * @param page 页码
     * @param size 每页大小
     * @return 实习岗位列表
     */
    InternJobRecommendationsDTO getInternJobsLatest(Integer page, Integer size);

    /**
     * 获取最新的全职岗位列表（按时间排序）
     *
     * @param page 页码
     * @param size 每页大小
     * @return 全职岗位列表
     */
    FullTimeJobRecommendationsDTO getFullTimeJobsLatest(Integer page, Integer size);

    /**
     * 获取面向求职者的岗位详情
     *
     * @param jobId 岗位ID
     * @return 岗位详情DTO
     */
    SeekerJobPositionDTO getJobPosition(Long jobId);

    /**
     * 响应面试邀请
     *
     * @param request 响应请求
     */
    void respondToInterview(InterviewResponseDTO request);
}
