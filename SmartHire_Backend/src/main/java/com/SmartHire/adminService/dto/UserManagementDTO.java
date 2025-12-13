package com.SmartHire.adminService.dto;

/**
 * 用户管理DTO
 *
 * @author SmartHire Team
 * @since 2025-12-02
 */
public class UserManagementDTO {

  private Long userId;
  private String username;
  private String email;
  private String userType; // "hr", "job_seeker", "admin"
  private Integer status; // 0-禁用, 1-启用
  private String createTime;
  private String lastLoginTime;

  // 构造函数
  public UserManagementDTO() {}

  public UserManagementDTO(
      Long userId,
      String username,
      String email,
      String userType,
      Integer status,
      String createTime,
      String lastLoginTime) {
    this.userId = userId;
    this.username = username;
    this.email = email;
    this.userType = userType;
    this.status = status;
    this.createTime = createTime;
    this.lastLoginTime = lastLoginTime;
  }

  // Getter和Setter
  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getUserType() {
    return userType;
  }

  public void setUserType(String userType) {
    this.userType = userType;
  }

  public Integer getStatus() {
    return status;
  }

  public void setStatus(Integer status) {
    this.status = status;
  }

  public String getCreateTime() {
    return createTime;
  }

  public void setCreateTime(String createTime) {
    this.createTime = createTime;
  }

  public String getLastLoginTime() {
    return lastLoginTime;
  }

  public void setLastLoginTime(String lastLoginTime) {
    this.lastLoginTime = lastLoginTime;
  }
}
