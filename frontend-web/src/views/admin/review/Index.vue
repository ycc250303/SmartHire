<template>
  <div class="review-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">招聘审核</h1>
        <p class="page-description">审核用户发布的招聘职位，确保信息准确合规</p>
      </div>
    </div>

    <!-- 状态筛选标签页 -->
    <NCard :bordered="false" class="filter-card">
      <NTabs v-model:value="activeTab" type="segment" animated>
        <NTabPane
          :name="tab.value"
          v-for="tab in statusTabs"
          :key="tab.value"
          :tab="() => tab.label"
        >
          <div class="tab-content">
            <div class="tab-header">
              <span class="tab-description">{{ tab.description }}</span>
              <div class="tab-count">
                <NTag :type="tab.type" size="small">{{ tab.count }}</NTag>
              </div>
            </div>
          </div>
        </NTabPane>
      </NTabs>
    </NCard>

    <!-- 搜索和筛选 -->
    <NCard :bordered="false" class="search-card">
      <div class="search-section">
        <div class="search-input">
          <NInput
            v-model:value="searchKeyword"
            placeholder="搜索职位名称、公司名称"
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
        </div>
      </div>
    </NCard>

    <!-- 职位列表 -->
    <NCard :bordered="false" class="list-card">
      <div class="list-header">
        <span class="list-title">{{ currentTab.title }}</span>
        <div class="list-actions">
          <span class="total-count">共 {{ filteredJobs.length }} 条记录</span>
        </div>
      </div>

      <div class="job-list">
        <div
          v-for="job in filteredJobs"
          :key="job.id"
          class="job-item"
          @click="viewJobDetail(job)"
        >
          <div class="job-header">
            <div class="job-title">
              <h3>{{ job.title }}</h3>
              <NTag :type="getStatusType(job.status)" size="small">
                {{ getStatusText(job.status) }}
              </NTag>
            </div>
            <div class="job-actions">
              <NButton
                v-if="job.status === 'pending'"
                size="small"
                type="success"
                @click.stop="handleApprove(job)"
              >
                通过
              </NButton>
              <NButton
                v-if="job.status === 'pending'"
                size="small"
                type="warning"
                @click.stop="handleModify(job)"
              >
                要求修改
              </NButton>
              <NButton
                v-if="job.status === 'pending'"
                size="small"
                type="error"
                @click.stop="handleReject(job)"
              >
                拒绝
              </NButton>
            </div>
          </div>

          <div class="job-company">
            <div class="company-info">
              <span class="company-name">{{ job.company }}</span>
              <span class="company-location">📍 {{ job.location }}</span>
            </div>
          </div>

          <div class="job-meta">
            <div class="meta-left">
              <span class="salary">💰 {{ job.salary }}</span>
              <span class="experience">🎓 {{ job.experience }}</span>
              <span class="education">🎓 {{ job.education }}</span>
            </div>
            <div class="meta-right">
              <span class="publisher">发布者：{{ job.publisher }}</span>
              <span class="time">发布时间：{{ formatTime(job.createTime) }}</span>
            </div>
          </div>

          <div class="job-tags" v-if="job.tags && job.tags.length > 0">
            <NTag
              v-for="tag in job.tags"
              :key="tag"
              size="small"
              type="info"
              class="job-tag"
            >
              {{ tag }}
            </NTag>
          </div>

          <div class="job-description" v-if="job.description">
            <p class="description-text">{{ job.description }}</p>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="filteredJobs.length === 0" class="empty-state">
        <div class="empty-icon">📋</div>
        <h3 class="empty-title">暂无{{ currentTab.emptyText }}</h3>
        <p class="empty-description">{{ currentTab.emptyDesc }}</p>
      </div>
    </NCard>

    <!-- 审核操作弹窗 -->
    <NModal v-model:show="showActionModal" :mask-closable="false">
      <NCard
        style="max-width: 600px"
        title="审核确认"
        :bordered="false"
        size="huge"
        role="dialog"
        aria-modal
      >
        <template #header-extra>
          <NButton
            quaternary
            circle
            @click="showActionModal = false"
          >
            <template #icon>
              <span class="close-icon">×</span>
            </template>
          </NButton>
        </template>

        <div class="modal-content">
          <div class="job-preview">
            <h4>{{ currentJob?.title }}</h4>
            <p class="preview-company">{{ currentJob?.company }}</p>
          </div>

          <div class="action-form" v-if="actionType === 'reject' || actionType === 'modify'">
            <NForm
              ref="actionFormRef"
              :model="actionForm"
              label-placement="left"
              label-width="auto"
            >
              <NFormItem
                label="审核意见"
                :rule="[
                  { required: true, message: '请输入审核意见', trigger: ['blur', 'input'] }
                ]"
              >
                <NInput
                  v-model:value="actionForm.reason"
                  type="textarea"
                  placeholder="请输入审核意见"
                  :rows="4"
                />
              </NFormItem>
            </NForm>
          </div>

          <div class="action-message" v-else>
            <p>确定要{{ actionText }}这条职位吗？</p>
          </div>
        </div>

        <template #footer>
          <div class="modal-actions">
            <NButton @click="showActionModal = false">取消</NButton>
            <NButton
              type="primary"
              :loading="actionLoading"
              @click="confirmAction"
              :disabled="actionType === 'reject' && !actionForm.reason.trim()"
            >
              {{ actionText }}
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
  NTabs,
  NTabPane,
  NInput,
  NButton,
  NTag,
  NModal,
  NForm,
  NFormItem,
  FormInst,
  useMessage,
  useDialog
} from 'naive-ui'
import dayjs from 'dayjs'

interface Job {
  id: string
  title: string
  company: string
  location: string
  salary: string
  experience: string
  education: string
  description?: string
  status: 'pending' | 'approved' | 'rejected' | 'modified'
  publisher: string
  createTime: string
  tags?: string[]
}

interface StatusTab {
  value: string
  label: string
  description: string
  count: number
  type: 'info' | 'success' | 'warning' | 'error'
  emptyText: string
  emptyDesc: string
}

const router = useRouter()
const message = useMessage()
const dialog = useDialog()

// 状态标签页
const activeTab = ref('pending')
const statusTabs: StatusTab[] = [
  {
    value: 'pending',
    label: '待审核',
    description: '需要审核的职位',
    count: 12,
    type: 'info',
    emptyText: '待审核职位',
    emptyDesc: '当前没有需要审核的职位'
  },
  {
    value: 'approved',
    label: '已通过',
    description: '已通过审核的职位',
    count: 45,
    type: 'success',
    emptyText: '已通过职位',
    emptyDesc: '当前没有已通过的职位'
  },
  {
    value: 'rejected',
    label: '已拒绝',
    description: '被拒绝的职位',
    count: 8,
    type: 'error',
    emptyText: '已拒绝职位',
    emptyDesc: '当前没有已拒绝的职位'
  },
  {
    value: 'modified',
    label: '需修改',
    description: '需要修改的职位',
    count: 3,
    type: 'warning',
    emptyText: '需修改职位',
    emptyDesc: '当前没有需要修改的职位'
  }
]

// 状态数据
const jobsData = ref<Job[]>([
  {
    id: '1',
    title: '高级前端开发工程师',
    company: '北京字节跳动科技有限公司',
    location: '北京市朝阳区',
    salary: '25k-35k·13薪',
    experience: '3-5年',
    education: '本科及以上',
    description: '负责公司核心产品的前端开发工作，需要熟练掌握Vue.js、React等主流前端框架，有大型项目经验者优先。',
    status: 'pending',
    publisher: '张三',
    createTime: '2024-01-15 10:30:00',
    tags: ['Vue.js', 'React', 'TypeScript', 'Node.js']
  },
  {
    id: '2',
    title: 'Java后端开发工程师',
    company: '阿里巴巴集团控股有限公司',
    location: '杭州市余杭区',
    salary: '30k-50k·16薪',
    experience: '5-8年',
    education: '本科及以上',
    description: '参与电商平台的架构设计和开发，熟悉微服务架构，有高并发处理经验。',
    status: 'pending',
    publisher: '李四',
    createTime: '2024-01-15 09:45:00',
    tags: ['Java', 'Spring Boot', '微服务', 'MySQL']
  },
  {
    id: '3',
    title: '产品经理',
    company: '腾讯计算机系统有限公司',
    location: '深圳市南山区',
    salary: '25k-40k·15薪',
    experience: '3-5年',
    education: '本科及以上',
    description: '负责互联网产品的规划和设计，有B端或C端产品设计经验，能够独立完成产品方案设计。',
    status: 'approved',
    publisher: '王五',
    createTime: '2024-01-14 16:20:00',
    tags: ['产品设计', '数据分析', '用户调研']
  },
  {
    id: '4',
    title: 'UI设计师',
    company: '网易（杭州）网络有限公司',
    location: '杭州市滨江区',
    salary: '15k-25k·14薪',
    experience: '2-4年',
    education: '本科及以上',
    description: '负责移动端和Web端的界面设计，熟悉设计工具，有完整的设计作品集。',
    status: 'rejected',
    publisher: '赵六',
    createTime: '2024-01-13 14:15:00',
    tags: ['Figma', 'Sketch', 'Adobe XD', 'Photoshop']
  }
])

// 搜索和筛选
const searchKeyword = ref('')
const actionType = ref<'approve' | 'reject' | 'modify'>('approve')
const actionText = computed(() => {
  const textMap = {
    approve: '通过',
    reject: '拒绝',
    modify: '要求修改'
  }
  return textMap[actionType.value] || '操作'
})

// 审核弹窗
const showActionModal = ref(false)
const actionLoading = ref(false)
const currentJob = ref<Job | null>(null)
const actionFormRef = ref<FormInst | null>(null)
const actionForm = ref({
  reason: ''
})

// 计算属性
const currentTab = computed(() => statusTabs.find(tab => tab.value === activeTab.value)!)
const filteredJobs = computed(() => {
  let filtered = jobsData.value.filter(job => job.status === activeTab.value)

  if (searchKeyword.value.trim()) {
    const keyword = searchKeyword.value.toLowerCase()
    filtered = filtered.filter(job =>
      job.title.toLowerCase().includes(keyword) ||
      job.company.toLowerCase().includes(keyword) ||
      job.publisher.toLowerCase().includes(keyword)
    )
  }

  return filtered
})

// 获取当前标签页的标题
const getCurrentTabTitle = computed(() => {
  const tab = statusTabs.find(t => t.value === activeTab.value)
  return tab ? tab.label : '职位列表'
})

// 获取状态类型
const getStatusType = (status: string) => {
  const typeMap: Record<string, string> = {
    pending: 'info',
    approved: 'success',
    rejected: 'error',
    modified: 'warning'
  }
  return typeMap[status] || 'default'
}

// 获取状态文本
const getStatusText = (status: string) => {
  const textMap: Record<string, string> = {
    pending: '待审核',
    approved: '已通过',
    rejected: '已拒绝',
    modified: '需修改'
  }
  return textMap[status] || status
}

// 格式化时间
const formatTime = (time: string) => {
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}

// 搜索处理
const handleSearch = (value: string) => {
  searchKeyword.value = value
}

// 刷新数据
const handleRefresh = () => {
  message.success('数据已刷新')
  // 这里可以调用API刷新数据
}

// 查看职位详情
const viewJobDetail = (job: Job) => {
  router.push(`/dashboard/review/${job.id}`)
}

// 通过审核
const handleApprove = (job: Job) => {
  currentJob.value = job
  actionType.value = 'approve'
  showActionModal.value = true
}

// 拒绝审核
const handleReject = (job: Job) => {
  currentJob.value = job
  actionType.value = 'reject'
  actionForm.value.reason = ''
  showActionModal.value = true
}

// 要求修改
const handleModify = (job: Job) => {
  currentJob.value = job
  actionType.value = 'modify'
  actionForm.value.reason = ''
  showActionModal.value = true
}

// 确认审核操作
const confirmAction = async () => {
  if (!currentJob.value) return

  actionLoading.value = true

  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1500))

    const actionTexts = {
      approve: '通过',
      reject: '拒绝',
      modify: '要求修改'
    }

    // 更新职位状态
    if (actionType.value === 'approve') {
      currentJob.value.status = 'approved'
      message.success('职位审核通过')
    } else if (actionType.value === 'reject') {
      currentJob.value.status = 'rejected'
      message.warning(`职位已拒绝，原因：${actionForm.value.reason}`)
    } else if (actionType.value === 'modify') {
      currentJob.value.status = 'modified'
      message.info(`已要求修改职位，建议：${actionForm.value.reason}`)
    }

    // 更新状态统计
    const tab = statusTabs.find(t => t.value === currentJob.value.status)
    if (tab) {
      tab.count = jobsData.value.filter(job => job.status === tab.value).length
    }

    showActionModal.value = false
  } catch (error) {
    console.error('审核操作失败:', error)
    message.error('操作失败，请重试')
  } finally {
    actionLoading.value = false
  }
}

// 页面初始化
onMounted(() => {
  // 这里可以调用API获取真实数据
})
</script>

<style scoped lang="scss">
.review-page {
  // 页面头部
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 32px;
    padding: 24px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
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
  }

  // 状态筛选卡片
  .filter-card {
    margin-bottom: 24px;

    .tab-content {
      padding: 16px 0;

      .tab-header {
        display: flex;
        justify-content: space-between;
        align-items: center;

        .tab-description {
          color: var(--text-secondary);
          font-size: 14px;
        }

        .tab-count {
          .n-tag {
            font-weight: 500;
          }
        }
      }
    }
  }

  // 搜索卡片
  .search-card {
    margin-bottom: 24px;

    .search-section {
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
        .n-button {
          min-width: 80px;
        }
      }
    }
  }

  // 职位列表卡片
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

    .job-list {
      .job-item {
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

        &:last-child {
          margin-bottom: 0;
        }

        .job-header {
          display: flex;
          justify-content: space-between;
          align-items: flex-start;
          margin-bottom: 12px;

          .job-title {
            display: flex;
            align-items: center;
            gap: 12px;

            h3 {
              margin: 0;
              font-size: 18px;
              font-weight: 600;
              color: var(--text-primary);
            }

            .n-tag {
              font-size: 12px;
            }
          }

          .job-actions {
            display: flex;
            gap: 8px;

            .n-button {
              min-width: 70px;
            }
          }
        }

        .job-company {
          margin-bottom: 12px;

          .company-info {
            display: flex;
            align-items: center;
            gap: 16px;

            .company-name {
              font-size: 16px;
              font-weight: 500;
              color: var(--text-primary);
            }

            .company-location {
              font-size: 14px;
              color: var(--text-secondary);
            }
          }
        }

        .job-meta {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 12px;

          .meta-left {
            display: flex;
            gap: 16px;

            .salary, .experience, .education {
              font-size: 14px;
              color: var(--text-secondary);
            }
          }

          .meta-right {
            display: flex;
            gap: 16px;

            .publisher, .time {
              font-size: 13px;
              color: var(--text-disabled);
            }
          }
        }

        .job-tags {
          display: flex;
          gap: 8px;
          margin-bottom: 12px;

          .job-tag {
            .n-tag {
              font-size: 12px;
              background: rgba(32, 128, 240, 0.1);
              border-color: rgba(32, 128, 240, 0.2);
            }
          }
        }

        .job-description {
          .description-text {
            margin: 0;
            font-size: 14px;
            color: var(--text-secondary);
            line-height: 1.5;
            display: -webkit-box;
            -webkit-line-clamp: 2;
            -webkit-box-orient: vertical;
            overflow: hidden;
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
  }

  // 审核操作弹窗
  .modal-content {
    .job-preview {
      margin-bottom: 24px;
      padding: 16px;
      background: var(--bg-secondary);
      border-radius: 8px;

      h4 {
        margin: 0 0 8px 0;
        font-size: 16px;
        font-weight: 600;
        color: var(--text-primary);
      }

      .preview-company {
        margin: 0;
        font-size: 14px;
        color: var(--text-secondary);
      }
    }

    .action-form {
      margin-bottom: 16px;
    }

    .action-message {
      text-align: center;
      padding: 16px;
      color: var(--text-secondary);
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
  .review-page {
    .page-header {
      padding: 16px;

      .header-content {
        .page-title {
          font-size: 24px;
        }

        .page-description {
          font-size: 14px;
        }
      }
    }

    .search-section {
      flex-direction: column;
      gap: 12px;

      .search-actions {
        align-self: flex-end;
      }
    }

    .job-list {
      .job-item {
        padding: 16px;

        .job-header {
          flex-direction: column;
          gap: 12px;
          align-items: flex-start;

          .job-actions {
            width: 100%;
            justify-content: flex-end;
          }
        }

        .job-meta {
          flex-direction: column;
          align-items: flex-start;
          gap: 8px;

          .meta-left {
            gap: 12px;
          }

          .meta-right {
            gap: 12px;
          }
        }

        .job-tags {
          flex-wrap: wrap;
        }
      }
    }
  }
}
</style>