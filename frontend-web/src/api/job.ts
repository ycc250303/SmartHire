import { adminRequest } from '@/utils/request'
import { useUserStore } from '@/store/user'

// 职位审核信息接口定义（严格匹配后端JobAuditListDTO）
export interface Job {
  id: number  // 后端返回的是number类型
  title: string
  company: string
  location: string
  salary: string
  experience: string
  education: string
  description?: string
  status: 'pending' | 'approved' | 'rejected' | 'modified'  // 后端返回的字符串状态
  publisher: string
  createTime: string
  tags?: string[]  // 从job_skill_requirement表动态获取
}

// 职位审核查询参数（严格匹配后端JobAuditQueryDTO）
export interface JobAuditQueryParams {
  current: number  // 后端期望的参数名是current
  size: number
  status?: string  // pending, approved, rejected, modified
  keyword?: string  // 职位名称、公司名称、发布者
}

// 职位审核列表响应
export interface JobAuditListResponse {
  records: Job[]
  total: number
  current: number
  size: number
  pages: number
}

// 职位审核操作DTO（严格匹配后端JobAuditDTO）
export interface JobAuditParams {
  reason: string  // 审核原因（拒绝原因或修改意见）
}

// 职位审核详情（匹配后端JobAuditRecord）
export interface JobAuditDetail {
  id: number
  jobId: number
  jobTitle: string
  companyId: number
  hrId: number
  companyName: string
  hrName: string
  auditNote?: string
  auditReason?: string  // 要求修改时的审核意见
  rejectReason?: string // 拒绝原因
  status: string
  auditorId?: number
  auditorName?: string
  auditedAt?: string
  createdAt: string
  updatedAt: string
}

// 职位状态统计
export interface JobAuditStats {
  pending: number
  approved: number
  rejected: number
  modified: number
}

// 获取职位审核列表
export const getJobAuditList = (params: JobAuditQueryParams): Promise<JobAuditListResponse> => {
  return adminRequest.get('/admin/review/jobs', { params })
}

// 获取职位审核详情
export const getJobAuditDetail = (jobId: number): Promise<JobAuditDetail> => {
  return adminRequest.get(`/admin/review/jobs/${jobId}`)
}

// 通过审核
export const approveJob = (jobId: number): Promise<void> => {
  // 后端从SecurityContext获取管理员信息，前端不需要传递
  return adminRequest.post(`/admin/review/jobs/${jobId}/approve`)
}

// 拒绝审核
export const rejectJob = (jobId: number, params: JobAuditParams): Promise<void> => {
  return adminRequest.post(`/admin/review/jobs/${jobId}/reject`, params)
}

// 要求修改
export const modifyJob = (jobId: number, params: JobAuditParams): Promise<void> => {
  return adminRequest.post(`/admin/review/jobs/${jobId}/modify`, params)
}

// 按状态统计职位数量
export const countJobsByStatus = (status: string): Promise<number> => {
  return adminRequest.get(`/admin/review/count/${status}`)
}

// 获取所有状态的统计信息
export const getJobAuditStats = (): Promise<JobAuditStats> => {
  return Promise.all([
    countJobsByStatus('pending'),
    countJobsByStatus('approved'),
    countJobsByStatus('rejected'),
    countJobsByStatus('modified')
  ]).then(([pending, approved, rejected, modified]) => ({
    pending,
    approved,
    rejected,
    modified
  }))
}