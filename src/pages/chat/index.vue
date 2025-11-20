<template>
  <view class="page">
    
  </view>
</template>

<script setup lang="ts">
import { onLoad } from '@dcloudio/uni-app';
import { t } from '@/locales';


onLoad(() => {
  uni.setNavigationBarTitle({
    title: t('navigation.chat')
  });

});
</script>

<style lang="scss" scoped>
@use "@/styles/variables.scss" as vars;

.page {
  min-height: 100vh;
  background-color: vars.$bg-color;
  padding: vars.$spacing-md;
}

.welcome-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 400rpx;
  padding: vars.$spacing-xl;
  background-color: vars.$surface-color;
  border-radius: vars.$border-radius;
}

.welcome-title {
  font-size: 48rpx;
  font-weight: 600;
  color: vars.$primary-color;
  margin-bottom: vars.$spacing-sm;
}

.welcome-subtitle {
  font-size: 28rpx;
  color: vars.$text-muted;
}
</style>
