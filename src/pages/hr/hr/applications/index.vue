<template>
  <view class="applications-page">
    <view class="filter-bar">
      <scroll-view scroll-x class="status-tabs">
        <view
          v-for="item in statusOptions"
          :key="item.value"
          class="status-tab"
          :class="{ active: item.value === activeStatus }"
          @click="activeStatus = item.value"
        >
          {{ item.label }}
        </view>
      </scroll-view>
    </view>

    <scroll-view scroll-y class="list" refresher-enabled :refresher-triggered="refreshing" @refresherrefresh="refresh">
      <view v-if="loading && applications.length === 0" class="hint">加载中...</view>
      <view v-else-if="!loading && applications.length === 0" class="hint">暂无投递</view>

      <view v-for="item in filtered" :key="item.id" class="card" @click="goDetail(item.id)">
        <view class="row">
          <text class="title">{{ item.applicantName }}</text>
          <text class="status" :class="statusClass(item.status)">{{ statusText(item.status) }}</text>
        </view>
        <view class="row meta">
          <text>{{ item.jobTitle }}</text>
          <text>{{ formatTime(item.createdAt) }}</text>
        </view>
      </view>
    </scroll-view>

    <view class="loading" v-if="loading && applications.length > 0">加载中...</view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import dayjs from 'dayjs';
import { getApplications, type ApplicationItem } from '@/services/api/hr';

const applications = ref<ApplicationItem[]>([]);
const loading = ref(false);
const refreshing = ref(false);
const activeStatus = ref<'all' | number>('all');

const statusOptions = [
  { label: '全部', value: 'all' as const },
  { label: '待处理', value: 0 },
  { label: '进行中', value: 1 },
  { label: '已完成', value: 2 },
];

const loadData = async () => {
  loading.value = true;
  try {
    const data = await getApplications(activeStatus.value === 'all' ? undefined : { status: activeStatus.value });
    applications.value = data || [];
  } catch (err) {
    console.error('Failed to load applications:', err);
    uni.showToast({ title: '加载失败', icon: 'none' });
  } finally {
    loading.value = false;
  }
};

const refresh = async () => {
  refreshing.value = true;
  await loadData();
  refreshing.value = false;
  uni.stopPullDownRefresh();
};

const filtered = computed(() => applications.value);

const goDetail = (id: number) => {
  uni.navigateTo({ url: `/pages/hr/hr/applications/detail?applicationId=${id}` });
};

const formatTime = (time?: string) => (time ? dayjs(time).format('MM/DD HH:mm') : '--');

const statusText = (status: number) => {
  const map: Record<number, string> = {
    0: '待处理',
    1: '进行中',
    2: '已完成',
  };
  return map[status] ?? '未知';
};

const statusClass = (status: number) => {
  if (status === 0) return 'pending';
  if (status === 1) return 'processing';
  return 'done';
};

onMounted(() => {
  loadData();
});
</script>

<style scoped lang="scss">
.applications-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #e5f0ff 0%, #f6f7fb 40%, #f6f7fb 100%);
  padding: calc(var(--status-bar-height) + 48rpx) 24rpx 24rpx;
  box-sizing: border-box;
}

.filter-bar {
  background: #fff;
  border-radius: 20rpx;
  padding: 16rpx;
  margin-bottom: 16rpx;
  box-shadow: 0 6rpx 18rpx rgba(0, 34, 90, 0.06);
}

.status-tabs {
  white-space: nowrap;
}

.status-tab {
  display: inline-flex;
  padding: 12rpx 28rpx;
  border-radius: 999rpx;
  background: #f1f2f6;
  color: #5f6b7c;
  margin-right: 12rpx;
  font-size: 24rpx;
}

.status-tab.active {
  background: #2f7cff;
  color: #fff;
}

.list {
  max-height: calc(100vh - 160rpx);
}

.card {
  background: #fff;
  border-radius: 20rpx;
  padding: 20rpx;
  margin-bottom: 16rpx;
  box-shadow: 0 8rpx 20rpx rgba(0, 34, 90, 0.05);
}

.row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title {
  font-size: 30rpx;
  font-weight: 600;
}

.status {
  font-size: 24rpx;
  padding: 6rpx 14rpx;
  border-radius: 12rpx;
}

.status.pending {
  background: #fff3e0;
  color: #fb8c00;
}
.status.processing {
  background: #e3f2fd;
  color: #1e88e5;
}
.status.done {
  background: #e8f5e9;
  color: #43a047;
}

.meta {
  margin-top: 8rpx;
  color: #7a859b;
  font-size: 24rpx;
}

.hint,
.loading {
  text-align: center;
  padding: 32rpx 0;
  color: #8a92a7;
}
</style>
