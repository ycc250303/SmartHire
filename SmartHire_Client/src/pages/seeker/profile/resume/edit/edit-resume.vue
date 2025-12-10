<template>
  <view class="edit-resume-page">
    <view v-if="loading" class="loading-container">
      <text class="loading-text">{{ t('pages.resume.attachment.saving') }}</text>
    </view>

    <scroll-view v-else class="form-content" scroll-y>
      <view class="form-group">
        <view class="form-item">
          <text class="form-label">{{ t('pages.resume.attachment.resumeName') }}</text>
          <input
            class="form-input"
            v-model="formData.resumeName"
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
      </view>
    </scroll-view>

    <view class="bottom-bar">
      <view class="delete-button" @tap="handleDelete" :class="{ 'button-disabled': deleting }">
        <text class="button-text button-text-danger">
          {{ deleting ? t('pages.resume.attachment.deleting') : t('pages.resume.attachment.delete') }}
        </text>
      </view>
      <view class="save-button" @tap="handleSave" :class="{ 'button-disabled': saving }">
        <text class="button-text">
          {{ saving ? t('pages.resume.attachment.saving') : t('pages.resume.attachment.save') }}
        </text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { t } from '@/locales';
import { useNavigationTitle } from '@/utils/useNavigationTitle';
import type { UpdateResumeParams, Resume } from '@/services/api/resume';
import { getResumes, updateResume, deleteResume } from '@/services/api/resume';

useNavigationTitle('navigation.editResume');

const loading = ref(false);
const saving = ref(false);
const deleting = ref(false);
const resumeId = ref<number>();
const formData = ref<UpdateResumeParams>({
  resumeName: '',
  privacyLevel: 0,
});

const privacyOptions = [
  { label: t('pages.resume.attachment.privacyPublic'), value: 0 },
  { label: t('pages.resume.attachment.privacyPrivate'), value: 1 },
  { label: t('pages.resume.attachment.privacyConfidential'), value: 2 },
];

const privacyIndex = computed(() => {
  if (formData.value.privacyLevel === undefined || formData.value.privacyLevel === null) {
    return 0;
  }
  return formData.value.privacyLevel;
});

onLoad((options: any) => {
  if (options?.id) {
    resumeId.value = parseInt(options.id);
    loadData();
  }
});

async function loadData() {
  if (!resumeId.value) return;

  loading.value = true;
  try {
    const list = await getResumes();
    const resume = list.find((r) => r.id === resumeId.value);
    
    if (resume) {
      formData.value = {
        resumeName: resume.resumeName,
        privacyLevel: resume.privacyLevel,
      };
    }
  } catch (error) {
    console.error('Failed to load resume:', error);
    uni.showToast({
      title: t('pages.resume.attachment.saveError'),
      icon: 'none',
    });
  } finally {
    loading.value = false;
  }
}

function handlePrivacyChange(e: any) {
  formData.value.privacyLevel = privacyOptions[parseInt(e.detail.value)].value;
}

async function handleSave() {
  if (saving.value || !resumeId.value) return;

  if (!formData.value.resumeName || !formData.value.resumeName.trim()) {
    uni.showToast({
      title: t('pages.resume.attachment.nameRequired'),
      icon: 'none',
    });
    return;
  }

  saving.value = true;

  try {
    await updateResume(resumeId.value, {
      resumeName: formData.value.resumeName.trim(),
      privacyLevel: formData.value.privacyLevel,
    });

    uni.showToast({
      title: t('pages.resume.attachment.saveSuccess'),
      icon: 'success',
    });

    setTimeout(() => {
      uni.navigateBack();
    }, 1500);
  } catch (error) {
    console.error('Save failed:', error);
    uni.showToast({
      title: t('pages.resume.attachment.saveError'),
      icon: 'none',
    });
  } finally {
    saving.value = false;
  }
}

async function handleDelete() {
  if (deleting.value || !resumeId.value) return;

  uni.showModal({
    title: t('pages.resume.edit.common.confirm'),
    content: t('pages.resume.attachment.deleteConfirm'),
    success: async (res) => {
      if (res.confirm) {
        deleting.value = true;
        try {
          await deleteResume(resumeId.value!);
          uni.showToast({
            title: t('pages.resume.attachment.deleteSuccess'),
            icon: 'success',
          });

          setTimeout(() => {
            uni.navigateBack();
          }, 1500);
        } catch (error) {
          console.error('Delete failed:', error);
          uni.showToast({
            title: t('pages.resume.attachment.deleteError'),
            icon: 'none',
          });
        } finally {
          deleting.value = false;
        }
      }
    },
  });
}
</script>

<style lang="scss" scoped>
@use "@/styles/variables.scss" as vars;

.edit-resume-page {
  min-height: 100vh;
  background-color: #F2F2F7;
  display: flex;
  flex-direction: column;
  padding-bottom: 120rpx;
}

.loading-container {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 80rpx 32rpx;
}

.loading-text {
  font-size: 28rpx;
  color: #8E8E93;
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

.bottom-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  padding: 24rpx 32rpx;
  padding-bottom: calc(24rpx + env(safe-area-inset-bottom));
  background-color: #F2F2F7;
  display: flex;
  gap: 24rpx;
}

.save-button,
.delete-button {
  flex: 1;
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

.save-button {
  background-color: vars.$primary-color;
}

.delete-button {
  background-color: #FF3B30;
}

.button-text {
  font-size: 32rpx;
  font-weight: 600;
  color: #FFFFFF;
}

.button-text-danger {
  color: #FFFFFF;
}
</style>

