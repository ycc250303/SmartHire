package com.SmartHire.hrService.service.impl;

import com.SmartHire.common.api.HrApi;
import com.SmartHire.hrService.dto.JobCardDTO;
import com.SmartHire.hrService.mapper.CompanyMapper;
import com.SmartHire.hrService.mapper.HrInfoMapper;
import com.SmartHire.hrService.mapper.JobInfoMapper;
import com.SmartHire.hrService.model.Company;
import com.SmartHire.hrService.model.HrInfo;
import com.SmartHire.hrService.model.JobInfo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** HR服务API实现类 用于模块间通信 */
@Service
public class HrApiImpl implements HrApi {

  @Autowired
  private HrInfoMapper hrInfoMapper;

  @Autowired
  private JobInfoMapper jobInfoMapper;

  @Autowired
  private CompanyMapper companyMapper;

  @Override
  public Long getHrIdByUserId(Long userId) {
    if (userId == null) {
      return null;
    }
    HrInfo hrInfo = hrInfoMapper.selectOne(
        new LambdaQueryWrapper<HrInfo>().eq(HrInfo::getUserId, userId));
    return hrInfo != null ? hrInfo.getId() : null;
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
    return jobInfo != null ? jobInfo.getHrId() : null;
  }

  @Override
  public JobCardDTO getJobCardByJobId(Long jobId) {
    if (jobId == null) {
      return null;
    }
    return jobInfoMapper.selectJobCardById(jobId);
  }

  @Override
  public Long getHrUserIdByHrId(Long hrId) {
    if (hrId == null) {
      return null;
    }
    HrInfo hrInfo = hrInfoMapper.selectById(hrId);
    return hrInfo != null ? hrInfo.getUserId() : null;
  }

  @Override
  public boolean updateJobInfo(JobInfo jobInfo) {
    if (jobInfo == null || jobInfo.getId() == null) {
      return false;
    }
    return jobInfoMapper.updateById(jobInfo) > 0;
  }

  @Override
  public JobInfo getJobInfoById(Long jobId) {
    if (jobId == null) {
      return null;
    }
    return jobInfoMapper.selectById(jobId);
  }

  @Override
  public HrInfo getHrInfoById(Long hrId) {
    if (hrId == null) {
      return null;
    }
    return hrInfoMapper.selectById(hrId);
  }

  @Override
  public Company getCompanyById(Long companyId) {
    if (companyId == null) {
      return null;
    }
    return companyMapper.selectById(companyId);
  }
}
