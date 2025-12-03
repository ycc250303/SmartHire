package com.SmartHire.seekerService.service.seekerTableService;

import com.SmartHire.seekerService.dto.seekerTableDto.ResumeDTO;
import com.SmartHire.seekerService.model.Resume;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

/**
 * 简历基础表 服务类
 *
 * @author SmartHire Team
 * @since 2025-11-19
 */
public interface ResumeService extends IService<Resume> {

  /**
   * 上传附件简历，仅需要文件本身，名称取自原文件名
   *
   * @param resumeFile 文件
   */
  void uploadResume(MultipartFile resumeFile);

  /** 查询当前求职者的所有附件简历 */
  List<Resume> getResumes();

  /**
   * 更新附件简历元数据（只能修改名称和隐私级别）
   *
   * @param id 简历ID
   * @param request 元数据
   */
  void updateResume(Long id, ResumeDTO request);

  /**
   * 删除附件简历
   *
   * @param id 简历ID
   */
  void deleteResume(Long id);
}
