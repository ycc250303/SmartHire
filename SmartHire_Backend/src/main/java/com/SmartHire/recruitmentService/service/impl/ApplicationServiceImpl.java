package com.SmartHire.recruitmentService.service.impl;

import com.SmartHire.common.api.HrApi;
import com.SmartHire.common.api.SeekerApi;
import com.SmartHire.common.api.UserAuthApi;
import com.SmartHire.common.exception.enums.ErrorCode;
import com.SmartHire.common.exception.exception.BusinessException;
import com.SmartHire.common.utils.JwtUtil;
import com.SmartHire.common.utils.SecurityContextUtil;
import com.SmartHire.recruitmentService.dto.ApplicationListDTO;
import com.SmartHire.recruitmentService.dto.ApplicationQueryDTO;
import com.SmartHire.recruitmentService.dto.SubmitResumeDTO;
import com.SmartHire.recruitmentService.mapper.ApplicationMapper;
import com.SmartHire.recruitmentService.model.Application;
import com.SmartHire.recruitmentService.service.ApplicationService;
import com.SmartHire.userAuthService.model.User;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.util.Date;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 投递/推荐记录表 服务实现类
 *
 * @author SmartHire Team
 * @since 2025-11-30
 */
@Slf4j
@Service
public class ApplicationServiceImpl extends ServiceImpl<ApplicationMapper, Application>
    implements ApplicationService {

  @Autowired
  private SeekerApi seekerApi;

  @Autowired
  private HrApi hrApi;

  @Autowired
  private UserAuthApi userAuthApi;

  @Autowired
  private JwtUtil jwtUtil;

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void submitResume(SubmitResumeDTO request) {
    // 参数校验
    if (request == null || request.getJobId() == null) {
      throw new BusinessException(ErrorCode.VALIDATION_ERROR);
    }

    Long jobId = request.getJobId();
    Long resumeId = request.getResumeId();

    // 获取当前用户ID（从JWT token中获取）
    Map<String, Object> map = SecurityContextUtil.getCurrentClaims();
    Long userId = jwtUtil.getUserIdFromToken(map);
    if (userId == null) {
      throw new BusinessException(ErrorCode.USER_ID_NOT_EXIST);
    }

    // 获取当前求职者ID
    Long seekerId = seekerApi.getJobSeekerIdByUserId(userId);

    // 如果提供了简历ID，则校验附件简历是否存在且属于当前用户
    if (resumeId != null) {
      if (!seekerApi.validateResumeOwnership(resumeId, seekerId)) {
        throw new BusinessException(ErrorCode.RESUME_NOT_BELONG_TO_USER);
      }
    }
    // 如果 resumeId 为 null，则投递在线简历（不需要额外校验）

    // 检查是否已投递过该职位（避免重复投递）
    long existingCount = lambdaQuery()
        .eq(Application::getJobId, jobId)
        .eq(Application::getJobSeekerId, seekerId)
        .count();
    if (existingCount > 0) {
      throw new BusinessException(ErrorCode.APPLICATION_ALREADY_EXISTS);
    }

    // 创建投递记录
    Application application = new Application();
    application.setJobId(jobId);
    application.setJobSeekerId(seekerId);
    application.setResumeId(resumeId); // 可能为null（在线简历）
    application.setInitiator((byte) 0); // 0-求职者投递
    application.setStatus((byte) 0); // 0-已投递/已推荐

    Date now = new Date();
    application.setCreatedAt(now);
    application.setUpdatedAt(now);

    // 保存投递记录
    boolean saved = this.save(application);
    if (!saved) {
      log.error("保存投递记录失败, jobId={}, seekerId={}, resumeId={}", jobId, seekerId, resumeId);
      throw new BusinessException(ErrorCode.SYSTEM_ERROR);
    }

    String resumeType = resumeId != null ? "附件简历" : "在线简历";
    log.info("投递{}成功, jobId={}, seekerId={}, resumeId={}", resumeType, jobId, seekerId, resumeId);

    // TODO：添加消息通知并发送给HR
  }

  /**
   * 获取当前登录HR的ID（hr_info表的id）
   */
  private Long getCurrentHrId() {
    Map<String, Object> map = SecurityContextUtil.getCurrentClaims();
    Long userId = jwtUtil.getUserIdFromToken(map);
    if (userId == null) {
      throw new BusinessException(ErrorCode.USER_ID_NOT_EXIST);
    }

    User user = userAuthApi.getUserById(userId);
    if (user.getUserType() != 2) {
      throw new BusinessException(ErrorCode.USER_NOT_HR);
    }

    Long hrId = hrApi.getHrIdByUserId(userId);
    if (hrId == null) {
      throw new BusinessException(ErrorCode.HR_NOT_EXIST);
    }

    return hrId;
  }

  /**
   * 校验投递是否属于当前HR
   */
  private Application validateApplicationOwnership(Long applicationId, Long hrId) {
    Application application = getById(applicationId);
    if (application == null) {
      throw new BusinessException(ErrorCode.APPLICATION_NOT_EXIST);
    }

    Long jobHrId = hrApi.getHrIdByJobId(application.getJobId());
    if (jobHrId == null) {
      throw new BusinessException(ErrorCode.JOB_NOT_EXIST);
    }

    if (!jobHrId.equals(hrId)) {
      throw new BusinessException(ErrorCode.APPLICATION_NOT_BELONG_TO_HR);
    }

    return application;
  }

  @Override
  public Page<ApplicationListDTO> getApplicationList(ApplicationQueryDTO queryDTO) {
    Long hrId = getCurrentHrId();
    int pageNum = queryDTO.getPageNum() == null ? 1 : queryDTO.getPageNum();
    int pageSize = queryDTO.getPageSize() == null ? 10 : queryDTO.getPageSize();

    Page<ApplicationListDTO> page = new Page<>(pageNum, pageSize);
    String keyword = StringUtils.hasText(queryDTO.getKeyword()) ? queryDTO.getKeyword().trim() : null;

    Byte status = queryDTO.getStatus() != null ? queryDTO.getStatus().byteValue() : null;
    return baseMapper.selectApplicationList(page, hrId, queryDTO.getJobId(), status, keyword);
  }

  @Override
  public ApplicationListDTO getApplicationDetail(Long applicationId) {
    Long hrId = getCurrentHrId();
    ApplicationListDTO detail = baseMapper.selectApplicationDetail(applicationId, hrId);
    if (detail == null) {
      throw new BusinessException(ErrorCode.APPLICATION_NOT_EXIST);
    }
    return detail;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void updateApplicationStatus(Long applicationId, Byte status) {
    if (status == null || status < 0 || status > 6) {
      throw new BusinessException(ErrorCode.VALIDATION_ERROR);
    }

    Long hrId = getCurrentHrId();
    Application application = validateApplicationOwnership(applicationId, hrId);

    Date now = new Date();
    application.setStatus(status);
    application.setUpdatedAt(now);

    updateById(application);
  }
}
