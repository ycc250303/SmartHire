<template>
  <view class="seeker-detail">
    <view v-if="loading" class="state-card">
      <text class="state-text">加载中...</text>
    </view>

    <scroll-view v-else class="content" scroll-y>
      <view class="resume-card header-card" v-if="seeker">
        <view class="header-left">
          <view class="header-avatar">
            <view class="avatar-placeholder">
              <text class="avatar-initial">{{ fallbackInitial }}</text>
            </view>
          </view>
          <view class="header-info">
            <view class="name-row">
              <text class="candidate-name">{{ seeker.username || '求职者' }}</text>
              <view class="status-badge">
                <text class="badge-text">{{ jobStatusText(seeker.jobStatus) }}</text>
              </view>
            </view>
            <text class="candidate-meta">
              {{ basicMeta }}
            </text>
          </view>
        </view>
      </view>

      <view class="resume-card section" v-if="seeker">
        <view class="section-header">
          <text class="section-title">简历概览</text>
        </view>
        <view class="chips">
          <view class="chip">{{ seeker.highestEducation || '学历未知' }}</view>
          <view class="chip">{{ seeker.major || '专业未知' }}</view>
          <view class="chip">{{ seeker.graduationYear || '毕业时间未知' }}</view>
          <view class="chip">{{ experienceText(seeker.workExperienceYear) }}</view>
          <view class="chip">{{ seeker.internshipExperience ? '有实习经验' : '无实习经验' }}</view>
          <view class="chip" v-if="displayUniversity">{{ displayUniversity }}</view>
          <view class="chip" v-if="displayCity">{{ displayCity }}</view>
        </view>

        <view class="info-grid">
          <view class="info-item">
            <text class="k">年龄</text>
            <text class="v">{{ seeker.age ?? '未知' }}</text>
          </view>
          <view class="info-item">
            <text class="k">城市</text>
            <text class="v">{{ displayCity || '未知' }}</text>
          </view>
          <view class="info-item">
            <text class="k">学校</text>
            <text class="v">{{ displayUniversity || '未知' }}</text>
          </view>
          <view class="info-item">
            <text class="k">毕业</text>
            <text class="v">{{ seeker.graduationYear || '未知' }}</text>
          </view>
        </view>
      </view>

      <view class="resume-card section">
        <view class="section-header">
          <text class="section-title">在线简历</text>
          <text class="link-button" @click="toggleResume">
            {{ resumeOpen ? '收起' : '查看' }}
          </text>
        </view>

        <view v-if="!resumeOpen" class="resume-hint">
          点击查看求职者详细简历信息
        </view>

        <view v-else class="resume-detail">
          <view v-if="resumeLoading" class="state-inline">加载中...</view>
          <view v-else-if="resumeError" class="state-inline">{{ resumeError }}</view>
          <view v-else-if="!onlineResume" class="state-inline">暂无在线简历</view>
          <view v-else>
            <view class="resume-subsection" v-if="resumeExpectations.length">
              <text class="sub-title">求职期望</text>
              <view class="resume-item" v-for="(item, idx) in resumeExpectations" :key="`exp-${idx}`">
                <text class="item-title">{{ item.jobTitle || '期望岗位' }}</text>
                <text class="item-meta">
                  {{ formatMeta([item.city, formatSalary(item), item.jobType]) }}
                </text>
              </view>
            </view>

            <view class="resume-subsection" v-if="resumeWorkExperiences.length">
              <text class="sub-title">工作经历</text>
              <view class="resume-item" v-for="(item, idx) in resumeWorkExperiences" :key="`work-${idx}`">
                <text class="item-title">{{ item.companyName || item.company || '工作经历' }}</text>
                <text class="item-meta">
                  {{ formatMeta([item.position, formatPeriod(item.startDate, item.endDate)]) }}
                </text>
                <text class="item-desc" v-if="item.description">{{ item.description }}</text>
              </view>
            </view>

            <view class="resume-subsection" v-if="resumeProjectExperiences.length">
              <text class="sub-title">项目经历</text>
              <view class="resume-item" v-for="(item, idx) in resumeProjectExperiences" :key="`project-${idx}`">
                <text class="item-title">{{ item.projectName || '项目经历' }}</text>
                <text class="item-meta">
                  {{ formatMeta([item.role, formatPeriod(item.startDate, item.endDate)]) }}
                </text>
                <text class="item-desc" v-if="item.description">{{ item.description }}</text>
              </view>
            </view>

            <view class="resume-subsection" v-if="resumeEducationExperiences.length">
              <text class="sub-title">教育经历</text>
              <view class="resume-item" v-for="(item, idx) in resumeEducationExperiences" :key="`edu-${idx}`">
                <text class="item-title">{{ item.schoolName || item.school || '教育经历' }}</text>
                <text class="item-meta">
                  {{ formatMeta([item.major, item.degree, formatPeriod(item.startDate, item.endDate)]) }}
                </text>
              </view>
            </view>

            <view class="resume-subsection" v-if="resumeSkills.length">
              <text class="sub-title">技能</text>
              <view class="skill-row" v-for="(item, idx) in resumeSkills" :key="`skill-${idx}`">
                <text class="skill-name">{{ item.skillName || item.name || '技能' }}</text>
                <text class="skill-level" v-if="item.level !== undefined || item.proficiency !== undefined">
                  {{ formatSkillLevel(item.level ?? item.proficiency) }}
                </text>
              </view>
            </view>
          </view>
        </view>
      </view>

      <view class="resume-card section">
        <view class="section-header">
          <text class="section-title">推荐岗位</text>
        </view>

        <view class="form-item">
          <text class="label">选择岗位</text>
          <picker :range="jobPickerOptions" @change="onJobChange">
            <view class="picker-value">{{ selectedJobLabel }}</view>
          </picker>
        </view>

        <view class="form-item textarea">
          <text class="label">备注（可选）</text>
          <textarea v-model="note" placeholder="匹配理由/推荐说明（可不填）"></textarea>
        </view>

        <button class="outline" :disabled="!selectedJob || loading" @click="goAi">
          AI 智能分析
        </button>

        <button class="primary" :disabled="loading || recommending" @click="doRecommend">
          {{ recommending ? '推荐中...' : '推荐并发起聊天' }}
        </button>
      </view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import {
  getSeekerCard,
  getJobPositionList,
  checkApplicationExists,
  getApplicationDetail,
  recommendJob,
  type SeekerCard,
  type JobPosition,
} from '@/services/api/hr';
import { getConversations, sendMessage } from '@/services/api/message';
import { getOnlineResume } from '@/services/api/seeker';

const userId = ref<number>(0);
const seeker = ref<SeekerCard | null>(null);
const jobs = ref<JobPosition[]>([]);
const selectedJobIndex = ref<number | null>(null);
const note = ref('');
const loading = ref(false);
const recommending = ref(false);
const resumeOpen = ref(false);
const resumeLoading = ref(false);
const resumeError = ref('');
const onlineResume = ref<any>(null);
const initialCity = ref('');
const initialUniversity = ref('');

const sleep = (ms: number) => new Promise((resolve) => setTimeout(resolve, ms));

const fallbackInitial = computed(() => (seeker.value?.username?.trim()?.[0] || 'H').toUpperCase());

const displayCity = computed(() => seeker.value?.city || initialCity.value || '');
const displayUniversity = computed(() => seeker.value?.university || initialUniversity.value || '');

const basicMeta = computed(() => {
  const parts: string[] = [];
  if (seeker.value?.age !== undefined && seeker.value?.age !== null) parts.push(`${seeker.value.age}岁`);
  if (displayCity.value) parts.push(displayCity.value);
  if (seeker.value?.highestEducation) parts.push(seeker.value.highestEducation);
  return parts.length > 0 ? parts.join(' · ') : '信息待完善';
});

const jobPickerOptions = computed(() =>
  jobs.value.map((j) => `${j.jobTitle}${j.city ? ` · ${j.city}` : ''}`)
);

const selectedJob = computed(() => {
  if (selectedJobIndex.value === null) return null;
  return jobs.value[selectedJobIndex.value] || null;
});

const selectedJobLabel = computed(() => {
  if (selectedJob.value) return `${selectedJob.value.jobTitle}${selectedJob.value.city ? ` · ${selectedJob.value.city}` : ''}`;
  return jobs.value.length > 0 ? '请选择岗位' : '暂无可推荐岗位';
});

const resumeExpectations = computed(() => onlineResume.value?.jobSeekerExpectations || []);
const resumeWorkExperiences = computed(() => onlineResume.value?.workExperiences || []);
const resumeProjectExperiences = computed(() => onlineResume.value?.projectExperiences || []);
const resumeEducationExperiences = computed(() => onlineResume.value?.educationExperiences || []);
const resumeSkills = computed(() => onlineResume.value?.skills || []);

const formatMeta = (items: Array<string | number | null | undefined>) =>
  items.filter((item) => item !== null && item !== undefined && String(item).trim() !== '').join(' · ');

const formatSalary = (item: any) => {
  const min = item?.salaryMin;
  const max = item?.salaryMax;
  if (min && max) return `${min}-${max}`;
  if (min) return `>=${min}`;
  if (max) return `<=${max}`;
  return '';
};

const formatDateOnly = (value?: string) => {
  if (!value) return '';
  const trimmed = value.trim();
  if (trimmed.includes('T')) return trimmed.split('T')[0];
  if (trimmed.includes(' ')) return trimmed.split(' ')[0];
  if (trimmed.length >= 10) return trimmed.slice(0, 10);
  return trimmed;
};

const formatPeriod = (start?: string, end?: string) => {
  const startText = formatDateOnly(start) || '未知';
  const endText = formatDateOnly(end) || '至今';
  if (!start && !end) return '';
  return `${startText} - ${endText}`;
};

const formatSkillLevel = (level?: number) => {
  const parsed = Number(level);
  if (!Number.isFinite(parsed)) return '';
  const map: Record<number, string> = {
    0: '了解',
    1: '熟悉',
    2: '掌握',
  };
  return map[parsed] ?? '';
};

const loadOnlineResume = async () => {
  if (!userId.value) return;
  resumeLoading.value = true;
  resumeError.value = '';
  try {
    const resp = await getOnlineResume(userId.value);
    onlineResume.value = resp?.data ?? resp ?? null;
    if (!onlineResume.value) {
      resumeError.value = '暂无在线简历';
    }
  } catch (err) {
    console.error('Failed to load online resume:', err);
    resumeError.value = '加载失败';
  } finally {
    resumeLoading.value = false;
  }
};

const toggleResume = () => {
  resumeOpen.value = !resumeOpen.value;
  if (resumeOpen.value && !onlineResume.value && !resumeLoading.value) {
    loadOnlineResume();
  }
};

const goAi = async () => {
  if (!selectedJob.value?.id) {
    uni.showToast({ title: "请先选择岗位", icon: "none" });
    return;
  }
  uni.showLoading({ title: "AI分析..." });
  try {
    const exists = await checkApplicationExists({
      jobId: selectedJob.value.id,
      seekerUserId: userId.value,
    });
    if (exists?.exists && exists.applicationId) {
      const detail = await getApplicationDetail(exists.applicationId);
      if (detail?.jobSeekerId) {
        uni.navigateTo({
          url: `/pages/hr/hr/ai/candidate-ai?jobSeekerId=${encodeURIComponent(
            detail.jobSeekerId
          )}&jobId=${encodeURIComponent(detail.jobId || selectedJob.value.id)}&candidateName=${encodeURIComponent(
            seeker.value?.username || ""
          )}&jobTitle=${encodeURIComponent(selectedJob.value.jobTitle || "")}`,
        });
        return;
      }
    }
  } catch (err) {
    console.error("Failed to open AI:", err);
  } finally {
    uni.hideLoading();
  }

  uni.navigateTo({
    url: `/pages/hr/hr/ai/candidate-ai?userId=${encodeURIComponent(
      userId.value
    )}&jobId=${encodeURIComponent(selectedJob.value.id)}&candidateName=${encodeURIComponent(
      seeker.value?.username || ""
    )}&jobTitle=${encodeURIComponent(selectedJob.value.jobTitle || "")}`,
  });
};

const onJobChange = (event: any) => {
  const index = Number(event.detail.value);
  selectedJobIndex.value = Number.isNaN(index) ? null : index;
};

const jobStatusText = (status?: number) => {
  const map: Record<number, string> = {
    0: '离职-随时到岗',
    1: '在职-月内到岗',
    2: '在职-考虑机会',
    3: '在职-暂不考虑',
  };
  if (status === undefined || status === null) return '状态未知';
  return map[status] ?? '状态未知';
};

const experienceText = (year?: number) => {
  if (year === undefined || year === null) return '未知';
  if (year <= 0) return '应届/0年';
  return `${year}年`;
};

const loadData = async () => {
  if (!userId.value) return;
  loading.value = true;
  try {
    const [card, jobList] = await Promise.all([
      getSeekerCard(userId.value),
      getJobPositionList(1),
    ]);
    seeker.value = card || null;
    if (seeker.value) {
      const city = seeker.value.city?.trim();
      const university = seeker.value.university?.trim();
      if (!city && initialCity.value) {
        seeker.value.city = initialCity.value;
      }
      if (!university && initialUniversity.value) {
        seeker.value.university = initialUniversity.value;
      }
    }
    jobs.value = Array.isArray(jobList) ? jobList : [];
  } catch (err) {
    console.error('Failed to load seeker detail:', err);
    uni.showToast({ title: '加载失败', icon: 'none' });
  } finally {
    loading.value = false;
  }
};

const getExistingConversation = async () => {
  const conversations = await getConversations();
  return conversations.find((c) => c.otherUserId === userId.value) || null;
};

const getConversationByApplicationId = async (applicationId?: number) => {
  if (!applicationId) return null;
  const conversations = await getConversations();
  return conversations.find((c) => c.otherUserId === userId.value && c.applicationId === applicationId) || null;
};

const goToChat = (conversationId: number, applicationId?: number) => {
  const username = seeker.value?.username || '';
  const appQuery = applicationId ? `&applicationId=${applicationId}` : '';
  if (applicationId) {
    uni.setStorageSync(`hr_chat_app_by_conv_${conversationId}`, applicationId);
    uni.setStorageSync(`hr_chat_app_by_user_${userId.value}`, applicationId);
  }
  uni.navigateTo({
    url: `/pages/hr/hr/messages/chat?id=${conversationId}&userId=${userId.value}&username=${encodeURIComponent(username)}${appQuery}`,
  });
};

const doRecommend = async () => {
  if (recommending.value || loading.value) return;
  if (!userId.value) {
    uni.showToast({ title: '缺少用户ID', icon: 'none' });
    return;
  }
  if (!selectedJob.value) {
    uni.showToast({ title: '请选择岗位', icon: 'none' });
    return;
  }

  recommending.value = true;
  try {
    const exists = await checkApplicationExists({
      jobId: selectedJob.value.id,
      seekerUserId: userId.value,
    });
    if (exists.exists) {
      uni.showModal({
        title: '已存在投递/推荐记录',
        content: '该求职者已对该岗位投递或已被推荐过，是否直接进入聊天？',
        success: async (res) => {
          if (!res.confirm) return;
          try {
            const conv =
              (await getConversationByApplicationId(exists.applicationId)) || (await getExistingConversation());
            if (!conv) {
              uni.showToast({ title: '未找到会话记录', icon: 'none' });
              return;
            }
            goToChat(conv.id, conv.applicationId);
          } catch (err) {
            console.error('Failed to open existing chat:', err);
            uni.showToast({ title: '进入聊天失败', icon: 'none' });
          }
        },
      });
      return;
    }

    const applicationId = await recommendJob({
      jobId: selectedJob.value.id,
      seekerUserId: userId.value,
      note: note.value.trim() || undefined,
    });

    if (!Number.isFinite(applicationId) || applicationId <= 0) {
      throw new Error(`Invalid applicationId: ${String(applicationId)}`);
    }

    const intro =
      `你好，我向你推荐岗位：${selectedJob.value.jobTitle}` +
      (selectedJob.value.city ? `（${selectedJob.value.city}）` : '') +
      (note.value.trim() ? `。备注：${note.value.trim()}` : '');

    let sentConversationId: number | null = null;
    let lastError: unknown = null;

    for (let attempt = 1; attempt <= 3; attempt += 1) {
      try {
        const sent = await sendMessage({
          receiverId: userId.value,
          applicationId,
          messageType: 1,
          content: intro,
          fileUrl: null,
          replyTo: null,
        });
        sentConversationId = sent.conversationId;
        break;
      } catch (err) {
        lastError = err;
        const message = err instanceof Error ? err.message : String(err);
        if (message.includes('投递记录不存在') && attempt < 3) {
          await sleep(400 * attempt);
          continue;
        }
        throw err;
      }
    }

    if (!sentConversationId) {
      const existing = await getExistingConversation();
      if (existing?.id) {
        sentConversationId = existing.id;
      } else {
        throw lastError instanceof Error ? lastError : new Error('Send failed');
      }
    }

    uni.showToast({ title: '推荐成功', icon: 'success' });
    goToChat(sentConversationId, applicationId);
  } catch (err) {
    console.error('Failed to recommend job:', err);
    uni.showToast({ title: '推荐失败', icon: 'none' });
  } finally {
    recommending.value = false;
  }
};

onLoad((options) => {
  const raw = options?.userId;
  const parsed = Number(raw);
  if (!raw || Number.isNaN(parsed)) {
    uni.showToast({ title: '缺少用户ID', icon: 'none' });
    return;
  }
  userId.value = parsed;
  if (options?.city) {
    try {
      initialCity.value = decodeURIComponent(options.city as string);
    } catch {
      initialCity.value = options.city as string;
    }
  }
  if (options?.university) {
    try {
      initialUniversity.value = decodeURIComponent(options.university as string);
    } catch {
      initialUniversity.value = options.university as string;
    }
  }
  loadData();
});
</script>

<style scoped lang="scss">
@use "@/styles/variables.scss" as vars;

.seeker-detail {
  min-height: 100vh;
  background: linear-gradient(180deg, #e5f0ff 0%, #f6f7fb 40%, #f6f7fb 100%);
}

.content {
  padding: calc(var(--status-bar-height) + 48rpx) vars.$spacing-md 24rpx;
  box-sizing: border-box;
  min-height: 100vh;
}

.state-card {
  margin: calc(var(--status-bar-height) + 48rpx) vars.$spacing-md 0;
  background: #fff;
  border-radius: vars.$border-radius;
  padding: vars.$spacing-xl;
  box-shadow: 0 12rpx 32rpx rgba(31, 55, 118, 0.08);
}

.state-text {
  font-size: 28rpx;
  color: vars.$text-muted;
}

.resume-card {
  background: vars.$surface-color;
  border-radius: vars.$border-radius;
  padding: vars.$spacing-lg;
  margin-bottom: vars.$spacing-md;
  box-shadow: 0 12rpx 32rpx rgba(31, 55, 118, 0.08);
}

.header-left {
  display: flex;
  align-items: center;
  gap: vars.$spacing-md;
}

.header-avatar {
  width: 96rpx;
  height: 96rpx;
  border-radius: 24rpx;
  overflow: hidden;
  flex-shrink: 0;
  background: vars.$primary-color-soft;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-initial {
  font-size: 40rpx;
  font-weight: 800;
  color: vars.$primary-color;
}

.header-info {
  flex: 1;
  min-width: 0;
}

.name-row {
  display: flex;
  align-items: center;
  gap: vars.$spacing-sm;
}

.candidate-name {
  font-size: 36rpx;
  font-weight: 800;
  color: vars.$text-color;
  max-width: 360rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.status-badge {
  background: rgba(75, 163, 255, 0.14);
  border-radius: 999rpx;
  padding: 6rpx 14rpx;
  flex-shrink: 0;
}

.badge-text {
  font-size: 22rpx;
  color: vars.$primary-color;
}

.candidate-meta {
  display: block;
  margin-top: 8rpx;
  font-size: 24rpx;
  color: vars.$text-muted;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: vars.$spacing-md;
}

.section-title {
  font-size: 30rpx;
  font-weight: 700;
  color: vars.$text-color;
}

.link-button {
  font-size: 24rpx;
  color: vars.$primary-color;
  padding: 6rpx 10rpx;
  border-radius: 999rpx;
  background: rgba(75, 163, 255, 0.12);
}

.resume-hint {
  font-size: 24rpx;
  color: vars.$text-muted;
  padding: 6rpx 0 2rpx;
}

.resume-detail {
  display: flex;
  flex-direction: column;
  gap: vars.$spacing-md;
}

.state-inline {
  font-size: 24rpx;
  color: vars.$text-muted;
  padding: 8rpx 0;
}

.resume-subsection {
  display: flex;
  flex-direction: column;
  gap: vars.$spacing-sm;
}

.sub-title {
  font-size: 26rpx;
  font-weight: 700;
  color: vars.$text-color;
}

.resume-item {
  background: #f8faff;
  border-radius: vars.$border-radius-sm;
  padding: 16rpx 18rpx;
}

.item-title {
  font-size: 26rpx;
  font-weight: 700;
  color: vars.$text-color;
}

.item-meta {
  display: block;
  margin-top: 6rpx;
  font-size: 22rpx;
  color: vars.$text-muted;
}

.item-desc {
  display: block;
  margin-top: 6rpx;
  font-size: 22rpx;
  color: vars.$text-color;
  line-height: 1.5;
}

.skill-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12rpx 16rpx;
  border-radius: vars.$border-radius-sm;
  background: #f8faff;
}

.skill-name {
  font-size: 24rpx;
  color: vars.$text-color;
}

.skill-level {
  font-size: 22rpx;
  color: vars.$text-muted;
}

.chips {
  display: flex;
  flex-wrap: wrap;
  gap: vars.$spacing-sm;
  margin-bottom: vars.$spacing-md;
}

.chip {
  padding: 8rpx 16rpx;
  border-radius: vars.$border-radius-sm;
  font-size: 24rpx;
  background: #f5f5f5;
  color: vars.$text-color;
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: vars.$spacing-sm;
}

.info-item {
  background: #f8faff;
  border-radius: vars.$border-radius-sm;
  padding: vars.$spacing-md;
}

.k {
  font-size: 22rpx;
  color: vars.$text-muted;
}

.v {
  display: block;
  margin-top: 8rpx;
  font-size: 28rpx;
  font-weight: 700;
  color: vars.$text-color;
}

.form-item {
  margin-bottom: vars.$spacing-md;
}

.label {
  display: block;
  margin-bottom: 10rpx;
  font-size: 26rpx;
  color: #5a6275;
}

.picker-value {
  height: 84rpx;
  line-height: 84rpx;
  padding: 0 18rpx;
  border-radius: vars.$border-radius-sm;
  background: #f6f7fb;
  font-size: 28rpx;
  color: vars.$text-color;
}

textarea {
  width: 100%;
  min-height: 180rpx;
  padding: 18rpx;
  border-radius: vars.$border-radius-sm;
  background: #f6f7fb;
  box-sizing: border-box;
  font-size: 28rpx;
  line-height: 1.6;
}

button.primary {
  width: 100%;
  height: 88rpx;
  border-radius: vars.$border-radius-sm;
  background: linear-gradient(135deg, #2f7cff 0%, #4ba3ff 100%);
  color: #fff;
  border: none;
  font-size: 30rpx;
  font-weight: 800;
  box-shadow: 0 12rpx 26rpx rgba(47, 124, 255, 0.24);
}

button.outline {
  width: 100%;
  height: 88rpx;
  border-radius: vars.$border-radius-sm;
  border: 2rpx solid vars.$primary-color;
  color: vars.$primary-color;
  background: transparent;
  font-size: 30rpx;
  font-weight: 800;
  margin-bottom: 14rpx;
}

</style>








