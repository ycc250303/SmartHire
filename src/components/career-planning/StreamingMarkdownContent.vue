<template>
  <view class="streaming-markdown-content">
    <!-- 直接渲染文本内容，让浏览器自然换行 -->
    <text class="markdown-text" decode>{{ props.content }}</text>
    <text v-if="isStreaming" class="streaming-cursor">|</text>
  </view>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';

interface Props {
  content: string;
  isStreaming?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  isStreaming: false,
});
</script>

<style lang="scss" scoped>
@use "@/styles/variables.scss" as vars;

.streaming-markdown-content {
  font-size: 28rpx;
  color: vars.$text-color;
  line-height: 1.8;
  word-wrap: break-word;
  position: relative;
  display: flex;
  flex-direction: row;
  align-items: flex-start;
}

.markdown-text {
  flex: 1;
  white-space: pre-wrap;
  word-break: break-word;
}

.streaming-cursor {
  display: inline-block;
  font-weight: bold;
  color: vars.$primary-color;
  animation: blink 1s infinite;
  margin-left: 4rpx;
  flex-shrink: 0;
}

@keyframes blink {
  0%, 50% {
    opacity: 1;
  }
  51%, 100% {
    opacity: 0;
  }
}
</style>
