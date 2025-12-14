package com.SmartHire.common.api;

import com.SmartHire.seekerService.dto.SeekerCardDTO;
import com.SmartHire.seekerService.model.JobSeeker;
import com.SmartHire.seekerService.model.Resume;
import java.util.List;

/** 求职者服务API接口 用于模块间通信，避免直接访问数据库 */
public interface SeekerApi {

  /**
   * 根据用户ID获取求职者ID
   *
   * @param userId 用户ID
   * @return 求职者ID
   */
  Long getJobSeekerIdByUserId(Long userId);

  /**
   * 根据求职者ID获取求职者信息
   *
   * @param jobSeekerId 求职者ID
   * @return 求职者信息
   */
  JobSeeker getJobSeekerById(Long jobSeekerId);

  /**
   * 根据简历ID获取简历信息
   *
   * @param resumeId 简历ID
   * @return 简历信息
   */
  Resume getResumeById(Long resumeId);

  /**
   * 验证简历是否属于指定求职者
   *
   * @param resumeId    简历ID
   * @param jobSeekerId 求职者ID
   * @return 是否属于
   */
  boolean validateResumeOwnership(Long resumeId, Long jobSeekerId);

  /**
   * 根据用户ID删除求职者信息（级联删除所有相关数据） 用于管理员删除用户时调用
   *
   * @param userId 用户ID
   */
  void deleteJobSeekerByUserId(Long userId);

  /**
   * 根据用户ID获取求职者卡片信息
   *
   * @param userId 用户ID
   * @return 求职者卡片信息，如果用户不是求职者或求职者不存在返回null
   */
  SeekerCardDTO getSeekerCard(Long userId);

  /**
   * @return 所有求职者卡片信息
   */
  // List<SeekerCardDTO> getSeekerCards();

  /**
   * 获取指定所在城市的求职者
   *
   * @param city 城市名称
   * @return 求职者卡片信息列表
   */
  List<SeekerCardDTO> getSeekersByCity(String city);

  /**
   * 根据学历筛选求职者（本科/硕士/博士）
   *
   * @param education 学历级别（2-本科，3-硕士，4-博士）
   * @return 求职者卡片信息列表
   */
  List<SeekerCardDTO> getSeekersByEducation(Integer education);

  /**
   * 根据期望薪资筛选求职者（需要区分实习和全职）
   *
   * @param salaryMin    最低期望薪资
   * @param salaryMax    最高期望薪资
   * @param isInternship 是否实习（0-全职，1-实习）
   * @return 求职者卡片信息列表
   */
  List<SeekerCardDTO> getSeekersBySalaryRange(Double salaryMin, Double salaryMax, Integer isInternship);

  /**
   * 根据求职者求职状态筛选求职者（寻找机会/暂不考虑）
   *
   * @param jobStatus 求职状态（0-离校-尽快到岗，1-在校-尽快到岗，2-在校-考虑机会，3-在校-暂不考虑）
   * @return 求职者卡片信息列表
   */
  List<SeekerCardDTO> getSeekersByJobStatus(Integer jobStatus);

  /**
   * 根据是否有实习经历筛选求职者
   *
   * @param hasInternship 是否有实习经历（0-无，1-有）
   * @return 求职者卡片信息列表
   */
  List<SeekerCardDTO> getSeekersByInternshipExperience(Integer hasInternship);
}
