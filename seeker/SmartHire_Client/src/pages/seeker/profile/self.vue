<template>
  <view class="self-page">
    <view class="section">
      <text class="section-title">{{ t('pages.self.basicInfo') }}</text>
      
      <view class="avatar-section">
        <view class="avatar-item" @click="handleAvatarClick">
          <view class="avatar-content">
            <text class="info-label">{{ t('pages.self.avatar') }}</text>
            <image 
              class="avatar-image"
              :src="userInfo?.avatarUrl || '/static/default-avatar.png'"
              mode="aspectFill"
            />
          </view>
          <text class="arrow">›</text>
        </view>
      </view>

      <view class="info-list">
        <view class="info-item">
          <view class="info-content">
            <text class="info-label">{{ t('pages.self.username') }}</text>
            <text class="info-value">{{ userInfo?.username || '-' }}</text>
          </view>
        </view>

        <view class="info-item">
          <view class="info-content">
            <text class="info-label">{{ t('pages.self.email') }}</text>
            <text class="info-value">{{ userInfo?.email || '-' }}</text>
          </view>
        </view>

        <view class="info-item">
          <view class="info-content">
            <text class="info-label">{{ t('pages.self.phone') }}</text>
            <text class="info-value">{{ userInfo?.phone || '-' }}</text>
          </view>
        </view>

        <view class="info-item">
          <view class="info-content">
            <text class="info-label">{{ t('pages.self.identity') }}</text>
            <text class="info-value">{{ userIdentity }}</text>
          </view>
        </view>
      </view>
    </view>

    <view class="section">
      <view class="edit-resume-button" @click="handleEditResumeInfo">
        <text class="button-text">{{ t('pages.self.editResumeInfo') }}</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { t } from '@/locales';
import { useNavigationTitle } from '@/utils/useNavigationTitle';
import { useUserStore } from '@/store/user';
import { UserType } from '@/services/api/auth';
import { updateUserAvatarWithFile, type UserInfo } from '@/services/api/user';

useNavigationTitle('navigation.self');

const userStore = useUserStore();
const uploading = ref(false);

const userInfo = computed(() => userStore.userInfo);

const userIdentity = computed(() => {
  if (!userInfo.value) return '-';
  // UserType.Seeker = 1 -> Talent, UserType.HR = 2 -> Recruiter
  return userInfo.value.userType === UserType.Seeker 
    ? t('pages.profile.talent')
    : t('pages.profile.recruiter');
});

onLoad(() => {
  // Navigation title is handled by useNavigationTitle
});

onMounted(async () => {
  if (!userStore.isLoaded) {
    try {
      await userStore.loadUserInfo();
    } catch (error) {
      console.error('Failed to load user info:', error);
      uni.showToast({
        title: t('pages.profile.loadError'),
        icon: 'none'
      });
    }
  }
});

async function handleAvatarClick() {
  if (uploading.value) return;

  try {
    const res = await uni.chooseImage({
      count: 1,
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
    });

    if (res.tempFilePaths && res.tempFilePaths.length > 0) {
      uploading.value = true;
      const filePath = res.tempFilePaths[0];
      
      if (!filePath) {
        uploading.value = false;
        return;
      }

      uni.showLoading({
        title: t('pages.self.uploading'),
      });

      try {
        await updateUserAvatarWithFile(filePath);
        await userStore.loadUserInfo();

        uni.showToast({
          title: t('pages.self.uploadSuccess'),
          icon: 'success',
        });
      } catch (error) {
        console.error('Failed to update avatar:', error);
        const errorMessage = error instanceof Error ? error.message : String(error);
        uni.showToast({
          title: errorMessage || t('pages.self.uploadError'),
          icon: 'none',
          duration: 3000,
        });
      } finally {
        uni.hideLoading();
        uploading.value = false;
      }
    }
  } catch (error) {
    console.error('Failed to choose image:', error);
    uploading.value = false;
  }
}

function handleEditResumeInfo() {
  uni.navigateTo({
    url: '/pages/seeker/profile/resume/edit/basic-info'
  });
}
</script>

<style lang="scss" scoped>
@use "@/styles/variables.scss" as vars;

.self-page {
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

.avatar-section {
  margin-bottom: vars.$spacing-xl;
}

.avatar-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: vars.$surface-color;
  border-bottom: 1rpx solid vars.$light-blue;
  padding: vars.$spacing-lg 0;
  cursor: pointer;
  transition: opacity 0.2s;

  &:active {
    opacity: 0.7;
  }
}

.avatar-content {
  display: flex;
  align-items: center;
  flex: 1;
  gap: vars.$spacing-lg;
}

.avatar-image {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  background-color: vars.$bg-color;
}

.info-list {
  display: flex;
  flex-direction: column;
  gap: vars.$spacing-lg;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: vars.$surface-color;
  border-bottom: 1rpx solid vars.$light-blue;
  padding: vars.$spacing-lg 0;

  &:last-child {
    border-bottom: none;
  }
}

.info-content {
  display: flex;
  flex-direction: column;
  flex: 1;
}

.info-label {
  font-size: 24rpx;
  color: vars.$text-muted;
  margin-bottom: vars.$spacing-xs;
}

.info-value {
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

.edit-resume-button {
  background-color: vars.$primary-color;
  border-radius: 16rpx;
  padding: 28rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: opacity 0.2s;
  margin-top: vars.$spacing-lg;
  
  &:active {
    opacity: 0.8;
  }
}

.button-text {
  font-size: 32rpx;
  font-weight: 600;
  color: #FFFFFF;
}
</style>

