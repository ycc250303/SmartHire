<template>
  <view class="education-experience-page">
    <view v-if="loading" class="loading-container">
      <text class="loading-text">{{ t('pages.resume.edit.education.saving') }}</text>
    </view>

    <scroll-view v-else class="form-content" scroll-y>
      <view class="form-group">
        <view class="form-item">
          <text class="form-label">{{ t('pages.resume.edit.education.schoolName') }}</text>
          <view class="picker-trigger" @tap="openUniversitySelector">
            <text :class="formData.schoolName ? 'picker-text' : 'picker-placeholder'">
              {{ formData.schoolName || t('pages.resume.edit.education.schoolNamePlaceholder') }}
            </text>
            <text class="picker-arrow">›</text>
          </view>
        </view>

        <view class="form-item">
          <text class="form-label">{{ t('pages.resume.edit.education.major') }}</text>
          <view class="picker-trigger" @tap="openMajorSelector">
            <text :class="formData.major ? 'picker-text' : 'picker-placeholder'">
              {{ formData.major || t('pages.resume.edit.education.majorPlaceholder') }}
            </text>
            <text class="picker-arrow">›</text>
          </view>
        </view>

        <view class="form-item">
          <text class="form-label">{{ t('pages.resume.edit.education.education') }}</text>
          <picker
            class="form-picker"
            mode="selector"
            :range="educationOptions"
            :range-key="'label'"
            :value="educationIndex"
            @change="handleEducationChange"
          >
            <view class="picker-value">
              <text :class="formData.education !== undefined ? 'picker-text' : 'picker-placeholder'">
                {{ educationOptions[educationIndex]?.label || t('pages.resume.edit.education.education') }}
              </text>
              <text class="picker-arrow">›</text>
            </view>
          </picker>
        </view>

        <view class="form-item">
          <text class="form-label">{{ t('pages.resume.edit.education.startYear') }}</text>
          <picker
            class="form-picker"
            mode="date"
            fields="year"
            :value="formData.startYear"
            @change="handleStartYearChange"
          >
            <view class="picker-value">
              <text :class="formData.startYear ? 'picker-text' : 'picker-placeholder'">
                {{ formData.startYear || t('pages.resume.edit.education.startYear') }}
              </text>
              <text class="picker-arrow">›</text>
            </view>
          </picker>
        </view>

        <view class="form-item">
          <text class="form-label">{{ t('pages.resume.edit.education.endYear') }}</text>
          <picker
            class="form-picker"
            mode="date"
            fields="year"
            :value="formData.endYear"
            @change="handleEndYearChange"
          >
            <view class="picker-value">
              <text :class="formData.endYear ? 'picker-text' : 'picker-placeholder'">
                {{ formData.endYear || t('pages.resume.edit.education.endYear') }}
              </text>
              <text class="picker-arrow">›</text>
            </view>
          </picker>
        </view>

        <view class="form-item">
          <text class="form-label">{{ t('pages.resume.edit.education.isCurrent') }}</text>
          <picker
            class="form-picker"
            mode="selector"
            :range="isCurrentOptions"
            :range-key="'label'"
            :value="isCurrentIndex"
            @change="handleIsCurrentChange"
          >
            <view class="picker-value">
              <text :class="formData.isCurrent !== undefined ? 'picker-text' : 'picker-placeholder'">
                {{ isCurrentOptions[isCurrentIndex]?.label || t('pages.resume.edit.education.isCurrent') }}
              </text>
              <text class="picker-arrow">›</text>
            </view>
          </picker>
        </view>
      </view>
    </scroll-view>

    <view class="bottom-bar">
      <view v-if="isEditMode" class="delete-button" @tap="handleDelete" :class="{ 'button-disabled': deleting }">
        <text class="button-text button-text-danger">
          {{ deleting ? t('pages.resume.edit.education.deleting') : t('pages.resume.edit.education.delete') }}
        </text>
      </view>
      <view class="save-button" @tap="handleSave" :class="{ 'button-disabled': saving }">
        <text class="button-text">
          {{ saving ? t('pages.resume.edit.education.saving') : t('pages.resume.edit.education.save') }}
        </text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { onLoad, onShow } from '@dcloudio/uni-app';
import { t } from '@/locales';
import { useNavigationTitle } from '@/utils/useNavigationTitle';
import type { AddEducationExperienceParams } from '@/services/api/seeker';
import {
  addEducationExperience,
  updateEducationExperience,
  deleteEducationExperience,
  getEducationExperiences,
} from '@/services/api/seeker';

useNavigationTitle('navigation.editEducationExperience');

const loading = ref(false);
const saving = ref(false);
const deleting = ref(false);
const isEditMode = ref(false);
const educationId = ref<number>();

const formData = ref<AddEducationExperienceParams>({
  schoolName: '',
  major: '',
  education: 2,
  startYear: '',
  endYear: '',
  isCurrent: 0,
});

const educationOptions = [
  { label: t('degree.highSchoolOrBelow'), value: 0 },
  { label: t('degree.associateDegree'), value: 1 },
  { label: t('degree.bachelorDegree'), value: 2 },
  { label: t('degree.masterDegree'), value: 3 },
  { label: t('degree.doctorDegree'), value: 4 },
];

const educationIndex = computed(() => {
  if (formData.value.education === undefined || formData.value.education === null) {
    return 2;
  }
  return formData.value.education;
});

const isCurrentOptions = [
  { label: t('pages.resume.edit.education.isCurrentNo'), value: 0 },
  { label: t('pages.resume.edit.education.isCurrentYes'), value: 1 },
];

const isCurrentIndex = computed(() => {
  return formData.value.isCurrent === 1 ? 1 : 0;
});

onLoad((options: any) => {
  if (options?.id) {
    educationId.value = parseInt(options.id);
    isEditMode.value = true;
    loadData();
  }
});

const selectedUniversity = ref<string | null>(null);
const selectedMajor = ref<string | null>(null);

onShow(() => {
  if (selectedUniversity.value) {
    formData.value.schoolName = selectedUniversity.value;
    selectedUniversity.value = null;
  }
  if (selectedMajor.value) {
    formData.value.major = selectedMajor.value;
    selectedMajor.value = null;
  }
});

function openUniversitySelector() {
  uni.navigateTo({
    url: '/pages/seeker/profile/resume/edit/university-selector',
    events: {
      selectUniversity: (universityName: string) => {
        selectedUniversity.value = universityName;
      },
    },
  });
}

function openMajorSelector() {
  uni.navigateTo({
    url: '/pages/seeker/profile/resume/edit/major-selector',
    events: {
      selectMajor: (majorName: string) => {
        selectedMajor.value = majorName;
      },
    },
  });
}

async function loadData() {
  if (!educationId.value) return;

  loading.value = true;
  try {
    const list = await getEducationExperiences();
    const item = list.find((edu) => edu.id === educationId.value);
    
    if (item) {
      formData.value = {
        schoolName: item.schoolName,
        major: item.major,
        education: item.education,
        startYear: item.startYear,
        endYear: item.endYear,
        isCurrent: item.isCurrent,
      };
    }
  } catch (error) {
    console.error('Failed to load education experience:', error);
    uni.showToast({
      title: t('pages.resume.edit.education.saveError'),
      icon: 'none',
    });
  } finally {
    loading.value = false;
  }
}

function handleEducationChange(e: any) {
  formData.value.education = parseInt(e.detail.value);
}

function handleStartYearChange(e: any) {
  const date = e.detail.value;
  formData.value.startYear = date.substring(0, 4);
}

function handleEndYearChange(e: any) {
  const date = e.detail.value;
  formData.value.endYear = date.substring(0, 4);
}

function handleIsCurrentChange(e: any) {
  formData.value.isCurrent = parseInt(e.detail.value);
}

async function handleSave() {
  if (saving.value) return;

  if (!formData.value.schoolName || !formData.value.major || !formData.value.startYear) {
    uni.showToast({
      title: t('pages.resume.edit.common.required'),
      icon: 'none',
    });
    return;
  }

  if (!formData.value.isCurrent && formData.value.startYear && formData.value.endYear && formData.value.startYear > formData.value.endYear) {
    uni.showToast({
      title: t('pages.resume.edit.education.dateInvalid'),
      icon: 'none',
    });
    return;
  }

  saving.value = true;

  try {
    if (isEditMode.value && educationId.value) {
      await updateEducationExperience(educationId.value, formData.value);
    } else {
      await addEducationExperience(formData.value);
    }

    uni.showToast({
      title: t('pages.resume.edit.education.saveSuccess'),
      icon: 'success',
    });

    setTimeout(() => {
      uni.navigateBack();
    }, 1500);
  } catch (error) {
    console.error('Save failed:', error);
    uni.showToast({
      title: t('pages.resume.edit.education.saveError'),
      icon: 'none',
    });
  } finally {
    saving.value = false;
  }
}

async function handleDelete() {
  if (deleting.value || !educationId.value) return;

  uni.showModal({
    title: t('pages.resume.edit.common.confirm'),
    content: t('pages.resume.edit.education.deleteConfirm'),
    success: async (res) => {
      if (res.confirm) {
        deleting.value = true;
        try {
          await deleteEducationExperience(educationId.value!);
          uni.showToast({
            title: t('pages.resume.edit.education.deleteSuccess'),
            icon: 'success',
          });

          setTimeout(() => {
            uni.navigateBack();
          }, 1500);
        } catch (error) {
          console.error('Delete failed:', error);
          uni.showToast({
            title: t('pages.resume.edit.education.deleteError'),
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

.education-experience-page {
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

.picker-trigger {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
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

