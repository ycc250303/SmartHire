// 通用响应类型
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
  timestamp: string
}

// 分页参数
export interface PaginationParams {
  current: number
  pageSize: number
  total?: number
}

// 分页响应
export interface PaginationResponse<T> {
  list: T[]
  total: number
  current: number
  pageSize: number
}

// 用户相关类型
export interface User {
  id: number  // 后端返回的是number类型
  username: string
  nickname?: string  // 可选字段
  email?: string
  phone?: string
  avatar?: string
  userType: number // 1-求职者 2-HR 3-管理员
  status: number // 0-禁用 1-正常
  avatarUrl?: string
  createTime?: string
  lastLoginTime?: string
  // 为了兼容前端逻辑，添加计算属性
  role?: 'admin' | 'hr' | 'user'  // 根据userType计算
}

// 招聘相关类型
export interface Job {
  id: string
  title: string
  company: string
  location: string
  salary: string
  experience: string
  education: string
  description: string
  requirements: string
  status: 'pending' | 'approved' | 'rejected'
  hrId: string
  hrName: string
  createTime: string
  publishTime?: string
  expireTime?: string
  viewCount: number
  applyCount: number
}

// 公告相关类型
export interface Announcement {
  id: string
  title: string
  content: string
  type: 'system' | 'maintenance' | 'feature' | 'urgent'
  status: 'draft' | 'published' | 'expired'
  publishType?: 'immediate' | 'scheduled'
  createTime: string
  publishTime?: string
  readCount?: number
  likeCount?: number
  commentCount?: number
}

// 统计数据类型
export interface StatisticsData {
  userCount: {
    total: number
    today: number
    active: number
  }
  jobCount: {
    total: number
    today: number
    pending: number
  }
  applyCount: {
    total: number
    today: number
    success: number
  }
  viewCount: {
    total: number
    today: number
    growth: number
  }
}

// 菜单类型
export interface MenuItem {
  key: string
  label: string
  icon?: string
  path?: string
  children?: MenuItem[]
}

// 路由元信息
export interface RouteMeta {
  title?: string
  icon?: string
  requiresAuth?: boolean
  hideInMenu?: boolean
  roles?: string[]
}

// 主题设置
export interface ThemeSettings {
  isDark: boolean
  primaryColor: string
  compact: boolean
  followSystem: boolean
}

// 用户管理DTO - 匹配后端UserManagementDTO
export interface UserManagementDTO {
  userId: number
  username: string
  email: string
  userType: string        // "job_seeker" | "hr" | "admin"
  status: number          // 0=禁用, 1=启用
  createTime: string
  lastLoginTime: string
}

// 封禁记录 - 匹配后端BanRecord
export interface BanRecord {
  id: number
  userId: number
  username: string
  email: string
  userType: number         // 1=求职者, 2=HR
  banReason: string
  banType: 'permanent' | 'temporary'
  banDays?: number
  banStartTime: string
  banEndTime?: string
  banStatus: 'active' | 'expired' | 'lifted'
  operatorId: number
  operatorName: string
  liftedByOperatorId?: number
  liftedByOperatorName?: string
  liftReason?: string
  liftedAt?: string
  createdAt: string
  updatedAt: string
}