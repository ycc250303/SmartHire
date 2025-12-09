package com.SmartHire.seekerService.service.seekerTableService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.SmartHire.common.exception.enums.ErrorCode;
import com.SmartHire.common.exception.exception.BusinessException;
import com.SmartHire.seekerService.dto.seekerTableDto.ProjectExperienceDTO;
import com.SmartHire.seekerService.mapper.ProjectExperienceMapper;
import com.SmartHire.seekerService.model.ProjectExperience;
import com.SmartHire.seekerService.service.JobSeekerService;
import com.SmartHire.seekerService.service.impl.seekerTableImpl.ProjectExperienceServiceImpl;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.lang.reflect.Field;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * ProjectExperienceService 单元测试
 *
 * <p>
 * 测试目标：防止项目经历添加、更新、删除等核心业务逻辑回归
 *
 * @author SmartHire Test Team
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("项目经历服务单元测试")
class ProjectExperienceServiceTest {

    @Mock
    private ProjectExperienceMapper projectExperienceMapper;

    @Mock
    private JobSeekerService jobSeekerService;

    @Spy
    @InjectMocks
    private ProjectExperienceServiceImpl projectExperienceService;

    private static final Long TEST_JOB_SEEKER_ID = 456L;
    private static final Long OTHER_JOB_SEEKER_ID = 999L;
    private static final Long TEST_PROJECT_ID = 1L;
    private static final Long OTHER_PROJECT_ID = 999L;

    @BeforeEach
    void setUp() throws Exception {
        Field baseMapperField = ServiceImpl.class.getDeclaredField("baseMapper");
        baseMapperField.setAccessible(true);
        baseMapperField.set(projectExperienceService, projectExperienceMapper);

        try {
            Field mapperClassField = ServiceImpl.class.getDeclaredField("mapperClass");
            mapperClassField.setAccessible(true);
            mapperClassField.set(projectExperienceService, ProjectExperienceMapper.class);
        } catch (NoSuchFieldException e) {
            // 忽略
        }
    }

    private ProjectExperienceDTO createTestProjectDTO() {
        ProjectExperienceDTO dto = new ProjectExperienceDTO();
        dto.setProjectName("智能招聘系统");
        dto.setProjectRole("后端开发");
        dto.setDescription("负责API设计");
        dto.setResponsibility("接口开发");
        dto.setStartMonth("2024-01");
        dto.setEndMonth("2024-06");
        return dto;
    }

    private ProjectExperience createExistingProject() {
        ProjectExperience project = new ProjectExperience();
        project.setId(TEST_PROJECT_ID);
        project.setJobSeekerId(TEST_JOB_SEEKER_ID);
        project.setProjectName("智能招聘系统");
        project.setProjectRole("后端开发");
        project.setStartDate(LocalDate.of(2024, 1, 1));
        project.setEndDate(LocalDate.of(2024, 6, 1));
        return project;
    }

    private ProjectExperience createOtherUserProject() {
        ProjectExperience project = new ProjectExperience();
        project.setId(OTHER_PROJECT_ID);
        project.setJobSeekerId(OTHER_JOB_SEEKER_ID);
        project.setProjectName("其他项目");
        return project;
    }

    private void mockLambdaQueryCount(long count) {
        LambdaQueryChainWrapper<ProjectExperience> queryWrapper = new LambdaQueryChainWrapper<>(projectExperienceMapper,
                ProjectExperience.class);
        doReturn(queryWrapper).when(projectExperienceService).lambdaQuery();
        when(projectExperienceMapper.selectCount(any())).thenReturn(count);
    }

    @Test
    @DisplayName("TC-PROJ-001: 项目经历不属于当前用户（越权删除）")
    void testDeleteProjectExperience_NotBelongToUser() {
        when(jobSeekerService.getJobSeekerId()).thenReturn(TEST_JOB_SEEKER_ID);
        ProjectExperience otherUserProject = createOtherUserProject();
        when(projectExperienceMapper.selectById(OTHER_PROJECT_ID)).thenReturn(otherUserProject);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> {
                    projectExperienceService.deleteProjectExperience(OTHER_PROJECT_ID);
                },
                "项目经历不属于当前用户应该抛出 BusinessException");

        assertEquals(
                ErrorCode.PROJECT_EXPERIENCE_NOT_BELONG_TO_USER.getCode(),
                exception.getCode(),
                "异常错误码应该是 PROJECT_EXPERIENCE_NOT_BELONG_TO_USER(1108)");
        verify(projectExperienceMapper, never()).deleteById(any());
    }

    @Test
    @DisplayName("TC-PROJ-002: 成功添加项目经历")
    void testAddProjectExperience_Success() {
        ProjectExperienceDTO request = createTestProjectDTO();
        when(jobSeekerService.getJobSeekerId()).thenReturn(TEST_JOB_SEEKER_ID);
        mockLambdaQueryCount(2);
        when(projectExperienceMapper.insert(any(ProjectExperience.class))).thenReturn(1);

        assertDoesNotThrow(
                () -> {
                    projectExperienceService.addProjectExperience(request);
                },
                "添加应该成功，不抛出异常");

        verify(jobSeekerService, times(1)).getJobSeekerId();
        ArgumentCaptor<ProjectExperience> captor = ArgumentCaptor.forClass(ProjectExperience.class);
        verify(projectExperienceMapper, times(1)).insert(captor.capture());
        ProjectExperience inserted = captor.getValue();
        assertEquals(TEST_JOB_SEEKER_ID, inserted.getJobSeekerId(), "求职者ID应该正确设置");
        assertEquals("智能招聘系统", inserted.getProjectName(), "项目名称应该正确设置");
        assertNotNull(inserted.getCreatedAt(), "创建时间应该被设置");
    }

    @Test
    @DisplayName("TC-PROJ-003: 成功修改项目经历")
    void testUpdateProjectExperience_Success() {
        ProjectExperienceDTO request = new ProjectExperienceDTO();
        request.setProjectName("更新后的项目");
        request.setProjectRole("技术负责人");

        when(jobSeekerService.getJobSeekerId()).thenReturn(TEST_JOB_SEEKER_ID);
        ProjectExperience existing = createExistingProject();
        when(projectExperienceMapper.selectById(TEST_PROJECT_ID)).thenReturn(existing);
        when(projectExperienceMapper.updateById(any(ProjectExperience.class))).thenReturn(1);

        assertDoesNotThrow(
                () -> {
                    projectExperienceService.updateProjectExperience(TEST_PROJECT_ID, request);
                },
                "更新应该成功，不抛出异常");

        verify(jobSeekerService, times(1)).getJobSeekerId();
        ArgumentCaptor<ProjectExperience> captor = ArgumentCaptor.forClass(ProjectExperience.class);
        verify(projectExperienceMapper, times(1)).updateById(captor.capture());
        ProjectExperience updated = captor.getValue();
        assertEquals("更新后的项目", updated.getProjectName(), "项目名称应该正确更新");
        assertNotNull(updated.getUpdatedAt(), "更新时间应该被设置");
    }
}
