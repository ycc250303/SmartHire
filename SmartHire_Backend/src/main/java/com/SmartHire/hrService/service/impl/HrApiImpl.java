package com.SmartHire.hrService.service.impl;

import com.SmartHire.common.api.HrApi;
import com.SmartHire.common.dto.hrDto.CompanyDTO;
import com.SmartHire.common.dto.hrDto.HrInfoDTO;
import com.SmartHire.common.dto.hrDto.JobCardDTO;
import com.SmartHire.common.dto.hrDto.JobFullDetailDTO;
import com.SmartHire.common.dto.hrDto.JobInfoDTO;
import com.SmartHire.common.dto.hrDto.JobSearchDTO;
import com.SmartHire.hrService.dto.JobInfoListDTO;
import com.SmartHire.hrService.mapper.CompanyMapper;
import com.SmartHire.hrService.mapper.HrInfoMapper;
import com.SmartHire.hrService.mapper.JobInfoMapper;
import com.SmartHire.hrService.model.Company;
import com.SmartHire.hrService.model.HrInfo;
import com.SmartHire.hrService.model.JobInfo;
import com.SmartHire.hrService.service.JobInfoService;
import com.SmartHire.hrService.service.HrInfoService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import java.util.List;
import org.springframework.beans.BeanUtils;
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

  @Autowired
  private JobInfoService jobInfoService;

  @Autowired
  private HrInfoService hrInfoService;

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
  public Long getCurrentHrId() {
    return hrInfoService.getCurrentHrId();
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
  public boolean isHrAuthorizedForJob(Long hrId, Long jobId) {
    // 目前逻辑简单，即验证所有权
    return validateJobOwnership(jobId, hrId);
  }

  @Override
  public JobFullDetailDTO getJobFullDetail(Long jobId) {
    if (jobId == null) {
      return null;
    }

    JobInfoDTO jobInfoDTO = getJobInfoById(jobId);
    if (jobInfoDTO == null) {
      return null;
    }

    JobFullDetailDTO fullDetail = new JobFullDetailDTO();
    fullDetail.setJobInfo(jobInfoDTO);

    if (jobInfoDTO.getCompanyId() != null) {
      fullDetail.setCompany(getCompanyById(jobInfoDTO.getCompanyId()));
    }

    if (jobInfoDTO.getHrId() != null) {
      fullDetail.setHrInfo(getHrInfoById(jobInfoDTO.getHrId()));
    }

    return fullDetail;
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
  public boolean updateJobInfo(JobInfoDTO jobInfoDTO) {
    if (jobInfoDTO == null || jobInfoDTO.getId() == null) {
      return false;
    }
    JobInfo jobInfo = new JobInfo();
    BeanUtils.copyProperties(jobInfoDTO, jobInfo);
    return jobInfoMapper.updateById(jobInfo) > 0;
  }

  @Override
  public JobInfoDTO getJobInfoById(Long jobId) {
    if (jobId == null) {
      return null;
    }
    JobInfo jobInfo = jobInfoMapper.selectById(jobId);
    if (jobInfo == null) {
      return null;
    }
    JobInfoDTO dto = new JobInfoDTO();
    BeanUtils.copyProperties(jobInfo, dto);
    return dto;
  }

  @Override
  public HrInfoDTO getHrInfoById(Long hrId) {
    if (hrId == null) {
      return null;
    }
    HrInfo hrInfo = hrInfoMapper.selectById(hrId);
    if (hrInfo == null) {
      return null;
    }
    HrInfoDTO dto = new HrInfoDTO();
    BeanUtils.copyProperties(hrInfo, dto);
    return dto;
  }

  @Override
  public CompanyDTO getCompanyById(Long companyId) {
    if (companyId == null) {
      return null;
    }
    Company company = companyMapper.selectById(companyId);
    if (company == null) {
      return null;
    }
    CompanyDTO dto = new CompanyDTO();
    BeanUtils.copyProperties(company, dto);
    return dto;
  }

  @Override
  public List<JobCardDTO> searchPublicJobs(JobSearchDTO searchDTO) {
    if (searchDTO == null) {
      return null;
    }
    JobSearchDTO internalSearchDTO = new JobSearchDTO();
    BeanUtils.copyProperties(searchDTO, internalSearchDTO);
    return jobInfoService.searchPublicJobs(internalSearchDTO);
  }

  @Override
  public JobInfoDTO getJobInfoWithSkills(Long jobId) {
    JobInfoListDTO internalDto = jobInfoService.getJobInfoById(jobId);
    if (internalDto == null) {
      return null;
    }
    JobInfoDTO dto = new JobInfoDTO();
    BeanUtils.copyProperties(internalDto, dto);
    return dto;
  }
}
