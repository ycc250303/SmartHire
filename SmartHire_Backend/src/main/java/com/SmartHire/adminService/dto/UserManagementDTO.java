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
  private String avatarUrl;
  private String createTime;
  private String lastLoginTime;

  // HR用户的扩展信息
  private String company;
  private String position;

  // 构造函数
  public UserManagementDTO() {}

  public UserManagementDTO(
      Long userId,
      String username,
      String email,
      String userType,
      Integer status,
      String avatarUrl,
      String createTime,
      String lastLoginTime) {
    this.userId = userId;
    this.username = username;
    this.email = email;
    this.userType = userType;
    this.status = status;
    this.avatarUrl = avatarUrl;
    this.createTime = createTime;
    this.lastLoginTime = lastLoginTime;
  }

  public UserManagementDTO(
      Long userId,
      String username,
      String email,
      String userType,
      Integer status,
      String avatarUrl,
      String createTime,
      String lastLoginTime,
      String company,
      String position) {
    this.userId = userId;
    this.username = username;
    this.email = email;
    this.userType = userType;
    this.status = status;
    this.avatarUrl = avatarUrl;
    this.createTime = createTime;
    this.lastLoginTime = lastLoginTime;
    this.company = company;
    this.position = position;
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

  public String getAvatarUrl() {
    return avatarUrl;
  }

  public void setAvatarUrl(String avatarUrl) {
    this.avatarUrl = avatarUrl;
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

  public String getCompany() {
    return company;
  }

  public void setCompany(String company) {
    this.company = company;
  }

  public String getPosition() {
    return position;
  }

  public void setPosition(String position) {
    this.position = position;
  }
}
