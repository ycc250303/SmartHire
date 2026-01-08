package com.SmartHire.recruitmentService.service.impl;

import com.SmartHire.common.api.ApplicationApi;
import com.SmartHire.recruitmentService.model.Application;
import com.SmartHire.recruitmentService.service.SeekerApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** 投递记录服务API实现类 */
@Service
public class ApplicationApiImpl implements ApplicationApi {

  @Autowired
  private SeekerApplicationService seekerApplicationService;

  @Override
  public Application getApplicationById(Long applicationId) {
    return seekerApplicationService.getById(applicationId);
  }

  @Override
  public boolean existsApplication(Long applicationId) {
    return seekerApplicationService.getById(applicationId) != null;
  }

  @Override
  public Long getConversationIdByApplicationId(Long applicationId) {
    return seekerApplicationService.getConversationIdByApplicationId(applicationId);
  }
}
