package com.SmartHire.seekerService.service.seekerTableService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.SmartHire.common.exception.enums.ErrorCode;
import com.SmartHire.common.exception.exception.BusinessException;
import com.SmartHire.seekerService.dto.seekerTableDto.EducationExperienceDTO;
import com.SmartHire.seekerService.mapper.EducationExperienceMapper;
import com.SmartHire.seekerService.model.EducationExperience;
import com.SmartHire.seekerService.service.JobSeekerService;
import com.SmartHire.seekerService.service.impl.seekerTableImpl.EducationExperienceServiceImpl;
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
 * EducationExperienceService 单元测试
 *
 * <p>测试目标：防止教育经历添加、更新、删除等核心业务逻辑回归
 *
 * <p>测试原则：
 * <ul>
 *   <li>使用 Mockito mock 外部依赖（JobSeekerService、数据库Mapper）</li>
 *   <li>保留真实业务逻辑测试（数据转换、字段映射、业务规则校验）</li>
 *   <li>使用 Arrange / Act / Assert 三段式结构</li>
 * </ul>
 *
 * <p>注意：本测试基于 API 文档和测试用例矩阵推导，不依赖具体实现细节
 *
 * @author SmartHire Test Team
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("教育经历服务单元测试")
class EducationExperienceServiceTest {

  @Mock private EducationExperienceMapper educationExperienceMapper;

  @Mock private JobSeekerService jobSeekerService;

  @Spy @InjectMocks private EducationExperienceServiceImpl educationExperienceService;

  // ==================== 测试数据常量 ====================

  /** 测试求职者ID */
  private static final Long TEST_JOB_SEEKER_ID = 456L;

  /** 测试其他用户的求职者ID */
  private static final Long OTHER_JOB_SEEKER_ID = 999L;

  /** 测试教育经历ID */
  private static final Long TEST_EDUCATION_ID = 1L;

  /** 测试其他用户的教育经历ID */
  private static final Long OTHER_EDUCATION_ID = 999L;

  /** 测试学校名称 */
  private static final String TEST_SCHOOL_NAME = "清华大学";

  /** 测试专业 */
  private static final String TEST_MAJOR = "计算机";

  /** 测试学历：2-本科 */
  private static final Integer TEST_EDUCATION = 2;

  /** 测试开始年份 */
  private static final String TEST_START_YEAR = "2018";

  /** 测试结束年份 */
  private static final String TEST_END_YEAR = "2022";

  // ==================== 测试初始化 ====================

  @BeforeEach
  void setUp() throws Exception {
    // 使用反射设置 MyBatis-Plus ServiceImpl 的 baseMapper 字段
    Field baseMapperField = ServiceImpl.class.getDeclaredField("baseMapper");
    baseMapperField.setAccessible(true);
    baseMapperField.set(educationExperienceService, educationExperienceMapper);

    // 设置 mapperClass 字段（如果存在）
    try {
      Field mapperClassField = ServiceImpl.class.getDeclaredField("mapperClass");
      mapperClassField.setAccessible(true);
      mapperClassField.set(educationExperienceService, EducationExperienceMapper.class);
    } catch (NoSuchFieldException e) {
      // mapperClass 字段可能不存在，忽略
    }
  }

  // ==================== 测试辅助方法 ====================

  /**
   * 创建测试用的 EducationExperienceDTO
   *
   * @return EducationExperienceDTO 对象
   */
  private EducationExperienceDTO createTestEducationDTO() {
    EducationExperienceDTO dto = new EducationExperienceDTO();
    dto.setSchoolName(TEST_SCHOOL_NAME);
    dto.setMajor(TEST_MAJOR);
    dto.setEducation(TEST_EDUCATION);
    dto.setStartYear(TEST_START_YEAR);
    dto.setEndYear(TEST_END_YEAR);
    return dto;
  }

  /**
   * 创建已存在的 EducationExperience 实体（属于当前用户）
   *
   * @return EducationExperience 对象
   */
  private EducationExperience createExistingEducation() {
    EducationExperience education = new EducationExperience();
    education.setId(TEST_EDUCATION_ID);
    education.setJobSeekerId(TEST_JOB_SEEKER_ID);
    education.setSchoolName(TEST_SCHOOL_NAME);
    education.setMajor(TEST_MAJOR);
    education.setEducation(TEST_EDUCATION);
    education.setStartYear(LocalDate.of(2018, 1, 1));
    education.setEndYear(LocalDate.of(2022, 1, 1));
    return education;
  }

  /**
   * 创建属于其他用户的 EducationExperience 实体
   *
   * @return EducationExperience 对象
   */
  private EducationExperience createOtherUserEducation() {
    EducationExperience education = new EducationExperience();
    education.setId(OTHER_EDUCATION_ID);
    education.setJobSeekerId(OTHER_JOB_SEEKER_ID);
    education.setSchoolName("其他学校");
    education.setMajor("其他专业");
    education.setEducation(1);
    return education;
  }

  /**
   * Mock lambdaQuery().count() 调用
   *
   * @param count 返回的计数
   */
  private void mockLambdaQueryCount(long count) {
    LambdaQueryChainWrapper<EducationExperience> queryWrapper =
        new LambdaQueryChainWrapper<>(educationExperienceMapper, EducationExperience.class);

    doReturn(queryWrapper).when(educationExperienceService).lambdaQuery();

    when(educationExperienceMapper.selectCount(any())).thenReturn(count);
  }

  // ==================== P0 关键业务测试用例 ====================

  @Test
  @DisplayName("TC-EDU-001: 教育经历不属于当前用户（越权删除）")
  void testDeleteEducationExperience_NotBelongToUser() {
    // Arrange: 准备测试数据
    // Mock JobSeekerService: 返回当前用户的求职者ID
    when(jobSeekerService.getJobSeekerId()).thenReturn(TEST_JOB_SEEKER_ID);

    // Mock 数据库查询: 教育经历存在，但属于其他用户
    EducationExperience otherUserEducation = createOtherUserEducation();
    when(educationExperienceMapper.selectById(OTHER_EDUCATION_ID))
        .thenReturn(otherUserEducation);

    // Act & Assert: 执行并验证抛出所有权异常
    BusinessException exception =
        assertThrows(
            BusinessException.class,
            () -> {
              educationExperienceService.deleteEducationExperience(OTHER_EDUCATION_ID);
            },
            "教育经历不属于当前用户应该抛出 BusinessException");

    // Assert: 验证异常
    assertEquals(
        ErrorCode.EDUCATION_EXPERIENCE_NOT_BELONG_TO_USER.getCode(),
        exception.getCode(),
        "异常错误码应该是 EDUCATION_EXPERIENCE_NOT_BELONG_TO_USER(1105)");
    assertEquals(
        "教育经历不属于当前用户",
        exception.getMessage(),
        "异常消息应该正确");

    // 验证教育经历未被删除
    verify(educationExperienceMapper, never()).deleteById(any());
  }

  // ==================== P1 次要业务测试用例 ====================

  @Test
  @DisplayName("TC-EDU-002: 成功添加教育经历")
  void testAddEducationExperience_Success() {
    // Arrange: 准备测试数据
    EducationExperienceDTO request = createTestEducationDTO();

    // Mock JobSeekerService: 返回当前用户的求职者ID
    when(jobSeekerService.getJobSeekerId()).thenReturn(TEST_JOB_SEEKER_ID);

    // Mock lambdaQuery() 调用：检查重复（返回空列表，表示不重复）
    mockLambdaQueryCount(0);

    // Mock 数据库插入: 返回插入的行数
    when(educationExperienceMapper.insert(any(EducationExperience.class))).thenReturn(1);

    // Act: 执行添加操作
    assertDoesNotThrow(
        () -> {
          educationExperienceService.addEducationExperience(request);
        },
        "添加应该成功，不抛出异常");

    // Assert: 验证业务流程
    // 1. 验证获取当前求职者ID被调用
    // 注意：addEducationExperience() 方法中 currentSeekerId() 只调用1次（在方法开始时获取 jobSeekerId）
    // 与 updateEducationExperience() 不同，add 方法不需要所有权校验（因为是新增，不存在需要校验的实体）
    verify(jobSeekerService, times(1)).getJobSeekerId();

    // Assert: 验证业务数据完整性（防止字段缺失或错误）
    ArgumentCaptor<EducationExperience> educationCaptor =
        ArgumentCaptor.forClass(EducationExperience.class);
    verify(educationExperienceMapper, times(1)).insert(educationCaptor.capture());
    EducationExperience insertedEducation = educationCaptor.getValue();

    // 验证所有字段都正确设置
    assertEquals(TEST_JOB_SEEKER_ID, insertedEducation.getJobSeekerId(), "求职者ID应该正确设置");
    assertEquals(TEST_SCHOOL_NAME, insertedEducation.getSchoolName(), "学校名称应该正确设置");
    assertEquals(TEST_MAJOR, insertedEducation.getMajor(), "专业应该正确设置");
    assertEquals(TEST_EDUCATION, insertedEducation.getEducation(), "学历应该正确设置");
    assertNotNull(insertedEducation.getCreatedAt(), "创建时间应该被设置");
    assertNotNull(insertedEducation.getUpdatedAt(), "更新时间应该被设置");
  }

  @Test
  @DisplayName("TC-EDU-003: 成功修改教育经历")
  void testUpdateEducationExperience_Success() {
    // Arrange: 准备测试数据
    EducationExperienceDTO request = new EducationExperienceDTO();
    request.setSchoolName("北京大学");
    request.setMajor("软件工程");
    request.setEducation(2);
    request.setStartYear("2019");
    request.setEndYear("2023");

    // Mock JobSeekerService: 返回当前用户的求职者ID
    when(jobSeekerService.getJobSeekerId()).thenReturn(TEST_JOB_SEEKER_ID);

    // Mock 数据库查询: 教育经历存在且属于当前用户
    EducationExperience existingEducation = createExistingEducation();
    when(educationExperienceMapper.selectById(TEST_EDUCATION_ID))
        .thenReturn(existingEducation);

    // Mock lambdaQuery() 调用：检查重复（返回空列表，表示不重复）
    mockLambdaQueryCount(0);

    // Mock 数据库更新: 返回更新的行数
    when(educationExperienceMapper.updateById(any(EducationExperience.class))).thenReturn(1);

    // Act: 执行更新操作
    assertDoesNotThrow(
        () -> {
          educationExperienceService.updateEducationExperience(TEST_EDUCATION_ID, request);
        },
        "更新应该成功，不抛出异常");

    // Assert: 验证业务流程
    // 1. 验证获取当前求职者ID被调用
    // 注意：updateEducationExperience() 方法中 currentSeekerId() 会被调用2次：
    // - 第一次：在方法开始时获取 jobSeekerId
    // - 第二次：在 getOwnedEducation() -> requireOwnedEntity() -> currentSeekerId() 中进行所有权校验
    verify(jobSeekerService, times(2)).getJobSeekerId();

    // 2. 验证查询教育经历
    verify(educationExperienceMapper, times(1)).selectById(TEST_EDUCATION_ID);

    // Assert: 验证业务数据完整性（防止字段缺失或错误）
    ArgumentCaptor<EducationExperience> educationCaptor =
        ArgumentCaptor.forClass(EducationExperience.class);
    verify(educationExperienceMapper, times(1)).updateById(educationCaptor.capture());
    EducationExperience updatedEducation = educationCaptor.getValue();

    // 验证所有字段都正确更新
    assertEquals("北京大学", updatedEducation.getSchoolName(), "学校名称应该正确更新");
    assertEquals("软件工程", updatedEducation.getMajor(), "专业应该正确更新");
    assertNotNull(updatedEducation.getUpdatedAt(), "更新时间应该被设置");
  }
}

