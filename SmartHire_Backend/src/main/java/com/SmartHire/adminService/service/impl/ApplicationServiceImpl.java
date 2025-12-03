package com.SmartHire.adminService.service.impl;

import com.SmartHire.adminService.model.Application;
import com.SmartHire.adminService.mapper.ApplicationMapper;
import com.SmartHire.adminService.service.ApplicationService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 投递/推荐记录表 服务实现类
 * </p>
 *
 * @author SmartHire Team
 * @since 2025-12-02
 */
@Service
public class ApplicationServiceImpl extends ServiceImpl<ApplicationMapper, Application> implements ApplicationService {

}
