package com.SmartHire.hrService.service.impl;

import com.SmartHire.common.api.HrApi;
import com.SmartHire.common.api.UserAuthApi;
import com.SmartHire.hrService.dto.JobCardDTO;
import com.SmartHire.hrService.mapper.CompanyMapper;
import com.SmartHire.hrService.mapper.HrInfoMapper;
import com.SmartHire.hrService.mapper.JobInfoMapper;
import com.SmartHire.hrService.model.Company;
import com.SmartHire.hrService.model.HrInfo;
import com.SmartHire.hrService.model.JobInfo;
import com.SmartHire.userAuthService.model.User;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** HR服务API实现类 用于模块间通信 */
@Service
public class HrApiImpl implements HrApi {

  @Autowired private HrInfoMapper hrInfoMapper;

  @Autowired private JobInfoMapper jobInfoMapper;

  @Autowired private CompanyMapper companyMapper;

  @Autowired private UserAuthApi userAuthApi;

  @Override
  public Long getHrIdByUserId(Long userId) {
    if (userId == null) {
      return null;
    }
    HrInfo hrInfo =
        hrInfoMapper.selectOne(new LambdaQueryWrapper<HrInfo>().eq(HrInfo::getUserId, userId));
    return hrInfo == null ? null : hrInfo.getId();
  }

  @Override
  public boolean validateJobOwnership(Long jobId, Long hrId) {
    if (jobId == null || hrId == null) {
      return false;
    }
    JobInfo jobInfo = jobInfoMapper.selectById(jobId);
    return jobInfo != null && hrId.equals(jobInfo.getHrId());
  }

  @Override
  public Long getHrIdByJobId(Long jobId) {
    if (jobId == null) {
      return null;
    }
    JobInfo jobInfo = jobInfoMapper.selectById(jobId);
    return jobInfo == null ? null : jobInfo.getHrId();
  }

  @Override
  public JobCardDTO getJobCardByJobId(Long jobId) {
    if (jobId == null) {
      return null;
    }

    // 1. 查询岗位信息
    JobInfo jobInfo = jobInfoMapper.selectById(jobId);
    if (jobInfo == null) {
      return null;
    }

    // 2. 查询公司信息
    Company company = companyMapper.selectById(jobInfo.getCompanyId());
    if (company == null) {
      return null;
    }

    // 3. 查询HR信息
    HrInfo hrInfo = hrInfoMapper.selectById(jobInfo.getHrId());
    if (hrInfo == null) {
      return null;
    }

    // 4. 查询用户信息（获取头像）
    User user = userAuthApi.getUserById(hrInfo.getUserId());

    // 5. 构建JobCard DTO
    JobCardDTO jobCard = new JobCardDTO();
    jobCard.setJobId(jobInfo.getId());
    jobCard.setJobTitle(jobInfo.getJobTitle());
    jobCard.setSalaryMin(jobInfo.getSalaryMin());
    jobCard.setSalaryMax(jobInfo.getSalaryMax());
    jobCard.setAddress(jobInfo.getAddress());
    jobCard.setCompanyName(company.getCompanyName());
    jobCard.setCompanyScale(company.getCompanyScale());
    jobCard.setFinancingStage(company.getFinancingStage());
    jobCard.setHrName(hrInfo.getRealName());
    jobCard.setHrAvatarUrl(user.getAvatarUrl());
    jobCard.setEducationRequired(jobInfo.getEducationRequired());

    return jobCard;
  }
}
