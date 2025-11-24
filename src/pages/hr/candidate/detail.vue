<template>
  <view class="candidate-detail">
    <view class="card">
      <view class="header">
        <view class="name">{{ candidate.name }}</view>
        <view class="score">鍖归厤搴?{{ candidate.score }}%</view>
      </view>
      <view class="meta">鎰忓悜宀椾綅锛歿{ candidate.intentPosition }}</view>
      <view class="meta">褰撳墠鐘舵€侊細{{ candidate.status }}</view>
      <view class="tags">
        <text class="tag" v-for="tag in candidate.tags" :key="tag">{{ tag }}</text>
      </view>
      <view class="section">
        <view class="section-title">浼樺娍浜偣</view>
        <view class="section-content">鍏峰澶у巶鑳屾櫙锛屼富瀵煎绔悓鏋勬柟妗堛€?/view>
      </view>
      <view class="section">
        <view class="section-title">闇€鍏虫敞浜嬮」</view>
        <view class="section-content">缂哄皯 ToB 澶嶆潅鏉冮檺缁忛獙锛屽缓璁潰璇曟椂閲嶇偣纭銆?/view>
      </view>
    </view>

    <view class="card actions">
      <button class="primary" @click="updateStatus('pass')">閫氳繃鍒濈瓫</button>
      <button class="secondary" @click="updateStatus('reject')">鏆備笉鍚堥€?/button>
      <button class="outline" @click="updateStatus('interview')">鍔犲叆闈㈣瘯</button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { mockCandidateMatches } from '@/mock/hr';

const candidate = ref(mockCandidateMatches[0]);

const loadCandidate = (id?: string) => {
  if (!id) return;
  const found = mockCandidateMatches.find((item) => item.id === id);
  if (found) candidate.value = found;
};

const updateStatus = (status: string) => {
  // TODO: POST /api/hr/candidates/:id/status
  uni.showToast({ title: `宸叉爣璁颁负 ${status}`, icon: 'success' });
};

onLoad((options) => {
  loadCandidate(options?.id as string);
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

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-top: 16rpx;
}

.tag {
  background: #eef2ff;
  color: #2f7cff;
  padding: 6rpx 20rpx;
  border-radius: 16rpx;
}

.section-title {
  font-weight: 600;
  margin-top: 20rpx;
  margin-bottom: 8rpx;
}

button.primary,
button.secondary,
button.outline {
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
</style>
