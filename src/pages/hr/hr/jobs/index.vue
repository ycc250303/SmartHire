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
      <view class="job-card" v-for="job in filteredJobs" :key="job.id" @click="goJobDetail(job.id)">
        <view class="job-card-header">
          <view>
            <view class="job-title">{{ job.jobTitle }}</view>
            <view class="job-meta">{{ job.city }} · {{ salaryText(job) }}</view>
          </view>
          <view class="status-tag" :class="statusClass(job.status)">{{ statusText(job.status) }}</view>
        </view>
        <view class="job-stats">
          <view class="stat">
            <text class="label">浏览</text>
            <text class="value">{{ job.viewCount ?? 0 }}</text>
          </view>
          <view class="stat">
            <text class="label">申请</text>
            <text class="value">{{ job.applicationCount ?? 0 }}</text>
          </view>
        </view>
        <view class="job-footer">
          <text class="update-time">更新于 {{ formatTime(job.updatedAt) }}</text>
        </view>
        <button class="edit-btn" @click.stop="editJob(job.id)">编辑</button>
      </view>
    </view>

    <view class="empty" v-if="!loading && filteredJobs.length === 0">
      <text>暂无岗位</text>
    </view>
    <view class="loading" v-if="loading">
      <text>加载中...</text>
    </view>
    
    <CustomTabBar />
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import dayjs from 'dayjs';
import { getJobPositionList, type JobPosition } from '@/services/api/hr';
import CustomTabBar from '@/components/common/CustomTabBar.vue';

type FilterStatus = 'all' | number;

const jobs = ref<JobPosition[]>([]);
const activeStatus = ref<FilterStatus>('all');
const keyword = ref('');
const loading = ref(false);

const statusOptions = [
  { label: '全部', value: 'all' as FilterStatus },
  { label: '招聘中', value: 1 as FilterStatus },
  { label: '已暂停', value: 2 as FilterStatus },
  { label: '已下线', value: 0 as FilterStatus },
];

const loadJobs = async () => {
  loading.value = true;
  try {
    const data = await getJobPositionList();
    jobs.value = data || [];
  } catch (error) {
    console.error('Failed to load jobs:', error);
    uni.showToast({ title: '加载失败', icon: 'none' });
  } finally {
    loading.value = false;
  }
};

const filteredJobs = computed(() => {
  return jobs.value.filter((job) => {
    const matchStatus = activeStatus.value === 'all' || job.status === activeStatus.value;
    const matchKeyword = !keyword.value || job.jobTitle.includes(keyword.value.trim());
    return matchStatus && matchKeyword;
  });
});

const goJobDetail = (jobId: number) => {
  uni.navigateTo({ url: `/pages/hr/hr/jobs/detail?jobId=${jobId}` });
};

const goCreateJob = () => {
  uni.navigateTo({ url: '/pages/hr/hr/jobs/edit?mode=create' });
};

const editJob = (jobId: number) => {
  uni.navigateTo({ url: `/pages/hr/hr/jobs/edit?jobId=${jobId}` });
};

const statusText = (status: number) => {
  const map: Record<number, string> = {
    1: '招聘中',
    2: '已暂停',
    0: '已下线',
  };
  return map[status] || '未知';
};

const statusClass = (status: number) => {
  if (status === 1) return 'open';
  if (status === 2) return 'paused';
  return 'closed';
};

const salaryText = (job: JobPosition) => {
  if (job.salaryMin == null && job.salaryMax == null) return '薪资面议';
  const min = job.salaryMin ?? 0;
  const max = job.salaryMax ?? min;
  const months = job.salaryMonths ? `${job.salaryMonths}薪` : '月薪';
  return `${min}-${max}k·${months}`;
};

const formatTime = (time?: string) => (time ? dayjs(time).format('MM月DD日 HH:mm') : '--');

onMounted(() => {
  loadJobs();
});

onShow(() => {
  uni.hideTabBar();
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
  padding-bottom: 120rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 10rpx 30rpx rgba(0, 34, 90, 0.05);
  position: relative;
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
  padding-right: 200rpx;
}

.update-time {
  font-size: 22rpx;
  color: #a0a7b7;
}

.edit-btn {
  position: absolute;
  right: 28rpx;
  bottom: 28rpx;
  background: #2f7cff;
  color: #fff;
  border: none;
  border-radius: 20rpx;
  padding: 0 40rpx;
  height: 72rpx;
  line-height: 72rpx;
  font-size: 26rpx;
  box-shadow: 0 12rpx 24rpx rgba(47, 124, 255, 0.25);
}

.empty,
.loading {
  text-align: center;
  color: #8a92a7;
  padding: 40rpx 0;
}
</style>
