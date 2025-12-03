package com.SmartHire.hrService.service.impl;

import com.SmartHire.common.api.HrApi;
import com.SmartHire.hrService.mapper.HrInfoMapper;
import com.SmartHire.hrService.mapper.JobPositionMapper;
import com.SmartHire.hrService.model.HrInfo;
import com.SmartHire.hrService.model.JobPosition;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * HR服务API实现类
 * 用于模块间通信
 */
@Service
public class HrApiImpl implements HrApi {

    @Autowired
    private HrInfoMapper hrInfoMapper;

    @Autowired
    private JobPositionMapper jobPositionMapper;

    @Override
    public Long getHrIdByUserId(Long userId) {
        if (userId == null) {
            return null;
        }
        HrInfo hrInfo = hrInfoMapper.selectOne(
                new LambdaQueryWrapper<HrInfo>()
                        .eq(HrInfo::getUserId, userId));
        return hrInfo == null ? null : hrInfo.getId();
    }

    @Override
    public boolean validateJobOwnership(Long jobId, Long hrId) {
        if (jobId == null || hrId == null) {
            return false;
        }
        JobPosition jobPosition = jobPositionMapper.selectById(jobId);
        return jobPosition != null && hrId.equals(jobPosition.getHrId());
    }

    @Override
    public Long getHrIdByJobId(Long jobId) {
        if (jobId == null) {
            return null;
        }
        JobPosition jobPosition = jobPositionMapper.selectById(jobId);
        return jobPosition == null ? null : jobPosition.getHrId();
    }
}
