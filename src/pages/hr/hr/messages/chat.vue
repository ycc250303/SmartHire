<template>
  <view class="chat-page">
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
        class="message-row"
        :class="isFromOther(message) ? 'other' : 'mine'"
      >
        <image
          v-if="isFromOther(message)"
          class="avatar"
          :src="otherUserAvatar || '/static/user-avatar.png'"
          mode="aspectFill"
        />
        <view class="bubble" :class="bubbleClass(message)">
          <template v-if="isImageMessage(message)">
            <image
              v-if="message.fileUrl"
              class="image"
              :src="message.fileUrl"
              mode="widthFix"
              @click="previewImage(message.fileUrl)"
            />
            <text v-else class="content">{{
              message.content || "[图片]"
            }}</text>
          </template>
          <text v-else class="content">{{ message.content }}</text>
          <text class="time">{{ formatTime(message.createdAt) }}</text>
        </view>
        <image
          v-if="!isFromOther(message)"
          class="avatar"
          :src="currentUserAvatar || '/static/user-avatar.png'"
          mode="aspectFill"
        />
      </view>
    </scroll-view>

    <view class="chat-input">
      <button class="media" :disabled="sending || loading" @click="doSendImage">
        图片
      </button>
      <input
        v-model="draft"
        placeholder="输入消息..."
        :disabled="sending || loading"
        :adjust-position="false"
        :cursor-spacing="16"
        confirm-type="send"
        @confirm="doSendMessage"
      />
      <button
        class="send"
        :disabled="sending || loading"
        @click="doSendMessage"
      >
        发送
      </button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick } from "vue";
import { onLoad, onShow } from "@dcloudio/uni-app";
import dayjs from "dayjs";
import {
  getChatHistory,
  sendMessage,
  sendMedia,
  markAsRead,
  getConversations,
  type Message,
} from "@/services/api/message";
import { getCurrentUserInfo } from "@/services/api/user";
import { t } from "@/locales";

const messages = ref<Message[]>([]);
const draft = ref("");
const chatTitle = ref(t("pages.chat.conversation.title"));
const scrollIntoView = ref("");
const conversationId = ref<number>(0);
const otherUserId = ref<number>(0);
const applicationId = ref<number>(0);
const otherUserAvatar = ref<string>("");
const currentUserAvatar = ref<string>("");
const loading = ref(false);
const sending = ref(false);

const sortedMessages = computed(() => {
  const copied = [...messages.value];
  copied.sort((a, b) => {
    const ta = a.createdAt ? new Date(a.createdAt).getTime() : 0;
    const tb = b.createdAt ? new Date(b.createdAt).getTime() : 0;
    if (ta !== tb) return ta - tb;
    return a.id - b.id;
  });
  return copied;
});

const isImageMessage = (message: Message) => message.messageType === 2;
const isFromOther = (message: Message) =>
  !!otherUserId.value && message.senderId === otherUserId.value;

const bubbleClass = (message: Message) => {
  const base = isFromOther(message) ? "other" : "mine";
  return isImageMessage(message) ? `${base} image-bubble` : base;
};

const previewImage = (url: string) => {
  if (!url) return;
  uni.previewImage({
    current: url,
    urls: [url],
  });
};

const ensureConversationMeta = async () => {
  if (!conversationId.value) return;
  if (
    otherUserId.value &&
    applicationId.value &&
    otherUserAvatar.value &&
    currentUserAvatar.value
  )
    return;

  if (!applicationId.value) {
    const cachedByConv = Number(
      uni.getStorageSync(`hr_chat_app_by_conv_${conversationId.value}`)
    );
    if (Number.isFinite(cachedByConv) && cachedByConv > 0)
      applicationId.value = cachedByConv;
  }

  if (!applicationId.value && otherUserId.value) {
    const cachedByUser = Number(
      uni.getStorageSync(`hr_chat_app_by_user_${otherUserId.value}`)
    );
    if (Number.isFinite(cachedByUser) && cachedByUser > 0)
      applicationId.value = cachedByUser;
  }

  try {
    if (!currentUserAvatar.value) {
      const me = await getCurrentUserInfo();
      currentUserAvatar.value = me?.avatarUrl || "";
    }
  } catch {}

  try {
    const list = await getConversations();
    const matched = list.find((c) => c.id === conversationId.value);
    if (!matched) return;
    if (!otherUserId.value) otherUserId.value = matched.otherUserId;
    if (!applicationId.value && matched.applicationId)
      applicationId.value = matched.applicationId;
    if (!otherUserAvatar.value)
      otherUserAvatar.value = matched.otherUserAvatar || "";
    if (
      !chatTitle.value ||
      chatTitle.value === t("pages.chat.conversation.title")
    ) {
      chatTitle.value = matched.otherUserName || chatTitle.value;
      uni.setNavigationBarTitle({ title: chatTitle.value });
    }
  } catch {}
};

const loadChat = async () => {
  if (!conversationId.value) return;
  loading.value = true;
  try {
    await ensureConversationMeta();
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
    if (!otherUserAvatar.value && otherUserId.value) {
      await ensureConversationMeta();
    }
    nextTick(() => {
      const lastId = messages.value[messages.value.length - 1]?.id;
      scrollIntoView.value = lastId ? `msg-${lastId}` : "";
    });
    if (conversationId.value) {
      await markAsRead(conversationId.value);
    }
  } catch (err) {
    console.error("Failed to load chat history:", err);
    uni.showToast({
      title: t("pages.chat.conversation.loadError"),
      icon: "none",
    });
  } finally {
    loading.value = false;
  }
};

const doSendMessage = async () => {
  if (!draft.value.trim() || !conversationId.value) return;
  if (!otherUserId.value) {
    uni.showToast({ title: "缺少收件人", icon: "none" });
    return;
  }
  if (!applicationId.value) {
    await ensureConversationMeta();
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
  draft.value = "";
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
    messages.value = messages.value.map((m) =>
      m.id === tempMessage.id ? sent : m
    );
    nextTick(() => {
      scrollIntoView.value = `msg-${sent.id}`;
    });
  } catch (err) {
    console.error("Failed to send message:", err);
    messages.value = messages.value.filter((m) => m.id !== tempMessage.id);
    uni.showToast({
      title: t("pages.chat.conversation.sendError"),
      icon: "none",
    });
  } finally {
    sending.value = false;
  }
};

const doSendImage = async () => {
  if (!conversationId.value) return;
  if (!otherUserId.value) {
    uni.showToast({ title: "缺少收件人", icon: "none" });
    return;
  }
  if (!applicationId.value) {
    await ensureConversationMeta();
  }

  const pick = await new Promise<{ tempFilePaths: string[] } | null>(
    (resolve) => {
      uni.chooseImage({
        count: 1,
        sizeType: ["compressed"],
        sourceType: ["album", "camera"],
        success: (res) => resolve(res),
        fail: () => resolve(null),
      });
    }
  );
  const filePath = pick?.tempFilePaths?.[0];
  if (!filePath) return;

  sending.value = true;
  const tempId = Date.now();
  const tempMessage: Message = {
    id: tempId,
    conversationId: conversationId.value,
    senderId: 0,
    receiverId: otherUserId.value,
    messageType: 2,
    content: "[图片]",
    fileUrl: filePath,
    replyTo: null,
    createdAt: new Date().toISOString(),
    isRead: 0,
    replyContent: null,
    replyMessageType: null,
  };
  messages.value = [...messages.value, tempMessage];
  nextTick(() => {
    scrollIntoView.value = `msg-${tempId}`;
  });

  try {
    const sent = await sendMedia({
      receiverId: otherUserId.value,
      applicationId: applicationId.value || 0,
      messageType: 2,
      filePath,
    });
    messages.value = messages.value.map((m) =>
      m.id === tempMessage.id ? sent : m
    );
    nextTick(() => {
      scrollIntoView.value = `msg-${sent.id}`;
    });
  } catch (err) {
    console.error("Failed to send image:", err);
    messages.value = messages.value.filter((m) => m.id !== tempMessage.id);
    uni.showToast({ title: "图片发送失败", icon: "none" });
  } finally {
    sending.value = false;
  }
};

const formatTime = (time: string) => dayjs(time).format("MM/DD HH:mm");

onLoad((options) => {
  if (options?.id) {
    conversationId.value = parseInt(options.id as string, 10);
  }
  if (options?.userId) {
    otherUserId.value = parseInt(options.userId as string, 10);
  }
  if (options?.applicationId) {
    applicationId.value = parseInt(options.applicationId as string, 10);
    if (applicationId.value) {
      uni.setStorageSync(
        `hr_chat_app_by_conv_${conversationId.value}`,
        applicationId.value
      );
      if (otherUserId.value)
        uni.setStorageSync(
          `hr_chat_app_by_user_${otherUserId.value}`,
          applicationId.value
        );
    }
  }
  if (options?.avatar) {
    try {
      otherUserAvatar.value = decodeURIComponent(options.avatar as string);
    } catch {
      otherUserAvatar.value = options.avatar as string;
    }
  }
  if (options?.username) {
    try {
      chatTitle.value = decodeURIComponent(options.username as string);
    } catch {
      chatTitle.value = options.username as string;
    }
  }
  uni.setNavigationBarTitle({
    title: chatTitle.value || t("pages.chat.conversation.title"),
  });
  ensureConversationMeta();
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
  background: #f6f7fb;
  box-sizing: border-box;
}

.chat-body {
  flex: 1;
  padding: 24rpx;
  padding-bottom: calc(24rpx + 120rpx + env(safe-area-inset-bottom));
}

.message-row {
  display: flex;
  align-items: flex-end;
  margin-bottom: 16rpx;
  gap: 12rpx;
}

.message-row.other {
  justify-content: flex-start;
}

.message-row.mine {
  justify-content: flex-end;
}

.avatar {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.05);
  flex-shrink: 0;
}

.bubble {
  max-width: 72%;
  padding: 18rpx 20rpx;
  border-radius: 20rpx;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.image {
  width: 440rpx;
  max-width: 100%;
  border-radius: 16rpx;
  background: rgba(0, 0, 0, 0.03);
}

.bubble.other {
  background: #fff;
  color: #111827;
}

.bubble.mine {
  background: #2f7cff;
  color: #fff;
}

.bubble.image-bubble {
  padding: 0;
  background: transparent;
}

.bubble.image-bubble .time {
  color: #6b7280;
}

.bubble.image-bubble .content {
  color: #111827;
}

.time {
  font-size: 22rpx;
  opacity: 0.7;
  align-self: flex-end;
}

.chat-input {
  display: flex;
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 10;
  padding: 14rpx 20rpx;
  background: #fff;
  gap: 12rpx;
  align-items: center;
  border-top: 1rpx solid #f0f0f0;
  box-shadow: 0 -10rpx 24rpx rgba(0, 0, 0, 0.06);
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

button.media,
button.send {
  height: 72rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
  font-weight: 600;
  line-height: 1;
  white-space: nowrap;
  border: none;
}

button.media {
  width: 88rpx;
  min-width: 88rpx;
  background: #eef2ff;
  color: #2f7cff;
  border-radius: 18rpx;
  border: 1rpx solid rgba(47, 124, 255, 0.18);
}

button.send {
  min-width: 140rpx;
  padding: 0 28rpx;
  background: #2f7cff;
  color: #fff;
  border-radius: 18rpx;
}

button.media:active,
button.send:active {
  opacity: 0.85;
}
</style>
