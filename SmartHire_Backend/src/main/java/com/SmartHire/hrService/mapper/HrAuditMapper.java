package com.SmartHire.hrService.mapper;

import com.SmartHire.hrService.model.HrAuditRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * HR审核记录 Mapper 接口
 *
 * @author SmartHire Team
 * @since 2025-12-13
 */
@Mapper
public interface HrAuditMapper extends BaseMapper<HrAuditRecord> {}

