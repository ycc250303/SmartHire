package com.SmartHire.hrService.service;

import com.SmartHire.hrService.dto.ApplicationListDTO;

import java.util.List;

/**
 * 匹配服务接口
 */
public interface MatchingService {

    /**
     * 对指定岗位的投递执行关键词匹配并返回排序结果
     *
     * @param jobId 岗位ID
     * @return 匹配结果列表（已按匹配度排序）
     */
    List<ApplicationListDTO> matchApplicationsForJob(Long jobId);
}

