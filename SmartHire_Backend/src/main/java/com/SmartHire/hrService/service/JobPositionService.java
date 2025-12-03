package com.SmartHire.hrService.service;

import com.SmartHire.hrService.dto.JobPositionCreateDTO;
import com.SmartHire.hrService.dto.JobPositionListDTO;
import com.SmartHire.hrService.dto.JobPositionUpdateDTO;
import com.SmartHire.hrService.model.JobPosition;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

/** 岗位服务接口 */
public interface JobPositionService extends IService<JobPosition> {

  /**
   * 发布岗位
   *
   * @param createDTO 岗位创建DTO
   * @return 岗位ID
   */
  Long createJobPosition(JobPositionCreateDTO createDTO);

  /**
   * 更新岗位信息
   *
   * @param jobId 岗位ID
   * @param updateDTO 更新DTO
   */
  void updateJobPosition(Long jobId, JobPositionUpdateDTO updateDTO);

  /**
   * 岗位下线
   *
   * @param jobId 岗位ID
   */
  void offlineJobPosition(Long jobId);

  /**
   * 查询当前HR的岗位列表
   *
   * @param status 状态筛选（可选）：0-已下线 1-招聘中 2-已暂停
   * @return 岗位列表
   */
  List<JobPositionListDTO> getJobPositionList(Integer status);

  /**
   * 更新岗位状态
   *
   * @param jobId 岗位ID
   * @param status 状态：0-已下线 1-招聘中 2-已暂停
   */
  void updateJobPositionStatus(Long jobId, Integer status);

  /**
   * 根据ID获取岗位详情（包含技能要求）
   *
   * @param jobId 岗位ID
   * @return 岗位详情
   */
  JobPositionListDTO getJobPositionById(Long jobId);
}
