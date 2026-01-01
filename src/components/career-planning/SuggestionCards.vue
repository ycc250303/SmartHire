<template>
  <view class="suggestions-section">
    <text class="section-title">{{ title }}</text>
    <view class="suggestions-list">
      <view 
        v-for="(suggestion, index) in immediateSuggestions" 
        :key="index"
        class="suggestion-card"
      >
        <view class="suggestion-icon">📌</view>
        <view class="suggestion-content">
          <text class="suggestion-title">{{ suggestion.title }}</text>
          <text class="suggestion-description">{{ suggestion.description }}</text>
        </view>
        <view class="suggestion-action" @click="handleAction(index)">
          <text class="action-text">{{ actionText }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { t } from '@/locales';

interface ImmediateSuggestion {
  title: string;
  description: string;
}

interface Props {
  immediateSuggestions: ImmediateSuggestion[];
  title?: string;
  actionText?: string;
}

const props = withDefaults(defineProps<Props>(), {
  title: () => t('pages.jobs.careerPlanningPage.immediateSuggestions'),
  actionText: () => t('pages.jobs.careerPlanningPage.startAction'),
});

const emit = defineEmits<{
  (e: 'action', index: number): void;
}>();

function handleAction(index: number) {
  emit('action', index);
}
</script>

<style lang="scss" scoped>
@use "@/styles/variables.scss" as vars;

.suggestions-section {
  background: vars.$surface-color;
  border-radius: 32rpx;
  padding: vars.$spacing-lg;
  margin-bottom: vars.$spacing-md;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);
}

.section-title {
  font-size: 34rpx;
  font-weight: 600;
  color: vars.$text-color;
  display: block;
  margin-bottom: vars.$spacing-lg;
}

.suggestions-list {
  display: flex;
  flex-direction: column;
  gap: vars.$spacing-md;
}

.suggestion-card {
  background: vars.$surface-color;
  border-radius: 24rpx;
  padding: vars.$spacing-lg;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
  display: flex;
  gap: vars.$spacing-md;
  align-items: flex-start;
}

.suggestion-icon {
  font-size: 32rpx;
  flex-shrink: 0;
  margin-top: 4rpx;
}

.suggestion-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: vars.$spacing-xs;
}

.suggestion-title {
  font-size: 28rpx;
  font-weight: 600;
  color: vars.$text-color;
  display: block;
}

.suggestion-description {
  font-size: 24rpx;
  color: vars.$text-muted;
  line-height: 1.6;
  display: block;
}

.suggestion-action {
  flex-shrink: 0;
  padding: 8rpx 20rpx;
  border-radius: 999rpx;
  border: 2rpx solid #007AFF;
  background: transparent;
}

.action-text {
  font-size: 24rpx;
  color: #007AFF;
  font-weight: 500;
}

.suggestion-action:active {
  opacity: 0.6;
  transform: scale(0.95);
}
</style>

