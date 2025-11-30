package com.SmartHire.recruitmentService.service;

import com.SmartHire.recruitmentService.dto.SubmitResumeDTO;
import com.SmartHire.recruitmentService.model.Application;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 投递/推荐记录表 服务类
 * </p>
 *
 * @author SmartHire Team
 * @since 2025-11-30
 */
public interface ApplicationService extends IService<Application> {

    /**
     * 投递简历
     *
     * @param request 投递简历请求
     */
    void submitResume(SubmitResumeDTO request);
}
