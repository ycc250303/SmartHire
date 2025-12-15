import { adminRequest } from '@/utils/request'

// 类型定义
export interface Report {
  id: string
  reportType: number  // 后端返回的数字类型：1-垃圾信息, 2-不当内容, 3-虚假职位, 4-欺诈行为, 5-骚扰行为, 6-其他
  status: number      // 后端返回的数字类型：0-处理中, 1-已处理
  reason: string
  targetType: number  // 后端返回的数字类型：1-用户, 2-职位
  targetTitle: string  // 后端返回的字段名，包含具体的目标信息
  targetId: string
  reporterName: string  // 后端返回的字段名
  reporterId: string
  createdAt: string
}

export interface ReportDetail {
  id: string
  reporterId: string
  reporterName: string
  reporterType: number
  targetType: number
  targetId: string
  targetTitle: string
  reportType: number
  reason: string
  status: number
  handlerId?: string
  handleResult?: number
  handleReason?: string
  handleTime?: string
  evidenceImage?: string
  createdAt: string
  updatedAt: string
  targetUser?: {
    id: number
    username: string
    email: string
    phone: string
    userType: number
    status: number
    avatarUrl: string
    createdAt: string
  }
  targetJob?: {
    id: number
    hrId: number
    jobTitle: string
    jobCategory: string
    city: string
    salaryMin: string
    salaryMax: string
    status: number
    createdAt: string
    companyId: number
    companyName: string
  }
}

export interface ReportQuery {
  current?: number  // 前端分页组件使用
  page?: number     // 后端期望的参数名
  size?: number
  targetType?: number
  reportType?: number
  status?: number
  keyword?: string
}

export interface ReportHandle {
  handleResult: number
  handleReason: string
  banInfo?: {
    banType: string  // 'permanent' | 'temporary'
    banDays?: number // 临时封禁天数
  }
  reporterNotificationContent: string
  targetNotificationContent: string
}

// 分页响应接口
export interface ReportPageResponse {
  records: Report[]
  total: number
  current: number
  size: number
  pages: number
}

// API接口
export const reportsApi = {
  // 分页查询举报列表
  getReports: (params: ReportQuery): Promise<ReportPageResponse> => {
    return adminRequest.get('/admin/reports', { params })
  },

  // 获取举报详情
  getReportDetail: (id: string) => {
    return adminRequest.get(`/admin/reports/${id}`)
  },

  // 处理举报
  handleReport: (id: string, data: ReportHandle, operatorId: number) => {
    return adminRequest.put(`/admin/reports/${id}/handle`, data, {
      params: { operatorId }
    })
  },

  // 获取举报统计
  getReportStats: () => {
    return adminRequest.get('/admin/reports/stats')
  },

  // 获取证据图片
  getEvidenceImage: (id: string) => {
    return adminRequest.get(`/admin/reports/${id}/evidence`)
  }
}

// 常量定义
export const REPORT_TYPE_MAP: Record<number, string> = {
  1: 'spam',
  2: 'inappropriate',
  3: 'fake_job',
  4: 'fraud',
  5: 'harassment',
  6: 'other'
}

export const REPORT_TYPE_LABEL_MAP: Record<string, string> = {
  spam: '垃圾信息',
  inappropriate: '不当内容',
  fake_job: '虚假职位',
  fraud: '欺诈行为',
  harassment: '骚扰行为',
  other: '其他'
}

export const STATUS_MAP: Record<number, string> = {
  0: 'processing',
  1: 'resolved'
}

export const STATUS_LABEL_MAP: Record<string, string> = {
  processing: '处理中',
  resolved: '已处理'
}

export const TARGET_TYPE_MAP: Record<number, string> = {
  1: 'user',
  2: 'job'
}

export const TARGET_TYPE_LABEL_MAP: Record<string, string> = {
  user: '用户',
  job: '职位'
}

export const HANDLE_RESULT_MAP: Record<number, string> = {
  0: 'no_action',
  1: 'warn',
  2: 'ban',
  3: 'offline'
}

export const HANDLE_RESULT_LABEL_MAP: Record<string, string> = {
  no_action: '无需处理',
  warn: '警告',
  ban: '封禁',
  offline: '下线'
}