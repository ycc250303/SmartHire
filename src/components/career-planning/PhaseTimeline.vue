<template>
  <view class="phase-timeline-section">
    <text class="section-title">{{ title }}</text>
    <view class="timeline-container">
      <view 
        v-for="(phase, index) in phaseRoadmap" 
        :key="phase.phase"
        class="timeline-item"
        :style="{ animationDelay: index * 100 + 'ms' }"
      >
        <view class="timeline-line-wrapper">
          <view class="timeline-dot"></view>
          <view 
            class="timeline-line" 
            v-if="index < phaseRoadmap.length - 1"
          ></view>
        </view>
        <view class="phase-card">
          <view class="phase-header">
            <text class="phase-number">{{ phaseLabel }} {{ phase.phase }}</text>
            <text class="phase-duration">{{ phase.duration_days }}{{ daysText }}</text>
          </view>
          <text class="phase-title">{{ phase.title }}</text>
          <text class="phase-description">{{ phase.description }}</text>
          <view class="phase-skills" v-if="phase.skills && phase.skills.length > 0">
            <view 
              v-for="(skill, skillIndex) in phase.skills" 
              :key="skillIndex"
              class="phase-skill-tag"
            >
              <text class="phase-skill-text">{{ skill }}</text>
            </view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { t } from '@/locales';

interface PhaseRoadmapItem {
  phase: number;
  title: string;
  description: string;
  duration_days: number;
  skills: string[];
  based_on_gap: string;
}

interface Props {
  phaseRoadmap: PhaseRoadmapItem[];
  title?: string;
}

const props = withDefaults(defineProps<Props>(), {
  title: () => t('pages.jobs.careerPlanningPage.phaseRoadmap'),
});

const phaseLabel = computed(() => t('pages.jobs.careerPlanningPage.phase'));
const daysText = computed(() => t('pages.jobs.careerPlanningPage.days'));
</script>

<style lang="scss" scoped>
@use "@/styles/variables.scss" as vars;

.phase-timeline-section {
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

.timeline-container {
  position: relative;
}

.timeline-item {
  display: flex;
  gap: vars.$spacing-md;
  margin-bottom: vars.$spacing-xl;
  animation: slideInLeft 0.5s ease-out both;
  
  &:last-child {
    margin-bottom: 0;
  }
}

@keyframes slideInLeft {
  from {
    opacity: 0;
    transform: translateX(-40rpx);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.timeline-line-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  width: 32rpx;
  flex-shrink: 0;
}

.timeline-dot {
  width: 24rpx;
  height: 24rpx;
  border-radius: 50%;
  background-color: #007AFF;
  border: 4rpx solid vars.$surface-color;
  box-shadow: 0 0 0 2rpx rgba(0, 122, 255, 0.2);
  z-index: 2;
}

.timeline-line {
  width: 2rpx;
  flex: 1;
  background-color: #E5E5EA;
  margin-top: 4rpx;
  min-height: 100rpx;
}

.phase-card {
  flex: 1;
  border-left: 8rpx solid #007AFF;
  padding-left: vars.$spacing-lg;
  padding-bottom: vars.$spacing-lg;
}

.phase-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: vars.$spacing-sm;
}

.phase-number {
  font-size: 24rpx;
  color: vars.$text-muted;
  font-weight: 500;
}

.phase-duration {
  font-size: 24rpx;
  color: vars.$text-muted;
}

.phase-title {
  font-size: 30rpx;
  font-weight: 600;
  color: vars.$text-color;
  display: block;
  margin-bottom: vars.$spacing-sm;
}

.phase-description {
  font-size: 26rpx;
  color: vars.$text-muted;
  line-height: 1.6;
  display: block;
  margin-bottom: vars.$spacing-md;
}

.phase-skills {
  display: flex;
  flex-wrap: wrap;
  gap: vars.$spacing-sm;
}

.phase-skill-tag {
  padding: 4rpx 12rpx;
  border-radius: 12rpx;
  background-color: rgba(0, 122, 255, 0.1);
}

.phase-skill-text {
  font-size: 22rpx;
  color: #007AFF;
}
</style>

