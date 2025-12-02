<template>
  <view class="jobs-page">
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
      <view class="search-bar">
        <input type="text" v-model="keyword" placeholder="搜索岗位标题" confirm-type="search" />
      </view>
      <button class="publish-btn" @click="goCreateJob">发布岗位</button>
    </view>

    <view class="job-list">
      <view class="job-card" v-for="job in filteredJobs" :key="job.jobId" @click="goJobDetail(job.jobId)">
        <view class="job-card-header">
          <view>
            <view class="job-title">{{ job.title }}</view>
            <view class="job-meta">{{ job.city }} · {{ job.salary }}</view>
          </view>
          <view class="status-tag" :class="job.status">{{ statusText(job.status) }}</view>
        </view>
        <view class="job-stats">
          <view class="stat">
            <text class="label">投递</text>
            <text class="value">{{ job.applied }}</text>
          </view>
          <view class="stat">
            <text class="label">面试中</text>
            <text class="value">{{ job.interviewing }}</text>
          </view>
          <view class="stat">
            <text class="label">Offer</text>
            <text class="value">{{ job.offer }}</text>
          </view>
        </view>
        <view class="job-footer">
          <text class="update-time">更新于 {{ formatTime(job.updatedAt) }}</text>
          <button class="ghost" @click.stop="editJob(job.jobId)">编辑</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import dayjs from 'dayjs';
import { fetchJobSummaries, type JobSummary } from '@/mock/hr';

type FilterStatus = 'all' | JobSummary['status'];

const jobs = ref<JobSummary[]>([]);
const activeStatus = ref<FilterStatus>('all');
const keyword = ref('');

const statusOptions = [
  { label: '全部', value: 'all' },
  { label: '招聘中', value: 'open' },
  { label: '已暂停', value: 'paused' },
  { label: '已关闭', value: 'closed' },
];

const loadJobs = async () => {
  // TODO: GET /api/hr/jobs
  jobs.value = await fetchJobSummaries();
};

const filteredJobs = computed(() => {
  return jobs.value.filter((job) => {
    const matchStatus = activeStatus.value === 'all' || job.status === activeStatus.value;
    const matchKeyword = !keyword.value || job.title.includes(keyword.value.trim());
    return matchStatus && matchKeyword;
  });
});

const goJobDetail = (jobId: number) => {
  uni.navigateTo({ url: `/pages/hr/jobs/detail?jobId=${jobId}` });
};

const goCreateJob = () => {
  uni.navigateTo({ url: '/pages/hr/jobs/edit?mode=create' });
};

const editJob = (jobId: number) => {
  uni.navigateTo({ url: `/pages/hr/jobs/edit?jobId=${jobId}` });
};

const statusText = (status: JobSummary['status']) => {
  const map = {
    open: '招聘中',
    paused: '已暂停',
    closed: '已关闭',
  } as const;
  return map[status];
};

const formatTime = (time: string) => dayjs(time).format('MM月DD日 HH:mm');

onMounted(() => {
  loadJobs();
});
</script>

<style scoped lang="scss">
.jobs-page {
  min-height: 100vh;
  padding: 24rpx;
  background: #f6f7fb;
  box-sizing: border-box;
}

.filter-bar {
  background: #fff;
  border-radius: 24rpx;
  padding: 24rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 10rpx 30rpx rgba(0, 34, 90, 0.08);
}

.status-tabs {
  white-space: nowrap;
  margin-bottom: 16rpx;
}

.status-tab {
  display: inline-flex;
  padding: 12rpx 32rpx;
  border-radius: 999rpx;
  background: #f1f2f6;
  color: #5f6b7c;
  margin-right: 16rpx;
  font-size: 24rpx;
}

.status-tab.active {
  background: #2f7cff;
  color: #fff;
}

.search-bar {
  background: #f5f7fb;
  border-radius: 16rpx;
  padding: 8rpx 16rpx;
  margin-bottom: 16rpx;
}

.publish-btn {
  width: 100%;
  background: #2f7cff;
  color: #fff;
  border-radius: 18rpx;
  height: 80rpx;
  border: none;
}

.job-card {
  background: #fff;
  border-radius: 24rpx;
  padding: 28rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 10rpx 30rpx rgba(0, 34, 90, 0.05);
}

.job-card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.job-title {
  font-size: 32rpx;
  font-weight: 600;
}

.job-meta {
  font-size: 24rpx;
  color: #8b95a7;
  margin-top: 6rpx;
}

.status-tag {
  padding: 10rpx 24rpx;
  border-radius: 999rpx;
  font-size: 24rpx;
  background: #edf2ff;
  color: #2f7cff;
}

.status-tag.paused {
  background: #fff5e5;
  color: #ff9f43;
}

.status-tag.closed {
  background: #f4f4f6;
  color: #9ea6b4;
}

.job-stats {
  display: flex;
  margin: 24rpx 0;
  border-top: 2rpx dashed #f0f1f4;
  padding-top: 24rpx;
}

.stat {
  flex: 1;
  text-align: center;
}

.stat .label {
  font-size: 24rpx;
  color: #99a3b4;
}

.stat .value {
  display: block;
  font-size: 30rpx;
  font-weight: 600;
  margin-top: 8rpx;
}

.job-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.update-time {
  font-size: 22rpx;
  color: #a0a7b7;
}

button.ghost {
  border: 2rpx solid #d6def2;
  background: transparent;
  color: #2f7cff;
  border-radius: 16rpx;
  padding: 12rpx 28rpx;
}
</style>
