<template>
  <view class="page">
    <view class="header">
      <text class="subtitle">HR 看板</text>
      <view class="title-row">
        <text class="title">优先处理列表</text>
        <view class="badge" v-if="summary.awaitingInterview > 0">
          {{ summary.awaitingInterview }}人待约面
        </view>
      </view>
    </view>
    
    <view class="summary-cards">
      <view class="summary-card">
        <text class="summary-value">{{ summary.newApplications }}</text>
        <text class="summary-label">新投递</text>
      </view>
      <view class="summary-card">
        <text class="summary-value">{{ summary.highMatch }}</text>
        <text class="summary-label">高匹配</text>
      </view>
      <view class="summary-card">
        <text class="summary-value">{{ summary.awaitingFeedback }}</text>
        <text class="summary-label">待反馈</text>
      </view>
    </view>
    
    <view class="candidate-list" v-if="candidates.length > 0">
      <CandidateCard
        v-for="candidate in candidates"
        :key="candidate.candidateId"
        :candidate="candidate"
        @click="handleCandidateClick"
      />
    </view>
    
    <view class="empty-state" v-else-if="!loading">
      <text class="empty-text">暂无候选人</text>
    </view>
    
    <view class="loading-state" v-if="loading">
      <text class="loading-text">加载中...</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { getPriorityList, type CandidateListItem } from '@/services/api/hr';
import CandidateCard from '@/components/common/CandidateCard.vue';

const candidates = ref<CandidateListItem[]>([]);
const summary = ref({
  newApplications: 0,
  highMatch: 0,
  awaitingFeedback: 0,
  awaitingInterview: 0,
});
const loading = ref(false);

const loadData = async () => {
  loading.value = true;
  try {
    const response = await getPriorityList();
    candidates.value = response.candidates;
    summary.value = response.summary;
  } catch (error) {
    console.error('Failed to load priority list:', error);
    uni.showToast({
      title: '加载失败',
      icon: 'none',
    });
  } finally {
    loading.value = false;
  }
};

const handleCandidateClick = (candidate: CandidateListItem) => {
  uni.navigateTo({
    url: `/pages/candidates/detail?candidateId=${candidate.candidateId}`,
  });
};

onLoad(() => {
  loadData();
});
</script>

<style lang="scss" scoped>
@use "@/styles/variables.scss" as vars;

.page {
  min-height: 100vh;
  background-color: vars.$bg-color;
  padding: vars.$spacing-md;
}

.header {
  margin-bottom: vars.$spacing-lg;
}

.subtitle {
  font-size: 24rpx;
  color: vars.$text-muted;
  display: block;
  margin-bottom: 8rpx;
}

.title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.title {
  font-size: 48rpx;
  font-weight: 600;
  color: vars.$text-color;
}

.badge {
  padding: 8rpx 16rpx;
  border-radius: 20rpx;
  background-color: vars.$primary-color-soft;
  color: vars.$primary-color;
  font-size: 24rpx;
}

.summary-cards {
  display: flex;
  gap: vars.$spacing-md;
  margin-bottom: vars.$spacing-lg;
}

.summary-card {
  flex: 1;
  background-color: vars.$primary-color-soft;
  border-radius: vars.$border-radius;
  padding: vars.$spacing-md;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.summary-value {
  font-size: 48rpx;
  font-weight: 600;
  color: vars.$primary-color;
  margin-bottom: 8rpx;
}

.summary-label {
  font-size: 24rpx;
  color: vars.$text-muted;
}

.candidate-list {
  display: flex;
  flex-direction: column;
}

.empty-state,
.loading-state {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400rpx;
  padding: vars.$spacing-xl;
}

.empty-text,
.loading-text {
  font-size: 28rpx;
  color: vars.$text-muted;
}
</style>




