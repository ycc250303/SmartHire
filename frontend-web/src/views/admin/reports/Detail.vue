<template>
  <div class="report-detail">
    <NCard :bordered="false">
      <template #header>
        <NSpace align="center">
          <NButton
            text
            @click="router.push('/dashboard/reports')"
          >
            <template #icon>
              <span class="icon">←</span>
            </template>
            返回举报列表
          </NButton>
          <NDivider vertical />
          <span class="detail-title">举报详情 #{{ reportId }}</span>
        </NSpace>
      </template>

      <div v-if="loading" class="loading-container">
        <NSpin size="large" />
      </div>

      <div v-else class="detail-content">
        <!-- 举报基本信息 -->
        <NCard title="举报信息" size="small" :bordered="false" class="info-card">
          <NDescriptions :column="2">
            <NDescriptionsItem label="举报编号">
              #{{ reportData.id }}
            </NDescriptionsItem>
            <NDescriptionsItem label="举报类型">
              <NTag :type="getReportTypeTagType(reportData.type)" size="small">
                {{ getReportTypeLabel(reportData.type) }}
              </NTag>
            </NDescriptionsItem>
            <NDescriptionsItem label="优先级">
              <NTag :type="getPriorityTagType(reportData.priority)" size="small">
                {{ getPriorityLabel(reportData.priority) }}
              </NTag>
            </NDescriptionsItem>
            <NDescriptionsItem label="处理状态">
              <NTag :type="getStatusTagType(reportData.status)" size="small">
                {{ getStatusLabel(reportData.status) }}
              </NTag>
            </NDescriptionsItem>
            <NDescriptionsItem label="举报人">
              <NSpace align="center">
                <NAvatar
                  :size="24"
                  :src="reportData.reporter.avatar"
                  fallback-src="/default-avatar.png"
                  round
                />
                <span>{{ reportData.reporter.name }}</span>
                <NText depth="3">({{ reportData.reporter.type }})</NText>
              </NSpace>
            </NDescriptionsItem>
            <NDescriptionsItem label="举报时间">
              {{ formatTime(reportData.createdAt) }}
            </NDescriptionsItem>
            <NDescriptionsItem label="被举报对象">
              <NSpace align="center">
                <NAvatar
                  :size="24"
                  :src="reportData.target.user.avatar"
                  fallback-src="/default-avatar.png"
                  round
                />
                <span>{{ reportData.target.user.name }}</span>
                <NText depth="3">({{ reportData.target.type }})</NText>
              </NSpace>
            </NDescriptionsItem>
            <NDescriptionsItem label="涉及内容">
              <NButton text @click="viewTargetContent">
                查看相关{{ reportData.target.type === 'job' ? '职位' : '用户' }}
              </NButton>
            </NDescriptionsItem>
          </NDescriptions>
        </NCard>

        <!-- 举报详情 -->
        <NCard title="举报描述" size="small" :bordered="false" class="info-card">
          <div class="report-description">
            <p>{{ reportData.description }}</p>
          </div>

          <!-- 举报证据 -->
          <div v-if="reportData.evidence && reportData.evidence.length > 0" class="evidence-section">
            <h4>相关证据</h4>
            <NSpace>
              <div
                v-for="(evidence, index) in reportData.evidence"
                :key="index"
                class="evidence-item"
              >
                <NImage
                  v-if="evidence.type === 'image'"
                  :src="evidence.url"
                  width="100"
                  height="100"
                  object-fit="cover"
                  preview
                />
                <NButton
                  v-else
                  text
                  @click="downloadEvidence(evidence)"
                >
                  <template #icon>
                    <span class="icon">📄</span>
                  </template>
                  {{ evidence.name }}
                </NButton>
              </div>
            </NSpace>
          </div>
        </NCard>

        <!-- 处理历史 -->
        <NCard title="处理历史" size="small" :bordered="false" class="info-card">
          <NTimeline>
            <NTimelineItem
              v-for="(history, index) in reportData.history"
              :key="index"
              :type="getHistoryType(history.action)"
              :time="formatTime(history.createdAt)"
            >
              <template #header>
                <NSpace align="center">
                  <span>{{ history.handler.name }}</span>
                  <NTag size="small" :type="getActionTagType(history.action)">
                    {{ getActionLabel(history.action) }}
                  </NTag>
                </NSpace>
              </template>
              <div class="history-content">
                <p>{{ history.comment }}</p>
                <div v-if="history.details" class="history-details">
                  <NDescriptions size="tiny" :column="1">
                    <NDescriptionsItem
                      v-for="(value, key) in history.details"
                      :key="key"
                      :label="key"
                    >
                      {{ value }}
                    </NDescriptionsItem>
                  </NDescriptions>
                </div>
              </div>
            </NTimelineItem>
          </NTimeline>
        </NCard>

        <!-- 相关内容预览 -->
        <NCard
          v-if="showTargetContent"
          :title="`相关${reportData.target.type === 'job' ? '职位' : '用户'}信息`"
          size="small"
          :bordered="false"
          class="info-card"
          closable
          @close="showTargetContent = false"
        >
          <!-- 职位信息 -->
          <div v-if="reportData.target.type === 'job'" class="job-preview">
            <NDescriptions :column="2">
              <NDescriptionsItem label="职位名称">
                {{ reportData.target.content.title }}
              </NDescriptionsItem>
              <NDescriptionsItem label="公司名称">
                {{ reportData.target.content.company }}
              </NDescriptionsItem>
              <NDescriptionsItem label="薪资范围">
                {{ reportData.target.content.salary }}
              </NDescriptionsItem>
              <NDescriptionsItem label="工作地点">
                {{ reportData.target.content.location }}
              </NDescriptionsItem>
              <NDescriptionsItem label="工作经验">
                {{ reportData.target.content.experience }}
              </NDescriptionsItem>
              <NDescriptionsItem label="学历要求">
                {{ reportData.target.content.education }}
              </NDescriptionsItem>
              <NDescriptionsItem label="发布时间">
                {{ formatTime(reportData.target.content.publishTime) }}
              </NDescriptionsItem>
              <NDescriptionsItem label="当前状态">
                <NTag :type="getJobStatusType(reportData.target.content.status)">
                  {{ reportData.target.content.status }}
                </NTag>
              </NDescriptionsItem>
            </NDescriptions>

            <div class="job-description">
              <h4>职位描述</h4>
              <p>{{ reportData.target.content.description }}</p>
            </div>
          </div>

          <!-- 用户信息 -->
          <div v-else class="user-preview">
            <NDescriptions :column="2">
              <NDescriptionsItem label="用户名">
                {{ reportData.target.content.username }}
              </NDescriptionsItem>
              <NDescriptionsItem label="真实姓名">
                {{ reportData.target.content.realName }}
              </NDescriptionsItem>
              <NDescriptionsItem label="用户类型">
                <NTag :type="reportData.target.content.type === 'hr' ? 'info' : 'success'">
                  {{ reportData.target.content.type === 'hr' ? 'HR用户' : '求职者' }}
                </NTag>
              </NDescriptionsItem>
              <NDescriptionsItem label="注册时间">
                {{ formatTime(reportData.target.content.registerTime) }}
              </NDescriptionsItem>
              <NDescriptionsItem label="账户状态">
                <NTag :type="getUserStatusType(reportData.target.content.status)">
                  {{ reportData.target.content.status }}
                </NTag>
              </NDescriptionsItem>
              <NDescriptionsItem label="信用评分">
                {{ reportData.target.content.creditScore }}
              </NDescriptionsItem>
            </NDescriptions>

            <div v-if="reportData.target.content.stats" class="user-stats">
              <h4>用户统计</h4>
              <NSpace>
                <NStatistic label="发布职位" :value="reportData.target.content.stats.jobCount" />
                <NStatistic label="收到申请" :value="reportData.target.content.stats.applicationCount" />
                <NStatistic label="登录次数" :value="reportData.target.content.stats.loginCount" />
                <NStatistic label="被举报次数" :value="reportData.target.content.stats.reportCount" />
              </NSpace>
            </div>
          </div>
        </NCard>
      </div>

      <!-- 处理操作 -->
      <div v-if="!loading && reportData.status === 'pending'" class="action-section">
        <NDivider />
        <NSpace justify="center">
          <NButton @click="openHandleModal('ignore')">
            忽略举报
          </NButton>
          <NButton @click="openHandleModal('warning')">
            警告处理
          </NButton>
          <NButton type="warning" @click="openHandleModal('suspend')">
            暂停账户
          </NButton>
          <NButton type="error" @click="openHandleModal('ban')">
            封禁账户
          </NButton>
        </NSpace>
      </div>
    </NCard>

    <!-- 处理弹窗 -->
    <NModal
      v-model:show="handleModalVisible"
      :mask-closable="false"
      preset="card"
      title="处理举报"
      style="width: 600px;"
    >
      <NForm
        ref="handleFormRef"
        :model="handleForm"
        :rules="handleRules"
        label-placement="left"
        label-width="100px"
      >
        <NFormItem label="处理结果" path="result">
          <NRadioGroup v-model:value="handleForm.result">
            <NSpace vertical>
              <NRadio value="ignore">忽略举报 - 举报不成立，无需处理</NRadio>
              <NRadio value="warning">警告处理 - 对用户进行警告</NRadio>
              <NRadio value="suspend">暂停账户 - 暂时冻结用户账户</NRadio>
              <NRadio value="ban">封禁账户 - 永久封禁用户账户</NRadio>
            </NSpace>
          </NRadioGroup>
        </NFormItem>

        <NFormItem label="处理原因" path="reason">
          <NInput
            v-model:value="handleForm.reason"
            type="textarea"
            :rows="4"
            placeholder="请输入处理原因（内部记录）"
          />
        </NFormItem>

        <NFormItem label="通知用户" path="notifyUser">
          <NSwitch v-model:value="handleForm.notifyUser">
            <template #checked>发送通知</template>
            <template #unchecked>不通知</template>
          </NSwitch>
        </NFormItem>

        <NFormItem
          v-if="handleForm.notifyUser"
          label="通知内容"
          path="notificationContent"
        >
          <NInput
            v-model:value="handleForm.notificationContent"
            type="textarea"
            :rows="3"
            placeholder="发送给用户的通知内容"
          />
        </NFormItem>

        <!-- 处罚时长设置 -->
        <NFormItem
          v-if="handleForm.result === 'suspend'"
          label="暂停时长"
          path="suspendDuration"
        >
          <NSelect
            v-model:value="handleForm.suspendDuration"
            :options="suspendDurationOptions"
            placeholder="选择暂停时长"
          />
        </NFormItem>
      </NForm>

      <template #footer>
        <NSpace justify="end">
          <NButton @click="handleModalVisible = false">取消</NButton>
          <NButton
            type="primary"
            :loading="handling"
            @click="handleSubmit"
          >
            确认处理
          </NButton>
        </NSpace>
      </template>
    </NModal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  NCard, NSpace, NButton, NDescriptions, NDescriptionsItem, NTag,
  NAvatar, NText, NSpin, NImage, NTimeline, NTimelineItem, NModal,
  NForm, NFormItem, NRadioGroup, NRadio, NInput, NSwitch, NSelect,
  NDivider, NStatistic, useMessage, useDialog
} from 'naive-ui'
import type { FormRules } from 'naive-ui'
import dayjs from 'dayjs'

const router = useRouter()
const route = useRoute()
const message = useMessage()
const dialog = useDialog()

// 获取举报ID
const reportId = computed(() => route.params.id as string)

// 状态
const loading = ref(true)
const handleModalVisible = ref(false)
const handling = ref(false)
const showTargetContent = ref(false)

// 表单引用
const handleFormRef = ref()

// 举报数据
const reportData = ref<any>({})

// 处理表单
const handleForm = reactive({
  result: '',
  reason: '',
  notifyUser: false,
  notificationContent: '',
  suspendDuration: ''
})

// 表单验证规则
const handleRules: FormRules = {
  result: [
    { required: true, message: '请选择处理结果', trigger: 'change' }
  ],
  reason: [
    { required: true, message: '请输入处理原因', trigger: 'blur' }
  ]
}

// 暂停时长选项
const suspendDurationOptions = [
  { label: '3天', value: '3d' },
  { label: '7天', value: '7d' },
  { label: '15天', value: '15d' },
  { label: '30天', value: '30d' },
  { label: '90天', value: '90d' }
]

// 工具方法
const formatTime = (time: string | Date) => {
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss')
}

const getReportTypeLabel = (type: string) => {
  const typeMap: Record<string, string> = {
    spam: '垃圾信息',
    fraud: '欺诈行为',
    harassment: '骚扰行为',
    inappropriate: '不当内容',
    copyright: '版权侵犯',
    other: '其他'
  }
  return typeMap[type] || type
}

const getReportTypeTagType = (type: string) => {
  const typeMap: Record<string, any> = {
    spam: 'default',
    fraud: 'error',
    harassment: 'warning',
    inappropriate: 'info',
    copyright: 'warning',
    other: 'default'
  }
  return typeMap[type] || 'default'
}

const getPriorityLabel = (priority: string) => {
  const priorityMap: Record<string, string> = {
    low: '普通',
    medium: '重要',
    high: '紧急'
  }
  return priorityMap[priority] || priority
}

const getPriorityTagType = (priority: string) => {
  const priorityMap: Record<string, any> = {
    low: 'default',
    medium: 'warning',
    high: 'error'
  }
  return priorityMap[priority] || 'default'
}

const getStatusLabel = (status: string) => {
  const statusMap: Record<string, string> = {
    pending: '待处理',
    processing: '处理中',
    resolved: '已解决',
    rejected: '已驳回'
  }
  return statusMap[status] || status
}

const getStatusTagType = (status: string) => {
  const statusMap: Record<string, any> = {
    pending: 'warning',
    processing: 'info',
    resolved: 'success',
    rejected: 'error'
  }
  return statusMap[status] || 'default'
}

const getActionLabel = (action: string) => {
  const actionMap: Record<string, string> = {
    create: '创建举报',
    assign: '分配处理',
    investigate: '调查取证',
    resolve: '处理完成',
    reject: '驳回举报',
    escalate: '上报处理'
  }
  return actionMap[action] || action
}

const getActionTagType = (action: string) => {
  const actionMap: Record<string, any> = {
    create: 'default',
    assign: 'info',
    investigate: 'warning',
    resolve: 'success',
    reject: 'error',
    escalate: 'warning'
  }
  return actionMap[action] || 'default'
}

const getHistoryType = (action: string) => {
  const typeMap: Record<string, any> = {
    create: 'default',
    assign: 'info',
    investigate: 'warning',
    resolve: 'success',
    reject: 'error',
    escalate: 'warning'
  }
  return typeMap[action] || 'default'
}

const getJobStatusType = (status: string) => {
  const statusMap: Record<string, any> = {
    active: 'success',
    inactive: 'warning',
    suspended: 'error'
  }
  return statusMap[status] || 'default'
}

const getUserStatusType = (status: string) => {
  const statusMap: Record<string, any> = {
    active: 'success',
    suspended: 'warning',
    banned: 'error'
  }
  return statusMap[status] || 'default'
}

// 事件处理
const viewTargetContent = () => {
  showTargetContent.value = true
}

const downloadEvidence = (evidence: any) => {
  // 下载证据文件
  window.open(evidence.url, '_blank')
}

const openHandleModal = (result: string) => {
  handleForm.result = result

  // 设置默认通知内容
  const notificationTemplates: Record<string, string> = {
    warning: '您的账户因违反平台规定受到警告，请注意规范使用行为。',
    suspend: '您的账户因严重违规被暂时冻结，请在期限到期后重新登录。',
    ban: '您的账户因严重违规被永久封禁，如有疑问请联系客服。'
  }

  if (result !== 'ignore' && notificationTemplates[result]) {
    handleForm.notifyUser = true
    handleForm.notificationContent = notificationTemplates[result]
  }

  handleModalVisible.value = true
}

const handleSubmit = async () => {
  try {
    await handleFormRef.value?.validate()

    handling.value = true

    // 模拟处理请求
    await new Promise(resolve => setTimeout(resolve, 2000))

    // 更新举报状态
    reportData.value.status = 'resolved'

    // 添加处理历史
    reportData.value.history.push({
      action: 'resolve',
      handler: {
        name: '当前管理员',
        avatar: '/admin-avatar.png'
      },
      comment: handleForm.reason,
      details: {
        '处理结果': getActionLabel(handleForm.result),
        '通知用户': handleForm.notifyUser ? '是' : '否',
        ...(handleForm.suspendDuration && { '暂停时长': handleForm.suspendDuration })
      },
      createdAt: new Date().toISOString()
    })

    message.success('举报处理成功')
    handleModalVisible.value = false
  } catch (error) {
    message.error('请完善处理信息')
  } finally {
    handling.value = false
  }
}

// 加载举报详情
const loadReportDetail = async () => {
  loading.value = true

  // 模拟API请求
  await new Promise(resolve => setTimeout(resolve, 1000))

  // 模拟举报数据
  reportData.value = {
    id: reportId.value,
    type: 'fraud',
    priority: 'high',
    status: 'pending',
    description: '该职位描述存在虚假信息，薪资待遇与实际不符，涉嫌欺诈求职者。经核实，该公司确实存在多起类似投诉，建议严肃处理。',
    createdAt: '2024-01-15T10:30:00Z',
    reporter: {
      id: 'user-001',
      name: '张三',
      type: '求职者',
      avatar: '/avatars/user-001.jpg'
    },
    target: {
      type: 'job',
      user: {
        id: 'user-002',
        name: '李四',
        type: 'HR用户',
        avatar: '/avatars/user-002.jpg'
      },
      content: {
        id: 'job-123',
        title: '高级前端工程师',
        company: '某科技有限公司',
        salary: '25K-35K',
        location: '北京朝阳区',
        experience: '3-5年',
        education: '本科',
        description: '诚聘高级前端工程师，负责公司核心产品的前端开发工作...',
        publishTime: '2024-01-10T09:00:00Z',
        status: 'active'
      }
    },
    evidence: [
      {
        type: 'image',
        url: '/evidence/chat-1.jpg',
        name: '聊天记录截图'
      },
      {
        type: 'document',
        url: '/evidence/complaint.pdf',
        name: '投诉信.pdf'
      }
    ],
    history: [
      {
        action: 'create',
        handler: {
          name: '张三',
          avatar: '/avatars/user-001.jpg'
        },
        comment: '提交举报',
        createdAt: '2024-01-15T10:30:00Z'
      },
      {
        action: 'assign',
        handler: {
          name: '系统自动',
          avatar: '/system-avatar.png'
        },
        comment: '举报已分配给管理员处理',
        details: {
          '分配给': '当前管理员',
          '优先级': '高'
        },
        createdAt: '2024-01-15T10:35:00Z'
      }
    ]
  }

  loading.value = false
}

onMounted(() => {
  loadReportDetail()
})
</script>

<style scoped lang="scss">
.report-detail {
  .detail-title {
    font-size: 16px;
    font-weight: 600;
  }

  .loading-container {
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 200px;
  }

  .detail-content {
    .info-card {
      margin-bottom: 16px;
    }

    .report-description {
      line-height: 1.6;
      margin-bottom: 16px;
    }

    .evidence-section {
      margin-top: 16px;

      h4 {
        margin-bottom: 12px;
        font-weight: 600;
      }

      .evidence-item {
        text-align: center;
        border: 1px solid var(--border-color);
        border-radius: 6px;
        padding: 8px;
        background: var(--bg-secondary);
      }
    }

    .history-content {
      .history-details {
        margin-top: 8px;
        padding: 12px;
        background: var(--bg-secondary);
        border-radius: 4px;
      }
    }

    .job-preview,
    .user-preview {
      .job-description,
      .user-stats {
        margin-top: 16px;

        h4 {
          margin-bottom: 12px;
          font-weight: 600;
        }
      }
    }
  }

  .action-section {
    margin-top: 24px;
    text-align: center;
  }
}

// 响应式设计
@media (max-width: 768px) {
  .report-detail {
    :deep(.n-descriptions) {
      .n-descriptions-table-content {
        grid-template-columns: 1fr;
      }
    }

    .evidence-section {
      :deep(.n-space) {
        flex-wrap: wrap;
      }
    }

    .action-section {
      :deep(.n-space) {
        flex-direction: column;
        align-items: stretch;

        .n-button {
          width: 100%;
        }
      }
    }
  }
}
</style>