<template>
  <view class="candidate-detail" v-if="candidate">
    <view class="card">
      <view class="header">
        <view class="name">{{ candidate.username }}</view>
        <view class="score">求职状态：{{ jobStatusText(candidate.jobStatus) }}</view>
      </view>
      <view class="meta">毕业年份：{{ candidate.graduationYear || '未填写' }}</view>
      <view class="meta">年龄：{{ candidate.age ?? '未填写' }}</view>
      <view class="meta">最高学历：{{ candidate.highestEducation || '未填写' }}</view>
      <view class="meta">专业：{{ candidate.major || '未填写' }}</view>
    </view>

    <view class="card actions">
      <button class="primary" @click="showToast('已添加到面试名单')">加入面试</button>
      <button class="secondary" @click="showToast('已标记为重点跟进')">标记重点</button>
      <button class="outline" @click="showToast('已记录备注')">添加备注</button>
    </view>
    <view class="loading" v-if="loading">
      <text>加载中...</text>
    </view>
  </view>
  <view class="loading" v-else>
    <text>加载中...</text>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { getSeekerCards, type SeekerCard } from '@/services/api/hr';

const candidate = ref<SeekerCard | null>(null);
const loading = ref(false);

const loadCandidate = async (userId?: number) => {
  loading.value = true;
  try {
    const list = await getSeekerCards(userId ? { userId } : undefined);
    if (Array.isArray(list) && list.length > 0) {
      candidate.value = userId
        ? list.find((item) => item.userId === userId) || list[0]
        : list[0];
    }
  } catch (error) {
    console.error('Failed to load seeker card:', error);
    uni.showToast({ title: '加载失败', icon: 'none' });
  } finally {
    loading.value = false;
  }
};

const jobStatusText = (status?: number) => {
  const map: Record<number, string> = {
    0: '在职观望',
    1: '离职-随时到岗',
    2: '在职-月内到岗',
  };
  if (status === undefined || status === null) return '未填写';
  return map[status] || '未填写';
};

const showToast = (msg: string) => {
  uni.showToast({ title: msg, icon: 'success' });
};

onLoad((options) => {
  const uid = options?.id ? Number(options.id) : undefined;
  loadCandidate(uid);
});
</script>

<style scoped lang="scss">
.candidate-detail {
  padding: 24rpx;
  background: #f6f7fb;
  min-height: 100vh;
}

.card {
  background: #fff;
  border-radius: 24rpx;
  padding: 28rpx;
  margin-bottom: 24rpx;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.name {
  font-size: 34rpx;
  font-weight: 600;
}

.score {
  color: #2f7cff;
  font-size: 28rpx;
}

.meta {
  margin-top: 10rpx;
  color: #8f96a9;
}

.actions button {
  width: 100%;
  height: 84rpx;
  border-radius: 18rpx;
  margin-bottom: 16rpx;
}

button.primary {
  background: #2f7cff;
  color: #fff;
  border: none;
}

button.secondary {
  background: #ffdfe0;
  color: #ff4d4f;
  border: none;
}

button.outline {
  border: 2rpx solid #2f7cff;
  color: #2f7cff;
  background: transparent;
}

.loading {
  text-align: center;
  color: #8a92a7;
  padding: 20rpx 0;
}
</style>
