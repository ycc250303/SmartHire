<template>
  <view class="upload-resume-page">
    <scroll-view class="form-content" scroll-y>
      <view class="form-group">
        <view class="form-item">
          <text class="form-label">{{ t('pages.resume.attachment.resumeName') }}</text>
          <input
            class="form-input"
            v-model="resumeName"
            :placeholder="t('pages.resume.attachment.resumeNamePlaceholder')"
            placeholder-class="input-placeholder"
            :maxlength="50"
          />
        </view>

        <view class="form-item">
          <text class="form-label">{{ t('pages.resume.attachment.privacyLevel') }}</text>
          <picker
            class="form-picker"
            mode="selector"
            :range="privacyOptions"
            :range-key="'label'"
            :value="privacyIndex"
            @change="handlePrivacyChange"
          >
            <view class="picker-value">
              <text class="picker-text">
                {{ privacyOptions[privacyIndex]?.label }}
              </text>
              <text class="picker-arrow">›</text>
            </view>
          </picker>
        </view>

        <view class="form-item">
          <text class="form-label">{{ t('pages.resume.attachment.selectFile') }}</text>
          <view class="file-info">
            <text class="file-name">{{ fileName || t('pages.resume.attachment.fileRequired') }}</text>
          </view>
        </view>
      </view>
    </scroll-view>

    <view class="bottom-bar">
      <view class="save-button" @tap="handleUpload" :class="{ 'button-disabled': uploading }">
        <text class="button-text">
          {{ uploading ? t('pages.resume.attachment.uploading') : t('pages.resume.attachment.upload') }}
        </text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { t } from '@/locales';
import { useNavigationTitle } from '@/utils/useNavigationTitle';
import { uploadResume } from '@/services/api/resume';

useNavigationTitle('navigation.uploadResume');

const uploading = ref(false);
const resumeName = ref('');
const privacyLevel = ref(0);
const filePath = ref('');
const fileName = ref('');

const privacyOptions = [
  { label: t('pages.resume.attachment.privacyPublic'), value: 0 },
  { label: t('pages.resume.attachment.privacyPrivate'), value: 1 },
  { label: t('pages.resume.attachment.privacyConfidential'), value: 2 },
];

const privacyIndex = ref(0);

onLoad((options: any) => {
  if (options?.filePath) {
    filePath.value = decodeURIComponent(options.filePath);
  }
  if (options?.defaultName) {
    resumeName.value = decodeURIComponent(options.defaultName);
    fileName.value = decodeURIComponent(options.defaultName) + '.pdf';
  }
});

function handlePrivacyChange(e: any) {
  privacyIndex.value = parseInt(e.detail.value);
  privacyLevel.value = privacyOptions[privacyIndex.value].value;
}

async function handleUpload() {
  if (uploading.value) return;

  if (!filePath.value) {
    uni.showToast({
      title: t('pages.resume.attachment.fileRequired'),
      icon: 'none',
    });
    return;
  }

  if (!resumeName.value.trim()) {
    uni.showToast({
      title: t('pages.resume.attachment.nameRequired'),
      icon: 'none',
    });
    return;
  }

  uploading.value = true;

  try {
    await uploadResume({
      filePath: filePath.value,
      resumeName: resumeName.value.trim(),
      privacyLevel: privacyLevel.value,
      fileName: fileName.value || 'resume.pdf',
    });

    uni.showToast({
      title: t('pages.resume.attachment.uploadSuccess'),
      icon: 'success',
    });

    setTimeout(() => {
      uni.navigateBack();
    }, 1500);
  } catch (error) {
    console.error('Upload failed:', error);
    const errorMsg = error instanceof Error ? error.message : String(error);
    uni.showToast({
      title: errorMsg || t('pages.resume.attachment.uploadError'),
      icon: 'none',
      duration: 3000,
    });
  } finally {
    uploading.value = false;
  }
}
</script>

<style lang="scss" scoped>
@use "@/styles/variables.scss" as vars;

.upload-resume-page {
  min-height: 100vh;
  background-color: #F2F2F7;
  display: flex;
  flex-direction: column;
  padding-bottom: 120rpx;
}

.form-content {
  flex: 1;
  padding: 32rpx;
}

.form-group {
  background-color: #FFFFFF;
  border-radius: 20rpx;
  margin-bottom: 32rpx;
  overflow: hidden;
}

.form-item {
  padding: 32rpx;
  border-bottom: 1rpx solid #F2F2F7;
  
  &:last-child {
    border-bottom: none;
  }
}

.form-label {
  font-size: 32rpx;
  font-weight: 600;
  color: #000000;
  margin-bottom: 16rpx;
  display: block;
}

.form-input {
  font-size: 30rpx;
  color: #000000;
  line-height: 1.5;
  padding: 0;
}

.input-placeholder {
  color: #C7C7CC;
  font-size: 30rpx;
}

.form-picker {
  width: 100%;
}

.picker-value {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.picker-text {
  font-size: 30rpx;
  color: #000000;
  flex: 1;
}

.picker-arrow {
  font-size: 48rpx;
  color: #C6C6C8;
  font-weight: 300;
  margin-left: 16rpx;
}

.file-info {
  padding: 16rpx 0;
}

.file-name {
  font-size: 28rpx;
  color: #000000;
}

.bottom-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  padding: 24rpx 32rpx;
  padding-bottom: calc(24rpx + env(safe-area-inset-bottom));
  background-color: #F2F2F7;
}

.save-button {
  background-color: vars.$primary-color;
  border-radius: 16rpx;
  padding: 28rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: opacity 0.2s;
  
  &:active:not(.button-disabled) {
    opacity: 0.8;
  }
  
  &.button-disabled {
    opacity: 0.6;
  }
}

.button-text {
  font-size: 32rpx;
  font-weight: 600;
  color: #FFFFFF;
}
</style>

