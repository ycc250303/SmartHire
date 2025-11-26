<template>
  <view class="help-page">
    <text class="help-message">{{ t('auth.help.contactMessage') }}</text>
  </view>
</template>

<script setup lang="ts">
import { onLoad } from '@dcloudio/uni-app';
import { t } from '@/locales';
import { useNavigationTitle } from '@/utils/useNavigationTitle';

useNavigationTitle('navigation.help');

onLoad(() => {
  // Navigation title is handled by useNavigationTitle
});
</script>

<style lang="scss" scoped>
@use "@/styles/variables.scss" as vars;

.help-page {
  min-height: 100vh;
  padding: vars.$spacing-xl;
  display: flex;
  align-items: center;
  justify-content: center;
}

.help-message {
  font-size: 28rpx;
  color: vars.$text-muted;
  text-align: center;
  line-height: 1.8;
}
</style>
