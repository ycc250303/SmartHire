package com.SmartHire.seekerService.service.impl;

import com.SmartHire.seekerService.dto.SeekerDTO;
import com.SmartHire.seekerService.mapper.EducationExperienceMapper;
import com.SmartHire.seekerService.mapper.JobSeekerExpectationMapper;
import com.SmartHire.seekerService.mapper.JobSeekerMapper;
import com.SmartHire.seekerService.mapper.ProjectExperienceMapper;
import com.SmartHire.seekerService.mapper.ResumeMapper;
import com.SmartHire.seekerService.mapper.SkillMapper;
import com.SmartHire.seekerService.mapper.WorkExperienceMapper;
import com.SmartHire.seekerService.model.JobSeeker;
import com.SmartHire.seekerService.model.Resume;
import com.SmartHire.seekerService.service.JobSeekerService;
import com.SmartHire.shared.exception.enums.ErrorCode;
import com.SmartHire.shared.exception.exception.BusinessException;
import com.SmartHire.shared.utils.AliOssUtil;
import com.SmartHire.shared.utils.JwtUtil;
import com.SmartHire.shared.utils.SecurityContextUtil;
import com.SmartHire.userAuthService.mapper.UserAuthMapper;
import com.SmartHire.userAuthService.model.User;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.util.Date;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 求职者信息表 服务实现类
 *
 * @author SmartHire Team
 * @since 2025-11-19
 */
@Slf4j
@Service
public class JobSeekerServiceImpl extends ServiceImpl<JobSeekerMapper, JobSeeker>
    implements JobSeekerService {
  @Autowired private JobSeekerMapper jobSeekerMapper;

  @Autowired private UserAuthMapper userAuthMapper;

  @Autowired private JwtUtil jwtUtil;

  @Autowired private ResumeMapper resumeMapper;

  @Autowired private JobSeekerExpectationMapper jobSeekerExpectationMapper;

  @Autowired private EducationExperienceMapper educationExperienceMapper;

  @Autowired private WorkExperienceMapper workExperienceMapper;

  @Autowired private ProjectExperienceMapper projectExperienceMapper;

  @Autowired private SkillMapper skillMapper;

  @Autowired private AliOssUtil aliOssUtil;

  /**
   * 注册求职者
   *
   * @param request 注册求职者信息
   */
  @Override
  public void registerSeeker(SeekerDTO request) {
    Map<String, Object> map = SecurityContextUtil.getCurrentClaims();
    Long userId = jwtUtil.getUserIdFromToken(map);
    log.info("用户ID：{}", userId);
    // 验证用户是否存在
    User user = userAuthMapper.findById(userId);
    if (user == null) {
      throw new BusinessException(ErrorCode.USER_ID_NOT_EXIST);
    }

    // 检查该用户是否已经注册过求职者信息
    JobSeeker existingSeeker = lambdaQuery().eq(JobSeeker::getUserId, userId).one();
    if (existingSeeker != null) {
      throw new BusinessException(ErrorCode.SEEKER_ALREADY_REGISTERED);
    }

    JobSeeker jobSeeker = new JobSeeker();
    jobSeeker.setUserId(userId);
    jobSeeker.setRealName(request.getRealName());
    jobSeeker.setBirthDate(request.getBirthDate());
    jobSeeker.setCurrentCity(request.getCurrentCity());
    jobSeeker.setJobStatus(request.getJobStatus());

    jobSeeker.setCreatedAt(new Date());
    jobSeeker.setUpdatedAt(new Date());

    jobSeekerMapper.insert(jobSeeker);
  }

  /**
   * 获取求职者信息
   *
   * @return 求职者信息
   */
  @Override
  public JobSeeker getSeekerInfo() {
    Map<String, Object> map = SecurityContextUtil.getCurrentClaims();
    Long userId = jwtUtil.getUserIdFromToken(map);

    // 验证用户是否存在
    User user = userAuthMapper.selectById(userId);
    if (user == null) {
      throw new BusinessException(ErrorCode.USER_ID_NOT_EXIST);
    }

    // 验证用户身份是否为求职者
    if (user.getUserType() != 1) {
      throw new BusinessException(ErrorCode.USER_NOT_SEEKER);
    }

    // 通过用户ID获取（job_seeker表有user_id字段，且是唯一索引）
    JobSeeker jobSeeker = lambdaQuery().eq(JobSeeker::getUserId, userId).one();

    if (jobSeeker == null) {
      throw new BusinessException(ErrorCode.SEEKER_NOT_EXIST);
    }

    return jobSeeker;
  }

  /**
   * 获取求职者ID
   *
   * @return 求职者ID
   */
  @Override
  public Long getJobSeekerId() {
    Map<String, Object> map = SecurityContextUtil.getCurrentClaims();
    Long userId = jwtUtil.getUserIdFromToken(map);
    if (userId == null) {
      throw new BusinessException(ErrorCode.USER_ID_NOT_EXIST);
    }
    JobSeeker jobSeeker = lambdaQuery().eq(JobSeeker::getUserId, userId).one();

    if (jobSeeker == null) {
      throw new BusinessException(ErrorCode.SEEKER_NOT_EXIST);
    }

    return jobSeeker.getId();
  }

  /**
   * 更新求职者信息
   *
   * @param request 更新求职者信息
   */
  @Override
  @Transactional(rollbackFor = Exception.class)
  public void updateSeekerInfo(SeekerDTO request) {
    // 获取当前求职者信息
    JobSeeker jobSeeker = getSeekerInfo();
    if (jobSeeker == null) {
      throw new BusinessException(ErrorCode.SEEKER_NOT_EXIST);
    }

    boolean hasUpdate = false;

    // 更新真实姓名
    if (StringUtils.hasText(request.getRealName())) {
      jobSeeker.setRealName(request.getRealName().trim());
      hasUpdate = true;
    }

    // 更新出生日期
    if (request.getBirthDate() != null) {
      jobSeeker.setBirthDate(request.getBirthDate());
      hasUpdate = true;
    }

    // 更新当前城市
    if (StringUtils.hasText(request.getCurrentCity())) {
      jobSeeker.setCurrentCity(request.getCurrentCity().trim());
      hasUpdate = true;
    }

    // 更新求职状态
    if (request.getJobStatus() != null) {
      jobSeeker.setJobStatus(request.getJobStatus());
      hasUpdate = true;
    }

    if (!hasUpdate) {
      throw new BusinessException(ErrorCode.VALIDATION_ERROR);
    }

    jobSeeker.setUpdatedAt(new Date());
    jobSeekerMapper.updateById(jobSeeker);
    log.info("更新求职者信息成功，求职者ID：{}", jobSeeker.getId());
  }

  /** 删除求职者信息（级联删除所有相关数据） */
  @Override
  @Transactional(rollbackFor = Exception.class)
  public void deleteJobSeeker() {
    // 获取当前求职者信息
    JobSeeker jobSeeker = getSeekerInfo();
    if (jobSeeker == null) {
      throw new BusinessException(ErrorCode.SEEKER_NOT_EXIST);
    }

    Long jobSeekerId = jobSeeker.getId();
    log.info("开始删除求职者，求职者ID：{}", jobSeekerId);

    // 注意：以下删除操作按照依赖关系顺序执行
    // 由于部分表的Mapper可能不存在，这里使用MyBatis Plus的lambdaQuery进行删除

    // 1. 删除沟通模块相关数据（需要先删除，因为它们依赖application）
    // 注意：interview和chat_message的Mapper可能不存在，这里使用SQL或MyBatis Plus
    // 如果这些表的Mapper存在，应该注入相应的Mapper进行删除

    // 2. 删除投递与匹配模块相关数据
    // 注意：application, job_favorite, candidate_favorite的Mapper可能不存在
    // 如果存在，应该注入相应的Mapper进行删除

    // 3. 删除简历模块相关数据
    // 这些表的Mapper应该都存在，使用现有的Service或Mapper进行删除
    deleteResumeModuleData(jobSeekerId);

    // 4. 删除求职者主表记录
    jobSeekerMapper.deleteById(jobSeekerId);
    log.info("删除求职者成功，求职者ID：{}", jobSeekerId);

    // 5. 注意：user表不在这里删除，因为用户可能同时是HR
    // 如果需要删除用户，应该在用户服务中处理
  }

  /**
   * 根据用户ID删除求职者信息（级联删除所有相关数据） 用于管理员删除用户时调用
   *
   * @param userId 用户ID
   */
  @Override
  @Transactional(rollbackFor = Exception.class)
  public void deleteJobSeekerByUserId(Long userId) {
    // 查询求职者信息
    JobSeeker jobSeeker = lambdaQuery().eq(JobSeeker::getUserId, userId).one();

    if (jobSeeker == null) {
      log.info("用户 {} 不是求职者，无需删除求职者数据", userId);
      return;
    }

    Long jobSeekerId = jobSeeker.getId();
    log.info("开始删除求职者，求职者ID：{}，用户ID：{}", jobSeekerId, userId);

    // 删除简历模块相关数据
    deleteResumeModuleData(jobSeekerId);

    // 删除求职者主表记录
    jobSeekerMapper.deleteById(jobSeekerId);
    log.info("删除求职者成功，求职者ID：{}，用户ID：{}", jobSeekerId, userId);
  }

  /**
   * 删除简历模块相关数据
   *
   * <p>效率分析： 1. 直接使用Mapper批量删除（当前实现）： - 每条表：1条SQL批量删除所有记录 - Resume特殊处理：1次查询获取fileUrl + 1次批量删除SQL +
   * N次OSS删除 - 总操作数：查询次数 + 删除SQL次数 + OSS删除次数
   *
   * <p>2. 复用Service接口（不推荐）： - 每条表：1次查询获取ID列表 + N次deleteById（N条SQL）+ N次权限验证 - Resume：1次查询获取ID列表 +
   * N次deleteById + N次权限验证 + N次OSS删除 - 总操作数：查询次数 + N*删除SQL次数 + N*权限验证 + N*OSS删除
   *
   * <p>结论：直接使用Mapper批量删除效率更高，因为： - 批量删除：1条SQL vs N条SQL（N是记录数） - 不需要权限验证（删除求职者时已经是自己的数据） - 代码更简洁
   *
   * @param jobSeekerId 求职者ID
   */
  private void deleteResumeModuleData(Long jobSeekerId) {
    log.info("删除简历模块相关数据，求职者ID：{}", jobSeekerId);

    // 1. 删除简历文件记录（需要先查询获取fileUrl，然后删除OSS文件）
    List<Resume> resumes =
        resumeMapper.selectList(
            new LambdaQueryWrapper<Resume>()
                .eq(Resume::getJobSeekerId, jobSeekerId)
                .select(Resume::getFileUrl)); // 只查询fileUrl字段，提高效率

    // 批量删除OSS文件
    for (Resume resume : resumes) {
      if (resume.getFileUrl() != null && !resume.getFileUrl().isBlank()) {
        String objectName = aliOssUtil.extractObjectName(resume.getFileUrl());
        if (objectName != null && !objectName.isBlank()) {
          boolean deleted = aliOssUtil.deleteFile(objectName);
          if (deleted) {
            log.info("删除附件简历文件成功, objectName={}", objectName);
          } else {
            log.warn("删除附件简历文件失败, objectName={}", objectName);
          }
        }
      }
    }

    // 批量删除数据库记录
    resumeMapper.delete(new LambdaQueryWrapper<Resume>().eq(Resume::getJobSeekerId, jobSeekerId));

    // 2. 删除求职期望记录（批量删除，1条SQL）
    jobSeekerExpectationMapper.delete(
        new LambdaQueryWrapper<com.SmartHire.seekerService.model.JobSeekerExpectation>()
            .eq(
                com.SmartHire.seekerService.model.JobSeekerExpectation::getJobSeekerId,
                jobSeekerId));

    // 3. 删除教育经历记录（批量删除，1条SQL）
    educationExperienceMapper.delete(
        new LambdaQueryWrapper<com.SmartHire.seekerService.model.EducationExperience>()
            .eq(
                com.SmartHire.seekerService.model.EducationExperience::getJobSeekerId,
                jobSeekerId));

    // 4. 删除工作经历记录（批量删除，1条SQL）
    workExperienceMapper.delete(
        new LambdaQueryWrapper<com.SmartHire.seekerService.model.WorkExperience>()
            .eq(com.SmartHire.seekerService.model.WorkExperience::getJobSeekerId, jobSeekerId));

    // 5. 删除项目经历记录（批量删除，1条SQL）
    projectExperienceMapper.delete(
        new LambdaQueryWrapper<com.SmartHire.seekerService.model.ProjectExperience>()
            .eq(com.SmartHire.seekerService.model.ProjectExperience::getJobSeekerId, jobSeekerId));

    // 6. 删除技能记录（批量删除，1条SQL）
    skillMapper.delete(
        new LambdaQueryWrapper<com.SmartHire.seekerService.model.Skill>()
            .eq(com.SmartHire.seekerService.model.Skill::getJobSeekerId, jobSeekerId));

    // 注意：certificate表的Mapper可能不存在，如果存在需要添加
    // 如果不存在，可以使用MyBatis Plus的通用删除方法
    log.info("删除简历模块相关数据完成，求职者ID：{}", jobSeekerId);
  }

  /**
   * 根据教育经历自动更新求职者的最高学历和毕业年份 此方法应在教育经历增删改时调用
   *
   * @param jobSeekerId 求职者ID
   */
  public void updateEducationAndGraduationYear(Long jobSeekerId) {
    // 查询该求职者的所有教育经历，按学历和结束年份排序
    List<com.SmartHire.seekerService.model.EducationExperience> educations =
        educationExperienceMapper.selectList(
            new LambdaQueryWrapper<com.SmartHire.seekerService.model.EducationExperience>()
                .eq(
                    com.SmartHire.seekerService.model.EducationExperience::getJobSeekerId,
                    jobSeekerId)
                .orderByDesc(com.SmartHire.seekerService.model.EducationExperience::getEducation)
                .orderByDesc(com.SmartHire.seekerService.model.EducationExperience::getEndYear));

    if (educations == null || educations.isEmpty()) {
      // 如果没有教育经历，清空最高学历和毕业年份
      JobSeeker jobSeeker = jobSeekerMapper.selectById(jobSeekerId);
      if (jobSeeker != null) {
        jobSeeker.setEducation(null);
        jobSeeker.setGraduationYear(null);
        jobSeeker.setUpdatedAt(new Date());
        jobSeekerMapper.updateById(jobSeeker);
      }
      return;
    }

    // 找到最高学历（education值最大的）
    Integer maxEducation =
        educations.stream()
            .map(com.SmartHire.seekerService.model.EducationExperience::getEducation)
            .filter(edu -> edu != null)
            .max(Integer::compareTo)
            .orElse(null);

    // 找到最高学历对应的最晚毕业年份
    String latestGraduationYear =
        educations.stream()
            .filter(edu -> maxEducation != null && maxEducation.equals(edu.getEducation()))
            .filter(edu -> edu.getEndYear() != null)
            .map(edu -> String.valueOf(edu.getEndYear().getYear()))
            .max(String::compareTo)
            .orElse(null);

    // 更新求职者信息
    JobSeeker jobSeeker = jobSeekerMapper.selectById(jobSeekerId);
    if (jobSeeker != null) {
      jobSeeker.setEducation(maxEducation);
      jobSeeker.setGraduationYear(latestGraduationYear);
      jobSeeker.setUpdatedAt(new Date());
      jobSeekerMapper.updateById(jobSeeker);
      log.info(
          "自动更新求职者最高学历和毕业年份，求职者ID：{}，最高学历：{}，毕业年份：{}",
          jobSeekerId,
          maxEducation,
          latestGraduationYear);
    }
  }
}
