<template>
  <view class="conversation-page">
    <view class="conversation-header" id="chat-header">
      <view class="header-back" @click="handleBack">
        <text class="back-icon">‹</text>
      </view>
      <view class="header-content">
        <text class="header-username">{{ displayUsername }}</text>
        <text v-if="headerSubtitle" class="header-subtitle">{{ headerSubtitle }}</text>
      </view>
    </view>

    <view v-if="jobInfo" class="job-info-card">
      <view class="job-info-content">
        <text class="job-title-text">{{ jobInfo.jobTitle }}</text>
        <view class="job-meta-info">
          <text class="job-meta-item" v-if="jobInfo.salary">{{ jobInfo.salary }}</text>
          <text class="job-meta-separator" v-if="jobInfo.salary && jobInfo.city">·</text>
          <text class="job-meta-item" v-if="jobInfo.city">{{ jobInfo.city }}</text>
          <text class="job-meta-separator" v-if="jobInfo.city && jobInfo.jobTypeText">·</text>
          <text class="job-meta-item" v-if="jobInfo.jobTypeText">{{ jobInfo.jobTypeText }}</text>
        </view>
        <text class="company-name-text" v-if="jobInfo.companyName">{{ jobInfo.companyName }}</text>
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
          <image
            v-if="!isSentByMe(message)"
            class="message-avatar"
            :src="getAvatarUrl(otherUserAvatar)"
            mode="aspectFill"
          />
          <view class="message-bubble">
            <view v-if="message.replyTo && message.replyContent" class="reply-preview">
              <view class="reply-line"></view>
              <view class="reply-content">
                <text class="reply-sender">{{ isSentByMe(message) ? t('pages.chat.conversation.you') : displayUsername }}</text>
                <text class="reply-text">{{ message.replyContent }}</text>
              </view>
            </view>
            <view v-if="message.fileUrl" class="message-file">
              <image
                v-if="isImage(message.fileUrl)"
                :src="message.fileUrl"
                mode="aspectFit"
                class="message-image"
                @click="previewImage(message.fileUrl)"
              />
              <view v-else class="file-card" @click="downloadFile(message.fileUrl)">
                <view class="file-icon">📄</view>
                <view class="file-meta">
                  <text class="file-name">{{ getFileName(message.fileUrl) }}</text>
                  <text v-if="getFileExt(message.fileUrl)" class="file-size">{{ getFileExt(message.fileUrl) }}</text>
                </view>
              </view>
            </view>
            <text v-if="message.content && !message.fileUrl" class="message-content">{{ message.content }}</text>
            <view class="message-footer">
              <text class="message-time">{{ formatMessageTime(message.createdAt) }}</text>
              <text v-if="isSentByMe(message) && message.isRead === 1" class="read-indicator">✓✓</text>
              <text v-else-if="isSentByMe(message)" class="read-indicator unread">✓</text>
            </view>
          </view>
          <image
            v-if="isSentByMe(message)"
            class="message-avatar"
            :src="getAvatarUrl(currentUserAvatar)"
            mode="aspectFill"
          />
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
        <view class="attachment-icon" @click="showAttachmentOptions">
          <text class="icon-text">+</text>
        </view>
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
          @click="handleSendClick"
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
import { getChatHistory, sendMessage, sendMedia, markAsRead, getConversations, type Message } from '@/services/api/message';
import { useUserStore } from '@/store/user';
import { useImageUrl } from '@/utils/useImageUrl';
import { getCurrentUserInfo } from '@/services/api/user';

const userStore = useUserStore();
const { processImageUrl } = useImageUrl();

const conversationId = ref<number>(0);
const otherUserId = ref<number>(0);
const otherUsername = ref<string>('');
const otherUserAvatar = ref<string>('');
const currentUserAvatar = ref<string>('');
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

interface JobInfo {
  jobTitle: string;
  salary?: string;
  city?: string;
  jobTypeText?: string;
  companyName?: string;
}

const jobInfo = ref<JobInfo | null>(null);

const keyboardHeight = ref(0);
const headerHeight = ref(88);
const inputHeight = ref(60);
const safeAreaBottom = ref(0);
const useScrollAnimation = ref(false);
const inputFocused = ref(false);

const inputContainerStyle = computed(() => ({
  bottom: `${keyboardHeight.value}px`
}));

const messageListStyle = computed(() => {
  const jobCardHeight = jobInfo.value ? 120 : 0;
  return {
    top: `${headerHeight.value + jobCardHeight}px`,
    bottom: `${inputHeight.value + keyboardHeight.value + safeAreaBottom.value}px`
  };
});

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

const headerSubtitle = computed(() => {
  if (jobInfo.value?.companyName) return jobInfo.value.companyName;
  return '';
});

onLoad((options: any) => {
  console.log('Conversation onLoad options:', options);
  
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
  if (options?.avatar && options.avatar !== 'undefined') {
    try {
      otherUserAvatar.value = decodeURIComponent(options.avatar);
    } catch (e) {
      otherUserAvatar.value = options.avatar;
    }
  }
  if (options?.applicationId) {
    applicationId.value = parseInt(options.applicationId);
  }
  
  if (options?.jobTitle) {
    try {
      const jobType = options.jobType ? parseInt(options.jobType) : undefined;
      const jobTypeText = jobType === 0 ? t('pages.jobs.fulltime') : jobType === 1 ? t('pages.jobs.internship') : jobType === 2 ? t('pages.jobs.parttime') : undefined;
      
      jobInfo.value = {
        jobTitle: decodeURIComponent(options.jobTitle),
        salary: options.salary ? decodeURIComponent(options.salary) : undefined,
        city: options.city ? decodeURIComponent(options.city) : undefined,
        jobTypeText: jobTypeText,
        companyName: options.companyName ? decodeURIComponent(options.companyName) : undefined,
      };
      console.log('Job info set:', jobInfo.value);
    } catch (e) {
      console.error('Failed to parse job info:', e);
    }
  }
  
  uni.setNavigationBarTitle({
    title: displayUsername.value
  });
});

onShow(() => {
  loadMessages();
  markMessagesAsRead();
});

onMounted(() => {
  initLayout();
  loadMessages();
  ensureAvatarInfo();
});

async function ensureAvatarInfo() {
  if (!currentUserAvatar.value) {
    try {
      if (userStore.userInfo?.avatarUrl) {
        currentUserAvatar.value = userStore.userInfo.avatarUrl;
      } else {
        const userInfo = await getCurrentUserInfo();
        currentUserAvatar.value = userInfo?.avatarUrl || '';
      }
    } catch (err) {
      console.error('Failed to load current user avatar:', err);
    }
  }

  if (!otherUserAvatar.value && conversationId.value) {
    try {
      const conversations = await getConversations();
      const currentConversation = conversations.find(c => c.id === conversationId.value);
      if (currentConversation) {
        otherUserAvatar.value = currentConversation.otherUserAvatar || '';
      }
    } catch (err) {
      console.error('Failed to load other user avatar:', err);
    }
  }
}

function getAvatarUrl(avatarUrl: string | null | undefined): string {
  return processImageUrl(avatarUrl);
}

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

async function ensureConversationId(): Promise<boolean> {
  if (conversationId.value) return true;
  if (!otherUserId.value) return false;
  try {
    const list = await getConversations();
    const found = list.find(c => c.otherUserId === otherUserId.value || (c.applicationId && c.applicationId === applicationId.value));
    if (found) {
      conversationId.value = found.id;
      if (found.applicationId) {
        applicationId.value = found.applicationId;
      }
      return true;
    }
  } catch (err) {
    console.error('Failed to resolve conversation id:', err);
  }
  return false;
}

async function loadMessages() {
  if (!conversationId.value) {
    const resolved = await ensureConversationId();
    if (!resolved) return;
  }
  
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
    
    await ensureAvatarInfo();
    
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
  console.log('handleSend called', { canSend: canSend.value, conversationId: conversationId.value, otherUserId: otherUserId.value, applicationId: applicationId.value });
  
  if (!canSend.value) {
    console.log('Early return: canSend check failed');
    return;
  }
  
  if (!otherUserId.value) {
    try {
      const conversations = await getConversations();
      const currentConversation = conversationId.value ? conversations.find(c => c.id === conversationId.value) : undefined;
      if (currentConversation) {
        otherUserId.value = currentConversation.otherUserId;
        if (currentConversation.applicationId) {
          applicationId.value = currentConversation.applicationId;
        }
      } else {
        uni.showToast({
          title: t('pages.chat.conversation.sendError'),
          icon: 'none'
        });
        return;
      }
    } catch (err) {
      console.error('Failed to get conversations:', err);
      uni.showToast({
        title: t('pages.chat.conversation.sendError'),
        icon: 'none'
      });
      return;
    }
  }
  
  const content = inputText.value.trim();
  if (!content) {
    console.log('No content to send');
    return;
  }
  
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
    
    if (!conversationId.value && sentMessage.conversationId) {
      conversationId.value = sentMessage.conversationId;
    }
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
  console.log('handleSendClick called', { canSend: canSend.value, sending: sending.value, otherUserId: otherUserId.value, conversationId: conversationId.value });
  if (!canSend.value || sending.value) {
    console.log('Cannot send:', { canSend: canSend.value, sending: sending.value });
    return;
  }
  if (!conversationId.value) {
    console.log('No conversationId');
    return;
  }
  handleSend();
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

function getFileName(url: string | null): string {
  if (!url) return 'file';
  const parts = url.split('/');
  const last = parts[parts.length - 1] || 'file';
  const decoded = decodeURIComponent(last);
  return decoded.length > 28 ? `${decoded.slice(0, 25)}...` : decoded;
}

function getFileExt(url: string | null): string {
  if (!url) return '';
  const match = url.toLowerCase().match(/\.([a-z0-9]+)(\?|#|$)/);
  return match && match[1] ? match[1].toUpperCase() : '';
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

function showAttachmentOptions() {
  if (sending.value) return;
  
  uni.showActionSheet({
    itemList: [t('pages.chat.conversation.sendImage'), t('pages.chat.conversation.sendFile')],
    success: (res) => {
      if (res.tapIndex === 0) {
        chooseImage();
      } else if (res.tapIndex === 1) {
        chooseFile();
      }
    }
  });
}

function chooseImage() {
  // #ifdef H5
  const input = document.createElement('input');
  input.type = 'file';
  input.accept = 'image/*';
  input.onchange = (e: any) => {
    const file = e.target.files?.[0];
    if (file) {
      const reader = new FileReader();
      reader.onload = (event: any) => {
        sendMediaMessage(event.target.result, 2);
      };
      reader.readAsDataURL(file);
    }
  };
  input.click();
  // #endif
  
  // #ifndef H5
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: (res) => {
      if (res.tempFilePaths && res.tempFilePaths[0]) {
        sendMediaMessage(res.tempFilePaths[0], 2);
      }
    }
  });
  // #endif
}

function chooseFile() {
  // #ifdef H5
  const input = document.createElement('input');
  input.type = 'file';
  input.accept = '.pdf,.doc,.docx,.xls,.xlsx,.ppt,.pptx';
  input.onchange = (e: any) => {
    const file = e.target.files?.[0];
    if (file) {
      const reader = new FileReader();
      reader.onload = (event: any) => {
        sendMediaMessage(event.target.result, 3);
      };
      reader.readAsDataURL(file);
    }
  };
  input.click();
  // #endif
  
  // #ifndef H5
  uni.chooseMessageFile({
    count: 1,
    type: 'file',
    success: (res) => {
      if (res.tempFiles && res.tempFiles[0] && res.tempFiles[0].path) {
        sendMediaMessage(res.tempFiles[0].path, 3);
      }
    }
  });
  // #endif
}

async function sendMediaMessage(filePath: string, messageType: number) {
  if (!otherUserId.value) {
    uni.showToast({ title: t('pages.chat.conversation.sendError'), icon: 'none' });
    return;
  }
  
  sending.value = true;
  
  try {
    const message = await sendMedia({
      receiverId: otherUserId.value,
      applicationId: applicationId.value || 0,
      messageType,
      filePath
    });
    
    if (!conversationId.value && message.conversationId) {
      conversationId.value = message.conversationId;
    }
    messages.value.push(message);
    scrollToBottom(true);
  } catch (err) {
    console.error('Failed to send media:', err);
    uni.showToast({ title: t('pages.chat.conversation.sendError'), icon: 'none' });
  } finally {
    sending.value = false;
  }
}

function previewImage(url: string) {
  uni.previewImage({ urls: [url], current: url });
}

function downloadFile(url: string) {
  // #ifdef H5
  window.open(url, '_blank');
  // #endif
  
  // #ifndef H5
  uni.showLoading({ title: t('pages.chat.conversation.downloading') });
  uni.downloadFile({
    url,
    success: (res) => {
      uni.hideLoading();
      if (res.statusCode === 200) {
        uni.openDocument({
          filePath: res.tempFilePath,
          showMenu: true,
          fail: () => uni.showToast({ title: t('pages.chat.conversation.openFileError'), icon: 'none' })
        });
      }
    },
    fail: () => {
      uni.hideLoading();
      uni.showToast({ title: t('pages.chat.conversation.downloadError'), icon: 'none' });
    }
  });
  // #endif
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
  flex-direction: column;
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

.header-subtitle {
  margin-top: 4rpx;
  font-size: 24rpx;
  color: vars.$text-muted;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.job-info-card {
  position: fixed;
  top: calc(var(--status-bar-height) + 88rpx);
  left: 0;
  right: 0;
  background-color: vars.$surface-color;
  border-bottom: 1rpx solid vars.$bg-color;
  z-index: 99;
  padding: vars.$spacing-md vars.$spacing-lg;
  box-sizing: border-box;
}

.job-info-content {
  display: flex;
  flex-direction: column;
  gap: vars.$spacing-xs;
}

.job-title-text {
  font-size: 30rpx;
  font-weight: 600;
  color: vars.$text-color;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.job-meta-info {
  display: flex;
  align-items: center;
  gap: vars.$spacing-xs;
  font-size: 24rpx;
  color: vars.$text-muted;
}

.job-meta-item {
  color: vars.$text-muted;
}

.job-meta-separator {
  color: vars.$text-muted;
}

.company-name-text {
  font-size: 24rpx;
  color: vars.$text-muted;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
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
  align-items: flex-end;
  margin-bottom: vars.$spacing-md;
  gap: vars.$spacing-sm;
  
  &.message-sent {
    justify-content: flex-end;
  }
  
  &.message-received {
    justify-content: flex-start;
  }
}

.message-avatar {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  background-color: vars.$bg-color;
  flex-shrink: 0;
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
  
  &:active {
    opacity: 0.8;
  }
}

.file-card {
  display: flex;
  align-items: center;
  gap: vars.$spacing-md;
  padding: vars.$spacing-md vars.$spacing-lg;
  background-color: rgba(0, 0, 0, 0.05);
  border-radius: vars.$border-radius-sm;
  min-width: 260rpx;
  max-width: 420rpx;
  box-sizing: border-box;
  
  &:active {
    opacity: 0.8;
  }
}

.message-sent .file-card {
  background-color: rgba(255, 255, 255, 0.2);
}

.file-icon {
  font-size: 48rpx;
  line-height: 1;
}

.file-meta {
  display: flex;
  flex-direction: column;
  gap: 6rpx;
  min-width: 0;
}

.file-name {
  font-size: 28rpx;
  color: vars.$text-color;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.message-sent .file-name {
  color: rgba(255, 255, 255, 0.9);
}

.file-size {
  font-size: 22rpx;
  color: vars.$text-muted;
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

.attachment-icon {
  width: 72rpx;
  height: 72rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: vars.$bg-color;
  border-radius: vars.$border-radius-sm;
  flex-shrink: 0;
  
  &:active {
    opacity: 0.6;
  }
}

.icon-text {
  font-size: 48rpx;
  color: vars.$text-muted;
  line-height: 1;
  font-weight: 300;
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
