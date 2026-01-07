<template>
  <view class="messages-page">
    <view class="tab-toggle">
      <view
        class="tab-item"
        :class="{ active: activeTab === 'chat' }"
        @click="activeTab = 'chat'"
      >会话</view>
      <view
        class="tab-item"
        :class="{ active: activeTab === 'notice' }"
        @click="activeTab = 'notice'"
      >
        通知
        <text v-if="systemUnreadCount > 0" class="tab-badge">{{
          systemUnreadCount > 99 ? '99+' : systemUnreadCount
        }}</text>
      </view>
    </view>

    <view v-if="activeTab === 'chat'" class="chat-list">
      <view
        class="chat-item"
        v-for="chat in sortedConversations"
        :key="chat.id"
        :class="{ pinned: chat.pinned === 1 }"
        @click="openChat(chat)"
        @longpress="showActionMenu(chat)"
      >
        <image
          class="avatar"
          :src="chat.otherUserAvatar || '/static/user-avatar.png'"
          mode="aspectFill"
        />
        <view class="chat-info">
          <view class="chat-title">{{ chat.otherUserName || t('pages.chat.conversation.title') }}</view>
          <view class="chat-preview">{{ chat.lastMessage }}</view>
        </view>
        <view class="chat-meta">
          <view class="ai-pill" @click.stop="openAi(chat)">
            <text class="ai-text">AI</text>
          </view>
          <text class="time">{{ formatTime(chat.lastMessageTime) }}</text>
          <view class="unread" v-if="chat.unreadCount > 0">
            <text class="unread-count">{{ chat.unreadCount > 99 ? '99+' : chat.unreadCount }}</text>
          </view>
        </view>
      </view>
    </view>

    <view v-else class="notice-list">
      <view class="notice-header">
        <text class="notice-title">系统通知</text>
        <text
          v-if="notifications.length > 0 && systemUnreadCount > 0"
          class="notice-action"
          @click="handleMarkAllRead"
        >全部已读</text>
      </view>

      <scroll-view
        class="notifications-scroll"
        scroll-y
        refresher-enabled
        :refresher-triggered="refreshing"
        @refresherrefresh="handleRefresh"
        @scrolltolower="loadMoreNotifications"
      >
        <view v-if="loadingNotifications && notifications.length === 0" class="notice-empty">
          <text class="empty-text">加载中...</text>
        </view>
        <view v-else-if="notifications.length === 0" class="notice-empty">
          <text class="empty-text">{{ t('pages.chat.notifications.empty') }}</text>
        </view>

        <SystemNotificationCard
          v-for="notification in notifications"
          :key="notification.id"
          :notification="notification"
          @click="handleNotificationClick"
          @longpress="handleNotificationLongPress"
        />

        <view v-if="loadingMore" class="loading-more">
          <text class="empty-text">加载更多...</text>
        </view>
      </scroll-view>
    </view>
    
    <CustomTabBar />
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue';
import { onLoad, onShow } from '@dcloudio/uni-app';
import dayjs from 'dayjs';
import {
  getConversations,
  getUnreadCount,
  type Conversation,
  pinConversation,
  deleteConversation,
} from '@/services/api/message';
import { getApplicationDetail } from '@/services/api/hr';
import { getJobPositionList } from '@/services/api/hr';
import CustomTabBar from '@/components/common/CustomTabBar.vue';
import { t } from '@/locales';
import SystemNotificationCard from '@/components/common/SystemNotificationCard.vue';
import {
  getSystemNotifications,
  getSystemNotificationUnreadCount,
  markNotificationAsRead,
  markAllNotificationsAsRead,
  deleteNotification,
  type SystemNotification,
} from '@/services/api/notification';

const activeTab = ref<'chat' | 'notice'>('chat');
const conversations = ref<Conversation[]>([]);
const notifications = ref<SystemNotification[]>([]);
const loading = ref(false);
const error = ref<string | null>(null);
const loadingNotifications = ref(false);
const loadingMore = ref(false);
const refreshing = ref(false);
const systemUnreadCount = ref(0);
const currentPage = ref(1);
const pageSize = 20;
const hasMore = ref(true);

const sortedConversations = computed(() => {
  const pinned = conversations.value.filter((c) => c.pinned === 1);
  const unpinned = conversations.value.filter((c) => c.pinned !== 1);
  const byTime = (a: Conversation, b: Conversation) => {
    const ta = a.lastMessageTime ? new Date(a.lastMessageTime).getTime() : 0;
    const tb = b.lastMessageTime ? new Date(b.lastMessageTime).getTime() : 0;
    return tb - ta;
  };
  return [...pinned.sort(byTime), ...unpinned.sort(byTime)];
});

const unreadTotal = ref(0);

const loadConversations = async () => {
  loading.value = true;
  error.value = null;
  try {
    conversations.value = await getConversations();
    unreadTotal.value = await getUnreadCount();
  } catch (err) {
    console.error('Failed to load conversations:', err);
    error.value = err instanceof Error ? err.message : 'Load failed';
  } finally {
    loading.value = false;
  }
};

const loadNotificationList = async (reset = true) => {
  if (reset) {
    loadingNotifications.value = true;
    currentPage.value = 1;
    hasMore.value = true;
    notifications.value = [];
  } else {
    loadingMore.value = true;
  }

  try {
    const [list, unread] = await Promise.all([
      getSystemNotifications({ page: currentPage.value, size: pageSize }),
      getSystemNotificationUnreadCount(),
    ]);

    if (reset) {
      notifications.value = Array.isArray(list) ? list : [];
    } else if (Array.isArray(list)) {
      notifications.value.push(...list);
    }

    systemUnreadCount.value = typeof unread === 'number' ? unread : 0;
    if (!Array.isArray(list) || list.length < pageSize) {
      hasMore.value = false;
    }
  } catch (err) {
    console.error('Failed to load notifications:', err);
  } finally {
    loadingNotifications.value = false;
    loadingMore.value = false;
  }
};

const handleRefresh = async () => {
  refreshing.value = true;
  try {
    await loadNotificationList(true);
  } finally {
    refreshing.value = false;
    uni.stopPullDownRefresh();
  }
};

const loadMoreNotifications = async () => {
  if (loadingMore.value || !hasMore.value) return;
  currentPage.value += 1;
  await loadNotificationList(false);
};

const handleNotificationClick = async (notification: SystemNotification) => {
  if (notification.isRead === 1) return;
  try {
    await markNotificationAsRead(notification.id);
    notification.isRead = 1;
    notification.readAt = new Date().toISOString();
    if (systemUnreadCount.value > 0) systemUnreadCount.value -= 1;
  } catch (err) {
    console.error('Failed to mark notification as read:', err);
  }
};

const handleMarkAllRead = async () => {
  try {
    await markAllNotificationsAsRead();
    notifications.value.forEach((n) => {
      n.isRead = 1;
      n.readAt = new Date().toISOString();
    });
    systemUnreadCount.value = 0;
    uni.showToast({ title: '已全部标为已读', icon: 'success' });
  } catch (err) {
    console.error('Failed to mark all read:', err);
    uni.showToast({ title: t('pages.chat.list.loadError'), icon: 'none' });
  }
};

const handleNotificationLongPress = (notification: SystemNotification) => {
  const canMarkRead = notification.isRead === 0;
  uni.showActionSheet({
    itemList: [canMarkRead ? '标为已读' : '', '删除'].filter(Boolean),
    success: (res) => {
      const actions = [canMarkRead ? 'markRead' : null, 'delete'].filter(Boolean);
      const action = actions[res.tapIndex];
      if (action === 'markRead') {
        handleNotificationClick(notification);
        return;
      }
      if (action === 'delete') {
        uni.showModal({
          title: t('common.confirm'),
          content: '确认删除该系统消息？',
          success: async (modalRes) => {
            if (!modalRes.confirm) return;
            try {
              await deleteNotification(notification.id);
              notifications.value = notifications.value.filter((n) => n.id !== notification.id);
              if (notification.isRead === 0 && systemUnreadCount.value > 0) {
                systemUnreadCount.value -= 1;
              }
              uni.showToast({ title: t('pages.chat.list.deleteSuccess'), icon: 'success' });
            } catch (err) {
              console.error('Failed to delete notification:', err);
              uni.showToast({ title: t('pages.chat.list.deleteError'), icon: 'none' });
            }
          },
        });
      }
    },
  });
};

const openChat = (conversation: Conversation) => {
  const applicationId = conversation.applicationId ? `&applicationId=${conversation.applicationId}` : '';
  const avatar = conversation.otherUserAvatar ? `&avatar=${encodeURIComponent(conversation.otherUserAvatar)}` : '';
  uni.navigateTo({
    url: `/pages/hr/hr/messages/chat?id=${conversation.id}&userId=${conversation.otherUserId || ''}&username=${encodeURIComponent(
      conversation.otherUserName || ''
    )}${applicationId}${avatar}`,
  });
};

const handleNotification = () => {
  uni.showToast({ title: t('pages.chat.notifications.empty'), icon: 'none' });
};

const formatTime = (time?: string) => (time ? dayjs(time).format('MM/DD HH:mm') : '');

const openAi = async (conversation: Conversation) => {
  const fromConv = uni.getStorageSync(`hr_chat_app_by_conv_${conversation.id}`);
  const fromUser = uni.getStorageSync(`hr_chat_app_by_user_${conversation.otherUserId}`);
  const raw = conversation.applicationId ?? fromConv ?? fromUser;
  const applicationId = Number(raw);
  if (!Number.isFinite(applicationId) || applicationId <= 0) {
    try {
      uni.showLoading({ title: '选择岗位...' });
      const jobs = await getJobPositionList(1);
      const list = (jobs || []).slice(0, 12);
      if (list.length === 0) {
        uni.showToast({ title: '暂无可用岗位', icon: 'none' });
        return;
      }
      uni.showActionSheet({
        itemList: list.map((j) => `${j.jobTitle}${j.city ? ` · ${j.city}` : ''}`),
        success: (res) => {
          const selected = list[res.tapIndex];
          if (!selected?.id) return;
          uni.navigateTo({
            url: `/pages/hr/hr/ai/candidate-ai?userId=${encodeURIComponent(
              conversation.otherUserId
            )}&jobId=${encodeURIComponent(selected.id)}&candidateName=${encodeURIComponent(
              conversation.otherUserName || ''
            )}&jobTitle=${encodeURIComponent(selected.jobTitle || '')}`,
          });
        },
      });
      return;
    } catch (err) {
      console.error('Failed to open AI without application:', err);
      uni.showToast({ title: '打开失败', icon: 'none' });
      return;
    } finally {
      uni.hideLoading();
    }
  }
  uni.showLoading({ title: '加载AI...' });
  try {
    const detail = await getApplicationDetail(applicationId);
    uni.navigateTo({
      url: `/pages/hr/hr/ai/candidate-ai?jobSeekerId=${encodeURIComponent(
        detail.jobSeekerId
      )}&jobId=${encodeURIComponent(detail.jobId)}&candidateName=${encodeURIComponent(
        conversation.otherUserName || ''
      )}&jobTitle=${encodeURIComponent(detail.jobTitle || '')}`,
    });
  } catch (err) {
    console.error('Failed to open AI:', err);
    uni.showToast({ title: '打开失败', icon: 'none' });
  } finally {
    uni.hideLoading();
  }
};

const syncStoredTab = () => {
  const cached = uni.getStorageSync('hr_messages_tab');
  if (cached) {
    activeTab.value = cached === 'notice' ? 'notice' : 'chat';
    uni.removeStorageSync('hr_messages_tab');
  }
};

const togglePin = async (conversation: Conversation) => {
  const nextPinned = conversation.pinned !== 1;
  try {
    await pinConversation(conversation.id, nextPinned);
    conversation.pinned = nextPinned ? 1 : 0;
    uni.showToast({
      title: nextPinned ? t('pages.chat.list.pinSuccess') : t('pages.chat.list.unpinSuccess'),
      icon: 'success',
    });
  } catch (err) {
    uni.showToast({ title: t('pages.chat.list.loadError'), icon: 'none' });
    console.error('Failed to pin conversation:', err);
  }
};

const confirmDelete = (conversation: Conversation) => {
  uni.showModal({
    title: t('common.confirm'),
    content: t('pages.chat.list.deleteConfirm'),
    success: async (res) => {
      if (!res.confirm) return;
      try {
        await deleteConversation(conversation.id);
        conversations.value = conversations.value.filter((c) => c.id !== conversation.id);
        uni.showToast({ title: t('pages.chat.list.deleteSuccess'), icon: 'success' });
      } catch (err) {
        uni.showToast({ title: t('pages.chat.list.deleteError'), icon: 'none' });
        console.error('Failed to delete conversation:', err);
      }
    },
  });
};

const showActionMenu = (conversation: Conversation) => {
  const isPinned = conversation.pinned === 1;
  uni.showActionSheet({
    itemList: [
      isPinned ? t('pages.chat.list.unpin') : t('pages.chat.list.pin'),
      t('pages.chat.list.delete'),
    ],
    success: (res) => {
      if (res.tapIndex === 0) {
        togglePin(conversation);
      } else if (res.tapIndex === 1) {
        confirmDelete(conversation);
      }
    },
  });
};

onLoad((options) => {
  if (options?.tab === 'notice') {
    activeTab.value = 'notice';
  }
});

onShow(() => {
  uni.hideTabBar();
  syncStoredTab();
  loadConversations();
  loadNotificationList(true);
});

onMounted(() => {
  loadConversations();
  loadNotificationList(true);
});

watch(activeTab, (tab) => {
  if (tab === 'notice') {
    loadNotificationList(true);
  }
});
</script>

<style scoped lang="scss">
.messages-page {
  padding: calc(var(--status-bar-height) + 64rpx) 24rpx 24rpx;
  background: linear-gradient(180deg, #e5f0ff 0%, #f6f7fb 40%, #f6f7fb 100%);
  min-height: 100vh;
  box-sizing: border-box;
}

.tab-toggle {
  display: flex;
  background: #fff;
  border-radius: 999rpx;
  padding: 8rpx;
  margin-bottom: 24rpx;
}

.tab-item {
  position: relative;
  flex: 1;
  text-align: center;
  padding: 18rpx 0;
  border-radius: 999rpx;
  font-size: 28rpx;
  color: #7d879c;
}

.tab-badge {
  position: absolute;
  top: 6rpx;
  right: 30rpx;
  min-width: 34rpx;
  height: 34rpx;
  padding: 0 10rpx;
  background: #ff4d4f;
  border-radius: 17rpx;
  font-size: 20rpx;
  color: #fff;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.tab-item.active {
  background: #2f7cff;
  color: #fff;
}

.chat-item {
  background: #fff;
  border-radius: 24rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
  display: flex;
  justify-content: flex-start;
  align-items: center;
  gap: 16rpx;
}

.chat-item.pinned {
  background: #eaf2ff;
}

.avatar {
  width: 84rpx;
  height: 84rpx;
  border-radius: 50%;
  background: #f0f2f5;
  flex-shrink: 0;
}

.chat-info {
  flex: 1;
  min-width: 0;
}

.chat-title {
  font-size: 30rpx;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.chat-preview {
  margin-top: 8rpx;
  color: #8b95a7;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.chat-meta {
  align-items: flex-end;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  margin-left: auto;
}

.ai-pill {
  padding: 8rpx 18rpx;
  border-radius: 999rpx;
  background: rgba(75, 163, 255, 0.14);
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 44rpx;
}

.ai-text {
  font-size: 24rpx;
  color: #2f7cff;
  font-weight: 800;
  line-height: 1;
}

.time {
  font-size: 22rpx;
  color: #a0a9bb;
}

.unread {
  min-width: 32rpx;
  height: 32rpx;
  padding: 0 10rpx;
  background: #ff4d4f;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.unread-count {
  color: #fff;
  font-size: 20rpx;
  font-weight: 600;
}

.notice-empty {
  background: #fff;
  border-radius: 24rpx;
  padding: 48rpx 24rpx;
  display: flex;
  justify-content: center;
  align-items: center;
}

.empty-text {
  color: #7a859b;
  font-size: 28rpx;
}

.notice-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
  min-height: 0;
}

.notice-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12rpx 8rpx 0;
}

.notice-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #0b1c33;
}

.notice-action {
  font-size: 24rpx;
  color: #2f7cff;
}

.notifications-scroll {
  flex: 1;
  min-height: 0;
}

.loading-more {
  padding: 20rpx 0 40rpx;
  display: flex;
  justify-content: center;
}
</style>
