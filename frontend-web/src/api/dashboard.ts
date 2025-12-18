import { adminRequest } from '@/utils/request'
import { countJobsByStatus } from './job'

// 待办事项统计接口
export interface TodoStats {
  pendingJobs: number      // 待审核职位数量
  pendingReports: number   // 待处理举报数量
}

// 获取待办事项统计
export const getTodoStats = async (): Promise<TodoStats> => {
  try {
    // 使用job.ts中已存在的API函数获取待审核职位数量
    const pendingJobs = await countJobsByStatus('pending')

    // 举报统计路径应该是 /admin/reports/stats
    const reportsResponse = await adminRequest.get('/admin/reports/stats')
    // 根据后端返回的数据结构：{total: 3, pendingCount: 1, resolvedCount: 2}
    const pendingReports = reportsResponse?.pendingCount || 0

    return {
      pendingJobs,
      pendingReports
    }
  } catch (error) {
    console.warn('获取待办事项统计失败:', error)
    // 如果API调用失败，返回默认值
    return {
      pendingJobs: 0,
      pendingReports: 0
    }
  }
}