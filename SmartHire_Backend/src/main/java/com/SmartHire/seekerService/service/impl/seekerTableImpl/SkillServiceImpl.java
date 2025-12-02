package com.SmartHire.seekerService.service.impl.seekerTableImpl;

import com.SmartHire.seekerService.dto.seekerTableDto.SkillDTO;
import com.SmartHire.seekerService.mapper.SkillMapper;
import com.SmartHire.seekerService.model.Skill;
import com.SmartHire.seekerService.service.seekerTableService.SkillService;
import com.SmartHire.shared.exception.enums.ErrorCode;
import com.SmartHire.shared.exception.exception.BusinessException;
import jakarta.validation.Valid;
import java.util.Date;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
public class SkillServiceImpl extends AbstractSeekerOwnedService<SkillMapper, Skill>
    implements SkillService {

  private static final int MAX_SKILL_COUNT = 20;

  @Autowired private SkillMapper skillMapper;

  @Override
  public void addSkill(@Valid SkillDTO request) {
    Long jobSeekerId = currentSeekerId();

    long skillCount = lambdaQuery().eq(Skill::getJobSeekerId, jobSeekerId).count();
    if (skillCount >= MAX_SKILL_COUNT) {
      throw new BusinessException(ErrorCode.SKILL_LIMIT_EXCEEDED);
    }

    ensureSkillNotDuplicate(jobSeekerId, request.getSkillName(), null);

    Skill skill = new Skill();
    skill.setJobSeekerId(jobSeekerId);
    skill.setSkillName(request.getSkillName().trim());
    skill.setLevel(request.getLevel().byteValue());
    Date now = new Date();
    skill.setCreatedAt(now);
    skill.setUpdatedAt(now);

    skillMapper.insert(skill);
  }

  @Override
  public void updateSkill(Long id, SkillDTO request) {
    Long jobSeekerId = currentSeekerId();
    Skill existingSkill = getOwnedSkill(id, jobSeekerId);

    boolean hasUpdate = false;
    if (StringUtils.hasText(request.getSkillName())) {
      ensureSkillNotDuplicate(jobSeekerId, request.getSkillName(), id);
      existingSkill.setSkillName(request.getSkillName().trim());
      hasUpdate = true;
    }
    if (request.getLevel() != null) {
      existingSkill.setLevel(request.getLevel().byteValue());
      hasUpdate = true;
    }

    if (!hasUpdate) {
      throw new BusinessException(ErrorCode.VALIDATION_ERROR);
    }

    existingSkill.setUpdatedAt(new Date());
    skillMapper.updateById(existingSkill);
  }

  @Override
  public List<Skill> getSkills() {
    Long jobSeekerId = currentSeekerId();
    return lambdaQuery()
        .eq(Skill::getJobSeekerId, jobSeekerId)
        .orderByDesc(Skill::getUpdatedAt)
        .list();
  }

  @Override
  public void deleteSkill(Long id) {
    Long jobSeekerId = currentSeekerId();
    getOwnedSkill(id, jobSeekerId);
    skillMapper.deleteById(id);
  }

  private Skill getOwnedSkill(Long id, Long jobSeekerId) {
    return requireOwnedEntity(
        id,
        skillMapper::selectById,
        Skill::getJobSeekerId,
        ErrorCode.SKILL_NOT_EXIST,
        ErrorCode.SKILL_NOT_BELONG_TO_USER);
  }

  private void ensureSkillNotDuplicate(Long jobSeekerId, String skillName, Long excludeId) {
    if (!StringUtils.hasText(skillName)) {
      return;
    }
    long count =
        lambdaQuery()
            .eq(Skill::getJobSeekerId, jobSeekerId)
            .eq(Skill::getSkillName, skillName.trim())
            .ne(excludeId != null, Skill::getId, excludeId)
            .count();
    if (count > 0) {
      throw new BusinessException(ErrorCode.SKILL_ALREADY_EXIST);
    }
  }
}
