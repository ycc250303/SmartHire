package com.SmartHire.seekerService.service.seekerTableService;

import com.SmartHire.seekerService.dto.seekerTableDto.ProjectExperienceDTO;
import com.SmartHire.seekerService.model.ProjectExperience;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

/**
 * <p>
 * 项目经历表 服务类
 * </p>
 *
 * @author SmartHire Team
 * @since 2025-11-19
 */
public interface ProjectExperienceService extends IService<ProjectExperience> {
    /**
     * 添加项目经历
     *
     * @param request 请求参数
     */
    void addProjectExperience(ProjectExperienceDTO request);

    /**
     * 更新项目经历
     *
     * @param id      项目经历ID
     * @param request 请求参数
     */
    void updateProjectExperience(Long id, ProjectExperienceDTO request);

    /**
     * 获取当前用户项目经历
     *
     * @return 项目经历列表
     */
    List<ProjectExperienceDTO> getProjectExperiences();

    /**
     * 删除项目经历
     *
     * @param id 项目经历ID
     */
    void deleteProjectExperience(Long id);
}
