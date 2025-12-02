<template>
  <view class="profile-page">
    <view class="card">
      <view class="name-row">
        <view class="profile-info">
          <view class="avatar-wrapper">
            <image :src="avatarImg" mode="aspectFill" />
          </view>
          <view>
            <view class="name">{{ profile.name }}</view>
            <view class="role">{{ profile.role }} · {{ profile.company }}</view>
          </view>
        </view>
        <view class="badge">{{ profile.companyStatus }}</view>
      </view>
      <view class="info">邮箱：{{ profile.email }}</view>
      <view class="info">手机号：{{ profile.phone }}</view>
    </view>

    <view class="card">
      <view class="section-title">设置</view>
      <view class="setting-item">
        <text>接收推送通知</text>
        <switch :checked="profile.notifyEnabled" @change="toggleNotify" color="#2f7cff" />
      </view>
      <view class="setting-item">
        <text>深色模式</text>
        <switch :checked="profile.darkMode" @change="toggleDarkMode" color="#2f7cff" />
      </view>
    </view>

    <view class="card logout-card">
      <button class="logout" @click="logout">退出登录</button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { fetchProfile, type HRProfile } from '@/mock/hr';
import { useHrStore } from '@/store/hr';
import avatarImg from '@/static/user-avatar.png';

const profile = ref<HRProfile>({
  name: '',
  role: '',
  email: '',
  phone: '',
  company: '',
  companyStatus: '未认证',
  notifyEnabled: true,
  darkMode: false,
});

const hrStore = useHrStore();

const loadProfile = async () => {
  // TODO: GET /api/hr/profile
  profile.value = await fetchProfile();
  hrStore.setCompanyName(profile.value.company);
  hrStore.toggleDarkMode(profile.value.darkMode);
};

const toggleNotify = (event: any) => {
  profile.value.notifyEnabled = event.detail.value;
  // TODO: PATCH /api/hr/profile/notify
};

const toggleDarkMode = (event: any) => {
  profile.value.darkMode = event.detail.value;
  hrStore.toggleDarkMode(event.detail.value);
  // TODO: PATCH /api/hr/profile/preferences
};

const logout = () => {
  // TODO: 调用退出登录接口
  uni.showModal({
    title: '提示',
    content: '确认退出当前账号吗？',
    success: (res) => {
      if (res.confirm) {
        uni.reLaunch({ url: '/pages/auth/login' });
      }
    },
  });
};

onMounted(() => {
  loadProfile();
});
</script>

<style scoped lang="scss">
.profile-page {
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

.name-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.profile-info {
  display: flex;
  align-items: center;
}

.avatar-wrapper {
  width: 112rpx;
  height: 112rpx;
  border-radius: 16rpx;
  background: #eef2ff;
  margin-right: 24rpx;
  overflow: hidden;
}

.avatar-wrapper image {
  width: 100%;
  height: 100%;
}

.name {
  font-size: 34rpx;
  font-weight: 600;
}

.role {
  color: #8a92a7;
  margin-top: 8rpx;
}

.badge {
  padding: 8rpx 20rpx;
  background: #e8f3ff;
  color: #2f7cff;
  border-radius: 999rpx;
}

.info {
  margin-top: 10rpx;
  color: #6f788f;
}

.section-title {
  font-size: 30rpx;
  font-weight: 600;
  margin-bottom: 16rpx;
}

.setting-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 0;
  border-top: 2rpx solid #f1f2f6;
}

.logout-card {
  text-align: center;
}

.logout {
  width: 100%;
  height: 90rpx;
  background: #ff4d4f;
  color: #fff;
  border: none;
  border-radius: 18rpx;
}
</style>
