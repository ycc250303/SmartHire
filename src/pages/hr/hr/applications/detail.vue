<template>
  <view class="application-detail">
    <view class="card">
      <view class="row">
        <text class="title">{{ detail?.jobSeekerName || 'Candidate' }}</text>
        <text class="status" :class="statusClass(detail?.status || 0)">{{ statusText(detail?.status || 0) }}</text>
      </view>
      <view class="meta">Position: {{ detail?.jobTitle || '--' }}</view>
      <view class="meta">Applied: {{ formatTime(detail?.createdAt) }}</view>
      <view class="meta">Match: {{ matchScoreText }}</view>
      <view class="meta" v-if="detail?.resumeId">Resume ID: {{ detail?.resumeId }}</view>
    </view>

    <view class="card" v-if="detail?.matchAnalysis">
      <view class="section-title">Match Analysis</view>
      <view class="meta">{{ detail?.matchAnalysis }}</view>
    </view>

    <view class="card">
      <view class="section-title">Status Update</view>
      <view class="status-buttons">
        <button
          v-for="item in statusOptions"
          :key="item.value"
          class="status-btn"
          :class="{ active: item.value === (detail?.status || 0) }"
          @click="changeStatus(item.value)"
        >
          {{ item.label }}
        </button>
      </view>
    </view>

    <view class="card actions">
      <button class="primary" @click="goChat">Message Candidate</button>
    </view>

    <view class="loading" v-if="loading">Loading..</view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import dayjs from 'dayjs';
import { getApplicationDetail, updateApplicationStatus, type ApplicationItem } from '@/services/api/hr';
import { getConversations } from '@/services/api/message';

const detail = ref<ApplicationItem | null>(null);
const applicationId = ref<number>(0);
const loading = ref(false);

const statusOptions = [
  { label: 'Submitted', value: 0 },
  { label: 'Viewed', value: 1 },
  { label: 'Interview', value: 2 },
  { label: 'Interviewed', value: 3 },
  { label: 'Hired', value: 4 },
  { label: 'Rejected', value: 5 },
  { label: 'Withdrawn', value: 6 },
];

const matchScoreText = computed(() => {
  const score = detail.value?.matchScore;
  if (score === undefined || score === null) return '--';
  const value = Number(score);
  if (Number.isNaN(value)) return '--';
  return `${Math.round(value)}%`;
});

const loadDetail = async () => {
  if (!applicationId.value) return;
  loading.value = true;
  try {
    const data = await getApplicationDetail(applicationId.value);
    detail.value = data;
  } catch (err) {
    console.error('Failed to load application detail:', err);
    uni.showToast({ title: 'Load failed', icon: 'none' });
  } finally {
    loading.value = false;
  }
};

const changeStatus = async (status: number) => {
  if (!applicationId.value) return;
  try {
    await updateApplicationStatus(applicationId.value, status);
    if (detail.value) {
      detail.value.status = status;
    }
    uni.showToast({ title: 'Updated', icon: 'success' });
  } catch (err) {
    console.error('Failed to update status:', err);
    uni.showToast({ title: 'Update failed', icon: 'none' });
  }
};

const goChat = async () => {
  if (!detail.value?.jobSeekerName) {
    uni.showToast({ title: 'Missing candidate info', icon: 'none' });
    return;
  }
  try {
    const conversations = await getConversations();
    const target = conversations.find((c) => c.otherUserName === detail.value?.jobSeekerName);
    if (target) {
      uni.navigateTo({
        url: `/pages/hr/hr/messages/chat?id=${target.id}&userId=${target.otherUserId || ''}&username=${encodeURIComponent(
          target.otherUserName || ''
        )}&applicationId=${applicationId.value}`,
      });
    } else {
      uni.switchTab({ url: '/pages/hr/hr/messages/index' });
    }
  } catch (err) {
    console.error('Failed to find conversation:', err);
    uni.switchTab({ url: '/pages/hr/hr/messages/index' });
  }
};

const formatTime = (time?: string) => (time ? dayjs(time).format('YYYY-MM-DD HH:mm') : '--');

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

const statusClass = (status: number) => {
  if (status <= 1) return 'pending';
  if (status <= 3) return 'processing';
  return 'done';
};

const pages = getCurrentPages();
const current = pages[pages.length - 1] as any;
const params = current?.options || {};
if (params?.applicationId) {
  applicationId.value = Number(params.applicationId);
  loadDetail();
} else {
  uni.showToast({ title: 'Missing application ID', icon: 'none' });
}
</script>

<style scoped lang="scss">
.application-detail {
  min-height: 100vh;
  background: linear-gradient(180deg, #e5f0ff 0%, #f6f7fb 40%, #f6f7fb 100%);
  padding: calc(var(--status-bar-height) + 48rpx) 24rpx 24rpx;
  box-sizing: border-box;
}

.card {
  background: #fff;
  border-radius: 20rpx;
  padding: 20rpx;
  margin-bottom: 16rpx;
  box-shadow: 0 6rpx 18rpx rgba(0, 34, 90, 0.06);
}

.row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title {
  font-size: 32rpx;
  font-weight: 700;
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

.section-title {
  font-size: 28rpx;
  font-weight: 600;
  margin-bottom: 12rpx;
}

.status-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
}

.status-btn {
  flex: 1 1 30%;
  height: 72rpx;
  border-radius: 12rpx;
  border: 1rpx solid #d9deea;
  color: #2f3b52;
  background: #f8f9fb;
}

.status-btn.active {
  border-color: #2f7cff;
  color: #2f7cff;
  background: #e5f0ff;
}

.actions .primary {
  width: 100%;
  height: 84rpx;
  border-radius: 18rpx;
  background: #2f7cff;
  color: #fff;
  border: none;
}

.loading {
  text-align: center;
  color: #8a92a7;
  padding: 24rpx 0;
}
</style>
