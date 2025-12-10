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
              v-model:value="filters.type"
              :options="typeOptions"
              placeholder="全部类型"
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
          <div class="filter-item">
            <label>优先级</label>
            <NSelect
              v-model:value="filters.priority"
              :options="priorityOptions"
              placeholder="全部优先级"
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
        <div class="list-actions">
          <span class="total-count">共 {{ filteredReports.length }} 条举报</span>
        </div>
      </div>

      <div class="report-list">
        <div
          v-for="report in paginatedReports"
          :key="report.id"
          class="report-item"
          :class="{
            'high-priority': report.priority === 'high',
            'pending': report.status === 'pending',
            'processing': report.status === 'processing'
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
                <NTag :type="getTypeType(report.type)" size="small">
                  {{ getTypeText(report.type) }}
                </NTag>
                <NTag :type="getStatusType(report.status)" size="small">
                  {{ getStatusText(report.status) }}
                </NTag>
                <NTag :type="getPriorityType(report.priority)" size="small">
                  {{ getPriorityText(report.priority) }}
                </NTag>
              </div>
            </div>
            <div class="report-actions">
              <NButton
                v-if="report.status === 'pending'"
                size="small"
                type="success"
                @click.stop="handleReport(report)"
              >
                处理
              </NButton>
              <NButton
                size="small"
                type="primary"
                ghost
                @click.stop="viewReportDetail(report)"
              >
                查看详情
              </NButton>
            </div>
          </div>

          <div class="report-content">
            <h4>举报内容</h4>
            <p>{{ report.reason }}</p>
          </div>

          <div class="report-details">
            <div class="detail-item">
              <span class="detail-label">举报对象</span>
              <span class="detail-value">{{ report.targetType }} - {{ report.targetInfo }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">举报人</span>
              <span class="detail-value">{{ report.reporter }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">举报时间</span>
              <span class="detail-value">{{ formatTime(report.createTime) }}</span>
            </div>
          </div>

          <div class="report-evidence" v-if="report.evidence && report.evidence.length > 0">
            <span class="evidence-label">举报证据：</span>
            <div class="evidence-list">
              <span
                v-for="(evidence, index) in report.evidence"
                :key="index"
                class="evidence-item"
              >
                {{ evidence.type }}: {{ evidence.name }}
              </span>
            </div>
          </div>

          <div class="report-status" v-if="report.status !== 'pending'">
            <span class="status-label">处理结果：</span>
            <span class="status-result">{{ report.result || '处理中' }}</span>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="filteredReports.length === 0" class="empty-state">
        <div class="empty-icon">⚠️</div>
        <h3 class="empty-title">暂无举报</h3>
        <p class="empty-description">当前没有需要处理的举报</p>
      </div>

      <!-- 分页 -->
      <div v-if="filteredReports.length > 0" class="pagination-wrapper">
        <NPagination
          v-model:page="currentPage"
          :page-size="pageSize"
          :item-count="filteredReports.length"
          show-size-picker
          :page-sizes="[10, 20, 50, 100]"
          @update:page="handlePageChange"
          @update:page-size="handlePageSizeChange"
        />
      </div>
    </NCard>

    <!-- 处理弹窗 -->
    <NModal v-model:show="showHandleModal" :mask-closable="false">
      <NCard
        style="max-width: 600px"
        title="处理举报"
        :bordered="false"
        size="huge"
        role="dialog"
        aria-modal
      >
        <template #header-extra>
          <NButton
            quaternary
            circle
            @click="showHandleModal = false"
          >
            <template #icon>
              <span class="close-icon">×</span>
            </template>
          </NButton>
        </template>

        <div v-if="selectedReport" class="handle-form">
          <div class="report-summary">
            <h4>举报概要</h4>
            <p><strong>举报类型：</strong>{{ getTypeText(selectedReport.type) }}</p>
            <p><strong>举报对象：</strong>{{ selectedReport.targetType }} - {{ selectedReport.targetInfo }}</p>
            <p><strong>举报内容：</strong>{{ selectedReport.reason }}</p>
          </div>

          <NForm
            ref="handleFormRef"
            :model="handleForm"
            label-placement="left"
            label-width="auto"
          >
            <NFormItem
              label="处理结果"
              :rule="[
                { required: true, message: '请选择处理结果', trigger: 'change' }
              ]"
            >
              <NRadioGroup v-model:value="handleForm.result">
                <NRadio value="valid">举报成立</NRadio>
                <NRadio value="invalid">举报不成立</NRadio>
                <NRadio value="partial">部分成立</NRadio>
              </NRadioGroup>
            </NFormItem>

            <NFormItem
              label="处理方式"
              :rule="[
                { required: true, message: '请选择处理方式', trigger: 'change' }
              ]"
            >
              <NSelect
                v-model:value="handleForm.action"
                :options="actionOptions"
                placeholder="请选择处理方式"
              />
            </NFormItem>

            <NFormItem
              label="处理说明"
              :rule="[
                { required: true, message: '请输入处理说明', trigger: ['blur', 'input'] }
              ]"
            >
              <NInput
                v-model:value="handleForm.description"
                type="textarea"
                placeholder="请输入处理说明和处理依据"
                :rows="4"
              />
            </NFormItem>

            <NFormItem label="后续处理">
              <NCheckboxGroup v-model:value="handleForm.followUp">
                <NCheckbox value="warn">警告用户</NCheckbox>
                <NCheckbox value="delete_content">删除内容</NCheckbox>
                <NCheckbox value="block_user">封禁用户</NCheckbox>
                <NCheckbox value="monitor">重点监控</NCheckbox>
              </NCheckboxGroup>
            </NFormItem>
          </NForm>
        </div>

        <template #footer>
          <div class="modal-actions">
            <NButton @click="showHandleModal = false">取消</NButton>
            <NButton
              type="primary"
              :loading="handleLoading"
              @click="confirmHandle"
            >
              确认处理
            </NButton>
          </div>
        </template>
      </NCard>
    </NModal>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  NCard,
  NSelect,
  NInput,
  NButton,
  NTag,
  NPagination,
  NModal,
  NForm,
  NFormItem,
  NRadioGroup,
  NRadio,
  NCheckboxGroup,
  NCheckbox,
  FormInst,
  useMessage,
  useDialog
} from 'naive-ui'
import dayjs from 'dayjs'

interface Report {
  id: string
  type: 'spam' | 'inappropriate' | 'fake_job' | 'fraud' | 'harassment' | 'other'
  status: 'pending' | 'processing' | 'resolved' | 'rejected'
  priority: 'low' | 'medium' | 'high' | 'urgent'
  reason: string
  targetType: 'job' | 'user' | 'company' | 'comment'
  targetInfo: string
  targetId: string
  reporter: string
  reporterId: string
  createTime: string
  evidence?: Array<{
    type: string
    name: string
    url?: string
  }>
  result?: string
  action?: string
  description?: string
}

interface Filters {
  type: string | null
  status: string | null
  priority: string | null
}

interface HandleForm {
  result: string
  action: string
  description: string
  followUp: string[]
}

const router = useRouter()
const message = useMessage()
const dialog = useDialog()

// 筛选选项
const typeOptions = [
  { label: '垃圾信息', value: 'spam' },
  { label: '不当内容', value: 'inappropriate' },
  { label: '虚假职位', value: 'fake_job' },
  { label: '欺诈行为', value: 'fraud' },
  { label: '骚扰行为', value: 'harassment' },
  { label: '其他', value: 'other' }
]

const statusOptions = [
  { label: '待处理', value: 'pending' },
  { label: '处理中', value: 'processing' },
  { label: '已解决', value: 'resolved' },
  { label: '已驳回', value: 'rejected' }
]

const priorityOptions = [
  { label: '低', value: 'low' },
  { label: '中', value: 'medium' },
  { label: '高', value: 'high' },
  { label: '紧急', value: 'urgent' }
]

const actionOptions = [
  { label: '警告用户', value: 'warn' },
  { label: '删除内容', value: 'delete_content' },
  { label: '封禁账户', value: 'block_user' },
  { label: '内容下架', value: 'remove_content' },
  { label: '无需处理', value: 'no_action' }
]

// 状态管理
const searchKeyword = ref('')
const filters = ref<Filters>({
  type: null,
  status: null,
  priority: null
})
const currentPage = ref(1)
const pageSize = ref(20)

// 统计数据
const reportStats = ref({
  pending: 12,
  processing: 3,
  total: 156
})

// 弹窗状态
const showHandleModal = ref(false)
const selectedReport = ref<Report | null>(null)
const handleLoading = ref(false)
const handleFormRef = ref<FormInst | null>(null)
const handleForm = ref<HandleForm>({
  result: '',
  action: '',
  description: '',
  followUp: []
})

// 模拟举报数据
const reportsData = ref<Report[]>([
  {
    id: '1',
    type: 'fake_job',
    status: 'pending',
    priority: 'high',
    reason: '该职位信息涉嫌虚假，公司信息与实际不符，要求高薪但实际工作内容不符',
    targetType: 'job',
    targetInfo: '高级产品经理 - 某科技公司',
    targetId: 'job_123456',
    reporter: '张三',
    reporterId: 'user_789012',
    createTime: '2024-01-15 14:30:00',
    evidence: [
      { type: '截图', name: '职位详情页.png' },
      { type: '聊天记录', name: 'HR聊天记录.docx' }
    ]
  },
  {
    id: '2',
    type: 'spam',
    status: 'processing',
    priority: 'medium',
    reason: '用户发布大量重复的评论内容，涉嫌垃圾信息',
    targetType: 'user',
    targetInfo: '用户ID: user_456789',
    targetId: 'user_456789',
    reporter: '李四',
    reporterId: 'user_123456',
    createTime: '2024-01-15 10:15:00',
    evidence: [
      { type: '截图', name: '评论列表.png' }
    ]
  },
  {
    id: '3',
    type: 'harassment',
    status: 'pending',
    priority: 'urgent',
    reason: '用户通过私信发送骚扰信息，言语不当，要求用户转账',
    targetType: 'user',
    targetInfo: '用户ID: user_234567',
    targetId: 'user_234567',
    reporter: '王五',
    reporterId: 'user_345678',
    createTime: '2024-01-15 09:45:00',
    evidence: [
      { type: '聊天记录', name: '私信截图1.jpg' },
      { type: '聊天记录', name: '私信截图2.jpg' }
    ]
  },
  {
    id: '4',
    type: 'fraud',
    status: 'resolved',
    priority: 'high',
    reason: '冒充知名企业进行招聘，收取费用后失联',
    targetType: 'company',
    targetInfo: '某知名互联网科技有限公司',
    targetId: 'company_123456',
    reporter: '赵六',
    reporterId: 'user_567890',
    createTime: '2024-01-14 16:20:00',
    result: '举报成立',
    action: '封禁账户',
    description: '经核实，该企业确实存在欺诈行为，已封禁相关账户并删除所有职位信息'
  }
])

// 计算属性
const filteredReports = computed(() => {
  let filtered = reportsData.value

  // 类型筛选
  if (filters.value.type) {
    filtered = filtered.filter(report => report.type === filters.value.type)
  }

  // 状态筛选
  if (filters.value.status) {
    filtered = filtered.filter(report => report.status === filters.value.status)
  }

  // 优先级筛选
  if (filters.value.priority) {
    filtered = filtered.filter(report => report.priority === filters.value.priority)
  }

  // 关键词搜索
  if (searchKeyword.value.trim()) {
    const keyword = searchKeyword.value.toLowerCase()
    filtered = filtered.filter(report =>
      report.reason.toLowerCase().includes(keyword) ||
      report.targetInfo.toLowerCase().includes(keyword) ||
      report.reporter.toLowerCase().includes(keyword)
    )
  }

  // 按优先级和时间排序
  return filtered.sort((a, b) => {
    const priorityOrder = { urgent: 4, high: 3, medium: 2, low: 1 }
    const aPriority = priorityOrder[a.priority] || 0
    const bPriority = priorityOrder[b.priority] || 0

    if (aPriority !== bPriority) {
      return bPriority - aPriority
    }

    return dayjs(b.createTime).valueOf() - dayjs(a.createTime).valueOf()
  })
})

const paginatedReports = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredReports.value.slice(start, end)
})

// 辅助方法
const formatTime = (time: string) => {
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}

const getTypeType = (type: string) => {
  const typeMap: Record<string, string> = {
    spam: 'warning',
    inappropriate: 'error',
    fake_job: 'error',
    fraud: 'error',
    harassment: 'error',
    other: 'default'
  }
  return typeMap[type] || 'default'
}

const getTypeText = (type: string) => {
  const textMap: Record<string, string> = {
    spam: '垃圾信息',
    inappropriate: '不当内容',
    fake_job: '虚假职位',
    fraud: '欺诈行为',
    harassment: '骚扰行为',
    other: '其他'
  }
  return textMap[type] || type
}

const getStatusType = (status: string) => {
  const typeMap: Record<string, string> = {
    pending: 'warning',
    processing: 'info',
    resolved: 'success',
    rejected: 'error'
  }
  return typeMap[status] || 'default'
}

const getStatusText = (status: string) => {
  const textMap: Record<string, string> = {
    pending: '待处理',
    processing: '处理中',
    resolved: '已解决',
    rejected: '已驳回'
  }
  return textMap[status] || status
}

const getPriorityType = (priority: string) => {
  const typeMap: Record<string, string> = {
    low: 'success',
    medium: 'warning',
    high: 'error',
    urgent: 'error'
  }
  return typeMap[priority] || 'default'
}

const getPriorityText = (priority: string) => {
  const textMap: Record<string, string> = {
    low: '低',
    medium: '中',
    high: '高',
    urgent: '紧急'
  }
  return textMap[priority] || priority
}

// 事件处理
const handleFilter = () => {
  currentPage.value = 1
}

const handleSearch = (value: string) => {
  searchKeyword.value = value
  currentPage.value = 1
}

const handleRefresh = () => {
  message.success('数据已刷新')
  // 更新统计数据
  updateStats()
}

const resetFilters = () => {
  filters.value = {
    type: null,
    status: null,
    priority: null
  }
  searchKeyword.value = ''
  currentPage.value = 1
}

const handlePageChange = (page: number) => {
  currentPage.value = page
}

const handlePageSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
}

// 查看详情
const viewReportDetail = (report: Report) => {
  router.push(`/dashboard/reports/${report.id}`)
}

// 处理举报
const handleReport = (report: Report) => {
  selectedReport.value = report
  handleForm.value = {
    result: '',
    action: '',
    description: '',
    followUp: []
  }
  showHandleModal.value = true
}

// 确认处理
const confirmHandle = async () => {
  if (!selectedReport.value || !handleFormRef.value) return

  try {
    await handleFormRef.value.validate()
  } catch {
    return
  }

  handleLoading.value = true

  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 2000))

    // 更新举报状态
    const reportIndex = reportsData.value.findIndex(r => r.id === selectedReport.value!.id)
    if (reportIndex !== -1) {
      reportsData.value[reportIndex] = {
        ...reportsData.value[reportIndex],
        status: 'resolved',
        result: handleForm.value.result === 'valid' ? '举报成立' :
               handleForm.value.result === 'invalid' ? '举报不成立' : '部分成立',
        action: handleForm.value.action,
        description: handleForm.value.description
      }
    }

    message.success('举报处理完成')
    showHandleModal.value = false
    updateStats()

  } catch (error) {
    message.error('处理失败，请重试')
  } finally {
    handleLoading.value = false
  }
}

// 更新统计数据
const updateStats = () => {
  const pending = reportsData.value.filter(r => r.status === 'pending').length
  const processing = reportsData.value.filter(r => r.status === 'processing').length
  const total = reportsData.value.length

  reportStats.value = {
    pending,
    processing,
    total
  }
}

// 页面初始化
onMounted(() => {
  updateStats()
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

        &.high-priority {
          border-left: 4px solid var(--error-color);
        }

        &.urgent {
          border-left: 4px solid var(--error-color);
          background: rgba(245, 34, 45, 0.05);
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

  // 处理弹窗
  .handle-form {
    .report-summary {
      margin-bottom: 24px;
      padding: 16px;
      background: var(--bg-secondary);
      border-radius: 8px;

      h4 {
        margin: 0 0 12px 0;
        font-size: 16px;
        font-weight: 600;
        color: var(--text-primary);
      }

      p {
        margin: 0 0 4px 0;
        font-size: 14px;
        color: var(--text-secondary);

        strong {
          color: var(--text-primary);
        }
      }
    }
  }

  .modal-actions {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
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