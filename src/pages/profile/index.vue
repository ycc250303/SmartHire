<template>
  <view class="page">
    <view class="profile-header" v-if="userInfo">
      <view class="avatar">{{ userInfo.username.charAt(0).toUpperCase() }}</view>
      <text class="username">{{ userInfo.username }}</text>
      <text class="email">{{ userInfo.email }}</text>
    </view>
    
    <view class="menu-list">
      <view class="menu-item" @click="handleLogout">
        <text class="menu-text">退出登录</text>
        <text class="menu-arrow">></text>
      </view>
    </view>
    
    <view class="loading-state" v-if="loading">
      <text class="loading-text">加载中...</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { useUserStore } from '@/store/user';

const userInfo = ref(useUserStore().userInfo);
const loading = ref(false);

const loadUserInfo = async () => {
  loading.value = true;
  try {
    const userStore = useUserStore();
    if (!userStore.isLoaded) {
      await userStore.loadUserInfo();
    }
    userInfo.value = userStore.userInfo;
  } catch (error) {
    console.error('Failed to load user info:', error);
  } finally {
    loading.value = false;
  }
};

const handleLogout = () => {
  uni.showModal({
    title: '确认退出',
    content: '确定要退出登录吗？',
    success: (res) => {
      if (res.confirm) {
        const userStore = useUserStore();
        userStore.logout();
      }
    },
  });
};

onLoad(() => {
  loadUserInfo();
});
</script>

<style lang="scss" scoped>
@use "@/styles/variables.scss" as vars;

.page {
  min-height: 100vh;
  background-color: vars.$bg-color;
}

.profile-header {
  background-color: vars.$surface-color;
  padding: vars.$spacing-xl;
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: vars.$spacing-md;
}

.avatar {
  width: 160rpx;
  height: 160rpx;
  border-radius: 50%;
  background-color: vars.$primary-color-soft;
  color: vars.$primary-color;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 64rpx;
  font-weight: 600;
  margin-bottom: vars.$spacing-md;
}

.username {
  font-size: 36rpx;
  font-weight: 600;
  color: vars.$text-color;
  margin-bottom: vars.$spacing-sm;
}

.email {
  font-size: 28rpx;
  color: vars.$text-muted;
}

.menu-list {
  background-color: vars.$surface-color;
}

.menu-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: vars.$spacing-lg;
  border-bottom: 1rpx solid #f0f0f0;
}

.menu-item:last-child {
  border-bottom: none;
}

.menu-text {
  font-size: 32rpx;
  color: vars.$text-color;
}

.menu-arrow {
  font-size: 32rpx;
  color: vars.$text-muted;
}

.loading-state {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400rpx;
  padding: vars.$spacing-xl;
}

.loading-text {
  font-size: 28rpx;
  color: vars.$text-muted;
}
</style>






