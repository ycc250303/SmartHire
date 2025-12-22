<template>
  <view class="job-detail" v-if="job">
    <view class="card">
      <view class="header">
        <view>
          <view class="job-title">{{ job.jobTitle }}</view>
          <view class="job-meta">{{ job.city }} · {{ salaryText(job) }}</view>
        </view>
        <view class="status-tag" :class="statusClass(job.status)">{{ statusText(job.status) }}</view>
      </view>
      <view class="info-row">岗位类别：{{ job.jobCategory || '未填写' }}</view>
      <view class="info-row">部门：{{ job.department || '未填写' }}</view>
      <view class="info-row">学历要求：{{ educationText(job.educationRequired) }}</view>
      <view class="info-row">工作类型：{{ jobTypeText(job.jobType) }}</view>
      <view class="info-row">技能：{{ skillText(job.skills) }}</view>
      <view class="job-actions">
        <button class="ghost" @click="editJob">编辑</button>
        <button class="primary" @click="toggleStatus">{{ toggleText }}</button>
        <button class="outline" v-if="job.status !== 0" @click="setOffline">下线</button>
      </view>
    </view>

    <view class="card" v-if="job.description">
      <view class="section-title">岗位描述</view>
      <view class="text">{{ job.description }}</view>
    </view>
    <view class="card" v-if="job.responsibilities">
      <view class="section-title">岗位职责</view>
      <view class="text">{{ job.responsibilities }}</view>
    </view>
    <view class="card" v-if="job.requirements">
      <view class="section-title">任职要求</view>
      <view class="text">{{ job.requirements }}</view>
    </view>

    <view class="loading" v-if="loading">
      <text>加载中...</text>
    </view>
  </view>
  <view class="loading" v-else-if="loading">
    <text>加载中...</text>
  </view>
  <view class="error" v-else>
    <text>{{ errorMessage || '未找到岗位信息' }}</text>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import {
  getJobPositionById,
  updateJobPositionStatus,
  offlineJobPosition,
  type JobPosition,
} from '@/services/api/hr';

const job = ref<JobPosition | null>(null);
const loading = ref(false);
const errorMessage = ref('');
const jobId = ref<number | null>(null);

const loadDetail = async (id: number) => {
  loading.value = true;
  errorMessage.value = '';
  try {
    const data = await getJobPositionById(id);
    if (!data) {
      throw new Error('岗位不存在');
    }
    job.value = data;
  } catch (error) {
    console.error('Failed to load job detail:', error);
    job.value = null;
    errorMessage.value = '加载失败';
    uni.showToast({ title: '加载失败', icon: 'none' });
  } finally {
    loading.value = false;
  }
};

const toggleText = computed(() => {
  if (!job.value) return '更新状态';
  if (job.value.status === 1) return '暂停招聘';
  return '开启招聘';
});

const toggleStatus = async () => {
  if (!job.value || !jobId.value) return;
  const nextStatus = job.value.status === 1 ? 2 : 1;
  try {
    await updateJobPositionStatus(jobId.value, nextStatus);
    uni.showToast({ title: '状态已更新', icon: 'success' });
    await loadDetail(jobId.value);
  } catch (error) {
    console.error('Failed to update status:', error);
    uni.showToast({ title: '更新失败', icon: 'none' });
  }
};

const setOffline = async () => {
  if (!jobId.value) return;
  try {
    await offlineJobPosition(jobId.value);
    uni.showToast({ title: '已下线', icon: 'success' });
    await loadDetail(jobId.value);
  } catch (error) {
    console.error('Failed to offline job:', error);
    uni.showToast({ title: '操作失败', icon: 'none' });
  }
};

const editJob = () => {
  if (!job.value) return;
  uni.navigateTo({ url: `/pages/hr/hr/jobs/edit?jobId=${job.value.id}` });
};

const statusText = (status: number) => {
  const map: Record<number, string> = { 0: '已下线', 1: '招聘中', 2: '已暂停' };
  return map[status] ?? '未知';
};

const statusClass = (status: number) => {
  if (status === 1) return 'open';
  if (status === 2) return 'paused';
  return 'closed';
};

const jobTypeText = (type?: number) => {
  const map: Record<number, string> = { 0: '全职', 1: '实习' };
  if (type === undefined || type === null) return '未填写';
  return map[type] ?? '未填写';
};

const educationText = (edu?: number) => {
  const map: Record<number, string> = {
    0: '高中及以上',
    1: '大专',
    2: '本科',
    3: '硕士',
    4: '博士',
    5: '不限',
  };
  if (edu === undefined || edu === null) return '未填写';
  return map[edu] || '未填写';
};

const salaryText = (jobInfo: JobPosition) => {
  if (jobInfo.salaryMin == null && jobInfo.salaryMax == null) return '薪资面议';
  const min = jobInfo.salaryMin ?? 0;
  const max = jobInfo.salaryMax ?? min;
  const months = jobInfo.salaryMonths ? `${jobInfo.salaryMonths}薪` : '月薪';
  return `${min}-${max}k·${months}`;
};

const skillText = (skills?: JobPosition['skills']) => {
  if (!skills || skills.length === 0) return 'Not set';
  return skills
    .map((skill) => (typeof skill === 'string' ? skill : skill.skillName))
    .filter((item) => item)
    .join(' / ');
};

onLoad((options) => {
  const rawId = options?.jobId;
  if (rawId === undefined || rawId === null || rawId === 'undefined') {
    uni.showToast({ title: '缺少岗位ID', icon: 'none' });
    return;
  }
  const parsed = Number(rawId);
  if (Number.isNaN(parsed)) {
    uni.showToast({ title: '岗位ID无效', icon: 'none' });
    return;
  }
  jobId.value = parsed;
  loadDetail(parsed);
});
</script>

<style scoped lang="scss">
.job-detail {
  padding: calc(var(--status-bar-height) + 48rpx) 24rpx 24rpx;
  background: #f6f7fb;
  min-height: 100vh;
  box-sizing: border-box;
}

.card {
  background: #fff;
  border-radius: 24rpx;
  padding: 28rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 10rpx 32rpx rgba(0, 34, 90, 0.08);
}

.job-actions {
  display: flex;
  gap: 16rpx;
  margin-top: 22rpx;
}

.job-actions button {
  flex: 1;
  height: 84rpx;
  line-height: 84rpx;
  border-radius: 18rpx;
  font-size: 30rpx;
  font-weight: 600;
  padding: 0 12rpx;
  box-sizing: border-box;
}

.job-actions button:active {
  transform: translateY(1rpx);
  opacity: 0.92;
}

.job-actions button[disabled] {
  opacity: 0.55;
}

button.ghost {
  background: #f3f6ff;
  color: #2f7cff;
  border: 2rpx solid rgba(47, 124, 255, 0.18);
}

button.primary {
  background: linear-gradient(135deg, #2f7cff 0%, #4ba3ff 100%);
  color: #fff;
  border: none;
  box-shadow: 0 12rpx 26rpx rgba(47, 124, 255, 0.26);
}

button.outline {
  background: #fff;
  color: #ff7a00;
  border: 2rpx solid rgba(255, 122, 0, 0.55);
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.job-title {
  font-size: 34rpx;
  font-weight: 600;
}

.job-meta {
  font-size: 26rpx;
  color: #8a92a7;
  margin-top: 6rpx;
}

.status-tag {
  padding: 10rpx 24rpx;
  border-radius: 999rpx;
  font-size: 24rpx;
}

.status-tag.open {
  background: #e8f3ff;
  color: #1f6bff;
}

.status-tag.paused {
  background: #fff1db;
  color: #ff8f2a;
}

.status-tag.closed {
  background: #f0f0f0;
  color: #8a92a7;
}

.info-row {
  margin-top: 10rpx;
  color: #6f768a;
}

.job-actions {
  display: flex;
  gap: 16rpx;
  margin-top: 20rpx;
}

button.primary,
button.ghost,
button.outline {
  flex: 1;
  height: 72rpx;
  border-radius: 18rpx;
  border: none;
}

button.primary {
  background: #2f7cff;
  color: #fff;
}

button.ghost {
  background: #eef2ff;
  color: #2f7cff;
}

button.outline {
  border: 2rpx solid #ff9f43;
  background: transparent;
  color: #ff9f43;
}

.section-title {
  font-size: 30rpx;
  font-weight: 600;
  margin-bottom: 12rpx;
}

.text {
  color: #4f5566;
  line-height: 1.6;
}

.loading {
  text-align: center;
  color: #8a92a7;
  padding: 40rpx 0;
}

.error {
  text-align: center;
  color: #e64a19;
  padding: 40rpx 0;
}
</style>
