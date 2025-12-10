package com.SmartHire.common.exception.enums;

public enum ErrorCode {
  VALIDATION_ERROR(1, "参数校验异常"),
  // 用户注册登录相关错误码 (1000-1999)
  USER_AUTH_USER_HAS_EXISTED(1001, "用户名已存在"),
  EMAIL_CODE_SEND_FAILED(1002, "验证码发送失败"),
  EMAIL_CODE_INVALID(1003, "验证码无效或已过期"),
  EMAIL_CODE_SEND_TOO_FREQUENT(1004, "验证码发送过于频繁，请稍后再试"),
  EMAIL_FORMAT_INVALID(1005, "邮箱格式不正确"),
  EMAIL_ALREADY_REGISTERED(1006, "邮箱已被注册"),
  USER_AUTH_USER_NOT_EXIST(1007, "用户名不存在"),
  USER_AUTH_USER_PASSWORD_ERROR(1008, "密码输入错误"),
  USER_AUTH_PHONE_HAS_EXISTED(1009, "手机号已存在"),
  USER_ID_NOT_EXIST(1010, "用户ID不存在"),
  TOKEN_IS_NULL(1011, "JWT Token 为空"),
  TOKEN_IS_INVALID(1012, "JWT Token 无效"),
  USER_NOT_LOGIN(1013, "用户未登录"),
  USER_AVATAR_FILE_EMPTY(1014, "上传的头像文件不能为空"),
  USER_AVATAR_UPLOAD_FAILED(1015, "头像上传失败"),
  ACCOUNT_DISABLED(1016, "账户已被禁用，请联系管理员"),
  REFRESH_TOKEN_EXPIRED(1017, "Refresh Token 已过期，请重新登录"),
  TOKEN_IS_REFRESH_TOKEN(1018, "JWT Token 为 refresh token"),
  PERMISSION_DENIED(1019, "权限不足，仅管理员可执行此操作"),
  CANNOT_DELETE_OTHER_USER(1020, "无权删除其他用户的账户，只能删除自己的账户"),

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
  PROJECT_EXPERIENCE_LIMIT_EXCEEDED(1115, "项目经历数量已达上限"),
  WORK_EXPERIENCE_LIMIT_EXCEEDED(1116, "工作/实习经历数量已达上限"),
  JOB_SEEKER_EXPECTATION_NOT_EXIST(1117, "求职期望不存在"),
  JOB_SEEKER_EXPECTATION_NOT_BELONG_TO_USER(1118, "求职期望不属于当前用户"),
  JOB_SEEKER_EXPECTATION_LIMIT_EXCEEDED(1119, "求职期望数量已达上限（最多5个）"),
  SALARY_MAX_LESS_THAN_MIN(1120, "期望薪资最高值不能小于最低值"),
  RESUME_NOT_EXIST(1121, "简历不存在"),
  RESUME_NOT_BELONG_TO_USER(1122, "简历不属于当前用户"),
  RESUME_LIMIT_EXCEEDED(1123, "附件简历数量已达上限（最多5个）"),
  RESUME_FILE_EMPTY(1124, "上传的简历文件不能为空"),
  RESUME_UPLOAD_FAILED(1125, "简历上传失败"),
  JOB_FAVORITE_ALREADY_EXISTS(1126, "该岗位已收藏，请勿重复收藏"),
  JOB_FAVORITE_NOT_EXIST(1127, "收藏记录不存在"),

  // HR和岗位相关错误码 (1200-1299)
  HR_NOT_EXIST(1201, "HR信息不存在"),
  HR_NOT_BELONG_TO_USER(1202, "HR信息不属于当前用户"),
  USER_NOT_HR(1203, "用户身份不是HR"),
  COMPANY_NOT_EXIST(1204, "公司信息不存在"),
  JOB_NOT_EXIST(1205, "岗位不存在"),
  JOB_NOT_BELONG_TO_HR(1206, "岗位不属于当前HR"),
  INTERNSHIP_DAYS_PER_WEEK_REQUIRED(1207, "实习类型职位必须填写每周实习天数"),
  INTERNSHIP_DURATION_MONTHS_REQUIRED(1208, "实习类型职位必须填写实习时长"),
  EXPERIENCE_REQUIRED_FOR_FULL_TIME(1209, "全职类型职位必须填写经验要求"),

  // 招聘（投递/推荐）相关错误码 (1300-1399)
  APPLICATION_ALREADY_EXISTS(1301, "您已投递过该职位，请勿重复投递"),

  APPLICATION_NOT_EXIST(1303, "投递记录不存在"),
  APPLICATION_NOT_BELONG_TO_HR(1304, "投递记录不属于当前HR"),
  // 会话/聊天相关错误码 (1400-1499)
  CONVERSATION_NOT_EXIST(1401, "沟通记录不存在"),
  MESSAGE_USER_TYPE_MISMATCH(1402, "求职者只能和HR发消息，HR只能和求职者发消息"),
  SYSTEM_ERROR(9999, "系统内部错误,请检查业务处理逻辑,以及是否存在未处理的异常情况");

  private final Integer code;
  private final String message;

  /**
   * 枚举构造函数（私有）
   *
   * @param code 错误码
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
