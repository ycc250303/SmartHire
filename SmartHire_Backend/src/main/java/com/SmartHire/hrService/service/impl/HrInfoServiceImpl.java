package com.SmartHire.hrService.service.impl;

import com.SmartHire.hrService.dto.HrInfoDTO;
import com.SmartHire.hrService.dto.HrInfoUpdateDTO;
import com.SmartHire.hrService.mapper.HrInfoMapper;
import com.SmartHire.hrService.model.HrInfo;
import com.SmartHire.hrService.service.HrInfoService;
import com.SmartHire.shared.exception.enums.ErrorCode;
import com.SmartHire.shared.exception.exception.BusinessException;
import com.SmartHire.shared.utils.JwtUtil;
import com.SmartHire.shared.utils.ThreadLocalUtil;
import com.SmartHire.userAuthService.mapper.UserAuthMapper;
import com.SmartHire.userAuthService.model.User;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.Map;

/**
 * HR信息服务实现类
 */
@Service
public class HrInfoServiceImpl extends ServiceImpl<HrInfoMapper, HrInfo> implements HrInfoService {

    @Autowired
    private UserAuthMapper userAuthMapper;

    /**
     * 获取当前登录HR的信息
     */
    @Override
    public HrInfoDTO getHrInfo() {
        Map<String, Object> map = ThreadLocalUtil.get();
        Long userId = JwtUtil.getUserIdFromToken(map);

        // 验证用户是否存在
        User user = userAuthMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_ID_NOT_EXIST);
        }

        // 验证用户身份是否为HR
        if (user.getUserType() != 2) {
            throw new BusinessException(ErrorCode.USER_NOT_HR);
        }

        // 查询HR信息（包含公司名称）
        HrInfoDTO hrInfoDTO = baseMapper.selectHrInfoWithCompanyByUserId(userId);
        if (hrInfoDTO == null) {
            throw new BusinessException(ErrorCode.HR_NOT_EXIST);
        }

        return hrInfoDTO;
    }

    /**
     * 更新当前登录HR的信息
     */
    @Override
    public void updateHrInfo(HrInfoUpdateDTO updateDTO) {
        Map<String, Object> map = ThreadLocalUtil.get();
        Long userId = JwtUtil.getUserIdFromToken(map);

        // 验证用户是否存在
        User user = userAuthMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_ID_NOT_EXIST);
        }

        // 验证用户身份是否为HR
        if (user.getUserType() != 2) {
            throw new BusinessException(ErrorCode.USER_NOT_HR);
        }

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
}

