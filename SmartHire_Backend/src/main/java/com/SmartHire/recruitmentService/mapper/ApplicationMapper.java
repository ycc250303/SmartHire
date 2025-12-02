package com.SmartHire.recruitmentService.mapper;

import com.SmartHire.recruitmentService.model.Application;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 投递/推荐记录表 Mapper 接口
 *
 * @author SmartHire Team
 * @since 2025-11-30
 */
@Mapper
public interface ApplicationMapper extends BaseMapper<Application> {}
