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
      <view class="chat-item" v-for="chat in conversations" :key="chat.id" @click="openChat(chat.id)">
        <view class="chat-info">
          <view class="chat-title">{{ chat.candidate }} · {{ chat.jobTitle }}</view>
          <view class="chat-preview">{{ chat.lastMessage }}</view>
        </view>
        <view class="chat-meta">
          <text class="time">{{ formatTime(chat.timestamp) }}</text>
          <view class="dot" v-if="chat.unread"></view>
        </view>
      </view>
    </view>

    <view v-else class="notice-list">
      <view class="notice-item" v-for="item in notifications" :key="item.id" @click="handleNotification(item)">
        <view class="notice-title">{{ item.title }}</view>
        <view class="notice-desc">{{ item.desc }}</view>
        <view class="notice-time">{{ formatTime(item.timestamp) }}</view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { onLoad, onShow } from '@dcloudio/uni-app';
import dayjs from 'dayjs';
import { fetchConversations, fetchNotifications, type ConversationPreview, type NotificationItem } from '@/mock/hr';

const activeTab = ref<'chat' | 'notice'>('chat');
const conversations = ref<ConversationPreview[]>([]);
const notifications = ref<NotificationItem[]>([]);

const loadData = async () => {
  // TODO: GET /api/hr/messages
  conversations.value = await fetchConversations();
  notifications.value = await fetchNotifications();
};

const openChat = (id: string) => {
  uni.navigateTo({ url: `/pages/hr/hr/messages/chat?id=${id}` });
};

const handleNotification = (item: NotificationItem) => {
  if (item.route) {
    if (item.route.includes('/pages/hr/hr/messages/index')) {
      uni.switchTab({ url: '/pages/hr/hr/messages/index' });
    } else {
      uni.navigateTo({ url: item.route });
    }
  }
};

const formatTime = (time: string) => dayjs(time).format('MM/DD HH:mm');

const syncStoredTab = () => {
  const cached = uni.getStorageSync('hr_messages_tab');
  if (cached) {
    activeTab.value = cached === 'notice' ? 'notice' : 'chat';
    uni.removeStorageSync('hr_messages_tab');
  }
};

onLoad((options) => {
  if (options?.tab === 'notice') {
    activeTab.value = 'notice';
  }
});

onShow(() => {
  syncStoredTab();
});

onMounted(() => {
  loadData();
});
</script>

<style scoped lang="scss">
.messages-page {
  padding: 24rpx;
  background: #f6f7fb;
  min-height: 100vh;
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

.chat-item,
.notice-item {
  background: #fff;
  border-radius: 24rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
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

.dot {
  width: 16rpx;
  height: 16rpx;
  background: #ff4d4f;
  border-radius: 50%;
}

.notice-title {
  font-size: 30rpx;
  font-weight: 600;
}

.notice-desc {
  margin-top: 8rpx;
  color: #7a859b;
}

.notice-time {
  margin-top: 12rpx;
  font-size: 22rpx;
  color: #a0a9bb;
}
</style>
