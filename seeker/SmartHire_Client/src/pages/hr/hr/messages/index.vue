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
      >通知</view>
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
        <view class="chat-info">
          <view class="chat-title">{{ chat.otherUserName || t('pages.chat.conversation.title') }}</view>
          <view class="chat-preview">{{ chat.lastMessage }}</view>
        </view>
        <view class="chat-meta">
          <text class="time">{{ formatTime(chat.lastMessageTime) }}</text>
          <view class="unread" v-if="chat.unreadCount > 0">
            <text class="unread-count">{{ chat.unreadCount > 99 ? '99+' : chat.unreadCount }}</text>
          </view>
        </view>
      </view>
    </view>

    <view v-else class="notice-list">
      <view class="notice-empty">
        <text class="empty-text">{{ t('pages.chat.notifications.empty') }}</text>
      </view>
    </view>
    
    <CustomTabBar />
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { onLoad, onShow } from '@dcloudio/uni-app';
import dayjs from 'dayjs';
import {
  getConversations,
  getUnreadCount,
  type Conversation,
  pinConversation,
  deleteConversation,
} from '@/services/api/message';
import CustomTabBar from '@/components/common/CustomTabBar.vue';
import { t } from '@/locales';

const activeTab = ref<'chat' | 'notice'>('chat');
const conversations = ref<Conversation[]>([]);
const notifications = ref<any[]>([]);
const loading = ref(false);
const error = ref<string | null>(null);

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

const openChat = (conversation: Conversation) => {
  const applicationId = conversation.applicationId ? `&applicationId=${conversation.applicationId}` : '';
  uni.navigateTo({
    url: `/pages/hr/hr/messages/chat?id=${conversation.id}&userId=${conversation.otherUserId || ''}&username=${encodeURIComponent(
      conversation.otherUserName || ''
    )}${applicationId}`,
  });
};

const handleNotification = () => {
  uni.showToast({ title: t('pages.chat.notifications.empty'), icon: 'none' });
};

const formatTime = (time?: string) => (time ? dayjs(time).format('MM/DD HH:mm') : '');

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
});

onMounted(() => {
  loadConversations();
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
  flex: 1;
  text-align: center;
  padding: 18rpx 0;
  border-radius: 999rpx;
  font-size: 28rpx;
  color: #7d879c;
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
  justify-content: space-between;
  align-items: center;
}

.chat-item.pinned {
  background: #eaf2ff;
}

.chat-info {
  flex: 1;
}

.chat-title {
  font-size: 30rpx;
  font-weight: 600;
}

.chat-preview {
  margin-top: 8rpx;
  color: #8b95a7;
}

.chat-meta {
  align-items: flex-end;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
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
</style>
