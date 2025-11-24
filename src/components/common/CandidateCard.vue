<template>
  <view class="candidate-card" @click="handleClick">
    <view class="card-header">
      <view class="avatar">{{ candidate.avatar || candidate.name.charAt(0) }}</view>
      <view class="candidate-info">
        <view class="name">{{ candidate.name }}</view>
        <view class="position">{{ candidate.position }}·{{ candidate.status }}</view>
      </view>
      <view class="match-score">
        <text class="score-value">{{ candidate.overallMatch }}%</text>
        <text class="score-label">匹配度</text>
      </view>
    </view>
    
    <view class="skills">
      <view 
        class="skill-tag" 
        v-for="skill in candidate.skills" 
        :key="skill"
      >
        {{ skill }}
      </view>
    </view>
    
    <view class="match-details">
      <view class="match-item">
        <text class="match-label">技能匹配</text>
        <view class="progress-bar">
          <view 
            class="progress-fill" 
            :style="{ width: candidate.skillMatch + '%' }"
          ></view>
        </view>
        <text class="match-value">{{ candidate.skillMatch }}%</text>
      </view>
      <view class="match-item">
        <text class="match-label">经验契合</text>
        <view class="progress-bar">
          <view 
            class="progress-fill" 
            :style="{ width: candidate.experienceFit + '%' }"
          ></view>
        </view>
        <text class="match-value">{{ candidate.experienceFit }}%</text>
      </view>
      <view class="match-item">
        <text class="match-label">文化匹配</text>
        <view class="progress-bar">
          <view 
            class="progress-fill" 
            :style="{ width: candidate.cultureMatch + '%' }"
          ></view>
        </view>
        <text class="match-value">{{ candidate.cultureMatch }}%</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import type { CandidateListItem } from '@/services/api/hr';

interface Props {
  candidate: CandidateListItem;
}

const props = defineProps<Props>();

const emit = defineEmits<{
  click: [candidate: CandidateListItem];
}>();

function handleClick() {
  emit('click', props.candidate);
}
</script>

<style lang="scss" scoped>
@use "@/styles/variables.scss" as vars;

.candidate-card {
  background-color: vars.$surface-color;
  border-radius: vars.$border-radius;
  padding: vars.$spacing-lg;
  margin-bottom: vars.$spacing-md;
}

.card-header {
  display: flex;
  align-items: flex-start;
  margin-bottom: vars.$spacing-md;
}

.avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  background-color: vars.$primary-color-soft;
  color: vars.$primary-color;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
  font-weight: 600;
  margin-right: vars.$spacing-md;
  flex-shrink: 0;
}

.candidate-info {
  flex: 1;
  min-width: 0;
}

.name {
  font-size: 32rpx;
  font-weight: 600;
  color: vars.$text-color;
  margin-bottom: 8rpx;
}

.position {
  font-size: 24rpx;
  color: vars.$text-muted;
}

.match-score {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  flex-shrink: 0;
}

.score-value {
  font-size: 48rpx;
  font-weight: 600;
  color: vars.$primary-color;
  line-height: 1;
}

.score-label {
  font-size: 20rpx;
  color: vars.$text-muted;
  margin-top: 4rpx;
}

.skills {
  display: flex;
  flex-wrap: wrap;
  gap: vars.$spacing-sm;
  margin-bottom: vars.$spacing-md;
}

.skill-tag {
  padding: 8rpx 16rpx;
  border-radius: vars.$border-radius-sm;
  font-size: 24rpx;
  background-color: #f5f5f5;
  color: vars.$text-color;
}

.match-details {
  display: flex;
  flex-direction: column;
  gap: vars.$spacing-sm;
}

.match-item {
  display: flex;
  align-items: center;
  gap: vars.$spacing-sm;
}

.match-label {
  font-size: 24rpx;
  color: vars.$text-muted;
  width: 120rpx;
  flex-shrink: 0;
}

.progress-bar {
  flex: 1;
  height: 8rpx;
  background-color: #f0f0f0;
  border-radius: 4rpx;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background-color: vars.$primary-color;
  border-radius: 4rpx;
  transition: width 0.3s ease;
}

.match-value {
  font-size: 24rpx;
  color: vars.$primary-color;
  font-weight: 600;
  width: 60rpx;
  text-align: right;
  flex-shrink: 0;
}
</style>




