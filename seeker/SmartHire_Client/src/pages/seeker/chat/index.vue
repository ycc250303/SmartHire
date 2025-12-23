<template>
  <view class="chat-page">
    <view class="header-container">
      <view class="header-title-section">
        <text class="page-title">{{ t('navigation.chat') }}</text>
        <view v-if="totalUnreadCount > 0" class="title-unread-badge">
          <text class="title-unread-count">{{ totalUnreadCount > 99 ? '99+' : totalUnreadCount }}</text>
        </view>
      </view>
    </view>
    <view class="content-section">
      <MessagesComponent @update-unread="handleUnreadUpdate" />
    </view>
    
    <CustomTabBar />
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { onLoad, onShow } from '@dcloudio/uni-app';
import { t } from '@/locales';
import CustomTabBar from '@/components/common/CustomTabBar.vue';
import MessagesComponent from './messages.vue';

const totalUnreadCount = ref(0);

function handleUnreadUpdate(count: number) {
  totalUnreadCount.value = count;
}

onLoad(() => {
  uni.setNavigationBarTitle({
    title: t('navigation.chat')
  });
});

onShow(() => {
  uni.hideTabBar();
});
</script>

<style lang="scss" scoped>
@use "@/styles/variables.scss" as vars;

.chat-page {
  min-height: 100vh;
  background: vars.$surface-color;
  display: flex;
  flex-direction: column;
}

.header-container {
  background: linear-gradient(to bottom, vars.$light-blue 0%, vars.$surface-color 100%);
  position: sticky;
  top: 0;
  z-index: 1000;
  padding-top: calc(var(--status-bar-height) + vars.$spacing-lg);
  padding-bottom: vars.$spacing-md;
  padding-left: vars.$spacing-xl;
  padding-right: vars.$spacing-xl;
}

.header-title-section {
  display: flex;
  align-items: center;
  gap: vars.$spacing-sm;
}

.page-title {
  font-size: 40rpx;
  font-weight: 700;
  color: vars.$text-color;
}

.title-unread-badge {
  min-width: 32rpx;
  height: 32rpx;
  background-color: #ff4d4f;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 10rpx;
}

.title-unread-count {
  font-size: 20rpx;
  color: #ffffff;
  font-weight: 600;
}

.content-section {
  flex: 1;
  padding-bottom: 120rpx;
  position: relative;
  z-index: 1;
  overflow: hidden;
}
</style>
