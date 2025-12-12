<template>
  <div class="dashboard-home">
    <!-- 欢迎区域 -->
    <div class="welcome-section">
      <div class="welcome-content">
        <div class="welcome-text">
          <h1 class="welcome-greeting">{{ greeting }}</h1>
          <p class="welcome-subtitle">{{ welcomeMessage }}</p>
        </div>
        <div class="welcome-info">
          <div class="system-status">
            <span class="status-dot online"></span>
            <span>系统运行正常</span>
          </div>
          <div class="runtime">
            <span>运行时间: {{ formatRuntime(systemRuntime) }}</span>
          </div>
        </div>
      </div>
      <div class="admin-avatar">
        <NAvatar :size="64" round />
        <div class="admin-info">
          <div class="admin-name">{{ userStore.displayName() }}</div>
          <div class="admin-role">系统管理员</div>
        </div>
      </div>
    </div>

    <!-- 待办事项 -->
    <NCard class="content-card" title="待办事项" :bordered="false">
      <div class="todos-grid">
        <div
          v-for="item in todoItems"
          :key="item.key"
          class="todo-item"
          :class="{ 'has-count': item.count > 0 }"
          @click="handleTodoClick(item)"
        >
          <div class="todo-icon">
            <span :style="{ color: item.color }">{{ item.icon }}</span>
          </div>
          <div class="todo-content">
            <div class="todo-title">{{ item.title }}</div>
            <div class="todo-desc">{{ item.description }}</div>
          </div>
          <div v-if="item.count > 0" class="todo-count">
            <NBadge :value="item.count" :max="99" />
          </div>
        </div>
      </div>
    </NCard>

    <!-- 今日数据统计 -->
    <NCard title="今日数据" :bordered="false" class="stats-card">
      <div class="stats-grid">
        <div class="stat-group">
          <h4 class="group-title">用户数据</h4>
          <div class="stat-items">
            <div class="stat-item" v-for="stat in userStats" :key="stat.key">
              <div class="stat-icon" :style="{ color: stat.color }">{{ stat.icon }}</div>
              <div class="stat-info">
                <div class="stat-value">{{ formatNumber(stat.value) }}</div>
                <div class="stat-label">{{ stat.label }}</div>
                <div class="stat-growth" :class="stat.trend">
                  <span>{{ stat.trend === 'up' ? '↑' : stat.trend === 'down' ? '↓' : '→' }}</span>
                  <span>{{ stat.changeText }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="stat-group">
          <h4 class="group-title">招聘数据</h4>
          <div class="stat-items">
            <div class="stat-item" v-for="stat in jobStats" :key="stat.key">
              <div class="stat-icon" :style="{ color: stat.color }">{{ stat.icon }}</div>
              <div class="stat-info">
                <div class="stat-value">{{ formatNumber(stat.value) }}</div>
                <div class="stat-label">{{ stat.label }}</div>
                <div class="stat-growth" :class="stat.trend">
                  <span>{{ stat.trend === 'up' ? '↑' : stat.trend === 'down' ? '↓' : '→' }}</span>
                  <span>{{ stat.changeText }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="stat-group">
          <h4 class="group-title">平台活跃</h4>
          <div class="stat-items">
            <div class="stat-item" v-for="stat in activityStats" :key="stat.key">
              <div class="stat-icon" :style="{ color: stat.color }">{{ stat.icon }}</div>
              <div class="stat-info">
                <div class="stat-value">{{ formatNumber(stat.value) }}</div>
                <div class="stat-label">{{ stat.label }}</div>
                <div class="stat-growth" :class="stat.trend">
                  <span>{{ stat.trend === 'up' ? '↑' : stat.trend === 'down' ? '↓' : '→' }}</span>
                  <span>{{ stat.changeText }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </NCard>

    <!-- 图表和快捷操作 -->
    <NGrid :x-gap="24" :y-gap="24" responsive="screen">
      <!-- 趋势图表 -->
      <NGi span="24 s:24 m:14 l:14">
        <NCard title="访问趋势" :bordered="false">
          <div class="chart-container">
            <div class="chart-placeholder">
              <span class="chart-icon">📈</span>
              <p>访问量趋势图表</p>
              <p class="chart-desc">显示最近7天的访问量变化趋势</p>
            </div>
          </div>
        </NCard>
      </NGi>

      <!-- 最新动态 -->
      <NGi span="24 s:24 m:10 l:10">
        <NCard title="最新动态" :bordered="false">
          <div class="activities-list">
            <div
              v-for="activity in activities"
              :key="activity.id"
              class="activity-item"
            >
              <div class="activity-avatar">
                <NAvatar
                  :size="32"
                  round
                />
              </div>
              <div class="activity-content">
                <div class="activity-header">
                  <span class="activity-type" :class="activity.type">{{ getTypeLabel(activity.type) }}</span>
                  <span class="activity-time">{{ formatTime(activity.time) }}</span>
                </div>
                <div class="activity-text">
                  <span class="activity-user">{{ activity.user }}</span>
                  <span class="activity-action">{{ activity.action }}</span>
                </div>
                <div class="activity-detail" v-if="activity.detail">{{ activity.detail }}</div>
              </div>
            </div>
          </div>
          <div class="activities-more">
            <NButton text type="primary">查看更多</NButton>
          </div>
        </NCard>
      </NGi>
    </NGrid>

    <!-- 系统提醒 -->
    <NCard title="系统提醒" :bordered="false" class="alerts-card">
      <div class="alerts-list">
        <div
          v-for="alert in alerts"
          :key="alert.id"
          class="alert-item"
          :class="alert.type"
        >
          <div class="alert-icon">
            <span>{{ getAlertIcon(alert.type) }}</span>
          </div>
          <div class="alert-content">
            <div class="alert-title">{{ alert.title }}</div>
            <div class="alert-desc">{{ alert.description }}</div>
          </div>
          <div class="alert-time">{{ formatTime(alert.time) }}</div>
        </div>
      </div>
    </NCard>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { NCard, NGrid, NGi, NBadge, NButton, NAvatar, useMessage } from 'naive-ui'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import { useUserStore } from '@/store/user'

dayjs.extend(relativeTime)

const router = useRouter()
const message = useMessage()
const userStore = useUserStore()

// 系统运行时间（模拟）
const systemStartTime = new Date('2024-01-01T00:00:00')
const systemRuntime = ref(dayjs().diff(systemStartTime, 'second'))

// 动态问候语
const greeting = computed(() => {
  const hour = dayjs().hour()
  if (hour < 6) return '凌晨好'
  if (hour < 9) return '早上好'
  if (hour < 12) return '上午好'
  if (hour < 14) return '中午好'
  if (hour < 17) return '下午好'
  if (hour < 19) return '傍晚好'
  return '晚上好'
})

const welcomeMessage = computed(() => {
  return `${greeting.value}，${userStore.displayName()}！今天又是充满希望的一天！`
})

// 待办事项数据
const todoItems = ref([
  {
    key: 'pending-jobs',
    title: '待审核职位',
    description: '需要审核的招聘职位',
    icon: '📋',
    color: '#2f7cff',
    count: 12,
    path: '/dashboard/review'
  },
  {
    key: 'user-verify',
    title: '用户认证',
    description: '待认证的用户信息',
    icon: '👤',
    color: '#52c41a',
    count: 5,
    path: '/dashboard/users'
  },
  {
    key: 'system-maintain',
    title: '系统维护',
    description: '定期系统检查与维护',
    icon: '🔧',
    color: '#faad14',
    count: 0,
    path: '/dashboard/system'
  },
  {
    key: 'announcement',
    title: '公告管理',
    description: '发布和管理系统公告',
    icon: '📢',
    color: '#ff5f5f',
    count: 2,
    path: '/dashboard/system/announcement'
  }
])

// 用户数据统计
const userStats = ref([
  {
    key: 'total-users',
    label: '总用户数',
    value: 1024,
    icon: '👥',
    color: '#2f7cff',
    trend: 'up',
    changeText: '较昨日 +12.5%'
  },
  {
    key: 'jobseekers',
    label: '求职者',
    value: 856,
    icon: '🎯',
    color: '#52c41a',
    trend: 'up',
    changeText: '较昨日 +8.3%'
  },
  {
    key: 'hr-users',
    label: 'HR用户',
    value: 168,
    icon: '👔',
    color: '#faad14',
    trend: 'stable',
    changeText: '与昨日持平'
  }
])

// 招聘数据统计
const jobStats = ref([
  {
    key: 'active-jobs',
    label: '活跃职位',
    value: 568,
    icon: '💼',
    color: '#722ed1',
    trend: 'up',
    changeText: '较昨日 +5.2%'
  },
  {
    key: 'today-applications',
    label: '今日申请',
    value: 234,
    icon: '📝',
    color: '#13c2c2',
    trend: 'up',
    changeText: '较昨日 +15.7%'
  },
  {
    key: 'pending-jobs',
    label: '待审核',
    value: 12,
    icon: '⏳',
    color: '#fa8c16',
    trend: 'down',
    changeText: '较昨日 -3'
  }
])

// 平台活跃度统计
const activityStats = ref([
  {
    key: 'today-active',
    label: '今日活跃',
    value: 428,
    icon: '🔥',
    color: '#f5222d',
    trend: 'up',
    changeText: '较昨日 +8.9%'
  },
  {
    key: 'online-users',
    label: '在线用户',
    value: 89,
    icon: '🟢',
    color: '#52c41a',
    trend: 'stable',
    changeText: '实时在线'
  },
  {
    key: 'new-users',
    label: '今日新增',
    value: 45,
    icon: '✨',
    color: '#1890ff',
    trend: 'up',
    changeText: '较昨日 +7'
  }
])

// 最新动态
const activities = ref([
  {
    id: 1,
    user: '张三',
    action: '发布了新的前端开发职位',
    time: '2024-01-15T10:30:00',
    type: 'job',
    detail: '高级前端工程师 - 北京字节跳动科技有限公司'
  },
  {
    id: 2,
    user: '李四',
    action: '完成了用户认证审核',
    time: '2024-01-15T09:45:00',
    type: 'user',
    detail: 'HR用户认证通过 - 腾讯科技有限公司'
  },
  {
    id: 3,
    user: '王五',
    action: '更新了系统公告',
    time: '2024-01-15T08:20:00',
    type: 'system',
    detail: '发布了新功能上线通知'
  },
  {
    id: 4,
    user: '赵六',
    action: '拒绝了违规职位',
    time: '2024-01-15T07:30:00',
    type: 'review',
    detail: '原因：职位描述不明确'
  },
  {
    id: 5,
    user: '系统',
    action: '自动数据备份完成',
    time: '2024-01-15T02:00:00',
    type: 'system',
    detail: '备份数据大小：2.3GB'
  }
])

// 系统提醒
const alerts = ref([
  {
    id: 1,
    title: '系统维护通知',
    description: '计划于今晚 23:00-01:00 进行系统维护',
    type: 'warning',
    time: '2024-01-15T14:00:00'
  },
  {
    id: 2,
    title: '数据备份完成',
    description: '昨日数据备份已成功完成',
    type: 'success',
    time: '2024-01-15T02:00:00'
  },
  {
    id: 3,
    title: '安全提醒',
    description: '检测到异常登录尝试，请及时检查',
    type: 'error',
    time: '2024-01-14T22:30:00'
  }
])

// 格式化数字
const formatNumber = (num: number) => {
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + 'w'
  } else if (num >= 1000) {
    return (num / 1000).toFixed(1) + 'k'
  }
  return num.toString()
}

// 格式化运行时间
const formatRuntime = (seconds: number) => {
  const days = Math.floor(seconds / 86400)
  const hours = Math.floor((seconds % 86400) / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)

  if (days > 0) {
    return `${days}天${hours}小时`
  } else if (hours > 0) {
    return `${hours}小时${minutes}分钟`
  } else {
    return `${minutes}分钟`
  }
}

// 格式化时间
const formatTime = (time: string) => {
  return dayjs(time).fromNow()
}

// 获取提醒图标
const getAlertIcon = (type: string) => {
  const iconMap = {
    success: '✅',
    warning: '⚠️',
    error: '❌',
    info: 'ℹ️'
  }
  return iconMap[type] || 'ℹ️'
}

// 获取动态类型标签
const getTypeLabel = (type: string) => {
  const typeMap: Record<string, string> = {
    job: '职位',
    user: '用户',
    system: '系统',
    review: '审核'
  }
  return typeMap[type] || '其他'
}

// 处理待办事项点击
const handleTodoClick = (item: any) => {
  if (item.path) {
    router.push(item.path)
  } else {
    message.info(`${item.title}功能开发中`)
  }
}

// 初始化页面数据
onMounted(() => {
  // 这里可以调用API获取真实数据

  // 每分钟更新系统运行时间
  setInterval(() => {
    systemRuntime.value = dayjs().diff(systemStartTime, 'second')
  }, 60000)
})
</script>

<style scoped lang="scss">
.dashboard-home {
  // 欢迎区域样式
  .welcome-section {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 32px;
    margin-bottom: 32px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 16px;
    color: white;
    box-shadow: 0 8px 32px rgba(102, 126, 234, 0.3);

    .welcome-content {
      flex: 1;

      .welcome-text {
        margin-bottom: 20px;

        .welcome-greeting {
          font-size: 32px;
          font-weight: 700;
          margin: 0 0 8px 0;
        }

        .welcome-subtitle {
          font-size: 16px;
          opacity: 0.9;
          margin: 0;
        }
      }

      .welcome-info {
        display: flex;
        gap: 24px;
        font-size: 14px;

        .system-status {
          display: flex;
          align-items: center;
          gap: 8px;

          .status-dot {
            width: 8px;
            height: 8px;
            border-radius: 50%;

            &.online {
              background: #52c41a;
              box-shadow: 0 0 8px rgba(82, 196, 26, 0.6);
            }
          }
        }

        .runtime {
          opacity: 0.8;
        }
      }
    }

    .admin-avatar {
      display: flex;
      align-items: center;
      gap: 16px;

      .admin-info {
        text-align: right;

        .admin-name {
          font-size: 18px;
          font-weight: 600;
          margin-bottom: 4px;
        }

        .admin-role {
          font-size: 14px;
          opacity: 0.8;
        }
      }
    }
  }

  .content-card {
    margin-bottom: 24px;
  }

  .todos-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
    gap: 16px;
  }

  .todo-item {
    display: flex;
    align-items: center;
    padding: 20px;
    border: 1px solid var(--border-color);
    border-radius: 8px;
    background: var(--bg-primary);
    cursor: pointer;
    transition: all 0.2s ease;
    position: relative;

    &:hover {
      border-color: var(--primary-color);
      box-shadow: 0 4px 12px rgba(47, 124, 255, 0.15);
      transform: translateY(-2px);
    }

    &.has-count {
      padding-right: 60px;
    }
  }

  .todo-icon {
    font-size: 24px;
    margin-right: 16px;
    flex-shrink: 0;
  }

  .todo-content {
    flex: 1;

    .todo-title {
      font-size: 16px;
      font-weight: 500;
      color: var(--text-primary);
      margin-bottom: 4px;
    }

    .todo-desc {
      font-size: 14px;
      color: var(--text-secondary);
    }
  }

  .todo-count {
    position: absolute;
    right: 20px;
    top: 50%;
    transform: translateY(-50%);
  }

  .stats-card {
    margin-bottom: 24px;

    .stats-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
      gap: 24px;
    }

    .stat-group {
      .group-title {
        font-size: 16px;
        font-weight: 600;
        color: var(--text-primary);
        margin: 0 0 16px 0;
        padding-bottom: 8px;
        border-bottom: 2px solid var(--border-light);
      }

      .stat-items {
        display: flex;
        flex-direction: column;
        gap: 12px;
      }

      .stat-item {
        display: flex;
        align-items: center;
        padding: 16px;
        background: var(--bg-secondary);
        border-radius: 8px;
        transition: all 0.2s ease;

        &:hover {
          background: var(--bg-tertiary);
          transform: translateY(-1px);
        }

        .stat-icon {
          font-size: 24px;
          margin-right: 12px;
          flex-shrink: 0;
        }

        .stat-info {
          flex: 1;

          .stat-value {
            font-size: 20px;
            font-weight: 600;
            color: var(--text-primary);
            margin-bottom: 4px;
          }

          .stat-label {
            font-size: 14px;
            color: var(--text-secondary);
            margin-bottom: 4px;
          }

          .stat-growth {
            display: flex;
            align-items: center;
            gap: 4px;
            font-size: 12px;

            &.up {
              color: var(--success-color);
            }

            &.down {
              color: var(--error-color);
            }

            &.stable {
              color: var(--text-disabled);
            }
          }
        }
      }
    }
  }

  .chart-container {
    height: 300px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: var(--bg-secondary);
    border-radius: 8px;
  }

  .chart-placeholder {
    text-align: center;
    color: var(--text-secondary);

    .chart-icon {
      font-size: 48px;
      display: block;
      margin-bottom: 16px;
    }

    .chart-desc {
      font-size: 14px;
      margin-top: 8px;
    }
  }

  .activities-list {
    max-height: 300px;
    overflow-y: auto;
  }

  .activity-item {
    display: flex;
    align-items: flex-start;
    padding: 12px 0;
    border-bottom: 1px solid var(--border-light);

    &:last-child {
      border-bottom: none;
    }
  }

  .activity-avatar {
    margin-right: 12px;
    flex-shrink: 0;
  }

  .activity-content {
    flex: 1;

    .activity-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 4px;

      .activity-type {
        font-size: 12px;
        padding: 2px 8px;
        border-radius: 12px;
        background: var(--bg-tertiary);
        color: var(--text-secondary);
        font-weight: 500;

        &.job {
          background: #e6f7ff;
          color: #1890ff;
        }

        &.user {
          background: #f6ffed;
          color: #52c41a;
        }

        &.system {
          background: #fff2e8;
          color: #fa8c16;
        }

        &.review {
          background: #fff1f0;
          color: #f5222d;
        }
      }

      .activity-time {
        font-size: 12px;
        color: var(--text-disabled);
      }
    }

    .activity-text {
      font-size: 14px;
      color: var(--text-primary);
      margin-bottom: 4px;

      .activity-user {
        font-weight: 500;
        margin-right: 4px;
      }

      .activity-action {
        color: var(--text-secondary);
      }
    }

    .activity-detail {
      font-size: 13px;
      color: var(--text-disabled);
      margin-top: 4px;
      padding-left: 4px;
      border-left: 2px solid var(--border-light);
      padding-left: 8px;
    }
  }

  .activities-more {
    text-align: center;
    margin-top: 16px;
    padding-top: 16px;
    border-top: 1px solid var(--border-light);
  }

  .alerts-card {
    .alerts-list {
      max-height: 400px;
      overflow-y: auto;
    }
  }

  .alert-item {
    display: flex;
    align-items: flex-start;
    padding: 16px;
    border-radius: 8px;
    margin-bottom: 12px;
    background: var(--bg-secondary);

    &.success {
      border-left: 4px solid var(--success-color);
    }

    &.warning {
      border-left: 4px solid var(--warning-color);
    }

    &.error {
      border-left: 4px solid var(--error-color);
    }

    &.info {
      border-left: 4px solid var(--info-color);
    }
  }

  .alert-icon {
    font-size: 20px;
    margin-right: 12px;
    flex-shrink: 0;
  }

  .alert-content {
    flex: 1;

    .alert-title {
      font-size: 14px;
      font-weight: 500;
      color: var(--text-primary);
      margin-bottom: 4px;
    }

    .alert-desc {
      font-size: 13px;
      color: var(--text-secondary);
    }
  }

  .alert-time {
    font-size: 12px;
    color: var(--text-disabled);
    flex-shrink: 0;
    margin-left: 12px;
  }
}

// 响应式设计
@media (max-width: 1200px) {
  .dashboard-home {
    .stats-grid {
      grid-template-columns: 1fr;
    }
  }
}

@media (max-width: 768px) {
  .dashboard-home {
    .welcome-section {
      flex-direction: column;
      gap: 24px;
      text-align: center;
      padding: 24px;

      .welcome-content {
        .welcome-text {
          .welcome-greeting {
            font-size: 24px;
          }
        }

        .welcome-info {
          flex-direction: column;
          gap: 12px;
        }
      }

      .admin-avatar {
        flex-direction: column;
        gap: 12px;

        .admin-info {
          text-align: center;
        }
      }
    }

    .todos-grid {
      grid-template-columns: 1fr;
    }

    .stat-item {
      padding: 16px;
    }

    .stat-icon {
      font-size: 28px;
      margin-right: 12px;
    }

    .stat-content .stat-value {
      font-size: 20px;
    }

    .chart-container {
      height: 200px;
    }
  }
}
</style>