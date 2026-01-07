<template>
  <view
    class="system-notification-card"
    :class="{ unread: notification.isRead === 0 }"
    @click="handleClick"
    @longpress="handleLongPress"
  >
    <view class="card-header">
      <view class="type-badge" :class="typeClass">
        <text class="type-text">{{ notification.typeName }}</text>
      </view>
      <text class="notification-time">{{ formattedTime }}</text>
    </view>
    <view class="card-body">
      <view class="notification-title">{{ notification.title }}</view>
      <view class="notification-content">{{ notification.content }}</view>
    </view>
    <view v-if="notification.isRead === 0" class="unread-indicator"></view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { t } from '@/locales';
import dayjs from 'dayjs';
import type { SystemNotification } from '@/services/api/notification';

interface Props {
  notification: SystemNotification;
}

interface Emits {
  (e: 'click', notification: SystemNotification): void;
  (e: 'longpress', notification: SystemNotification): void;
}

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

const typeClass = computed(() => {
  const typeMap: Record<number, string> = {
    1: 'type-system',
    2: 'type-report',
    3: 'type-ban'
  };
  return typeMap[props.notification.type] || 'type-system';
});

const formattedTime = computed(() => {
  if (!props.notification.createdAt) return '';
  const notificationTime = dayjs(props.notification.createdAt);
  const now = dayjs();
  const diffDays = now.diff(notificationTime, 'day');
  if (diffDays === 0) {
    return notificationTime.format('HH:mm');
  }
  return notificationTime.format('YYYY/MM/DD HH:mm');
});

function handleClick() {
  emit('click', props.notification);
}

function handleLongPress() {
  emit('longpress', props.notification);
}
</script>

<style lang="scss" scoped>
@use "@/styles/variables.scss" as vars;

.system-notification-card {
  position: relative;
  background: #ffffff;
  border-radius: 16rpx;
  padding: vars.$spacing-lg;
  margin-bottom: vars.$spacing-md;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.08);
  transition: all 0.2s;
  overflow: hidden;

  &:active {
    background-color: vars.$bg-color;
    transform: scale(0.98);
  }

  &.unread {
    background: linear-gradient(to right, vars.$pale-blue 0%, #ffffff 4%);
    border-left: 4rpx solid vars.$primary-color;
    box-shadow: 0 2rpx 16rpx rgba(75, 163, 255, 0.15);
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: vars.$spacing-sm;
}

.type-badge {
  padding: 6rpx 16rpx;
  border-radius: 20rpx;
  font-size: 22rpx;
  font-weight: 500;

  &.type-system {
    background-color: #e6f7ff;
    color: #1890ff;
  }

  &.type-report {
    background-color: #fff7e6;
    color: #fa8c16;
  }

  &.type-ban {
    background-color: #fff1f0;
    color: #ff4d4f;
  }
}

.type-text {
  font-size: 22rpx;
  font-weight: 500;
}

.notification-time {
  font-size: 24rpx;
  color: vars.$text-muted;
}

.card-body {
  margin-top: vars.$spacing-xs;
}

.notification-title {
  font-size: 32rpx;
  font-weight: 600;
  color: vars.$text-color;
  margin-bottom: vars.$spacing-xs;
  line-height: 1.4;
}

.notification-content {
  font-size: 28rpx;
  color: vars.$text-muted;
  line-height: 1.6;
  margin-top: vars.$spacing-xs;
  word-break: break-word;
}

.unread-indicator {
  position: absolute;
  left: vars.$spacing-sm;
  top: vars.$spacing-lg;
  width: 12rpx;
  height: 12rpx;
  background-color: #ff4d4f;
  border-radius: 50%;
  box-shadow: 0 0 0 2rpx #ffffff;
}
</style>

