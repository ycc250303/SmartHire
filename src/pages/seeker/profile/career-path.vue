<template>
  <view class="career-path-page">
    <view class="page-header">
      <view class="header-back" @click="handleBack">
        <text class="back-icon">‹</text>
      </view>
      <text class="header-title">{{ t('pages.profile.careerPath.title') }}</text>
      <view class="header-actions">
        <view class="header-action-btn" @click="handleRefresh" v-if="!loading">
          <text class="action-icon">↻</text>
        </view>
      </view>
    </view>

    <scroll-view class="content-scroll" scroll-y>
      <view v-if="loading && !data" class="skeleton-container">
        <view class="skeleton-card skeleton-overview"></view>
        <view class="skeleton-card"></view>
        <view class="skeleton-card"></view>
      </view>

      <template v-else-if="!error && data">
        <view class="overview-card">
          <view class="overview-header">
            <text class="overview-title">{{ t('pages.profile.careerPath.profileSummary') }}</text>
            <text
              v-if="data.error"
              class="downgrade-text"
            >
              {{ t('pages.profile.careerPath.downgradeNote') }}
            </text>
          </view>
          <text class="current-level">
            {{ t('pages.profile.careerPath.currentLevel') }}：{{ data.profile_summary.current_level }}
          </text>
          <view class="readiness-row">
            <text class="readiness-label">
              {{ t('pages.profile.careerPath.careerReadiness') }}：
            </text>
            <text class="readiness-score">
              {{ data.profile_summary.career_readiness_score }}
            </text>
          </view>
          <ProgressBar
            :percentage="data.profile_summary.career_readiness_score"
            class="readiness-progress"
          />

          <view class="tags-section">
            <view v-if="data.profile_summary.key_strengths.length" class="tag-group">
              <text class="tag-group-title">
                {{ t('pages.profile.careerPath.strengths') }}
              </text>
              <view class="tag-list">
                <view
                  v-for="item in data.profile_summary.key_strengths"
                  :key="item"
                  class="tag strength-tag"
                >
                  <text class="tag-text">{{ item }}</text>
                </view>
              </view>
            </view>

            <view v-if="data.profile_summary.key_weaknesses.length" class="tag-group">
              <text class="tag-group-title">
                {{ t('pages.profile.careerPath.weaknesses') }}
              </text>
              <view class="tag-list">
                <view
                  v-for="item in data.profile_summary.key_weaknesses"
                  :key="item"
                  class="tag weakness-tag"
                >
                  <text class="tag-text">{{ item }}</text>
                </view>
              </view>
            </view>
          </view>
        </view>

        <view class="card gap-card">
          <text class="card-title">
            {{ t('pages.profile.careerPath.gapAnalysis') }}
          </text>

          <view class="gap-section">
            <view class="gap-header">
              <text class="gap-title">
                {{ t('pages.profile.careerPath.skillGaps') }}
              </text>
              <view class="gap-count-badge">
                <text class="gap-count-text">
                  {{ data.gap_analysis.skill_gaps.length }}
                </text>
              </view>
            </view>
            <view v-if="data.gap_analysis.skill_gaps.length" class="skill-gap-list">
              <view
                v-for="item in data.gap_analysis.skill_gaps"
                :key="item.skill_name"
                class="skill-gap-item"
              >
                <view class="skill-gap-main">
                  <text class="skill-name">{{ item.skill_name }}</text>
                  <view
                    class="priority-badge"
                    :class="priorityClass(item.priority)"
                  >
                    <text class="priority-text">{{ item.priority }}</text>
                  </view>
                </view>
                <text class="gap-reason">{{ item.gap_reason }}</text>
              </view>
            </view>
            <text
              v-else
              class="gap-empty"
            >
              {{ t('pages.profile.careerPath.emptySkillGaps') }}
            </text>
          </view>
        </view>

        <view class="card roadmap-card">
          <text class="card-title">
            {{ t('pages.profile.careerPath.careerRoadmap') }}
          </text>

          <view class="roadmap-overview">
            <text class="roadmap-target">
              {{ data.career_roadmap.overview.target_position }}
            </text>
            <text class="roadmap-level">
              {{ data.career_roadmap.overview.current_level }}
            </text>
            <view class="roadmap-meta">
              <text class="meta-item">
                {{ data.career_roadmap.overview.plan_duration_months }}{{ t('pages.profile.careerPath.months') }}
              </text>
              <text class="meta-dot">·</text>
              <text class="meta-item">
                {{ data.career_roadmap.overview.milestones_count }}
              </text>
            </view>
          </view>

          <view class="timeline">
            <view
              v-for="item in data.career_roadmap.phases"
              :key="item.phase"
              class="timeline-item"
            >
              <view class="timeline-left">
                <view class="timeline-dot">
                  <text class="dot-text">{{ item.phase }}</text>
                </view>
                <view class="timeline-line"></view>
              </view>
              <view class="timeline-content">
                <text class="phase-title">{{ item.title }}</text>
                <text class="phase-duration">
                  {{ item.duration_months }}{{ t('pages.profile.careerPath.months') }}
                </text>
                <text class="phase-description">
                  {{ item.description }}
                </text>
                <view v-if="item.key_skills.length" class="phase-skills">
                  <view
                    v-for="skill in item.key_skills"
                    :key="skill"
                    class="phase-skill-tag"
                  >
                    <text class="phase-skill-text">{{ skill }}</text>
                  </view>
                </view>
                <view v-if="item.deliverables.length" class="phase-deliverables">
                  <text class="deliverable-label">
                    {{ t('pages.jobs.careerPlanningPage.immediateSuggestions') }}
                  </text>
                  <view
                    v-for="deliverable in item.deliverables"
                    :key="deliverable"
                    class="deliverable-item"
                  >
                    <text class="deliverable-text">{{ deliverable }}</text>
                  </view>
                </view>
              </view>
            </view>
          </view>
        </view>

        <view class="card actions-card">
          <text class="card-title">
            {{ t('pages.profile.careerPath.immediateActions') }}
          </text>
          <view v-if="data.career_roadmap.immediate_actions.length" class="action-list">
            <view
              v-for="(item, index) in data.career_roadmap.immediate_actions"
              :key="index"
              class="action-item"
            >
              <view class="action-header">
                <text class="action-title">{{ item.title }}</text>
                <view class="action-priority" :class="priorityClass(item.priority)">
                  <text class="action-priority-text">
                    {{
                      item.priority === '高'
                        ? t('pages.profile.careerPath.highPriority')
                        : item.priority === '中'
                          ? t('pages.profile.careerPath.mediumPriority')
                          : t('pages.profile.careerPath.lowPriority')
                    }}
                  </text>
                </view>
              </view>
              <text class="action-description">
                {{ item.description }}
              </text>
            </view>
          </view>
          <text
            v-else
            class="gap-empty"
          >
            {{ t('pages.profile.careerPath.emptyActions') }}
          </text>
        </view>

        <view class="card learning-card">
          <text class="card-title">
            {{ t('pages.profile.careerPath.learningPlan') }}
          </text>

          <view v-if="data.learning_plan.skills.length" class="learning-list">
            <view
              v-for="(item, index) in data.learning_plan.skills"
              :key="item.skill_name"
              class="learning-item"
            >
              <view class="learning-header">
                <view class="learning-title-row">
                  <text class="learning-title">{{ item.skill_name }}</text>
                  <view class="learning-badges">
                    <view
                      class="learning-priority-badge"
                      :class="priorityClass(item.priority)"
                    >
                      <text class="learning-priority-text">
                        {{
                          item.priority === '高'
                            ? t('pages.profile.careerPath.highPriority')
                            : item.priority === '中'
                              ? t('pages.profile.careerPath.mediumPriority')
                              : t('pages.profile.careerPath.lowPriority')
                        }}
                      </text>
                    </view>
                    <view class="learning-time-badge">
                      <text class="learning-time-text">
                        {{ item.estimated_weeks }}{{ t('pages.profile.careerPath.weeks') }}
                      </text>
                    </view>
                  </view>
                </view>
                <view class="learning-meta">
                  <text class="meta-text">
                    {{ item.reason }}
                  </text>
                  <text class="meta-text">
                    {{ t('pages.profile.careerPath.difficulty' + difficultyKey(item.difficulty)) }}
                  </text>
                </view>
                <view class="expand-btn" @click="toggleSkillExpand(index)">
                  <text class="expand-icon" :class="{ expanded: expandedSkills[index] }">▼</text>
                </view>
              </view>

              <view v-if="expandedSkills[index]" class="learning-detail">
                <view class="detail-section">
                  <text class="detail-title">
                    {{ t('pages.jobs.careerPlanningPage.learningSteps') }}
                  </text>
                  <view
                    v-for="(step, stepIndex) in item.learning_steps"
                    :key="stepIndex"
                    class="detail-line"
                  >
                    <text class="detail-number">
                      {{ stepIndex + 1 }}.
                    </text>
                    <text class="detail-text">{{ step }}</text>
                  </view>
                </view>

                <view v-if="item.resources.length" class="detail-section">
                  <text class="detail-title">
                    {{ t('pages.jobs.careerPlanningPage.recommendedResources') }}
                  </text>
                  <view
                    v-for="(res, resIndex) in item.resources"
                    :key="resIndex"
                    class="resource-item"
                  >
                    <text class="resource-name">
                      {{ res.name }}
                    </text>
                    <text class="resource-type">
                      {{ res.type }}
                    </text>
                    <text
                      v-if="res.url"
                      class="resource-link"
                      @click.stop="openResource(res.url)"
                    >
                      {{ res.url }}
                    </text>
                  </view>
                </view>
              </view>
            </view>
          </view>
          <text
            v-else
            class="gap-empty"
          >
            {{ t('pages.profile.careerPath.emptyLearningPlan') }}
          </text>
        </view>

        <view class="card interview-card">
          <view class="interview-header" @click="toggleInterview">
            <text class="card-title">
              {{ t('pages.profile.careerPath.interviewPrep') }}
            </text>
            <text class="collapse-icon" :class="{ expanded: interviewExpanded }">▼</text>
          </view>

          <view v-if="interviewExpanded" class="interview-content">
            <view class="interview-block">
              <text class="interview-title">
                {{ t('pages.jobs.careerPlanningPage.projectTips') }}
              </text>
              <StreamingMarkdownContent :content="data.interview_prep.project_tips" />
            </view>
            <view class="interview-block">
              <text class="interview-title">
                {{ t('pages.jobs.careerPlanningPage.possibleQuestions') }}
              </text>
              <StreamingMarkdownContent :content="data.interview_prep.possible_questions" />
            </view>
            <view class="interview-block">
              <text class="interview-title">
                {{ t('pages.jobs.careerPlanningPage.weaknessStrategy') }}
              </text>
              <StreamingMarkdownContent :content="data.interview_prep.weakness_strategy" />
            </view>
            <view class="interview-block">
              <text class="interview-title">
                {{ t('pages.jobs.careerPlanningPage.strengthEmphasis') }}
              </text>
              <StreamingMarkdownContent :content="data.interview_prep.strength_emphasis" />
            </view>
          </view>
        </view>
      </template>

      <view v-if="error" class="error-container">
        <text class="error-icon">⚠️</text>
        <text class="error-text">{{ error }}</text>
        <view class="retry-button" @click="loadCareerPath(false)">
          <text class="retry-text">{{ t('pages.profile.careerPath.retry') }}</text>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { onLoad } from '@dcloudio/uni-app';
import { ref } from 'vue';
import { t } from '@/locales';
import ProgressBar from '@/components/common/ProgressBar.vue';
import StreamingMarkdownContent from '@/components/career-planning/StreamingMarkdownContent.vue';
import {
  getPersonalCareerPath,
  type PersonalCareerPathResponse,
} from '@/services/api/seeker';

const loading = ref(true);
const error = ref<string | null>(null);
const data = ref<PersonalCareerPathResponse | null>(null);
const expandedSkills = ref<Record<number, boolean>>({});
const interviewExpanded = ref(false);

onLoad(() => {
  uni.setNavigationBarTitle({
    title: t('navigation.careerPath'),
  });
  loadCareerPath(false);
});

function priorityClass(priority: string): string {
  if (priority === '高') {
    return 'priority-high';
  }
  if (priority === '中') {
    return 'priority-medium';
  }
  return 'priority-low';
}

function difficultyKey(value: string): string {
  if (value === '简单') {
    return 'Easy';
  }
  if (value === '中等') {
    return 'Medium';
  }
  if (value === '困难') {
    return 'Hard';
  }
  return '';
}

async function loadCareerPath(forceRefresh: boolean) {
  loading.value = true;
  error.value = null;
  
  if (forceRefresh) {
    data.value = null;
  }
  
  try {
    const response = await getPersonalCareerPath(undefined, forceRefresh);
    data.value = response;
    loading.value = false;
  } catch (err) {
    console.error('Personal career path error:', err);
    error.value =
      err instanceof Error
        ? err.message
        : t('pages.profile.careerPath.loadError');
    loading.value = false;
    uni.showToast({
      title: t('pages.jobs.careerPlanningPage.networkError'),
      icon: 'none',
    });
  }
}

function handleBack() {
  uni.navigateBack();
}

function handleRefresh() {
  if (loading.value) {
    return;
  }
  expandedSkills.value = {};
  interviewExpanded.value = false;
  loadCareerPath(true);
}

function toggleSkillExpand(index: number) {
  expandedSkills.value[index] = !expandedSkills.value[index];
}

function toggleInterview() {
  interviewExpanded.value = !interviewExpanded.value;
}

function openResource(url: string) {
  if (!url) {
    return;
  }
  
  // #ifdef H5
  window.open(url, '_blank');
  // #endif
  
  // #ifdef APP-PLUS
  plus.runtime.openURL(url);
  // #endif
  
  // #ifdef MP
  uni.setClipboardData({
    data: url,
    success: () => {
      uni.showToast({
        title: '链接已复制',
        icon: 'none'
      });
    }
  });
  // #endif
}
</script>

<style lang="scss" scoped>
@use "@/styles/variables.scss" as vars;

.career-path-page {
  min-height: 100vh;
  background: #f2f2f7;
  display: flex;
  flex-direction: column;
}

.page-header {
  position: sticky;
  top: 0;
  z-index: 1000;
  background: #ffffff;
  padding-top: calc(var(--status-bar-height, 0px) + vars.$spacing-md);
  padding-bottom: vars.$spacing-md;
  padding-left: vars.$spacing-lg;
  padding-right: vars.$spacing-lg;
  display: flex;
  align-items: center;
  gap: vars.$spacing-md;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
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

.header-actions {
  display: flex;
  gap: vars.$spacing-md;
  align-items: center;
}

.header-action-btn {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;

  &:active {
    opacity: 0.6;
    transform: scale(0.95);
  }
}

.action-icon {
  font-size: 40rpx;
  color: vars.$text-color;
  font-weight: 300;
  line-height: 1;
}

.content-scroll {
  flex: 1;
  height: calc(100vh - calc(var(--status-bar-height, 44px) + 88rpx));
}

.skeleton-container {
  padding: vars.$spacing-md;
  display: flex;
  flex-direction: column;
  gap: vars.$spacing-md;
}

.skeleton-card {
  background: #e5e5ea;
  border-radius: 16rpx;
  height: 220rpx;
  animation: skeleton-loading 1.5s ease-in-out infinite;
}

.skeleton-overview {
  height: 260rpx;
}

@keyframes skeleton-loading {
  0% {
    opacity: 1;
  }
  50% {
    opacity: 0.6;
  }
  100% {
    opacity: 1;
  }
}

.overview-card {
  background: #ffffff;
  border-radius: 24rpx;
  padding: vars.$spacing-lg;
  margin: vars.$spacing-md;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);
}

.overview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: vars.$spacing-md;
}

.overview-title {
  font-size: 32rpx;
  font-weight: 700;
  color: vars.$text-color;
}

.downgrade-text {
  font-size: 22rpx;
  color: #ff9500;
}

.current-level {
  font-size: 26rpx;
  color: vars.$text-color;
  margin-bottom: vars.$spacing-sm;
}

.readiness-row {
  display: flex;
  align-items: baseline;
  margin-bottom: vars.$spacing-sm;
}

.readiness-label {
  font-size: 24rpx;
  color: vars.$text-muted;
  margin-right: 8rpx;
}

.readiness-score {
  font-size: 40rpx;
  font-weight: 700;
  color: #007aff;
}

.readiness-progress {
  margin-bottom: vars.$spacing-md;
}

.tags-section {
  display: flex;
  flex-direction: column;
  gap: vars.$spacing-md;
}

.tag-group-title {
  font-size: 24rpx;
  color: vars.$text-muted;
  margin-bottom: vars.$spacing-xs;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: vars.$spacing-sm;
}

.tag {
  padding: 8rpx 16rpx;
  border-radius: 999rpx;
}

.strength-tag {
  background: #e8f5e9;

  .tag-text {
    color: #34c759;
  }
}

.weakness-tag {
  background: #fff4e5;

  .tag-text {
    color: #ff9500;
  }
}

.tag-text {
  font-size: 24rpx;
  font-weight: 500;
}

.card {
  background: #ffffff;
  border-radius: 24rpx;
  padding: vars.$spacing-lg;
  margin: vars.$spacing-md;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);
}

.card-title {
  font-size: 32rpx;
  font-weight: 700;
  color: vars.$text-color;
  margin-bottom: vars.$spacing-md;
}

.gap-card {
  margin-top: 0;
}

.gap-section {
  margin-bottom: vars.$spacing-lg;

  &:last-child {
    margin-bottom: 0;
  }
}

.gap-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: vars.$spacing-sm;
}

.gap-title {
  font-size: 28rpx;
  font-weight: 600;
  color: vars.$text-color;
}

.gap-count-badge {
  min-width: 40rpx;
  height: 40rpx;
  border-radius: 20rpx;
  background: #ff3b30;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 16rpx;
}

.gap-count-text {
  font-size: 22rpx;
  color: #ffffff;
  font-weight: 700;
}

.skill-gap-list {
  display: flex;
  flex-direction: column;
  gap: vars.$spacing-sm;
}

.skill-gap-item {
  padding: vars.$spacing-sm vars.$spacing-md;
  border-radius: 16rpx;
  background: #f5f5f7;
}

.skill-gap-main {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 4rpx;
}

.skill-name {
  font-size: 26rpx;
  font-weight: 500;
  color: vars.$text-color;
}

.priority-badge {
  padding: 4rpx 12rpx;
  border-radius: 999rpx;
}

.priority-text {
  font-size: 22rpx;
  font-weight: 600;
}

.priority-high {
  background: rgba(255, 59, 48, 0.12);

  .priority-text {
    color: #ff3b30;
  }
}

.priority-medium {
  background: rgba(255, 149, 0, 0.12);

  .priority-text {
    color: #ff9500;
  }
}

.priority-low {
  background: rgba(0, 122, 255, 0.12);

  .priority-text {
    color: #007aff;
  }
}

.gap-reason {
  font-size: 24rpx;
  color: vars.$text-muted;
}

.gap-detail-list {
  display: flex;
  flex-direction: column;
  gap: vars.$spacing-xs;
}

.gap-detail-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.detail-label {
  font-size: 24rpx;
  color: vars.$text-muted;
}

.detail-value {
  font-size: 24rpx;
  color: vars.$text-color;
}

.gap-value {
  color: #ff3b30;
}

.qualified-badge {
  padding: 6rpx 16rpx;
  border-radius: 999rpx;
}

.qualified {
  background: #e8f5e9;

  .qualified-text {
    color: #34c759;
  }
}

.not-qualified {
  background: #ffebee;

  .qualified-text {
    color: #ff3b30;
  }
}

.qualified-text {
  font-size: 22rpx;
  font-weight: 600;
}

.gap-empty {
  font-size: 24rpx;
  color: vars.$text-muted;
}

.roadmap-card {
  margin-top: 0;
}

.roadmap-overview {
  margin-bottom: vars.$spacing-md;
}

.roadmap-target {
  font-size: 30rpx;
  font-weight: 600;
  color: vars.$text-color;
}

.roadmap-level {
  font-size: 24rpx;
  color: vars.$text-muted;
  margin-top: 4rpx;
}

.roadmap-meta {
  display: flex;
  align-items: center;
  margin-top: 8rpx;
}

.meta-item {
  font-size: 22rpx;
  color: vars.$text-muted;
}

.meta-dot {
  margin: 0 8rpx;
  color: vars.$text-muted;
}

.timeline {
  margin-top: vars.$spacing-md;
}

.timeline-item {
  display: flex;
  gap: vars.$spacing-md;
}

.timeline-item + .timeline-item {
  margin-top: vars.$spacing-lg;
}

.timeline-left {
  width: 40rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.timeline-dot {
  width: 40rpx;
  height: 40rpx;
  border-radius: 20rpx;
  background: #007aff;
  display: flex;
  align-items: center;
  justify-content: center;
}

.dot-text {
  font-size: 24rpx;
  color: #ffffff;
  font-weight: 600;
}

.timeline-line {
  flex: 1;
  width: 4rpx;
  margin-top: 4rpx;
  background: linear-gradient(180deg, #007aff 0%, rgba(0, 122, 255, 0) 100%);
}

.timeline-content {
  flex: 1;
  padding-bottom: vars.$spacing-sm;
}

.phase-title {
  font-size: 28rpx;
  font-weight: 600;
  color: vars.$text-color;
}

.phase-duration {
  font-size: 24rpx;
  color: vars.$text-muted;
  margin-top: 4rpx;
}

.phase-description {
  font-size: 24rpx;
  color: vars.$text-color;
  margin-top: 8rpx;
}

.phase-skills {
  display: flex;
  flex-wrap: wrap;
  gap: vars.$spacing-xs;
  margin-top: 8rpx;
}

.phase-skill-tag {
  padding: 6rpx 12rpx;
  border-radius: 999rpx;
  background: #e3f2fd;
}

.phase-skill-text {
  font-size: 22rpx;
  color: #1976d2;
}

.phase-deliverables {
  margin-top: 8rpx;
}

.deliverable-label {
  font-size: 22rpx;
  color: vars.$text-muted;
}

.deliverable-item {
  margin-top: 4rpx;
}

.deliverable-text {
  font-size: 24rpx;
  color: vars.$text-color;
}

.actions-card {
  margin-top: 0;
}

.action-list {
  display: flex;
  flex-direction: column;
  gap: vars.$spacing-md;
}

.action-item {
  padding: vars.$spacing-md;
  border-radius: 16rpx;
  background: #f5f5f7;
}

.action-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 4rpx;
}

.action-title {
  font-size: 26rpx;
  font-weight: 500;
  color: vars.$text-color;
}

.action-priority {
  padding: 4rpx 12rpx;
  border-radius: 999rpx;
}

.action-priority-text {
  font-size: 22rpx;
  font-weight: 600;
}

.action-description {
  font-size: 24rpx;
  color: vars.$text-muted;
}

.learning-card {
  margin-top: 0;
}

.learning-list {
  display: flex;
  flex-direction: column;
  gap: vars.$spacing-md;
}

.learning-item {
  border-radius: 16rpx;
  background: #f5f5f7;
  padding: vars.$spacing-md;
}

.learning-header {
  position: relative;
  padding-right: 40rpx;
}

.learning-title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4rpx;
}

.learning-title {
  font-size: 28rpx;
  font-weight: 600;
  color: vars.$text-color;
}

.learning-badges {
  display: flex;
  gap: 8rpx;
}

.learning-priority-badge {
  padding: 4rpx 12rpx;
  border-radius: 999rpx;
}

.learning-priority-text {
  font-size: 22rpx;
  font-weight: 600;
}

.learning-time-badge {
  padding: 4rpx 12rpx;
  border-radius: 999rpx;
  background: #e3f2fd;
}

.learning-time-text {
  font-size: 22rpx;
  color: #1976d2;
}

.learning-meta {
  margin-top: 4rpx;
}

.meta-text {
  font-size: 22rpx;
  color: vars.$text-muted;
}

.expand-btn {
  position: absolute;
  right: 0;
  top: 0;
  padding: 8rpx;
}

.expand-icon {
  font-size: 22rpx;
  color: vars.$text-muted;
  transition: transform 0.3s ease;

  &.expanded {
    transform: rotate(180deg);
  }
}

.learning-detail {
  margin-top: vars.$spacing-sm;
  padding-top: vars.$spacing-sm;
  border-top: 1rpx solid #e5e5ea;
}

.detail-section {
  margin-top: vars.$spacing-sm;
}

.detail-title {
  font-size: 24rpx;
  font-weight: 600;
  color: vars.$text-color;
}

.detail-line {
  display: flex;
  margin-top: 4rpx;
}

.detail-number {
  font-size: 22rpx;
  color: vars.$text-muted;
  margin-right: 8rpx;
}

.detail-text {
  font-size: 24rpx;
  color: vars.$text-color;
}

.resource-item {
  margin-top: 4rpx;
}

.resource-name {
  font-size: 24rpx;
  color: vars.$text-color;
}

.resource-type {
  font-size: 22rpx;
  color: vars.$text-muted;
}

.resource-link {
  font-size: 22rpx;
  color: #007aff;
  margin-top: 2rpx;
}

.interview-card {
  margin-top: 0;
  margin-bottom: 64rpx;
}

.interview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.collapse-icon {
  font-size: 24rpx;
  color: vars.$text-muted;
  transition: transform 0.3s ease-in-out;

  &.expanded {
    transform: rotate(180deg);
  }
}

.interview-content {
  margin-top: vars.$spacing-md;
  padding-top: vars.$spacing-md;
  border-top: 1rpx solid #e5e5ea;
}

.interview-block + .interview-block {
  margin-top: vars.$spacing-md;
}

.interview-title {
  font-size: 26rpx;
  font-weight: 600;
  color: vars.$text-color;
  margin-bottom: 4rpx;
}

.error-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: vars.$spacing-xl;
  margin: vars.$spacing-md;
}

.error-icon {
  font-size: 96rpx;
  margin-bottom: vars.$spacing-md;
}

.error-text {
  font-size: 28rpx;
  color: #ff3b30;
  text-align: center;
  margin-bottom: vars.$spacing-lg;
}

.retry-button {
  padding: vars.$spacing-sm vars.$spacing-lg;
  background: vars.$primary-color;
  border-radius: vars.$border-radius-sm;
  cursor: pointer;

  &:active {
    opacity: 0.6;
    transform: scale(0.98);
  }

  .retry-text {
    font-size: 28rpx;
    color: #ffffff;
    font-weight: 500;
  }
}
</style>


