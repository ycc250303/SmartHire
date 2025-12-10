<template>
  <view class="work-experience-page">
    <view v-if="loading" class="loading-container">
      <text class="loading-text">{{ t('pages.resume.edit.work.saving') }}</text>
    </view>

    <scroll-view v-else class="form-content" scroll-y>
      <view class="form-group">
        <view class="form-item">
          <text class="form-label">{{ t('pages.resume.edit.work.companyName') }}</text>
          <input
            class="form-input"
            v-model="formData.companyName"
            :placeholder="t('pages.resume.edit.work.companyNamePlaceholder')"
            placeholder-class="input-placeholder"
          />
        </view>

        <view class="form-item">
          <text class="form-label">{{ t('pages.resume.edit.work.position') }}</text>
          <input
            class="form-input"
            v-model="formData.position"
            :placeholder="t('pages.resume.edit.work.positionPlaceholder')"
            placeholder-class="input-placeholder"
          />
        </view>

        <view class="form-item">
          <text class="form-label">{{ t('pages.resume.edit.work.department') }}</text>
          <input
            class="form-input"
            v-model="formData.department"
            :placeholder="t('pages.resume.edit.work.departmentPlaceholder')"
            placeholder-class="input-placeholder"
          />
        </view>

        <view class="form-item">
          <text class="form-label">{{ t('pages.resume.edit.work.startMonth') }}</text>
          <picker
            class="form-picker"
            mode="date"
            fields="month"
            :value="formData.startMonth"
            @change="handleStartDateChange"
          >
            <view class="picker-value">
              <text :class="formData.startMonth ? 'picker-text' : 'picker-placeholder'">
                {{ formData.startMonth || t('pages.resume.edit.work.startMonth') }}
              </text>
              <text class="picker-arrow">›</text>
            </view>
          </picker>
        </view>

        <view class="form-item">
          <text class="form-label">{{ t('pages.resume.edit.work.endMonth') }}</text>
          <picker
            class="form-picker"
            mode="date"
            fields="month"
            :value="formData.endMonth"
            @change="handleEndDateChange"
          >
            <view class="picker-value">
              <text :class="formData.endMonth ? 'picker-text' : 'picker-placeholder'">
                {{ formData.endMonth || t('pages.resume.edit.work.endMonth') }}
              </text>
              <text class="picker-arrow">›</text>
            </view>
          </picker>
        </view>

        <view class="form-item">
          <text class="form-label">{{ t('pages.resume.edit.work.isInternship') }}</text>
          <picker
            class="form-picker"
            mode="selector"
            :range="workTypeOptions"
            :range-key="'label'"
            :value="workTypeIndex"
            @change="handleWorkTypeChange"
          >
            <view class="picker-value">
              <text :class="formData.isInternship !== undefined ? 'picker-text' : 'picker-placeholder'">
                {{ workTypeOptions[workTypeIndex]?.label || t('pages.resume.edit.work.isInternship') }}
              </text>
              <text class="picker-arrow">›</text>
            </view>
          </picker>
        </view>
      </view>

      <view class="form-group">
        <view class="form-item form-item-full">
          <text class="form-label">{{ t('pages.resume.edit.work.description') }}</text>
          <textarea
            class="form-textarea"
            v-model="formData.description"
            :placeholder="t('pages.resume.edit.work.descriptionPlaceholder')"
            placeholder-class="input-placeholder"
            :maxlength="1000"
            :show-confirm-bar="false"
          />
        </view>

        <view class="form-item form-item-full">
          <text class="form-label">{{ t('pages.resume.edit.work.achievements') }}</text>
          <textarea
            class="form-textarea"
            v-model="formData.achievements"
            :placeholder="t('pages.resume.edit.work.achievementsPlaceholder')"
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
          {{ deleting ? t('pages.resume.edit.work.deleting') : t('pages.resume.edit.work.delete') }}
        </text>
      </view>
      <view class="save-button" @tap="handleSave" :class="{ 'button-disabled': saving }">
        <text class="button-text">
          {{ saving ? t('pages.resume.edit.work.saving') : t('pages.resume.edit.work.save') }}
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
import type { AddWorkExperienceParams } from '@/services/api/seeker';
import {
  addWorkExperience,
  updateWorkExperience,
  deleteWorkExperience,
  getWorkExperiences,
} from '@/services/api/seeker';

useNavigationTitle('navigation.editWorkExperience');

const loading = ref(false);
const saving = ref(false);
const deleting = ref(false);
const isEditMode = ref(false);
const workId = ref<number>();

const formData = ref<AddWorkExperienceParams>({
  companyName: '',
  position: '',
  department: '',
  startMonth: '',
  endMonth: '',
  description: '',
  achievements: '',
  isInternship: 0,
});

const workTypeOptions = [
  { label: t('pages.resume.edit.work.isInternshipFulltime'), value: 0 },
  { label: t('pages.resume.edit.work.isInternshipInternship'), value: 1 },
];

const workTypeIndex = computed(() => {
  return formData.value.isInternship === 1 ? 1 : 0;
});

onLoad((options: any) => {
  if (options?.id) {
    workId.value = parseInt(options.id);
    isEditMode.value = true;
    loadData();
  }
});

async function loadData() {
  if (!workId.value) return;

  loading.value = true;
  try {
    const list = await getWorkExperiences();
    const item = list.find((work) => work.id === workId.value);
    
    if (item) {
      formData.value = {
        companyName: item.companyName,
        position: item.position,
        department: item.department,
        startMonth: item.startMonth,
        endMonth: item.endMonth,
        description: item.description,
        achievements: item.achievements,
        isInternship: item.isInternship,
      };
    }
  } catch (error) {
    console.error('Failed to load work experience:', error);
    uni.showToast({
      title: t('pages.resume.edit.work.saveError'),
      icon: 'none',
    });
  } finally {
    loading.value = false;
  }
}

function handleStartDateChange(e: any) {
  const date = e.detail.value;
  formData.value.startMonth = date.substring(0, 7);
}

function handleEndDateChange(e: any) {
  const date = e.detail.value;
  formData.value.endMonth = date.substring(0, 7);
}

function handleWorkTypeChange(e: any) {
  formData.value.isInternship = parseInt(e.detail.value);
}

async function handleSave() {
  if (saving.value) return;

  if (!formData.value.companyName || !formData.value.position || !formData.value.startMonth) {
    uni.showToast({
      title: t('pages.resume.edit.common.required'),
      icon: 'none',
    });
    return;
  }

  if (formData.value.startMonth && formData.value.endMonth && formData.value.startMonth > formData.value.endMonth) {
    uni.showToast({
      title: t('pages.resume.edit.work.dateInvalid'),
      icon: 'none',
    });
    return;
  }

  saving.value = true;

  try {
    if (isEditMode.value && workId.value) {
      await updateWorkExperience(workId.value, formData.value);
    } else {
      await addWorkExperience(formData.value);
    }

    uni.showToast({
      title: t('pages.resume.edit.work.saveSuccess'),
      icon: 'success',
    });

    setTimeout(() => {
      uni.navigateBack();
    }, 1500);
  } catch (error) {
    console.error('Save failed:', error);
    uni.showToast({
      title: t('pages.resume.edit.work.saveError'),
      icon: 'none',
    });
  } finally {
    saving.value = false;
  }
}

async function handleDelete() {
  if (deleting.value || !workId.value) return;

  uni.showModal({
    title: t('pages.resume.edit.common.confirm'),
    content: t('pages.resume.edit.work.deleteConfirm'),
    success: async (res) => {
      if (res.confirm) {
        deleting.value = true;
        try {
          await deleteWorkExperience(workId.value!);
          uni.showToast({
            title: t('pages.resume.edit.work.deleteSuccess'),
            icon: 'success',
          });

          setTimeout(() => {
            uni.navigateBack();
          }, 1500);
        } catch (error) {
          console.error('Delete failed:', error);
          uni.showToast({
            title: t('pages.resume.edit.work.deleteError'),
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

.work-experience-page {
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

