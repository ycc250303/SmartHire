<template>
  <view class="admin-home">
    <view class="top-bar">
      <view class="welcome-block">
        <text class="welcome-title">管理台</text>
        <text class="welcome-subtitle">{{ currentTime }}</text>
      </view>
      <view class="avatar">
        <image :src="avatarImg" mode="aspectFill" />
      </view>
    </view>

    <view class="card todo-card">
      <view class="section-title">今日待办</view>
      <view class="todo-list">
        <view
          class="todo-item"
          v-for="todo in todos"
          :key="todo.id"
          @click="handleTodoClick(todo.route)"
        >
          <view>
            <view class="todo-title">{{ todo.title }}</view>
            <view class="todo-desc">{{ todo.desc }}</view>
          </view>
          <view class="todo-count">{{ todo.count }}</view>
        </view>
      </view>
    </view>

    <view class="card stats-card">
      <view class="section-title">平台数据概览</view>
      <view class="stats-grid">
        <view class="stat-item" v-for="stat in stats" :key="stat.id">
          <text class="stat-label">{{ stat.label }}</text>
          <text class="stat-value">{{ formatNumber(stat.value) }}</text>
          <text class="stat-trend" :class="stat.trend">{{ trendText(stat.trend) }}</text>
        </view>
      </view>
    </view>

    <view class="card alerts-card">
      <view class="section-title">系统提醒</view>
      <view class="alert-list">
        <view
          class="alert-item"
          v-for="alert in alerts"
          :key="alert.id"
          :class="alert.level"
          @click="handleAlertClick(alert)"
        >
          <view class="alert-title">{{ alert.title }}</view>
          <view class="alert-desc">{{ alert.desc }}</view>
          <view class="alert-time">{{ formatTime(alert.time) }}</view>
        </view>
      </view>
    </view>

    </view>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import avatarImg from '@/static/user-avatar.png';

interface TodoItem {
  id: number;
  title: string;
  desc: string;
  count: number;
  route?: string;
}

interface StatItem {
  id: number;
  label: string;
  value: number;
  trend: 'up' | 'down' | 'stable';
  change?: number;
}

interface AlertItem {
  id: number;
  title: string;
  desc: string;
  level: 'info' | 'warning' | 'error';
  time: Date;
  route?: string;
}

const todos = ref<TodoItem[]>([]);
const stats = ref<StatItem[]>([]);
const alerts = ref<AlertItem[]>([]);

const currentTime = computed(() => {
  const now = new Date();
  const hour = now.getHours();
  if (hour < 12) return '上午好';
  if (hour < 18) return '下午好';
  return '晚上好';
});

const loadDashboardData = () => {
  // 模拟数据，实际应该从API获取
  todos.value = [
    {
      id: 1,
      title: '待审核职位',
      desc: '新发布职位等待审核',
      count: 12,
      route: '/pages/admin/review/index'
    },
    {
      id: 2,
      title: '用户举报',
      desc: '处理用户举报内容',
      count: 3,
      route: '/pages/admin/system/index'
    },
    {
      id: 3,
      title: '系统消息',
      desc: '未读系统通知',
      count: 5,
      route: '/pages/admin/system/index'
    }
  ];

  stats.value = [
    {
      id: 1,
      label: '总用户数',
      value: 2847,
      trend: 'up',
      change: 8.5
    },
    {
      id: 2,
      label: '求职者',
      value: 1923,
      trend: 'up',
      change: 6.2
    },
    {
      id: 3,
      label: 'HR用户',
      value: 924,
      trend: 'up',
      change: 12.8
    },
    {
      id: 4,
      label: '活跃职位',
      value: 456,
      trend: 'down',
      change: -2.1
    },
    {
      id: 5,
      label: '今日新增',
      value: 38,
      trend: 'stable'
    },
    {
      id: 6,
      label: '今日申请',
      value: 127,
      trend: 'up',
      change: 15.3
    }
  ];

  alerts.value = [
    {
      id: 1,
      title: '系统维护通知',
      desc: '计划于今晚 23:00-01:00 进行系统维护',
      level: 'info',
      time: new Date(Date.now() - 2 * 60 * 60 * 1000)
    },
    {
      id: 2,
      title: '异常登录提醒',
      desc: '检测到来自异常地点的登录尝试',
      level: 'warning',
      time: new Date(Date.now() - 4 * 60 * 60 * 1000)
    },
    {
      id: 3,
      title: '数据同步异常',
      desc: '用户数据同步失败，请检查',
      level: 'error',
      time: new Date(Date.now() - 6 * 60 * 60 * 1000)
    }
  ];
};

const handleTodoClick = (route?: string) => {
  if (!route) return;
  uni.navigateTo({ url: route });
};

const handleAlertClick = (alert: AlertItem) => {
  if (alert.route) {
    uni.navigateTo({ url: alert.route });
  } else {
    uni.showToast({
      title: '查看详情',
      icon: 'none'
    });
  }
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

const formatTime = (date: Date): string => {
  const now = new Date();
  const diff = now.getTime() - date.getTime();
  const hours = Math.floor(diff / (1000 * 60 * 60));
  const minutes = Math.floor(diff / (1000 * 60));

  if (hours > 0) {
    return `${hours}小时前`;
  }
  if (minutes > 0) {
    return `${minutes}分钟前`;
  }
  return '刚刚';
};

const trendText = (trend: StatItem['trend']): string => {
  switch (trend) {
    case 'up':
      return '较昨日 ↑';
    case 'down':
      return '较昨日 ↓';
    default:
      return '持平';
  }
};

onMounted(() => {
  loadDashboardData();
});
</script>

<style scoped lang="scss">
.admin-home {
  min-height: 100vh;
  padding: 32rpx;
  background: #f6f7fb;
  box-sizing: border-box;
}

.top-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32rpx;
}

.welcome-block {
  display: flex;
  flex-direction: column;
}

.welcome-title {
  font-size: 34rpx;
  font-weight: 600;
  margin-bottom: 8rpx;
}

.welcome-subtitle {
  font-size: 26rpx;
  color: #7a869a;
}

.avatar {
  width: 88rpx;
  height: 88rpx;
  border-radius: 16rpx;
  background: #eef2ff;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.avatar image {
  width: 100%;
  height: 100%;
}

.card {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 32rpx;
  margin-bottom: 28rpx;
  box-shadow: 0 12rpx 32rpx rgba(31, 55, 118, 0.08);
}

.section-title {
  font-size: 30rpx;
  font-weight: 600;
  margin-bottom: 24rpx;
}

.todo-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx 0;
  border-bottom: 2rpx solid #f0f2f7;
}

.todo-item:last-child {
  border-bottom: none;
}

.todo-title {
  font-size: 28rpx;
  font-weight: 500;
}

.todo-desc {
  font-size: 24rpx;
  color: #97a0b3;
  margin-top: 4rpx;
}

.todo-count {
  font-size: 32rpx;
  font-weight: 700;
  color: #2f7cff;
}

.stats-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}

.stat-item {
  flex: 1;
  min-width: calc(33.333% - 16rpx);
  background: #f8faff;
  border-radius: 20rpx;
  padding: 24rpx;
  text-align: center;
}

.stat-label {
  color: #7a869a;
  font-size: 24rpx;
}

.stat-value {
  display: block;
  font-size: 36rpx;
  font-weight: 600;
  margin: 12rpx 0;
}

.stat-trend.up {
  color: #28a745;
  font-size: 22rpx;
}

.stat-trend.down {
  color: #ff5f5f;
  font-size: 22rpx;
}

.stat-trend.stable {
  color: #7a869a;
  font-size: 22rpx;
}

.alert-item {
  padding: 24rpx;
  border-radius: 20rpx;
  margin-bottom: 20rpx;
  border-left: 8rpx solid #e0e6ed;
}

.alert-item.warning {
  border-left-color: #ffb347;
  background: #fff9f0;
}

.alert-item.error {
  border-left-color: #ff5f5f;
  background: #fff5f5;
}

.alert-item.info {
  border-left-color: #2f7cff;
  background: #f0f7ff;
}

.alert-item:last-child {
  margin-bottom: 0;
}

.alert-title {
  font-size: 28rpx;
  font-weight: 600;
  margin-bottom: 8rpx;
}

.alert-desc {
  font-size: 24rpx;
  color: #6b758a;
  margin-bottom: 8rpx;
}

.alert-time {
  font-size: 22rpx;
  color: #97a0b3;
}

</style>