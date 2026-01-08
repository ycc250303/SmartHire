<template>
  <view class="profile-page">
    <view class="header-section">
      <image 
        class="settings-icon"
        src="/static/settings.png"
        mode="aspectFit"
        @click="goToSettings"
      />
    </view>

    <view class="user-info-section" @click="goToSelf">
      <image 
        class="avatar"
        :src="userInfo?.avatarUrl || '/static/default-avatar.png'"
        mode="aspectFill"
      />
      <view class="user-details">
        <text class="username">{{ userInfo?.username || '' }}</text>
        <text class="identity">{{ userIdentity }}</text>
      </view>
      <image 
        class="enter-icon"
        src="/static/enter.png"
        mode="aspectFit"
      />
    </view>

    <view class="resume-section">
      <view class="resume-item" @click="goToOnlineResume">
        <text class="resume-label">{{ t('pages.profile.onlineResume') }}</text>
        <text class="arrow">›</text>
      </view>
      <view class="resume-item" @click="goToAttachmentResume">
        <text class="resume-label">{{ t('pages.profile.attachmentResume') }}</text>
        <text class="arrow">›</text>
      </view>
      <view class="resume-item" @click="goToApplications">
        <text class="resume-label">{{ t('pages.profile.applications') }}</text>
        <text class="arrow">›</text>
      </view>
      <view class="resume-item" @click="goToFavorites">
        <text class="resume-label">{{ t('pages.profile.favorites') }}</text>
        <text class="arrow">›</text>
      </view>
      <view class="resume-item" @click="goToCareerPath">
        <text class="resume-label">{{ t('pages.profile.careerPath.title') }}</text>
        <text class="arrow">›</text>
      </view>
    </view>
    
    <CustomTabBar />
  </view>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue';
import { onLoad, onShow } from '@dcloudio/uni-app';
import { t } from '@/locales';
import { useUserStore } from '@/store/user';
import { UserType } from '@/services/api/auth';
import CustomTabBar from '@/components/common/CustomTabBar.vue';

const userStore = useUserStore();

const userInfo = computed(() => userStore.userInfo);

const userIdentity = computed(() => {
  if (!userInfo.value) return '';
  // UserType.Seeker = 1 -> Talent, UserType.HR = 2 -> Recruiter
  return userInfo.value.userType === UserType.Seeker 
    ? t('pages.profile.talent')
    : t('pages.profile.recruiter');
});

onLoad(() => {
  uni.setNavigationBarTitle({
    title: t('navigation.profile')
  });
});

onShow(() => {
  uni.hideTabBar();
});

onMounted(async () => {
  if (!userStore.isLoaded) {
    try {
      await userStore.loadUserInfo();
    } catch (error) {
      console.error('Failed to load user info:', error);
    }
  }
});

function goToSettings() {
  uni.navigateTo({
    url: '/pages/seeker/profile/settings'
  });
}

function goToSelf() {
  uni.navigateTo({
    url: '/pages/seeker/profile/self'
  });
}

function goToOnlineResume() {
  uni.navigateTo({
    url: '/pages/seeker/profile/resume/online-resume'
  });
}

function goToAttachmentResume() {
  uni.navigateTo({
    url: '/pages/seeker/profile/resume/attachment-resume'
  });
}

function goToApplications() {
  uni.navigateTo({
    url: '/pages/seeker/jobs/applications'
  });
}

function goToFavorites() {
  uni.navigateTo({
    url: '/pages/seeker/jobs/favorites'
  });
}

function goToCareerPath() {
  uni.navigateTo({
    url: '/pages/seeker/profile/career-path'
  });
}
</script>

<style lang="scss" scoped>
@use "@/styles/variables.scss" as vars;

.profile-page {
  min-height: 100vh;
  background: linear-gradient(to bottom, vars.$light-blue 0%, vars.$surface-color 25%);
  padding: vars.$spacing-xl;
  padding-top: calc(var(--status-bar-height) + vars.$spacing-lg);
  padding-bottom: 120rpx;
}

.header-section {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 0rpx;
}

.settings-icon {
  width: 48rpx;
  height: 48rpx;
  cursor: pointer;
}

.user-info-section {
  display: flex;
  align-items: center;
  padding: vars.$spacing-lg;
  cursor: pointer;
}

.avatar {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  margin-right: vars.$spacing-lg;
  background-color: vars.$bg-color;
}

.user-details {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.username {
  font-size: 36rpx;
  font-weight: 700;
  color: vars.$text-color;
  margin-bottom: vars.$spacing-xs;
}

.identity {
  font-size: 26rpx;
  color: vars.$text-muted;
}

.enter-icon {
  width: 32rpx;
  height: 32rpx;
  margin-left: vars.$spacing-md;
}

.resume-section {
  margin-top: vars.$spacing-xl;
  background-color: vars.$surface-color;
  border-radius: 16rpx;
  padding: vars.$spacing-md;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
}

.resume-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: vars.$spacing-lg;
  border-bottom: 1rpx solid vars.$light-blue;
  cursor: pointer;
  transition: opacity 0.2s;

  &:active {
    opacity: 0.7;
  }

  &:last-child {
    border-bottom: none;
  }
}

.resume-label {
  font-size: 30rpx;
  color: vars.$text-color;
  font-weight: 500;
}

.arrow {
  font-size: 48rpx;
  color: vars.$text-muted;
  font-weight: 300;
  margin-left: vars.$spacing-md;
}
</style>
