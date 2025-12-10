<template>
  <view class="notifications-component">
    <view class="empty-container">
      <text class="empty-text">{{ t('pages.chat.notifications.empty') }}</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { t } from '@/locales';
</script>

<style lang="scss" scoped>
@use "@/styles/variables.scss" as vars;

.notifications-component {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.empty-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  flex: 1;
  padding: vars.$spacing-xl;
}

.empty-text {
  font-size: 28rpx;
  color: vars.$text-muted;
}
</style>

