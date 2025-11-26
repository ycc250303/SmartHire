<template>
  <view class="project-experience-page">
    <view v-if="loading" class="loading-container">
      <text class="loading-text">{{ t('pages.resume.edit.project.saving') }}</text>
    </view>

    <scroll-view v-else class="form-content" scroll-y>
      <view class="form-group">
        <view class="form-item">
          <text class="form-label">{{ t('pages.resume.edit.project.projectName') }}</text>
          <input
            class="form-input"
            v-model="formData.projectName"
            :placeholder="t('pages.resume.edit.project.projectNamePlaceholder')"
            placeholder-class="input-placeholder"
          />
        </view>

        <view class="form-item">
          <text class="form-label">{{ t('pages.resume.edit.project.projectRole') }}</text>
          <input
            class="form-input"
            v-model="formData.projectRole"
            :placeholder="t('pages.resume.edit.project.projectRolePlaceholder')"
            placeholder-class="input-placeholder"
          />
        </view>

        <view class="form-item">
          <text class="form-label">{{ t('pages.resume.edit.project.startMonth') }}</text>
          <picker
            class="form-picker"
            mode="date"
            fields="month"
            :value="formData.startMonth"
            @change="handleStartDateChange"
          >
            <view class="picker-value">
              <text :class="formData.startMonth ? 'picker-text' : 'picker-placeholder'">
                {{ formData.startMonth || t('pages.resume.edit.project.startMonth') }}
              </text>
              <text class="picker-arrow">›</text>
            </view>
          </picker>
        </view>

        <view class="form-item">
          <text class="form-label">{{ t('pages.resume.edit.project.endMonth') }}</text>
          <picker
            class="form-picker"
            mode="date"
            fields="month"
            :value="formData.endMoneth"
            @change="handleEndDateChange"
          >
            <view class="picker-value">
              <text :class="formData.endMoneth ? 'picker-text' : 'picker-placeholder'">
                {{ formData.endMoneth || t('pages.resume.edit.project.endMonth') }}
              </text>
              <text class="picker-arrow">›</text>
            </view>
          </picker>
        </view>
      </view>

      <view class="form-group">
        <view class="form-item form-item-full">
          <text class="form-label">{{ t('pages.resume.edit.project.description') }}</text>
          <textarea
            class="form-textarea"
            v-model="formData.description"
            :placeholder="t('pages.resume.edit.project.descriptionPlaceholder')"
            placeholder-class="input-placeholder"
            :maxlength="1000"
            :show-confirm-bar="false"
          />
        </view>

        <view class="form-item form-item-full">
          <text class="form-label">{{ t('pages.resume.edit.project.responsibility') }}</text>
          <textarea
            class="form-textarea"
            v-model="formData.responsibility"
            :placeholder="t('pages.resume.edit.project.responsibilityPlaceholder')"
            placeholder-class="input-placeholder"
            :maxlength="1000"
            :show-confirm-bar="false"
          />
        </view>
      </view>
    </scroll-view>

    <view class="bottom-bar">
      <view v-if="isEditMode" class="delete-button" @tap="handleDelete" :class="{ 'button-disabled': deleting }">
        <text class="button-text button-text-danger">
          {{ deleting ? t('pages.resume.edit.project.deleting') : t('pages.resume.edit.project.delete') }}
        </text>
      </view>
      <view class="save-button" @tap="handleSave" :class="{ 'button-disabled': saving }">
        <text class="button-text">
          {{ saving ? t('pages.resume.edit.project.saving') : t('pages.resume.edit.project.save') }}
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
import type { AddProjectExperienceParams } from '@/services/api/seeker';
import {
  addProjectExperience,
  updateProjectExperience,
  deleteProjectExperience,
  getProjectExperiences,
} from '@/services/api/seeker';

useNavigationTitle('navigation.editProjectExperience');

const loading = ref(false);
const saving = ref(false);
const deleting = ref(false);
const isEditMode = ref(false);
const projectId = ref<number>();

const formData = ref<AddProjectExperienceParams>({
  projectName: '',
  projectRole: '',
  description: '',
  responsibility: '',
  startMonth: '',
  endMoneth: '',
});

onLoad((options: any) => {
  if (options?.id) {
    projectId.value = parseInt(options.id);
    isEditMode.value = true;
    loadData();
  }
});

async function loadData() {
  if (!projectId.value) return;

  loading.value = true;
  try {
    const list = await getProjectExperiences();
    const item = list.find((proj) => proj.id === projectId.value);
    
    if (item) {
      formData.value = {
        projectName: item.projectName,
        projectRole: item.projectRole,
        description: item.description,
        responsibility: item.responsibility,
        startMonth: item.startMonth,
        endMoneth: item.endMoneth,
      };
    }
  } catch (error) {
    console.error('Failed to load project experience:', error);
    uni.showToast({
      title: t('pages.resume.edit.project.saveError'),
      icon: 'none',
    });
  } finally {
    loading.value = false;
  }
}

function handleStartDateChange(e: any) {
  formData.value.startMonth = e.detail.value;
}

function handleEndDateChange(e: any) {
  formData.value.endMoneth = e.detail.value;
}

async function handleSave() {
  if (saving.value) return;

  if (!formData.value.projectName || !formData.value.projectRole) {
    uni.showToast({
      title: t('pages.resume.edit.common.required'),
      icon: 'none',
    });
    return;
  }

  saving.value = true;

  try {
    if (isEditMode.value && projectId.value) {
      await updateProjectExperience(projectId.value, formData.value);
    } else {
      await addProjectExperience(formData.value);
    }

    uni.showToast({
      title: t('pages.resume.edit.project.saveSuccess'),
      icon: 'success',
    });

    setTimeout(() => {
      uni.navigateBack();
    }, 1500);
  } catch (error) {
    console.error('Save failed:', error);
    uni.showToast({
      title: t('pages.resume.edit.project.saveError'),
      icon: 'none',
    });
  } finally {
    saving.value = false;
  }
}

async function handleDelete() {
  if (deleting.value || !projectId.value) return;

  uni.showModal({
    title: t('pages.resume.edit.common.confirm'),
    content: t('pages.resume.edit.project.deleteConfirm'),
    success: async (res) => {
      if (res.confirm) {
        deleting.value = true;
        try {
          await deleteProjectExperience(projectId.value!);
          uni.showToast({
            title: t('pages.resume.edit.project.deleteSuccess'),
            icon: 'success',
          });

          setTimeout(() => {
            uni.navigateBack();
          }, 1500);
        } catch (error) {
          console.error('Delete failed:', error);
          uni.showToast({
            title: t('pages.resume.edit.project.deleteError'),
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

.project-experience-page {
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
  
  &.form-item-full {
    display: flex;
    flex-direction: column;
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

.form-textarea {
  font-size: 30rpx;
  color: #000000;
  line-height: 1.6;
  min-height: 160rpx;
  width: 100%;
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

.picker-placeholder {
  font-size: 30rpx;
  color: #C7C7CC;
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

