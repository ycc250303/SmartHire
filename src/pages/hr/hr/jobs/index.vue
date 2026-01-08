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

    <view v-if="!loading && loadError" class="error-card">
      <view class="error-title">岗位列表加载失败</view>
      <view class="error-desc">{{ loadError }}</view>
      <view class="error-actions">
        <button class="secondary-btn" @click="goProfile">去完善HR信息</button>
        <button class="primary-btn" @click="loadJobs">重试</button>
      </view>
    </view>

    <view class="job-list">
      <view class="job-card" v-for="job in filteredJobs" :key="job.id" @click="goJobDetail(job.id)">
        <view class="job-card-header">
          <view class="title-area">
            <view class="job-title">{{ job.jobTitle }}</view>
            <view class="job-meta">{{ job.city }} · {{ salaryText(job) }}</view>
          </view>
          <view class="header-actions">
            <view class="status-tag" :class="statusClass(job.status)">{{ statusText(job.status) }}</view>
            <button class="edit-btn" @click.stop="editJob(job.id)">编辑</button>
          </view>
        </view>

        <view class="job-summary">
          <view class="summary-item">
            <text class="summary-label">浏览</text>
            <text class="summary-value">{{ job.viewCount ?? 0 }}</text>
          </view>
          <view class="summary-divider"></view>
          <view class="summary-item">
            <text class="summary-label">申请</text>
            <text class="summary-value">{{ job.applicationCount ?? 0 }}</text>
          </view>
        </view>

        <view class="job-footer">
          <text class="update-time">更新于 {{ formatTime(job.updatedAt) }}</text>
          <text class="hint">点击查看详情</text>
        </view>
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
import { getHrInfo, getJobPositionList, type JobPosition } from '@/services/api/hr';
import CustomTabBar from '@/components/common/CustomTabBar.vue';

type FilterStatus = 'all' | number;

const jobs = ref<JobPosition[]>([]);
const activeStatus = ref<FilterStatus>('all');
const keyword = ref('');
const loading = ref(false);
const loadError = ref<string>('');

const statusOptions = [
  { label: '全部', value: 'all' as FilterStatus },
  { label: '招聘中', value: 1 as FilterStatus },
  { label: '已暂停', value: 2 as FilterStatus },
  { label: '已下线', value: 0 as FilterStatus },
];

const loadJobs = async () => {
  loading.value = true;
  loadError.value = '';
  try {
    // 先确保 HR 信息存在，避免后端因 companyId/hrId 为空直接报“系统内部错误”
    await getHrInfo();
    const data = await getJobPositionList();
    const normalizeCount = (value: unknown) => {
      const parsed = Number(value);
      return Number.isFinite(parsed) ? parsed : 0;
    };
    jobs.value = (data || []).map((job) => {
      const raw = job as JobPosition & Record<string, unknown>;
      return {
        ...job,
        viewCount: normalizeCount(raw.viewCount ?? raw.view_count ?? raw.views),
        applicationCount: normalizeCount(
          raw.applicationCount ?? raw.application_count ?? raw.applyCount ?? raw.application_num
        ),
      };
    });
  } catch (error) {
    console.error('Failed to load jobs:', error);
    const message = error instanceof Error ? error.message : String(error);
    loadError.value =
      message.includes('系统内部错误') || message.includes('Internal server error')
        ? '系统内部错误（通常是HR信息/公司信息未完善导致），请先到“我的-个人信息”完善后重试。'
        : message || '加载失败，请重试';
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

const goJobDetail = (jobId?: number) => {
  if (jobId === undefined || jobId === null) {
    uni.showToast({ title: '缺少岗位ID', icon: 'none' });
    return;
  }
  uni.navigateTo({ url: `/pages/hr/hr/jobs/detail?jobId=${jobId}` });
};

const goCreateJob = () => {
  uni.navigateTo({ url: '/pages/hr/hr/jobs/edit?mode=create' });
};

const editJob = (jobId: number) => {
  uni.navigateTo({ url: `/pages/hr/hr/jobs/edit?jobId=${jobId}` });
};

const goProfile = () => {
  uni.navigateTo({ url: '/pages/hr/hr/profile/index' });
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
  loadJobs();
});
</script>

<style scoped lang="scss">
.jobs-page {
  min-height: 100vh;
  padding: calc(var(--status-bar-height) + 64rpx) 24rpx 24rpx;
  background: linear-gradient(180deg, #e5f0ff 0%, #f6f7fb 40%, #f6f7fb 100%);
  box-sizing: border-box;
}

.filter-bar {
  background: #fff;
  border-radius: 24rpx;
  padding: 24rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 10rpx 30rpx rgba(0, 34, 90, 0.08);
}

.error-card {
  background: #fff;
  border-radius: 24rpx;
  padding: 28rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 10rpx 30rpx rgba(0, 34, 90, 0.05);
}

.error-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #1f2a3a;
}

.error-desc {
  margin-top: 12rpx;
  font-size: 26rpx;
  color: #6f788f;
  line-height: 1.5;
}

.error-actions {
  display: flex;
  gap: 16rpx;
  margin-top: 18rpx;
}

.secondary-btn,
.primary-btn {
  flex: 1;
  height: 80rpx;
  border-radius: 18rpx;
  font-size: 26rpx;
  border: none;
}

.secondary-btn {
  background: #f1f2f6;
  color: #2b3445;
}

.primary-btn {
  background: #2f7cff;
  color: #fff;
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
  padding: 24rpx;
  margin-bottom: 18rpx;
  box-shadow: 0 10rpx 26rpx rgba(0, 34, 90, 0.06);
  position: relative;
  overflow: hidden;
}

.job-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 6rpx;
  background: linear-gradient(90deg, rgba(47, 124, 255, 0.6), rgba(75, 163, 255, 0.2));
}

.job-card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 18rpx;
}

.title-area {
  min-width: 0;
  flex: 1;
}

.job-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #1e2532;
}

.job-meta {
  font-size: 24rpx;
  color: #8b95a7;
  margin-top: 6rpx;
}

.header-actions {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 12rpx;
  flex-shrink: 0;
}

.status-tag {
  padding: 8rpx 22rpx;
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

.edit-btn {
  background: #f1f4ff;
  color: #2f7cff;
  border: none;
  border-radius: 999rpx;
  padding: 0 26rpx;
  height: 56rpx;
  line-height: 56rpx;
  font-size: 24rpx;
}

.job-summary {
  display: flex;
  align-items: center;
  justify-content: space-around;
  margin-top: 22rpx;
  padding: 18rpx 0;
  background: #f7f9ff;
  border-radius: 16rpx;
}

.summary-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.summary-label {
  font-size: 22rpx;
  color: #9aa3b2;
}

.summary-value {
  margin-top: 6rpx;
  font-size: 30rpx;
  font-weight: 700;
  color: #1e2532;
}

.summary-divider {
  width: 2rpx;
  height: 48rpx;
  background: #e5e9f2;
}

.job-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 18rpx;
}

.update-time {
  font-size: 22rpx;
  color: #a0a7b7;
}

.hint {
  font-size: 22rpx;
  color: #c0c6d4;
}

.empty,
.loading {
  text-align: center;
  color: #8a92a7;
  padding: 40rpx 0;
}
</style>
