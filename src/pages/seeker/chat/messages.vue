<template>
  <view class="messages-component">
    <view v-if="loading && conversations.length === 0" class="loading-container">
      <text class="loading-text">{{ t('pages.chat.list.loading') }}</text>
    </view>

    <view v-else-if="error && conversations.length === 0" class="error-container">
      <text class="error-text">{{ t('pages.chat.list.loadError') }}</text>
      <button class="retry-button" @click="loadConversations">
        {{ t('pages.chat.list.retry') }}
      </button>
    </view>

    <scroll-view
      v-else
      class="conversation-list"
      scroll-y
      refresher-enabled
      :refresher-triggered="refreshing"
      @refresherrefresh="handleRefresh"
    >
      <view v-if="conversations.length === 0" class="empty-container">
        <text class="empty-text">{{ t('pages.chat.list.noConversations') }}</text>
      </view>

      <view
        v-for="conversation in sortedConversations"
        :key="conversation.id"
        class="conversation-item"
        :class="{ pinned: conversation.pinned === 1 }"
        @click="openConversation(conversation)"
        @longpress="showActionMenu(conversation)"
      >
        <image
          class="avatar"
          :src="conversation.otherUserAvatar || '/static/user-avatar.png'"
          mode="aspectFill"
        />
        <view class="conversation-info">
          <view class="conversation-header">
            <text class="username">{{ conversation.otherUserName }}</text>
            <text class="time">{{ formatTime(conversation.lastMessageTime) }}</text>
          </view>
          <view class="conversation-footer">
            <text class="last-message">{{ conversation.lastMessage || '' }}</text>
            <view v-if="conversation.unreadCount > 0" class="unread-badge">
              <text class="unread-count">{{ conversation.unreadCount > 99 ? '99+' : conversation.unreadCount }}</text>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { onShow, onPullDownRefresh } from '@dcloudio/uni-app';
import { t } from '@/locales';
import dayjs from 'dayjs';
import { getConversations, pinConversation, deleteConversation, type Conversation } from '@/services/api/message';

const conversations = ref<Conversation[]>([]);
const loading = ref(false);
const error = ref<string | null>(null);
const refreshing = ref(false);

const sortedConversations = computed(() => {
  const pinned = conversations.value.filter(c => c.pinned === 1);
  const unpinned = conversations.value.filter(c => c.pinned !== 1);
  
  const sortByTime = (a: Conversation, b: Conversation) => {
    const timeA = a.lastMessageTime ? new Date(a.lastMessageTime).getTime() : 0;
    const timeB = b.lastMessageTime ? new Date(b.lastMessageTime).getTime() : 0;
    return timeB - timeA;
  };
  
  return [...pinned.sort(sortByTime), ...unpinned.sort(sortByTime)];
});

onShow(() => {
  loadConversations();
});

onMounted(() => {
  loadConversations();
});

onPullDownRefresh(() => {
  handleRefresh();
});

async function loadConversations() {
  loading.value = true;
  error.value = null;
  
  try {
    const data = await getConversations();
    conversations.value = data;
  } catch (err) {
    error.value = err instanceof Error ? err.message : 'Unknown error';
    console.error('Failed to load conversations:', err);
  } finally {
    loading.value = false;
  }
}

async function handleRefresh() {
  refreshing.value = true;
  try {
    await loadConversations();
  } finally {
    refreshing.value = false;
    uni.stopPullDownRefresh();
  }
}

function formatTime(time?: string): string {
  if (!time) return '';
  
  const messageTime = dayjs(time);
  const now = dayjs();
  const diffDays = now.diff(messageTime, 'day');
  
  if (diffDays === 0) {
    return messageTime.format('HH:mm');
  } else if (diffDays === 1) {
    return t('pages.chat.list.yesterday');
  } else if (diffDays < 7) {
    return messageTime.format('MM/DD');
  } else {
    return messageTime.format('YYYY/MM/DD');
  }
}

function openConversation(conversation: Conversation) {
  uni.navigateTo({
    url: `/pages/seeker/chat/conversation?id=${conversation.id}&userId=${conversation.otherUserId}&username=${encodeURIComponent(conversation.otherUserName)}`
  });
}

function showActionMenu(conversation: Conversation) {
  const isPinned = conversation.pinned === 1;
  uni.showActionSheet({
    itemList: [
      isPinned ? t('pages.chat.list.unpin') : t('pages.chat.list.pin'),
      t('pages.chat.list.delete')
    ],
    success: (res) => {
      if (res.tapIndex === 0) {
        handlePinConversation(conversation, !isPinned);
      } else if (res.tapIndex === 1) {
        handleDeleteConversation(conversation);
      }
    }
  });
}

async function handlePinConversation(conversation: Conversation, pinned: boolean) {
  try {
    await pinConversation(conversation.id, pinned);
    conversation.pinned = pinned ? 1 : 0;
    uni.showToast({
      title: pinned ? t('pages.chat.list.pinSuccess') : t('pages.chat.list.unpinSuccess'),
      icon: 'success',
      duration: 1500
    });
  } catch (err) {
    console.error('Failed to pin conversation:', err);
    uni.showToast({
      title: t('pages.chat.list.loadError'),
      icon: 'none'
    });
  }
}

async function handleDeleteConversation(conversation: Conversation) {
  uni.showModal({
    title: t('common.confirm'),
    content: t('pages.chat.list.deleteConfirm'),
    success: async (res) => {
      if (res.confirm) {
        try {
          await deleteConversation(conversation.id);
          conversations.value = conversations.value.filter(c => c.id !== conversation.id);
          uni.showToast({
            title: t('pages.chat.list.deleteSuccess'),
            icon: 'success',
            duration: 1500
          });
        } catch (err) {
          console.error('Failed to delete conversation:', err);
          uni.showToast({
            title: t('pages.chat.list.deleteError'),
            icon: 'none'
          });
        }
      }
    }
  });
}
</script>

<style lang="scss" scoped>
@use "@/styles/variables.scss" as vars;

.messages-component {
  height: 100%;
  display: flex;
  flex-direction: column;
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

.conversation-list {
  flex: 1;
  overflow-y: auto;
}

.conversation-item {
  display: flex;
  align-items: center;
  padding: vars.$spacing-lg vars.$spacing-xl;
  background-color: vars.$surface-color;
  border-bottom: 1rpx solid vars.$bg-color;
  transition: background-color 0.2s;

  &:active {
    background-color: vars.$bg-color;
  }

  &.pinned {
    background-color: vars.$pale-blue;
  }
}

.avatar {
  width: 96rpx;
  height: 96rpx;
  border-radius: 50%;
  margin-right: vars.$spacing-md;
  background-color: vars.$bg-color;
  flex-shrink: 0;
}

.conversation-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.conversation-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: vars.$spacing-xs;
}

.username {
  font-size: 32rpx;
  font-weight: 600;
  color: vars.$text-color;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.time {
  font-size: 24rpx;
  color: vars.$text-muted;
  margin-left: vars.$spacing-sm;
  flex-shrink: 0;
}

.conversation-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.last-message {
  font-size: 28rpx;
  color: vars.$text-muted;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.unread-badge {
  min-width: 36rpx;
  height: 36rpx;
  background-color: #ff4d4f;
  border-radius: 18rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 12rpx;
  margin-left: vars.$spacing-sm;
  flex-shrink: 0;
}

.unread-count {
  font-size: 20rpx;
  color: #ffffff;
  font-weight: 600;
}
</style>

