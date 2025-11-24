package com.SmartHire.seekerService.service.impl;

import com.SmartHire.seekerService.dto.ResumeDTO;
import com.SmartHire.seekerService.mapper.ResumeMapper;
import com.SmartHire.seekerService.model.Resume;
import com.SmartHire.seekerService.service.ResumeService;
import com.SmartHire.shared.exception.enums.ErrorCode;
import com.SmartHire.shared.exception.exception.BusinessException;
import com.SmartHire.shared.utils.AliOssUtil;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * <p>
 * 简历基础表 服务实现类
 * </p>
 *
 * @author SmartHire Team
 * @since 2025-11-19
 */
@Slf4j
@Service
public class ResumeServiceImpl extends AbstractSeekerOwnedService<ResumeMapper, Resume> implements ResumeService {

    private static final int MAX_RESUME_COUNT = 5;
    private static final String RESUME_DIRECTORY_KEY = "resume";

    @Autowired
    private ResumeMapper resumeMapper;

    @Autowired
    private AliOssUtil aliOssUtil;

    @Override
    public void uploadResume(MultipartFile resumeFile) {
        Long jobSeekerId = currentSeekerId();

        long currentCount = lambdaQuery()
                .eq(Resume::getJobSeekerId, jobSeekerId)
                .count();
        if (currentCount >= MAX_RESUME_COUNT) {
            throw new BusinessException(ErrorCode.RESUME_LIMIT_EXCEEDED);
        }

        String resumeUrl = uploadResumeFile(resumeFile);

        Resume resume = new Resume();
        resume.setJobSeekerId(jobSeekerId);
        resume.setResumeName(extractFileDisplayName(resumeFile));
        resume.setPrivacyLevel((byte) 2);
        resume.setFileUrl(resumeUrl);
        resume.setCompleteness(0);
        Date now = new Date();
        resume.setCreatedAt(now);
        resume.setUpdatedAt(now);

        resumeMapper.insert(resume);
    }

    @Override
    public List<Resume> getResumes() {
        Long jobSeekerId = currentSeekerId();
        return lambdaQuery()
                .eq(Resume::getJobSeekerId, jobSeekerId)
                .orderByDesc(Resume::getUpdatedAt)
                .list();
    }

    @Override
    public void updateResume(Long id, ResumeDTO request) {
        Resume resume = getOwnedResume(id);

        boolean hasUpdate = false;

        if (request != null && StringUtils.hasText(request.getResumeName())) {
            resume.setResumeName(request.getResumeName().trim());
            hasUpdate = true;
        }
        if (request != null && request.getPrivacyLevel() != null) {
            resume.setPrivacyLevel(resolvePrivacyLevel(request.getPrivacyLevel()));
            hasUpdate = true;
        }

        if (!hasUpdate) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }

        resume.setUpdatedAt(new Date());
        resumeMapper.updateById(resume);
    }

    @Override
    public void deleteResume(Long id) {
        Resume resume = getOwnedResume(id);
        resumeMapper.deleteById(id);
        deleteStoredFile(resume.getFileUrl(), null);
    }

    private Resume getOwnedResume(Long id) {
        return requireOwnedEntity(id,
                resumeMapper::selectById,
                Resume::getJobSeekerId,
                ErrorCode.RESUME_NOT_EXIST,
                ErrorCode.RESUME_NOT_BELONG_TO_USER);
    }

    private String uploadResumeFile(MultipartFile resumeFile) {
        if (resumeFile == null || resumeFile.isEmpty()) {
            throw new BusinessException(ErrorCode.RESUME_FILE_EMPTY);
        }

        String originalFileName = resumeFile.getOriginalFilename();
        String extension = "";
        if (StringUtils.hasText(originalFileName) && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        String fileName = UUID.randomUUID() + extension;

        try (InputStream inputStream = resumeFile.getInputStream()) {
            return aliOssUtil.uploadFile(RESUME_DIRECTORY_KEY, fileName, inputStream);
        } catch (IOException | RuntimeException ex) {
            log.error("上传简历文件失败, originalFileName={}, generatedName={}", originalFileName, fileName, ex);
            throw new BusinessException(ErrorCode.RESUME_UPLOAD_FAILED);
        }
    }

    private String extractFileDisplayName(MultipartFile resumeFile) {
        String originalFileName = resumeFile.getOriginalFilename();
        if (StringUtils.hasText(originalFileName)) {
            return originalFileName;
        }
        return UUID.randomUUID() + ".resume";
    }

    private Byte resolvePrivacyLevel(Byte privacyLevel) {
        if (privacyLevel == null) {
            return 1;
        }
        if (privacyLevel < 1 || privacyLevel > 2) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        return privacyLevel;
    }

    private void deleteStoredFile(String fileUrl, String newFileUrl) {
        if (!StringUtils.hasText(fileUrl)) {
            return;
        }
        if (StringUtils.hasText(newFileUrl) && fileUrl.equals(newFileUrl)) {
            return;
        }
        String objectName = aliOssUtil.extractObjectName(fileUrl);
        if (!StringUtils.hasText(objectName)) {
            return;
        }
        boolean deleted = aliOssUtil.deleteFile(objectName);
        if (deleted) {
            log.info("删除附件简历文件成功, objectName={}", objectName);
        } else {
            log.warn("删除附件简历文件失败, objectName={}", objectName);
        }
    }
}
