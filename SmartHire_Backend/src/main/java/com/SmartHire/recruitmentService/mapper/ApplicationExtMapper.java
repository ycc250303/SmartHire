package com.SmartHire.recruitmentService.mapper;

import org.apache.ibatis.annotations.Mapper;
import java.util.List;

/**
 * Application扩展Mapper接口
 * 用于自定义SQL查询
 *
 * @author SmartHire Team
 */
@Mapper
public interface ApplicationExtMapper {
    
    /**
     * 根据用户ID查找相关的Application记录ID
     * 通过关联user表、hr_info表、job_seeker表和job_info表
     * 找到与这两个用户相关且没有conversation_id的Application记录
     * 
     * @param user1Id 用户1ID
     * @param user2Id 用户2ID
     * @return Application记录ID列表
     */
    List<Long> findApplicationIdsByUserIds(Long user1Id, Long user2Id);
}
