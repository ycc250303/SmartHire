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
}
