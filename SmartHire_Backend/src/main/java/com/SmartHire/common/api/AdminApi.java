package com.SmartHire.common.api;

import com.SmartHire.common.dto.adminDto.ReportSubmitDTO;

public interface AdminApi {
    /**
     * 提交举报
     * @param submitDTO 举报信息
     * @param reporterId 举报人ID
     * @return 是否成功
     */
    boolean submitReport(ReportSubmitDTO submitDTO, Long reporterId);
}
