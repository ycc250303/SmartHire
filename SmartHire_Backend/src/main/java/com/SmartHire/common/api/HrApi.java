package com.SmartHire.common.api;

import com.SmartHire.common.dto.hrDto.CompanyDTO;
import com.SmartHire.common.dto.hrDto.HrInfoDTO;
import com.SmartHire.common.dto.hrDto.JobCardDTO;
import com.SmartHire.common.dto.hrDto.JobFullDetailDTO;
import com.SmartHire.common.dto.hrDto.JobInfoDTO;
import com.SmartHire.common.dto.hrDto.JobSearchDTO;
import java.util.List;

/** HR服务API接口 用于模块间通信，避免直接访问数据库 */
public interface HrApi {

  /**
   * 根据用户ID获取HR ID
   *
   * @param userId 用户ID
   * @return HR ID，如果用户不是HR或HR不存在返回null
   */
  Long getHrIdByUserId(Long userId);

  /**
   * 验证岗位是否属于指定HR
   *
   * @param jobId 岗位ID
   * @param hrId  HR ID
   * @return 是否属于
   */
  boolean validateJobOwnership(Long jobId, Long hrId);

  /**
   * 语义化校验：当前HR是否有权操作该岗位
   *
   * @param hrId  HR ID
   * @param jobId 岗位ID
   * @return 是否有权
   */
  boolean isHrAuthorizedForJob(Long hrId, Long jobId);

  /**
   * 获取岗位的完整详情（包含公司和HR信息）
   *
   * @param jobId 岗位ID
   * @return 岗位完整详情
   */
  JobFullDetailDTO getJobFullDetail(Long jobId);

  /**
   * 求职者端岗位筛选
   *
   * @param searchDTO 筛选条件
   * @return 岗位卡片列表
   */
  List<JobCardDTO> searchPublicJobs(JobSearchDTO searchDTO);

  /**
   * 根据ID获取岗位详情（包含技能要求）
   *
   * @param jobId 岗位ID
   * @return 岗位详情
   */
  JobInfoDTO getJobInfoWithSkills(Long jobId);

  /**
   * 根据岗位ID获取HR ID
   *
   * @param jobId 岗位ID
   * @return HR ID，如果岗位不存在返回null
   */
  Long getHrIdByJobId(Long jobId);

  /**
   * 获取当前登录HR的ID（hr_info表的id）
   *
   * @return HR ID，如果用户不是HR或HR不存在返回null
   */
  Long getCurrentHrId();

  /**
   * 根据岗位ID获取岗位卡片信息
   *
   * @param jobId 岗位ID
   * @return 岗位卡片信息，如果岗位不存在返回null
   */
  JobCardDTO getJobCardByJobId(Long jobId);

  /**
   * 根据HR ID获取HR用户ID
   *
   * @param hrId HR ID（hr_info表的id）
   * @return HR用户ID（user表的id），如果HR不存在返回null
   */
  Long getHrUserIdByHrId(Long hrId);

  /**
   * 更新职位信息
   *
   * @param jobInfoDTO 职位信息
   * @return 是否更新成功
   */
  boolean updateJobInfo(JobInfoDTO jobInfoDTO);

  /**
   * 根据职位ID获取职位信息
   *
   * @param jobId 职位ID
   * @return 职位信息，如果职位不存在返回null
   */
  JobInfoDTO getJobInfoById(Long jobId);

  /**
   * 根据HR ID获取HR信息
   *
   * @param hrId HR ID
   * @return HR信息，如果HR不存在返回null
   */
  HrInfoDTO getHrInfoById(Long hrId);

  /**
   * 根据公司ID获取公司信息
   *
   * @param companyId 公司ID
   * @return 公司信息，如果公司不存在返回null
   */
  CompanyDTO getCompanyById(Long companyId);
}
