<template>
  <view class="logs-page">
    <!-- 筛选栏 -->
    <view class="filter-bar">
      <view class="filter-item">
        <text class="filter-label">时间范围</text>
        <picker
          :value="timeRangeIndex"
          :range="timeRangeOptions"
          range-key="label"
          @change="onTimeRangeChange"
        >
          <view class="picker-content">
            <text>{{ currentTimeRange.label }}</text>
            <text class="picker-arrow">▼</text>
          </view>
        </picker>
      </view>

      <view class="filter-item">
        <text class="filter-label">操作类型</text>
        <picker
          :value="actionTypeIndex"
          :range="actionTypeOptions"
          range-key="label"
          @change="onActionTypeChange"
        >
          <view class="picker-content">
            <text>{{ currentActionType.label }}</text>
            <text class="picker-arrow">▼</text>
          </view>
        </picker>
      </view>

      <view class="filter-item">
        <text class="filter-label">操作者</text>
        <input
          type="text"
          class="filter-input"
          placeholder="输入操作者姓名"
          v-model="operatorFilter"
          @confirm="loadLogs"
        />
      </view>
    </view>

    <!-- 统计概览 -->
    <view class="stats-overview">
      <view class="stat-item">
        <text class="stat-value">{{ formatNumber(totalLogs) }}</text>
        <text class="stat-label">总日志数</text>
      </view>
      <view class="stat-item">
        <text class="stat-value">{{ formatNumber(todayLogs) }}</text>
        <text class="stat-label">今日操作</text>
      </view>
      <view class="stat-item">
        <text class="stat-value">{{ formatNumber(errorLogs) }}</text>
        <text class="stat-label">异常操作</text>
      </view>
    </view>

    <!-- 日志列表 -->
    <view class="log-list">
      <view
        class="log-item"
        v-for="log in filteredLogs"
        :key="log.id"
        :class="{ 'error-log': log.level === 'error' }"
      >
        <view class="log-header">
          <view class="log-action-info">
            <text class="log-action">{{ log.action }}</text>
            <view class="log-level" :class="log.level">{{ getLevelText(log.level) }}</view>
          </view>
          <text class="log-time">{{ formatDateTime(log.timestamp) }}</text>
        </view>

        <view class="log-details">
          <view class="log-operator">
            <text class="operator-label">操作者:</text>
            <text class="operator-name">{{ log.operator }}</text>
            <text class="operator-role">({{ log.operatorRole }})</text>
          </view>

          <view class="log-target" v-if="log.target">
            <text class="target-label">操作对象:</text>
            <text class="target-info">{{ log.target.type }} - {{ log.target.name }}</text>
          </view>

          <view class="log-description" v-if="log.description">
            <text class="description-text">{{ log.description }}</text>
          </view>

          <view class="log-metadata" v-if="log.metadata">
            <view class="metadata-item" v-for="(value, key) in log.metadata" :key="key">
              <text class="metadata-key">{{ key }}:</text>
              <text class="metadata-value">{{ value }}</text>
            </view>
          </view>

          <view class="log-ip" v-if="log.ip">
            <text class="ip-label">IP地址:</text>
            <text class="ip-value">{{ log.ip }}</text>
            <text class="ip-location" v-if="log.location">{{ log.location }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 加载更多 -->
    <view class="load-more" v-if="hasMore" @click="loadMore">
      <text>{{ loading ? '加载中...' : '加载更多' }}</text>
    </view>

    <!-- 空状态 -->
    <view class="empty-state" v-if="filteredLogs.length === 0 && !loading">
      <view class="empty-icon">📋</view>
      <text class="empty-title">暂无日志记录</text>
      <text class="empty-desc">没有找到符合条件的操作日志</text>
    </view>

    <!-- 导出按钮 -->
    <view class="export-actions">
      <button class="export-btn" @click="exportLogs">导出日志</button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';

interface LogMetadata {
  [key: string]: string | number;
}

interface LogTarget {
  type: string;
  name: string;
  id: string;
}

interface LogItem {
  id: string;
  action: string;
  operator: string;
  operatorRole: string;
  timestamp: Date;
  level: 'info' | 'warning' | 'error';
  description?: string;
  target?: LogTarget;
  metadata?: LogMetadata;
  ip?: string;
  location?: string;
}

interface FilterOption {
  label: string;
  value: string;
}

const logs = ref<LogItem[]>([]);
const loading = ref(false);
const hasMore = ref(true);
const operatorFilter = ref('');

// 筛选选项
const timeRangeIndex = ref(0);
const timeRangeOptions: FilterOption[] = [
  { label: '今天', value: 'today' },
  { label: '最近3天', value: '3days' },
  { label: '最近7天', value: '7days' },
  { label: '最近30天', value: '30days' },
  { label: '全部', value: 'all' }
];

const actionTypeIndex = ref(0);
const actionTypeOptions: FilterOption[] = [
  { label: '全部', value: 'all' },
  { label: '用户管理', value: 'user' },
  { label: '职位审核', value: 'job' },
  { label: '系统设置', value: 'system' },
  { label: '发布公告', value: 'announcement' },
  { label: '登录登出', value: 'auth' }
];

const currentTimeRange = ref(timeRangeOptions[0]);
const currentActionType = ref(actionTypeOptions[0]);

// 统计数据
const totalLogs = ref(12547);
const todayLogs = ref(238);
const errorLogs = ref(12);

// 模拟日志数据
const mockLogs: LogItem[] = [
  {
    id: '1',
    action: '审核通过职位',
    operator: '张管理员',
    operatorRole: '超级管理员',
    timestamp: new Date(Date.now() - 30 * 60 * 1000),
    level: 'info',
    description: '通过了前端开发工程师职位的审核',
    target: {
      type: '职位',
      name: '前端开发工程师',
      id: 'job_123'
    },
    metadata: {
      '职位ID': 'job_123',
      '公司': '科技有限公司',
      '审核结果': '通过'
    },
    ip: '192.168.1.100',
    location: '北京'
  },
  {
    id: '2',
    action: '禁用用户',
    operator: '李管理员',
    operatorRole: '普通管理员',
    timestamp: new Date(Date.now() - 2 * 60 * 60 * 1000),
    level: 'warning',
    description: '因违规行为禁用用户账号',
    target: {
      type: '用户',
      name: '张三',
      id: 'user_456'
    },
    metadata: {
      '用户ID': 'user_456',
      '禁用原因': '发布虚假信息',
      '禁用时长': '30天'
    },
    ip: '192.168.1.101',
    location: '上海'
  },
  {
    id: '3',
    action: '系统登录失败',
    operator: '未知用户',
    operatorRole: '访客',
    timestamp: new Date(Date.now() - 3 * 60 * 60 * 1000),
    level: 'error',
    description: '多次登录失败，可能存在恶意攻击',
    metadata: {
      '登录IP': '117.21.188.123',
      '失败次数': '5',
      '失败原因': '密码错误'
    },
    ip: '117.21.188.123',
    location: '美国'
  },
  {
    id: '4',
    action: '发布公告',
    operator: '王管理员',
    operatorRole: '超级管理员',
    timestamp: new Date(Date.now() - 4 * 60 * 60 * 1000),
    level: 'info',
    description: '发布了系统维护通知',
    target: {
      type: '公告',
      name: '系统维护通知',
      id: 'announcement_789'
    },
    metadata: {
      '公告ID': 'announcement_789',
      '公告类型': '维护通知',
      '发布方式': '立即发布'
    },
    ip: '192.168.1.102',
    location: '深圳'
  },
  {
    id: '5',
    action: '数据备份',
    operator: '系统自动',
    operatorRole: '系统',
    timestamp: new Date(Date.now() - 6 * 60 * 60 * 1000),
    level: 'info',
    description: '自动执行数据库备份任务',
    metadata: {
      '备份类型': '全量备份',
      '备份大小': '2.3GB',
      '备份耗时': '15分钟'
    }
  }
];

const filteredLogs = computed(() => {
  let filtered = logs.value;

  // 按操作类型筛选
  if (currentActionType.value.value !== 'all') {
    filtered = filtered.filter(log => {
      // 根据action判断类型
      switch (currentActionType.value.value) {
        case 'user':
          return log.action.includes('用户') || log.target?.type === '用户';
        case 'job':
          return log.action.includes('职位') || log.action.includes('审核');
        case 'system':
          return log.action.includes('系统') || log.action.includes('备份') || log.action.includes('设置');
        case 'announcement':
          return log.action.includes('公告') || log.target?.type === '公告';
        case 'auth':
          return log.action.includes('登录') || log.action.includes('登出');
        default:
          return true;
      }
    });
  }

  // 按操作者筛选
  if (operatorFilter.value) {
    const keyword = operatorFilter.value.toLowerCase();
    filtered = filtered.filter(log =>
      log.operator.toLowerCase().includes(keyword)
    );
  }

  // 按时间范围筛选
  if (currentTimeRange.value.value !== 'all') {
    const now = new Date();
    const startTime = new Date();

    switch (currentTimeRange.value.value) {
      case 'today':
        startTime.setHours(0, 0, 0, 0);
        break;
      case '3days':
        startTime.setDate(now.getDate() - 3);
        break;
      case '7days':
        startTime.setDate(now.getDate() - 7);
        break;
      case '30days':
        startTime.setDate(now.getDate() - 30);
        break;
    }

    filtered = filtered.filter(log => log.timestamp >= startTime);
  }

  return filtered;
});

const onTimeRangeChange = (e: any) => {
  timeRangeIndex.value = e.detail.value;
  currentTimeRange.value = timeRangeOptions[timeRangeIndex.value];
  loadLogs();
};

const onActionTypeChange = (e: any) => {
  actionTypeIndex.value = e.detail.value;
  currentActionType.value = actionTypeOptions[actionTypeIndex.value];
  loadLogs();
};

const loadLogs = () => {
  loading.value = true;

  setTimeout(() => {
    logs.value = mockLogs;
    loading.value = false;
    hasMore.value = false;
  }, 1000);
};

const loadMore = () => {
  if (loading.value) return;
  loading.value = true;

  setTimeout(() => {
    // 模拟加载更多数据
    hasMore.value = false;
    loading.value = false;
  }, 1000);
};

const exportLogs = () => {
  uni.showModal({
    title: '导出日志',
    content: '确定要导出当前筛选的日志记录吗？',
    success: (res) => {
      if (res.confirm) {
        uni.showLoading({ title: '导出中...' });

        setTimeout(() => {
          uni.hideLoading();
          uni.showToast({ title: '导出成功', icon: 'success' });
        }, 2000);
      }
    }
  });
};

const getLevelText = (level: string): string => {
  const levelMap = {
    info: '信息',
    warning: '警告',
    error: '错误'
  };
  return levelMap[level] || level;
};

const formatNumber = (num: number): string => {
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + 'w';
  }
  if (num >= 1000) {
    return (num / 1000).toFixed(1) + 'k';
  }
  return num.toString();
};

const formatDateTime = (date: Date): string => {
  const now = new Date();
  const diff = now.getTime() - date.getTime();
  const hours = Math.floor(diff / (1000 * 60 * 60));
  const minutes = Math.floor(diff / (1000 * 60));

  if (minutes < 1) {
    return '刚刚';
  } else if (minutes < 60) {
    return `${minutes}分钟前`;
  } else if (hours < 24) {
    return `${hours}小时前`;
  } else {
    return date.toLocaleString('zh-CN', {
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    });
  }
};

onMounted(() => {
  loadLogs();
});
</script>

<style scoped lang="scss">
.logs-page {
  min-height: 100vh;
  background: #f6f7fb;
  padding: 32rpx;
  padding-bottom: 120rpx;
}

.filter-bar {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
  background: #ffffff;
  border-radius: 24rpx;
  padding: 24rpx;
  margin-bottom: 28rpx;
  box-shadow: 0 12rpx 32rpx rgba(31, 55, 118, 0.08);
}

.filter-item {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.filter-label {
  font-size: 26rpx;
  font-weight: 500;
  color: #2f3542;
}

.picker-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx;
  border: 2rpx solid #e5edff;
  border-radius: 16rpx;
  font-size: 28rpx;
}

.picker-arrow {
  color: #7a869a;
  font-size: 20rpx;
}

.filter-input {
  padding: 20rpx;
  border: 2rpx solid #e5edff;
  border-radius: 16rpx;
  font-size: 28rpx;
}

.stats-overview {
  display: flex;
  gap: 16rpx;
  margin-bottom: 28rpx;
}

.stat-item {
  flex: 1;
  background: #ffffff;
  border-radius: 16rpx;
  padding: 24rpx;
  text-align: center;
  box-shadow: 0 12rpx 32rpx rgba(31, 55, 118, 0.08);
}

.stat-value {
  display: block;
  font-size: 32rpx;
  font-weight: 600;
  color: #2f7cff;
  margin-bottom: 8rpx;
}

.stat-label {
  font-size: 24rpx;
  color: #7a869a;
}

.log-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.log-item {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 24rpx;
  box-shadow: 0 12rpx 32rpx rgba(31, 55, 118, 0.08);
  border-left: 6rpx solid #e0e6ed;
}

.log-item.error-log {
  border-left-color: #ff5f5f;
}

.log-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16rpx;
}

.log-action-info {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.log-action {
  font-size: 28rpx;
  font-weight: 600;
  color: #2f3542;
}

.log-level {
  padding: 6rpx 16rpx;
  border-radius: 12rpx;
  font-size: 22rpx;
  font-weight: 500;
}

.log-level.info {
  background: #e5edff;
  color: #2f7cff;
}

.log-level.warning {
  background: #fff7e6;
  color: #ff8c00;
}

.log-level.error {
  background: #fff5f5;
  color: #ff5f5f;
}

.log-time {
  font-size: 24rpx;
  color: #97a0b3;
}

.log-details {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.log-operator {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.operator-label,
.target-label,
.ip-label {
  font-size: 24rpx;
  color: #7a869a;
}

.operator-name,
.target-info {
  font-size: 24rpx;
  color: #2f3542;
  font-weight: 500;
}

.operator-role {
  font-size: 22rpx;
  color: #97a0b3;
}

.log-target {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.log-description {
  padding: 16rpx;
  background: #f8faff;
  border-radius: 12rpx;
}

.description-text {
  font-size: 26rpx;
  color: #6b758a;
  line-height: 1.5;
}

.log-metadata {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}

.metadata-item {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 8rpx 16rpx;
  background: #f0f2f7;
  border-radius: 12rpx;
}

.metadata-key {
  font-size: 22rpx;
  color: #7a869a;
}

.metadata-value {
  font-size: 22rpx;
  color: #2f3542;
  font-weight: 500;
}

.log-ip {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.ip-value {
  font-size: 24rpx;
  color: #2f7cff;
  font-family: monospace;
}

.ip-location {
  font-size: 22rpx;
  color: #97a0b3;
}

.load-more {
  padding: 32rpx;
  text-align: center;
  color: #7a869a;
  font-size: 28rpx;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 0;
}

.empty-icon {
  font-size: 80rpx;
  margin-bottom: 24rpx;
}

.empty-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #7a869a;
  margin-bottom: 8rpx;
}

.empty-desc {
  font-size: 26rpx;
  color: #97a0b3;
}

.export-actions {
  position: fixed;
  bottom: 32rpx;
  right: 32rpx;
}

.export-btn {
  padding: 20rpx 32rpx;
  background: #2f7cff;
  color: #ffffff;
  border: none;
  border-radius: 20rpx;
  font-size: 28rpx;
  font-weight: 500;
  box-shadow: 0 12rpx 32rpx rgba(31, 55, 118, 0.3);
}
</style>