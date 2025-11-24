package com.SmartHire.hrService.service.impl;

import com.SmartHire.hrService.model.JobPosition;
import com.SmartHire.hrService.mapper.JobPositionMapper;
import com.SmartHire.hrService.service.JobPositionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 岗位服务实现类
 */
@Service
public class JobPositionServiceImpl extends ServiceImpl<JobPositionMapper, JobPosition> implements JobPositionService {

}

