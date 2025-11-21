package com.SmartHire.shared.exception.enums;

public enum ErrorCode {
    VALIDATION_ERROR(1, "参数校验异常"),
    // 用户登录相关错误码 (1000-1999)
    // 用户注册相关错误码
    USER_AUTH_USER_HAS_EXISTED(1001, "用户名已存在"),
    EMAIL_CODE_SEND_FAILED(1002, "验证码发送失败"),
    EMAIL_CODE_INVALID(1003, "验证码无效或已过期"),
    EMAIL_CODE_SEND_TOO_FREQUENT(1004, "验证码发送过于频繁，请稍后再试"),
    EMAIL_FORMAT_INVALID(1005, "邮箱格式不正确"),
    EMAIL_ALREADY_REGISTERED(1006, "邮箱已被注册"),
    USER_AUTH_USER_NOT_EXIST(1007, "用户名不存在"),
    USER_AUTH_USER_PASSWORD_ERROR(1008, "密码输入错误"),
    USER_AUTH_PHONE_HAS_EXISTED(1009, "手机号已存在"),
    USER_ID_NOT_EXIST(1010, "用户ID不存在，请先注册用户"),
    TOKEN_IS_NULL(1011, "JWT Token 为空"),
    TOKEN_IS_INVALID(1012, "JWT Token 无效"),
    USER_NOT_LOGIN(1013, "用户未登录"),
    // 求职者相关错误码 (1100-1199)
    SEEKER_ALREADY_REGISTERED(1101, "该用户已注册求职者信息，请勿重复注册"),
    SEEKER_NOT_EXIST(1102, "求职者信息不存在"),
    USER_NOT_SEEKER(1103, "用户身份不是求职者"),
    EDUCATION_EXPERIENCE_NOT_EXIST(1104, "教育经历不存在"),
    EDUCATION_EXPERIENCE_NOT_BELONG_TO_USER(1105, "教育经历不属于当前用户"),
    EDUCATION_EXPERIENCE_ALREADY_EXIST(1106, "存在相同的教育经历"),
    PROJECT_EXPERIENCE_NOT_EXIST(1107, "项目经历不存在"),
    PROJECT_EXPERIENCE_NOT_BELONG_TO_USER(1108, "项目经历不属于当前用户"),
    WORK_EXPERIENCE_NOT_EXIST(1109, "工作/实习经历不存在"),
    WORK_EXPERIENCE_NOT_BELONG_TO_USER(1110, "工作/实习经历不属于当前用户"),
    SKILL_NOT_EXIST(1111, "技能不存在"),
    SKILL_NOT_BELONG_TO_USER(1112, "技能不属于当前用户"),
    SKILL_LIMIT_EXCEEDED(1113, "技能数量已达上限"),
    SKILL_ALREADY_EXIST(1114, "已存在相同技能"),
    SYSTEM_ERROR(9999, "系统内部错误,请检查业务处理逻辑,以及是否存在未处理的异常情况");

    private final Integer code;
    private final String message;

    /**
     * 枚举构造函数（私有）
     *
     * @param code    错误码
     * @param message 错误消息
     */
    ErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 获取错误码
     *
     * @return 错误码
     */
    public Integer getCode() {
        return code;
    }

    /**
     * 获取错误消息
     *
     * @return 错误消息
     */
    public String getMessage() {
        return message;
    }
}
