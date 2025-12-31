package com.SmartHire.recruitmentService.service;

import com.SmartHire.recruitmentService.dto.ApplicationListDTO;
import com.SmartHire.recruitmentService.dto.ApplicationQueryDTO;
import com.SmartHire.recruitmentService.dto.RecommendRequest;
import com.SmartHire.recruitmentService.model.Application;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * HR侧招聘投递管理服务接口
 */
public interface HrApplicationService extends IService<Application> {

    /**
     * HR推荐岗位给求职者
     *
     * @param request 推荐请求
     * @return 推荐记录ID
     */
    Long recommend(RecommendRequest request);

    /**
     * 分页查询HR可见的投递列表
     *
     * @param queryDTO 查询条件
     * @return 投递列表分页数据
     */
    Page<ApplicationListDTO> getApplicationList(ApplicationQueryDTO queryDTO);

    /**
     * 获取投递详情
     *
     * @param applicationId 投递ID
     * @return 投递详情
     */
    ApplicationListDTO getApplicationDetail(Long applicationId);

    /**
     * 更新投递状态
     *
     * @param applicationId 投递ID
     * @param status        目标状态
     */
    void updateApplicationStatus(Long applicationId, Byte status);

    /**
     * 拒绝投递
     *
     * @param applicationId    投递ID
     * @param reason           拒绝原因
     * @param sendNotification 是否发送通知
     * @param templateId       通知模板ID
     */
    void rejectApplication(Long applicationId, String reason, Boolean sendNotification, String templateId);
}

