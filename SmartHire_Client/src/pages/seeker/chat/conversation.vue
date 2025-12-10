<template>
  <view class="conversation-page">
    <view class="conversation-header">
      <view class="header-back" @click="handleBack">
        <text class="back-icon">‹</text>
      </view>
      <view class="header-content">
        <text class="header-username">{{ displayUsername }}</text>
      </view>
    </view>

    <view v-if="loading && messages.length === 0" class="loading-container">
      <text class="loading-text">{{ t('pages.chat.conversation.loading') }}</text>
    </view>

    <view v-else-if="error && messages.length === 0" class="error-container">
      <text class="error-text">{{ t('pages.chat.conversation.loadError') }}</text>
      <button class="retry-button" @click="loadMessages">
        {{ t('pages.chat.list.retry') }}
      </button>
    </view>

    <scroll-view
      v-else
      class="message-list"
      scroll-y
      :scroll-into-view="scrollIntoView"
      :scroll-top="scrollTop"
      @scrolltolower="loadMoreMessages"
    >
      <view v-if="messages.length === 0" class="empty-container">
        <text class="empty-text">{{ t('pages.chat.conversation.empty') }}</text>
      </view>

      <view v-for="(message, index) in messagesWithDates" :key="message.id || index">
        <view v-if="message.isDateSeparator" class="date-separator">
          <text class="date-text">{{ formatDateSeparator(message.date!) }}</text>
        </view>
        <view v-else class="message-item" :class="{ 'message-sent': isSentByMe(message), 'message-received': !isSentByMe(message) }">
          <view class="message-bubble">
            <text class="message-content">{{ message.content }}</text>
            <text class="message-time">{{ formatMessageTime(message.createdAt) }}</text>
          </view>
        </view>
      </view>

      <view v-if="loadingMore" class="loading-more">
        <text class="loading-more-text">{{ t('pages.chat.conversation.loadMore') }}</text>
      </view>
      <view v-else-if="noMore" class="no-more">
        <text class="no-more-text">{{ t('pages.chat.conversation.noMore') }}</text>
      </view>
    </scroll-view>

    <view class="input-container">
      <input
        class="message-input"
        v-model="inputText"
        :placeholder="t('pages.chat.conversation.inputPlaceholder')"
        @confirm="handleSend"
        :disabled="sending"
      />
      <button
        class="send-button"
        :class="{ disabled: !canSend || sending }"
        @click="handleSend"
        :disabled="!canSend || sending"
      >
        <text v-if="sending" class="send-button-text">{{ t('pages.chat.conversation.sending') }}</text>
        <text v-else class="send-button-text">{{ t('pages.chat.conversation.send') }}</text>
      </button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue';
import { onLoad, onShow } from '@dcloudio/uni-app';
import { t } from '@/locales';
import dayjs from 'dayjs';
import { getChatHistory, sendMessage, markAsRead, type Message } from '@/services/api/message';
import { useUserStore } from '@/store/user';

const userStore = useUserStore();

const conversationId = ref<string>('');
const otherUserId = ref<number>(0);
const otherUsername = ref<string>('');
const messages = ref<Message[]>([]);
const loading = ref(false);
const loadingMore = ref(false);
const error = ref<string | null>(null);
const sending = ref(false);
const inputText = ref('');
const scrollIntoView = ref('');
const scrollTop = ref(0);
const noMore = ref(false);
const currentPage = ref(1);
const pageSize = 20;

interface MessageWithDate extends Message {
  isDateSeparator?: boolean;
  date?: string;
}

const messagesWithDates = computed(() => {
  const result: MessageWithDate[] = [];
  let lastDate: string | null = null;

  messages.value.forEach((message, index) => {
    const messageDate = dayjs(message.createdAt).format('YYYY-MM-DD');
    
    if (messageDate !== lastDate) {
      result.push({
        id: `date-${messageDate}`,
        conversationId: message.conversationId,
        senderId: 0,
        receiverId: 0,
        content: '',
        messageType: 0,
        createdAt: message.createdAt,
        isRead: 0,
        isDateSeparator: true,
        date: messageDate,
      } as MessageWithDate);
      lastDate = messageDate;
    }
    
    result.push(message as MessageWithDate);
  });

  return result;
});

const canSend = computed(() => {
  return inputText.value.trim().length > 0 && !sending.value;
});

const displayUsername = computed(() => {
  if (otherUsername.value && otherUsername.value !== 'undefined') {
    return otherUsername.value;
  }
  return t('pages.chat.conversation.title');
});

onLoad((options: any) => {
  if (options?.id) {
    conversationId.value = options.id;
  }
  if (options?.userId) {
    otherUserId.value = parseInt(options.userId);
  }
  if (options?.username && options.username !== 'undefined') {
    try {
      otherUsername.value = decodeURIComponent(options.username);
    } catch (e) {
      otherUsername.value = options.username;
    }
  }
  
  uni.setNavigationBarTitle({
    title: displayUsername.value
  });
});

onShow(() => {
  if (conversationId.value) {
    loadMessages();
    markMessagesAsRead();
  }
});

onMounted(() => {
  if (conversationId.value) {
    loadMessages();
  }
});

async function loadMessages() {
  if (!conversationId.value) return;
  
  loading.value = true;
  error.value = null;
  currentPage.value = 1;
  noMore.value = false;
  
  try {
    const data = await getChatHistory({
      conversationId: conversationId.value,
      page: currentPage.value,
      pageSize,
    });
    
    messages.value = data.reverse();
    scrollToBottom();
    
    if (data.length < pageSize) {
      noMore.value = true;
    }
  } catch (err) {
    error.value = err instanceof Error ? err.message : 'Unknown error';
    console.error('Failed to load messages:', err);
  } finally {
    loading.value = false;
  }
}

async function loadMoreMessages() {
  if (loadingMore.value || noMore.value || !conversationId.value) return;
  
  loadingMore.value = true;
  currentPage.value += 1;
  
  try {
    const data = await getChatHistory({
      conversationId: conversationId.value,
      page: currentPage.value,
      pageSize,
    });
    
    if (data.length === 0) {
      noMore.value = true;
    } else {
      messages.value = [...data.reverse(), ...messages.value];
    }
  } catch (err) {
    console.error('Failed to load more messages:', err);
    currentPage.value -= 1;
  } finally {
    loadingMore.value = false;
  }
}

async function handleSend() {
  if (!canSend.value || !conversationId.value) return;
  
  const content = inputText.value.trim();
  if (!content) return;
  
  sending.value = true;
  const tempMessage: Message = {
    id: `temp-${Date.now()}`,
    conversationId: conversationId.value,
    senderId: 0,
    receiverId: otherUserId.value,
    content,
    messageType: 0,
    createdAt: new Date().toISOString(),
    isRead: 0,
  };
  
  messages.value.push(tempMessage);
  inputText.value = '';
  scrollToBottom();
  
  try {
    const sentMessage = await sendMessage({
      receiverId: otherUserId.value,
      content,
    });
    
    const index = messages.value.findIndex(m => m.id === tempMessage.id);
    if (index !== -1) {
      messages.value[index] = sentMessage;
    }
  } catch (err) {
    console.error('Failed to send message:', err);
    const index = messages.value.findIndex(m => m.id === tempMessage.id);
    if (index !== -1) {
      messages.value.splice(index, 1);
    }
    uni.showToast({
      title: t('pages.chat.conversation.sendError'),
      icon: 'none'
    });
  } finally {
    sending.value = false;
  }
}

function scrollToBottom() {
  nextTick(() => {
    if (messages.value.length > 0) {
      const lastMessage = messages.value[messages.value.length - 1];
      scrollIntoView.value = lastMessage.id;
    }
  });
}

function isSentByMe(message: Message): boolean {
  return message.receiverId === otherUserId.value;
}

function formatMessageTime(time: string): string {
  return dayjs(time).format(t('pages.chat.conversation.timeFormat'));
}

function formatDateSeparator(date: string): string {
  const messageDate = dayjs(date);
  const today = dayjs();
  const yesterday = today.subtract(1, 'day');
  
  if (messageDate.isSame(today, 'day')) {
    return t('pages.chat.conversation.today');
  } else if (messageDate.isSame(yesterday, 'day')) {
    return t('pages.chat.conversation.yesterday');
  } else {
    return messageDate.format(t('pages.chat.conversation.dateFormat'));
  }
}

async function markMessagesAsRead() {
  if (!conversationId.value) return;
  
  try {
    await markAsRead(conversationId.value);
  } catch (err) {
    console.error('Failed to mark messages as read:', err);
  }
}

function handleBack() {
  uni.navigateBack();
}
</script>

<style lang="scss" scoped>
@use "@/styles/variables.scss" as vars;

.conversation-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background-color: vars.$bg-color;
}

.conversation-header {
  display: flex;
  align-items: center;
  padding-top: calc(var(--status-bar-height) + vars.$spacing-sm);
  padding-bottom: vars.$spacing-md;
  padding-left: vars.$spacing-lg;
  padding-right: vars.$spacing-lg;
  background-color: vars.$surface-color;
  border-bottom: 1rpx solid vars.$bg-color;
  position: sticky;
  top: 0;
  z-index: 1000;
}

.header-back {
  width: 80rpx;
  height: 80rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: vars.$spacing-md;
  cursor: pointer;
  transition: opacity 0.2s;

  &:active {
    opacity: 0.6;
  }
}

.back-icon {
  font-size: 56rpx;
  font-weight: 300;
  color: vars.$text-color;
  line-height: 1;
}

.header-content {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.header-username {
  font-size: 36rpx;
  font-weight: 600;
  color: vars.$text-color;
  text-align: center;
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

.message-list {
  flex: 1;
  padding: vars.$spacing-md;
  overflow-y: auto;
  padding-top: vars.$spacing-lg;
}

.date-separator {
  display: flex;
  justify-content: center;
  margin: vars.$spacing-lg 0;
}

.date-text {
  font-size: 24rpx;
  color: vars.$text-muted;
  background-color: rgba(0, 0, 0, 0.05);
  padding: vars.$spacing-xs vars.$spacing-md;
  border-radius: vars.$border-radius-sm;
}

.message-item {
  display: flex;
  margin-bottom: vars.$spacing-md;
  
  &.message-sent {
    justify-content: flex-end;
  }
  
  &.message-received {
    justify-content: flex-start;
  }
}

.message-bubble {
  max-width: 70%;
  padding: vars.$spacing-md vars.$spacing-lg;
  border-radius: vars.$border-radius;
  display: flex;
  flex-direction: column;
  gap: vars.$spacing-xs;
}

.message-sent .message-bubble {
  background-color: vars.$primary-color;
  border-bottom-right-radius: 8rpx;
}

.message-received .message-bubble {
  background-color: vars.$surface-color;
  border-bottom-left-radius: 8rpx;
}

.message-content {
  font-size: 30rpx;
  line-height: 1.5;
  word-wrap: break-word;
}

.message-sent .message-content {
  color: #ffffff;
}

.message-received .message-content {
  color: vars.$text-color;
}

.message-time {
  font-size: 20rpx;
  align-self: flex-end;
}

.message-sent .message-time {
  color: rgba(255, 255, 255, 0.7);
}

.message-received .message-time {
  color: vars.$text-muted;
}

.loading-more,
.no-more {
  display: flex;
  justify-content: center;
  padding: vars.$spacing-lg;
}

.loading-more-text,
.no-more-text {
  font-size: 24rpx;
  color: vars.$text-muted;
}

.input-container {
  display: flex;
  align-items: center;
  padding: vars.$spacing-md vars.$spacing-lg;
  background-color: vars.$surface-color;
  border-top: 1rpx solid vars.$bg-color;
  gap: vars.$spacing-md;
}

.message-input {
  flex: 1;
  height: 72rpx;
  padding: 0 vars.$spacing-lg;
  background-color: vars.$bg-color;
  border-radius: vars.$border-radius-sm;
  font-size: 30rpx;
  color: vars.$text-color;
  border: none;
  line-height: 72rpx;
  box-sizing: border-box;
}

.send-button {
  height: 72rpx;
  padding: 0 vars.$spacing-xl;
  background-color: vars.$primary-color;
  color: #ffffff;
  border-radius: vars.$border-radius-sm;
  font-size: 28rpx;
  border: none;
  min-width: 120rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  box-sizing: border-box;
  
  &.disabled {
    opacity: 0.5;
  }
}

.send-button-text {
  color: #ffffff;
  font-size: 28rpx;
}
</style>

