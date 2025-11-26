<template>
  <view class="attachment-resume-page">
    
  </view>
</template>

<script setup lang="ts">
import { onLoad } from '@dcloudio/uni-app';
import { t } from '@/locales';
import { useNavigationTitle } from '@/utils/useNavigationTitle';

useNavigationTitle('navigation.attachmentResume');

onLoad(() => {
  // Navigation title is handled by useNavigationTitle
});
</script>

<style lang="scss" scoped>
@use "@/styles/variables.scss" as vars;

.attachment-resume-page {
  min-height: 100vh;
  background-color: vars.$surface-color;
  padding: vars.$spacing-xl;
}

.section {
  margin-bottom: vars.$spacing-xl;
}

.section-title {
  display: block;
  font-size: 32rpx;
  font-weight: 600;
  color: vars.$text-color;
  margin-bottom: vars.$spacing-lg;
}

.content-placeholder {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400rpx;
  background-color: vars.$bg-color;
  border-radius: 16rpx;
  padding: vars.$spacing-xl;
}

.placeholder-text {
  font-size: 28rpx;
  color: vars.$text-muted;
}
</style>

