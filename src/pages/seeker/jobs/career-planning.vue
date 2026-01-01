<template>
  <view class="career-planning-page">
    <view class="page-header">
      <view class="header-back" @click="handleBack">
        <text class="back-icon">‹</text>
      </view>
      <text class="header-title">{{ t('pages.jobs.careerPlanningPage.title') }}</text>
    </view>

    <scroll-view class="content-scroll" scroll-y v-if="!loading && !error">
      <view class="match-analysis-section" v-if="analysisData">
        <text class="section-title">{{ t('pages.jobs.careerPlanningPage.matchAnalysis') }}</text>
        
        <view class="match-score-card">
          <Gauge 
            :value="analysisData.match_analysis?.overall_score || 0" 
            :label="t('pages.jobs.careerPlanningPage.overallScore')"
          />
        </view>

        <view class="radar-chart-card">
          <RadarChart
            :data="radarData"
            :labels="radarLabels"
            :max="1"
          />
        </view>
      </view>

      <view class="gap-analysis-section" v-if="analysisData?.gap_analysis">
        <text class="section-title">{{ t('pages.jobs.careerPlanningPage.gapAnalysis') }}</text>

        <view class="gap-card" v-if="analysisData.gap_analysis.skills">
          <view class="gap-card-header">
            <text class="gap-card-title">{{ t('pages.jobs.careerPlanningPage.skills') }}</text>
            <text class="gap-card-subtitle">{{ Math.round((analysisData.gap_analysis.skills.match_rate || 0) * 100) }}% {{ t('pages.jobs.careerPlanningPage.matched') }}</text>
          </view>

          <view class="skill-list" v-if="analysisData.gap_analysis.skills.matched && analysisData.gap_analysis.skills.matched.length > 0">
            <SkillProgress
              v-for="skill in analysisData.gap_analysis.skills.matched"
              :key="skill.name"
              :name="skill.name"
              :level="skill.your_level"
              :maxLevel="4"
              :isRequired="skill.is_required"
            />
          </view>

          <view class="missing-skills" v-if="analysisData.gap_analysis.skills.required_missing && analysisData.gap_analysis.skills.required_missing.length > 0">
            <text class="missing-title">{{ t('pages.jobs.careerPlanningPage.required') }} {{ t('pages.jobs.careerPlanningPage.missing') }}:</text>
            <view class="missing-tags">
              <view v-for="skill in analysisData.gap_analysis.skills.required_missing" :key="skill" class="missing-tag required">
                <text class="missing-tag-text">{{ skill }}</text>
              </view>
            </view>
          </view>

          <view class="missing-skills" v-if="analysisData.gap_analysis.skills.optional_missing && analysisData.gap_analysis.skills.optional_missing.length > 0">
            <text class="missing-title">{{ t('pages.jobs.careerPlanningPage.optional') }} {{ t('pages.jobs.careerPlanningPage.missing') }}:</text>
            <view class="missing-tags">
              <view v-for="skill in analysisData.gap_analysis.skills.optional_missing" :key="skill" class="missing-tag optional">
                <text class="missing-tag-text">{{ skill }}</text>
              </view>
            </view>
          </view>
        </view>

        <view class="gap-card" v-if="analysisData.gap_analysis.experience">
          <view class="gap-card-header">
            <text class="gap-card-title">{{ t('pages.jobs.careerPlanningPage.experience') }}</text>
          </view>
          <view class="gap-info">
            <view class="gap-info-item">
              <text class="gap-info-label">{{ t('pages.jobs.careerPlanningPage.yourYears') }}:</text>
              <text class="gap-info-value">{{ analysisData.gap_analysis.experience.your_years }}年</text>
            </view>
            <view class="gap-info-item">
              <text class="gap-info-label">{{ t('pages.jobs.careerPlanningPage.requiredText') }}:</text>
              <text class="gap-info-value">{{ analysisData.gap_analysis.experience.required_text }}</text>
            </view>
            <view class="gap-status" :class="{ qualified: analysisData.gap_analysis.experience.is_qualified }">
              <text class="gap-status-text">{{ analysisData.gap_analysis.experience.is_qualified ? t('pages.jobs.careerPlanningPage.isQualified') : t('pages.jobs.careerPlanningPage.notQualified') }}</text>
            </view>
          </view>
        </view>

        <view class="gap-card" v-if="analysisData.gap_analysis.education">
          <view class="gap-card-header">
            <text class="gap-card-title">{{ t('pages.jobs.careerPlanningPage.education') }}</text>
          </view>
          <view class="gap-info">
            <view class="gap-info-item">
              <text class="gap-info-label">{{ t('pages.jobs.careerPlanningPage.yourEducation') }}:</text>
              <text class="gap-info-value">{{ analysisData.gap_analysis.education.your_text }}</text>
            </view>
            <view class="gap-info-item">
              <text class="gap-info-label">{{ t('pages.jobs.careerPlanningPage.requiredText') }}:</text>
              <text class="gap-info-value">{{ analysisData.gap_analysis.education.required_text }}</text>
            </view>
            <view class="gap-status" :class="{ qualified: analysisData.gap_analysis.education.is_qualified }">
              <text class="gap-status-text">{{ analysisData.gap_analysis.education.is_qualified ? t('pages.jobs.careerPlanningPage.isQualified') : t('pages.jobs.careerPlanningPage.notQualified') }}</text>
            </view>
          </view>
        </view>
      </view>

      <view class="learning-plan-section">
        <view class="section-header">
          <text class="section-title">{{ t('pages.jobs.careerPlanningPage.learningPlan') }}</text>
          <view v-if="learningPlanLoading" class="streaming-indicator">
            <view class="streaming-dot"></view>
            <text class="streaming-label">{{ t('pages.jobs.careerPlanningPage.streaming') }}</text>
          </view>
        </view>
        <view class="learning-content">
          <MarkdownContent v-if="learningPlanContent" :content="learningPlanContent" />
          <view v-else-if="!learningPlanLoading" class="empty-hint">
            <text class="empty-text">{{ t('pages.jobs.careerPlanningPage.loadError') }}</text>
          </view>
        </view>
      </view>

      <view class="interview-prep-section">
        <view class="section-header">
          <text class="section-title">{{ t('pages.jobs.careerPlanningPage.interviewPrep') }}</text>
          <view v-if="interviewPrepLoading" class="streaming-indicator">
            <view class="streaming-dot"></view>
            <text class="streaming-label">{{ t('pages.jobs.careerPlanningPage.streaming') }}</text>
          </view>
        </view>
        <view class="interview-content">
          <MarkdownContent v-if="interviewPrepContent" :content="interviewPrepContent" />
          <view v-else-if="!interviewPrepLoading" class="empty-hint">
            <text class="empty-text">{{ t('pages.jobs.careerPlanningPage.loadError') }}</text>
          </view>
        </view>
      </view>
    </scroll-view>

    <view v-if="loading" class="loading-container">
      <text class="loading-text">{{ t('pages.jobs.careerPlanningPage.loading') }}</text>
    </view>

    <view v-if="error" class="error-container">
      <text class="error-text">{{ error }}</text>
      <button class="retry-button" @click="loadCareerPlanning">
        {{ t('pages.jobs.careerPlanningPage.retry') }}
      </button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onUnmounted } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { t } from '@/locales';
import { getCareerPlanningAnalysis, getCareerPlanningLearningPlanStream, getCareerPlanningInterviewPrepStream, type CareerPlanningAnalysisResponse } from '@/services/api/recommendations';
import Gauge from '@/components/career-planning/Gauge.vue';
import RadarChart from '@/components/career-planning/RadarChart.vue';
import SkillProgress from '@/components/career-planning/SkillProgress.vue';
import MarkdownContent from '@/components/career-planning/MarkdownContent.vue';

const jobId = ref<number | null>(null);
const loading = ref(true);
const error = ref<string | null>(null);
const analysisData = ref<CareerPlanningAnalysisResponse | null>(null);
const learningPlanContent = ref<string>('');
const interviewPrepContent = ref<string>('');
const learningPlanLoading = ref(false);
const interviewPrepLoading = ref(false);

let cancelLearningPlan: (() => void) | null = null;
let cancelInterviewPrep: (() => void) | null = null;

const radarData = computed(() => {
  if (!analysisData.value?.match_analysis) return [0, 0, 0];
  return [
    analysisData.value.match_analysis.skill_match || 0,
    analysisData.value.match_analysis.education_match || 0,
    analysisData.value.match_analysis.experience_qualified ? 1 : 0,
  ];
});

const radarLabels = computed(() => [
  t('pages.jobs.careerPlanningPage.skillMatch'),
  t('pages.jobs.careerPlanningPage.educationMatch'),
  t('pages.jobs.careerPlanningPage.experienceMatch'),
]);

onLoad((options: any) => {
  if (options?.jobId) {
    jobId.value = parseInt(options.jobId, 10);
    loadCareerPlanning();
  } else {
    error.value = 'Invalid job ID';
    loading.value = false;
  }
});

onUnmounted(() => {
  if (cancelLearningPlan) {
    cancelLearningPlan();
  }
  if (cancelInterviewPrep) {
    cancelInterviewPrep();
  }
});

async function loadCareerPlanning() {
  if (!jobId.value) return;
  
  loading.value = true;
  error.value = null;
  analysisData.value = null;
  learningPlanContent.value = '';
  interviewPrepContent.value = '';

  try {
    const analysis = await getCareerPlanningAnalysis(jobId.value);
    analysisData.value = analysis;
    loading.value = false;

    loadLearningPlan();
    loadInterviewPrep();
  } catch (err) {
    console.error('Career planning error:', err);
    error.value = err instanceof Error ? err.message : t('pages.jobs.careerPlanningPage.loadError');
    loading.value = false;
  }
}

function loadLearningPlan() {
  if (!jobId.value) return;
  
  learningPlanLoading.value = true;
  learningPlanContent.value = '';
  
  cancelLearningPlan = getCareerPlanningLearningPlanStream(
    jobId.value,
    {
      onStart: (message) => {
        console.log('[Page] Learning plan started:', message);
        learningPlanLoading.value = true;
        learningPlanContent.value = '';
      },
      onChunk: (content) => {
        console.log('[Page] Learning plan chunk received, length:', content.length);
        learningPlanContent.value += content;
        learningPlanLoading.value = true;
      },
      onDone: () => {
        console.log('[Page] Learning plan done, total content length:', learningPlanContent.value.length);
        learningPlanLoading.value = false;
      },
      onError: (err) => {
        console.error('[Page] Learning plan stream error:', err);
        learningPlanLoading.value = false;
      },
    }
  );
}

function loadInterviewPrep() {
  if (!jobId.value) return;
  
  interviewPrepLoading.value = true;
  interviewPrepContent.value = '';
  
  cancelInterviewPrep = getCareerPlanningInterviewPrepStream(
    jobId.value,
    {
      onStart: (message) => {
        console.log('[Page] Interview prep started:', message);
        interviewPrepLoading.value = true;
        interviewPrepContent.value = '';
      },
      onChunk: (content) => {
        console.log('[Page] Interview prep chunk received, length:', content.length);
        interviewPrepContent.value += content;
        interviewPrepLoading.value = true;
      },
      onDone: () => {
        console.log('[Page] Interview prep done, total content length:', interviewPrepContent.value.length);
        interviewPrepLoading.value = false;
      },
      onError: (err) => {
        console.error('[Page] Interview prep stream error:', err);
        interviewPrepLoading.value = false;
      },
    }
  );
}

function handleBack() {
  uni.navigateBack();
}
</script>

<style lang="scss" scoped>
@use "@/styles/variables.scss" as vars;

.career-planning-page {
  min-height: 100vh;
  background: vars.$surface-color;
  display: flex;
  flex-direction: column;
}

.page-header {
  position: sticky;
  top: 0;
  z-index: 1000;
  background: linear-gradient(to bottom, vars.$light-blue 0%, vars.$surface-color 100%);
  padding-top: calc(var(--status-bar-height) + vars.$spacing-md);
  padding-bottom: vars.$spacing-md;
  padding-left: vars.$spacing-lg;
  padding-right: vars.$spacing-lg;
  display: flex;
  align-items: center;
  gap: vars.$spacing-md;
}

.header-back {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;

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

.header-title {
  font-size: 36rpx;
  font-weight: 600;
  color: vars.$text-color;
  flex: 1;
}

.content-scroll {
  flex: 1;
  height: calc(100vh - calc(var(--status-bar-height) + 88rpx));
}

.match-analysis-section,
.gap-analysis-section,
.learning-plan-section,
.interview-prep-section {
  background: vars.$surface-color;
  padding: vars.$spacing-xl;
  margin-top: vars.$spacing-md;
  border-radius: vars.$border-radius;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: vars.$spacing-md;
}

.section-title {
  font-size: 34rpx;
  font-weight: 600;
  color: vars.$text-color;
  display: block;
}

.streaming-indicator {
  display: flex;
  align-items: center;
  gap: vars.$spacing-xs;
}

.streaming-dot {
  width: 12rpx;
  height: 12rpx;
  border-radius: 50%;
  background-color: vars.$primary-color;
  animation: pulse 1.5s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.5;
    transform: scale(0.8);
  }
}

.streaming-label {
  font-size: 24rpx;
  color: vars.$primary-color;
}

.match-score-card,
.radar-chart-card {
  background: vars.$bg-color;
  border-radius: vars.$border-radius-lg;
  padding: vars.$spacing-xl;
  margin-bottom: vars.$spacing-md;
  display: flex;
  justify-content: center;
  align-items: center;
}

.gap-card {
  background: vars.$bg-color;
  border-radius: vars.$border-radius-lg;
  padding: vars.$spacing-lg;
  margin-bottom: vars.$spacing-md;
}

.gap-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: vars.$spacing-md;
}

.gap-card-title {
  font-size: 30rpx;
  font-weight: 600;
  color: vars.$text-color;
}

.gap-card-subtitle {
  font-size: 26rpx;
  color: vars.$primary-color;
  font-weight: 500;
}

.skill-list {
  display: flex;
  flex-direction: column;
  gap: vars.$spacing-md;
  margin-bottom: vars.$spacing-md;
}

.missing-skills {
  margin-top: vars.$spacing-md;
}

.missing-title {
  font-size: 28rpx;
  color: vars.$text-color;
  font-weight: 500;
  display: block;
  margin-bottom: vars.$spacing-sm;
}

.missing-tags {
  display: flex;
  flex-wrap: wrap;
  gap: vars.$spacing-sm;
}

.missing-tag {
  padding: vars.$spacing-xs vars.$spacing-md;
  border-radius: vars.$border-radius-sm;
}

.missing-tag.required {
  background-color: #ffebee;
  border: 1rpx solid #ef5350;
}

.missing-tag.optional {
  background-color: #fff3e0;
  border: 1rpx solid #ff9800;
}

.missing-tag-text {
  font-size: 24rpx;
  color: vars.$text-color;
}

.gap-info {
  display: flex;
  flex-direction: column;
  gap: vars.$spacing-sm;
}

.gap-info-item {
  display: flex;
  align-items: center;
  gap: vars.$spacing-md;
}

.gap-info-label {
  font-size: 28rpx;
  color: vars.$text-muted;
  min-width: 160rpx;
}

.gap-info-value {
  font-size: 28rpx;
  color: vars.$text-color;
  font-weight: 500;
}

.gap-status {
  margin-top: vars.$spacing-sm;
  padding: vars.$spacing-xs vars.$spacing-md;
  border-radius: vars.$border-radius-sm;
  background-color: #ffebee;
  align-self: flex-start;
}

.gap-status.qualified {
  background-color: #e8f5e9;
}

.gap-status-text {
  font-size: 24rpx;
  color: vars.$text-color;
}

.learning-content,
.interview-content {
  background: vars.$bg-color;
  border-radius: vars.$border-radius-lg;
  padding: vars.$spacing-lg;
  min-height: 200rpx;
}

.streaming-status {
  padding: vars.$spacing-lg;
  text-align: center;
}

.streaming-text {
  font-size: 26rpx;
  color: vars.$text-muted;
}

.empty-hint {
  padding: vars.$spacing-lg;
  text-align: center;
}

.empty-text {
  font-size: 26rpx;
  color: vars.$text-muted;
}

.loading-container,
.error-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: vars.$spacing-xl;
}

.loading-text,
.error-text {
  font-size: 28rpx;
  color: vars.$text-muted;
  margin-bottom: vars.$spacing-lg;
}

.error-text {
  color: #ff6b6b;
}

.retry-button {
  padding: vars.$spacing-sm vars.$spacing-lg;
  background: vars.$primary-color;
  color: #ffffff;
  border-radius: vars.$border-radius-sm;
  font-size: 28rpx;
  border: none;
}
</style>
