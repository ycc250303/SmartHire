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
  id: string
  username: string
  nickname: string
  email: string
  phone: string
  avatar?: string
  status: 'active' | 'disabled' | 'pending'
  role: 'admin' | 'hr' | 'user'
  createTime: string
  lastLoginTime?: string
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