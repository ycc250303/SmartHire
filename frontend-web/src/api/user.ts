import { adminRequest } from '@/utils/request'
import { useUserStore } from '@/store/user'

// 用户类型定义
export interface User {
  userId: number  // 后端返回的是userId
  username: string
  email?: string
  phone?: string
  userType: 'jobseeker' | 'hr' | 'admin' | 'unknown'  // 后端返回的是字符串格式
  status: number // 0-禁用 1-正常
  avatarUrl?: string
  createTime?: string
  lastLoginTime?: string
}

// 用户封禁请求参数
export interface BanUserParams {
  banReason: string
  banDurationType: 'permanent' | 'temporary'
  banDays?: number
  sendEmailNotification?: boolean
  sendSystemNotification?: boolean
  adminId: number
  adminUsername: string
}

// 用户状态更新参数
export interface UpdateUserStatusParams {
  userId: number
  targetStatus: number
  reason: string
  adminId: number
  adminUsername: string
  sendNotification?: boolean
}

// 用户查询参数
export interface UserQueryParams {
  current: number  // 后端期望的参数名是current
  size: number
  keyword?: string
  userType?: string
  status?: string // 'active' | 'banned'
  startTime?: string
  endTime?: string
}

// 用户列表响应
export interface UserListResponse {
  records: User[]
  total: number
  current: number
  size: number
  pages: number
}

// 用户统计信息
export interface UserStats {
  totalUsers: number
  activeUsers: number
  bannedUsers: number
  jobSeekers: number
  hrs: number
  admins: number
  recentLogins: number
}

// 获取用户列表
export const getUserList = (params: UserQueryParams): Promise<UserListResponse> => {
  return adminRequest.get('/admin/users', { params })
}

// 获取用户详情
export const getUserDetail = (userId: number): Promise<User> => {
  return adminRequest.get(`/admin/users/${userId}`)
}

// 封禁用户
export const banUser = (userId: number, params: Omit<BanUserParams, 'adminId' | 'adminUsername' | 'userId'>): Promise<void> => {
  const userStore = useUserStore()
  return adminRequest.post(`/admin/users/${userId}/ban`, {
    ...params,
    userId,  // 确保请求体中包含userId字段
    adminId: userStore.auth.user?.id,
    adminUsername: userStore.auth.user?.username
  })
}

// 解封用户
export const unbanUser = (userId: number, params: { liftReason: string; sendNotification?: boolean }): Promise<void> => {
  const userStore = useUserStore()
  return adminRequest.post(`/admin/users/${userId}/unban`, null, {
    params: {
      operatorId: userStore.auth.user?.id,
      operatorName: userStore.auth.user?.username,
      liftReason: params.liftReason
    }
  })
}

// 更新用户状态
export const updateUserStatus = (params: UpdateUserStatusParams): Promise<void> => {
  return adminRequest.put('/admin/users/status', params)
}

// 获取用户统计信息
export const getUserStats = (): Promise<UserStats> => {
  return adminRequest.get('/admin/users/stats')
}

// 检查用户是否被封禁
export const checkUserBanned = (userId: number): Promise<{ isBanned: boolean; banRecord?: any }> => {
  return adminRequest.get(`/admin/users/${userId}/ban-status`)
}