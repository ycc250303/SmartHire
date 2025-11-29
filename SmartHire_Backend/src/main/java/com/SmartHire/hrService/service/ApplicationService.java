package com.SmartHire.hrService.service;

import com.SmartHire.hrService.dto.ApplicationListDTO;
import com.SmartHire.hrService.dto.ApplicationQueryDTO;
import com.SmartHire.hrService.model.Application;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 简历服务接口
 */
public interface ApplicationService extends IService<Application> {

    /**
     * 查询当前HR的投递列表
     *
     * @param queryDTO 查询参数
     * @return 分页结果
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
     * @param status        新状态
     */
    void updateApplicationStatus(Long applicationId, Integer status);

    /**
     * 更新HR备注
     *
     * @param applicationId 投递ID
     * @param comment       备注
     */
    void updateApplicationComment(Long applicationId, String comment);
}

