<template>
  <view class="progress-bar">
    <view 
      class="progress-fill" 
      :style="{ width: percentage + '%' }"
    ></view>
  </view>
</template>

<script setup lang="ts">
interface Props {
  percentage: number;
}

defineProps<Props>();
</script>

<style lang="scss" scoped>
@use "@/styles/variables.scss" as vars;

.progress-bar {
  width: 100%;
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
</style>




