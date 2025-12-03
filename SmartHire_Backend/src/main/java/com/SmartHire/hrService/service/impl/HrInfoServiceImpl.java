package com.SmartHire.hrService.service.impl;

import com.SmartHire.common.api.UserAuthApi;
import com.SmartHire.common.auth.UserContext;
import com.SmartHire.common.exception.enums.ErrorCode;
import com.SmartHire.common.exception.exception.BusinessException;
import com.SmartHire.hrService.dto.HrInfoDTO;
import com.SmartHire.hrService.dto.HrInfoUpdateDTO;
import com.SmartHire.hrService.mapper.HrInfoMapper;
import com.SmartHire.hrService.model.HrInfo;
import com.SmartHire.hrService.service.HrInfoService;
import com.SmartHire.userAuthService.model.User;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/** HR信息服务实现类 */
@Service
public class HrInfoServiceImpl extends ServiceImpl<HrInfoMapper, HrInfo> implements HrInfoService {

  @Autowired private UserAuthApi userAuthApi;

  @Autowired private UserContext userContext;

  /** 获取当前登录用户ID并校验HR身份 */
  private Long validateAndGetCurrentHrUserId() {
    Long userId = userContext.getCurrentUserId();
    // 验证用户是否存在和身份
    User user = userAuthApi.getUserById(userId);
    if (user.getUserType() != 2) {
      throw new BusinessException(ErrorCode.USER_NOT_HR);
    }
    return userId;
  }

  /** 获取当前登录HR的信息 */
  @Override
  public HrInfoDTO getHrInfo() {
    Long userId = validateAndGetCurrentHrUserId();

    // 查询HR信息（包含公司名称）
    HrInfoDTO hrInfoDTO = baseMapper.selectHrInfoWithCompanyByUserId(userId);
    if (hrInfoDTO == null) {
      throw new BusinessException(ErrorCode.HR_NOT_EXIST);
    }

    return hrInfoDTO;
  }

  /** 新增当前登录HR的信息（若已存在则进行更新） */
  @Override
  public void createHrInfo(HrInfoUpdateDTO updateDTO) {
    Long userId = validateAndGetCurrentHrUserId();

    HrInfo hrInfo = lambdaQuery().eq(HrInfo::getUserId, userId).one();

    Date now = new Date();
    if (hrInfo == null) {
      // 新建HR信息
      hrInfo = new HrInfo();
      hrInfo.setUserId(userId);
      if (StringUtils.hasText(updateDTO.getRealName())) {
        hrInfo.setRealName(updateDTO.getRealName());
      }
      if (StringUtils.hasText(updateDTO.getPosition())) {
        hrInfo.setPosition(updateDTO.getPosition());
      }
      if (StringUtils.hasText(updateDTO.getWorkPhone())) {
        hrInfo.setWorkPhone(updateDTO.getWorkPhone());
      }
      hrInfo.setCreatedAt(now);
      hrInfo.setUpdatedAt(now);
      // TODO: HR信息关联公司
      hrInfo.setCompanyId(1L);
      save(hrInfo);
    } else {
      // 已存在则走更新逻辑
      updateHrInfo(updateDTO);
    }
  }

  /** 更新当前登录HR的信息 */
  @Override
  public void updateHrInfo(HrInfoUpdateDTO updateDTO) {
    Long userId = validateAndGetCurrentHrUserId();

    // 查询HR信息
    HrInfo hrInfo = lambdaQuery().eq(HrInfo::getUserId, userId).one();

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
