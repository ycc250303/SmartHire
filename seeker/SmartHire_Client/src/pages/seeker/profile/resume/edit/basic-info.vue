<template>
  <view class="basic-info-page">
    <view v-if="loading" class="loading-container">
      <text class="loading-text">{{ t('pages.resume.edit.basicInfo.saving') }}</text>
    </view>

    <scroll-view v-else class="form-content" scroll-y>
      <view class="form-group">
        <view class="form-item">
          <text class="form-label">{{ t('pages.resume.edit.basicInfo.realName') }}</text>
          <input
            class="form-input"
            v-model="formData.realName"
            :placeholder="t('pages.resume.edit.basicInfo.realNamePlaceholder')"
            placeholder-class="input-placeholder"
          />
        </view>

        <view class="form-item">
          <text class="form-label">{{ t('pages.resume.edit.basicInfo.birthDate') }}</text>
          <picker
            class="form-picker"
            mode="date"
            :value="formData.birthDate"
            @change="handleDateChange"
          >
            <view class="picker-value">
              <text :class="formData.birthDate ? 'picker-text' : 'picker-placeholder'">
                {{ formData.birthDate || t('pages.resume.edit.basicInfo.birthDate') }}
              </text>
              <text class="picker-arrow">›</text>
            </view>
          </picker>
        </view>

        <view class="form-item">
          <text class="form-label">{{ t('pages.resume.edit.basicInfo.city') }}</text>
          <CityPicker
            v-model="formData.currentCity"
            :placeholder="t('pages.resume.edit.basicInfo.cityPlaceholder')"
          />
        </view>
        
        <view class="form-item">
          <text class="form-label">求职状态</text>
          <picker
            class="form-picker"
            mode="selector"
            :range="jobStatusOptions"
            :range-key="'label'"
            :value="jobStatusIndex"
            @change="handleJobStatusChange"
          >
            <view class="picker-value">
              <text :class="formData.jobStatus !== undefined ? 'picker-text' : 'picker-placeholder'">
                {{ jobStatusOptions[jobStatusIndex]?.label || '求职状态' }}
              </text>
              <text class="picker-arrow">›</text>
            </view>
          </picker>
        </view>
      </view>
    </scroll-view>

    <view class="bottom-bar">
      <view class="save-button" @tap="handleSave" :class="{ 'button-disabled': saving }">
        <text class="button-text">
          {{ saving ? t('pages.resume.edit.basicInfo.saving') : t('pages.resume.edit.basicInfo.save') }}
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
import type { SeekerInfo } from '@/services/api/seeker';
import { getSeekerInfo, updateSeekerInfo } from '@/services/api/seeker';
import CityPicker from '@/components/common/CityPicker.vue';

useNavigationTitle('navigation.editBasicInfo');

const loading = ref(true);
const saving = ref(false);

const formData = ref<Partial<SeekerInfo>>({
  realName: undefined,
  birthDate: undefined,
  currentCity: undefined,
  jobStatus: undefined,
});

const jobStatusOptions = [
  { label: '离校-尽快到岗', value: 0 },
  { label: '在校-尽快到岗', value: 1 },
  { label: '在校-考虑机会', value: 2 },
  { label: '在校-暂不考虑', value: 3 },
];

const jobStatusIndex = computed(() => {
  if (formData.value.jobStatus === undefined || formData.value.jobStatus === null) {
    return 0;
  }
  return formData.value.jobStatus;
});

onLoad(() => {
  loadData();
});

function formatDateForPicker(dateString: string | undefined): string | undefined {
  if (!dateString) return undefined;
  const date = new Date(dateString);
  if (isNaN(date.getTime())) return undefined;
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

async function loadData() {
  loading.value = true;
  try {
    const data = await getSeekerInfo();
    if (data) {
      formData.value = {
        realName: data.realName || undefined,
        birthDate: formatDateForPicker(data.birthDate),
        currentCity: data.currentCity || undefined,
        jobStatus: data.jobStatus !== undefined ? data.jobStatus : 0,
      };
    } else {
      formData.value.jobStatus = 0;
    }
  } catch (error) {
    console.error('Failed to load seeker info:', error);
    formData.value.jobStatus = 0;
  } finally {
    loading.value = false;
  }
}

function handleJobStatusChange(e: any) {
  formData.value.jobStatus = parseInt(e.detail.value);
}

function handleDateChange(e: any) {
  formData.value.birthDate = e.detail.value;
}

async function handleSave() {
  if (saving.value) return;

  if (!formData.value.realName) {
    uni.showToast({
      title: t('pages.resume.edit.common.required'),
      icon: 'none',
    });
    return;
  }

  saving.value = true;

  try {
    await updateSeekerInfo({
      realName: formData.value.realName!,
      birthDate: formData.value.birthDate || '',
      currentCity: formData.value.currentCity || '',
      jobStatus: formData.value.jobStatus ?? 0,
    });

    uni.showToast({
      title: t('pages.resume.edit.basicInfo.saveSuccess'),
      icon: 'success',
    });

    setTimeout(() => {
      uni.navigateBack();
    }, 1500);
  } catch (error) {
    console.error('Save failed:', error);
    uni.showToast({
      title: t('pages.resume.edit.basicInfo.saveError'),
      icon: 'none',
    });
  } finally {
    saving.value = false;
  }
}
</script>

<style lang="scss" scoped>
@use "@/styles/variables.scss" as vars;

.basic-info-page {
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
  min-height: 200rpx;
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

