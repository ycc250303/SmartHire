<template>
  <view class="system-notifications-page">
    <view class="page-header">
      <view class="header-back" @click="handleBack">
        <text class="back-icon">‹</text>
      </view>
      <view class="header-content">
        <text class="header-title">{{ t('pages.chat.systemNotifications.title') }}</text>
      </view>
      <view v-if="notifications.length > 0 && unreadCount > 0" class="header-action" @click="handleMarkAllRead">
        <text class="action-text">{{ t('pages.chat.systemNotifications.markAllRead') }}</text>
      </view>
    </view>

    <view v-if="loading" class="loading-container">
      <text class="loading-text">{{ t('pages.chat.list.loading') }}</text>
    </view>

    <view v-else-if="error" class="error-container">
      <text class="error-text">{{ t('pages.chat.list.loadError') }}</text>
      <button class="retry-button" @click="() => loadNotifications(true)">
        {{ t('pages.chat.list.retry') }}
      </button>
    </view>

    <scroll-view
      v-else
      class="notifications-list"
      scroll-y
      refresher-enabled
      :refresher-triggered="refreshing"
      @refresherrefresh="handleRefresh"
      @scrolltolower="loadMore"
    >
      <view v-if="notifications.length === 0" class="empty-container">
        <text class="empty-text">{{ t('pages.chat.systemNotifications.empty') }}</text>
      </view>

      <SystemNotificationCard
        v-for="notification in notifications"
        :key="notification.id"
        :notification="notification"
        @click="handleNotificationClick"
        @longpress="handleNotificationLongPress"
      />

      <view v-if="loadingMore" class="loading-more">
        <text class="loading-more-text">{{ t('pages.chat.conversation.loadMore') }}</text>
      </view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { onShow, onLoad } from '@dcloudio/uni-app';
import { t } from '@/locales';
import SystemNotificationCard from '@/components/common/SystemNotificationCard.vue';
import {
  getSystemNotifications,
  getSystemNotificationUnreadCount,
  markNotificationAsRead,
  markAllNotificationsAsRead,
  deleteNotification,
  type SystemNotification
} from '@/services/api/notification';

const notifications = ref<SystemNotification[]>([]);
const unreadCount = ref(0);
const loading = ref(false);
const loadingMore = ref(false);
const error = ref<string | null>(null);
const refreshing = ref(false);
const currentPage = ref(1);
const pageSize = 20;
const hasMore = ref(true);

onLoad(() => {
  uni.setNavigationBarTitle({
    title: t('pages.chat.systemNotifications.title')
  });
});

onShow(() => {
  loadNotifications();
});

onMounted(() => {
  loadNotifications();
});

async function loadNotifications(reset = true) {
  if (reset) {
    loading.value = true;
    currentPage.value = 1;
    hasMore.value = true;
    notifications.value = [];
  } else {
    loadingMore.value = true;
  }
  
  error.value = null;
  
  try {
    const [notificationsData, unreadCountData] = await Promise.all([
      getSystemNotifications({ page: currentPage.value, size: pageSize }),
      getSystemNotificationUnreadCount()
    ]);
    
    if (reset) {
      notifications.value = Array.isArray(notificationsData) ? notificationsData : [];
    } else {
      if (Array.isArray(notificationsData)) {
        notifications.value.push(...notificationsData);
      }
    }
    
    unreadCount.value = typeof unreadCountData === 'number' ? unreadCountData : 0;
    
    if (!Array.isArray(notificationsData) || notificationsData.length < pageSize) {
      hasMore.value = false;
    }
  } catch (err) {
    error.value = err instanceof Error ? err.message : 'Unknown error';
    console.error('Failed to load notifications:', err);
    if (reset) {
      notifications.value = [];
    }
  } finally {
    loading.value = false;
    loadingMore.value = false;
  }
}

async function handleRefresh() {
  refreshing.value = true;
  try {
    await loadNotifications(true);
  } finally {
    refreshing.value = false;
    uni.stopPullDownRefresh();
  }
}

async function loadMore() {
  if (loadingMore.value || !hasMore.value) return;
  
  currentPage.value++;
  await loadNotifications(false);
}


async function handleNotificationClick(notification: SystemNotification) {
  if (notification.isRead === 0) {
    try {
      await markNotificationAsRead(notification.id);
      notification.isRead = 1;
      notification.readAt = new Date().toISOString();
      if (unreadCount.value > 0) {
        unreadCount.value--;
      }
    } catch (err) {
      console.error('Failed to mark notification as read:', err);
    }
  }
}

async function handleMarkAllRead() {
  try {
    await markAllNotificationsAsRead();
    notifications.value.forEach(n => {
      n.isRead = 1;
      n.readAt = new Date().toISOString();
    });
    unreadCount.value = 0;
    uni.showToast({
      title: t('pages.chat.systemNotifications.markAllReadSuccess'),
      icon: 'success',
      duration: 1500
    });
  } catch (err) {
    console.error('Failed to mark all notifications as read:', err);
    uni.showToast({
      title: t('pages.chat.list.loadError'),
      icon: 'none'
    });
  }
}

function handleNotificationLongPress(notification: SystemNotification) {
  uni.showActionSheet({
    itemList: [
      notification.isRead === 0 ? t('pages.chat.systemNotifications.markRead') : '',
      t('pages.chat.list.delete')
    ].filter(Boolean),
    success: async (res) => {
      const actions = [
        notification.isRead === 0 ? 'markRead' : null,
        'delete'
      ].filter(Boolean);
      
      const action = actions[res.tapIndex];
      
      if (action === 'markRead' && notification.isRead === 0) {
        try {
          await markNotificationAsRead(notification.id);
          notification.isRead = 1;
          notification.readAt = new Date().toISOString();
          if (unreadCount.value > 0) {
            unreadCount.value--;
          }
          uni.showToast({
            title: t('pages.chat.systemNotifications.markReadSuccess'),
            icon: 'success',
            duration: 1500
          });
        } catch (err) {
          console.error('Failed to mark notification as read:', err);
          uni.showToast({
            title: t('pages.chat.list.loadError'),
            icon: 'none'
          });
        }
      } else if (action === 'delete') {
        uni.showModal({
          title: t('common.confirm'),
          content: t('pages.chat.systemNotifications.deleteConfirm'),
          success: async (modalRes) => {
            if (modalRes.confirm) {
              try {
                await deleteNotification(notification.id);
                notifications.value = notifications.value.filter(n => n.id !== notification.id);
                if (notification.isRead === 0 && unreadCount.value > 0) {
                  unreadCount.value--;
                }
                uni.showToast({
                  title: t('pages.chat.list.deleteSuccess'),
                  icon: 'success',
                  duration: 1500
                });
              } catch (err) {
                console.error('Failed to delete notification:', err);
                uni.showToast({
                  title: t('pages.chat.list.deleteError'),
                  icon: 'none'
                });
              }
            }
          }
        });
      }
    }
  });
}

function handleBack() {
  uni.navigateBack();
}
</script>

<style lang="scss" scoped>
@use "@/styles/variables.scss" as vars;

.system-notifications-page {
  min-height: 100vh;
  background: vars.$surface-color;
  display: flex;
  flex-direction: column;
}

.page-header {
  position: sticky;
  top: 0;
  z-index: 1000;
  display: flex;
  align-items: center;
  padding: calc(var(--status-bar-height) + vars.$spacing-md) vars.$spacing-lg vars.$spacing-md;
  background: vars.$surface-color;
  border-bottom: 1rpx solid vars.$bg-color;
}

.header-back {
  width: 60rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: vars.$spacing-md;
}

.back-icon {
  font-size: 48rpx;
  color: vars.$text-color;
  font-weight: 300;
}

.header-content {
  flex: 1;
}

.header-title {
  font-size: 36rpx;
  font-weight: 600;
  color: vars.$text-color;
}

.header-action {
  padding: vars.$spacing-xs vars.$spacing-md;
}

.action-text {
  font-size: 28rpx;
  color: vars.$primary-color;
}

.loading-container,
.error-container,
.empty-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  flex: 1;
  padding: vars.$spacing-xl;
}

.loading-text,
.error-text,
.empty-text {
  font-size: 28rpx;
  color: vars.$text-muted;
  margin-bottom: vars.$spacing-lg;
}

.retry-button {
  padding: vars.$spacing-md vars.$spacing-xl;
  background-color: vars.$primary-color;
  color: #ffffff;
  border-radius: vars.$border-radius-sm;
  font-size: 28rpx;
  border: none;
}

.notifications-list {
  flex: 1;
  overflow-y: auto;
  padding: vars.$spacing-md;
}

.loading-more {
  display: flex;
  justify-content: center;
  padding: vars.$spacing-lg;
}

.loading-more-text {
  font-size: 24rpx;
  color: vars.$text-muted;
}
</style>

