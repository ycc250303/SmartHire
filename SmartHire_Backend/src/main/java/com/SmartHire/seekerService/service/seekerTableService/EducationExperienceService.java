package com.SmartHire.seekerService.service.seekerTableService;

import com.SmartHire.seekerService.dto.seekerTableDto.EducationExperienceDTO;
import com.SmartHire.seekerService.model.EducationExperience;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.validation.Valid;

/**
 * <p>
 * 教育经历表 服务类
 * </p>
 *
 * @author SmartHire Team
 * @since 2025-11-19
 */
public interface EducationExperienceService extends IService<EducationExperience> {

    /**
     * 添加教育经历
     *
     * @param request 添加教育经历请求参数
     */
    void addEducationExperience(@Valid EducationExperienceDTO request);

    /**
     * 修改教育经历
     *
     * @param id      教育经历ID
     * @param request 修改教育经历请求参数（部分字段，至少一项不为空）
     */
    void updateEducationExperience(Long id, EducationExperienceDTO request);

    /**
     * 获取当前用户的所有教育经历
     *
     * @return 教育经历列表
     */
    java.util.List<EducationExperience> getEducationExperiences();

    /**
     * 删除教育经历
     *
     * @param id 教育经历ID
     */
    void deleteEducationExperience(Long id);
}
