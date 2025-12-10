package com.SmartHire.seekerService.service.seekerTableService;

import com.SmartHire.seekerService.dto.seekerTableDto.SkillDTO;
import com.SmartHire.seekerService.model.Skill;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.validation.Valid;
import java.util.List;

public interface SkillService extends IService<Skill> {
  void addSkill(@Valid SkillDTO request);

  void updateSkill(Long id, SkillDTO request);

  List<SkillDTO> getSkills();

  void deleteSkill(Long id);
}
