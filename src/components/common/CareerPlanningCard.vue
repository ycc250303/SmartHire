<template>
  <view class="career-planning-card" @click="handleCardClick">
    <view class="card-content">
      <view class="icon-wrapper">
        <view class="icon-circle">
          <view class="icon-chart">
            <view class="chart-bar chart-bar-1"></view>
            <view class="chart-bar chart-bar-2"></view>
            <view class="chart-bar chart-bar-3"></view>
          </view>
        </view>
      </view>
      <view class="card-info">
        <text class="card-description">{{ t('pages.jobs.jobDetail.careerPlanningDescription') }}</text>
      </view>
      <view class="arrow-wrapper">
        <text class="arrow">›</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { t } from '@/locales';

interface Props {
  jobId: number;
}

const props = defineProps<Props>();

function handleCardClick() {
  uni.navigateTo({
    url: `/pages/seeker/jobs/career-planning?jobId=${props.jobId}`
  });
}
</script>

<style lang="scss" scoped>
@use "@/styles/variables.scss" as vars;

.career-planning-card {
  background-color: vars.$bg-color;
  border-radius: vars.$border-radius-sm;
  padding: vars.$spacing-md;
  margin-top: vars.$spacing-md;
  cursor: pointer;
  transition: all 0.2s;
  
  &:active {
    background-color: #E8EDF5;
    transform: scale(0.98);
  }
}

.card-content {
  display: flex;
  align-items: center;
  gap: vars.$spacing-md;
}

.icon-wrapper {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon-circle {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  background-color: vars.$primary-color-soft;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.icon-chart {
  display: flex;
  align-items: flex-end;
  gap: 4rpx;
  height: 32rpx;
}

.chart-bar {
  background-color: vars.$primary-color;
  border-radius: 2rpx;
  width: 8rpx;
}

.chart-bar-1 {
  height: 16rpx;
}

.chart-bar-2 {
  height: 24rpx;
}

.chart-bar-3 {
  height: 32rpx;
}

.card-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.card-description {
  font-size: 28rpx;
  color: vars.$text-color;
  line-height: 1.5;
}

.arrow-wrapper {
  flex-shrink: 0;
  display: flex;
  align-items: center;
}

.arrow {
  font-size: 48rpx;
  color: vars.$text-muted;
  font-weight: 300;
  line-height: 1;
}
</style>
