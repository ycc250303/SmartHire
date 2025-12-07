package com.SmartHire.adminService.exception;

import com.SmartHire.common.exception.exception.BaseException;

/**
 * 管理员服务异常
 *
 * @author SmartHire Team
 * @since 2025-12-06
 */
public class AdminServiceException extends BaseException {

    public AdminServiceException(com.SmartHire.common.exception.enums.ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public AdminServiceException(com.SmartHire.common.exception.enums.ErrorCode errorCode) {
        super(errorCode);
    }

    /**
     * 用户不存在异常
     */
    public static AdminServiceException userNotFound(Long userId) {
        return new AdminServiceException(com.SmartHire.common.exception.enums.ErrorCode.ADMIN_USER_NOT_FOUND, "用户不存在: " + userId);
    }

    /**
     * 用户已封禁异常
     */
    public static AdminServiceException userAlreadyBanned(Long userId) {
        return new AdminServiceException(com.SmartHire.common.exception.enums.ErrorCode.ADMIN_USER_ALREADY_BANNED, "用户已被封禁: " + userId);
    }

    /**
     * 用户未封禁异常
     */
    public static AdminServiceException userNotBanned(Long userId) {
        return new AdminServiceException(com.SmartHire.common.exception.enums.ErrorCode.ADMIN_USER_NOT_BANNED, "用户当前未被封禁: " + userId);
    }

    /**
     * 不能封禁管理员异常
     */
    public static AdminServiceException cannotBanAdmin() {
        return new AdminServiceException(com.SmartHire.common.exception.enums.ErrorCode.ADMIN_CANNOT_BAN_ADMIN, "不能封禁管理员账户");
    }

    /**
     * 不能修改管理员状态异常
     */
    public static AdminServiceException cannotModifyAdminStatus() {
        return new AdminServiceException(com.SmartHire.common.exception.enums.ErrorCode.ADMIN_CANNOT_MODIFY_ADMIN_STATUS, "不能修改管理员账户状态");
    }

    /**
     * 无效的封禁类型异常
     */
    public static AdminServiceException invalidBanType(String banType) {
        return new AdminServiceException(com.SmartHire.common.exception.enums.ErrorCode.ADMIN_INVALID_BAN_TYPE, "无效的封禁类型: " + banType);
    }

    /**
     * 封禁天数必须大于0异常
     */
    public static AdminServiceException banDaysMustBePositive() {
        return new AdminServiceException(com.SmartHire.common.exception.enums.ErrorCode.ADMIN_BAN_DAYS_MUST_BE_POSITIVE, "封禁天数必须大于0");
    }

    /**
     * 操作失败异常
     */
    public static AdminServiceException operationFailed(String operation) {
        return new AdminServiceException(com.SmartHire.common.exception.enums.ErrorCode.ADMIN_OPERATION_FAILED, operation + "失败");
    }
}