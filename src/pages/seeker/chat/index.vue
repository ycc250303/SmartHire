<template>
  <view class="page">
    <!--Debug button-->
    <button class="logout-btn" @click="handleLogout">Logout (Debug)</button>
    
    <CustomTabBar />
  </view>
</template>

<script setup lang="ts">
import { onLoad, onShow } from '@dcloudio/uni-app';
import { t } from '@/locales';
import { clearToken } from '@/services/http';
import CustomTabBar from '@/components/common/CustomTabBar.vue';

onLoad(() => {
  uni.setNavigationBarTitle({
    title: t('navigation.chat')
  });
});

onShow(() => {
  uni.hideTabBar();
});

// Debug 
function handleLogout() {
  clearToken();
  uni.redirectTo({
    url: '/pages/seeker/auth/login',
  });
}

</script>

<style lang="scss" scoped>
@use "@/styles/variables.scss" as vars;

.page {
  min-height: 100vh;
  background: linear-gradient(to bottom, vars.$light-blue 0%, vars.$surface-color 25%);
  padding: vars.$spacing-md;
  padding-top: calc(var(--status-bar-height) + vars.$spacing-lg);
  padding-bottom: 120rpx;
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

/* debug */
.logout-btn {
  width: 200rpx;
  height: 80rpx;
  
}

</style>
