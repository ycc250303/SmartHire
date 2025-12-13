package com.SmartHire.hrService.service;

import com.SmartHire.hrService.dto.JobInfoCreateDTO;
import com.SmartHire.hrService.dto.JobInfoListDTO;
import com.SmartHire.hrService.dto.JobInfoUpdateDTO;
import com.SmartHire.hrService.dto.JobSearchDTO;
import com.SmartHire.hrService.dto.JobCardDTO;
import com.SmartHire.hrService.model.JobInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

/** 岗位服务接口 */
public interface JobInfoService extends IService<JobInfo> {

  /**
   * 发布岗位
   *
   * @param createDTO 岗位创建DTO
   * @return 岗位ID
   */
  Long createJobInfo(JobInfoCreateDTO createDTO);

  /**
   * 更新岗位信息
   *
   * @param jobId 岗位ID
   * @param updateDTO 更新DTO
   */
  void updateJobInfo(Long jobId, JobInfoUpdateDTO updateDTO);

  /**
   * 岗位下线
   *
   * @param jobId 岗位ID
   */
  void offlineJobInfo(Long jobId);

  /**
   * 查询当前HR的岗位列表
   *
   * @param status 状态筛选（可选）：0-已下线 1-招聘中 2-已暂停
   * @return 岗位列表
   */
  List<JobInfoListDTO> getJobInfoList(Integer status);

  /**
   * 更新岗位状态
   *
   * @param jobId 岗位ID
   * @param status 状态：0-已下线 1-招聘中 2-已暂停
   */
  void updateJobInfoStatus(Long jobId, Integer status);

  /**
   * 根据ID获取岗位详情（包含技能要求）
   *
   * @param jobId 岗位ID
   * @return 岗位详情
   */
  JobInfoListDTO getJobInfoById(Long jobId);

  /**
   * 求职者端岗位筛选
   *
   * @param searchDTO 筛选条件
   * @return 岗位卡片列表
   */
  List<JobCardDTO> searchPublicJobs(JobSearchDTO searchDTO);

  /**
   * 提交岗位审核
   *
   * @param jobId 岗位ID
   */
  void submitJobForAudit(Long jobId);
}
