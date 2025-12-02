<template>
  <view class="page">
    <view class="help-container">
      <text class="title">帮助</text>
      <text class="content">请联系 SmartHire 开发团队: sincetodayz@gmail.com</text>
    </view>
  </view>
</template>

<script setup lang="ts">
</script>

<style lang="scss" scoped>
@use "@/styles/variables.scss" as vars;

.page {
  min-height: 100vh;
  background-color: vars.$bg-color;
  padding: vars.$spacing-lg;
}

.help-container {
  background-color: vars.$surface-color;
  border-radius: vars.$border-radius-lg;
  padding: vars.$spacing-xl;
}

.title {
  font-size: 48rpx;
  font-weight: 600;
  color: vars.$text-color;
  display: block;
  margin-bottom: vars.$spacing-lg;
}

.content {
  font-size: 28rpx;
  color: vars.$text-color;
  line-height: 1.6;
}
</style>






