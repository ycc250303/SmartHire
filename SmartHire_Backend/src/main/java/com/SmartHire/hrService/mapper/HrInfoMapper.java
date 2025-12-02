package com.SmartHire.hrService.mapper;

import com.SmartHire.hrService.dto.HrInfoDTO;
import com.SmartHire.hrService.model.HrInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * HR信息 Mapper 接口
 */
@Mapper
public interface HrInfoMapper extends BaseMapper<HrInfo> {

    /**
     * 根据用户ID查询HR信息（包含公司名称）
     *
     * @param userId 用户ID
     * @return HR信息DTO
     */
    HrInfoDTO selectHrInfoWithCompanyByUserId(@Param("userId") Long userId);
}

