<template>
  <view class="home-page">
    <view class="top-bar">
      <view class="welcome-block">
        <text class="welcome-title">{{ companyName }} · SmartHire+</text>
        <text class="welcome-desc">今日招聘概览</text>
      </view>
      <view class="avatar" @click="goProfile">
        <image src="https://cdn.jsdelivr.net/gh/placeholderuser/assets/hr-avatar.png" mode="aspectFill" />
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
      <view class="section-title">招聘进展概览</view>
      <view class="stats-grid">
        <view class="stat-item" v-for="stat in stats" :key="stat.id">
          <text class="stat-label">{{ stat.label }}</text>
          <text class="stat-value">{{ stat.value }}{{ stat.unit }}</text>
          <text class="stat-trend" :class="stat.trend">{{ trendText(stat.trend) }}</text>
        </view>
      </view>
    </view>

    <view class="card insights-card">
      <view class="section-title">智能洞察</view>
      <view class="insight-list">
        <view
          class="insight-item"
          v-for="insight in insights"
          :key="insight.id"
          :class="insight.severity"
          @click="handleTodoClick(insight.route)"
        >
          <view class="insight-title">{{ insight.title }}</view>
          <view class="insight-desc">{{ insight.desc }}</view>
          <view class="insight-action" v-if="insight.actionText">{{ insight.actionText }}</view>
        </view>
      </view>
    </view>

    <view class="card quick-card">
      <view class="section-title">快捷操作</view>
      <view class="quick-actions">
        <button class="primary" @click="goCreateJob">发布新岗位</button>
        <button class="secondary" @click="goAnalytics">查看数据分析</button>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { fetchDashboardData, type DashboardTodoItem, type RecruitStatistic, type InsightCardItem } from '@/mock/hr';
import { useHrStore } from '@/store/hr';

const hrStore = useHrStore();
const companyName = computed(() => hrStore.companyName);

const todos = ref<DashboardTodoItem[]>([]);
const stats = ref<RecruitStatistic[]>([]);
const insights = ref<InsightCardItem[]>([]);

const loadDashboard = async () => {
  // TODO: 替换为真实接口 GET /api/hr/dashboard
  const data = await fetchDashboardData();
  todos.value = data.todos;
  stats.value = data.stats;
  insights.value = data.insights;
};

const goProfile = () => {
  uni.navigateTo({ url: '/pages/hr/profile/index' });
};

const goCreateJob = () => {
  uni.navigateTo({ url: '/pages/hr/jobs/edit?mode=create' });
};

const goAnalytics = () => {
  uni.navigateTo({ url: '/pages/hr/analytics/index' });
};

const parseQuery = (queryString?: string) => {
  if (!queryString) return {} as Record<string, string>;
  return queryString.split('&').reduce((acc, pair) => {
    const [key, value] = pair.split('=');
    acc[key] = decodeURIComponent(value || '');
    return acc;
  }, {} as Record<string, string>);
};

const handleTodoClick = (route?: string) => {
  if (!route) return;
  const [path, query] = route.split('?');
  if (path === '/pages/hr/messages/index') {
    const params = parseQuery(query);
    if (params.tab) {
      uni.setStorageSync('hr_messages_tab', params.tab);
    }
    uni.switchTab({ url: path });
    return;
  }
  if (path === '/pages/hr/home/index' || path === '/pages/hr/jobs/index') {
    uni.switchTab({ url: path });
    return;
  }
  uni.navigateTo({ url: route });
};

const trendText = (trend: RecruitStatistic['trend']) => {
  switch (trend) {
    case 'up':
      return '较上周 ↑';
    case 'down':
      return '较上周 ↓';
    default:
      return '持平';
  }
};

onMounted(() => {
  loadDashboard();
});
</script>

<style scoped lang="scss">
.home-page {
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

.welcome-title {
  font-size: 34rpx;
  font-weight: 600;
}

.welcome-desc {
  font-size: 26rpx;
  color: #8a94a6;
}

.avatar {
  width: 88rpx;
  height: 88rpx;
  border-radius: 50%;
  background: #eef2ff;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
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
}

.todo-count {
  font-size: 32rpx;
  font-weight: 700;
  color: #2f7cff;
}

.stats-grid {
  display: flex;
  gap: 24rpx;
}

.stat-item {
  flex: 1;
  background: #f8faff;
  border-radius: 20rpx;
  padding: 24rpx;
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
}

.stat-trend.down {
  color: #ff5f5f;
}

.insight-item {
  padding: 24rpx;
  border-radius: 20rpx;
  margin-bottom: 20rpx;
  background: #f5f6fb;
}

.insight-item.warning {
  border: 2rpx solid #ffb347;
}

.insight-item.danger {
  border: 2rpx solid #ff5f5f;
}

.insight-title {
  font-size: 28rpx;
  font-weight: 600;
  margin-bottom: 10rpx;
}

.insight-desc {
  font-size: 24rpx;
  color: #6b758a;
}

.insight-action {
  margin-top: 8rpx;
  color: #2f7cff;
  font-size: 24rpx;
}

.quick-actions {
  display: flex;
  gap: 20rpx;
}

button.primary {
  flex: 1;
  background: #2f7cff;
  color: #fff;
  border: none;
  border-radius: 20rpx;
  height: 88rpx;
}

button.secondary {
  flex: 1;
  background: #e5edff;
  color: #2f7cff;
  border: none;
  border-radius: 20rpx;
  height: 88rpx;
}
</style>
