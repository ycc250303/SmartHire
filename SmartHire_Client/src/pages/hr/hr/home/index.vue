<template>
  <view class="home-page">
    <view class="hero">
      <view class="hero-top">
        <view class="welcome-title">SmartHire+</view>
        <view class="avatar" @click="goProfile">
          <image :src="avatarImg" mode="aspectFill" />
        </view>
      </view>
      <view class="hero-sub">欢迎回来，查看今天的待办</view>
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

    <view class="card recommend-card">
      <view class="recommend-header">
        <view class="section-title">推荐求职者</view>
        <view class="action-link" @click="refreshSeekerCards">刷新</view>
      </view>

      <view v-if="seekerLoading" class="hint">加载中...</view>
      <view v-else-if="seekerError" class="hint">{{ seekerError }}</view>
      <view v-else class="seeker-list">
        <view v-if="seekerCards.length === 0" class="hint">暂无推荐</view>
        <SeekerRecommendCard
          v-for="card in seekerCards"
          :key="card.userId ?? card.id ?? card.username"
          :seeker="card"
          @click="openSeekerDetail"
        />
      </view>
    </view>

    <CustomTabBar />
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import { fetchDashboardData, type DashboardTodoItem, type RecruitStatistic } from '@/mock/hr';
import avatarImg from '@/static/user-avatar.png';
import CustomTabBar from '@/components/common/CustomTabBar.vue';
import { getPublicSeekerCards, type PublicSeekerCard } from '@/services/api/hr';
import SeekerRecommendCard from '@/pages/hr/hr/seeker/components/SeekerRecommendCard.vue';

const todos = ref<DashboardTodoItem[]>([]);
const stats = ref<RecruitStatistic[]>([]);

const seekerCards = ref<PublicSeekerCard[]>([]);
const seekerLoading = ref(false);
const seekerError = ref('');

const loadDashboard = async () => {
  // TODO: 替换为真实接口 GET /api/hr/dashboard
  const data = await fetchDashboardData();
  todos.value = data.todos;
  stats.value = data.stats;
};

const refreshSeekerCards = async () => {
  seekerLoading.value = true;
  seekerError.value = '';
  try {
    const data = await getPublicSeekerCards();
    seekerCards.value = Array.isArray(data) ? data : [];
  } catch (err) {
    console.error('Failed to load public seeker cards:', err);
    seekerError.value = '推荐加载失败';
  } finally {
    seekerLoading.value = false;
  }
};

const goProfile = () => {
  uni.navigateTo({ url: '/pages/hr/hr/profile/index' });
};

const parseQuery = (queryString?: string) => {
  if (!queryString) return {} as Record<string, string>;
  return queryString.split('&').reduce((acc, pair) => {
    const [key, value] = pair.split('=');
    if (key) {
      acc[key] = decodeURIComponent(value || '');
    }
    return acc;
  }, {} as Record<string, string>);
};

const handleTodoClick = (route?: string) => {
  if (!route) return;
  const [path, query] = route.split('?');
  if (path === '/pages/hr/hr/messages/index') {
    const params = parseQuery(query);
    if (params.tab) {
      uni.setStorageSync('hr_messages_tab', params.tab);
    }
    uni.switchTab({ url: path });
    return;
  }
  if (path === '/pages/hr/hr/home/index' || path === '/pages/hr/hr/jobs/index') {
    uni.switchTab({ url: path });
    return;
  }
  uni.navigateTo({ url: route });
};

const openSeekerDetail = (card: PublicSeekerCard) => {
  const userId = (card.userId ?? card.id) as number | undefined;
  if (!userId || Number.isNaN(Number(userId))) {
    uni.showToast({ title: '缺少用户ID', icon: 'none' });
    return;
  }
  uni.navigateTo({
    url: `/pages/hr/hr/seeker/detail?userId=${encodeURIComponent(userId)}&username=${encodeURIComponent(card.username || '')}`,
  });
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
  refreshSeekerCards();
});

onShow(() => {
  uni.hideTabBar({ fail: () => {} });
  refreshSeekerCards();
});
</script>

<style scoped lang="scss">
.home-page {
  min-height: 100vh;
  padding: 0 32rpx 32rpx;
  background: linear-gradient(180deg, #e5f0ff 0%, #f6f7fb 40%, #f6f7fb 100%);
  box-sizing: border-box;
}

.hero {
  padding-top: calc(var(--status-bar-height) + 64rpx);
  padding-bottom: 32rpx;
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.hero-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.welcome-title {
  font-size: 38rpx;
  font-weight: 700;
  color: #0b1c33;
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

.hero-sub {
  font-size: 26rpx;
  color: #2f4b76;
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

.recommend-header {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
}

.action-link {
  font-size: 24rpx;
  color: #2f7cff;
}

.hint {
  color: #8a92a7;
  font-size: 24rpx;
  padding: 10rpx 0;
}

.seeker-list {
  padding-top: 6rpx;
}

</style>
