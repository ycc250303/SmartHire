<template>
  <view class="fulltime-page">
    <view class="empty-state">
      
    </view>
  </view>
</template>

<script setup lang="ts">
import { t } from '@/locales';
</script>

<style lang="scss" scoped>
@use "@/styles/variables.scss" as vars;

.fulltime-page {
  display: flex;
  flex-direction: column;
  min-height: 400rpx;
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400rpx;
  padding: vars.$spacing-xl;
}

.empty-text {
  font-size: 28rpx;
  color: vars.$text-muted;
}
</style>

