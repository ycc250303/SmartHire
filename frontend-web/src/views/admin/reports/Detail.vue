<template>
  <div class="report-detail">
    <NCard :bordered="false">
      <template #header>
        <NSpace align="center" justify="space-between" style="width: 100%">
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
          <NButton
            v-if="reportData.status === 0"
            type="primary"
            @click="openHandleModal"
          >
            处理举报
          </NButton>
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
              <NTag :type="getReportTypeTagType(reportData.reportType)" size="small">
                {{ getReportTypeLabel(reportData.reportType) }}
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
                  fallback-src="/default-avatar.png"
                  round
                />
                <span>{{ reportData.reporterName }}</span>
                <NText depth="3">({{ reportData.reporterType === 1 ? '求职者' : 'HR' }})</NText>
              </NSpace>
            </NDescriptionsItem>
            <NDescriptionsItem label="举报时间">
              {{ formatTime(reportData.createdAt) }}
            </NDescriptionsItem>
            <NDescriptionsItem label="被举报对象">
              <NSpace align="center">
                <NAvatar
                  :size="24"
                  fallback-src="/default-avatar.png"
                  round
                />
                <span>{{ reportData.targetTitle }}</span>
                <NText depth="3">({{ reportData.targetType === 1 ? '用户' : '职位' }})</NText>
              </NSpace>
            </NDescriptionsItem>
            <NDescriptionsItem label="涉及内容">
              <NButton text type="primary" @click="viewTargetContent">
                查看{{ reportData.targetType === 2 ? '职位' : '用户' }}详情
              </NButton>
            </NDescriptionsItem>
          </NDescriptions>
        </NCard>

        <!-- 举报详情 -->
        <NCard title="举报描述" size="small" :bordered="false" class="info-card">
          <div class="report-description">
            <p>{{ reportData.reason }}</p>
          </div>

          <!-- 举报证据 -->
          <div v-if="reportData.evidenceImage" class="evidence-section">
            <h4>相关证据</h4>
            <NSpace>
              <div class="evidence-item">
                <NImage
                  :src="`data:image/jpeg;base64,${reportData.evidenceImage}`"
                  width="100"
                  height="100"
                  object-fit="cover"
                  preview
                />
              </div>
            </NSpace>
          </div>
        </NCard>
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
              <!-- 根据举报类型显示不同的处理选项 -->
              <template v-if="reportData.targetType === 1">
                <!-- 举报用户 -->
                <NRadio :value="0">无需处理</NRadio>
                <NRadio :value="1">警告用户</NRadio>
                <NRadio :value="2">封禁用户</NRadio>
              </template>
              <template v-else-if="reportData.targetType === 2">
                <!-- 举报职位 -->
                <NRadio :value="0">无需处理</NRadio>
                <NRadio :value="1">警告HR</NRadio>
                <NRadio :value="3">下线职位</NRadio>
              </template>
            </NSpace>
          </NRadioGroup>
        </NFormItem>

        <NFormItem label="处理原因" path="handleReason">
          <NInput
            v-model:value="handleForm.handleReason"
            type="textarea"
            :rows="4"
            placeholder="请输入处理原因（内部记录）"
          />
        </NFormItem>

        <NFormItem label="通知被举报方" path="notifyTarget">
          <NSwitch v-model:value="handleForm.notifyTarget">
            <template #checked>发送通知</template>
            <template #unchecked>不通知</template>
          </NSwitch>
        </NFormItem>

        <NFormItem
          v-if="handleForm.notifyTarget"
          label="通知内容"
          path="notificationContent"
        >
          <NInput
            v-model:value="handleForm.notificationContent"
            type="textarea"
            :rows="3"
            placeholder="发送给被举报方的通知内容"
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

    <!-- 职位详情弹窗 -->
    <NModal
      v-model:show="showJobDetailModal"
      :mask-closable="false"
      preset="card"
      title="职位详情"
      style="width: 800px;"
    >
      <div v-if="reportData.targetJob" class="job-detail">
        <NDescriptions :column="2">
          <NDescriptionsItem label="职位名称">
            {{ reportData.targetJob.jobTitle }}
          </NDescriptionsItem>
          <NDescriptionsItem label="职位类别">
            {{ reportData.targetJob.jobCategory }}
          </NDescriptionsItem>
          <NDescriptionsItem label="工作城市">
            {{ reportData.targetJob.city }}
          </NDescriptionsItem>
          <NDescriptionsItem label="薪资范围">
            {{ reportData.targetJob.salaryMin }}-{{ reportData.targetJob.salaryMax }}
          </NDescriptionsItem>
          <NDescriptionsItem label="职位状态">
            <NTag :type="reportData.targetJob.status === 1 ? 'success' : 'warning'">
              {{ reportData.targetJob.status === 1 ? '已发布' : '未发布' }}
            </NTag>
          </NDescriptionsItem>
          <NDescriptionsItem label="HR ID">
            {{ reportData.targetJob.hrId }}
          </NDescriptionsItem>
          <NDescriptionsItem label="公司名称">
            {{ reportData.targetJob.companyName }}
          </NDescriptionsItem>
          <NDescriptionsItem label="发布时间">
            {{ formatTime(reportData.targetJob.createdAt) }}
          </NDescriptionsItem>
        </NDescriptions>
      </div>
    </NModal>

    <!-- 用户详情弹窗 -->
    <NModal
      v-model:show="showUserDetailModal"
      :mask-closable="false"
      preset="card"
      title="用户详情"
      style="width: 800px;"
    >
      <div v-if="reportData.targetUser" class="user-detail">
        <NDescriptions :column="2">
          <NDescriptionsItem label="用户名">
            {{ reportData.targetUser.username }}
          </NDescriptionsItem>
          <NDescriptionsItem label="邮箱">
            {{ reportData.targetUser.email }}
          </NDescriptionsItem>
          <NDescriptionsItem label="手机号">
            {{ reportData.targetUser.phone }}
          </NDescriptionsItem>
          <NDescriptionsItem label="用户类型">
            <NTag :type="reportData.targetUser.userType === 2 ? 'info' : 'success'">
              {{ reportData.targetUser.userType === 2 ? 'HR' : '求职者' }}
            </NTag>
          </NDescriptionsItem>
          <NDescriptionsItem label="账户状态">
            <NTag :type="reportData.targetUser.status === 1 ? 'success' : 'error'">
              {{ reportData.targetUser.status === 1 ? '正常' : '禁用' }}
            </NTag>
          </NDescriptionsItem>
          <NDescriptionsItem label="注册时间">
            {{ formatTime(reportData.targetUser.createdAt) }}
          </NDescriptionsItem>
        </NDescriptions>
      </div>
    </NModal>

    </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  NCard, NSpace, NButton, NDescriptions, NDescriptionsItem, NTag,
  NAvatar, NText, NSpin, NImage, NModal,
  NForm, NFormItem, NRadioGroup, NRadio, NInput, NSwitch,
  NDivider, useMessage
} from 'naive-ui'
import type { FormRules } from 'naive-ui'
import dayjs from 'dayjs'
import { reportsApi, type ReportDetail, type ReportHandle, HANDLE_RESULT_LABEL_MAP, HANDLE_RESULT_MAP } from '@/api/reports'
import { useUserStore } from '@/store/user'

const router = useRouter()
const route = useRoute()
const message = useMessage()
const userStore = useUserStore()

// 获取举报ID
const reportId = computed(() => route.params.id as string)

// 状态
const loading = ref(true)
const handleModalVisible = ref(false)
const handling = ref(false)
const showJobDetailModal = ref(false)
const showUserDetailModal = ref(false)

// 表单引用
const handleFormRef = ref()

// 举报数据
const reportData = ref<ReportDetail>({} as ReportDetail)

// 处理表单
const handleForm = reactive({
  result: 0,
  handleReason: '',
  notifyTarget: false,
  notificationContent: ''
})

// 表单验证规则
const handleRules: FormRules = {
  result: [
    { required: true, message: '请选择处理结果', trigger: 'change' }
  ],
  handleReason: [
    { required: true, message: '请输入处理原因', trigger: 'blur' }
  ]
}


// 工具方法
const formatTime = (time: string | Date) => {
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss')
}

const getReportTypeLabel = (type: number) => {
  const typeMap: Record<number, string> = {
    1: '垃圾信息',
    2: '不当内容',
    3: '虚假职位',
    4: '欺诈行为',
    5: '骚扰行为',
    6: '其他'
  }
  return typeMap[type] || '其他'
}

const getReportTypeTagType = (type: number) => {
  const typeMap: Record<number, any> = {
    1: 'default',
    2: 'warning',
    3: 'error',
    4: 'error',
    5: 'error',
    6: 'default'
  }
  return typeMap[type] || 'default'
}


const getStatusLabel = (status: number) => {
  const statusMap: Record<number, string> = {
    0: '处理中',
    1: '已处理'
  }
  return statusMap[status] || '未知状态'
}

const getStatusTagType = (status: number) => {
  const statusMap: Record<number, any> = {
    0: 'info',
    1: 'success'
  }
  return statusMap[status] || 'default'
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
  if (reportData.value.targetType === 2) {
    showJobDetailModal.value = true
  } else if (reportData.value.targetType === 1) {
    showUserDetailModal.value = true
  }
}

const openHandleModal = () => {
  // 重置表单
  handleForm.result = 0
  handleForm.handleReason = ''
  handleForm.notifyTarget = false
  handleForm.notificationContent = ''

  handleModalVisible.value = true
}

const handleSubmit = async () => {
  try {
    await handleFormRef.value?.validate()

    handling.value = true

    // 准备处理数据
    const handleData: ReportHandle = {
      handleResult: handleForm.result,
      handleReason: handleForm.handleReason,
      notifyTarget: handleForm.notifyTarget,
      notificationContent: handleForm.notificationContent
    }

    // 调用后端API处理举报
    await reportsApi.handleReport(
      reportId.value,
      handleData,
      userStore.auth.user?.id || 0
    )

    // 重新加载举报详情
    await loadReportDetail()

    message.success('举报处理成功')
    handleModalVisible.value = false
  } catch (error: any) {
    console.error('处理举报失败:', error)
    message.error(error.message || '处理举报失败')
  } finally {
    handling.value = false
  }
}

// 加载举报详情
const loadReportDetail = async () => {
  try {
    loading.value = true

    // 调用后端API获取举报详情
    const response = await reportsApi.getReportDetail(reportId.value)
    reportData.value = response.data || response

  } catch (error: any) {
    console.error('加载举报详情失败:', error)
    message.error(error.message || '加载举报详情失败')
  } finally {
    loading.value = false
  }
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
  }
}

// 详情弹窗样式
.job-detail,
.user-detail {
  .job-description,
  .user-stats {
    margin-top: 24px;
    padding-top: 16px;
    border-top: 1px solid var(--border-color);

    h4 {
      margin-bottom: 12px;
      font-size: 16px;
      font-weight: 600;
      color: var(--text-primary);
    }

    p {
      line-height: 1.6;
      color: var(--text-secondary);
    }
  }
}
</style>