<template>
  <div class="users-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">用户管理</h1>
        <p class="page-description">管理平台所有用户信息，支持精细化用户操作</p>
      </div>
      <div class="header-actions">
        <NButton type="primary" @click="exportUsers">
          <template #icon>📤</template>
          导出用户
        </NButton>
      </div>
    </div>

    <!-- 筛选和搜索 -->
    <NCard :bordered="false" class="filter-card">
      <div class="filter-section">
        <div class="filter-row">
          <div class="filter-item">
            <label>用户类型</label>
            <NSelect
              v-model:value="filters.userType"
              :options="userTypeOptions"
              placeholder="全部类型"
              clearable
              style="width: 150px"
              @update:value="handleFilter"
            />
          </div>
          <div class="filter-item">
            <label>用户状态</label>
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
            <label>注册时间</label>
            <NDatePicker
              v-model:value="filters.registerTime"
              type="daterange"
              clearable
              style="width: 240px"
              @update:value="handleFilter"
            />
          </div>
        </div>
        <div class="search-row">
          <div class="search-input">
            <NInput
              v-model:value="searchKeyword"
              placeholder="搜索用户姓名、手机、邮箱、公司"
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

    <!-- 用户列表 -->
    <NCard :bordered="false" class="list-card">
      <div class="list-header">
        <span class="list-title">用户列表</span>
        <div class="list-actions">
          <span class="total-count">共 {{ filteredUsers.length }} 条记录</span>
        </div>
      </div>

      <div class="user-list">
        <div
          v-for="user in paginatedUsers"
          :key="user.id"
          class="user-item"
          @click="viewUserDetail(user)"
        >
          <div class="user-avatar">
            <div class="avatar-circle" :class="user.userType">
              {{ getUserIcon(user.userType) }}
            </div>
          </div>

          <div class="user-info">
            <div class="user-basic">
              <div class="user-name">
                <h3>{{ user.name }}</h3>
                <NTag :type="getStatusType(user.status)" size="small">
                  {{ getStatusText(user.status) }}
                </NTag>
                <NTag :type="getUserTypeType(user.userType)" size="small">
                  {{ getUserTypeText(user.userType) }}
                </NTag>
              </div>
              <div class="user-contact">
                <span class="contact-item">📱 {{ user.phone }}</span>
                <span class="contact-item">📧 {{ user.email }}</span>
              </div>
            </div>

            <div class="user-company" v-if="user.company">
              <span class="company-name">🏢 {{ user.company }}</span>
              <span class="position" v-if="user.position">{{ user.position }}</span>
            </div>

            <div class="user-stats">
              <div class="stat-item">
                <span class="stat-label">注册时间</span>
                <span class="stat-value">{{ formatTime(user.registerTime) }}</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">最后登录</span>
                <span class="stat-value">{{ formatTime(user.lastLoginTime) }}</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">登录次数</span>
                <span class="stat-value">{{ user.loginCount }}次</span>
              </div>
            </div>

            <div class="user-activity" v-if="user.activityStats">
              <span class="activity-item">
                发布职位: {{ user.activityStats.jobsPosted }}
              </span>
              <span class="activity-item">
                投递简历: {{ user.activityStats.applicationsSent }}
              </span>
              <span class="activity-item" v-if="user.activityStats.viewsReceived">
                简历浏览: {{ user.activityStats.viewsReceived }}
              </span>
            </div>
          </div>

          <div class="user-actions">
            <NButton
              size="small"
              type="info"
              ghost
              @click.stop="viewUserDetail(user)"
            >
              查看详情
            </NButton>
            <NButton
              size="small"
              :type="user.status === 'active' ? 'warning' : 'success'"
              @click.stop="toggleUserStatus(user)"
            >
              {{ user.status === 'active' ? '禁用' : '启用' }}
            </NButton>
            <NDropdown
              :options="moreActions"
              @select="handleMoreAction($event, user)"
            >
              <NButton size="small" quaternary>
                更多
              </NButton>
            </NDropdown>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="filteredUsers.length === 0" class="empty-state">
        <div class="empty-icon">👥</div>
        <h3 class="empty-title">暂无用户数据</h3>
        <p class="empty-description">当前没有符合条件的用户</p>
      </div>

      <!-- 分页 -->
      <div v-if="filteredUsers.length > 0" class="pagination-wrapper">
        <NPagination
          v-model:page="currentPage"
          :page-size="pageSize"
          :item-count="filteredUsers.length"
          show-size-picker
          :page-sizes="[10, 20, 50, 100]"
          @update:page="handlePageChange"
          @update:page-size="handlePageSizeChange"
        />
      </div>
    </NCard>

    <!-- 用户详情弹窗 -->
    <NModal v-model:show="showDetailModal" :mask-closable="false">
      <NCard
        style="max-width: 800px"
        title="用户详情"
        :bordered="false"
        size="huge"
        role="dialog"
        aria-modal
      >
        <template #header-extra>
          <NButton
            quaternary
            circle
            @click="showDetailModal = false"
          >
            <template #icon>
              <span class="close-icon">×</span>
            </template>
          </NButton>
        </template>

        <div v-if="selectedUser" class="user-detail">
          <!-- 基本信息 -->
          <div class="detail-section">
            <h4 class="section-title">基本信息</h4>
            <div class="detail-grid">
              <div class="detail-item">
                <label>用户姓名</label>
                <span>{{ selectedUser.name }}</span>
              </div>
              <div class="detail-item">
                <label>用户类型</label>
                <NTag :type="getUserTypeType(selectedUser.userType)" size="small">
                  {{ getUserTypeText(selectedUser.userType) }}
                </NTag>
              </div>
              <div class="detail-item">
                <label>用户状态</label>
                <NTag :type="getStatusType(selectedUser.status)" size="small">
                  {{ getStatusText(selectedUser.status) }}
                </NTag>
              </div>
              <div class="detail-item">
                <label>手机号码</label>
                <span>{{ selectedUser.phone }}</span>
              </div>
              <div class="detail-item">
                <label>邮箱地址</label>
                <span>{{ selectedUser.email }}</span>
              </div>
              <div class="detail-item">
                <label>注册时间</label>
                <span>{{ formatTime(selectedUser.registerTime) }}</span>
              </div>
            </div>
          </div>

          <!-- 公司信息 -->
          <div class="detail-section" v-if="selectedUser.company">
            <h4 class="section-title">公司信息</h4>
            <div class="detail-grid">
              <div class="detail-item">
                <label>公司名称</label>
                <span>{{ selectedUser.company }}</span>
              </div>
              <div class="detail-item" v-if="selectedUser.position">
                <label>职位</label>
                <span>{{ selectedUser.position }}</span>
              </div>
            </div>
          </div>

          <!-- 账户统计 -->
          <div class="detail-section">
            <h4 class="section-title">账户统计</h4>
            <div class="stats-grid">
              <div class="stat-card">
                <div class="stat-icon">🔐</div>
                <div class="stat-content">
                  <div class="stat-value">{{ selectedUser.loginCount }}</div>
                  <div class="stat-label">登录次数</div>
                </div>
              </div>
              <div class="stat-card">
                <div class="stat-icon">⏰</div>
                <div class="stat-content">
                  <div class="stat-value">{{ formatTime(selectedUser.lastLoginTime) }}</div>
                  <div class="stat-label">最后登录</div>
                </div>
              </div>
              <div class="stat-card" v-if="selectedUser.activityStats">
                <div class="stat-icon">💼</div>
                <div class="stat-content">
                  <div class="stat-value">{{ selectedUser.activityStats.jobsPosted || 0 }}</div>
                  <div class="stat-label">发布职位</div>
                </div>
              </div>
              <div class="stat-card" v-if="selectedUser.activityStats">
                <div class="stat-icon">📄</div>
                <div class="stat-content">
                  <div class="stat-value">{{ selectedUser.activityStats.applicationsSent || 0 }}</div>
                  <div class="stat-label">投递简历</div>
                </div>
              </div>
            </div>
          </div>

          <!-- 操作记录 -->
          <div class="detail-section">
            <h4 class="section-title">最近操作</h4>
            <div class="activity-list">
              <div
                v-for="activity in selectedUser.recentActivities"
                :key="activity.id"
                class="activity-item"
              >
                <div class="activity-type" :class="activity.type">
                  {{ getActivityIcon(activity.type) }}
                </div>
                <div class="activity-content">
                  <div class="activity-title">{{ activity.title }}</div>
                  <div class="activity-time">{{ formatTime(activity.time) }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <template #footer>
          <div class="modal-actions">
            <NButton @click="showDetailModal = false">关闭</NButton>
            <NButton
              :type="selectedUser?.status === 'active' ? 'warning' : 'success'"
              @click="toggleUserStatus(selectedUser!)"
            >
              {{ selectedUser?.status === 'active' ? '禁用用户' : '启用用户' }}
            </NButton>
            <NButton type="primary" @click="sendNotification(selectedUser!)">
              发送通知
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
  NDatePicker,
  NInput,
  NButton,
  NTag,
  NPagination,
  NModal,
  NDropdown,
  useMessage,
  useDialog
} from 'naive-ui'
import dayjs from 'dayjs'

interface User {
  id: string
  name: string
  phone: string
  email: string
  userType: 'jobseeker' | 'hr' | 'admin'
  status: 'active' | 'inactive' | 'banned'
  company?: string
  position?: string
  registerTime: string
  lastLoginTime: string
  loginCount: number
  activityStats?: {
    jobsPosted?: number
    applicationsSent?: number
    viewsReceived?: number
  }
  recentActivities?: Array<{
    id: string
    type: 'login' | 'job_post' | 'application' | 'profile_update'
    title: string
    time: string
  }>
}

interface Filters {
  userType: string | null
  status: string | null
  registerTime: [number, number] | null
}

const router = useRouter()
const message = useMessage()
const dialog = useDialog()

// 筛选选项
const userTypeOptions = [
  { label: '求职者', value: 'jobseeker' },
  { label: 'HR', value: 'hr' },
  { label: '管理员', value: 'admin' }
]

const statusOptions = [
  { label: '正常', value: 'active' },
  { label: '禁用', value: 'inactive' },
  { label: '封禁', value: 'banned' }
]

const moreActions = [
  { label: '发送通知', key: 'notify' },
  { label: '重置密码', key: 'reset-password' },
  { label: '查看记录', key: 'view-logs' },
  { label: '导出数据', key: 'export' }
]

// 状态管理
const searchKeyword = ref('')
const filters = ref<Filters>({
  userType: null,
  status: null,
  registerTime: null
})
const currentPage = ref(1)
const pageSize = ref(20)

// 弹窗状态
const showDetailModal = ref(false)
const selectedUser = ref<User | null>(null)

// 模拟用户数据
const usersData = ref<User[]>([
  {
    id: '1',
    name: '张三',
    phone: '13812345678',
    email: 'zhangsan@example.com',
    userType: 'jobseeker',
    status: 'active',
    registerTime: '2024-01-10 09:30:00',
    lastLoginTime: '2024-01-15 14:20:00',
    loginCount: 15,
    activityStats: {
      jobsPosted: 0,
      applicationsSent: 8,
      viewsReceived: 23
    },
    recentActivities: [
      {
        id: '1',
        type: 'application',
        title: '投递了"高级前端开发工程师"职位',
        time: '2024-01-15 14:20:00'
      },
      {
        id: '2',
        type: 'profile_update',
        title: '更新了个人简历',
        time: '2024-01-14 10:15:00'
      }
    ]
  },
  {
    id: '2',
    name: '李四',
    phone: '13987654321',
    email: 'lisi@company.com',
    userType: 'hr',
    status: 'active',
    company: '北京字节跳动科技有限公司',
    position: 'HR经理',
    registerTime: '2024-01-08 11:20:00',
    lastLoginTime: '2024-01-15 16:45:00',
    loginCount: 32,
    activityStats: {
      jobsPosted: 5,
      applicationsSent: 0,
      viewsReceived: 0
    },
    recentActivities: [
      {
        id: '3',
        type: 'job_post',
        title: '发布了"高级前端开发工程师"职位',
        time: '2024-01-15 16:45:00'
      },
      {
        id: '4',
        type: 'login',
        title: '登录系统',
        time: '2024-01-15 09:00:00'
      }
    ]
  },
  {
    id: '3',
    name: '王五',
    phone: '13666666666',
    email: 'wangwu@example.com',
    userType: 'jobseeker',
    status: 'inactive',
    registerTime: '2024-01-05 15:30:00',
    lastLoginTime: '2024-01-12 10:20:00',
    loginCount: 8,
    activityStats: {
      jobsPosted: 0,
      applicationsSent: 3,
      viewsReceived: 5
    },
    recentActivities: [
      {
        id: '5',
        type: 'application',
        title: '投递了"产品经理"职位',
        time: '2024-01-12 10:20:00'
      }
    ]
  },
  {
    id: '4',
    name: '赵六',
    phone: '13888888888',
    email: 'zhaoliu@tech.com',
    userType: 'hr',
    status: 'active',
    company: '阿里巴巴集团控股有限公司',
    position: '技术招聘官',
    registerTime: '2024-01-03 14:10:00',
    lastLoginTime: '2024-01-15 13:30:00',
    loginCount: 28,
    activityStats: {
      jobsPosted: 8,
      applicationsSent: 0,
      viewsReceived: 0
    }
  },
  {
    id: '5',
    name: 'admin',
    phone: '13999999999',
    email: 'admin@smarthire.com',
    userType: 'admin',
    status: 'active',
    registerTime: '2024-01-01 00:00:00',
    lastLoginTime: '2024-01-15 18:00:00',
    loginCount: 156
  }
])

// 计算属性
const filteredUsers = computed(() => {
  let filtered = usersData.value

  // 用户类型筛选
  if (filters.value.userType) {
    filtered = filtered.filter(user => user.userType === filters.value.userType)
  }

  // 状态筛选
  if (filters.value.status) {
    filtered = filtered.filter(user => user.status === filters.value.status)
  }

  // 注册时间筛选
  if (filters.value.registerTime) {
    const [start, end] = filters.value.registerTime
    filtered = filtered.filter(user => {
      const registerTime = dayjs(user.registerTime).valueOf()
      return registerTime >= start && registerTime <= end
    })
  }

  // 关键词搜索
  if (searchKeyword.value.trim()) {
    const keyword = searchKeyword.value.toLowerCase()
    filtered = filtered.filter(user =>
      user.name.toLowerCase().includes(keyword) ||
      user.phone.includes(keyword) ||
      user.email.toLowerCase().includes(keyword) ||
      (user.company && user.company.toLowerCase().includes(keyword))
    )
  }

  return filtered
})

const paginatedUsers = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredUsers.value.slice(start, end)
})

// 辅助方法
const getUserIcon = (userType: string) => {
  const iconMap: Record<string, string> = {
    jobseeker: '👤',
    hr: '💼',
    admin: '👑'
  }
  return iconMap[userType] || '👤'
}

const getUserTypeType = (userType: string) => {
  const typeMap: Record<string, string> = {
    jobseeker: 'info',
    hr: 'warning',
    admin: 'error'
  }
  return typeMap[userType] || 'default'
}

const getUserTypeText = (userType: string) => {
  const textMap: Record<string, string> = {
    jobseeker: '求职者',
    hr: 'HR',
    admin: '管理员'
  }
  return textMap[userType] || userType
}

const getStatusType = (status: string) => {
  const typeMap: Record<string, string> = {
    active: 'success',
    inactive: 'warning',
    banned: 'error'
  }
  return typeMap[status] || 'default'
}

const getStatusText = (status: string) => {
  const textMap: Record<string, string> = {
    active: '正常',
    inactive: '禁用',
    banned: '封禁'
  }
  return textMap[status] || status
}

const getActivityIcon = (type: string) => {
  const iconMap: Record<string, string> = {
    login: '🔐',
    job_post: '💼',
    application: '📄',
    profile_update: '✏️'
  }
  return iconMap[type] || '📝'
}

// 格式化时间
const formatTime = (time: string) => {
  return dayjs(time).format('YYYY-MM-DD HH:mm')
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
}

const resetFilters = () => {
  filters.value = {
    userType: null,
    status: null,
    registerTime: null
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

// 查看用户详情
const viewUserDetail = (user: User) => {
  selectedUser.value = user
  showDetailModal.value = true
}

// 切换用户状态
const toggleUserStatus = (user: User) => {
  const action = user.status === 'active' ? '禁用' : '启用'
  const targetType = user.status === 'active' ? 'inactive' : 'active'

  dialog.warning({
    title: `确认${action}`,
    content: `确定要${action}用户"${user.name}"吗？`,
    positiveText: `确定${action}`,
    negativeText: '取消',
    onPositiveClick: () => {
      // 更新用户状态
      const userIndex = usersData.value.findIndex(u => u.id === user.id)
      if (userIndex !== -1) {
        usersData.value[userIndex].status = targetType as any
        message.success(`用户已${action}`)
      }
    }
  })
}

// 更多操作
const handleMoreAction = (key: string, user: User) => {
  switch (key) {
    case 'notify':
      sendNotification(user)
      break
    case 'reset-password':
      resetPassword(user)
      break
    case 'view-logs':
      viewUserLogs(user)
      break
    case 'export':
      exportUserData(user)
      break
  }
}

// 发送通知
const sendNotification = (user: User) => {
  message.info(`发送通知功能开发中 - 用户：${user.name}`)
}

// 重置密码
const resetPassword = (user: User) => {
  dialog.warning({
    title: '确认重置密码',
    content: `确定要重置用户"${user.name}"的密码吗？`,
    positiveText: '确定重置',
    negativeText: '取消',
    onPositiveClick: () => {
      message.success(`已重置用户"${user.name}"的密码`)
    }
  })
}

// 查看操作记录
const viewUserLogs = (user: User) => {
  message.info(`查看用户记录功能开发中 - 用户：${user.name}`)
}

// 导出用户数据
const exportUserData = (user: User) => {
  message.info(`导出用户数据功能开发中 - 用户：${user.name}`)
}

// 批量导出用户
const exportUsers = () => {
  message.info('批量导出功能开发中')
}

// 页面初始化
onMounted(() => {
  // 这里可以调用API获取真实数据
})
</script>

<style scoped lang="scss">
.users-page {
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

    .header-actions {
      .n-button {
        min-width: 120px;
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

  // 用户列表卡片
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

    .user-list {
      .user-item {
        display: flex;
        gap: 20px;
        padding: 24px;
        background: var(--bg-primary);
        border: 1px solid var(--border-color);
        border-radius: 12px;
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

        .user-avatar {
          flex-shrink: 0;

          .avatar-circle {
            width: 64px;
            height: 64px;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 24px;
            font-weight: 600;

            &.jobseeker {
              background: linear-gradient(135deg, #2f7cff, #1e5fcc);
              color: white;
            }

            &.hr {
              background: linear-gradient(135deg, #faad14, #d48806);
              color: white;
            }

            &.admin {
              background: linear-gradient(135deg, #f5222d, #cf1322);
              color: white;
            }
          }
        }

        .user-info {
          flex: 1;
          display: flex;
          flex-direction: column;
          gap: 12px;

          .user-basic {
            .user-name {
              display: flex;
              align-items: center;
              gap: 12px;
              margin-bottom: 8px;

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

            .user-contact {
              display: flex;
              gap: 16px;

              .contact-item {
                font-size: 14px;
                color: var(--text-secondary);
              }
            }
          }

          .user-company {
            display: flex;
            gap: 12px;
            align-items: center;

            .company-name {
              font-size: 14px;
              font-weight: 500;
              color: var(--text-primary);
            }

            .position {
              font-size: 13px;
              color: var(--text-secondary);
            }
          }

          .user-stats {
            display: flex;
            gap: 24px;

            .stat-item {
              display: flex;
              flex-direction: column;
              gap: 4px;

              .stat-label {
                font-size: 12px;
                color: var(--text-disabled);
              }

              .stat-value {
                font-size: 14px;
                color: var(--text-secondary);
                font-weight: 500;
              }
            }
          }

          .user-activity {
            display: flex;
            gap: 16px;
            flex-wrap: wrap;

            .activity-item {
              font-size: 13px;
              color: var(--text-secondary);
              background: var(--bg-secondary);
              padding: 4px 8px;
              border-radius: 4px;
            }
          }
        }

        .user-actions {
          flex-shrink: 0;
          display: flex;
          flex-direction: column;
          gap: 8px;
          align-items: flex-end;
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

  // 用户详情弹窗
  .user-detail {
    .detail-section {
      margin-bottom: 32px;

      &:last-child {
        margin-bottom: 0;
      }

      .section-title {
        font-size: 16px;
        font-weight: 600;
        color: var(--text-primary);
        margin: 0 0 16px 0;
        padding-bottom: 8px;
        border-bottom: 2px solid var(--primary-color);
      }

      .detail-grid {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
        gap: 16px;

        .detail-item {
          display: flex;
          flex-direction: column;
          gap: 8px;

          label {
            font-size: 14px;
            color: var(--text-secondary);
            font-weight: 500;
          }

          span {
            font-size: 14px;
            color: var(--text-primary);
          }
        }
      }

      .stats-grid {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
        gap: 16px;

        .stat-card {
          display: flex;
          align-items: center;
          gap: 12px;
          padding: 16px;
          background: var(--bg-secondary);
          border-radius: 8px;

          .stat-icon {
            font-size: 24px;
          }

          .stat-content {
            .stat-value {
              font-size: 18px;
              font-weight: 600;
              color: var(--text-primary);
              margin-bottom: 4px;
            }

            .stat-label {
              font-size: 12px;
              color: var(--text-secondary);
            }
          }
        }
      }

      .activity-list {
        .activity-item {
          display: flex;
          align-items: center;
          gap: 12px;
          padding: 12px;
          background: var(--bg-secondary);
          border-radius: 8px;
          margin-bottom: 8px;

          &:last-child {
            margin-bottom: 0;
          }

          .activity-type {
            font-size: 16px;
          }

          .activity-content {
            flex: 1;

            .activity-title {
              font-size: 14px;
              color: var(--text-primary);
              margin-bottom: 4px;
            }

            .activity-time {
              font-size: 12px;
              color: var(--text-disabled);
            }
          }
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
  .users-page {
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

      .header-actions {
        align-self: stretch;

        .n-button {
          width: 100%;
        }
      }
    }

    .filter-card {
      .filter-section {
        .filter-row {
          flex-direction: column;
          gap: 12px;

          .filter-item {
            width: 100%;

            .n-select, .n-date-picker {
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

    .user-list {
      .user-item {
        flex-direction: column;
        gap: 16px;
        padding: 16px;

        .user-avatar {
          align-self: center;

          .avatar-circle {
            width: 48px;
            height: 48px;
            font-size: 20px;
          }
        }

        .user-info {
          width: 100%;

          .user-basic {
            .user-name {
              flex-wrap: wrap;
              justify-content: center;
              text-align: center;
            }

            .user-contact {
              justify-content: center;
            }
          }

          .user-company {
            justify-content: center;
          }

          .user-stats {
            justify-content: center;
          }

          .user-activity {
            justify-content: center;
          }
        }

        .user-actions {
          flex-direction: row;
          justify-content: center;
          width: 100%;

          .n-button {
            flex: 1;
          }
        }
      }
    }
  }
}
</style>