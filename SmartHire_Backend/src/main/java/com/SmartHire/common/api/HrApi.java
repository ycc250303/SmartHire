package com.SmartHire.common.api;

/**
 * HR服务API接口
 * 用于模块间通信，避免直接访问数据库
 */
public interface HrApi {

    /**
     * 根据用户ID获取HR ID
     * 
     * @param userId 用户ID
     * @return HR ID，如果用户不是HR或HR不存在返回null
     */
    Long getHrIdByUserId(Long userId);

    /**
     * 验证岗位是否属于指定HR
     * 
     * @param jobId 岗位ID
     * @param hrId  HR ID
     * @return 是否属于
     */
    boolean validateJobOwnership(Long jobId, Long hrId);

    /**
     * 根据岗位ID获取HR ID
     * 
     * @param jobId 岗位ID
     * @return HR ID，如果岗位不存在返回null
     */
    Long getHrIdByJobId(Long jobId);
}
