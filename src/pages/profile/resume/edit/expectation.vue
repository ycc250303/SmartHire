<template>
  <view class="expectation-page">
    <view v-if="loading" class="loading-container">
      <text class="loading-text">{{ t('pages.resume.edit.expectation.saving') }}</text>
    </view>

    <scroll-view v-else class="form-content" scroll-y>
      <view class="form-group">
        <view class="form-item">
          <text class="form-label">{{ t('pages.resume.edit.expectation.expectedPosition') }}</text>
          <input
            class="form-input"
            v-model="formData.expectedPosition"
            :placeholder="t('pages.resume.edit.expectation.expectedPositionPlaceholder')"
            placeholder-class="input-placeholder"
          />
        </view>

        <view class="form-item">
          <text class="form-label">{{ t('pages.resume.edit.expectation.expectedIndustry') }}</text>
          <input
            class="form-input"
            v-model="formData.expectedIndustry"
            :placeholder="t('pages.resume.edit.expectation.expectedIndustryPlaceholder')"
            placeholder-class="input-placeholder"
          />
        </view>

        <view class="form-item">
          <text class="form-label">{{ t('pages.resume.edit.expectation.workCity') }}</text>
          <input
            class="form-input"
            v-model="formData.workCity"
            :placeholder="t('pages.resume.edit.expectation.workCityPlaceholder')"
            placeholder-class="input-placeholder"
          />
        </view>

        <view class="form-item">
          <text class="form-label">{{ t('pages.resume.edit.expectation.salaryMin') }}</text>
          <input
            class="form-input"
            v-model.number="formData.salaryMin"
            type="number"
            :placeholder="t('pages.resume.edit.expectation.salaryMinPlaceholder')"
            placeholder-class="input-placeholder"
          />
        </view>

        <view class="form-item">
          <text class="form-label">{{ t('pages.resume.edit.expectation.salaryMax') }}</text>
          <input
            class="form-input"
            v-model.number="formData.salaryMax"
            type="number"
            :placeholder="t('pages.resume.edit.expectation.salaryMaxPlaceholder')"
            placeholder-class="input-placeholder"
          />
        </view>
      </view>
    </scroll-view>

    <view class="bottom-bar">
      <view v-if="isEditMode" class="delete-button" @tap="handleDelete" :class="{ 'button-disabled': deleting }">
        <text class="button-text button-text-danger">
          {{ deleting ? t('pages.resume.edit.expectation.deleting') : t('pages.resume.edit.expectation.delete') }}
        </text>
      </view>
      <view class="save-button" @tap="handleSave" :class="{ 'button-disabled': saving }">
        <text class="button-text">
          {{ saving ? t('pages.resume.edit.expectation.saving') : t('pages.resume.edit.expectation.save') }}
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
import type { AddJobSeekerExpectationParams } from '@/services/api/seeker';
import {
  addJobSeekerExpectation,
  updateJobSeekerExpectation,
  deleteJobSeekerExpectation,
  getJobSeekerExpectations,
} from '@/services/api/seeker';

useNavigationTitle('navigation.editExpectation');

const loading = ref(false);
const saving = ref(false);
const deleting = ref(false);
const isEditMode = ref(false);
const expectationId = ref<number>();

const formData = ref<AddJobSeekerExpectationParams>({
  expectedPosition: '',
  expectedIndustry: '',
  workCity: '',
  salaryMin: 0,
  salaryMax: 0,
});

onLoad((options: any) => {
  if (options?.id) {
    expectationId.value = parseInt(options.id);
    isEditMode.value = true;
    loadData();
  }
});

async function loadData() {
  if (!expectationId.value) return;

  loading.value = true;
  try {
    const list = await getJobSeekerExpectations();
    const item = list.find((exp) => exp.id === expectationId.value);
    
    if (item) {
      formData.value = {
        expectedPosition: item.expectedPosition,
        expectedIndustry: item.expectedIndustry,
        workCity: item.workCity,
        salaryMin: item.salaryMin,
        salaryMax: item.salaryMax,
      };
    }
  } catch (error) {
    console.error('Failed to load expectation:', error);
    uni.showToast({
      title: t('pages.resume.edit.expectation.saveError'),
      icon: 'none',
    });
  } finally {
    loading.value = false;
  }
}

async function handleSave() {
  if (saving.value) return;

  if (!formData.value.expectedPosition || !formData.value.workCity) {
    uni.showToast({
      title: t('pages.resume.edit.common.required'),
      icon: 'none',
    });
    return;
  }

  saving.value = true;

  try {
    if (isEditMode.value && expectationId.value) {
      await updateJobSeekerExpectation(expectationId.value, formData.value);
    } else {
      await addJobSeekerExpectation(formData.value);
    }

    uni.showToast({
      title: t('pages.resume.edit.expectation.saveSuccess'),
      icon: 'success',
    });

    setTimeout(() => {
      uni.navigateBack();
    }, 1500);
  } catch (error) {
    console.error('Save failed:', error);
    uni.showToast({
      title: t('pages.resume.edit.expectation.saveError'),
      icon: 'none',
    });
  } finally {
    saving.value = false;
  }
}

async function handleDelete() {
  if (deleting.value || !expectationId.value) return;

  uni.showModal({
    title: t('pages.resume.edit.common.confirm'),
    content: t('pages.resume.edit.expectation.deleteConfirm'),
    success: async (res) => {
      if (res.confirm) {
        deleting.value = true;
        try {
          await deleteJobSeekerExpectation(expectationId.value!);
          uni.showToast({
            title: t('pages.resume.edit.expectation.deleteSuccess'),
            icon: 'success',
          });

          setTimeout(() => {
            uni.navigateBack();
          }, 1500);
        } catch (error) {
          console.error('Delete failed:', error);
          uni.showToast({
            title: t('pages.resume.edit.expectation.deleteError'),
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

.expectation-page {
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

