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
          <template v-else-if="isActionCardMessage(message)">
            <view class="action-card">
              <view class="action-title">{{ parseActionCard(message.content).title }}</view>
              <view class="action-body">
                <view
                  v-for="(row, idx) in parseActionCard(message.content).rows"
                  :key="idx"
                  class="action-row"
                >
                  <text class="k">{{ row.k }}</text>
                  <text class="v">{{ row.v }}</text>
                </view>
              </view>
              <view v-if="parseActionCard(message.content).primaryAction" class="action-footer">
                <button class="action-btn" @click="handleCardPrimary(parseActionCard(message.content))">
                  {{ parseActionCard(message.content).primaryAction!.label }}
                </button>
              </view>
            </view>
          </template>
          <text v-else class="content">{{ message.content }}</text>
          <view class="meta">
            <text class="time">{{ formatTime(message.createdAt) }}</text>
            <text v-if="!isFromOther(message)" class="read" :class="{ yes: message.isRead === 1 }">
              {{ message.isRead === 1 ? '已读' : '未读' }}
            </text>
          </view>
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
      <button class="ops" :disabled="sending || loading" @click="openOps">+</button>
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

    <view v-if="opsVisible" class="overlay" @click.self="closeOps">
      <view class="sheet">
        <view class="sheet-header">
          <text class="sheet-title">{{ opsTitle }}</text>
          <text class="sheet-close" @click="closeOps">×</text>
        </view>

        <view v-if="opsStep === 'menu'" class="sheet-menu">
          <button class="sheet-item" @click="startRecommend">推荐岗位</button>
          <button class="sheet-item" @click="startInterview">发送面试邀请</button>
          <button class="sheet-item" @click="startOffer">发送 Offer</button>
          <button class="sheet-item danger" @click="startReject">拒绝候选人</button>
        </view>

        <view v-else class="sheet-form">
          <template v-if="opsType === 'recommend'">
            <view class="form-item">
              <text class="label">选择岗位</text>
              <picker :range="myJobs" range-key="jobTitle" :value="selectedJobIndex" @change="onPickJob">
                <view class="picker">{{ selectedJobLabel }}</view>
              </picker>
            </view>
            <view class="form-item">
              <text class="label">备注（可选）</text>
              <textarea v-model="recommendNote" placeholder="写点推荐理由（可不填）" />
            </view>
          </template>

          <template v-else-if="opsType === 'interview'">
            <view class="form-item">
              <text class="label">面试时间</text>
              <input v-model="interviewForm.interviewTime" placeholder="如：2026-01-08 14:00" />
            </view>
            <view class="form-item">
              <text class="label">面试方式</text>
              <picker :range="interviewTypeOptions" range-key="label" :value="interviewTypeIndex" @change="onPickInterviewType">
                <view class="picker">{{ interviewTypeOptions[interviewTypeIndex]?.label }}</view>
              </picker>
            </view>
            <view class="form-item">
              <text class="label">地点/会议链接</text>
              <input v-model="interviewForm.locationOrLink" placeholder="现场地址或会议链接" />
            </view>
            <view class="form-item">
              <text class="label">备注（可选）</text>
              <textarea v-model="interviewForm.note" placeholder="通知内容（可不填）" />
            </view>
          </template>

          <template v-else-if="opsType === 'offer'">
            <view class="form-item">
              <text class="label">职位名称（可选）</text>
              <input v-model="offerForm.title" placeholder="如：数据分析师" />
            </view>
            <view class="form-item two">
              <view class="half">
                <text class="label">基本薪资</text>
                <input v-model="offerForm.baseSalary" placeholder="数字" type="number" />
              </view>
              <view class="half">
                <text class="label">奖金/补贴</text>
                <input v-model="offerForm.bonus" placeholder="数字" type="number" />
              </view>
            </view>
            <view class="form-item">
              <text class="label">到岗日期（可选）</text>
              <input v-model="offerForm.startDate" placeholder="如：2026-02-01" />
            </view>
            <view class="form-item">
              <text class="label">雇佣类型</text>
              <picker :range="employmentTypeOptions" range-key="label" :value="employmentTypeIndex" @change="onPickEmploymentType">
                <view class="picker">{{ employmentTypeOptions[employmentTypeIndex]?.label }}</view>
              </picker>
            </view>
            <view class="form-item">
              <text class="label">备注（可选）</text>
              <textarea v-model="offerForm.note" placeholder="Offer说明（可不填）" />
            </view>
          </template>

          <template v-else-if="opsType === 'reject'">
            <view class="form-item">
              <text class="label">拒绝原因（可选）</text>
              <textarea v-model="rejectReason" placeholder="如：经验匹配度不够/时间不合适等（可不填）" />
            </view>
          </template>

          <view class="sheet-actions">
            <button class="sheet-back" @click="backToMenu">返回</button>
            <button class="sheet-confirm" :disabled="sending || loading" @click="submitOps">发送</button>
          </view>
        </view>
      </view>
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
import {
  getJobPositionList,
  recommendJob,
  scheduleInterview,
  sendOffer,
  rejectCandidate,
  type JobPosition,
} from "@/services/api/hr";
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

type OpsType = "recommend" | "interview" | "offer" | "reject";
type OpsStep = "menu" | "form";
const opsVisible = ref(false);
const opsType = ref<OpsType>("recommend");
const opsStep = ref<OpsStep>("menu");

const myJobs = ref<JobPosition[]>([]);
const selectedJobIndex = ref(0);
const recommendNote = ref("");

const interviewTypeOptions = [
  { label: "电话面试", value: 1 },
  { label: "视频面试", value: 2 },
  { label: "现场面试", value: 3 },
];
const interviewTypeIndex = ref(1);
const interviewForm = ref<{
  interviewTime: string;
  locationOrLink: string;
  note: string;
}>({
  interviewTime: "",
  locationOrLink: "",
  note: "",
});

const employmentTypeOptions = [
  { label: "全职", value: "full_time" },
  { label: "兼职", value: "part_time" },
  { label: "实习", value: "intern" },
];
const employmentTypeIndex = ref(0);
const offerForm = ref<{
  title: string;
  baseSalary: string;
  bonus: string;
  startDate: string;
  note: string;
}>({
  title: "",
  baseSalary: "",
  bonus: "",
  startDate: "",
  note: "",
});

const rejectReason = ref("");

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

type ActionCardModel = {
  type: string;
  title: string;
  rows: Array<{ k: string; v: string }>;
  primaryAction?: { label: string; jobId?: number; url?: string };
};

const isActionCardMessage = (message: Message) => {
  if (message.messageType !== 1) return false;
  const content = (message.content || "").trim();
  return (
    content.startsWith("【岗位推荐】") ||
    content.startsWith("【面试邀请】") ||
    content.startsWith("【Offer】") ||
    content.startsWith("【拒绝通知】")
  );
};

const parseActionCard = (content: string): ActionCardModel => {
  const lines = (content || "")
    .split("\n")
    .map((l) => l.trim())
    .filter(Boolean);
  const title = lines[0] || "通知";
  const rows: Array<{ k: string; v: string }> = [];
  let jobId: number | undefined;
  for (const line of lines.slice(1)) {
    const idx = line.indexOf("：");
    if (idx === -1) continue;
    const k = line.slice(0, idx).trim();
    const v = line.slice(idx + 1).trim();
    if (!k || !v) continue;
    if (k === "岗位ID") {
      const parsed = Number(v.replace(/[^0-9]/g, ""));
      if (Number.isFinite(parsed) && parsed > 0) jobId = parsed;
      continue;
    }
    rows.push({ k, v });
  }
  const model: ActionCardModel = { type: title, title, rows };
  if (title.startsWith("【岗位推荐】") && jobId) {
    model.primaryAction = { label: "查看岗位", jobId };
  }
  if (title.startsWith("【面试邀请】")) {
    const linkRow = rows.find((r) => r.k === "会议链接");
    if (linkRow?.v) model.primaryAction = { label: "复制链接", url: linkRow.v };
  }
  return model;
};

const handleCardPrimary = (card: ActionCardModel) => {
  if (card.primaryAction?.jobId) {
    uni.navigateTo({
      url: `/pages/hr/hr/jobs/detail?jobId=${card.primaryAction.jobId}`,
    });
    return;
  }
  if (card.primaryAction?.url) {
    uni.setClipboardData({ data: card.primaryAction.url });
  }
};

const restoreCachedApplicationId = () => {
  if (applicationId.value) return;
  if (!conversationId.value) return;
  const cachedByConv = Number(
    uni.getStorageSync(`hr_chat_app_by_conv_${conversationId.value}`)
  );
  if (Number.isFinite(cachedByConv) && cachedByConv > 0) {
    applicationId.value = cachedByConv;
    return;
  }
  if (!otherUserId.value) return;
  const cachedByUser = Number(
    uni.getStorageSync(`hr_chat_app_by_user_${otherUserId.value}`)
  );
  if (Number.isFinite(cachedByUser) && cachedByUser > 0) {
    applicationId.value = cachedByUser;
  }
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
      const last = messages.value.at(-1);
      if (last) {
        const candidateId = last.senderId !== 0 ? last.senderId : last.receiverId;
        if (candidateId) {
          otherUserId.value = candidateId;
        }
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
      applicationId: applicationId.value || undefined,
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
      title:
        (err instanceof Error ? err.message : String(err)).includes("投递记录不存在")
          ? "缺少投递/推荐记录，请先用“流程-推荐岗位”建立记录"
          : t("pages.chat.conversation.sendError"),
      icon: "none",
    });
  } finally {
    sending.value = false;
  }
};

const doSendImage = () => {
  if (!conversationId.value) return;
  if (!otherUserId.value) {
    uni.showToast({ title: "缺少收件人", icon: "none" });
    return;
  }
  restoreCachedApplicationId();
  if (!applicationId.value) {
    uni.showToast({ title: "缺少投递/推荐记录，请先用“+”进行跟进招聘", icon: "none" });
    return;
  }

  uni.chooseImage({
    count: 1,
    sizeType: ["compressed"],
    sourceType: ["album", "camera"],
    success: async (res) => {
      const filePath = res?.tempFilePaths?.[0];
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
    },
  });
};

const formatTime = (time: string) => dayjs(time).format("MM/DD HH:mm");

const openOps = () => {
  opsVisible.value = true;
  opsStep.value = "menu";
};

const closeOps = () => {
  opsVisible.value = false;
  opsStep.value = "menu";
};

const opsTitle = computed(() => {
  if (opsStep.value === "menu") return "跟进招聘";
  if (opsType.value === "recommend") return "推荐岗位";
  if (opsType.value === "interview") return "发送面试邀请";
  if (opsType.value === "offer") return "发送 Offer";
  return "拒绝候选人";
});

const backToMenu = () => {
  opsStep.value = "menu";
};

const loadMyJobs = async () => {
  if (myJobs.value.length > 0) return;
  const jobs = await getJobPositionList(1);
  myJobs.value = (jobs || []).filter((j) => j && (j as any).id);
  selectedJobIndex.value = 0;
};

const selectedJobLabel = computed(() => {
  const job = myJobs.value[selectedJobIndex.value];
  return job ? `${job.jobTitle}${job.city ? `（${job.city}）` : ""}` : "请选择岗位";
});

const onPickJob = (e: any) => {
  selectedJobIndex.value = Number(e?.detail?.value ?? 0);
};

const onPickInterviewType = (e: any) => {
  interviewTypeIndex.value = Number(e?.detail?.value ?? 0);
};

const onPickEmploymentType = (e: any) => {
  employmentTypeIndex.value = Number(e?.detail?.value ?? 0);
};

const startRecommend = async () => {
  opsType.value = "recommend";
  opsStep.value = "form";
  recommendNote.value = "";
  try {
    await loadMyJobs();
  } catch (err) {
    console.error("Failed to load jobs:", err);
    uni.showToast({ title: "岗位列表加载失败", icon: "none" });
  }
};

const requireApplicationId = async (): Promise<number | null> => {
  if (!applicationId.value) {
    await ensureConversationMeta();
  }
  if (applicationId.value && applicationId.value > 0) return applicationId.value;
  uni.showToast({ title: "缺少投递/推荐记录，请先推荐岗位", icon: "none" });
  return null;
};

const startInterview = async () => {
  opsType.value = "interview";
  opsStep.value = "form";
  interviewForm.value = { interviewTime: "", locationOrLink: "", note: "" };
  interviewTypeIndex.value = 1;
  await requireApplicationId();
};

const startOffer = async () => {
  opsType.value = "offer";
  opsStep.value = "form";
  offerForm.value = { title: "", baseSalary: "", bonus: "", startDate: "", note: "" };
  employmentTypeIndex.value = 0;
  await requireApplicationId();
};

const startReject = async () => {
  opsType.value = "reject";
  opsStep.value = "form";
  rejectReason.value = "";
  await requireApplicationId();
};

const pushTempText = (text: string) => {
  const tempId = Date.now();
  const tempMessage: Message = {
    id: tempId,
    conversationId: conversationId.value,
    senderId: 0,
    receiverId: otherUserId.value,
    messageType: 1,
    content: text,
    fileUrl: null,
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
  return tempId;
};

const replaceTemp = (tempId: number, sent: Message) => {
  messages.value = messages.value.map((m) => (m.id === tempId ? sent : m));
  nextTick(() => {
    scrollIntoView.value = `msg-${sent.id}`;
  });
};

const removeTemp = (tempId: number) => {
  messages.value = messages.value.filter((m) => m.id !== tempId);
};

const submitOps = async () => {
  if (!conversationId.value || !otherUserId.value) return;
  sending.value = true;
  try {
    if (opsType.value === "recommend") {
      if (!myJobs.value.length) await loadMyJobs();
      const job = myJobs.value[selectedJobIndex.value];
      if (!job?.id) {
        uni.showToast({ title: "请选择岗位", icon: "none" });
        return;
      }

      const newApplicationId = await recommendJob({
        jobId: job.id,
        seekerUserId: otherUserId.value,
        note: recommendNote.value.trim() || undefined,
      });

      if (Number.isFinite(newApplicationId) && newApplicationId > 0) {
        applicationId.value = newApplicationId;
        uni.setStorageSync(`hr_chat_app_by_conv_${conversationId.value}`, newApplicationId);
        uni.setStorageSync(`hr_chat_app_by_user_${otherUserId.value}`, newApplicationId);
      }

      const text =
        `【岗位推荐】\n岗位：${job.jobTitle}${job.city ? `（${job.city}）` : ""}\n` +
        `岗位ID：${job.id}\n` +
        `备注：${recommendNote.value.trim() || "无"}`;

      const tempId = pushTempText(text);
      const sent = await sendMessage({
        receiverId: otherUserId.value,
        applicationId: applicationId.value || undefined,
        messageType: 1,
        content: text,
        fileUrl: null,
        replyTo: null,
      });
      replaceTemp(tempId, sent);
      uni.showToast({ title: "已推荐", icon: "success" });
      closeOps();
      return;
    }

    const appId = await requireApplicationId();
    if (!appId) return;

    if (opsType.value === "interview") {
      if (!interviewForm.value.interviewTime.trim()) {
        uni.showToast({ title: "请填写面试时间", icon: "none" });
        return;
      }
      const type = interviewTypeOptions[interviewTypeIndex.value]?.value ?? 2;
      const locationOrLink = interviewForm.value.locationOrLink.trim();
      const normalizeDateTime = (raw: string): string => {
        const trimmed = raw.trim().replace(/\//g, "-").replace(/\./g, "-");
        const pad2 = (v: string) => v.padStart(2, "0");
        const cnDate = trimmed.match(/^(\d{4})年(\d{1,2})月(\d{1,2})日$/);
        if (cnDate) {
          const [, y, mo, d] = cnDate;
          return `${y}-${pad2(mo)}-${pad2(d)}T09:00:00`;
        }
        const dateOnly = trimmed.match(/^(\d{4})-(\d{1,2})-(\d{1,2})$/);
        if (dateOnly) {
          const [, y, mo, d] = dateOnly;
          return `${y}-${pad2(mo)}-${pad2(d)}T09:00:00`;
        }
        const match = trimmed.match(
          /^(\d{4})-(\d{1,2})-(\d{1,2})\s+(\d{1,2}):(\d{2})(?::(\d{2}))?$/
        );
        if (match) {
          const [, y, mo, d, h, mi, s] = match;
          return `${y}-${pad2(mo)}-${pad2(d)}T${pad2(h)}:${pad2(mi)}:${pad2(s || "00")}`;
        }
        if (/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}$/.test(trimmed)) return `${trimmed}:00`;
        if (/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}$/.test(trimmed)) return trimmed;
        if (/^\d{4}-\d{2}-\d{2}\s/.test(trimmed)) {
          return trimmed.replace(" ", "T");
        }
        return trimmed;
      };
      const interviewTime = normalizeDateTime(interviewForm.value.interviewTime);
      const isLikelyUrl = (value: string) => /^https?:\/\//i.test(value);
      const noteText = interviewForm.value.note.trim();
      const interviewPayload: any = {
        applicationId: appId,
        interviewTime,
        interviewType: type,
      };
      if (type === 3 && locationOrLink) {
        interviewPayload.location = locationOrLink;
      } else if (type !== 3 && locationOrLink) {
        if (isLikelyUrl(locationOrLink)) {
          interviewPayload.meetingLink = locationOrLink;
        } else {
          interviewPayload.note = noteText
            ? `${noteText}（会议链接：${locationOrLink}）`
            : `会议链接：${locationOrLink}`;
        }
      }
      if (noteText && !interviewPayload.note) interviewPayload.note = noteText;

      await scheduleInterview(interviewPayload);

      const text =
        `【面试邀请】\n面试时间：${interviewForm.value.interviewTime.trim()}\n` +
        `方式：${interviewTypeOptions[interviewTypeIndex.value]?.label}\n` +
        (type === 3
          ? `地点：${locationOrLink || "待定"}\n`
          : `会议链接：${locationOrLink || "待定"}\n`) +
        `备注：${interviewForm.value.note.trim() || "无"}`;

      const tempId = pushTempText(text);
      const sent = await sendMessage({
        receiverId: otherUserId.value,
        applicationId: appId,
        messageType: 1,
        content: text,
        fileUrl: null,
        replyTo: null,
      });
      replaceTemp(tempId, sent);
      uni.showToast({ title: "已发送", icon: "success" });
      closeOps();
      return;
    }

    if (opsType.value === "offer") {
      const baseSalary = Number(offerForm.value.baseSalary);
      const bonus = Number(offerForm.value.bonus);

      const normalizeDate = (raw: string): string => {
        const trimmed = raw.trim().replace(/\//g, "-").replace(/\./g, "-");
        const pad2 = (v: string) => v.padStart(2, "0");
        const cnDate = trimmed.match(/^(\d{4})年(\d{1,2})月(\d{1,2})日$/);
        if (cnDate) {
          const [, y, mo, d] = cnDate;
          return `${y}-${pad2(mo)}-${pad2(d)}`;
        }
        const dateOnly = trimmed.match(/^(\d{4})-(\d{1,2})-(\d{1,2})$/);
        if (dateOnly) {
          const [, y, mo, d] = dateOnly;
          return `${y}-${pad2(mo)}-${pad2(d)}`;
        }
        if (/^\d{4}-\d{2}-\d{2}T/.test(trimmed)) return trimmed.split("T")[0];
        if (/^\d{4}-\d{2}-\d{2}\s/.test(trimmed)) return trimmed.split(" ")[0];
        return trimmed;
      };
      const startDate = normalizeDate(offerForm.value.startDate);

      await sendOffer({
        applicationId: appId,
        title: offerForm.value.title.trim() || undefined,
        baseSalary: Number.isFinite(baseSalary) ? baseSalary : undefined,
        bonus: Number.isFinite(bonus) ? bonus : undefined,
        startDate: startDate || undefined,
        employmentType: employmentTypeOptions[employmentTypeIndex.value]?.value,
        send: true,
        note: offerForm.value.note.trim() || undefined,
      });

      const text =
        `【Offer】\n职位：${offerForm.value.title.trim() || "（未填写）"}\n` +
        `基本薪资：${offerForm.value.baseSalary || "未填写"}\n` +
        `奖金/补贴：${offerForm.value.bonus || "未填写"}\n` +
        `到岗日期：${offerForm.value.startDate.trim() || "未填写"}\n` +
        `雇佣类型：${employmentTypeOptions[employmentTypeIndex.value]?.label}\n` +
        `备注：${offerForm.value.note.trim() || "无"}`;

      const tempId = pushTempText(text);
      const sent = await sendMessage({
        receiverId: otherUserId.value,
        applicationId: appId,
        messageType: 1,
        content: text,
        fileUrl: null,
        replyTo: null,
      });
      replaceTemp(tempId, sent);
      uni.showToast({ title: "已发送", icon: "success" });
      closeOps();
      return;
    }

    if (opsType.value === "reject") {
      await rejectCandidate(appId, {
        reason: rejectReason.value.trim() || undefined,
        sendNotification: true,
      });

      const text =
        `【拒绝通知】\n结果：未通过\n` + `原因：${rejectReason.value.trim() || "未填写"}`;

      const tempId = pushTempText(text);
      const sent = await sendMessage({
        receiverId: otherUserId.value,
        applicationId: appId,
        messageType: 1,
        content: text,
        fileUrl: null,
        replyTo: null,
      });
      replaceTemp(tempId, sent);
      uni.showToast({ title: "已发送", icon: "success" });
      closeOps();
      return;
    }
  } catch (err) {
    console.error("Failed to submit ops:", err);
    uni.showToast({ title: "操作失败", icon: "none" });
  } finally {
    sending.value = false;
  }
};

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

.meta {
  display: flex;
  gap: 10rpx;
  align-items: center;
  justify-content: flex-end;
}

.read {
  font-size: 22rpx;
  opacity: 0.75;
}

.bubble.mine .read {
  color: rgba(255, 255, 255, 0.75);
}

.bubble.other .read {
  color: #6b7280;
}

.read.yes {
  opacity: 0.95;
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

button.ops {
  width: 72rpx;
  min-width: 72rpx;
  height: 72rpx;
  border-radius: 20rpx;
  background: #2f7cff;
  color: #fff;
  border: none;
  font-size: 40rpx;
  font-weight: 800;
  line-height: 72rpx;
  padding: 0;
  box-shadow: 0 12rpx 24rpx rgba(47, 124, 255, 0.22);
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

.action-card {
  background: #ffffff;
  border-radius: 18rpx;
  padding: 18rpx 18rpx 14rpx;
  border: 1rpx solid #eef2ff;
  box-shadow: 0 8rpx 18rpx rgba(0, 0, 0, 0.04);
}

.bubble.mine .action-card {
  background: rgba(255, 255, 255, 0.96);
  border-color: rgba(255, 255, 255, 0.6);
}

.action-title {
  font-size: 28rpx;
  font-weight: 700;
  color: #111827;
}

.action-body {
  margin-top: 12rpx;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.action-row {
  display: flex;
  gap: 12rpx;
}

.action-row .k {
  width: 140rpx;
  color: #6b7280;
  font-size: 24rpx;
}

.action-row .v {
  flex: 1;
  color: #111827;
  font-size: 24rpx;
  word-break: break-all;
}

.action-footer {
  margin-top: 12rpx;
  display: flex;
  justify-content: flex-end;
}

.action-btn {
  height: 64rpx;
  padding: 0 26rpx;
  border-radius: 16rpx;
  background: #2f7cff;
  color: #fff;
  border: none;
  font-size: 24rpx;
}

.overlay {
  position: fixed;
  left: 0;
  right: 0;
  top: 0;
  bottom: 0;
  z-index: 100;
  background: rgba(0, 0, 0, 0.35);
  display: flex;
  align-items: flex-end;
}

.sheet {
  width: 100%;
  background: #fff;
  border-top-left-radius: 28rpx;
  border-top-right-radius: 28rpx;
  padding: 24rpx;
  padding-bottom: calc(24rpx + env(safe-area-inset-bottom));
}

.sheet-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16rpx;
}

.sheet-title {
  font-size: 30rpx;
  font-weight: 700;
  color: #111827;
}

.sheet-close {
  font-size: 44rpx;
  line-height: 44rpx;
  color: #9ca3af;
}

.sheet-menu {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16rpx;
}

.sheet-item {
  height: 96rpx;
  border-radius: 18rpx;
  background: #f6f7fb;
  border: none;
  color: #111827;
  font-size: 28rpx;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
}

.sheet-item.danger {
  background: #fff1f0;
  color: #cf1322;
}

.sheet-form {
  display: flex;
  flex-direction: column;
  gap: 18rpx;
}

.form-item .label {
  display: block;
  color: #6f788f;
  font-size: 24rpx;
  margin-bottom: 8rpx;
}

.picker,
.sheet-form input,
.sheet-form textarea {
  width: 100%;
  background: #f5f7fb;
  border-radius: 16rpx;
  padding: 0 20rpx;
  box-sizing: border-box;
  font-size: 26rpx;
}

.picker {
  height: 92rpx;
  display: flex;
  align-items: center;
  color: #111827;
}

.sheet-form input {
  height: 92rpx;
  line-height: 92rpx;
}

.sheet-form textarea {
  min-height: 220rpx;
  padding: 18rpx 20rpx;
  line-height: 1.5;
}

.form-item.two {
  display: flex;
  gap: 16rpx;
}

.form-item.two .half {
  flex: 1;
}

.sheet-actions {
  display: flex;
  gap: 16rpx;
  margin-top: 10rpx;
}

.sheet-back,
.sheet-confirm {
  flex: 1;
  height: 88rpx;
  border-radius: 18rpx;
  border: none;
  font-size: 28rpx;
  font-weight: 700;
}

.sheet-back {
  background: #f1f2f6;
  color: #2b3445;
}

.sheet-confirm {
  background: #2f7cff;
  color: #fff;
}
</style>
