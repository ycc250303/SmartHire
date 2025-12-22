package com.SmartHire.common.api;

import com.SmartHire.hrService.dto.JobCardDTO;
import com.SmartHire.hrService.model.HrInfo;
import com.SmartHire.hrService.model.JobInfo;
import com.SmartHire.hrService.model.Company;

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
   * 根据岗位ID获取HR ID
   *
   * @param jobId 岗位ID
   * @return HR ID，如果岗位不存在返回null
   */
  Long getHrIdByJobId(Long jobId);

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
   * @param jobInfo 职位信息
   * @return 是否更新成功
   */
  boolean updateJobInfo(JobInfo jobInfo);

  /**
   * 根据职位ID获取职位信息
   *
   * @param jobId 职位ID
   * @return 职位信息，如果职位不存在返回null
   */
  JobInfo getJobInfoById(Long jobId);

  /**
   * 根据HR ID获取HR信息
   *
   * @param hrId HR ID
   * @return HR信息，如果HR不存在返回null
   */
  HrInfo getHrInfoById(Long hrId);

  /**
   * 根据公司ID获取公司信息
   *
   * @param companyId 公司ID
   * @return 公司信息，如果公司不存在返回null
   */
  Company getCompanyById(Long companyId);
}
