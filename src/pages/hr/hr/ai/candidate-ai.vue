<template>
  <view class="candidate-ai-page">
    <view class="page-header">
      <view class="header-back" @click="handleBack">
        <text class="back-icon">‹</text>
      </view>
      <text class="header-title">AI 候选人助手</text>
      <view class="header-actions">
        <view class="header-action-btn" @click="handleRefresh" v-if="!anyLoading">
          <text class="action-icon">↻</text>
        </view>
      </view>
    </view>

    <view class="tabs">
      <view
        v-for="tab in tabs"
        :key="tab.key"
        class="tab"
        :class="{ active: activeTab === tab.key }"
        @click="switchTab(tab.key)"
      >
        <text class="tab-text">{{ tab.label }}</text>
      </view>
    </view>

    <scroll-view class="content-scroll" scroll-y>
      <view v-if="!jobId" class="state-card">
        <text class="state-text">缺少参数：jobId</text>
      </view>

      <view v-else-if="!jobSeekerId && seekerUserId && resolving" class="state-card">
        <text class="state-text">正在解析候选人信息...</text>
      </view>

      <view v-else-if="!jobSeekerId && seekerUserId && resolveError" class="state-card">
        <text class="state-text">{{ resolveError }}</text>
        <view class="retry-btn" @click="resolveCandidate"><text class="retry-text">重试</text></view>
      </view>

      <view v-else-if="!jobSeekerId" class="state-card">
        <text class="state-text">缺少候选人ID（需要 jobSeekerId，或提供 userId 并确保已存在投递/推荐记录）</text>
      </view>

      <template v-else>
        <view class="basic-card">
          <view class="basic-row">
            <text class="basic-name">{{ candidateName || `候选人#${jobSeekerId}` }}</text>
            <view class="basic-badge">
              <text class="basic-badge-text">岗位ID: {{ jobId }}</text>
            </view>
          </view>
          <text v-if="jobTitle" class="basic-sub">岗位：{{ jobTitle }}</text>
        </view>

        <view v-if="activeTab === 'match'">
          <view v-if="matchLoading && !matchData" class="skeleton-container">
            <view class="skeleton-item skeleton-radar"></view>
            <view class="skeleton-item skeleton-card"></view>
            <view class="skeleton-item skeleton-card"></view>
          </view>

          <view v-else-if="matchError" class="state-card">
            <text class="state-text">{{ matchError }}</text>
            <view class="retry-btn" @click="loadMatch(true)"><text class="retry-text">重试</text></view>
          </view>

          <template v-else-if="matchData">
            <view class="match-score-card">
              <view class="radar-section">
                <RadarChart :data="matchRadarData" :labels="matchRadarLabels" :max="100" :size="180" color="#4ba3ff" />
              </view>
              <view class="score-section">
                <text class="score-number" :style="{ color: scoreColor }">
                  {{ Math.round(matchData.match_analysis?.overall_score || 0) }}
                </text>
                <text class="score-label">综合匹配分</text>
              </view>
            </view>

            <view class="gap-analysis-card" v-if="matchData.gap_analysis?.skills">
              <view class="gap-header-main">
                <text class="gap-main-title">技能差距分析</text>
                <view class="match-rate-badge">
                  <text class="match-rate-text">匹配率: {{ matchRateText }}</text>
                </view>
              </view>

              <view class="gap-subsection" v-if="requiredMissingSkills.length > 0">
                <view class="gap-header">
                  <text class="gap-icon warning">⚠</text>
                  <text class="gap-title">缺失必备技能</text>
                  <view class="count-badge danger"><text class="count-text">{{ requiredMissingSkills.length }}</text></view>
                </view>
                <view class="missing-tags">
                  <view v-for="(skill, index) in requiredMissingSkills" :key="index" class="missing-tag required">
                    <text class="missing-tag-text">{{ skill }}</text>
                  </view>
                </view>
              </view>

              <view class="gap-subsection" v-if="optionalMissingSkills.length > 0">
                <view class="gap-header">
                  <text class="gap-icon info">i</text>
                  <text class="gap-title">缺失加分技能</text>
                  <view class="count-badge info"><text class="count-text">{{ optionalMissingSkills.length }}</text></view>
                </view>
                <view class="missing-tags">
                  <view v-for="(skill, index) in optionalMissingSkills" :key="index" class="missing-tag optional">
                    <text class="missing-tag-text">{{ skill }}</text>
                  </view>
                </view>
              </view>
            </view>

            <view class="card" v-if="matchData.gap_analysis?.experience || matchData.gap_analysis?.education">
              <view class="section-title">硬性要求</view>
              <view class="kv" v-if="matchData.gap_analysis?.experience">
                <text class="k">经验</text>
                <text class="v">
                  {{ matchData.gap_analysis.experience.required_text || '--' }}（候选人:
                  {{ matchData.gap_analysis.experience.candidate_years ?? '--' }}年）
                </text>
              </view>
              <view class="kv" v-if="matchData.gap_analysis?.education">
                <text class="k">学历</text>
                <text class="v">
                  {{ matchData.gap_analysis.education.required_text || '--' }}（候选人:
                  {{ matchData.gap_analysis.education.candidate_text || '--' }}）
                </text>
              </view>
            </view>
          </template>
        </view>

        <view v-else-if="activeTab === 'evaluation'">
          <view v-if="evaluationLoading && !evaluationData" class="skeleton-container">
            <view class="skeleton-item skeleton-card"></view>
            <view class="skeleton-item skeleton-card"></view>
          </view>

          <view v-else-if="evaluationError" class="state-card">
            <text class="state-text">{{ evaluationError }}</text>
            <view class="retry-btn" @click="loadEvaluation(true)"><text class="retry-text">重试</text></view>
          </view>

          <template v-else-if="evaluationData?.evaluation">
            <view class="card">
              <view class="section-title">整体评估</view>
              <text class="paragraph">{{ evaluationData.evaluation.overall_assessment || '—' }}</text>
            </view>

            <view class="card" v-if="evaluationData.evaluation.strengths?.length">
              <view class="section-title">优势</view>
              <view v-for="(item, idx) in evaluationData.evaluation.strengths" :key="idx" class="list-item">
                <text class="list-title">{{ item.category }}</text>
                <text class="paragraph">{{ item.description }}</text>
                <text class="muted">证据：{{ item.evidence }}</text>
              </view>
            </view>

            <view class="card" v-if="evaluationData.evaluation.weaknesses?.length">
              <view class="section-title">风险点</view>
              <view v-for="(item, idx) in evaluationData.evaluation.weaknesses" :key="idx" class="list-item">
                <text class="list-title">{{ item.category }}（影响：{{ item.impact }}）</text>
                <text class="paragraph">{{ item.description }}</text>
              </view>
            </view>

            <view class="card" v-if="evaluationData.evaluation.recommendation">
              <view class="section-title">推荐结论</view>
              <view class="kv">
                <text class="k">等级</text>
                <text class="v">{{ evaluationData.evaluation.recommendation.level || '—' }}</text>
              </view>
              <text class="paragraph">{{ evaluationData.evaluation.recommendation.reason || '—' }}</text>
              <view v-if="evaluationData.evaluation.recommendation.suggested_actions?.length" class="bullets">
                <view v-for="(a, idx) in evaluationData.evaluation.recommendation.suggested_actions" :key="idx" class="bullet">
                  <text class="bullet-dot">•</text>
                  <text class="bullet-text">{{ a }}</text>
                </view>
              </view>
            </view>

            <view class="card" v-if="evaluationData.evaluation.interview_focus?.length">
              <view class="section-title">面试重点</view>
              <view v-for="(q, idx) in evaluationData.evaluation.interview_focus" :key="idx" class="bullet">
                <text class="bullet-dot">•</text>
                <text class="bullet-text">{{ q }}</text>
              </view>
            </view>
          </template>
        </view>

        <view v-else-if="activeTab === 'interview'">
          <view class="card">
            <view class="section-title">面试问题</view>
            <view v-if="interviewStatus === 'initial'" class="center">
              <text class="muted">点击生成，SSE流式输出</text>
              <view class="primary-btn" @click="startInterview(false)"><text class="primary-btn-text">生成面试问题</text></view>
            </view>
            <view v-else-if="interviewStatus === 'loading'" class="center">
              <text class="muted">生成中...</text>
            </view>
            <view v-else-if="interviewStatus === 'error'" class="center">
              <text class="danger">{{ interviewError }}</text>
              <view class="primary-btn" @click="startInterview(true)"><text class="primary-btn-text">重试</text></view>
            </view>
            <view v-else class="markdown-wrap">
              <StreamingMarkdown :content="interviewContent" :is-streaming="interviewStatus === 'streaming'" />
              <view class="actions-row" v-if="interviewStatus === 'done'">
                <view class="ghost-btn" @click="startInterview(true)"><text class="ghost-btn-text">重新生成</text></view>
              </view>
            </view>
          </view>
        </view>

        <view v-else-if="activeTab === 'recommendation'">
          <view v-if="recommendationLoading && !recommendationData" class="skeleton-container">
            <view class="skeleton-item skeleton-card"></view>
            <view class="skeleton-item skeleton-card"></view>
          </view>

          <view v-else-if="recommendationError" class="state-card">
            <text class="state-text">{{ recommendationError }}</text>
            <view class="retry-btn" @click="loadRecommendation(true)"><text class="retry-text">重试</text></view>
          </view>

          <template v-else-if="recommendationData?.recommendation">
            <view class="card">
              <view class="section-title">摘要</view>
              <text class="paragraph">{{ recommendationData.recommendation.summary || '—' }}</text>
            </view>

            <view class="card" v-if="recommendationData.recommendation.key_points?.length">
              <view class="section-title">推荐要点</view>
              <view v-for="(item, idx) in recommendationData.recommendation.key_points" :key="idx" class="list-item">
                <text class="list-title">{{ item.point }}</text>
                <text class="paragraph">{{ item.description }}</text>
                <text class="muted">证据：{{ item.evidence }}</text>
              </view>
            </view>

            <view class="card" v-if="recommendationData.recommendation.concerns?.length">
              <view class="section-title">关注点</view>
              <view v-for="(item, idx) in recommendationData.recommendation.concerns" :key="idx" class="list-item">
                <text class="list-title">{{ item.concern }}</text>
                <text class="paragraph">{{ item.mitigation }}</text>
              </view>
            </view>

            <view class="card" v-if="recommendationData.recommendation.recommendation_text">
              <view class="section-title">完整推荐理由</view>
              <StreamingMarkdown :content="recommendationData.recommendation.recommendation_text" />
            </view>

            <view class="card" v-if="recommendationData.recommendation.suggested_next_steps?.length">
              <view class="section-title">下一步建议</view>
              <view v-for="(s, idx) in recommendationData.recommendation.suggested_next_steps" :key="idx" class="bullet">
                <text class="bullet-dot">•</text>
                <text class="bullet-text">{{ s }}</text>
              </view>
            </view>
          </template>
        </view>
      </template>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import RadarChart from '@/components/common/RadarChart.vue';
import StreamingMarkdown from '@/components/career-planning/StreamingMarkdown.vue';
import { checkApplicationExists, getApplicationDetail } from '@/services/api/hr';
import {
  getCandidateEvaluation,
  getCandidateMatchAnalysis,
  getCandidateRecommendation,
  streamCandidateInterviewQuestions,
  type CandidateEvaluationResponse,
  type CandidateMatchAnalysisResponse,
  type CandidateRecommendationResponse,
} from '@/services/api/hr-ai';

type TabKey = 'match' | 'evaluation' | 'interview' | 'recommendation';

const tabs: Array<{ key: TabKey; label: string }> = [
  { key: 'match', label: '匹配分析' },
  { key: 'evaluation', label: '综合评估' },
  { key: 'interview', label: '面试问题' },
  { key: 'recommendation', label: '推荐理由' },
];

const jobSeekerId = ref<number>(0);
const seekerUserId = ref<number>(0);
const jobId = ref<number>(0);
const candidateName = ref<string>('');
const jobTitle = ref<string>('');
const activeTab = ref<TabKey>('match');

const resolving = ref(false);
const resolveError = ref('');

const matchLoading = ref(false);
const matchError = ref<string>('');
const matchData = ref<CandidateMatchAnalysisResponse | null>(null);

const evaluationLoading = ref(false);
const evaluationError = ref<string>('');
const evaluationData = ref<CandidateEvaluationResponse | null>(null);

const recommendationLoading = ref(false);
const recommendationError = ref<string>('');
const recommendationData = ref<CandidateRecommendationResponse | null>(null);

const interviewContent = ref('');
const interviewError = ref('');
const interviewStatus = ref<'initial' | 'loading' | 'streaming' | 'done' | 'error'>('initial');
let interviewCancel: null | (() => void) = null;

const anyLoading = computed(
  () => matchLoading.value || evaluationLoading.value || recommendationLoading.value || interviewStatus.value === 'loading',
);

const matchRateText = computed(() => {
  const rate = matchData.value?.gap_analysis?.skills?.match_rate;
  const v = typeof rate === 'number' ? Math.round(rate * 100) : 0;
  return `${v}%`;
});

const requiredMissingSkills = computed(
  () => matchData.value?.gap_analysis?.skills?.required_missing?.filter(Boolean) ?? [],
);
const optionalMissingSkills = computed(
  () => matchData.value?.gap_analysis?.skills?.optional_missing?.filter(Boolean) ?? [],
);

const matchRadarLabels = computed(() => ['技能', '学历', '经验']);
const matchRadarData = computed(() => {
  const m = matchData.value?.match_analysis;
  const skill = typeof m?.skill_match === 'number' ? m.skill_match * 100 : 0;
  const edu = typeof m?.education_match === 'number' ? m.education_match * 100 : 0;
  const exp = m?.experience_qualified ? 100 : 40;
  return [skill, edu, exp].map((n) => Math.max(0, Math.min(100, Math.round(n))));
});

const scoreColor = computed(() => {
  const score = matchData.value?.match_analysis?.overall_score ?? 0;
  if (score >= 80) return '#00C853';
  if (score >= 60) return '#FF9800';
  return '#FF3B30';
});

const handleBack = () => uni.navigateBack();

const switchTab = async (key: TabKey) => {
  activeTab.value = key;
  if (key === 'match' && !matchData.value && !matchLoading.value) await loadMatch(false);
  if (key === 'evaluation' && !evaluationData.value && !evaluationLoading.value) await loadEvaluation(false);
  if (key === 'recommendation' && !recommendationData.value && !recommendationLoading.value) await loadRecommendation(false);
};

const handleRefresh = async () => {
  if (!jobId.value) return;
  const ok = await resolveCandidate();
  if (!ok) return;
  if (activeTab.value === 'match') return loadMatch(true);
  if (activeTab.value === 'evaluation') return loadEvaluation(true);
  if (activeTab.value === 'recommendation') return loadRecommendation(true);
  if (activeTab.value === 'interview') return startInterview(true);
};

const resolveCandidate = async (): Promise<boolean> => {
  if (jobSeekerId.value) return true;
  if (!seekerUserId.value || !jobId.value) return false;

  resolving.value = true;
  resolveError.value = '';
  try {
    const exists = await checkApplicationExists({ jobId: jobId.value, seekerUserId: seekerUserId.value });
    if (!exists?.exists || !exists.applicationId) {
      resolveError.value = '缺少投递/推荐记录，无法生成AI分析（请先在“求职者详情-推荐并发起聊天”创建记录）';
      return false;
    }
    const detail = await getApplicationDetail(exists.applicationId);
    if (!detail?.jobSeekerId) {
      resolveError.value = '解析候选人失败';
      return false;
    }
    jobSeekerId.value = detail.jobSeekerId;
    jobId.value = detail.jobId || jobId.value;
    if (!jobTitle.value) jobTitle.value = detail.jobTitle || '';
    if (!candidateName.value) candidateName.value = detail.jobSeekerName || '';
    return true;
  } catch (e) {
    console.error('Failed to resolve jobSeekerId:', e);
    resolveError.value = e instanceof Error ? e.message : '解析失败';
    return false;
  } finally {
    resolving.value = false;
  }
};

const loadMatch = async (forceRefresh: boolean) => {
  const ok = await resolveCandidate();
  if (!ok) return;
  matchLoading.value = true;
  matchError.value = '';
  try {
    matchData.value = await getCandidateMatchAnalysis(jobSeekerId.value, jobId.value, forceRefresh);
  } catch (e) {
    console.error('Failed to load match analysis:', e);
    matchError.value = e instanceof Error ? e.message : '加载失败';
  } finally {
    matchLoading.value = false;
  }
};

const loadEvaluation = async (forceRefresh: boolean) => {
  const ok = await resolveCandidate();
  if (!ok) return;
  evaluationLoading.value = true;
  evaluationError.value = '';
  try {
    evaluationData.value = await getCandidateEvaluation(jobSeekerId.value, jobId.value, forceRefresh);
  } catch (e) {
    console.error('Failed to load evaluation:', e);
    evaluationError.value = e instanceof Error ? e.message : '加载失败';
  } finally {
    evaluationLoading.value = false;
  }
};

const loadRecommendation = async (forceRefresh: boolean) => {
  const ok = await resolveCandidate();
  if (!ok) return;
  recommendationLoading.value = true;
  recommendationError.value = '';
  try {
    recommendationData.value = await getCandidateRecommendation(jobSeekerId.value, jobId.value, forceRefresh);
  } catch (e) {
    console.error('Failed to load recommendation:', e);
    recommendationError.value = e instanceof Error ? e.message : '加载失败';
  } finally {
    recommendationLoading.value = false;
  }
};

const startInterview = (force: boolean) => {
  if (!jobId.value) return;
  interviewCancel?.();
  interviewCancel = null;
  interviewError.value = '';
  interviewContent.value = '';
  interviewStatus.value = 'loading';

  resolveCandidate().then((ok) => {
    if (!ok) {
      interviewStatus.value = 'error';
      interviewError.value = resolveError.value || '缺少候选人信息';
      return;
    }

    interviewCancel = streamCandidateInterviewQuestions(jobSeekerId.value, jobId.value, {
      onStart: () => {
        interviewStatus.value = 'streaming';
      },
      onChunk: (content) => {
        interviewContent.value += content;
        interviewStatus.value = 'streaming';
      },
      onDone: () => {
        interviewStatus.value = 'done';
      },
      onError: (err) => {
        interviewError.value = err.message || '生成失败';
        interviewStatus.value = 'error';
      },
    });
  });

  if (force) {
    // currently the HR interview-questions endpoint has no force_refresh param in docs
  }
};

onBeforeUnmount(() => {
  interviewCancel?.();
  interviewCancel = null;
});

onLoad((options) => {
  const jsid = Number(options?.jobSeekerId);
  const uid = Number(options?.userId);
  const jid = Number(options?.jobId);
  if (!Number.isNaN(jsid)) jobSeekerId.value = jsid;
  if (!Number.isNaN(uid)) seekerUserId.value = uid;
  if (!Number.isNaN(jid)) jobId.value = jid;
  candidateName.value = String(options?.candidateName || options?.username || '');
  jobTitle.value = String(options?.jobTitle || '');

  if (jobId.value) {
    resolveCandidate().finally(() => {
      if (jobSeekerId.value) {
        loadMatch(false);
      }
    });
  }
});
</script>

<style scoped lang="scss">
@use "@/styles/variables.scss" as vars;

.candidate-ai-page {
  background: #f6f9ff;
  min-height: 100vh;
}

.page-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 10;
  height: calc(var(--status-bar-height) + 96rpx);
  padding: var(--status-bar-height) 24rpx 0;
  box-sizing: border-box;
  display: flex;
  align-items: center;
  background: #ffffff;
  border-bottom: 1rpx solid #eef2fb;
}

.header-back {
  width: 72rpx;
  height: 72rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 18rpx;
  background: #f5f7ff;
}

.back-icon {
  font-size: 34rpx;
  color: vars.$text-color;
  font-weight: 700;
}

.header-title {
  flex: 1;
  text-align: center;
  font-size: 32rpx;
  font-weight: 800;
  color: vars.$text-color;
}

.header-actions {
  width: 72rpx;
  display: flex;
  justify-content: flex-end;
}

.header-action-btn {
  width: 72rpx;
  height: 72rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 18rpx;
  background: #f5f7ff;
}

.action-icon {
  font-size: 30rpx;
  color: vars.$primary-color;
  font-weight: 800;
}

.tabs {
  position: fixed;
  top: calc(var(--status-bar-height) + 96rpx);
  left: 0;
  right: 0;
  z-index: 9;
  display: flex;
  gap: 12rpx;
  padding: 12rpx 16rpx;
  background: #ffffff;
  border-bottom: 1rpx solid #eef2fb;
}

.tab {
  flex: 1;
  height: 68rpx;
  border-radius: 999rpx;
  background: #f5f7ff;
  display: flex;
  align-items: center;
  justify-content: center;
}

.tab.active {
  background: rgba(75, 163, 255, 0.14);
}

.tab-text {
  font-size: 26rpx;
  color: vars.$text-muted;
  font-weight: 700;
}

.tab.active .tab-text {
  color: vars.$primary-color;
}

.content-scroll {
  padding: calc(var(--status-bar-height) + 96rpx + 92rpx) 16rpx 32rpx;
  box-sizing: border-box;
  min-height: 100vh;
}

.basic-card {
  background: #ffffff;
  border-radius: vars.$border-radius;
  padding: 20rpx;
  box-shadow: 0 10rpx 28rpx rgba(31, 55, 118, 0.08);
  margin-bottom: 16rpx;
}

.basic-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.basic-name {
  font-size: 32rpx;
  font-weight: 800;
  color: vars.$text-color;
}

.basic-sub {
  display: block;
  margin-top: 10rpx;
  font-size: 24rpx;
  color: vars.$text-muted;
}

.basic-badge {
  background: rgba(75, 163, 255, 0.14);
  border-radius: 999rpx;
  padding: 6rpx 14rpx;
}

.basic-badge-text {
  font-size: 22rpx;
  color: vars.$primary-color;
}

.state-card {
  background: #fff;
  border-radius: vars.$border-radius;
  padding: 28rpx;
  box-shadow: 0 10rpx 28rpx rgba(31, 55, 118, 0.08);
  margin-bottom: 16rpx;
}

.state-text {
  font-size: 26rpx;
  color: vars.$text-muted;
}

.retry-btn {
  margin-top: 16rpx;
  width: 180rpx;
  height: 68rpx;
  border-radius: 999rpx;
  background: vars.$primary-color;
  display: flex;
  align-items: center;
  justify-content: center;
}

.retry-text {
  color: #fff;
  font-size: 26rpx;
  font-weight: 700;
}

.skeleton-container {
  padding: 16rpx;
}

.skeleton-item {
  background: linear-gradient(90deg, #eef2fb 0%, #f7f9ff 50%, #eef2fb 100%);
  background-size: 200% 100%;
  animation: shimmer 1.4s infinite;
  border-radius: vars.$border-radius;
  margin-bottom: 16rpx;
}

.skeleton-radar {
  height: 260rpx;
}

.skeleton-card {
  height: 220rpx;
}

@keyframes shimmer {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}

.match-score-card {
  background: #ffffff;
  border-radius: vars.$border-radius;
  padding: 24rpx;
  display: flex;
  gap: 16rpx;
  align-items: center;
  box-shadow: 0 10rpx 28rpx rgba(31, 55, 118, 0.08);
  margin-bottom: 16rpx;
}

.radar-section {
  width: 200rpx;
  display: flex;
  justify-content: center;
}

.score-section {
  flex: 1;
}

.score-number {
  font-size: 72rpx;
  font-weight: 900;
}

.score-label {
  display: block;
  margin-top: 10rpx;
  color: vars.$text-muted;
  font-size: 24rpx;
}

.gap-analysis-card,
.card {
  background: #ffffff;
  border-radius: vars.$border-radius;
  padding: 24rpx;
  margin-bottom: 16rpx;
  box-shadow: 0 10rpx 28rpx rgba(31, 55, 118, 0.08);
}

.gap-header-main {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16rpx;
}

.gap-main-title,
.section-title {
  font-size: 30rpx;
  font-weight: 800;
  color: vars.$text-color;
}

.match-rate-badge {
  background: rgba(75, 163, 255, 0.14);
  border-radius: 999rpx;
  padding: 6rpx 14rpx;
}

.match-rate-text {
  font-size: 22rpx;
  color: vars.$primary-color;
  font-weight: 700;
}

.gap-subsection {
  margin-top: 16rpx;
}

.gap-header {
  display: flex;
  align-items: center;
  gap: 10rpx;
  margin-bottom: 12rpx;
}

.gap-icon {
  width: 30rpx;
  text-align: center;
  font-weight: 900;
}

.gap-title {
  font-size: 26rpx;
  font-weight: 700;
  color: vars.$text-color;
}

.count-badge {
  margin-left: auto;
  border-radius: 999rpx;
  padding: 4rpx 12rpx;
}

.count-badge.danger {
  background: rgba(255, 59, 48, 0.12);
}

.count-badge.info {
  background: rgba(75, 163, 255, 0.14);
}

.count-text {
  font-size: 22rpx;
  font-weight: 800;
  color: vars.$text-color;
}

.missing-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10rpx;
}

.missing-tag {
  border-radius: 999rpx;
  padding: 8rpx 14rpx;
}

.missing-tag.required {
  background: rgba(255, 59, 48, 0.12);
}

.missing-tag.optional {
  background: rgba(75, 163, 255, 0.14);
}

.missing-tag-text {
  font-size: 22rpx;
  color: vars.$text-color;
  font-weight: 700;
}

.kv {
  display: flex;
  gap: 12rpx;
  margin-top: 14rpx;
}

.k {
  width: 90rpx;
  font-size: 24rpx;
  color: vars.$text-muted;
}

.v {
  flex: 1;
  font-size: 24rpx;
  color: vars.$text-color;
  line-height: 1.6;
}

.paragraph {
  display: block;
  margin-top: 12rpx;
  font-size: 26rpx;
  color: vars.$text-color;
  line-height: 1.7;
}

.muted {
  display: block;
  margin-top: 10rpx;
  font-size: 24rpx;
  color: vars.$text-muted;
  line-height: 1.6;
}

.list-item {
  padding: 18rpx 0;
  border-top: 1rpx solid #eef2fb;
}

.list-item:first-child {
  border-top: none;
  padding-top: 0;
}

.list-title {
  font-size: 26rpx;
  font-weight: 800;
  color: vars.$text-color;
}

.bullets {
  margin-top: 16rpx;
}

.bullet {
  display: flex;
  gap: 10rpx;
  align-items: flex-start;
  margin-top: 10rpx;
}

.bullet-dot {
  color: vars.$primary-color;
  font-weight: 900;
}

.bullet-text {
  flex: 1;
  font-size: 26rpx;
  color: vars.$text-color;
  line-height: 1.6;
}

.center {
  padding: 22rpx 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16rpx;
}

.primary-btn {
  padding: 14rpx 28rpx;
  border-radius: 999rpx;
  background: vars.$primary-color;
}

.primary-btn-text {
  color: #fff;
  font-size: 26rpx;
  font-weight: 700;
}

.ghost-btn {
  padding: 12rpx 26rpx;
  border-radius: 999rpx;
  border: 2rpx solid vars.$primary-color;
}

.ghost-btn-text {
  color: vars.$primary-color;
  font-size: 26rpx;
  font-weight: 700;
}

.danger {
  color: #ff3b30;
  font-size: 26rpx;
}

.markdown-wrap {
  margin-top: 14rpx;
}

.actions-row {
  margin-top: 18rpx;
  display: flex;
  justify-content: flex-end;
}
</style>
