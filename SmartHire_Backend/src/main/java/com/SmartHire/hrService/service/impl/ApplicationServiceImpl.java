package com.SmartHire.hrService.service.impl;

import com.SmartHire.hrService.model.Application;
import com.SmartHire.hrService.mapper.ApplicationMapper;
import com.SmartHire.hrService.service.ApplicationService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 简历服务实现类
 */
@Service
public class ApplicationServiceImpl extends ServiceImpl<ApplicationMapper, Application> implements ApplicationService {

}

