<template>
  <div class="reports-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">举报处理</h1>
        <p class="page-description">处理用户举报内容，维护平台良好环境</p>
      </div>
      <div class="header-stats">
        <div class="stat-item">
          <span class="stat-value">{{ reportStats.pending }}</span>
          <span class="stat-label">待处理</span>
        </div>
        <div class="stat-item">
          <span class="stat-value">{{ reportStats.total }}</span>
          <span class="stat-label">总举报</span>
        </div>
      </div>
    </div>

    <!-- 筛选和搜索 -->
    <NCard :bordered="false" class="filter-card">
      <div class="filter-section">
        <div class="filter-row">
          <div class="filter-item">
            <label>举报类型</label>
            <NSelect
              v-model:value="filters.reportType"
              :options="reportTypeOptions"
              placeholder="全部类型"
              clearable
              style="width: 150px"
              @update:value="handleFilter"
            />
          </div>
          <div class="filter-item">
            <label>对象类型</label>
            <NSelect
              v-model:value="filters.targetType"
              :options="targetTypeOptions"
              placeholder="全部对象"
              clearable
              style="width: 150px"
              @update:value="handleFilter"
            />
          </div>
          <div class="filter-item">
            <label>处理状态</label>
            <NSelect
              v-model:value="filters.status"
              :options="statusOptions"
              placeholder="全部状态"
              clearable
              style="width: 150px"
              @update:value="handleFilter"
            />
          </div>
          </div>
        <div class="search-row">
          <div class="search-input">
            <NInput
              v-model:value="searchKeyword"
              placeholder="搜索举报内容、涉及用户"
              clearable
              @update:value="handleSearch"
            >
              <template #prefix>
                <span class="search-icon">🔍</span>
              </template>
            </NInput>
          </div>
          <div class="search-actions">
            <NButton @click="handleRefresh">
              <template #icon>🔄</template>
              刷新
            </NButton>
            <NButton @click="resetFilters">
              <template #icon>🔄</template>
              重置
            </NButton>
          </div>
        </div>
      </div>
    </NCard>

    <!-- 举报列表 -->
    <NCard :bordered="false" class="list-card">
      <div class="list-header">
        <span class="list-title">举报列表</span>
      </div>

      <div class="report-list">
        <div
          v-for="report in reportsData"
          :key="report.id"
          class="report-item"
          :class="{
            'processing': report.status === 0,
            'resolved': report.status === 1
          }"
          @click="viewReportDetail(report)"
        >
          <div class="report-header">
            <div class="report-info">
              <div class="report-id">
                <span class="id-label">举报编号</span>
                <span class="id-value">#{{ report.id }}</span>
              </div>
              <div class="report-badges">
                <NTag :type="getTypeType(report.reportType)" size="small">
                  {{ getTypeText(report.reportType) }}
                </NTag>
                <NTag :type="getStatusType(report.status)" size="small">
                  {{ getStatusText(report.status) }}
                </NTag>
                </div>
            </div>
            </div>

          <div class="report-content">
            <h4>举报内容</h4>
            <p>{{ report.reason }}</p>
          </div>

          <div class="report-details">
            <div class="detail-item">
              <span class="detail-label">举报对象</span>
              <span class="detail-value">
                {{ getTargetTypeText(report.targetType) }} - {{ report.targetTitle }}
              </span>
            </div>
            <div class="detail-item">
              <span class="detail-label">举报人</span>
              <span class="detail-value">{{ report.reporterName }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">举报时间</span>
              <span class="detail-value">{{ formatTime(report.createdAt) }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="reportsData.length === 0 && !loading" class="empty-state">
        <div class="empty-icon">⚠️</div>
        <h3 class="empty-title">暂无举报</h3>
        <p class="empty-description">当前没有需要处理的举报</p>
      </div>

      <!-- 分页 -->
      <div v-if="reportsData.length > 0" class="pagination-wrapper">
        <NPagination
          v-model:page="currentPage"
          :page-size="pageSize"
          show-size-picker
          :page-sizes="[10, 20, 50, 100]"
          @update:page="handlePageChange"
          @update:page-size="handlePageSizeChange"
        />
      </div>
    </NCard>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  NCard,
  NSelect,
  NInput,
  NButton,
  NTag,
  NPagination,
  useMessage
} from 'naive-ui'
import dayjs from 'dayjs'
import { reportsApi, type Report, type ReportQuery, REPORT_TYPE_LABEL_MAP, STATUS_LABEL_MAP, TARGET_TYPE_LABEL_MAP } from '@/api/reports'

interface Filters {
  reportType: number | null
  targetType: number | null
  status: string | null
}

const router = useRouter()
const message = useMessage()

// 筛选选项 - 举报类型（对应后端数字类型）
const reportTypeOptions = [
  { label: '垃圾信息', value: 1 },
  { label: '不当内容', value: 2 },
  { label: '虚假职位', value: 3 },
  { label: '欺诈行为', value: 4 },
  { label: '骚扰行为', value: 5 },
  { label: '其他', value: 6 }
]

// 筛选选项 - 对象类型
const targetTypeOptions = [
  { label: '用户', value: 1 },
  { label: '职位', value: 2 }
]

const statusOptions = [
  { label: '处理中', value: 'processing' },
  { label: '已处理', value: 'resolved' }
]

// 状态管理
const searchKeyword = ref('')
const filters = ref<Filters>({
  reportType: null,
  targetType: null,
  status: null
})
const currentPage = ref(1)
const pageSize = ref(20)
const loading = ref(false)

// 统计数据
const reportStats = ref({
  pending: 0,
  total: 0
})

// 举报数据
const reportsData = ref<Report[]>([])



// 辅助方法
const formatTime = (time: string) => {
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}

const getTypeType = (type: string | number): 'default' | 'warning' | 'error' | 'info' | 'success' | 'primary' => {
  // 处理数字类型的举报类型
  const typeNum = typeof type === 'number' ? type : parseInt(type)
  const typeMap: Record<number, 'default' | 'warning' | 'error' | 'info' | 'success' | 'primary'> = {
    1: 'warning',     // 垃圾信息
    2: 'warning',     // 不当内容
    3: 'error',       // 虚假职位
    4: 'error',       // 欺诈行为
    5: 'error',       // 骚扰行为
    6: 'default'      // 其他
  }

  // 处理字符串类型的举报类型（兼容性）
  if (typeof type === 'string') {
    const stringMap: Record<string, 'default' | 'warning' | 'error' | 'info' | 'success' | 'primary'> = {
      spam: 'warning',
      inappropriate: 'warning',
      fake_job: 'error',
      fraud: 'error',
      harassment: 'error',
      other: 'default'
    }
    return stringMap[type] || 'default'
  }

  return typeMap[typeNum] || 'default'
}

const getTypeText = (type: string | number) => {
  // 处理数字类型的举报类型
  const typeNum = typeof type === 'number' ? type : parseInt(type)
  const textMap: Record<number, string> = {
    1: '垃圾信息',
    2: '不当内容',
    3: '虚假职位',
    4: '欺诈行为',
    5: '骚扰行为',
    6: '其他'
  }

  // 处理字符串类型的举报类型（兼容性）
  if (typeof type === 'string') {
    const stringMap: Record<string, string> = {
      spam: '垃圾信息',
      inappropriate: '不当内容',
      fake_job: '虚假职位',
      fraud: '欺诈行为',
      harassment: '骚扰行为',
      other: '其他'
    }
    return stringMap[type] || type
  }

  return textMap[typeNum] || '其他'
}

const getStatusType = (status: string | number): 'default' | 'warning' | 'error' | 'info' | 'success' | 'primary' => {
  // 处理数字类型的状态
  const statusNum = typeof status === 'number' ? status : parseInt(status)
  const typeMap: Record<number, 'default' | 'warning' | 'error' | 'info' | 'success' | 'primary'> = {
    0: 'info',        // 处理中
    1: 'success'      // 已处理
  }

  // 处理字符串类型的状态（兼容性）
  if (typeof status === 'string') {
    const stringMap: Record<string, 'default' | 'warning' | 'error' | 'info' | 'success' | 'primary'> = {
      pending: 'warning',
      processing: 'info',
      resolved: 'success',
      rejected: 'error'
    }
    return stringMap[status] || 'default'
  }

  return typeMap[statusNum] || 'default'
}

const getStatusText = (status: string | number) => {
  // 处理数字类型的状态
  const statusNum = typeof status === 'number' ? status : parseInt(status)
  const textMap: Record<number, string> = {
    0: '处理中',
    1: '已处理'
  }

  // 处理字符串类型的状态（兼容性）
  if (typeof status === 'string') {
    const stringMap: Record<string, string> = {
      pending: '待处理',
      processing: '处理中',
      resolved: '已处理',
      rejected: '已驳回'
    }
    return stringMap[status] || status
  }

  return textMap[statusNum] || '未知状态'
}

const getTargetTypeText = (targetType: string | number) => {
  // 处理数字类型的对象类型
  const targetTypeNum = typeof targetType === 'number' ? targetType : parseInt(targetType)
  const textMap: Record<number, string> = {
    1: '用户',
    2: '职位'
  }

  // 处理字符串类型的对象类型（兼容性）
  if (typeof targetType === 'string') {
    const stringMap: Record<string, string> = {
      user: '用户',
      job: '职位'
    }
    return stringMap[targetType] || targetType
  }

  return textMap[targetTypeNum] || '未知对象'
}


// 事件处理
const handleFilter = () => {
  currentPage.value = 1
  loadReports()
}

const handleSearch = (value: string) => {
  searchKeyword.value = value
  currentPage.value = 1
  loadReports()
}

const handleRefresh = () => {
  message.success('数据已刷新')
  loadReports()
}

const resetFilters = () => {
  filters.value = {
    reportType: null,
    targetType: null,
    status: null
  }
  searchKeyword.value = ''
  currentPage.value = 1
  loadReports()
}

const handlePageChange = (page: number) => {
  currentPage.value = page
  loadReports()
}

const handlePageSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
  loadReports()
}

// 查看详情
const viewReportDetail = (report: Report) => {
  router.push(`/dashboard/reports/${report.id}`)
}



// 加载举报列表
const loadReports = async () => {
  try {
    loading.value = true
    const params: ReportQuery = {
      current: currentPage.value,
      size: pageSize.value,
      targetType: filters.value.targetType || undefined,
      reportType: filters.value.reportType || undefined,
      status: filters.value.status === 'processing' ? 0 : filters.value.status === 'resolved' ? 1 : undefined,
      keyword: searchKeyword.value || undefined
    }

    const result = await reportsApi.getReports(params)
    console.log('举报列表API响应:', result)

    // MyBatis-Plus分页格式：records数组
    reportsData.value = result.records
  } catch (error: any) {
    console.error('加载举报列表失败:', error)
    message.error(error.message || '加载举报列表失败')
  } finally {
    loading.value = false
  }
}

// 加载统计信息
const loadStats = async () => {
  try {
    const stats = await reportsApi.getReportStats() as any
    const total = stats?.total || 0
    const pending = stats?.pendingCount || 0
    const resolved = stats?.resolvedCount || 0

    reportStats.value = {
      total: total,
      pending: pending
    }
  } catch (error) {
    console.error('加载统计信息失败:', error)
  }
}



// 页面初始化
onMounted(() => {
  loadReports()
  loadStats()
})
</script>

<style scoped lang="scss">
.reports-page {
  // 页面头部
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 32px;
    padding: 24px;
    background: linear-gradient(135deg, #ff6b6b 0%, #ee5a24 100%);
    border-radius: 16px;
    color: white;

    .header-content {
      .page-title {
        font-size: 28px;
        font-weight: 600;
        margin: 0 0 8px 0;
      }

      .page-description {
        font-size: 16px;
        opacity: 0.9;
        margin: 0;
      }
    }

    .header-stats {
      display: flex;
      gap: 24px;

      .stat-item {
        text-align: center;

        .stat-value {
          display: block;
          font-size: 24px;
          font-weight: 600;
          margin-bottom: 4px;
        }

        .stat-label {
          font-size: 14px;
          opacity: 0.8;
        }
      }
    }
  }

  // 筛选卡片
  .filter-card {
    margin-bottom: 24px;

    .filter-section {
      .filter-row {
        display: flex;
        gap: 24px;
        margin-bottom: 16px;
        flex-wrap: wrap;

        .filter-item {
          display: flex;
          align-items: center;
          gap: 8px;

          label {
            font-size: 14px;
            color: var(--text-secondary);
            white-space: nowrap;
          }
        }
      }

      .search-row {
        display: flex;
        gap: 16px;
        align-items: center;

        .search-input {
          flex: 1;

          .search-icon {
            color: var(--text-disabled);
          }
        }

        .search-actions {
          display: flex;
          gap: 12px;

          .n-button {
            min-width: 80px;
          }
        }
      }
    }
  }

  // 举报列表卡片
  .list-card {
    .list-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 24px;
      padding: 20px 24px;
      background: var(--bg-secondary);
      border-radius: 8px;

      .list-title {
        font-size: 18px;
        font-weight: 600;
        color: var(--text-primary);
      }

      .total-count {
        font-size: 14px;
        color: var(--text-secondary);
      }
    }

    .report-list {
      .report-item {
        background: var(--bg-primary);
        border: 1px solid var(--border-color);
        border-radius: 12px;
        padding: 24px;
        margin-bottom: 16px;
        cursor: pointer;
        transition: all 0.3s ease;

        &:hover {
          transform: translateY(-2px);
          box-shadow: var(--shadow-md);
          border-color: var(--primary-color);
        }

  
        &.pending {
          background: rgba(250, 173, 20, 0.05);
        }

        &.processing {
          background: rgba(32, 128, 240, 0.05);
        }

        &:last-child {
          margin-bottom: 0;
        }

        .report-header {
          display: flex;
          justify-content: space-between;
          align-items: flex-start;
          margin-bottom: 16px;

          .report-info {
            flex: 1;
            margin-right: 16px;

            .report-id {
              display: flex;
              align-items: center;
              gap: 8px;
              margin-bottom: 8px;

              .id-label {
                font-size: 14px;
                color: var(--text-secondary);
              }

              .id-value {
                font-size: 14px;
                font-weight: 600;
                color: var(--text-primary);
              }
            }

            .report-badges {
              display: flex;
              gap: 8px;
              flex-wrap: wrap;

              .n-tag {
                font-size: 12px;
              }
            }
          }

          .report-actions {
            display: flex;
            gap: 8px;
            flex-shrink: 0;
          }
        }

        .report-content {
          margin-bottom: 16px;

          h4 {
            margin: 0 0 8px 0;
            font-size: 14px;
            font-weight: 600;
            color: var(--text-primary);
          }

          p {
            margin: 0;
            font-size: 14px;
            color: var(--text-secondary);
            line-height: 1.5;
          }
        }

        .report-details {
          display: grid;
          grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
          gap: 12px;
          margin-bottom: 12px;

          .detail-item {
            display: flex;
            flex-direction: column;
            gap: 4px;

            .detail-label {
              font-size: 12px;
              color: var(--text-disabled);
            }

            .detail-value {
              font-size: 14px;
              color: var(--text-primary);
            }
          }
        }

        .report-evidence {
          display: flex;
          flex-wrap: wrap;
          align-items: center;
          gap: 8px;
          margin-bottom: 12px;

          .evidence-label {
            font-size: 12px;
            color: var(--text-disabled);
          }

          .evidence-list {
            display: flex;
            flex-wrap: wrap;
            gap: 8px;

            .evidence-item {
              font-size: 12px;
              color: var(--text-secondary);
              background: var(--bg-secondary);
              padding: 4px 8px;
              border-radius: 4px;
            }
          }
        }

        .report-status {
          display: flex;
          align-items: center;
          gap: 8px;
          padding: 8px 0;
          border-top: 1px solid var(--border-color);

          .status-label {
            font-size: 12px;
            color: var(--text-disabled);
          }

          .status-result {
            font-size: 13px;
            color: var(--text-secondary);
            font-weight: 500;
          }
        }
      }
    }

    .empty-state {
      text-align: center;
      padding: 60px 24px;

      .empty-icon {
        font-size: 64px;
        margin-bottom: 16px;
        opacity: 0.5;
      }

      .empty-title {
        font-size: 18px;
        font-weight: 600;
        color: var(--text-primary);
        margin: 0 0 8px 0;
      }

      .empty-description {
        font-size: 14px;
        color: var(--text-secondary);
        margin: 0;
      }
    }

    .pagination-wrapper {
      display: flex;
      justify-content: center;
      margin-top: 24px;
      padding: 20px;
    }
  }
}


// 响应式设计
@media (max-width: 768px) {
  .reports-page {
    .page-header {
      flex-direction: column;
      gap: 16px;
      padding: 16px;

      .header-content {
        text-align: center;

        .page-title {
          font-size: 24px;
        }

        .page-description {
          font-size: 14px;
        }
      }

      .header-stats {
        width: 100%;
        justify-content: center;
        gap: 32px;
      }
    }

    .filter-card {
      .filter-section {
        .filter-row {
          flex-direction: column;
          gap: 12px;

          .filter-item {
            width: 100%;

            .n-select {
              width: 100% !important;
            }
          }
        }

        .search-row {
          flex-direction: column;
          gap: 12px;

          .search-actions {
            width: 100%;

            .n-button {
              flex: 1;
            }
          }
        }
      }
    }

    .report-list {
      .report-item {
        padding: 16px;

        .report-header {
          flex-direction: column;
          gap: 12px;
          align-items: flex-start;

          .report-info {
            margin-right: 0;
          }

          .report-actions {
            width: 100%;
            justify-content: flex-end;
          }
        }

        .report-details {
          grid-template-columns: 1fr;
        }

        .report-evidence {
          flex-direction: column;
          align-items: flex-start;
        }

        .report-status {
          flex-direction: column;
          align-items: flex-start;
          gap: 4px;
        }
      }
    }
  }
}
</style>