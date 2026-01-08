<template>
  <view class="bracket-card" :class="{ sent }">
    <view class="bracket-header">
      <text class="bracket-title">{{ title }}</text>
    </view>
    <view class="bracket-body">
      <text class="bracket-text">{{ body }}</text>
    </view>
    <view v-if="showActions" class="bracket-actions">
      <button class="bracket-btn reject" size="mini" @click.stop="emitReject">{{ t('pages.chat.conversation.actionReject') }}</button>
      <button class="bracket-btn accept" size="mini" @click.stop="emitAccept">{{ t('pages.chat.conversation.actionAccept') }}</button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { defineProps, defineEmits } from 'vue';
import { t } from '@/locales';

interface Props {
  title: string;
  body: string;
  sent?: boolean;
  showActions?: boolean;
}

const props = defineProps<Props>();
const emit = defineEmits<{
  (e: 'accept'): void;
  (e: 'reject'): void;
}>();

function emitAccept() {
  emit('accept');
}

function emitReject() {
  emit('reject');
}
</script>

<style scoped lang="scss">
@use "@/styles/variables.scss" as vars;

.bracket-card {
  width: 100%;
  background: vars.$surface-color;
  border-radius: vars.$border-radius;
  padding: vars.$spacing-md vars.$spacing-lg;
  display: flex;
  flex-direction: column;
  gap: vars.$spacing-sm;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);
  box-sizing: border-box;

  &.sent {
    background: rgba(255, 255, 255, 0.12);
  }
}

.bracket-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.bracket-title {
  font-size: 30rpx;
  font-weight: 700;
  color: vars.$primary-color;
}

.bracket-body {
  font-size: 28rpx;
  color: vars.$text-color;
  line-height: 1.5;
  word-break: break-word;
}

.bracket-actions {
  display: flex;
  gap: vars.$spacing-sm;
  justify-content: flex-end;
}

.bracket-btn {
  min-width: 140rpx;
  border-radius: vars.$border-radius-sm;
  font-size: 26rpx;
  padding: 0 vars.$spacing-md;
  box-sizing: border-box;
  border: none;
  color: #ffffff;

  &.accept {
    background: vars.$primary-color;
  }

  &.reject {
    background: vars.$text-muted;
  }
}
</style>

