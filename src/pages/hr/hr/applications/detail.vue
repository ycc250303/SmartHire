<template>
  <view class="application-detail">
    <view class="card">
      <view class="row">
        <text class="title">{{ detail?.applicantName || '候选人' }}</text>
        <text class="status" :class="statusClass(detail?.status || 0)">{{ statusText(detail?.status || 0) }}</text>
      </view>
      <view class="meta">岗位：{{ detail?.jobTitle || '--' }}</view>
      <view class="meta">投递时间：{{ formatTime(detail?.createdAt) }}</view>
      <view class="meta">联系方式：{{ detail?.contactPhone || '--' }} / {{ detail?.contactEmail || '--' }}</view>
      <view class="meta" v-if="detail?.resumeUrl">
        <text class="link" @click="openResume(detail?.resumeUrl)">查看简历</text>
      </view>
    </view>

    <view class="card">
      <view class="section-title">状态更新</view>
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
      <button class="primary" @click="goChat">与候选人沟通</button>
    </view>

    <view class="loading" v-if="loading">加载中...</view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import dayjs from 'dayjs';
import { getApplicationDetail, updateApplicationStatus, type ApplicationDetail } from '@/services/api/hr';
import { getConversations } from '@/services/api/message';

const detail = ref<ApplicationDetail | null>(null);
const applicationId = ref<number>(0);
const loading = ref(false);

const statusOptions = [
  { label: '待处理', value: 0 },
  { label: '进行中', value: 1 },
  { label: '已完成', value: 2 },
];

const loadDetail = async () => {
  if (!applicationId.value) return;
  loading.value = true;
  try {
    const data = await getApplicationDetail(applicationId.value);
    detail.value = data;
  } catch (err) {
    console.error('Failed to load application detail:', err);
    uni.showToast({ title: '加载失败', icon: 'none' });
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
    uni.showToast({ title: '更新成功', icon: 'success' });
  } catch (err) {
    console.error('Failed to update status:', err);
    uni.showToast({ title: '更新失败', icon: 'none' });
  }
};

const openResume = (url?: string) => {
  if (!url) return;
  uni.setClipboardData({
    data: url,
    success: () => {
      uni.showToast({ title: '已复制简历链接', icon: 'none' });
    },
  });
};

const goChat = async () => {
  if (!detail.value?.applicantName) {
    uni.showToast({ title: '缺少候选人信息', icon: 'none' });
    return;
  }
  // 如果已有会话，跳转现有；否则回到消息列表
  try {
    const conversations = await getConversations();
    const target = conversations.find((c) => c.otherUserName === detail.value?.applicantName);
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
  const pages = getCurrentPages();
  const current = pages[pages.length - 1] as any;
  const params = current?.options || {};
  if (params?.applicationId) {
    applicationId.value = Number(params.applicationId);
    loadDetail();
  } else {
    uni.showToast({ title: '缺少投递ID', icon: 'none' });
  }
});
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
  gap: 12rpx;
}

.status-btn {
  flex: 1;
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

.link {
  color: #2f7cff;
}

.loading {
  text-align: center;
  color: #8a92a7;
  padding: 24rpx 0;
}
</style>
