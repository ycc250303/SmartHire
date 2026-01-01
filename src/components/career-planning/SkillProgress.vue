<template>
  <view class="skill-progress">
    <view class="skill-header">
      <text class="skill-name">{{ name }}</text>
      <view class="skill-level-badge" :class="levelClass">
        <text class="skill-level-text">{{ levelText }}</text>
      </view>
    </view>
    <view class="progress-container">
      <view class="progress-bar">
        <view 
          class="progress-fill" 
          :style="{ width: percentage + '%', backgroundColor: fillColor }"
        ></view>
      </view>
      <text class="skill-percentage">{{ percentage }}%</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue';

interface Props {
  name: string;
  level: number;
  maxLevel?: number;
  isRequired?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  maxLevel: 4,
  isRequired: false,
});

const percentage = computed(() => {
  return Math.round((props.level / props.maxLevel) * 100);
});

const levelText = computed(() => {
  const levels = ['初级', '中级', '高级', '专家'];
  return levels[Math.min(props.level, levels.length - 1)] || '';
});

const levelClass = computed(() => {
  if (props.isRequired) {
    return 'required';
  }
  return '';
});

const fillColor = computed(() => {
  if (props.isRequired) {
    return '#4ba3ff';
  }
  return '#90caf9';
});
</script>

<style lang="scss" scoped>
@use "@/styles/variables.scss" as vars;

.skill-progress {
  display: flex;
  flex-direction: column;
  gap: vars.$spacing-sm;
}

.skill-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.skill-name {
  font-size: 28rpx;
  color: vars.$text-color;
  font-weight: 500;
}

.skill-level-badge {
  padding: 4rpx 12rpx;
  border-radius: vars.$border-radius-sm;
  background-color: vars.$primary-color-soft;
}

.skill-level-badge.required {
  background-color: #e3f2fd;
}

.skill-level-text {
  font-size: 20rpx;
  color: vars.$primary-color;
}

.progress-container {
  display: flex;
  align-items: center;
  gap: vars.$spacing-md;
}

.progress-bar {
  flex: 1;
  height: 12rpx;
  background-color: #f0f0f0;
  border-radius: 6rpx;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  border-radius: 6rpx;
  transition: width 0.3s ease;
}

.skill-percentage {
  font-size: 24rpx;
  color: vars.$text-muted;
  min-width: 60rpx;
  text-align: right;
}
</style>
