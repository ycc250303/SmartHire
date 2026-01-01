<template>
  <view class="overview-card">
    <view class="overview-header">
      <text class="overview-title">{{ title }}</text>
      <view class="overview-badges">
        <view class="badge badge-blue" v-if="planDuration">
          <text class="badge-text">{{ planDuration }}{{ daysText }}{{ t('pages.jobs.careerPlanningPage.plan') }}</text>
        </view>
        <view class="badge badge-green" v-if="milestones">
          <text class="badge-text">🏆 {{ milestones }}{{ milestonesText }}</text>
        </view>
      </view>
    </view>
    
    <text class="target-line">{{ t('pages.jobs.careerPlanningPage.target') }}：{{ targetPosition }}</text>
    <text class="current-line">{{ currentLevel }}</text>
    
    <view class="progress-section" v-if="overallScore !== undefined">
      <ProgressBar :percentage="overallScore" />
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { t } from '@/locales';
import ProgressBar from '@/components/common/ProgressBar.vue';

interface Props {
  targetPosition: string;
  currentLevel: string;
  skillGapsCount?: number;
  planDurationDays?: number;
  milestonesCount?: number;
  overallScore?: number;
  title?: string;
}

const props = withDefaults(defineProps<Props>(), {
  title: () => t('pages.jobs.careerPlanningPage.developmentRoute'),
});

const planDuration = computed(() => {
  if (props.planDurationDays) {
    return props.planDurationDays;
  }
  return props.skillGapsCount ? props.skillGapsCount * 30 : null;
});

const milestones = computed(() => props.milestonesCount || props.skillGapsCount);

const daysText = computed(() => t('pages.jobs.careerPlanningPage.days'));
const milestonesText = computed(() => t('pages.jobs.careerPlanningPage.milestones'));
</script>

<style lang="scss" scoped>
@use "@/styles/variables.scss" as vars;

.overview-card {
  background: linear-gradient(to bottom, #EBF5FF, vars.$surface-color);
  border-radius: vars.$border-radius;
  padding: vars.$spacing-lg;
  margin: vars.$spacing-md;
  margin-bottom: vars.$spacing-md;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);
}

.overview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: vars.$spacing-md;
}

.overview-title {
  font-size: 32rpx;
  font-weight: 600;
  color: vars.$text-color;
}

.overview-badges {
  display: flex;
  gap: vars.$spacing-sm;
}

.badge {
  padding: 6rpx 16rpx;
  border-radius: 999rpx;
  
  &.badge-blue {
    background-color: vars.$primary-color-soft;
  }
  
  &.badge-green {
    background-color: rgba(52, 199, 89, 0.1);
  }
}

.badge-text {
  font-size: 22rpx;
  font-weight: 500;
  
  .badge-blue & {
    color: vars.$primary-color;
  }
  
  .badge-green & {
    color: #34C759;
  }
}

.target-line {
  font-size: 28rpx;
  color: vars.$text-color;
  display: block;
  margin-bottom: vars.$spacing-sm;
}

.current-line {
  font-size: 24rpx;
  color: vars.$text-muted;
  display: block;
  margin-bottom: vars.$spacing-md;
}

.progress-section {
  margin-top: vars.$spacing-md;
  
  :deep(.progress-bar) {
    height: 12rpx;
  }
}
</style>

