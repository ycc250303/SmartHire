package com.SmartHire.seekerService.service.seekerTableService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.SmartHire.common.exception.enums.ErrorCode;
import com.SmartHire.common.exception.exception.BusinessException;
import com.SmartHire.seekerService.dto.seekerTableDto.SkillDTO;
import com.SmartHire.seekerService.mapper.SkillMapper;
import com.SmartHire.seekerService.model.Skill;
import com.SmartHire.seekerService.service.JobSeekerService;
import com.SmartHire.seekerService.service.impl.seekerTableImpl.SkillServiceImpl;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.lang.reflect.Field;
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
 * SkillService 单元测试
 *
 * <p>测试目标：防止技能添加、更新、删除等核心业务逻辑回归
 *
 * <p>测试原则：
 *
 * <ul>
 *   <li>使用 Mockito mock 外部依赖（JobSeekerService、数据库Mapper）
 *   <li>保留真实业务逻辑测试（数据转换、字段映射、业务规则校验）
 *   <li>使用 Arrange / Act / Assert 三段式结构
 * </ul>
 *
 * <p>注意：本测试基于 API 文档和测试用例矩阵推导，不依赖具体实现细节
 *
 * @author SmartHire Test Team
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("技能服务单元测试")
class SkillServiceTest {

  @Mock private SkillMapper skillMapper;

  @Mock private JobSeekerService jobSeekerService;

  @Spy @InjectMocks private SkillServiceImpl skillService;

  // ==================== 测试数据常量 ====================

  /** 测试求职者ID */
  private static final Long TEST_JOB_SEEKER_ID = 456L;

  /** 测试其他用户的求职者ID */
  private static final Long OTHER_JOB_SEEKER_ID = 999L;

  /** 测试技能ID */
  private static final Long TEST_SKILL_ID = 1L;

  /** 测试其他用户的技能ID */
  private static final Long OTHER_SKILL_ID = 999L;

  /** 测试技能名称 */
  private static final String TEST_SKILL_NAME = "Spring Boot";

  /** 测试熟练度：1-熟练 */
  private static final Integer TEST_LEVEL = 1;

  /** 技能数量上限 */
  private static final int MAX_SKILL_COUNT = 20;

  // ==================== 测试初始化 ====================

  @BeforeEach
  void setUp() throws Exception {
    // 使用反射设置 MyBatis-Plus ServiceImpl 的 baseMapper 字段
    Field baseMapperField = ServiceImpl.class.getDeclaredField("baseMapper");
    baseMapperField.setAccessible(true);
    baseMapperField.set(skillService, skillMapper);

    // 设置 mapperClass 字段（如果存在）
    try {
      Field mapperClassField = ServiceImpl.class.getDeclaredField("mapperClass");
      mapperClassField.setAccessible(true);
      mapperClassField.set(skillService, SkillMapper.class);
    } catch (NoSuchFieldException e) {
      // mapperClass 字段可能不存在，忽略
    }
  }

  // ==================== 测试辅助方法 ====================

  /**
   * 创建测试用的 SkillDTO
   *
   * @return SkillDTO 对象
   */
  private SkillDTO createTestSkillDTO() {
    SkillDTO dto = new SkillDTO();
    dto.setSkillName(TEST_SKILL_NAME);
    dto.setLevel(TEST_LEVEL);
    return dto;
  }

  /**
   * 创建已存在的 Skill 实体（属于当前用户）
   *
   * @return Skill 对象
   */
  private Skill createExistingSkill() {
    Skill skill = new Skill();
    skill.setId(TEST_SKILL_ID);
    skill.setJobSeekerId(TEST_JOB_SEEKER_ID);
    skill.setSkillName(TEST_SKILL_NAME);
    skill.setLevel(TEST_LEVEL.byteValue());
    return skill;
  }

  /**
   * 创建属于其他用户的 Skill 实体
   *
   * @return Skill 对象
   */
  private Skill createOtherUserSkill() {
    Skill skill = new Skill();
    skill.setId(OTHER_SKILL_ID);
    skill.setJobSeekerId(OTHER_JOB_SEEKER_ID);
    skill.setSkillName("其他技能");
    skill.setLevel((byte) 2);
    return skill;
  }

  /**
   * Mock lambdaQuery().count() 调用
   *
   * @param count 返回的计数
   */
  private void mockLambdaQueryCount(long count) {
    LambdaQueryChainWrapper<Skill> queryWrapper =
        new LambdaQueryChainWrapper<>(skillMapper, Skill.class);

    doReturn(queryWrapper).when(skillService).lambdaQuery();

    when(skillMapper.selectCount(any())).thenReturn(count);
  }

  // ==================== P0 关键业务测试用例 ====================

  @Test
  @DisplayName("TC-SKILL-001: 技能数量已达上限")
  void testAddSkill_SkillLimitExceeded() {
    // Arrange: 准备测试数据
    SkillDTO request = createTestSkillDTO();

    // Mock JobSeekerService: 返回当前用户的求职者ID
    when(jobSeekerService.getJobSeekerId()).thenReturn(TEST_JOB_SEEKER_ID);

    // Mock lambdaQuery().count() 调用：技能数量已达上限
    mockLambdaQueryCount(MAX_SKILL_COUNT);

    // Act & Assert: 执行并验证抛出技能数量已达上限异常
    BusinessException exception =
        assertThrows(
            BusinessException.class,
            () -> {
              skillService.addSkill(request);
            },
            "技能数量已达上限应该抛出 BusinessException");

    // Assert: 验证异常
    assertEquals(
        ErrorCode.SKILL_LIMIT_EXCEEDED.getCode(),
        exception.getCode(),
        "异常错误码应该是 SKILL_LIMIT_EXCEEDED(1113)");
    assertEquals("技能数量已达上限", exception.getMessage(), "异常消息应该正确");

    // 验证技能未被保存到数据库
    verify(skillMapper, never()).insert(any(Skill.class));
  }

  @Test
  @DisplayName("TC-SKILL-002: 技能不属于当前用户（越权删除）")
  void testDeleteSkill_NotBelongToUser() {
    // Arrange: 准备测试数据
    // Mock JobSeekerService: 返回当前用户的求职者ID
    when(jobSeekerService.getJobSeekerId()).thenReturn(TEST_JOB_SEEKER_ID);

    // Mock 数据库查询: 技能存在，但属于其他用户
    Skill otherUserSkill = createOtherUserSkill();
    when(skillMapper.selectById(OTHER_SKILL_ID)).thenReturn(otherUserSkill);

    // Act & Assert: 执行并验证抛出所有权异常
    BusinessException exception =
        assertThrows(
            BusinessException.class,
            () -> {
              skillService.deleteSkill(OTHER_SKILL_ID);
            },
            "技能不属于当前用户应该抛出 BusinessException");

    // Assert: 验证异常
    assertEquals(
        ErrorCode.SKILL_NOT_BELONG_TO_USER.getCode(),
        exception.getCode(),
        "异常错误码应该是 SKILL_NOT_BELONG_TO_USER(1112)");
    assertEquals("技能不属于当前用户", exception.getMessage(), "异常消息应该正确");

    // 验证技能未被删除
    verify(skillMapper, never()).deleteById(any());
  }

  // ==================== P1 次要业务测试用例 ====================

  @Test
  @DisplayName("TC-SKILL-003: 成功添加技能")
  void testAddSkill_Success() {
    // Arrange: 准备测试数据
    SkillDTO request = createTestSkillDTO();

    // Mock JobSeekerService: 返回当前用户的求职者ID
    when(jobSeekerService.getJobSeekerId()).thenReturn(TEST_JOB_SEEKER_ID);

    // Mock lambdaQuery().count() 调用：
    // 1. 第一次：检查技能数量上限（返回5，未达上限）
    // 2. 第二次：在 ensureSkillNotDuplicate() 中检查重复（返回0，表示不重复）
    LambdaQueryChainWrapper<Skill> queryWrapper =
        new LambdaQueryChainWrapper<>(skillMapper, Skill.class);
    doReturn(queryWrapper).when(skillService).lambdaQuery();
    // 使用 thenReturn() 链式调用，第一次返回5（检查上限），第二次返回0（检查重复）
    when(skillMapper.selectCount(any())).thenReturn(5L, 0L);

    // Mock 数据库插入: 返回插入的行数
    when(skillMapper.insert(any(Skill.class))).thenReturn(1);

    // Act: 执行添加操作
    assertDoesNotThrow(
        () -> {
          skillService.addSkill(request);
        },
        "添加应该成功，不抛出异常");

    // Assert: 验证业务流程
    // 1. 验证获取当前求职者ID被调用
    // 注意：addSkill() 方法中 currentSeekerId() 只调用1次（在方法开始时获取 jobSeekerId）
    // 与 updateSkill() 不同，add 方法不需要所有权校验（因为是新增，不存在需要校验的实体）
    verify(jobSeekerService, times(1)).getJobSeekerId();

    // Assert: 验证业务数据完整性（防止字段缺失或错误）
    ArgumentCaptor<Skill> skillCaptor = ArgumentCaptor.forClass(Skill.class);
    verify(skillMapper, times(1)).insert(skillCaptor.capture());
    Skill insertedSkill = skillCaptor.getValue();

    // 验证所有字段都正确设置
    assertEquals(TEST_JOB_SEEKER_ID, insertedSkill.getJobSeekerId(), "求职者ID应该正确设置");
    assertEquals(TEST_SKILL_NAME, insertedSkill.getSkillName(), "技能名称应该正确设置");
    assertEquals(TEST_LEVEL.byteValue(), insertedSkill.getLevel(), "熟练度应该正确设置");
    assertNotNull(insertedSkill.getCreatedAt(), "创建时间应该被设置");
    assertNotNull(insertedSkill.getUpdatedAt(), "更新时间应该被设置");
  }

  @Test
  @DisplayName("TC-SKILL-004: 成功修改技能")
  void testUpdateSkill_Success() {
    // Arrange: 准备测试数据
    SkillDTO request = new SkillDTO();
    request.setSkillName("Spring Cloud");
    request.setLevel(2);

    // Mock JobSeekerService: 返回当前用户的求职者ID
    when(jobSeekerService.getJobSeekerId()).thenReturn(TEST_JOB_SEEKER_ID);

    // Mock 数据库查询: 技能存在且属于当前用户
    Skill existingSkill = createExistingSkill();
    when(skillMapper.selectById(TEST_SKILL_ID)).thenReturn(existingSkill);

    // Mock lambdaQuery().count() 调用：检查重复（返回0，表示不重复）
    mockLambdaQueryCount(0);

    // Mock 数据库更新: 返回更新的行数
    when(skillMapper.updateById(any(Skill.class))).thenReturn(1);

    // Act: 执行更新操作
    assertDoesNotThrow(
        () -> {
          skillService.updateSkill(TEST_SKILL_ID, request);
        },
        "更新应该成功，不抛出异常");

    // Assert: 验证业务流程
    // 1. 验证获取当前求职者ID被调用
    // 注意：updateSkill() 方法中 currentSeekerId() 会被调用2次：
    // - 第一次：在方法开始时获取 jobSeekerId
    // - 第二次：在 getOwnedSkill() -> requireOwnedEntity() -> currentSeekerId() 中进行所有权校验
    verify(jobSeekerService, times(2)).getJobSeekerId();

    // 2. 验证查询技能
    verify(skillMapper, times(1)).selectById(TEST_SKILL_ID);

    // Assert: 验证业务数据完整性（防止字段缺失或错误）
    ArgumentCaptor<Skill> skillCaptor = ArgumentCaptor.forClass(Skill.class);
    verify(skillMapper, times(1)).updateById(skillCaptor.capture());
    Skill updatedSkill = skillCaptor.getValue();

    // 验证所有字段都正确更新
    assertEquals("Spring Cloud", updatedSkill.getSkillName(), "技能名称应该正确更新");
    assertEquals(Integer.valueOf(2).byteValue(), updatedSkill.getLevel(), "熟练度应该正确更新");
    assertNotNull(updatedSkill.getUpdatedAt(), "更新时间应该被设置");
  }
}
