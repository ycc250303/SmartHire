package com.SmartHire.hrService.service.impl;

import com.SmartHire.common.auth.UserContext;
import com.SmartHire.common.exception.enums.ErrorCode;
import com.SmartHire.common.exception.exception.BusinessException;
import com.SmartHire.hrService.mapper.HrApplicationMapper;
import com.SmartHire.hrService.mapper.HrInfoMapper;
import com.SmartHire.hrService.mapper.JobInfoMapper;
import com.SmartHire.hrService.mapper.JobSeekerSkillMapper;
import com.SmartHire.hrService.mapper.JobSkillRequirementMapper;
import com.SmartHire.hrService.model.HrInfo;
import com.SmartHire.hrService.model.JobInfo;
import com.SmartHire.hrService.service.MatchingService;
import com.SmartHire.recruitmentService.dto.ApplicationListDTO;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/** 匹配服务实现类 */
@Service
public class MatchingServiceImpl implements MatchingService {

  @Autowired
  private UserContext userContext;

  @Autowired
  private JobInfoMapper jobInfoMapper;

  @Autowired
  private JobSkillRequirementMapper jobSkillRequirementMapper;

  @Autowired
  private JobSeekerSkillMapper jobSeekerSkillMapper;

  @Autowired
  private HrApplicationMapper hrApplicationMapper;


  /** 校验岗位归属 */
  private JobInfo validateJobOwnership(Long jobId, Long hrId) {
    JobInfo jobInfo = jobInfoMapper.selectById(jobId);
    if (jobInfo == null) {
      throw new BusinessException(ErrorCode.JOB_NOT_EXIST);
    }
    if (!jobInfo.getHrId().equals(hrId)) {
      throw new BusinessException(ErrorCode.JOB_NOT_BELONG_TO_HR);
    }
    return jobInfo;
  }

  @Override
  @Transactional
  public List<ApplicationListDTO> matchApplicationsForJob(Long jobId) {
    Long hrId = userContext.getCurrentHrId();
    validateJobOwnership(jobId, hrId);

    List<String> requiredSkills = jobSkillRequirementMapper.selectSkillNamesByJobId(jobId);
    Set<String> requiredSkillSet = requiredSkills == null
        ? new LinkedHashSet<>()
        : requiredSkills.stream()
            .filter(StringUtils::hasText)
            .map(this::normalizeSkill)
            .collect(Collectors.toCollection(LinkedHashSet::new));

    List<ApplicationListDTO> applications = hrApplicationMapper.selectApplicationsByJob(hrId, jobId);
    if (CollectionUtils.isEmpty(applications)) {
      return new ArrayList<>();
    }

    Date now = new Date();
    for (ApplicationListDTO application : applications) {
      List<String> seekerSkills = jobSeekerSkillMapper.selectSkillNamesByJobSeekerId(application.getJobSeekerId());
      Set<String> seekerSkillSet = seekerSkills == null
          ? new LinkedHashSet<>()
          : seekerSkills.stream()
              .filter(StringUtils::hasText)
              .map(this::normalizeSkill)
              .collect(Collectors.toCollection(LinkedHashSet::new));

      Set<String> matchedSkills = new LinkedHashSet<>();
      for (String req : requiredSkillSet) {
        if (seekerSkillSet.contains(req)) {
          matchedSkills.add(req);
        }
      }

      BigDecimal score = calculateScore(requiredSkillSet.size(), matchedSkills.size(), seekerSkillSet.size());
      String analysis = buildMatchAnalysis(requiredSkills, seekerSkills, matchedSkills);

      hrApplicationMapper.updateMatchResult(application.getId(), score, analysis, now);
      application.setMatchScore(score);
      application.setMatchAnalysis(analysis);
    }

    Comparator<BigDecimal> scoreComparator = Comparator.nullsLast(Comparator.reverseOrder());
    Comparator<Date> dateComparator = Comparator.nullsLast(Comparator.reverseOrder());

    applications.sort(
        Comparator.comparing(ApplicationListDTO::getMatchScore, scoreComparator)
            .thenComparing(ApplicationListDTO::getCreatedAt, dateComparator));

    return applications;
  }

  /** 规范化技能名称 */
  private String normalizeSkill(String skill) {
    return skill == null ? "" : skill.trim().toLowerCase(Locale.ROOT);
  }

  /** 计算匹配得分（0-100） */
  private BigDecimal calculateScore(int requiredCount, int matchedCount, int seekerTotal) {
    double baseScore;
    if (requiredCount > 0) {
      baseScore = (double) matchedCount / requiredCount * 100D;
    } else {
      baseScore = seekerTotal > 0 ? 70D : 50D;
    }

    double bonus = seekerTotal > matchedCount ? Math.min(20D, (seekerTotal - matchedCount) * 5D) : 0D;
    double finalScore = Math.min(100D, baseScore + bonus);
    return BigDecimal.valueOf(finalScore).setScale(2, RoundingMode.HALF_UP);
  }

  /** 构建匹配分析说明 */
  private String buildMatchAnalysis(
      List<String> requiredSkills, List<String> seekerSkills, Set<String> matchedSkills) {
    int requiredSize = requiredSkills == null ? 0 : requiredSkills.size();
    int seekerSize = seekerSkills == null ? 0 : seekerSkills.size();
    int matchedSize = matchedSkills == null ? 0 : matchedSkills.size();

    String matchedStr = matchedSkills == null || matchedSkills.isEmpty()
        ? "[]"
        : matchedSkills.stream()
            .map(skill -> "\"" + skill + "\"")
            .collect(Collectors.joining(", ", "[", "]"));

    return String.format(
        "{\"required\":%d,\"seeker\":%d,\"matched\":%d,\"matchedSkills\":%s}",
        requiredSize, seekerSize, matchedSize, matchedStr);
  }
}