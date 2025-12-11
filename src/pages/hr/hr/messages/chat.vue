<template>
  <view class="chat-page">
    <view class="chat-header">
      <view class="title">{{ chatTitle }}</view>
    </view>

    <scroll-view
      scroll-y
      class="chat-body"
      :scroll-into-view="scrollIntoView"
      :scroll-with-animation="true"
    >
      <view
        v-for="message in sortedMessages"
        :key="message.id"
        :id="`msg-${message.id}`"
        class="bubble"
        :class="message.senderId === otherUserId ? 'candidate' : 'hr'"
      >
        <text class="content">{{ message.content }}</text>
        <text class="time">{{ formatTime(message.createdAt) }}</text>
      </view>
    </scroll-view>

    <view class="chat-input">
      <input
        v-model="draft"
        placeholder="输入消息..."
        :disabled="sending || loading"
        :adjust-position="false"
        :cursor-spacing="16"
        confirm-type="send"
        @confirm="doSendMessage"
      />
      <button class="send" :disabled="sending || loading" @click="doSendMessage">发送</button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick } from 'vue';
import { onLoad, onShow } from '@dcloudio/uni-app';
import dayjs from 'dayjs';
import { getChatHistory, sendMessage, markAsRead, type Message } from '@/services/api/message';
import { t } from '@/locales';

const messages = ref<Message[]>([]);
const draft = ref('');
const chatTitle = ref(t('pages.chat.conversation.title'));
const scrollIntoView = ref('');
const conversationId = ref<number>(0);
const otherUserId = ref<number>(0);
const applicationId = ref<number>(0);
const loading = ref(false);
const sending = ref(false);

const sortedMessages = computed(() => messages.value);

const loadChat = async () => {
  if (!conversationId.value) return;
  loading.value = true;
  try {
    const data = await getChatHistory({
      conversationId: conversationId.value,
      page: 1,
      size: 20,
    });
    messages.value = data;
    if (!otherUserId.value && messages.value.length > 0) {
      const last = messages.value[messages.value.length - 1];
      const candidateId = last.senderId !== 0 ? last.senderId : last.receiverId;
      if (candidateId) {
        otherUserId.value = candidateId;
      }
    }
    nextTick(() => {
      const lastId = messages.value[messages.value.length - 1]?.id;
      scrollIntoView.value = lastId ? `msg-${lastId}` : '';
    });
    if (conversationId.value) {
      await markAsRead(conversationId.value);
    }
  } catch (err) {
    console.error('Failed to load chat history:', err);
    uni.showToast({ title: t('pages.chat.conversation.loadError'), icon: 'none' });
  } finally {
    loading.value = false;
  }
};

const doSendMessage = async () => {
  if (!draft.value.trim() || !conversationId.value) return;
  if (!otherUserId.value) {
    uni.showToast({ title: '缺少收件人', icon: 'none' });
    return;
  }
  const content = draft.value.trim();
  sending.value = true;
  const tempId = Date.now();
  const tempMessage: Message = {
    id: tempId,
    conversationId: conversationId.value,
    senderId: 0,
    receiverId: otherUserId.value,
    messageType: 1,
    content,
    fileUrl: null,
    replyTo: null,
    createdAt: new Date().toISOString(),
    isRead: 0,
    replyContent: null,
    replyMessageType: null,
  };
  messages.value = [...messages.value, tempMessage];
  draft.value = '';
  nextTick(() => {
    scrollIntoView.value = `msg-${tempId}`;
  });

  try {
    const sent = await sendMessage({
      receiverId: otherUserId.value,
      applicationId: applicationId.value || 0,
      messageType: 1,
      content,
      fileUrl: null,
      replyTo: null,
    });
    messages.value = messages.value.map((m) => (m.id === tempMessage.id ? sent : m));
    nextTick(() => {
      scrollIntoView.value = `msg-${sent.id}`;
    });
  } catch (err) {
    console.error('Failed to send message:', err);
    messages.value = messages.value.filter((m) => m.id !== tempMessage.id);
    uni.showToast({ title: t('pages.chat.conversation.sendError'), icon: 'none' });
  } finally {
    sending.value = false;
  }
};

const formatTime = (time: string) => dayjs(time).format('MM/DD HH:mm');

onLoad((options) => {
  if (options?.id) {
    conversationId.value = parseInt(options.id as string, 10);
  }
  if (options?.userId) {
    otherUserId.value = parseInt(options.userId as string, 10);
  }
  if (options?.applicationId) {
    applicationId.value = parseInt(options.applicationId as string, 10);
  }
  if (options?.username) {
    try {
      chatTitle.value = decodeURIComponent(options.username as string);
    } catch {
      chatTitle.value = options.username as string;
    }
  }
  loadChat();
});

onShow(() => {
  loadChat();
});

onMounted(() => {
  loadChat();
});
</script>

<style scoped lang="scss">
.chat-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  padding-top: calc(var(--status-bar-height) + 32rpx);
  box-sizing: border-box;
}

.chat-header {
  padding: 20rpx 24rpx;
  background: #fff;
  box-shadow: 0 8rpx 16rpx rgba(0, 0, 0, 0.04);
  display: flex;
  align-items: center;
  justify-content: center;
}

.title {
  font-size: 30rpx;
  font-weight: 600;
  margin-bottom: 0;
}

.chat-body {
  flex: 1;
  padding: 24rpx;
  background: #f6f7fb;
}

.bubble {
  max-width: 80%;
  padding: 20rpx;
  border-radius: 20rpx;
  margin-bottom: 16rpx;
  display: flex;
  flex-direction: column;
}

.bubble.candidate {
  align-self: flex-start;
  background: #fff;
}

.bubble.hr {
  align-self: flex-end;
  background: #2f7cff;
  color: #fff;
}

.time {
  font-size: 22rpx;
  margin-top: 8rpx;
  opacity: 0.7;
}

.chat-input {
  display: flex;
  padding: 16rpx 24rpx;
  background: #fff;
  gap: 12rpx;
  align-items: center;
  border-top: 1rpx solid #f0f0f0;
  padding-bottom: calc(16rpx + env(safe-area-inset-bottom));
}

.chat-input input {
  flex: 1;
  height: 72rpx;
  padding: 0 20rpx;
  border-radius: 16rpx;
  background: #f6f7fb;
  border: none;
  font-size: 28rpx;
  line-height: 72rpx;
}

button.send {
  width: 140rpx;
  background: #2f7cff;
  color: #fff;
  border-radius: 16rpx;
  border: none;
}
</style>
