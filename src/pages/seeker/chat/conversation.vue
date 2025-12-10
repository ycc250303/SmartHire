<template>
  <view class="conversation-page">
    <view class="conversation-header" id="chat-header">
      <view class="header-back" @click="handleBack">
        <text class="back-icon">‹</text>
      </view>
      <view class="header-content">
        <text class="header-username">{{ displayUsername }}</text>
      </view>
    </view>

    <view v-if="loading && messages.length === 0" class="loading-container" :style="messageListStyle">
      <text class="loading-text">{{ t('pages.chat.conversation.loading') }}</text>
    </view>

    <view v-else-if="error && messages.length === 0" class="error-container" :style="messageListStyle">
      <text class="error-text">{{ t('pages.chat.conversation.loadError') }}</text>
      <button class="retry-button" @click="loadMessages">
        {{ t('pages.chat.list.retry') }}
      </button>
    </view>

    <scroll-view
      v-else
      class="message-list"
      :style="messageListStyle"
      scroll-y
      :scroll-into-view="scrollIntoView"
      :scroll-with-animation="useScrollAnimation"
      :enhanced="true"
      :show-scrollbar="false"
      :bounces="true"
      @scrolltoupper="loadMoreMessages"
    >
      <view v-if="loadingMore" class="loading-more">
        <view class="loading-spinner"></view>
      </view>

      <view v-if="messages.length === 0" class="empty-hint">
        <text class="empty-text">{{ t('pages.chat.conversation.empty') }}</text>
      </view>

      <view v-for="(message, index) in messagesWithDates" :key="message.id || index" :id="message.id ? `msg-${message.id}` : undefined">
        <view v-if="message.isDateSeparator" class="date-separator">
          <text class="date-text">{{ formatDateSeparator(message.date!) }}</text>
        </view>
        <view 
          v-else 
          class="message-item" 
          :class="{ 'message-sent': isSentByMe(message), 'message-received': !isSentByMe(message) }"
          @longpress="handleMessageLongPress(message)"
        >
          <view class="message-bubble">
            <view v-if="message.replyTo && message.replyContent" class="reply-preview">
              <view class="reply-line"></view>
              <view class="reply-content">
                <text class="reply-sender">{{ isSentByMe(message) ? t('pages.chat.conversation.you') : displayUsername }}</text>
                <text class="reply-text">{{ message.replyContent }}</text>
              </view>
            </view>
            <view v-if="message.fileUrl" class="message-file">
              <image v-if="isImage(message.fileUrl)" :src="message.fileUrl" mode="aspectFit" class="message-image" />
              <text v-else class="file-link">{{ t('pages.chat.conversation.file') }}</text>
            </view>
            <text v-if="message.content" class="message-content">{{ message.content }}</text>
            <view class="message-footer">
              <text class="message-time">{{ formatMessageTime(message.createdAt) }}</text>
              <text v-if="isSentByMe(message) && message.isRead === 1" class="read-indicator">✓✓</text>
              <text v-else-if="isSentByMe(message)" class="read-indicator unread">✓</text>
            </view>
          </view>
        </view>
      </view>
      
      <view id="msg-bottom" class="scroll-anchor"></view>
    </scroll-view>

    <view class="input-container" id="chat-input" :style="inputContainerStyle">
      <view v-if="replyingTo" class="reply-bar">
        <view class="reply-bar-content">
          <text class="reply-bar-text">{{ replyingTo.content }}</text>
          <text class="reply-bar-close" @click="cancelReply">×</text>
        </view>
      </view>
      <view class="input-row">
        <input
          ref="messageInputRef"
          class="message-input"
          v-model="inputText"
          :placeholder="t('pages.chat.conversation.inputPlaceholder')"
          :adjust-position="false"
          :cursor-spacing="0"
          :confirm-hold="true"
          :focus="inputFocused"
          @confirm="handleSend"
          @focus="onInputFocus"
          @blur="onInputBlur"
          @keyboardheightchange="onKeyboardHeightChange"
          :disabled="sending"
        />
        <view
          class="send-button"
          :class="{ disabled: !canSend || sending }"
          @touchend.prevent="handleSendClick"
        >
          <text v-if="sending" class="send-button-text">{{ t('pages.chat.conversation.sending') }}</text>
          <text v-else class="send-button-text">{{ t('pages.chat.conversation.send') }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick, watch } from 'vue';
import { onLoad, onShow } from '@dcloudio/uni-app';
import { t } from '@/locales';
import dayjs from 'dayjs';
import { getChatHistory, sendMessage, markAsRead, type Message } from '@/services/api/message';
import { useUserStore } from '@/store/user';

const userStore = useUserStore();

const conversationId = ref<number>(0);
const otherUserId = ref<number>(0);
const otherUsername = ref<string>('');
const messages = ref<Message[]>([]);
const loading = ref(false);
const loadingMore = ref(false);
const error = ref<string | null>(null);
const sending = ref(false);
const inputText = ref('');
const scrollIntoView = ref('');
const noMore = ref(false);
const currentPage = ref(1);
const pageSize = 20;
const replyingTo = ref<Message | null>(null);
const applicationId = ref<number>(0);

const keyboardHeight = ref(0);
const headerHeight = ref(88);
const inputHeight = ref(60);
const safeAreaBottom = ref(0);
const useScrollAnimation = ref(false);
const inputFocused = ref(false);

const inputContainerStyle = computed(() => ({
  bottom: `${keyboardHeight.value}px`
}));

const messageListStyle = computed(() => ({
  top: `${headerHeight.value}px`,
  bottom: `${inputHeight.value + keyboardHeight.value + safeAreaBottom.value}px`
}));

interface MessageWithDate extends Omit<Message, 'id'> {
  id: number | string;
  isDateSeparator?: boolean;
  date?: string;
}

const messagesWithDates = computed(() => {
  const result: MessageWithDate[] = [];
  let lastDate: string | null = null;

  messages.value.forEach((message) => {
    const messageDate = dayjs(message.createdAt).format('YYYY-MM-DD');
    
    if (messageDate !== lastDate) {
      result.push({
        id: `date-${messageDate}`,
        conversationId: message.conversationId,
        senderId: 0,
        receiverId: 0,
        content: '',
        messageType: 0,
        fileUrl: null,
        replyTo: null,
        createdAt: message.createdAt,
        isRead: 0,
        replyContent: null,
        replyMessageType: null,
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
    conversationId.value = parseInt(options.id);
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
  if (options?.applicationId) {
    applicationId.value = parseInt(options.applicationId);
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
  initLayout();
  if (conversationId.value) {
    loadMessages();
  }
});

function initLayout() {
  const sysInfo = uni.getSystemInfoSync();
  const statusBarHeight = sysInfo.statusBarHeight || 0;
  safeAreaBottom.value = sysInfo.safeAreaInsets?.bottom || 0;
  
  headerHeight.value = statusBarHeight + 44;
  
  nextTick(() => {
    measureInputHeight();
  });
}

function measureInputHeight() {
  const query = uni.createSelectorQuery();
  query.select('#chat-input').boundingClientRect((rect: any) => {
    if (rect && rect.height) {
      inputHeight.value = rect.height;
    }
  }).exec();
}

watch(replyingTo, () => {
  nextTick(() => {
    measureInputHeight();
  });
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
      size: pageSize,
    });
    
    messages.value = data.reverse();
    
    nextTick(() => {
      scrollToBottom(false);
    });
    
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
      size: pageSize,
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
    id: 0,
    conversationId: conversationId.value,
    senderId: 0,
    receiverId: otherUserId.value,
    content,
    messageType: 1,
    fileUrl: null,
    replyTo: (replyingTo.value ? replyingTo.value.id : null) as number | null,
    createdAt: new Date().toISOString(),
    isRead: 0,
    replyContent: replyingTo.value?.content || null,
    replyMessageType: replyingTo.value?.messageType || null,
  };
  
  messages.value.push(tempMessage);
  const messageContent = content;
  inputText.value = '';
  replyingTo.value = null;
  
  scrollToBottom(true);
  
  try {
    const sentMessage = await sendMessage({
      receiverId: otherUserId.value,
      applicationId: applicationId.value || 0,
      messageType: 1,
      content: messageContent,
      fileUrl: null,
      replyTo: tempMessage.replyTo,
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

function scrollToBottom(animated = false) {
  useScrollAnimation.value = animated;
  nextTick(() => {
    scrollIntoView.value = 'msg-bottom';
    setTimeout(() => {
      scrollIntoView.value = '';
    }, 50);
  });
}

function onKeyboardHeightChange(e: any) {
  const height = e.detail?.height || 0;
  keyboardHeight.value = height;
  
  nextTick(() => {
    scrollToBottom(false);
  });
}

function onInputFocus() {
  inputFocused.value = true;
  // H5 fallback since keyboardheightchange may not fire
  // #ifdef H5
  setTimeout(() => {
    scrollToBottom(false);
  }, 350);
  // #endif
}

function onInputBlur() {
  inputFocused.value = false;
  // #ifdef H5
  keyboardHeight.value = 0;
  // #endif
}

function handleSendClick() {
  if (!canSend.value || sending.value) return;
  handleSend();
  inputFocused.value = true;
}

function cancelReply() {
  replyingTo.value = null;
}

function isSentByMe(message: MessageWithDate): boolean {
  if (message.isDateSeparator) return false;
  return message.senderId !== otherUserId.value;
}

function handleMessageLongPress(message: MessageWithDate) {
  if (message.isDateSeparator || isSentByMe(message)) return;
  
  const msg = message as Message;
  uni.showActionSheet({
    itemList: [t('pages.chat.conversation.reply')],
    success: () => {
      replyingTo.value = msg;
      uni.showToast({
        title: t('pages.chat.conversation.replying'),
        icon: 'none',
        duration: 1500
      });
    }
  });
}

function isImage(url: string | null): boolean {
  if (!url) return false;
  const imageExtensions = ['.jpg', '.jpeg', '.png', '.gif', '.webp'];
  return imageExtensions.some(ext => url.toLowerCase().includes(ext));
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
  position: relative;
  width: 100%;
  height: 100vh;
  background-color: vars.$bg-color;
  overflow: hidden;
}

.conversation-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  display: flex;
  align-items: flex-end;
  padding-top: var(--status-bar-height);
  height: calc(var(--status-bar-height) + 88rpx);
  background-color: vars.$surface-color;
  border-bottom: 1rpx solid vars.$bg-color;
  z-index: 100;
  box-sizing: border-box;
}

.header-back {
  position: absolute;
  left: vars.$spacing-sm;
  bottom: 0;
  width: 88rpx;
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1;

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
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 100rpx;
  box-sizing: border-box;
}

.header-username {
  font-size: 34rpx;
  font-weight: 600;
  color: vars.$text-color;
  text-align: center;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 100%;
}

.loading-container,
.error-container {
  position: fixed;
  left: 0;
  right: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background-color: vars.$bg-color;
}

.loading-text,
.error-text,
.empty-text {
  font-size: 28rpx;
  color: vars.$text-muted;
}

.retry-button {
  margin-top: vars.$spacing-lg;
  padding: vars.$spacing-md vars.$spacing-xl;
  background-color: vars.$primary-color;
  color: #ffffff;
  border-radius: vars.$border-radius-sm;
  font-size: 28rpx;
  border: none;
}

.message-list {
  position: fixed;
  left: 0;
  right: 0;
  padding: vars.$spacing-md;
  background-color: vars.$bg-color;
  box-sizing: border-box;
  -webkit-overflow-scrolling: touch;
}

.empty-hint {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: vars.$spacing-xl;
}

.loading-more {
  display: flex;
  justify-content: center;
  padding: vars.$spacing-md;
}

.loading-spinner {
  width: 40rpx;
  height: 40rpx;
  border: 4rpx solid vars.$bg-color;
  border-top-color: vars.$primary-color;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.scroll-anchor {
  height: 1px;
}

.date-separator {
  display: flex;
  justify-content: center;
  margin: vars.$spacing-lg 0;
}

.date-text {
  font-size: 24rpx;
  color: vars.$text-muted;
  background-color: vars.$pale-blue;
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
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
}

.reply-preview {
  display: flex;
  align-items: flex-start;
  gap: vars.$spacing-xs;
  margin-bottom: vars.$spacing-xs;
  padding: vars.$spacing-xs vars.$spacing-sm;
  background-color: rgba(0, 0, 0, 0.05);
  border-radius: vars.$border-radius-sm;
  border-left: 4rpx solid vars.$primary-color;
}

.message-sent .reply-preview {
  background-color: rgba(255, 255, 255, 0.2);
  border-left-color: rgba(255, 255, 255, 0.8);
}

.reply-line {
  width: 4rpx;
  background-color: vars.$primary-color;
  border-radius: 2rpx;
  flex-shrink: 0;
}

.message-sent .reply-line {
  background-color: rgba(255, 255, 255, 0.8);
}

.reply-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4rpx;
  min-width: 0;
}

.reply-sender {
  font-size: 22rpx;
  font-weight: 600;
  color: vars.$primary-color;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.message-sent .reply-sender {
  color: rgba(255, 255, 255, 0.9);
}

.reply-text {
  font-size: 24rpx;
  color: vars.$text-muted;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  line-height: 1.4;
}

.message-sent .reply-text {
  color: rgba(255, 255, 255, 0.8);
}

.message-file {
  margin-bottom: vars.$spacing-xs;
}

.message-image {
  max-width: 400rpx;
  max-height: 600rpx;
  border-radius: vars.$border-radius-sm;
}

.file-link {
  font-size: 28rpx;
  color: vars.$primary-color;
  text-decoration: underline;
}

.message-sent .file-link {
  color: rgba(255, 255, 255, 0.9);
}

.message-content {
  font-size: 30rpx;
  line-height: 1.5;
  word-wrap: break-word;
  word-break: break-word;
}

.message-sent .message-content {
  color: #ffffff;
}

.message-received .message-content {
  color: vars.$text-color;
}

.message-footer {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: vars.$spacing-xs;
  margin-top: vars.$spacing-xs;
}

.message-time {
  font-size: 20rpx;
  line-height: 1;
}

.message-sent .message-time {
  color: rgba(255, 255, 255, 0.7);
}

.message-received .message-time {
  color: vars.$text-muted;
}

.read-indicator {
  font-size: 20rpx;
  line-height: 1;
  color: rgba(255, 255, 255, 0.7);
  
  &.unread {
    color: rgba(255, 255, 255, 0.5);
  }
}

.input-container {
  position: fixed;
  left: 0;
  right: 0;
  display: flex;
  flex-direction: column;
  padding-bottom: env(safe-area-inset-bottom);
  background-color: vars.$surface-color;
  border-top: 1rpx solid vars.$bg-color;
  z-index: 100;
}

.reply-bar {
  padding: vars.$spacing-sm vars.$spacing-lg;
  background-color: vars.$pale-blue;
  border-bottom: 1rpx solid vars.$bg-color;
}

.reply-bar-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: vars.$spacing-md;
}

.reply-bar-text {
  flex: 1;
  font-size: 26rpx;
  color: vars.$text-muted;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.reply-bar-close {
  font-size: 40rpx;
  color: vars.$text-muted;
  line-height: 1;
  width: 48rpx;
  height: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  
  &:active {
    opacity: 0.6;
  }
}

.input-row {
  display: flex;
  align-items: center;
  padding: vars.$spacing-md vars.$spacing-lg;
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
