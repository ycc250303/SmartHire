package com.SmartHire.seekerService.service;

import java.util.Map;

/**
 * 在线简历聚合查询服务。
 */
public interface OnlineResumeService {
    /**
     * 根据用户ID聚合查询在线简历信息。
     *
     * @param userId 用户ID
     * @return 在线简历完整数据
     */
    Map<String, Object> getOnlineResumeByUserId(Long userId);
}



