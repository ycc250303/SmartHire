package com.SmartHire.seekerService.service.seekerTableService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.SmartHire.common.exception.enums.ErrorCode;
import com.SmartHire.common.exception.exception.BusinessException;
import com.SmartHire.seekerService.dto.seekerTableDto.JobSeekerExpectationDTO;
import com.SmartHire.seekerService.mapper.JobSeekerExpectationMapper;
import com.SmartHire.seekerService.model.JobSeekerExpectation;
import com.SmartHire.seekerService.service.JobSeekerService;
import com.SmartHire.seekerService.service.impl.seekerTableImpl.JobSeekerExpectationServiceImpl;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.lang.reflect.Field;
import java.math.BigDecimal;
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
 * JobSeekerExpectationService 单元测试
 *
 * <p>测试目标：防止求职期望添加、上限校验等核心业务逻辑回归
 *
 * @author SmartHire Test Team
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("求职期望服务单元测试")
class JobSeekerExpectationServiceTest {

  @Mock private JobSeekerExpectationMapper jobSeekerExpectationMapper;

  @Mock private JobSeekerService jobSeekerService;

  @Spy @InjectMocks private JobSeekerExpectationServiceImpl jobSeekerExpectationService;

  private static final Long TEST_JOB_SEEKER_ID = 456L;
  private static final int MAX_EXPECTATION_COUNT = 5;

  @BeforeEach
  void setUp() throws Exception {
    Field baseMapperField = ServiceImpl.class.getDeclaredField("baseMapper");
    baseMapperField.setAccessible(true);
    baseMapperField.set(jobSeekerExpectationService, jobSeekerExpectationMapper);

    try {
      Field mapperClassField = ServiceImpl.class.getDeclaredField("mapperClass");
      mapperClassField.setAccessible(true);
      mapperClassField.set(jobSeekerExpectationService, JobSeekerExpectationMapper.class);
    } catch (NoSuchFieldException e) {
      // 忽略
    }
  }

  private JobSeekerExpectationDTO createTestExpectationDTO() {
    JobSeekerExpectationDTO dto = new JobSeekerExpectationDTO();
    dto.setExpectedPosition("Java开发");
    dto.setExpectedIndustry("互联网");
    dto.setWorkCity("北京");
    dto.setSalaryMin(new BigDecimal("15000"));
    dto.setSalaryMax(new BigDecimal("25000"));
    return dto;
  }

  private void mockLambdaQueryCount(long count) {
    LambdaQueryChainWrapper<JobSeekerExpectation> queryWrapper =
        new LambdaQueryChainWrapper<>(
            jobSeekerExpectationMapper, JobSeekerExpectation.class);
    doReturn(queryWrapper).when(jobSeekerExpectationService).lambdaQuery();
    when(jobSeekerExpectationMapper.selectCount(any())).thenReturn(count);
  }

  @Test
  @DisplayName("TC-EXPECT-001: 求职期望数量已达上限")
  void testAddJobSeekerExpectation_LimitExceeded() {
    JobSeekerExpectationDTO request = createTestExpectationDTO();
    when(jobSeekerService.getJobSeekerId()).thenReturn(TEST_JOB_SEEKER_ID);
    mockLambdaQueryCount(MAX_EXPECTATION_COUNT);

    BusinessException exception =
        assertThrows(
            BusinessException.class,
            () -> {
              jobSeekerExpectationService.addJobSeekerExpectation(request);
            },
            "求职期望数量已达上限应该抛出 BusinessException");

    assertEquals(
        ErrorCode.JOB_SEEKER_EXPECTATION_LIMIT_EXCEEDED.getCode(),
        exception.getCode(),
        "异常错误码应该是 JOB_SEEKER_EXPECTATION_LIMIT_EXCEEDED(1119)");
    assertEquals(
        "求职期望数量已达上限（最多5个）",
        exception.getMessage(),
        "异常消息应该正确");
    verify(jobSeekerExpectationMapper, never()).insert(any(JobSeekerExpectation.class));
  }

  @Test
  @DisplayName("TC-EXPECT-002: 成功添加求职期望")
  void testAddJobSeekerExpectation_Success() {
    JobSeekerExpectationDTO request = createTestExpectationDTO();
    when(jobSeekerService.getJobSeekerId()).thenReturn(TEST_JOB_SEEKER_ID);
    mockLambdaQueryCount(2);
    when(jobSeekerExpectationMapper.insert(any(JobSeekerExpectation.class))).thenReturn(1);

    assertDoesNotThrow(
        () -> {
          jobSeekerExpectationService.addJobSeekerExpectation(request);
        },
        "添加应该成功，不抛出异常");

    verify(jobSeekerService, times(1)).getJobSeekerId();
    ArgumentCaptor<JobSeekerExpectation> captor =
        ArgumentCaptor.forClass(JobSeekerExpectation.class);
    verify(jobSeekerExpectationMapper, times(1)).insert(captor.capture());
    JobSeekerExpectation inserted = captor.getValue();
    assertEquals(TEST_JOB_SEEKER_ID, inserted.getJobSeekerId(), "求职者ID应该正确设置");
    assertEquals("Java开发", inserted.getExpectedPosition(), "期望职位应该正确设置");
    assertNotNull(inserted.getCreatedAt(), "创建时间应该被设置");
  }
}

