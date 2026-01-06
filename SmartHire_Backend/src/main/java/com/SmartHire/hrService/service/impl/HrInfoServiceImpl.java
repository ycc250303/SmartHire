package com.SmartHire.hrService.service.impl;

import com.SmartHire.common.auth.UserContext;
import com.SmartHire.common.exception.enums.ErrorCode;
import com.SmartHire.common.exception.exception.BusinessException;
import com.SmartHire.hrService.dto.HrInfoCreateDTO;
import com.SmartHire.hrService.dto.HrInfoDTO;
import com.SmartHire.hrService.dto.HrInfoUpdateDTO;
import com.SmartHire.hrService.mapper.CompanyMapper;
import com.SmartHire.hrService.mapper.HrAuditMapper;
import com.SmartHire.hrService.mapper.HrInfoMapper;
import com.SmartHire.hrService.model.Company;
import com.SmartHire.hrService.model.HrAuditRecord;
import com.SmartHire.hrService.model.HrInfo;
import com.SmartHire.hrService.service.HrInfoService;
import com.SmartHire.adminService.enums.AuditStatus;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/** HR信息服务实现类 */
@Service
public class HrInfoServiceImpl extends ServiceImpl<HrInfoMapper, HrInfo> implements HrInfoService {

  @Autowired
  private UserContext userContext;

  @Autowired
  private CompanyMapper companyMapper;

  @Autowired
  private HrAuditMapper hrAuditMapper;

  @Autowired
  private HrInfoMapper hrInfoMapper;

  /**
   * 获取当前登录用户ID
   * 注意：用户身份验证已由AOP在Controller层统一处理，此处无需再次验证
   */
  private Long getCurrentUserId() {
    return userContext.getCurrentUserId();
  }

  @Override
  public  Long getCurrentHrId(){
      Long userId = getCurrentUserId();
      if (userId == null) {
          return null;
      }
      HrInfo hrInfo = hrInfoMapper.selectOne(
              new LambdaQueryWrapper<HrInfo>().eq(HrInfo::getUserId, userId));
      return hrInfo != null ? hrInfo.getId() : null;
  }

  /** 获取当前登录HR的信息 */
  @Override
  public HrInfoDTO getHrInfo() {
    Long userId = getCurrentUserId();

    // 查询HR信息（包含公司名称）
    HrInfoDTO hrInfoDTO = hrInfoMapper.selectHrInfoWithCompanyByUserId(userId);
    if (hrInfoDTO == null) {
      throw new BusinessException(ErrorCode.HR_NOT_EXIST);
    }

    return hrInfoDTO;
  }

  /** 注册HR信息（原 HEAD 功能） */
  @Override
  @Transactional
  public Long createHrInfo(HrInfoCreateDTO createDTO) {
    Long userId = getCurrentUserId();

    // 检查是否已经注册过HR信息
    HrInfo existingHrInfo = lambdaQuery().eq(HrInfo::getUserId, userId).one();
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
    hrInfo.setIsCompanyAdmin(createDTO.getIsCompanyAdmin());

    Date now = new Date();
    hrInfo.setCreatedAt(now);
    hrInfo.setUpdatedAt(now);

    save(hrInfo);

   

    // TODO：HR审核和公司审核

    // 创建HR审核记录（状态为待审核）
    HrAuditRecord auditRecord = new HrAuditRecord();
    auditRecord.setHrId(hrInfo.getId());
    auditRecord.setUserId(userId);
    auditRecord.setCompanyId(createDTO.getCompanyId());
    auditRecord.setHrName(hrInfo.getRealName());
    auditRecord.setAuditStatus(AuditStatus.PENDING.getCode());
    hrAuditMapper.insert(auditRecord);

    return hrInfo.getId();
  }

  /** 新增当前登录HR的信息（若已存在则进行更新）（原 main 功能） */
  @Override
  public void createHrInfo(HrInfoUpdateDTO updateDTO) {
    Long userId = getCurrentUserId();

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
    Long userId = getCurrentUserId();

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