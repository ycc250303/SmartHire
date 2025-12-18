package com.SmartHire.seekerService.service.seekerTableService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.SmartHire.common.exception.enums.ErrorCode;
import com.SmartHire.common.exception.exception.BusinessException;
import com.SmartHire.seekerService.dto.seekerTableDto.WorkExperienceDTO;
import com.SmartHire.seekerService.mapper.WorkExperienceMapper;
import com.SmartHire.seekerService.model.WorkExperience;
import com.SmartHire.seekerService.service.JobSeekerService;
import com.SmartHire.seekerService.service.impl.seekerTableImpl.WorkExperienceServiceImpl;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
 * WorkExperienceService 单元测试
 *
 * <p>测试目标：防止工作经历添加、更新、删除等核心业务逻辑回归
 *
 * @author SmartHire Test Team
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("工作经历服务单元测试")
class WorkExperienceServiceTest {

  @Mock private WorkExperienceMapper workExperienceMapper;

  @Mock private JobSeekerService jobSeekerService;

  @Spy @InjectMocks private WorkExperienceServiceImpl workExperienceService;

  private static final Long TEST_JOB_SEEKER_ID = 456L;
  private static final Long OTHER_JOB_SEEKER_ID = 999L;
  private static final Long TEST_WORK_ID = 1L;
  private static final Long OTHER_WORK_ID = 999L;
  private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM");

  @BeforeEach
  void setUp() throws Exception {
    Field baseMapperField = ServiceImpl.class.getDeclaredField("baseMapper");
    baseMapperField.setAccessible(true);
    baseMapperField.set(workExperienceService, workExperienceMapper);

    try {
      Field mapperClassField = ServiceImpl.class.getDeclaredField("mapperClass");
      mapperClassField.setAccessible(true);
      mapperClassField.set(workExperienceService, WorkExperienceMapper.class);
    } catch (NoSuchFieldException e) {
      // 忽略
    }
  }

  private WorkExperienceDTO createTestWorkDTO() throws ParseException {
    WorkExperienceDTO dto = new WorkExperienceDTO();
    dto.setCompanyName("ABC公司");
    dto.setPosition("Java工程师");
    dto.setStartMonth("2022-01");
    dto.setEndMonth("2024-01");
    dto.setDescription("负责后端开发");
    dto.setAchievements("优化性能30%");
    dto.setIsInternship(false);
    return dto;
  }

  private WorkExperience createExistingWork() throws ParseException {
    WorkExperience work = new WorkExperience();
    work.setId(TEST_WORK_ID);
    work.setJobSeekerId(TEST_JOB_SEEKER_ID);
    work.setCompanyName("ABC公司");
    work.setPosition("Java工程师");
    work.setStartDate(DATE_FORMAT.parse("2022-01"));
    work.setEndDate(DATE_FORMAT.parse("2024-01"));
    work.setIsInternship(false);
    return work;
  }

  private WorkExperience createOtherUserWork() {
    WorkExperience work = new WorkExperience();
    work.setId(OTHER_WORK_ID);
    work.setJobSeekerId(OTHER_JOB_SEEKER_ID);
    work.setCompanyName("其他公司");
    return work;
  }

  private void mockLambdaQueryCount(long count) {
    LambdaQueryChainWrapper<WorkExperience> queryWrapper =
        new LambdaQueryChainWrapper<>(workExperienceMapper, WorkExperience.class);
    doReturn(queryWrapper).when(workExperienceService).lambdaQuery();
    when(workExperienceMapper.selectCount(any())).thenReturn(count);
  }

  @Test
  @DisplayName("TC-WORK-001: 工作经历不属于当前用户（越权删除）")
  void testDeleteWorkExperience_NotBelongToUser() {
    when(jobSeekerService.getJobSeekerId()).thenReturn(TEST_JOB_SEEKER_ID);
    WorkExperience otherUserWork = createOtherUserWork();
    when(workExperienceMapper.selectById(OTHER_WORK_ID)).thenReturn(otherUserWork);

    BusinessException exception =
        assertThrows(
            BusinessException.class,
            () -> {
              workExperienceService.deleteWorkExperience(OTHER_WORK_ID);
            },
            "工作经历不属于当前用户应该抛出 BusinessException");

    assertEquals(
        ErrorCode.WORK_EXPERIENCE_NOT_BELONG_TO_USER.getCode(),
        exception.getCode(),
        "异常错误码应该是 WORK_EXPERIENCE_NOT_BELONG_TO_USER(1110)");
    verify(workExperienceMapper, never()).deleteById(any());
  }

  @Test
  @DisplayName("TC-WORK-002: 成功添加工作经历")
  void testAddWorkExperience_Success() throws ParseException {
    WorkExperienceDTO request = createTestWorkDTO();
    when(jobSeekerService.getJobSeekerId()).thenReturn(TEST_JOB_SEEKER_ID);
    mockLambdaQueryCount(2);
    when(workExperienceMapper.insert(any(WorkExperience.class))).thenReturn(1);

    assertDoesNotThrow(
        () -> {
          workExperienceService.addWorkExperience(request);
        },
        "添加应该成功，不抛出异常");

    verify(jobSeekerService, times(1)).getJobSeekerId();
    ArgumentCaptor<WorkExperience> captor = ArgumentCaptor.forClass(WorkExperience.class);
    verify(workExperienceMapper, times(1)).insert(captor.capture());
    WorkExperience inserted = captor.getValue();
    assertEquals(TEST_JOB_SEEKER_ID, inserted.getJobSeekerId(), "求职者ID应该正确设置");
    assertEquals("ABC公司", inserted.getCompanyName(), "公司名称应该正确设置");
    assertNotNull(inserted.getCreatedAt(), "创建时间应该被设置");
  }

  @Test
  @DisplayName("TC-WORK-003: 成功修改工作经历")
  void testUpdateWorkExperience_Success() throws ParseException {
    WorkExperienceDTO request = new WorkExperienceDTO();
    request.setPosition("高级工程师");
    request.setEndMonth("至今");
    request.setDescription("负责架构设计");

    when(jobSeekerService.getJobSeekerId()).thenReturn(TEST_JOB_SEEKER_ID);
    WorkExperience existing = createExistingWork();
    when(workExperienceMapper.selectById(TEST_WORK_ID)).thenReturn(existing);
    when(workExperienceMapper.updateById(any(WorkExperience.class))).thenReturn(1);

    assertDoesNotThrow(
        () -> {
          workExperienceService.updateWorkExperience(TEST_WORK_ID, request);
        },
        "更新应该成功，不抛出异常");

    verify(jobSeekerService, times(1)).getJobSeekerId();
    ArgumentCaptor<WorkExperience> captor = ArgumentCaptor.forClass(WorkExperience.class);
    verify(workExperienceMapper, times(1)).updateById(captor.capture());
    WorkExperience updated = captor.getValue();
    assertEquals("高级工程师", updated.getPosition(), "职位应该正确更新");
    assertNotNull(updated.getUpdatedAt(), "更新时间应该被设置");
  }
}
