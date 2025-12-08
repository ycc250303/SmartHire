package com.SmartHire.seekerService.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.SmartHire.common.auth.UserContext;
import com.SmartHire.common.exception.enums.ErrorCode;
import com.SmartHire.common.exception.exception.BusinessException;
import com.SmartHire.seekerService.dto.SeekerDTO;
import com.SmartHire.seekerService.dto.SeekerInfoDTO;
import com.SmartHire.seekerService.mapper.JobSeekerMapper;
import com.SmartHire.seekerService.model.JobSeeker;
import com.SmartHire.seekerService.service.impl.JobSeekerServiceImpl;
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
 * JobSeekerService 单元测试
 *
 * <p>
 * 测试目标：防止求职者信息注册、更新、查询等核心业务逻辑回归
 *
 * <p>
 * 测试原则：
 * <ul>
 * <li>使用 Mockito mock 外部依赖（UserContext、数据库Mapper）</li>
 * <li>保留真实业务逻辑测试（数据转换、字段映射、业务规则校验）</li>
 * <li>使用 Arrange / Act / Assert 三段式结构</li>
 * </ul>
 *
 * <p>
 * 注意：本测试基于 API 文档和测试用例矩阵推导，不依赖具体实现细节
 *
 * @author SmartHire Test Team
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("求职者信息服务单元测试")
class JobSeekerServiceTest {

    @Mock
    private JobSeekerMapper jobSeekerMapper;

    @Mock
    private UserContext userContext;

    @Spy
    @InjectMocks
    private JobSeekerServiceImpl jobSeekerService;

    // ==================== 测试数据常量 ====================
    // 说明：使用常量定义测试数据，避免硬编码，提高可维护性

    /** 测试用户ID */
    private static final Long TEST_USER_ID = 123L;

    /** 测试求职者ID */
    private static final Long TEST_JOB_SEEKER_ID = 456L;

    /** 测试真实姓名 */
    private static final String TEST_REAL_NAME = "张三";

    /** 测试出生日期字符串 */
    private static final String TEST_BIRTH_DATE_STR = "2000-01-01";

    /** 测试当前城市 */
    private static final String TEST_CURRENT_CITY = "北京";

    /** 测试求职状态：1-在职，正在找工作 */
    private static final Integer TEST_JOB_STATUS = 1;

    /** 日期格式化器 */
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    // ==================== 测试初始化 ====================

    @BeforeEach
    void setUp() throws Exception {
        // 使用反射设置 MyBatis-Plus ServiceImpl 的 baseMapper 字段
        // 这是必需的，因为 @InjectMocks 不会自动注入 ServiceImpl 的 protected baseMapper 字段
        Field baseMapperField = ServiceImpl.class.getDeclaredField("baseMapper");
        baseMapperField.setAccessible(true);
        baseMapperField.set(jobSeekerService, jobSeekerMapper);

        // 由于 MyBatis-Plus 的 lambdaQuery() 会尝试从 baseMapper 获取 Mapper 类
        // 而 Mockito 的 mock 对象不是真正的 MyBatis 代理，所以我们需要通过反射设置 mapperClass
        // 这样 lambdaQuery() 就能正确创建 LambdaQueryChainWrapper 实例
        try {
            Field mapperClassField = ServiceImpl.class.getDeclaredField("mapperClass");
            mapperClassField.setAccessible(true);
            mapperClassField.set(jobSeekerService, JobSeekerMapper.class);
        } catch (NoSuchFieldException e) {
            // mapperClass 字段可能不存在，忽略
        }
    }

    // ==================== 测试辅助方法 ====================

    /**
     * 创建测试用的 SeekerDTO
     *
     * @return SeekerDTO 对象
     */
    private SeekerDTO createTestSeekerDTO() throws ParseException {
        SeekerDTO dto = new SeekerDTO();
        dto.setRealName(TEST_REAL_NAME);
        dto.setBirthDate(DATE_FORMAT.parse(TEST_BIRTH_DATE_STR));
        dto.setCurrentCity(TEST_CURRENT_CITY);
        dto.setJobStatus(TEST_JOB_STATUS);
        return dto;
    }

    /**
     * 创建已存在的 JobSeeker 实体（用于模拟已注册场景）
     *
     * @return JobSeeker 对象
     */
    private JobSeeker createExistingJobSeeker() throws ParseException {
        JobSeeker jobSeeker = new JobSeeker();
        jobSeeker.setId(TEST_JOB_SEEKER_ID);
        jobSeeker.setUserId(TEST_USER_ID);
        jobSeeker.setRealName(TEST_REAL_NAME);
        jobSeeker.setBirthDate(DATE_FORMAT.parse(TEST_BIRTH_DATE_STR));
        jobSeeker.setCurrentCity(TEST_CURRENT_CITY);
        jobSeeker.setJobStatus(TEST_JOB_STATUS);
        return jobSeeker;
    }

    /**
     * Mock lambdaQuery() 调用，使用真正的 LambdaQueryChainWrapper 实例
     *
     * @param result 查询结果（null 表示不存在）
     */
    private void mockLambdaQueryResult(JobSeeker result) {
        // 创建真正的 LambdaQueryChainWrapper 实例
        LambdaQueryChainWrapper<JobSeeker> queryWrapper = new LambdaQueryChainWrapper<>(jobSeekerMapper,
                JobSeeker.class);

        // Mock lambdaQuery() 返回真正的 LambdaQueryChainWrapper 实例
        doReturn(queryWrapper)
                .when(jobSeekerService)
                .lambdaQuery();

        // Mock baseMapper.selectOne() 返回指定结果
        // 这是 lambdaQuery().eq(...).one() 最终调用的方法
        when(jobSeekerMapper.selectOne(any())).thenReturn(result);
    }

    /**
     * Mock getJobSeekerEntity() 方法，返回指定的 JobSeeker 对象
     * 注意：这是私有方法，需要通过反射来 mock
     *
     * @param result 查询结果（null 表示不存在）
     */
    private void mockGetJobSeekerEntity(JobSeeker result) {
        // 由于 getJobSeekerEntity() 是私有方法，我们无法直接 mock
        // 但我们可以通过 mock lambdaQuery() 的最终调用（baseMapper.selectOne）来实现
        mockLambdaQueryResult(result);
    }

    // ==================== P0 关键业务测试用例 ====================

    @Test
    @DisplayName("TC-SEEKER-001: 成功注册求职者信息")
    void testRegisterSeeker_Success() throws ParseException {
        // Arrange: 准备测试数据
        SeekerDTO request = createTestSeekerDTO();

        // Mock UserContext: 返回当前用户ID
        when(userContext.getCurrentUserId()).thenReturn(TEST_USER_ID);

        // Mock lambdaQuery() 调用链：返回 null（表示未注册）
        // 注意：registerSeeker() 直接调用 lambdaQuery()，我们需要 mock baseMapper.selectOne()
        mockLambdaQueryResult(null);

        // Mock 数据库插入: 返回插入的行数
        when(jobSeekerMapper.insert(any(JobSeeker.class))).thenReturn(1);

        // Act: 执行注册操作
        assertDoesNotThrow(
                () -> {
                    jobSeekerService.registerSeeker(request);
                },
                "注册应该成功，不抛出异常");

        // Assert: 验证业务流程
        // 1. 验证获取当前用户ID被调用
        verify(userContext, times(1)).getCurrentUserId();

        // 2. 验证检查是否已注册（通过 lambdaQuery 查询）
        verify(jobSeekerMapper, atLeastOnce()).selectOne(any());

        // Assert: 验证业务数据完整性（防止字段缺失或错误）
        ArgumentCaptor<JobSeeker> jobSeekerCaptor = ArgumentCaptor.forClass(JobSeeker.class);
        verify(jobSeekerMapper, times(1)).insert(jobSeekerCaptor.capture());
        JobSeeker insertedJobSeeker = jobSeekerCaptor.getValue();

        // 验证所有字段都正确设置
        assertEquals(TEST_USER_ID, insertedJobSeeker.getUserId(), "用户ID应该正确设置");
        assertEquals(TEST_REAL_NAME, insertedJobSeeker.getRealName(), "真实姓名应该正确设置");
        assertEquals(
                DATE_FORMAT.parse(TEST_BIRTH_DATE_STR),
                insertedJobSeeker.getBirthDate(),
                "出生日期应该正确设置");
        assertEquals(TEST_CURRENT_CITY, insertedJobSeeker.getCurrentCity(), "当前城市应该正确设置");
        assertEquals(TEST_JOB_STATUS, insertedJobSeeker.getJobStatus(), "求职状态应该正确设置");
        assertNotNull(insertedJobSeeker.getCreatedAt(), "创建时间应该被设置");
        assertNotNull(insertedJobSeeker.getUpdatedAt(), "更新时间应该被设置");
    }

    @Test
    @DisplayName("TC-SEEKER-002: 用户ID不存在")
    void testRegisterSeeker_UserIdNotExist() throws ParseException {
        // Arrange: 准备测试数据
        SeekerDTO request = createTestSeekerDTO();

        // Mock UserContext: 用户ID不存在，抛出异常
        when(userContext.getCurrentUserId())
                .thenThrow(new BusinessException(ErrorCode.USER_ID_NOT_EXIST));

        // Act & Assert: 执行并验证抛出用户ID不存在异常
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> {
                    jobSeekerService.registerSeeker(request);
                },
                "用户ID不存在应该抛出 BusinessException");

        // Assert: 验证异常
        assertEquals(
                ErrorCode.USER_ID_NOT_EXIST.getCode(),
                exception.getCode(),
                "异常错误码应该是 USER_ID_NOT_EXIST(1010)");
        assertEquals(
                "用户ID不存在",
                exception.getMessage(),
                "异常消息应该正确");

        // 验证求职者信息未被保存到数据库
        verify(jobSeekerMapper, never()).insert(any(JobSeeker.class));
    }

    @Test
    @DisplayName("TC-SEEKER-003: 成功更新求职者信息")
    void testUpdateSeekerInfo_Success() throws ParseException {
        // Arrange: 准备测试数据
        SeekerDTO request = new SeekerDTO();
        request.setRealName("李四");
        request.setBirthDate(DATE_FORMAT.parse("1999-05-15"));
        request.setCurrentCity("上海");
        request.setJobStatus(0);

        // Mock UserContext: 返回当前用户ID
        when(userContext.getCurrentUserId()).thenReturn(TEST_USER_ID);

        // Mock getJobSeekerEntity() 方法：返回已存在的求职者信息
        JobSeeker existingJobSeeker = createExistingJobSeeker();
        mockGetJobSeekerEntity(existingJobSeeker);

        // Mock 数据库更新: 返回更新的行数
        when(jobSeekerMapper.updateById(any(JobSeeker.class))).thenReturn(1);

        // Act: 执行更新操作
        assertDoesNotThrow(
                () -> {
                    jobSeekerService.updateSeekerInfo(request);
                },
                "更新应该成功，不抛出异常");

        // Assert: 验证业务流程
        // 1. 验证获取当前用户ID被调用
        verify(userContext, times(1)).getCurrentUserId();

        // 2. 验证查询求职者信息
        verify(jobSeekerMapper, atLeastOnce()).selectOne(any());

        // Assert: 验证业务数据完整性（防止字段缺失或错误）
        ArgumentCaptor<JobSeeker> jobSeekerCaptor = ArgumentCaptor.forClass(JobSeeker.class);
        verify(jobSeekerMapper, times(1)).updateById(jobSeekerCaptor.capture());
        JobSeeker updatedJobSeeker = jobSeekerCaptor.getValue();

        // 验证所有字段都正确更新
        assertEquals("李四", updatedJobSeeker.getRealName(), "真实姓名应该正确更新");
        assertEquals(
                DATE_FORMAT.parse("1999-05-15"),
                updatedJobSeeker.getBirthDate(),
                "出生日期应该正确更新");
        assertEquals("上海", updatedJobSeeker.getCurrentCity(), "当前城市应该正确更新");
        assertEquals(0, updatedJobSeeker.getJobStatus(), "求职状态应该正确更新");
        assertNotNull(updatedJobSeeker.getUpdatedAt(), "更新时间应该被设置");
    }

    @Test
    @DisplayName("TC-SEEKER-004: 求职者信息不存在")
    void testUpdateSeekerInfo_SeekerNotExist() throws ParseException {
        // Arrange: 准备测试数据
        SeekerDTO request = createTestSeekerDTO();

        // Mock UserContext: 返回当前用户ID
        when(userContext.getCurrentUserId()).thenReturn(TEST_USER_ID);

        // Mock getJobSeekerEntity() 方法：返回 null（表示不存在）
        mockGetJobSeekerEntity(null);

        // Act & Assert: 执行并验证抛出求职者信息不存在异常
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> {
                    jobSeekerService.updateSeekerInfo(request);
                },
                "求职者信息不存在应该抛出 BusinessException");

        // Assert: 验证异常
        assertEquals(
                ErrorCode.SEEKER_NOT_EXIST.getCode(),
                exception.getCode(),
                "异常错误码应该是 SEEKER_NOT_EXIST(1102)");
        assertEquals(
                "求职者信息不存在",
                exception.getMessage(),
                "异常消息应该正确");

        // 验证求职者信息未被更新
        verify(jobSeekerMapper, never()).updateById(any(JobSeeker.class));
    }

    @Test
    @DisplayName("TC-SEEKER-005: 成功获取求职者信息")
    void testGetSeekerInfo_Success() throws ParseException {
        // Arrange: 准备测试数据
        JobSeeker existingJobSeeker = createExistingJobSeeker();

        // Mock UserContext: 返回当前用户ID
        when(userContext.getCurrentUserId()).thenReturn(TEST_USER_ID);

        // Mock getJobSeekerEntity() 方法：返回已存在的求职者信息
        mockGetJobSeekerEntity(existingJobSeeker);

        // Act: 执行查询操作
        SeekerInfoDTO result = assertDoesNotThrow(
                () -> {
                    return jobSeekerService.getSeekerInfo();
                },
                "查询应该成功，不抛出异常");

        // Assert: 验证业务流程
        // 1. 验证获取当前用户ID被调用
        verify(userContext, times(1)).getCurrentUserId();

        // 2. 验证查询求职者信息
        verify(jobSeekerMapper, atLeastOnce()).selectOne(any());

        // Assert: 验证返回数据完整性（防止字段缺失或映射错误）
        assertNotNull(result, "返回的 DTO 不应该为 null");
        assertEquals(TEST_REAL_NAME, result.getRealName(), "真实姓名应该正确映射");
        assertEquals(
                DATE_FORMAT.parse(TEST_BIRTH_DATE_STR),
                result.getBirthDate(),
                "出生日期应该正确映射");
        assertEquals(TEST_CURRENT_CITY, result.getCurrentCity(), "当前城市应该正确映射");
        assertEquals(TEST_JOB_STATUS, result.getJobStatus(), "求职状态应该正确映射");
    }

    @Test
    @DisplayName("获取求职者信息 - 求职者信息不存在")
    void testGetSeekerInfo_SeekerNotExist() {
        // Arrange: 准备测试数据
        // Mock UserContext: 返回当前用户ID
        when(userContext.getCurrentUserId()).thenReturn(TEST_USER_ID);

        // Mock getJobSeekerEntity() 方法：返回 null（表示不存在）
        mockGetJobSeekerEntity(null);

        // Act & Assert: 执行并验证抛出求职者信息不存在异常
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> {
                    jobSeekerService.getSeekerInfo();
                },
                "求职者信息不存在应该抛出 BusinessException");

        // Assert: 验证异常
        assertEquals(
                ErrorCode.SEEKER_NOT_EXIST.getCode(),
                exception.getCode(),
                "异常错误码应该是 SEEKER_NOT_EXIST(1102)");
    }

    @Test
    @DisplayName("获取求职者ID - 成功")
    void testGetJobSeekerId_Success() throws ParseException {
        // Arrange: 准备测试数据
        JobSeeker existingJobSeeker = createExistingJobSeeker();

        // Mock UserContext: 返回当前用户ID
        when(userContext.getCurrentUserId()).thenReturn(TEST_USER_ID);

        // Mock lambdaQuery() 调用链：返回已存在的求职者信息
        // 注意：getJobSeekerId() 直接调用 lambdaQuery()，我们需要 mock baseMapper.selectOne()
        mockLambdaQueryResult(existingJobSeeker);

        // Act: 执行查询操作
        Long result = assertDoesNotThrow(
                () -> {
                    return jobSeekerService.getJobSeekerId();
                },
                "查询应该成功，不抛出异常");

        // Assert: 验证返回结果
        assertEquals(TEST_JOB_SEEKER_ID, result, "返回的求职者ID应该正确");
        verify(userContext, times(1)).getCurrentUserId();
    }

    @Test
    @DisplayName("获取求职者ID - 求职者信息不存在")
    void testGetJobSeekerId_SeekerNotExist() {
        // Arrange: 准备测试数据
        // Mock UserContext: 返回当前用户ID
        when(userContext.getCurrentUserId()).thenReturn(TEST_USER_ID);

        // Mock lambdaQuery() 调用链：返回 null（表示不存在）
        // 注意：getJobSeekerId() 直接调用 lambdaQuery()，我们需要 mock baseMapper.selectOne()
        mockLambdaQueryResult(null);

        // Act & Assert: 执行并验证抛出求职者信息不存在异常
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> {
                    jobSeekerService.getJobSeekerId();
                },
                "求职者信息不存在应该抛出 BusinessException");

        // Assert: 验证异常
        assertEquals(
                ErrorCode.SEEKER_NOT_EXIST.getCode(),
                exception.getCode(),
                "异常错误码应该是 SEEKER_NOT_EXIST(1102)");
    }
}
