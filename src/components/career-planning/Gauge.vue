<template>
  <view class="gauge-container">
    <view class="gauge-wrapper" :style="wrapperStyle">
      <view class="gauge-circle" :style="circleStyle">
        <view class="gauge-fill" :style="fillStyle"></view>
        <view class="gauge-center">
          <text class="gauge-value" :style="valueStyle">{{ animatedValue }}</text>
          <text class="gauge-unit">{{ unit }}</text>
        </view>
      </view>
    </view>
    <text v-if="label" class="gauge-label">{{ label }}</text>
  </view>
</template>

<script setup lang="ts">
import { computed, ref, watch, onMounted, onUnmounted } from 'vue';

interface Props {
  value: number;
  max?: number;
  unit?: string;
  label?: string;
  color?: string;
  size?: number;
}

const props = withDefaults(defineProps<Props>(), {
  max: 100,
  unit: '%',
  color: '#4ba3ff',
  size: 180,
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

const wrapperStyle = computed(() => {
  return {
    width: props.size + 'rpx',
    height: props.size + 'rpx',
  };
});

const valueStyle = computed(() => {
  return {
    fontSize: Math.round(props.size * 0.22) + 'rpx',
  };
});

const animatedValue = ref(0);
let animationTimer: number | null = null;

function animateValue(target: number) {
  if (animationTimer !== null) {
    clearInterval(animationTimer);
    animationTimer = null;
  }
  
  const duration = 800;
  const steps = 60;
  const stepDuration = duration / steps;
  const startValue = animatedValue.value;
  const diff = target - startValue;
  let currentStep = 0;

  animationTimer = setInterval(() => {
    currentStep++;
    if (currentStep >= steps) {
      animatedValue.value = target;
      if (animationTimer !== null) {
        clearInterval(animationTimer);
        animationTimer = null;
      }
    } else {
      const progress = currentStep / steps;
      const easeProgress = 1 - Math.pow(1 - progress, 3);
      animatedValue.value = Math.round(startValue + diff * easeProgress);
    }
  }, stepDuration) as unknown as number;
}

watch(() => props.value, (newValue) => {
  animateValue(newValue);
}, { immediate: true });

onMounted(() => {
  animatedValue.value = 0;
  animateValue(props.value);
});

onUnmounted(() => {
  if (animationTimer !== null) {
    clearInterval(animationTimer);
    animationTimer = null;
  }
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
  font-weight: 700;
  color: vars.$text-color;
  line-height: 1;
}

.gauge-unit {
  font-size: 20rpx;
  color: vars.$text-muted;
  margin-top: 4rpx;
}

.gauge-label {
  font-size: 24rpx;
  color: vars.$text-muted;
  margin-top: vars.$spacing-sm;
}
</style>
