<template>
  <view class="page">
    <view class="card" v-if="detail">
      <view class="header">
        <text class="name">{{ detail.jobSeekerName || 'Candidate' }}</text>
        <text class="status">{{ statusText(detail.status) }}</text>
      </view>
      <view class="meta">Position: {{ detail.jobTitle || '--' }}</view>
      <view class="meta">Match: {{ matchScoreText }}</view>
      <view class="meta">Applied: {{ formatTime(detail.createdAt) }}</view>
    </view>

    <view class="card" v-if="detail?.matchAnalysis">
      <view class="section-title">Match Analysis</view>
      <text class="analysis-text">{{ detail.matchAnalysis }}</text>
    </view>

    <view class="loading-state" v-if="loading">
      <text class="loading-text">Loading..</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { getApplicationDetail, type ApplicationItem } from '@/services/api/hr';

const detail = ref<ApplicationItem | null>(null);
const loading = ref(false);

const matchScoreText = computed(() => {
  const score = detail.value?.matchScore;
  if (score === undefined || score === null) return '--';
  const value = Number(score);
  if (Number.isNaN(value)) return '--';
  return `${Math.round(value)}%`;
});

const loadDetail = async (applicationId: number) => {
  loading.value = true;
  try {
    const data = await getApplicationDetail(applicationId);
    detail.value = data || null;
  } catch (error) {
    console.error('Failed to load application detail:', error);
    uni.showToast({ title: 'Load failed', icon: 'none' });
  } finally {
    loading.value = false;
  }
};

const formatTime = (time?: string) => (time ? time : '--');

const statusText = (status: number) => {
  const map: Record<number, string> = {
    0: 'Submitted',
    1: 'Viewed',
    2: 'Interview',
    3: 'Interviewed',
    4: 'Hired',
    5: 'Rejected',
    6: 'Withdrawn',
  };
  return map[status] ?? 'Unknown';
};

onLoad((options) => {
  const rawId = options?.candidateId;
  const parsed = Number(rawId);
  if (!rawId || Number.isNaN(parsed)) {
    uni.showToast({ title: 'Missing application ID', icon: 'none' });
    return;
  }
  loadDetail(parsed);
});
</script>

<style lang="scss" scoped>
@use "@/styles/variables.scss" as vars;

.page {
  min-height: 100vh;
  background-color: vars.$bg-color;
  padding: vars.$spacing-md;
}

.card {
  background-color: vars.$surface-color;
  border-radius: vars.$border-radius;
  padding: vars.$spacing-lg;
  margin-bottom: vars.$spacing-md;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: vars.$spacing-sm;
}

.name {
  font-size: 32rpx;
  font-weight: 600;
  color: vars.$text-color;
}

.status {
  font-size: 24rpx;
  color: vars.$primary-color;
}

.meta {
  font-size: 26rpx;
  color: vars.$text-muted;
  margin-top: 8rpx;
}

.section-title {
  font-size: 28rpx;
  font-weight: 600;
  margin-bottom: vars.$spacing-sm;
  color: vars.$text-color;
}

.analysis-text {
  font-size: 26rpx;
  color: vars.$text-color;
  line-height: 1.6;
}

.loading-state {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 200rpx;
}

.loading-text {
  font-size: 28rpx;
  color: vars.$text-muted;
}
</style>
