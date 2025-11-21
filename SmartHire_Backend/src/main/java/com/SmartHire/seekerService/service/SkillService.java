package com.SmartHire.seekerService.service;

import com.SmartHire.seekerService.dto.SkillDTO;
import com.SmartHire.seekerService.model.Skill;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.validation.Valid;
import java.util.List;

public interface SkillService extends IService<Skill> {
    void addSkill(@Valid SkillDTO request);

    void updateSkill(Long id, SkillDTO request);

    List<Skill> getSkills();

    void deleteSkill(Long id);
}
