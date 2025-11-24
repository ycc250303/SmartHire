<template>
  <view class="chat-page">
    <view class="chat-header">
      <view class="title">{{ chatTitle }}</view>
      <button class="outline" @click="showSummary">AI 对话总结（预览）</button>
    </view>

    <scroll-view scroll-y class="chat-body" :scroll-into-view="lastMessageId">
      <view v-for="message in messages" :key="message.id" :id="message.id" class="bubble" :class="message.from">
        <text class="content">{{ message.content }}</text>
        <text class="time">{{ message.time }}</text>
      </view>
    </scroll-view>

    <view class="chat-input">
      <input v-model="draft" placeholder="输入消息..." @confirm="sendMessage" />
      <button class="send" @click="sendMessage">发送</button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { fetchChatHistory, mockConversations, type MessageBubble } from '@/mock/hr';

const messages = ref<MessageBubble[]>([]);
const draft = ref('');
const chatTitle = ref('候选人对话');
const lastMessageId = ref('');

const loadChat = async () => {
  // TODO: GET /api/hr/messages/:chatId
  messages.value = await fetchChatHistory();
  setTimeout(() => {
    lastMessageId.value = messages.value[messages.value.length - 1]?.id || '';
  }, 0);
};

const sendMessage = () => {
  if (!draft.value.trim()) return;
  const newMessage: MessageBubble = {
    id: `msg-${Date.now()}`,
    from: 'hr',
    content: draft.value,
    time: new Date().toLocaleTimeString(),
  };
  messages.value = [...messages.value, newMessage];
  draft.value = '';
  lastMessageId.value = newMessage.id;
  // TODO: POST /api/hr/messages/send
};

const showSummary = () => {
  uni.showToast({ title: 'LLM 总结功能待接入', icon: 'none' });
};

onLoad((options) => {
  const chatId = options?.id as string;
  const meta = mockConversations.find((item) => item.id === chatId);
  if (meta) {
    chatTitle.value = `${meta.candidate} · ${meta.jobTitle}`;
  }
  loadChat();
});
</script>

<style scoped lang="scss">
.chat-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
}

.chat-header {
  padding: 20rpx 24rpx;
  background: #fff;
  box-shadow: 0 8rpx 16rpx rgba(0, 0, 0, 0.04);
}

.title {
  font-size: 30rpx;
  font-weight: 600;
  margin-bottom: 12rpx;
}

button.outline {
  border: 2rpx solid #2f7cff;
  color: #2f7cff;
  border-radius: 16rpx;
  padding: 10rpx 20rpx;
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
}

.chat-input input {
  flex: 1;
  padding: 20rpx;
  border-radius: 16rpx;
  background: #f6f7fb;
  border: none;
}

button.send {
  width: 140rpx;
  background: #2f7cff;
  color: #fff;
  border-radius: 16rpx;
  border: none;
}
</style>
