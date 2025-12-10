<template>
  <view class="skill-page">
    <view v-if="loading" class="loading-container">
      <text class="loading-text">{{ t('pages.resume.edit.skill.saving') }}</text>
    </view>

    <scroll-view v-else class="form-content" scroll-y>
      <view class="form-group">
        <view class="form-item">
          <text class="form-label">{{ t('pages.resume.edit.skill.skillName') }}</text>
          <input
            class="form-input"
            v-model="formData.skillName"
            :placeholder="t('pages.resume.edit.skill.skillNamePlaceholder')"
            placeholder-class="input-placeholder"
          />
        </view>

        <view class="form-item form-item-slider">
          <view class="slider-label-row">
            <text class="form-label">{{ t('pages.resume.edit.skill.level') }}</text>
            <text class="level-value">{{ t('pages.resume.edit.skill.levelLabel') }} {{ formData.level }}</text>
          </view>
          <slider
            class="skill-slider"
            :value="formData.level"
            :min="1"
            :max="5"
            :step="1"
            :show-value="false"
            @change="handleSliderChange"
            activeColor="#4ba3ff"
            backgroundColor="#E5E5EA"
            block-size="28"
          />
          <view class="slider-labels">
            <text class="slider-label-text">1</text>
            <text class="slider-label-text">2</text>
            <text class="slider-label-text">3</text>
            <text class="slider-label-text">4</text>
            <text class="slider-label-text">5</text>
          </view>
        </view>
      </view>
    </scroll-view>

    <view class="bottom-bar">
      <view v-if="isEditMode" class="delete-button" @tap="handleDelete" :class="{ 'button-disabled': deleting }">
        <text class="button-text button-text-danger">
          {{ deleting ? t('pages.resume.edit.skill.deleting') : t('pages.resume.edit.skill.delete') }}
        </text>
      </view>
      <view class="save-button" @tap="handleSave" :class="{ 'button-disabled': saving }">
        <text class="button-text">
          {{ saving ? t('pages.resume.edit.skill.saving') : t('pages.resume.edit.skill.save') }}
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
import type { AddSkillParams } from '@/services/api/seeker';
import {
  addSkill,
  updateSkill,
  deleteSkill,
  getSkills,
} from '@/services/api/seeker';

useNavigationTitle('navigation.editSkill');

const loading = ref(false);
const saving = ref(false);
const deleting = ref(false);
const isEditMode = ref(false);
const skillId = ref<number>();

const formData = ref<AddSkillParams>({
  skillName: '',
  level: 3,
});

onLoad((options: any) => {
  if (options?.id) {
    skillId.value = parseInt(options.id);
    isEditMode.value = true;
    loadData();
  }
});

async function loadData() {
  if (!skillId.value) return;

  loading.value = true;
  try {
    const list = await getSkills();
    const item = list.find((skill) => skill.id === skillId.value);
    
    if (item) {
      formData.value = {
        skillName: item.skillName,
        level: item.level,
      };
    }
  } catch (error) {
    console.error('Failed to load skill:', error);
    uni.showToast({
      title: t('pages.resume.edit.skill.saveError'),
      icon: 'none',
    });
  } finally {
    loading.value = false;
  }
}

function handleSliderChange(e: any) {
  formData.value.level = e.detail.value;
}

async function handleSave() {
  if (saving.value) return;

  if (!formData.value.skillName) {
    uni.showToast({
      title: t('pages.resume.edit.common.required'),
      icon: 'none',
    });
    return;
  }

  saving.value = true;

  try {
    if (isEditMode.value && skillId.value) {
      await updateSkill(skillId.value, formData.value);
    } else {
      await addSkill(formData.value);
    }

    uni.showToast({
      title: t('pages.resume.edit.skill.saveSuccess'),
      icon: 'success',
    });

    setTimeout(() => {
      uni.navigateBack();
    }, 1500);
  } catch (error) {
    console.error('Save failed:', error);
    uni.showToast({
      title: t('pages.resume.edit.skill.saveError'),
      icon: 'none',
    });
  } finally {
    saving.value = false;
  }
}

async function handleDelete() {
  if (deleting.value || !skillId.value) return;

  uni.showModal({
    title: t('pages.resume.edit.common.confirm'),
    content: t('pages.resume.edit.skill.deleteConfirm'),
    success: async (res) => {
      if (res.confirm) {
        deleting.value = true;
        try {
          await deleteSkill(skillId.value!);
          uni.showToast({
            title: t('pages.resume.edit.skill.deleteSuccess'),
            icon: 'success',
          });

          setTimeout(() => {
            uni.navigateBack();
          }, 1500);
        } catch (error) {
          console.error('Delete failed:', error);
          uni.showToast({
            title: t('pages.resume.edit.skill.deleteError'),
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

.skill-page {
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
  
  &.form-item-slider {
    display: flex;
    flex-direction: column;
    gap: 24rpx;
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

.slider-label-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0;
}

.level-value {
  font-size: 28rpx;
  color: vars.$primary-color;
  font-weight: 600;
}

.skill-slider {
  width: 100%;
  margin: 8rpx 0;
}

.slider-labels {
  display: flex;
  justify-content: space-between;
  padding: 0 4rpx;
}

.slider-label-text {
  font-size: 24rpx;
  color: #8E8E93;
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

