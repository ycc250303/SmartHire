import { adminRequest } from '@/utils/request'
import { useUserStore } from '@/store/user'

// 用户类型定义
export interface User {
  id: number
  username: string
  email?: string
  phone?: string
  userType: number // 1-求职者 2-HR 3-管理员
  status: number // 0-禁用 1-正常
  avatarUrl?: string
  createTime?: string
  lastLoginTime?: string
}

// 用户封禁请求参数
export interface BanUserParams {
  userId: number
  banType: string // 'permanent' | 'temporary'
  banDays?: number
  banReason: string
  operatorId: number
  operatorUsername: string
  sendNotification?: boolean
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
  page: number
  size: number
  keyword?: string
  userType?: number
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
export const banUser = (userId: number, params: Omit<BanUserParams, 'userId' | 'operatorId' | 'operatorUsername'>): Promise<void> => {
  const userStore = useUserStore()
  return adminRequest.post(`/admin/users/${userId}/ban`, {
    ...params,
    operatorId: userStore.auth.user?.id,
    operatorUsername: userStore.auth.user?.username
  })
}

// 解封用户
export const unbanUser = (userId: number, params: { reason: string; sendNotification?: boolean }): Promise<void> => {
  const userStore = useUserStore()
  return adminRequest.post(`/admin/users/${userId}/unban`, {
    ...params,
    adminId: userStore.auth.user?.id,
    adminUsername: userStore.auth.user?.username
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