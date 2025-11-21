package com.SmartHire.seekerService.service.impl;

import com.SmartHire.seekerService.dto.SkillDTO;
import com.SmartHire.seekerService.mapper.SkillMapper;
import com.SmartHire.seekerService.model.Skill;
import com.SmartHire.seekerService.service.JobSeekerService;
import com.SmartHire.seekerService.service.SkillService;
import com.SmartHire.shared.exception.enums.ErrorCode;
import com.SmartHire.shared.exception.exception.BusinessException;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.validation.Valid;
import java.util.Date;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
public class SkillServiceImpl extends ServiceImpl<SkillMapper, Skill> implements SkillService {

    private static final int MAX_SKILL_COUNT = 20;

    @Autowired
    private SkillMapper skillMapper;

    @Autowired
    private JobSeekerService jobSeekerService;

    @Override
    public void addSkill(@Valid SkillDTO request) {
        Long jobSeekerId = jobSeekerService.getJobSeekerId();

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
        Long jobSeekerId = jobSeekerService.getJobSeekerId();
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
        Long jobSeekerId = jobSeekerService.getJobSeekerId();
        return lambdaQuery()
                .eq(Skill::getJobSeekerId, jobSeekerId)
                .orderByDesc(Skill::getUpdatedAt)
                .list();
    }

    @Override
    public void deleteSkill(Long id) {
        Long jobSeekerId = jobSeekerService.getJobSeekerId();
        getOwnedSkill(id, jobSeekerId);
        skillMapper.deleteById(id);
    }

    private Skill getOwnedSkill(Long id, Long jobSeekerId) {
        Skill skill = skillMapper.selectById(id);
        if (skill == null) {
            throw new BusinessException(ErrorCode.SKILL_NOT_EXIST);
        }
        if (!skill.getJobSeekerId().equals(jobSeekerId)) {
            throw new BusinessException(ErrorCode.SKILL_NOT_BELONG_TO_USER);
        }
        return skill;
    }

    private void ensureSkillNotDuplicate(Long jobSeekerId, String skillName, Long excludeId) {
        if (!StringUtils.hasText(skillName)) {
            return;
        }
        long count = lambdaQuery()
                .eq(Skill::getJobSeekerId, jobSeekerId)
                .eq(Skill::getSkillName, skillName.trim())
                .ne(excludeId != null, Skill::getId, excludeId)
                .count();
        if (count > 0) {
            throw new BusinessException(ErrorCode.SKILL_ALREADY_EXIST);
        }
    }
}
