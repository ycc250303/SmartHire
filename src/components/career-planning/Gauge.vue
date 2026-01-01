<template>
  <view class="gauge-container">
    <view class="gauge-wrapper">
      <view class="gauge-circle" :style="circleStyle">
        <view class="gauge-fill" :style="fillStyle"></view>
        <view class="gauge-center">
          <text class="gauge-value">{{ value }}</text>
          <text class="gauge-unit">{{ unit }}</text>
        </view>
      </view>
    </view>
    <text v-if="label" class="gauge-label">{{ label }}</text>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue';

interface Props {
  value: number;
  max?: number;
  unit?: string;
  label?: string;
  color?: string;
}

const props = withDefaults(defineProps<Props>(), {
  max: 100,
  unit: '%',
  color: '#4ba3ff',
});

const percentage = computed(() => {
  return Math.min(Math.max((props.value / props.max) * 100, 0), 100);
});

const circleStyle = computed(() => {
  return {
    background: `conic-gradient(${props.color} 0deg ${(percentage.value / 100) * 360}deg, #f0f0f0 ${(percentage.value / 100) * 360}deg 360deg)`,
  };
});

const fillStyle = computed(() => {
  return {
    background: '#ffffff',
  };
});
</script>

<style lang="scss" scoped>
@use "@/styles/variables.scss" as vars;

.gauge-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.gauge-wrapper {
  position: relative;
  width: 240rpx;
  height: 240rpx;
}

.gauge-circle {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.gauge-fill {
  position: absolute;
  width: 80%;
  height: 80%;
  border-radius: 50%;
  top: 10%;
  left: 10%;
}

.gauge-center {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.gauge-value {
  font-size: 56rpx;
  font-weight: 700;
  color: vars.$text-color;
  line-height: 1;
}

.gauge-unit {
  font-size: 24rpx;
  color: vars.$text-muted;
  margin-top: 8rpx;
}

.gauge-label {
  font-size: 28rpx;
  color: vars.$text-muted;
  margin-top: vars.$spacing-md;
}
</style>
