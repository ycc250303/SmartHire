<template>
  <div class="system-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">系统管理</h1>
        <p class="page-description">监控平台运行状态，管理系统配置和数据</p>
      </div>
      <div class="header-status">
        <div class="status-item" :class="systemStatus.health">
          <div class="status-indicator"></div>
          <span class="status-text">{{ systemStatus.text }}</span>
        </div>
      </div>
    </div>

    <!-- 系统状态监控 -->
    <div class="status-section">
      <NCard title="系统状态" :bordered="false" class="status-card">
        <div class="status-grid">
          <div class="status-item-card" :class="serverStatus.status">
            <div class="status-header">
              <div class="status-icon">🖥️</div>
              <div class="status-info">
                <h4>服务器状态</h4>
                <p>{{ serverStatus.text }}</p>
              </div>
            </div>
            <div class="status-metrics">
              <div class="metric">
                <span class="label">CPU使用率</span>
                <span class="value">{{ serverStatus.cpu }}%</span>
              </div>
              <div class="metric">
                <span class="label">内存使用率</span>
                <span class="value">{{ serverStatus.memory }}%</span>
              </div>
            </div>
          </div>

          <div class="status-item-card" :class="databaseStatus.status">
            <div class="status-header">
              <div class="status-icon">🗄️</div>
              <div class="status-info">
                <h4>数据库状态</h4>
                <p>{{ databaseStatus.text }}</p>
              </div>
            </div>
            <div class="status-metrics">
              <div class="metric">
                <span class="label">连接数</span>
                <span class="value">{{ databaseStatus.connections }}</span>
              </div>
              <div class="metric">
                <span class="label">响应时间</span>
                <span class="value">{{ databaseStatus.responseTime }}ms</span>
              </div>
            </div>
          </div>

          <div class="status-item-card" :class="apiStatus.status">
            <div class="status-header">
              <div class="status-icon">🌐</div>
              <div class="status-info">
                <h4>API服务</h4>
                <p>{{ apiStatus.text }}</p>
              </div>
            </div>
            <div class="status-metrics">
              <div class="metric">
                <span class="label">今日请求</span>
                <span class="value">{{ formatNumber(apiStatus.todayRequests) }}</span>
              </div>
              <div class="metric">
                <span class="label">成功率</span>
                <span class="value">{{ apiStatus.successRate }}%</span>
              </div>
            </div>
          </div>

          <div class="status-item-card" :class="storageStatus.status">
            <div class="status-header">
              <div class="status-icon">💾</div>
              <div class="status-info">
                <h4>存储空间</h4>
                <p>{{ storageStatus.text }}</p>
              </div>
            </div>
            <div class="status-metrics">
              <div class="metric">
                <span class="label">已用空间</span>
                <span class="value">{{ storageStatus.used }}</span>
              </div>
              <div class="metric">
                <span class="label">总容量</span>
                <span class="value">{{ storageStatus.total }}</span>
              </div>
            </div>
          </div>
        </div>
      </NCard>
    </div>

    <!-- 功能入口 -->
    <div class="functions-section">
      <NCard title="系统功能" :bordered="false" class="functions-card">
        <NGrid :x-gap="24" :y-gap="24" responsive="screen">
          <NGi span="24 s:12 m:8 l:6" v-for="item in functionItems" :key="item.key">
            <div class="function-item" :class="item.type" @click="handleFunctionClick(item)">
              <div class="function-icon">{{ item.icon }}</div>
              <div class="function-content">
                <h4>{{ item.title }}</h4>
                <p>{{ item.description }}</p>
              </div>
              <div class="function-arrow">→</div>
            </div>
          </NGi>
        </NGrid>
      </NCard>
    </div>

    <!-- 快捷操作 -->
    <div class="actions-section">
      <NCard title="快捷操作" :bordered="false" class="actions-card">
        <div class="actions-grid">
          <div class="action-item" @click="handleQuickAction('cache')">
            <div class="action-icon">🗑️</div>
            <div class="action-content">
              <h5>清理缓存</h5>
              <p>清理系统临时缓存数据</p>
            </div>
          </div>

          <div class="action-item" @click="handleQuickAction('backup')">
            <div class="action-icon">💾</div>
            <div class="action-content">
              <h5>数据备份</h5>
              <p>立即执行数据备份</p>
            </div>
          </div>

          <div class="action-item" @click="handleQuickAction('restart')">
            <div class="action-icon">🔄</div>
            <div class="action-content">
              <h5>重启服务</h5>
              <p>重启应用服务</p>
            </div>
          </div>

          <div class="action-item warning" @click="handleQuickAction('maintenance')">
            <div class="action-icon">⚠️</div>
            <div class="action-content">
              <h5>维护模式</h5>
              <p>开启/关闭系统维护</p>
            </div>
          </div>
        </div>
      </NCard>
    </div>

    <!-- 系统日志 -->
    <div class="logs-section">
      <NCard title="系统日志" :bordered="false" class="logs-card">
        <div class="logs-header">
          <div class="logs-tabs">
            <div
              class="log-tab"
              :class="{ active: activeLogTab === tab.key }"
              v-for="tab in logTabs"
              :key="tab.key"
              @click="activeLogTab = tab.key"
            >
              {{ tab.label }}
              <NTag :type="tab.type" size="small">{{ tab.count }}</NTag>
            </div>
          </div>
          <div class="logs-actions">
            <NButton size="small" @click="refreshLogs">
              <template #icon>🔄</template>
              刷新
            </NButton>
            <NButton size="small" type="primary" @click="viewAllLogs">
              查看全部
            </NButton>
          </div>
        </div>

        <div class="logs-content">
          <div
            class="log-item"
            v-for="log in filteredLogs"
            :key="log.id"
            :class="log.level"
          >
            <div class="log-time">{{ formatTime(log.time) }}</div>
            <div class="log-level">
              <NTag :type="getLogLevelType(log.level)" size="small">
                {{ log.level }}
              </NTag>
            </div>
            <div class="log-message">{{ log.message }}</div>
          </div>
        </div>

        <div v-if="filteredLogs.length === 0" class="logs-empty">
          <div class="empty-icon">📝</div>
          <p>暂无日志记录</p>
        </div>
      </NCard>
    </div>

    <!-- 操作确认弹窗 -->
    <NModal v-model:show="showActionModal" :mask-closable="false">
      <NCard
        style="max-width: 500px"
        :title="actionTitle"
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

        <div class="action-confirm">
          <div class="confirm-icon">{{ actionIcon }}</div>
          <p class="confirm-message">{{ actionMessage }}</p>
          <div v-if="showPasswordField" class="confirm-password">
            <NInput
              v-model:value="confirmPassword"
              type="password"
              placeholder="请输入管理员密码确认"
              show-password-on="click"
            />
          </div>
        </div>

        <template #footer>
          <div class="modal-actions">
            <NButton @click="showActionModal = false">取消</NButton>
            <NButton
              :type="actionType === 'maintenance' ? 'warning' : 'primary'"
              :loading="actionLoading"
              @click="executeAction"
              :disabled="showPasswordField && !confirmPassword.trim()"
            >
              确认执行
            </NButton>
          </div>
        </template>
      </NCard>
    </NModal>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  NCard,
  NGrid,
  NGi,
  NTag,
  NButton,
  NModal,
  NInput,
  useMessage,
  useDialog
} from 'naive-ui'
import dayjs from 'dayjs'

interface SystemStatus {
  health: 'healthy' | 'warning' | 'error'
  text: string
}

interface StatusItem {
  status: 'healthy' | 'warning' | 'error'
  text: string
  [key: string]: any
}

interface FunctionItem {
  key: string
  title: string
  description: string
  icon: string
  path: string
  type: 'primary' | 'warning' | 'danger' | 'info'
}

interface LogItem {
  id: string
  time: string
  level: 'INFO' | 'WARNING' | 'ERROR' | 'DEBUG'
  message: string
}

interface LogTab {
  key: string
  label: string
  count: number
  type: string
}

const router = useRouter()
const message = useMessage()
const dialog = useDialog()

// 系统状态
const systemStatus = ref<SystemStatus>({
  health: 'healthy',
  text: '系统运行正常'
})

const serverStatus = ref<StatusItem>({
  status: 'healthy',
  text: '服务器运行正常',
  cpu: 45,
  memory: 68
})

const databaseStatus = ref<StatusItem>({
  status: 'healthy',
  text: '数据库连接正常',
  connections: 25,
  responseTime: 12
})

const apiStatus = ref<StatusItem>({
  status: 'healthy',
  text: 'API服务正常',
  todayRequests: 15420,
  successRate: 99.8
})

const storageStatus = ref<StatusItem>({
  status: 'warning',
  text: '存储空间较满',
  used: '850GB',
  total: '1TB'
})

// 功能入口
const functionItems: FunctionItem[] = [
  {
    key: 'logs',
    title: '操作日志',
    description: '查看系统操作记录',
    icon: '📋',
    path: '/dashboard/system/logs',
    type: 'info'
  },
  {
    key: 'settings',
    title: '系统设置',
    description: '配置系统参数',
    icon: '⚙️',
    path: '/dashboard/system/settings',
    type: 'info'
  },
  {
    key: 'backup',
    title: '数据备份',
    description: '管理数据备份任务',
    icon: '💾',
    path: '/dashboard/system/backup',
    type: 'primary'
  },
  {
    key: 'monitor',
    title: '性能监控',
    description: '查看系统性能指标',
    icon: '📊',
    path: '/dashboard/system/monitor',
    type: 'info'
  },
  {
    key: 'security',
    title: '安全管理',
    description: '系统安全配置和监控',
    icon: '🔒',
    path: '/dashboard/system/security',
    type: 'warning'
  }
]

// 日志相关
const activeLogTab = ref('all')
const logTabs: LogTab[] = [
  { key: 'all', label: '全部', count: 128, type: 'default' },
  { key: 'error', label: '错误', count: 3, type: 'error' },
  { key: 'warning', label: '警告', count: 12, type: 'warning' },
  { key: 'info', label: '信息', count: 113, type: 'info' }
]

const systemLogs = ref<LogItem[]>([
  {
    id: '1',
    time: '2024-01-15 18:30:00',
    level: 'INFO',
    message: '用户 admin 登录系统'
  },
  {
    id: '2',
    time: '2024-01-15 18:25:00',
    level: 'WARNING',
    message: 'API响应时间超过阈值: /api/jobs/list'
  },
  {
    id: '3',
    time: '2024-01-15 18:20:00',
    level: 'INFO',
    message: '数据备份任务完成'
  },
  {
    id: '4',
    time: '2024-01-15 18:15:00',
    level: 'ERROR',
    message: '数据库连接超时'
  },
  {
    id: '5',
    time: '2024-01-15 18:10:00',
    level: 'INFO',
    message: '系统缓存已清理'
  }
])

// 操作弹窗
const showActionModal = ref(false)
const actionType = ref('')
const actionTitle = ref('')
const actionMessage = ref('')
const actionIcon = ref('')
const actionLoading = ref(false)
const showPasswordField = ref(false)
const confirmPassword = ref('')

// 计算属性
const filteredLogs = computed(() => {
  if (activeLogTab.value === 'all') {
    return systemLogs.value
  }
  return systemLogs.value.filter(log => log.level.toLowerCase() === activeLogTab.value)
})

// 定时器
let statusTimer: NodeJS.Timeout | null = null

// 辅助方法
const formatNumber = (num: number) => {
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + 'w'
  } else if (num >= 1000) {
    return (num / 1000).toFixed(1) + 'k'
  }
  return num.toString()
}

const formatTime = (time: string) => {
  return dayjs(time).format('HH:mm:ss')
}

const getLogLevelType = (level: string) => {
  const typeMap: Record<string, string> = {
    ERROR: 'error',
    WARNING: 'warning',
    INFO: 'info',
    DEBUG: 'default'
  }
  return typeMap[level] || 'default'
}

// 更新系统状态
const updateSystemStatus = () => {
  // 模拟状态更新
  const healthStates = ['healthy', 'warning', 'error']
  const randomHealth = healthStates[Math.floor(Math.random() * 10) > 7 ? 1 : 0]

  serverStatus.value.cpu = Math.floor(Math.random() * 30) + 30
  serverStatus.value.memory = Math.floor(Math.random() * 20) + 60
  databaseStatus.value.responseTime = Math.floor(Math.random() * 10) + 8

  // 更新存储状态
  const usedGB = Math.floor(Math.random() * 100) + 800
  storageStatus.value.used = `${usedGB}GB`
  if (usedGB > 900) {
    storageStatus.value.status = 'warning'
    storageStatus.value.text = '存储空间较满'
  } else {
    storageStatus.value.status = 'healthy'
    storageStatus.value.text = '存储空间充足'
  }
}

// 事件处理
const handleFunctionClick = (item: FunctionItem) => {
  if (item.path) {
    router.push(item.path)
  } else {
    message.info(`${item.title}功能开发中`)
  }
}

const handleQuickAction = (type: string) => {
  actionType.value = type
  showPasswordField.value = type === 'maintenance' || type === 'restart'

  const actionConfigs: Record<string, any> = {
    cache: {
      title: '清理缓存',
      icon: '🗑️',
      message: '确定要清理系统缓存吗？这将清理所有临时缓存数据。'
    },
    backup: {
      title: '数据备份',
      icon: '💾',
      message: '确定要立即执行数据备份吗？备份过程可能需要几分钟时间。'
    },
    restart: {
      title: '重启服务',
      icon: '🔄',
      message: '确定要重启应用服务吗？这将导致服务短暂中断。'
    },
    maintenance: {
      title: '维护模式',
      icon: '⚠️',
      message: '确定要切换系统维护模式吗？维护模式下用户将无法正常访问系统。'
    }
  }

  const config = actionConfigs[type]
  actionTitle.value = config.title
  actionIcon.value = config.icon
  actionMessage.value = config.message
  confirmPassword.value = ''
  showActionModal.value = true
}

const executeAction = async () => {
  actionLoading.value = true

  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 2000))

    const successMessages: Record<string, string> = {
      cache: '系统缓存清理完成',
      backup: '数据备份任务已启动',
      restart: '服务重启指令已发送',
      maintenance: '系统维护模式已切换'
    }

    message.success(successMessages[actionType.value])
    showActionModal.value = false
    confirmPassword.value = ''

    // 添加操作日志
    const newLog: LogItem = {
      id: Date.now().toString(),
      time: dayjs().format('YYYY-MM-DD HH:mm:ss'),
      level: 'INFO',
      message: `执行操作: ${successMessages[actionType.value]}`
    }
    systemLogs.value.unshift(newLog)

  } catch (error) {
    message.error('操作执行失败，请重试')
  } finally {
    actionLoading.value = false
  }
}

const refreshLogs = () => {
  message.success('日志已刷新')
  // 模拟获取新日志
  const newLog: LogItem = {
    id: Date.now().toString(),
    time: dayjs().format('YYYY-MM-DD HH:mm:ss'),
    level: 'INFO',
    message: '手动刷新系统日志'
  }
  systemLogs.value.unshift(newLog)
}

const viewAllLogs = () => {
  router.push('/dashboard/system/logs')
}

// 页面初始化
onMounted(() => {
  // 启动状态监控定时器
  statusTimer = setInterval(() => {
    updateSystemStatus()
  }, 30000) // 每30秒更新一次

  // 立即更新一次
  updateSystemStatus()
})

onUnmounted(() => {
  if (statusTimer) {
    clearInterval(statusTimer)
  }
})
</script>

<style scoped lang="scss">
.system-page {
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

    .header-status {
      .status-item {
        display: flex;
        align-items: center;
        gap: 8px;
        padding: 8px 16px;
        background: rgba(255, 255, 255, 0.1);
        border-radius: 20px;
        backdrop-filter: blur(10px);

        .status-indicator {
          width: 8px;
          height: 8px;
          border-radius: 50%;
          background: currentColor;
          animation: pulse 2s infinite;
        }

        .status-text {
          font-size: 14px;
          font-weight: 500;
        }

        &.healthy {
          color: #52c41a;
        }

        &.warning {
          color: #faad14;
        }

        &.error {
          color: #f5222d;
        }
      }
    }
  }

  // 系统状态监控
  .status-section {
    margin-bottom: 32px;

    .status-card {
      .status-grid {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
        gap: 20px;

        .status-item-card {
          padding: 20px;
          border-radius: 12px;
          border: 2px solid var(--border-color);
          background: var(--bg-primary);
          transition: all 0.3s ease;

          &.healthy {
            border-color: var(--success-color);
            background: rgba(82, 196, 26, 0.05);
          }

          &.warning {
            border-color: var(--warning-color);
            background: rgba(250, 173, 20, 0.05);
          }

          &.error {
            border-color: var(--error-color);
            background: rgba(245, 34, 45, 0.05);
          }

          .status-header {
            display: flex;
            align-items: center;
            gap: 12px;
            margin-bottom: 16px;

            .status-icon {
              font-size: 32px;
            }

            .status-info {
              h4 {
                margin: 0 0 4px 0;
                font-size: 16px;
                font-weight: 600;
                color: var(--text-primary);
              }

              p {
                margin: 0;
                font-size: 14px;
                color: var(--text-secondary);
              }
            }
          }

          .status-metrics {
            display: flex;
            gap: 16px;

            .metric {
              display: flex;
              flex-direction: column;
              gap: 4px;

              .label {
                font-size: 12px;
                color: var(--text-disabled);
              }

              .value {
                font-size: 18px;
                font-weight: 600;
                color: var(--text-primary);
              }
            }
          }
        }
      }
    }
  }

  // 功能入口
  .functions-section {
    margin-bottom: 32px;

    .functions-card {
      .function-item {
        display: flex;
        align-items: center;
        gap: 16px;
        padding: 20px;
        border-radius: 12px;
        border: 1px solid var(--border-color);
        background: var(--bg-primary);
        cursor: pointer;
        transition: all 0.3s ease;

        &:hover {
          transform: translateY(-2px);
          box-shadow: var(--shadow-md);
        }

        &.primary {
          border-color: var(--primary-color);
          background: rgba(32, 128, 240, 0.05);

          &:hover {
            border-color: var(--primary-color);
            background: rgba(32, 128, 240, 0.1);
          }
        }

        &.warning {
          border-color: var(--warning-color);
          background: rgba(250, 173, 20, 0.05);

          &:hover {
            border-color: var(--warning-color);
            background: rgba(250, 173, 20, 0.1);
          }
        }

        &.danger {
          border-color: var(--error-color);
          background: rgba(245, 34, 45, 0.05);

          &:hover {
            border-color: var(--error-color);
            background: rgba(245, 34, 45, 0.1);
          }
        }

        .function-icon {
          font-size: 28px;
          flex-shrink: 0;
        }

        .function-content {
          flex: 1;

          h4 {
            margin: 0 0 4px 0;
            font-size: 16px;
            font-weight: 600;
            color: var(--text-primary);
          }

          p {
            margin: 0;
            font-size: 14px;
            color: var(--text-secondary);
          }
        }

        .function-arrow {
          font-size: 18px;
          color: var(--text-disabled);
          transition: all 0.3s ease;
        }

        &:hover .function-arrow {
          transform: translateX(4px);
          color: var(--primary-color);
        }
      }
    }
  }

  // 快捷操作
  .actions-section {
    margin-bottom: 32px;

    .actions-card {
      .actions-grid {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
        gap: 16px;

        .action-item {
          display: flex;
          align-items: center;
          gap: 12px;
          padding: 16px;
          border-radius: 8px;
          border: 1px solid var(--border-color);
          background: var(--bg-primary);
          cursor: pointer;
          transition: all 0.3s ease;

          &:hover {
            transform: translateY(-2px);
            box-shadow: var(--shadow-md);
          }

          &.warning {
            border-color: var(--warning-color);
            background: rgba(250, 173, 20, 0.05);

            &:hover {
              border-color: var(--warning-color);
              background: rgba(250, 173, 20, 0.1);
            }
          }

          .action-icon {
            font-size: 24px;
            flex-shrink: 0;
          }

          .action-content {
            h5 {
              margin: 0 0 4px 0;
              font-size: 14px;
              font-weight: 600;
              color: var(--text-primary);
            }

            p {
              margin: 0;
              font-size: 12px;
              color: var(--text-secondary);
            }
          }
        }
      }
    }
  }

  // 系统日志
  .logs-section {
    .logs-card {
      .logs-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 20px;
        padding-bottom: 16px;
        border-bottom: 1px solid var(--border-color);

        .logs-tabs {
          display: flex;
          gap: 8px;

          .log-tab {
            display: flex;
            align-items: center;
            gap: 8px;
            padding: 8px 12px;
            border-radius: 6px;
            cursor: pointer;
            transition: all 0.2s ease;

            &:hover {
              background: var(--bg-secondary);
            }

            &.active {
              background: var(--primary-color);
              color: white;
            }
          }
        }

        .logs-actions {
          display: flex;
          gap: 12px;
        }
      }

      .logs-content {
        .log-item {
          display: flex;
          align-items: center;
          gap: 12px;
          padding: 12px 0;
          border-bottom: 1px solid var(--border-color);

          &:last-child {
            border-bottom: none;
          }

          &.ERROR {
            background: rgba(245, 34, 45, 0.05);
            margin: 0 -16px;
            padding: 12px 16px;
            border-radius: 4px;
          }

          &.WARNING {
            background: rgba(250, 173, 20, 0.05);
            margin: 0 -16px;
            padding: 12px 16px;
            border-radius: 4px;
          }

          .log-time {
            font-size: 12px;
            color: var(--text-disabled);
            min-width: 60px;
          }

          .log-level {
            min-width: 60px;
          }

          .log-message {
            flex: 1;
            font-size: 14px;
            color: var(--text-primary);
          }
        }
      }

      .logs-empty {
        text-align: center;
        padding: 40px;

        .empty-icon {
          font-size: 48px;
          margin-bottom: 16px;
          opacity: 0.5;
        }

        p {
          margin: 0;
          color: var(--text-secondary);
        }
      }
    }
  }

  // 操作确认弹窗
  .action-confirm {
    text-align: center;
    padding: 20px 0;

    .confirm-icon {
      font-size: 48px;
      margin-bottom: 16px;
    }

    .confirm-message {
      margin-bottom: 20px;
      color: var(--text-secondary);
      line-height: 1.5;
    }

    .confirm-password {
      max-width: 300px;
      margin: 0 auto;
    }
  }

  .modal-actions {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
  }
}

// 脉冲动画
@keyframes pulse {
  0% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
  100% {
    opacity: 1;
  }
}

// 响应式设计
@media (max-width: 768px) {
  .system-page {
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
    }

    .status-section {
      .status-card {
        .status-grid {
          grid-template-columns: 1fr;
        }
      }
    }

    .functions-section {
      .functions-card {
        .n-grid {
          .n-gi {
            span: 24;
          }
        }
      }
    }

    .actions-section {
      .actions-card {
        .actions-grid {
          grid-template-columns: 1fr;
        }
      }
    }

    .logs-section {
      .logs-card {
        .logs-header {
          flex-direction: column;
          gap: 16px;

          .logs-tabs {
            width: 100%;
            justify-content: center;
          }

          .logs-actions {
            width: 100%;
            justify-content: center;
          }
        }
      }
    }
  }
}
</style>