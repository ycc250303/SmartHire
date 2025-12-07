package com.SmartHire.hrService.service.impl;

import com.SmartHire.hrService.dto.HrInfoCreateDTO;
import com.SmartHire.hrService.dto.HrInfoDTO;
import com.SmartHire.hrService.dto.HrInfoUpdateDTO;
import com.SmartHire.hrService.mapper.CompanyMapper;
import com.SmartHire.hrService.mapper.HrInfoMapper;
import com.SmartHire.hrService.model.Company;
import com.SmartHire.hrService.model.HrInfo;
import com.SmartHire.hrService.service.HrInfoService;
import com.SmartHire.shared.exception.enums.ErrorCode;
import com.SmartHire.shared.exception.exception.BusinessException;
import com.SmartHire.shared.utils.JwtUtil;
import com.SmartHire.shared.utils.SecurityContextUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.Map;

/**
 * HR信息服务实现类
 */
@Service
public class HrInfoServiceImpl extends ServiceImpl<HrInfoMapper, HrInfo> implements HrInfoService {

    @Autowired
    private CompanyMapper companyMapper;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取当前登录HR的信息
     * 注意：用户身份验证已由AOP切面统一处理
     */
    @Override
    public HrInfoDTO getHrInfo() {
        Map<String, Object> map = SecurityContextUtil.getCurrentClaims();
        Long userId = jwtUtil.getUserIdFromToken(map);

        // 查询HR信息（包含公司名称）
        HrInfoDTO hrInfoDTO = baseMapper.selectHrInfoWithCompanyByUserId(userId);
        if (hrInfoDTO == null) {
            throw new BusinessException(ErrorCode.HR_NOT_EXIST);
        }

        return hrInfoDTO;
    }

    /**
     * 更新当前登录HR的信息
     * 注意：用户身份验证已由AOP切面统一处理
     */
    @Override
    public void updateHrInfo(HrInfoUpdateDTO updateDTO) {
        Map<String, Object> map = SecurityContextUtil.getCurrentClaims();
        Long userId = jwtUtil.getUserIdFromToken(map);

        // 查询HR信息
        HrInfo hrInfo = lambdaQuery()
                .eq(HrInfo::getUserId, userId)
                .one();

        if (hrInfo == null) {
            throw new BusinessException(ErrorCode.HR_NOT_EXIST);
        }

        // 更新字段（只更新非空字段）
        boolean needUpdate = false;
        if (StringUtils.hasText(updateDTO.getRealName())) {
            hrInfo.setRealName(updateDTO.getRealName());
            needUpdate = true;
        }
        if (StringUtils.hasText(updateDTO.getPosition())) {
            hrInfo.setPosition(updateDTO.getPosition());
            needUpdate = true;
        }
        if (StringUtils.hasText(updateDTO.getWorkPhone())) {
            hrInfo.setWorkPhone(updateDTO.getWorkPhone());
            needUpdate = true;
        }

        if (needUpdate) {
            hrInfo.setUpdatedAt(new Date());
            updateById(hrInfo);
        }
    }

    /**
     * 注册HR信息
     * 注意：用户身份验证已由AOP切面统一处理
     */
    @Override
    @Transactional
    public Long createHrInfo(HrInfoCreateDTO createDTO) {
        Map<String, Object> map = SecurityContextUtil.getCurrentClaims();
        Long userId = jwtUtil.getUserIdFromToken(map);

        // 检查是否已经注册过HR信息
        HrInfo existingHrInfo = lambdaQuery()
                .eq(HrInfo::getUserId, userId)
                .one();
        if (existingHrInfo != null) {
            throw new BusinessException(ErrorCode.HR_ALREADY_EXIST);
        }

        // 验证公司是否存在
        Company company = companyMapper.selectById(createDTO.getCompanyId());
        if (company == null) {
            throw new BusinessException(ErrorCode.COMPANY_NOT_EXIST);
        }

        // 创建HR信息
        HrInfo hrInfo = new HrInfo();
        hrInfo.setUserId(userId);
        hrInfo.setCompanyId(createDTO.getCompanyId());
        hrInfo.setRealName(createDTO.getRealName());
        hrInfo.setPosition(createDTO.getPosition());
        hrInfo.setWorkPhone(createDTO.getWorkPhone());
        
        Date now = new Date();
        hrInfo.setCreatedAt(now);
        hrInfo.setUpdatedAt(now);

        save(hrInfo);
        return hrInfo.getId();
    }
}

