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
                查看被举报{{ reportData.targetType === 1 ? '用户' : '职位' }}详情
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
          <div v-if="evidenceImages.length > 0" class="evidence-section">
            <h4>相关证据</h4>
            <NSpace>
              <div
                v-for="(image, index) in evidenceImages"
                :key="index"
                class="evidence-item"
              >
                <NImage
                  :src="image"
                  width="100"
                  height="100"
                  object-fit="cover"
                  preview
                />
                <div class="image-caption">
                  <span>证据图片 {{ index + 1 }}</span>
                </div>
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
        :rules="dynamicRules"
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

        <!-- 封禁天数选择（当选择封禁用户时显示） -->
        <NFormItem
          v-if="reportData.targetType === 1 && handleForm.result === 2"
          label="封禁设置"
          path="banInfo.banType"
        >
          <NSpace vertical>
            <NRadioGroup v-model:value="handleForm.banInfo.banType">
              <NSpace>
                <NRadio value="permanent">永久封禁</NRadio>
                <NRadio value="temporary">临时封禁</NRadio>
              </NSpace>
            </NRadioGroup>
          </NSpace>
        </NFormItem>

        <NFormItem
          v-if="reportData.targetType === 1 && handleForm.result === 2 && handleForm.banInfo.banType === 'temporary'"
          label="封禁天数"
          path="banInfo.banDays"
        >
          <NInputNumber
            v-model:value="handleForm.banInfo.banDays"
            :min="1"
            :max="365"
            placeholder="封禁天数"
            style="width: 200px"
          >
            <template #suffix>天</template>
          </NInputNumber>
        </NFormItem>

        <NFormItem label="处理原因" path="handleReason">
          <NInput
            v-model:value="handleForm.handleReason"
            type="textarea"
            :rows="4"
            placeholder="请输入处理原因（内部记录）"
          />
        </NFormItem>

        <NFormItem label="举报方通知内容" path="reporterNotificationContent">
          <NInput
            v-model:value="handleForm.reporterNotificationContent"
            type="textarea"
            :rows="3"
            placeholder="发送给举报方的通知内容"
          />
        </NFormItem>

        <NFormItem label="被举报方通知内容" path="targetNotificationContent">
          <NInput
            v-model:value="handleForm.targetNotificationContent"
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
  NForm, NFormItem, NRadioGroup, NRadio, NInput, NInputNumber,
  NDivider, useMessage
} from 'naive-ui'
import type { FormRules } from 'naive-ui'
import dayjs from 'dayjs'
import { reportsApi, type ReportDetail, type ReportHandle } from '@/api/reports'
import { useUserStore } from '@/store/user'

const router = useRouter()
const route = useRoute()
const message = useMessage()
const userStore = useUserStore()

// 获取举报ID
const reportId = computed(() => route.params.id as string)

// 计算属性 - 解析证据图片
const evidenceImages = computed(() => {
  if (!reportData.value.evidenceImage) {
    return []
  }

  // 直接返回URL数组，支持单个或多个URL
  return [reportData.value.evidenceImage]
})

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
  banInfo: {
    banType: 'temporary',
    banDays: 7
  },
  reporterNotificationContent: '',
  targetNotificationContent: ''
})


// 计算属性：动态验证规则
const dynamicRules = computed(() => {
  // 直接定义所有基础验证规则
  const rules: FormRules = {
    result: [
      {
        validator: (rule: any, value: any) => {
          if (value === null || value === undefined || value === '') {
            return new Error('请选择处理结果')
          }
          // 允许的值：0-无需处理, 1-警告, 2-封禁, 3-下线
          if (![0, 1, 2, 3].includes(Number(value))) {
            return new Error('请选择有效的处理结果')
          }
          return true
        },
        trigger: 'change'
      }
    ],
    handleReason: [
      { required: true, message: '请输入处理原因', trigger: 'blur' }
    ],
    reporterNotificationContent: [
      { required: true, message: '请输入举报方通知内容', trigger: 'blur' }
    ],
    targetNotificationContent: [
      { required: true, message: '请输入被举报方通知内容', trigger: 'blur' }
    ]
  }

  // 如果选择封禁，添加封禁信息验证
  if (handleForm.result === 2) {
    rules['banInfo.banType'] = [
      {
        validator: (rule: any, value: any) => {
          if (!value || (value !== 'permanent' && value !== 'temporary')) {
            return new Error('请选择封禁类型')
          }
          return true
        },
        trigger: 'change'
      }
    ]
    rules['banInfo.banDays'] = [
      {
        validator: (rule: any, value: any) => {
          // 只有临时封禁才需要验证天数
          if (handleForm.banInfo.banType === 'temporary') {
            if (value === null || value === undefined || value === '') {
              return new Error('请设置封禁天数')
            }
            if (Number(value) < 1 || Number(value) > 365) {
              return new Error('封禁天数必须在1-365天之间')
            }
          }
          return true
        },
        trigger: 'blur'
      }
    ]
  }

  return rules
})


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
  handleForm.banInfo.banType = 'temporary'
  handleForm.banInfo.banDays = 7
  handleForm.reporterNotificationContent = ''
  handleForm.targetNotificationContent = ''

  handleModalVisible.value = true
}

const handleSubmit = async () => {
  let handleData: ReportHandle

  try {
    // 先检查表单验证
    console.log('开始表单验证...')
    console.log('表单数据:', handleForm)
    await handleFormRef.value?.validate()
    console.log('表单验证通过')

    handling.value = true

    // 准备处理数据
    handleData = {
      handleResult: handleForm.result,
      handleReason: handleForm.handleReason,
      reporterNotificationContent: handleForm.reporterNotificationContent,
      targetNotificationContent: handleForm.targetNotificationContent
    }

    // 如果选择封禁，添加封禁信息
    if (handleForm.result === 2) {
      // 验证封禁信息
      if (!handleForm.banInfo.banType) {
        throw new Error('请选择封禁类型')
      }

      if (handleForm.banInfo.banType === 'temporary' && (!handleForm.banInfo.banDays || handleForm.banInfo.banDays < 1)) {
        throw new Error('临时封禁必须设置封禁天数')
      }

      handleData.banInfo = {
        banType: handleForm.banInfo.banType,
        banDays: handleForm.banInfo.banType === 'permanent' ? undefined : handleForm.banInfo.banDays
      }
    }

    // 调用后端API处理举报
    console.log('发送到后端的数据:', handleData)
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
    console.error('错误类型:', typeof error)
    console.error('错误构造函数:', error?.constructor?.name)
    console.error('错误响应:', error.response)
    console.error('错误状态:', error.response?.status)
    console.error('错误数据:', error.response?.data)

    // 处理Naive UI表单验证错误 - 最常见的情况
    if (Array.isArray(error)) {
      console.log('检测到数组类型的错误，详细内容:', error)

      // 处理可能的嵌套数组结构
      let flatErrors: any[] = []
      error.forEach((item: any) => {
        if (Array.isArray(item)) {
          // 如果是嵌套数组，展开它
          flatErrors.push(...item)
        } else {
          flatErrors.push(item)
        }
      })

      console.log('展开后的错误数组:', flatErrors)

      const errorMessages = flatErrors.map((err: any, index: number) => {
        console.log(`错误 ${index}:`, err)
        console.log(`  - 类型:`, typeof err)
        console.log(`  - 构造函数:`, err?.constructor?.name)
        console.log(`  - 内容:`, JSON.stringify(err, null, 2))

        if (err && typeof err === 'object') {
          if (err.field && err.message) {
            return `${err.field}: ${err.message}`
          }
          if (err.message) {
            return err.message
          }
          return `字段: ${err.field || '未知'}, 值: ${err.fieldValue}, 消息: ${JSON.stringify(err)}`
        }
        return String(err)
      }).join('; ')
      message.error(`表单验证失败: ${errorMessages}`)
    }
    // 处理前端验证错误
    else if (error.errors && Array.isArray(error.errors)) {
      const errorMessages = error.errors.map((err: any) => {
        if (err.field) {
          return `${err.field}: ${err.message}`
        }
        return err.message
      }).join('; ')
      message.error(`表单验证失败: ${errorMessages}`)
    }
    // 处理后端错误
    else if (error.response?.data) {
      const responseData = error.response.data

      // 如果有具体的验证错误
      if (responseData.data && Array.isArray(responseData.data)) {
        const errors = responseData.data
        const errorMessages = errors.map((err: any) => {
          if (err.field) {
            return `${err.field}: ${err.message || err.defaultMessage}`
          }
          return err.message || err.defaultMessage || JSON.stringify(err)
        }).join('; ')
        message.error(`处理失败: ${errorMessages}`)
      } else if (responseData.message) {
        message.error(`处理失败: ${responseData.message}`)
      } else {
        message.error(`处理失败: ${JSON.stringify(responseData)}`)
      }
    } else if (error.message) {
      message.error(`处理失败: ${error.message}`)
    } else {
      message.error('处理举报失败，请查看控制台了解详情')
    }
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