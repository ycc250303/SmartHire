<template>
  <view class="search-page">
    <view class="search-container">
      <!-- Search interface placeholder -->
    </view>
  </view>
</template>

<script setup lang="ts">
import { onLoad } from '@dcloudio/uni-app';
import { t } from '@/locales';

onLoad(() => {
  uni.setNavigationBarTitle({
    title: t('navigation.search')
  });
});
</script>

<style lang="scss" scoped>
@use "@/styles/variables.scss" as vars;

.search-page {
  min-height: 100vh;
  background: vars.$surface-color;
  padding-top: calc(var(--status-bar-height) + vars.$spacing-lg);
}

.search-container {
  padding: vars.$spacing-xl;
}
</style>

