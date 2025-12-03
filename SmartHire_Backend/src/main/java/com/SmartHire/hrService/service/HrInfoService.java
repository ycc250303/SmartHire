package com.SmartHire.hrService.service;

import com.SmartHire.hrService.dto.HrInfoDTO;
import com.SmartHire.hrService.dto.HrInfoUpdateDTO;
import com.SmartHire.hrService.model.HrInfo;
import com.baomidou.mybatisplus.extension.service.IService;

/** HR信息服务接口 */
public interface HrInfoService extends IService<HrInfo> {

  /**
   * 获取当前登录HR的信息
   *
   * @return HR信息DTO
   */
  HrInfoDTO getHrInfo();

  /**
   * 新增当前登录HR的信息（若已存在则进行更新）
   *
   * @param updateDTO HR信息
   */
  void createHrInfo(HrInfoUpdateDTO updateDTO);

  /**
   * 更新当前登录HR的信息
   *
   * @param updateDTO 更新DTO
   */
  void updateHrInfo(HrInfoUpdateDTO updateDTO);
}
