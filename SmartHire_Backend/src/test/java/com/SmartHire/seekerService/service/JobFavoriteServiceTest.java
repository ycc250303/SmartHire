package com.SmartHire.seekerService.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.SmartHire.common.api.HrApi;
import com.SmartHire.common.exception.enums.ErrorCode;
import com.SmartHire.common.exception.exception.BusinessException;
import com.SmartHire.hrService.dto.JobCardDTO;
import com.SmartHire.seekerService.mapper.JobFavoriteMapper;
import com.SmartHire.seekerService.model.JobFavorite;
import com.SmartHire.seekerService.service.impl.JobFavoriteServiceImpl;
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
 * JobFavoriteService 单元测试
 *
 * <p>测试目标：防止岗位收藏、重复检查等核心业务逻辑回归
 *
 * @author SmartHire Test Team
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("岗位收藏服务单元测试")
class JobFavoriteServiceTest {

  @Mock private JobFavoriteMapper jobFavoriteMapper;

  @Mock private JobSeekerService jobSeekerService;

  @Mock private HrApi hrApi;

  @Spy @InjectMocks private JobFavoriteServiceImpl jobFavoriteService;

  private static final Long TEST_JOB_SEEKER_ID = 456L;
  private static final Long TEST_JOB_ID = 100L;

  @BeforeEach
  void setUp() throws Exception {
    Field baseMapperField = ServiceImpl.class.getDeclaredField("baseMapper");
    baseMapperField.setAccessible(true);
    baseMapperField.set(jobFavoriteService, jobFavoriteMapper);

    try {
      Field mapperClassField = ServiceImpl.class.getDeclaredField("mapperClass");
      mapperClassField.setAccessible(true);
      mapperClassField.set(jobFavoriteService, JobFavoriteMapper.class);
    } catch (NoSuchFieldException e) {
      // 忽略
    }
  }

  private JobCardDTO createJobCard() {
    JobCardDTO jobCard = new JobCardDTO();
    jobCard.setJobId(TEST_JOB_ID);
    jobCard.setJobTitle("Java开发工程师");
    return jobCard;
  }

  private void mockLambdaQueryCount(long count) {
    LambdaQueryChainWrapper<JobFavorite> queryWrapper =
        new LambdaQueryChainWrapper<>(jobFavoriteMapper, JobFavorite.class);
    doReturn(queryWrapper).when(jobFavoriteService).lambdaQuery();
    when(jobFavoriteMapper.selectCount(any())).thenReturn(count);
  }

  @Test
  @DisplayName("TC-FAVOR-001: 岗位已收藏（重复收藏）")
  void testAddJobFavorite_AlreadyExists() {
    when(hrApi.getJobCardByJobId(TEST_JOB_ID)).thenReturn(createJobCard());
    when(jobSeekerService.getJobSeekerId()).thenReturn(TEST_JOB_SEEKER_ID);
    mockLambdaQueryCount(1);

    BusinessException exception =
        assertThrows(
            BusinessException.class,
            () -> {
              jobFavoriteService.addJobFavorite(TEST_JOB_ID);
            },
            "岗位已收藏应该抛出 BusinessException");

    assertEquals(
        ErrorCode.JOB_FAVORITE_ALREADY_EXISTS.getCode(),
        exception.getCode(),
        "异常错误码应该是 JOB_FAVORITE_ALREADY_EXISTS(1126)");
    assertEquals("该岗位已收藏，请勿重复收藏", exception.getMessage(), "异常消息应该正确");
    verify(jobFavoriteMapper, never()).insert(any(JobFavorite.class));
  }

  @Test
  @DisplayName("TC-FAVOR-002: 成功收藏岗位")
  void testAddJobFavorite_Success() {
    when(hrApi.getJobCardByJobId(TEST_JOB_ID)).thenReturn(createJobCard());
    when(jobSeekerService.getJobSeekerId()).thenReturn(TEST_JOB_SEEKER_ID);
    mockLambdaQueryCount(0);
    // 注意：业务代码使用 save() 方法，这是 MyBatis-Plus 的 ServiceImpl 提供的方法
    // save() 最终会调用 baseMapper.insert()，所以我们 mock insert() 来确保测试能够进行
    when(jobFavoriteMapper.insert(any(JobFavorite.class))).thenReturn(1);

    assertDoesNotThrow(
        () -> {
          jobFavoriteService.addJobFavorite(TEST_JOB_ID);
        },
        "收藏应该成功，不抛出异常");

    verify(jobSeekerService, times(1)).getJobSeekerId();
    // 验证 save() 被调用（业务代码使用 save() 方法）
    verify(jobFavoriteService, times(1)).save(any(JobFavorite.class));
    // 验证 insert() 被调用（save() 最终会调用 insert()）
    ArgumentCaptor<JobFavorite> captor = ArgumentCaptor.forClass(JobFavorite.class);
    verify(jobFavoriteMapper, times(1)).insert(captor.capture());
    JobFavorite inserted = captor.getValue();
    assertEquals(TEST_JOB_SEEKER_ID, inserted.getJobSeekerId(), "求职者ID应该正确设置");
    assertEquals(TEST_JOB_ID, inserted.getJobId(), "岗位ID应该正确设置");
    assertNotNull(inserted.getCreatedAt(), "创建时间应该被设置");
  }
}
