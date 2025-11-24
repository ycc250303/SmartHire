<template>
  <view class="page">
    <view class="header-actions">
      <button class="create-btn" @click="handleCreateJob">+ 创建岗位</button>
    </view>
    
    <view class="job-list" v-if="jobs.length > 0">
      <view 
        class="job-card" 
        v-for="job in jobs" 
        :key="job.jobId"
        @click="handleJobClick(job)"
      >
        <view class="job-header">
          <text class="job-title">{{ job.title }}</text>
          <view class="job-status" :class="job.status">
            {{ getStatusText(job.status) }}
          </view>
        </view>
        <text class="job-meta">{{ job.location }} · {{ job.salary }} · {{ job.headcount }}个HC</text>
        <text class="job-description">{{ job.description }}</text>
      </view>
    </view>
    
    <view class="empty-state" v-else-if="hasLoaded && !loading">
      <text class="empty-text">暂无岗位</text>
      <button class="create-btn" @click="handleCreateJob">创建第一个岗位</button>
    </view>
    
    <view class="loading-state" v-if="loading">
      <text class="loading-text">加载中...</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { onLoad, onPullDownRefresh } from '@dcloudio/uni-app';
import { getJobList, type JobPosting } from '@/services/api/hr';

const jobs = ref<JobPosting[]>([]);
const loading = ref(false);
const hasLoaded = ref(false);

const loadJobs = async () => {
  if (loading.value) return;
  loading.value = true;
  try {
    const data = await getJobList();
    jobs.value = Array.isArray(data) ? data : [];
    hasLoaded.value = true;
  } catch (error) {
    console.error('Failed to load jobs:', error);
    uni.showToast({
      title: '加载失败',
      icon: 'none',
    });
  } finally {
    loading.value = false;
    uni.stopPullDownRefresh();
  }
};

const getStatusText = (status: string) => {
  const statusMap: Record<string, string> = {
    draft: '草稿',
    published: '已发布',
    closed: '已关闭',
  };
  return statusMap[status] || status;
};

const handleCreateJob = () => {
  uni.navigateTo({
    url: '/pages/jobs/create',
  });
};

const handleJobClick = (job: JobPosting) => {
  uni.navigateTo({
    url: `/pages/jobs/create?jobId=${job.jobId}`,
  });
};

onLoad(() => {
  loadJobs();
});

onPullDownRefresh(() => {
  loadJobs();
});
</script>

<style lang="scss" scoped>
@use "@/styles/variables.scss" as vars;

.page {
  min-height: 100vh;
  background-color: vars.$bg-color;
  padding: vars.$spacing-md;
}

.header-actions {
  margin-bottom: vars.$spacing-md;
}

.create-btn {
  width: 100%;
  height: 88rpx;
  background-color: vars.$primary-color;
  color: #ffffff;
  border-radius: vars.$border-radius;
  font-size: 32rpx;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
}

.job-list {
  display: flex;
  flex-direction: column;
  gap: vars.$spacing-md;
}

.job-card {
  background-color: vars.$surface-color;
  border-radius: vars.$border-radius;
  padding: vars.$spacing-lg;
}

.job-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: vars.$spacing-sm;
}

.job-title {
  font-size: 36rpx;
  font-weight: 600;
  color: vars.$text-color;
  flex: 1;
}

.job-status {
  padding: 8rpx 16rpx;
  border-radius: vars.$border-radius-sm;
  font-size: 24rpx;
  background-color: vars.$primary-color-soft;
  color: vars.$primary-color;
}

.job-status.published {
  background-color: #e8f5e9;
  color: #4caf50;
}

.job-status.closed {
  background-color: #f5f5f5;
  color: vars.$text-muted;
}

.job-meta {
  font-size: 24rpx;
  color: vars.$text-muted;
  display: block;
  margin-bottom: vars.$spacing-sm;
}

.job-description {
  font-size: 28rpx;
  color: vars.$text-color;
  line-height: 1.6;
  display: block;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 400rpx;
  padding: vars.$spacing-xl;
}

.empty-text {
  font-size: 28rpx;
  color: vars.$text-muted;
  margin-bottom: vars.$spacing-lg;
}

.loading-state {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400rpx;
  padding: vars.$spacing-xl;
}

.loading-text {
  font-size: 28rpx;
  color: vars.$text-muted;
}
</style>




