package com.SmartHire.seekerService.service;

import com.SmartHire.seekerService.dto.WorkExperienceDTO;
import com.SmartHire.seekerService.model.WorkExperience;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.validation.Valid;

import java.util.List;

/**
 * <p>
 * 工作经历表 服务类
 * </p>
 *
 * @author SmartHire Team
 * @since 2025-11-19
 */
public interface WorkExperienceService extends IService<WorkExperience> {
    /**
     * 添加工作/实习经历
     */
    void addWorkExperience(@Valid WorkExperienceDTO request);

    /**
     * 更新工作/实习经历
     */
    void updateWorkExperience(Long id, WorkExperienceDTO request);

    /**
     * 查询当前用户的所有工作/实习经历
     */
    List<WorkExperienceDTO> getWorkExperiences();

    /**
     * 删除工作/实习经历
     */
    void deleteWorkExperience(Long id);
}
