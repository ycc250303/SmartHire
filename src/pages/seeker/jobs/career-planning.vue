<template>
  <view class="career-planning-page">
    <view class="page-header">
      <view class="header-back" @click="handleBack">
        <text class="back-icon">‹</text>
      </view>
      <text class="header-title">{{ t('pages.jobs.careerPlanningPage.title') }}</text>
      <view class="header-actions">
        <view class="header-action-btn" @click="handleRefresh" v-if="!loading">
          <text class="action-icon">↻</text>
        </view>
      </view>
    </view>

    <scroll-view 
      class="content-scroll" 
      scroll-y 
      @scrolltolower="onScrollToLower"
    >
      <view v-if="loading && !analysisData" class="skeleton-container">
        <view class="skeleton-item skeleton-radar"></view>
        <view class="skeleton-item skeleton-card"></view>
        <view class="skeleton-item skeleton-card"></view>
        <view class="skeleton-item skeleton-card"></view>
      </view>

      <template v-else-if="!error && analysisData">
        <view class="match-score-card">
          <view class="radar-section">
            <RadarChart
              :data="radarData"
              :labels="radarLabels"
              :max="100"
              :size="200"
              color="#4ba3ff"
            />
          </view>
          <view class="score-section">
            <text class="score-number" :style="{ color: scoreColor }">
              {{ Math.round(analysisData.match_analysis?.overall_score || 0) }}
            </text>
            <text class="score-label">{{ t('pages.jobs.careerPlanningPage.overallMatchScore') }}</text>
          </view>
        </view>

        <OverviewCard
          v-if="analysisData?.career_roadmap?.overview"
          :target-position="analysisData.career_roadmap.overview.target_position"
          :current-level="`${analysisData.career_roadmap.overview.current_level}·${t('pages.jobs.careerPlanningPage.skillGaps')}${analysisData.career_roadmap.overview.skill_gaps_count}${t('pages.jobs.careerPlanningPage.items')}`"
          :skill-gaps-count="analysisData.career_roadmap.overview.skill_gaps_count"
          :plan-duration-days="analysisData.career_roadmap.overview.plan_duration_days"
          :milestones-count="analysisData.career_roadmap.overview.milestones_count"
          :overall-score="analysisData.match_analysis?.overall_score"
        />

        <view class="gap-analysis-card" v-if="analysisData?.gap_analysis">
          <!-- 技能匹配分析 -->
          <view class="gap-section" v-if="analysisData.gap_analysis.skills">
            <view class="gap-header-main">
              <text class="gap-main-title">{{ t('pages.jobs.careerPlanningPage.skillGapAnalysis') }}</text>
              <view class="match-rate-badge">
                <text class="match-rate-text">{{ t('pages.jobs.careerPlanningPage.matchRate') }}: {{ Math.round((analysisData.gap_analysis.skills.match_rate || 0) * 100) }}%</text>
              </view>
            </view>
            
            <!-- 缺失的必备技能 -->
            <view class="gap-subsection" v-if="requiredMissingSkills.length > 0">
              <view class="gap-header">
                <text class="gap-icon warning">⚠️</text>
                <text class="gap-title">{{ t('pages.jobs.careerPlanningPage.missingRequiredSkills') }}</text>
                <view class="count-badge danger">
                  <text class="count-text">{{ requiredMissingSkills.length }}</text>
                </view>
              </view>
              <view class="missing-tags">
                <view 
                  v-for="(skill, index) in requiredMissingSkills" 
                  :key="index" 
                  class="missing-tag required"
                >
                  <text class="missing-tag-text">{{ skill }}</text>
                </view>
              </view>
            </view>

            <!-- 缺失的可选技能 -->
            <view class="gap-subsection" v-if="optionalMissingSkills.length > 0">
              <view class="gap-header">
                <text class="gap-icon info">ℹ️</text>
                <text class="gap-title">{{ t('pages.jobs.careerPlanningPage.missingOptionalSkills') }}</text>
                <view class="count-badge info">
                  <text class="count-text">{{ optionalMissingSkills.length }}</text>
                </view>
              </view>
              <view class="missing-tags">
                <view 
                  v-for="(skill, index) in optionalMissingSkills" 
                  :key="index" 
                  class="missing-tag optional"
                >
                  <text class="missing-tag-text">{{ skill }}</text>
                </view>
              </view>
            </view>

            <!-- 已掌握的技能 -->
            <view class="gap-subsection" v-if="matchedSkills.length > 0">
              <view class="gap-header">
                <text class="gap-icon success">✓</text>
                <text class="gap-title">{{ t('pages.jobs.careerPlanningPage.masteredSkills') }}</text>
                <view class="count-badge success">
                  <text class="count-text">{{ matchedSkills.length }}</text>
                </view>
              </view>
              <view class="matched-skills-list">
                <view 
                  v-for="(skill, index) in matchedSkills" 
                  :key="index"
                  class="matched-skill-item"
                >
                  <view class="skill-header">
                    <text class="skill-name">{{ skill.name }}</text>
                    <view class="level-badge" :class="{ 'required-badge': skill.is_required }">
                      <text class="level-text">Lv.{{ skill.your_level }}</text>
                    </view>
                  </view>
                  <ProgressBar 
                    :percentage="skill.your_level * 20"
                    :class="skill.is_required ? 'required-progress' : 'optional-progress'"
                  />
                </view>
              </view>
            </view>
          </view>

          <!-- 工作经验分析 -->
          <view class="gap-section" v-if="analysisData.gap_analysis.experience">
            <view class="gap-header-main">
              <text class="gap-main-title">{{ t('pages.jobs.careerPlanningPage.experienceAnalysis') }}</text>
              <view class="qualified-badge" :class="analysisData.gap_analysis.experience.is_qualified ? 'qualified' : 'not-qualified'">
                <text class="qualified-text">{{ analysisData.gap_analysis.experience.is_qualified ? t('pages.jobs.careerPlanningPage.qualified') : t('pages.jobs.careerPlanningPage.notQualified') }}</text>
              </view>
            </view>
            <view class="experience-details">
              <view class="detail-row">
                <text class="detail-label">{{ t('pages.jobs.careerPlanningPage.yourExperience') }}</text>
                <text class="detail-value">{{ analysisData.gap_analysis.experience.your_years }} {{ t('common.years') }}</text>
              </view>
              <view class="detail-row">
                <text class="detail-label">{{ t('pages.jobs.careerPlanningPage.requiredExperience') }}</text>
                <text class="detail-value">{{ analysisData.gap_analysis.experience.required_text }}</text>
              </view>
              <view class="detail-row" v-if="!analysisData.gap_analysis.experience.is_qualified && analysisData.gap_analysis.experience.gap_years > 0">
                <text class="detail-label">{{ t('pages.jobs.careerPlanningPage.experienceGap') }}</text>
                <text class="detail-value gap-value">{{ analysisData.gap_analysis.experience.gap_years }} {{ t('common.years') }}</text>
              </view>
            </view>
          </view>

          <!-- 学历要求分析 -->
          <view class="gap-section" v-if="analysisData.gap_analysis.education">
            <view class="gap-header-main">
              <text class="gap-main-title">{{ t('pages.jobs.careerPlanningPage.educationAnalysis') }}</text>
              <view class="qualified-badge" :class="analysisData.gap_analysis.education.is_qualified ? 'qualified' : 'not-qualified'">
                <text class="qualified-text">{{ analysisData.gap_analysis.education.is_qualified ? t('pages.jobs.careerPlanningPage.qualified') : t('pages.jobs.careerPlanningPage.notQualified') }}</text>
              </view>
            </view>
            <view class="experience-details">
              <view class="detail-row">
                <text class="detail-label">{{ t('pages.jobs.careerPlanningPage.yourEducation') }}</text>
                <text class="detail-value">{{ analysisData.gap_analysis.education.your_text }}</text>
              </view>
              <view class="detail-row">
                <text class="detail-label">{{ t('pages.jobs.careerPlanningPage.requiredEducation') }}</text>
                <text class="detail-value">{{ analysisData.gap_analysis.education.required_text }}</text>
              </view>
            </view>
          </view>
        </view>

        <TechStackTags
          v-if="analysisData?.career_roadmap?.technology_stacks && analysisData.career_roadmap.technology_stacks.length > 0"
          :technology-stacks="analysisData.career_roadmap.technology_stacks"
        />

        <PhaseTimeline
          v-if="analysisData?.career_roadmap?.phase_roadmap && analysisData.career_roadmap.phase_roadmap.length > 0"
          :phase-roadmap="analysisData.career_roadmap.phase_roadmap"
        />

        <view class="learning-plan-section">
          <text class="section-title">{{ t('pages.jobs.careerPlanningPage.structuredLearningPlan') }}</text>
          
          <!-- 无需学习 -->
          <view v-if="!hasMissingSkills" class="congrats-card">
            <text class="congrats-icon">🎉</text>
            <text class="congrats-text">{{ t('pages.jobs.careerPlanningPage.congratsAllSkills') }}</text>
          </view>

          <!-- 有学习计划 -->
          <view v-else-if="sortedLearningPlan.length > 0" class="learning-plan-cards">
            <LearningPlanCard
              v-for="(skill, index) in sortedLearningPlan"
              :key="index"
              :skill="skill"
              :is-first="index === 0"
              @start="handleSkillStart"
              @complete="handleSkillComplete"
            />
          </view>

          <!-- 备选方案：基于 gap_analysis 自动生成简单学习建议 -->
          <view v-else class="learning-plan-auto">
            <text class="auto-hint">{{ t('pages.jobs.careerPlanningPage.autoGeneratedPlan') }}</text>
            
            <view class="auto-plan-list">
              <!-- 必备技能 -->
              <view 
                v-for="(skill, index) in requiredMissingSkills" 
                :key="'req-' + index" 
                class="simple-skill-card priority-high"
              >
                <view class="skill-card-header">
                  <view class="header-content">
                    <text class="skill-title">{{ skill }}</text>
                    <view class="skill-badges">
                      <view class="priority-tag high">
                        <text class="tag-text">{{ t('pages.jobs.careerPlanningPage.priorityHigh') }}</text>
                      </view>
                      <view class="required-tag">
                        <text class="tag-text">{{ t('pages.jobs.careerPlanningPage.required') }}</text>
                      </view>
                    </view>
                  </view>
                  <view class="expand-btn" @click="toggleSkillExpand('req-' + index)">
                    <text class="expand-icon" :class="{ expanded: expandedSkills['req-' + index] }">▼</text>
                  </view>
                </view>
                
                <view class="skill-brief">
                  <text class="brief-icon">⚠️</text>
                  <text class="brief-text">{{ getSkillBrief(skill, true) }}</text>
                </view>

                <view v-if="expandedSkills['req-' + index]" class="skill-details">
                  <view class="detail-section">
                    <text class="section-label">{{ t('pages.jobs.careerPlanningPage.whyLearn') }}</text>
                    <text class="section-content">{{ getSkillImportance(skill, true) }}</text>
                  </view>
                  
                  <view class="detail-section" v-if="getSkillEstimatedTime(skill)">
                    <text class="section-label">{{ t('pages.jobs.careerPlanningPage.estimatedTime') }}</text>
                    <text class="section-content">{{ getSkillEstimatedTime(skill) }}</text>
                  </view>
                  
                  <view class="detail-section">
                    <text class="section-label">{{ t('pages.jobs.careerPlanningPage.learningPath') }}</text>
                    <view class="learning-tips">
                      <view 
                        v-for="(tip, tipIndex) in getSkillLearningTips(skill)" 
                        :key="tipIndex"
                        class="tip-item"
                      >
                        <text class="tip-bullet">{{ tipIndex + 1 }}.</text>
                        <text class="tip-text">{{ tip }}</text>
                      </view>
                    </view>
                  </view>
                </view>
              </view>

              <!-- 可选技能 -->
              <view 
                v-for="(skill, index) in optionalMissingSkills.slice(0, 3)" 
                :key="'opt-' + index" 
                class="simple-skill-card priority-medium"
              >
                <view class="skill-card-header">
                  <view class="header-content">
                    <text class="skill-title">{{ skill }}</text>
                    <view class="skill-badges">
                      <view class="priority-tag medium">
                        <text class="tag-text">{{ t('pages.jobs.careerPlanningPage.priorityMedium') }}</text>
                      </view>
                      <view class="optional-tag">
                        <text class="tag-text">{{ t('pages.jobs.careerPlanningPage.optional') }}</text>
                      </view>
                    </view>
                  </view>
                  <view class="expand-btn" @click="toggleSkillExpand('opt-' + index)">
                    <text class="expand-icon" :class="{ expanded: expandedSkills['opt-' + index] }">▼</text>
                  </view>
                </view>
                
                <view class="skill-brief">
                  <text class="brief-icon">💡</text>
                  <text class="brief-text">{{ getSkillBrief(skill, false) }}</text>
                </view>

                <view v-if="expandedSkills['opt-' + index]" class="skill-details">
                  <view class="detail-section">
                    <text class="section-label">{{ t('pages.jobs.careerPlanningPage.whyLearn') }}</text>
                    <text class="section-content">{{ getSkillImportance(skill, false) }}</text>
                  </view>
                  
                  <view class="detail-section" v-if="getSkillEstimatedTime(skill)">
                    <text class="section-label">{{ t('pages.jobs.careerPlanningPage.estimatedTime') }}</text>
                    <text class="section-content">{{ getSkillEstimatedTime(skill) }}</text>
                  </view>
                  
                  <view class="detail-section">
                    <text class="section-label">{{ t('pages.jobs.careerPlanningPage.learningPath') }}</text>
                    <view class="learning-tips">
                      <view 
                        v-for="(tip, tipIndex) in getSkillLearningTips(skill)" 
                        :key="tipIndex"
                        class="tip-item"
                      >
                        <text class="tip-bullet">{{ tipIndex + 1 }}.</text>
                        <text class="tip-text">{{ tip }}</text>
                      </view>
                    </view>
                  </view>
                </view>
              </view>
            </view>
          </view>
        </view>

        <SuggestionCards
          v-if="analysisData?.career_roadmap?.immediate_suggestions && analysisData.career_roadmap.immediate_suggestions.length > 0"
          :immediate-suggestions="analysisData.career_roadmap.immediate_suggestions"
          @action="handleSuggestionAction"
        />

        <view class="interview-prep-card">
          <view class="interview-header" @click="toggleInterviewPrep">
            <text class="interview-title">{{ t('pages.jobs.careerPlanningPage.interviewPrep') }}</text>
            <text class="collapse-icon" :class="{ expanded: interviewPrepExpanded }">▼</text>
          </view>

          <view v-if="interviewPrepExpanded" class="interview-content-wrapper">
            <view v-if="interviewPrepStatus === 'initial'" class="interview-initial">
              <text class="initial-text">{{ t('pages.jobs.careerPlanningPage.interviewPrepHint') }}</text>
              <view class="initial-button" @click="loadInterviewPrep">
                <text class="button-text">{{ t('pages.jobs.careerPlanningPage.viewInterviewPrep') }}</text>
              </view>
            </view>

            <view v-else-if="interviewPrepStatus === 'loading'" class="interview-loading">
              <view class="loading-dots">
                <view class="dot"></view>
                <view class="dot"></view>
                <view class="dot"></view>
              </view>
              <text class="loading-text">{{ t('pages.jobs.careerPlanningPage.generatingInterviewPrep') }}</text>
            </view>

            <view v-else-if="interviewPrepStatus === 'streaming' || interviewPrepStatus === 'done'" class="interview-result">
              <StreamingMarkdown 
                :content="interviewPrepContent" 
                :is-streaming="interviewPrepStatus === 'streaming'"
              />
              <view v-if="interviewPrepStatus === 'done'" class="regenerate-button" @click="loadInterviewPrep">
                <text class="regenerate-text">{{ t('pages.jobs.careerPlanningPage.regenerate') }}</text>
              </view>
            </view>

            <view v-else-if="interviewPrepStatus === 'error'" class="interview-error">
              <text class="error-text">{{ interviewPrepError || t('pages.jobs.careerPlanningPage.interviewPrepError') }}</text>
              <view class="retry-button" @click="loadInterviewPrep">
                <text class="retry-text">{{ t('pages.jobs.careerPlanningPage.retry') }}</text>
              </view>
            </view>
          </view>
        </view>
      </template>

      <view v-if="error" class="error-container">
        <text class="error-icon">⚠️</text>
        <text class="error-text">{{ error }}</text>
        <view class="retry-button" @click="loadCareerPlanning(false)">
          <text class="retry-text">{{ t('pages.jobs.careerPlanningPage.retry') }}</text>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onUnmounted } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { t } from '@/locales';
import { getCareerPlanningAnalysis, getCareerPlanningInterviewPrepStream, type CareerPlanningAnalysisResponse, type StructuredSkillLearning } from '@/services/api/recommendations';
import RadarChart from '@/components/career-planning/RadarChart.vue';
import ProgressBar from '@/components/common/ProgressBar.vue';
import skillKnowledgeData from '@/data/skill-knowledge.json';
import OverviewCard from '@/components/career-planning/OverviewCard.vue';
import TechStackTags from '@/components/career-planning/TechStackTags.vue';
import PhaseTimeline from '@/components/career-planning/PhaseTimeline.vue';
import SuggestionCards from '@/components/career-planning/SuggestionCards.vue';
import LearningPlanCard from '@/components/career-planning/LearningPlanCard.vue';
import StreamingMarkdown from '@/components/career-planning/StreamingMarkdown.vue';

const jobId = ref<number | null>(null);
const loading = ref(true);
const error = ref<string | null>(null);
const analysisData = ref<CareerPlanningAnalysisResponse | null>(null);
const interviewPrepContent = ref<string>('');
const interviewPrepStatus = ref<'initial' | 'loading' | 'streaming' | 'done' | 'error'>('initial');
const interviewPrepExpanded = ref(false);
const interviewPrepError = ref<string | null>(null);
const expandedSkills = ref<Record<string, boolean>>({});

let cancelInterviewPrep: (() => void) | null = null;

const radarData = computed(() => {
  if (!analysisData.value?.match_analysis) return [0, 0, 0];
  const analysis = analysisData.value.match_analysis;
  return [
    (analysis.skill_match || 0) * 100,
    (analysis.education_match || 0) * 100,
    analysis.experience_qualified ? 100 : 0,
  ];
});

const radarLabels = computed(() => [
  t('pages.jobs.careerPlanningPage.skillMatch'),
  t('pages.jobs.careerPlanningPage.educationMatch'),
  t('pages.jobs.careerPlanningPage.experienceMatch'),
]);

const scoreColor = computed(() => {
  const score = analysisData.value?.match_analysis?.overall_score || 0;
  if (score >= 75) return '#34C759';
  if (score >= 50) return '#4ba3ff';
  if (score >= 25) return '#FF9500';
  return '#FF3B30';
});

const optionalMissingSkills = computed(() => {
  return analysisData.value?.gap_analysis?.skills?.optional_missing || [];
});

const requiredMissingSkills = computed(() => {
  return analysisData.value?.gap_analysis?.skills?.required_missing || [];
});

const matchedSkills = computed(() => {
  return analysisData.value?.gap_analysis?.skills?.matched || [];
});

const hasMissingSkills = computed(() => {
  return requiredMissingSkills.value.length > 0;
});

const sortedLearningPlan = computed(() => {
  if (!analysisData.value?.learning_plan_structured?.skills) {
    return [];
  }
  
  const skills = [...analysisData.value.learning_plan_structured.skills];
  const priorityOrder: Record<string, number> = {
    '高': 1,
    '中': 2,
    '低': 3,
  };
  
  return skills.sort((a, b) => {
    const priorityA = priorityOrder[a.priority] || 99;
    const priorityB = priorityOrder[b.priority] || 99;
    return priorityA - priorityB;
  });
});

onLoad((options: any) => {
  if (options?.jobId) {
    jobId.value = parseInt(options.jobId, 10);
    loadCareerPlanning(false);
  } else {
    error.value = t('pages.jobs.careerPlanningPage.invalidJobId');
    loading.value = false;
  }
});

onUnmounted(() => {
  if (cancelInterviewPrep) {
    cancelInterviewPrep();
  }
});

async function loadCareerPlanning(forceRefresh = false) {
  if (!jobId.value) return;
  
  if (!forceRefresh) {
    loading.value = true;
  }
  error.value = null;
  if (forceRefresh) {
    analysisData.value = null;
    interviewPrepContent.value = '';
    interviewPrepStatus.value = 'initial';
    interviewPrepExpanded.value = false;
  }

  try {
    const analysis = await getCareerPlanningAnalysis(jobId.value, forceRefresh);
    analysisData.value = analysis;
    loading.value = false;
  } catch (err) {
    console.error('Career planning error:', err);
    error.value = err instanceof Error ? err.message : t('pages.jobs.careerPlanningPage.loadError');
    loading.value = false;
    uni.showToast({
      title: t('pages.jobs.careerPlanningPage.networkError'),
      icon: 'none',
    });
  }
}

function onScrollToLower() {
  if (!interviewPrepExpanded.value && interviewPrepStatus.value === 'initial') {
    toggleInterviewPrep();
  }
}

function toggleInterviewPrep() {
  interviewPrepExpanded.value = !interviewPrepExpanded.value;
  if (interviewPrepExpanded.value && interviewPrepStatus.value === 'initial') {
    loadInterviewPrep();
  }
}

function loadInterviewPrep() {
  if (!jobId.value) return;
  
  interviewPrepStatus.value = 'loading';
  interviewPrepContent.value = '';
  interviewPrepError.value = null;
  
  if (cancelInterviewPrep) {
    cancelInterviewPrep();
  }
  
  cancelInterviewPrep = getCareerPlanningInterviewPrepStream(
    jobId.value,
    {
      onStart: () => {
        interviewPrepStatus.value = 'streaming';
        interviewPrepContent.value = '';
        interviewPrepError.value = null;
      },
      onChunk: (content) => {
        interviewPrepContent.value += content;
        interviewPrepStatus.value = 'streaming';
      },
      onDone: () => {
        interviewPrepStatus.value = 'done';
      },
      onError: (err) => {
        console.error('Interview prep stream error:', err);
        interviewPrepStatus.value = 'error';
        interviewPrepError.value = err.message || t('pages.jobs.careerPlanningPage.loadError');
      },
    }
  );
}

function handleSuggestionAction(index: number) {
  console.log('Suggestion action:', index);
}

function handleSkillStart(skill: StructuredSkillLearning) {
  uni.showToast({
    title: t('pages.jobs.careerPlanningPage.startLearningSuccess').replace('{skillName}', skill.skill_name),
    icon: 'success',
  });
}

function handleSkillComplete(skill: StructuredSkillLearning) {
  uni.showModal({
    title: t('pages.jobs.careerPlanningPage.confirmCompleteTitle'),
    content: t('pages.jobs.careerPlanningPage.confirmCompleteContent').replace('{skillName}', skill.skill_name),
    success: (res) => {
      if (res.confirm) {
        uni.showToast({
          title: t('pages.jobs.careerPlanningPage.markCompleteSuccess').replace('{skillName}', skill.skill_name),
          icon: 'success',
        });
      }
    }
  });
}

function handleBack() {
  uni.navigateBack();
}

function toggleSkillExpand(skillKey: string) {
  expandedSkills.value[skillKey] = !expandedSkills.value[skillKey];
}

// Load skill knowledge from JSON
interface SkillKnowledge {
  brief: {
    required: string;
    optional: string;
  };
  importance: {
    required: string;
    optional?: string;
  };
  estimatedTime: string;
  learningTips: string[];
}

const skillKnowledge = skillKnowledgeData as Record<string, SkillKnowledge>;

// Get skill brief description
function getSkillBrief(skillName: string, isRequired: boolean): string {
  const knowledge = skillKnowledge[skillName];
  if (!knowledge) {
    return isRequired 
      ? t('pages.jobs.careerPlanningPage.defaultRequiredBrief')
      : t('pages.jobs.careerPlanningPage.defaultOptionalBrief');
  }
  
  return isRequired ? knowledge.brief.required : knowledge.brief.optional;
}

// Get skill importance description
function getSkillImportance(skillName: string, isRequired: boolean): string {
  const knowledge = skillKnowledge[skillName];
  if (!knowledge) {
    return isRequired 
      ? t('pages.jobs.careerPlanningPage.defaultRequiredImportance')
      : t('pages.jobs.careerPlanningPage.defaultOptionalImportance');
  }
  
  return isRequired ? knowledge.importance.required : (knowledge.importance.optional || knowledge.importance.required);
}

// Get estimated learning time
function getSkillEstimatedTime(skillName: string): string {
  const knowledge = skillKnowledge[skillName];
  return knowledge?.estimatedTime || '';
}

// Get learning tips
function getSkillLearningTips(skillName: string): string[] {
  const knowledge = skillKnowledge[skillName];
  if (!knowledge) {
    return [
      t('pages.jobs.careerPlanningPage.defaultTip1'),
      t('pages.jobs.careerPlanningPage.defaultTip2'),
      t('pages.jobs.careerPlanningPage.defaultTip3')
    ];
  }
  
  return knowledge.learningTips;
}

function handleRefresh() {
  if (!jobId.value || loading.value) return;
  loadCareerPlanning(true);
}
</script>

<style lang="scss" scoped>
@use "@/styles/variables.scss" as vars;

.career-planning-page {
  min-height: 100vh;
  background: vars.$bg-color;
  display: flex;
  flex-direction: column;
}

.page-header {
  position: sticky;
  top: 0;
  z-index: 1000;
  background: vars.$surface-color;
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

.skeleton-item {
  background: #E5E5EA;
  border-radius: vars.$border-radius;
  animation: skeleton-loading 1.5s ease-in-out infinite;
}

.skeleton-radar {
  height: 320rpx;
  background: linear-gradient(90deg, #E5E5EA 25%, #F0F0F0 50%, #E5E5EA 75%);
  background-size: 200% 100%;
}

.skeleton-card {
  height: 200rpx;
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

.match-score-card {
  background: vars.$surface-color;
  border-radius: vars.$border-radius;
  padding: vars.$spacing-lg;
  margin: vars.$spacing-md vars.$spacing-md 0;
  height: 320rpx;
  display: flex;
  align-items: center;
  gap: vars.$spacing-lg;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);
}

.radar-section {
  flex: 0 0 60%;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
}

.score-section {
  flex: 0 0 40%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
}

.score-number {
  font-size: 72rpx;
  font-weight: 700;
  line-height: 1;
  margin-bottom: vars.$spacing-sm;
}

.score-label {
  font-size: 28rpx;
  color: vars.$text-muted;
}

.gap-analysis-card {
  background: vars.$surface-color;
  border-radius: vars.$border-radius;
  padding: vars.$spacing-lg;
  margin: vars.$spacing-md;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);
}

.gap-section {
  margin-bottom: vars.$spacing-xl;

  &:last-child {
    margin-bottom: 0;
  }
}

.gap-header-main {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: vars.$spacing-lg;
  padding-bottom: vars.$spacing-md;
  border-bottom: 1rpx solid #E5E5EA;
}

.gap-main-title {
  font-size: 32rpx;
  font-weight: 700;
  color: vars.$text-color;
}

.match-rate-badge {
  background: #E3F2FD;
  padding: 8rpx 20rpx;
  border-radius: 999rpx;
}

.match-rate-text {
  font-size: 24rpx;
  color: #2196F3;
  font-weight: 600;
}

.qualified-badge {
  padding: 8rpx 20rpx;
  border-radius: 999rpx;

  &.qualified {
    background: #E8F5E9;
  }

  &.not-qualified {
    background: #FFEBEE;
  }
}

.qualified-text {
  font-size: 24rpx;
  font-weight: 600;

  .qualified & {
    color: #34C759;
  }

  .not-qualified & {
    color: #FF3B30;
  }
}

.gap-subsection {
  margin-bottom: vars.$spacing-md;

  &:last-child {
    margin-bottom: 0;
  }
}

.gap-divider {
  height: 1rpx;
  background: #E5E5EA;
  margin: vars.$spacing-md 0;
}

.gap-header {
  display: flex;
  align-items: center;
  gap: vars.$spacing-sm;
  margin-bottom: vars.$spacing-md;
}

.gap-icon {
  font-size: 28rpx;

  &.warning {
    color: #FF3B30;
  }

  &.success {
    color: #34C759;
  }

  &.info {
    color: #4ba3ff;
  }
}

.gap-title {
  font-size: 28rpx;
  font-weight: 600;
  color: vars.$text-color;
  flex: 1;
}

.count-badge {
  min-width: 40rpx;
  height: 40rpx;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 12rpx;

  &.danger {
    background: #FF3B30;
  }

  &.success {
    background: #34C759;
  }

  &.info {
    background: #4ba3ff;
  }
}

.count-text {
  font-size: 22rpx;
  color: #fff;
  font-weight: 700;
}

.missing-tags {
  display: flex;
  flex-wrap: wrap;
  gap: vars.$spacing-sm;
}

.missing-tag {
  padding: 12rpx 24rpx;
  border-radius: 999rpx;

  &.required {
    background: #FFEBEE;

    .missing-tag-text {
      color: #FF3B30;
    }
  }

  &.optional {
    background: #E3F2FD;

    .missing-tag-text {
      color: #2196F3;
    }
  }
}

.missing-tag-text {
  font-size: 24rpx;
  font-weight: 500;
}

.matched-skills-list {
  display: flex;
  flex-direction: column;
  gap: vars.$spacing-md;
}

.matched-skill-item {
  display: flex;
  flex-direction: column;
  gap: vars.$spacing-xs;
}

.skill-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.skill-name {
  font-size: 26rpx;
  color: vars.$text-color;
  font-weight: 500;
}

.level-badge {
  background: #E3F2FD;
  padding: 4rpx 12rpx;
  border-radius: 999rpx;

  &.required-badge {
    background: #E8F5E9;

    .level-text {
      color: #34C759;
    }
  }
}

.level-text {
  font-size: 20rpx;
  color: #2196F3;
  font-weight: 500;
}

.experience-details {
  display: flex;
  flex-direction: column;
  gap: vars.$spacing-md;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: vars.$spacing-sm 0;
}

.detail-label {
  font-size: 26rpx;
  color: vars.$text-muted;
}

.detail-value {
  font-size: 26rpx;
  color: vars.$text-color;
  font-weight: 500;
}

.gap-value {
  color: #FF3B30;
}

.required-progress :deep(.progress-fill) {
  background-color: #34C759;
}

.optional-progress :deep(.progress-fill) {
  background-color: vars.$primary-color;
}

.learning-plan-section {
  padding: vars.$spacing-md;
}

.section-title {
  font-size: 32rpx;
  font-weight: 600;
  color: vars.$text-color;
  display: block;
  margin-bottom: vars.$spacing-md;
}

.congrats-card {
  background: vars.$surface-color;
  border-radius: vars.$border-radius;
  padding: vars.$spacing-xl;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: vars.$spacing-md;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);
}

.congrats-icon {
  font-size: 96rpx;
}

.congrats-text {
  font-size: 28rpx;
  color: vars.$text-muted;
  text-align: center;
}

.learning-plan-cards {
  display: flex;
  flex-direction: column;
  gap: vars.$spacing-md;
}

.learning-plan-fallback {
  background: vars.$surface-color;
  border-radius: vars.$border-radius;
  padding: vars.$spacing-lg;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);
}

.auto-hint {
  display: block;
  font-size: 24rpx;
  color: vars.$text-muted;
  text-align: center;
  margin-bottom: vars.$spacing-lg;
  padding-bottom: vars.$spacing-md;
  border-bottom: 1rpx solid #E5E5EA;
}

.auto-plan-list {
  display: flex;
  flex-direction: column;
  gap: vars.$spacing-md;
}

.simple-skill-card {
  background: vars.$surface-color;
  border-radius: 16rpx;
  padding: 32rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);
  position: relative;
  transition: all 0.3s ease;

  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 0;
    bottom: 0;
    width: 6rpx;
    border-radius: 16rpx 0 0 16rpx;
  }

  &.priority-high::before {
    background: linear-gradient(180deg, #FF3B30 0%, #FF6B60 100%);
  }

  &.priority-medium::before {
    background: linear-gradient(180deg, #FF9500 0%, #FFB340 100%);
  }
}

.skill-card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20rpx;
}

.header-content {
  flex: 1;
}

.skill-title {
  font-size: 34rpx;
  font-weight: 700;
  color: vars.$text-color;
  display: block;
  margin-bottom: 12rpx;
}

.skill-badges {
  display: flex;
  gap: 12rpx;
  flex-wrap: wrap;
}

.priority-tag {
  padding: 6rpx 16rpx;
  border-radius: 999rpx;
  
  &.high {
    background: rgba(255, 59, 48, 0.12);
    
    .tag-text {
      color: #FF3B30;
    }
  }
  
  &.medium {
    background: rgba(255, 149, 0, 0.12);
    
    .tag-text {
      color: #FF9500;
    }
  }
}

.required-tag {
  padding: 6rpx 16rpx;
  border-radius: 999rpx;
  background: rgba(255, 59, 48, 0.08);
  
  .tag-text {
    color: #FF3B30;
  }
}

.optional-tag {
  padding: 6rpx 16rpx;
  border-radius: 999rpx;
  background: rgba(0, 122, 255, 0.08);
  
  .tag-text {
    color: #007AFF;
  }
}

.tag-text {
  font-size: 22rpx;
  font-weight: 700;
}

.expand-btn {
  padding: 8rpx;
  cursor: pointer;
  user-select: none;
}

.expand-icon {
  font-size: 24rpx;
  color: vars.$text-muted;
  transition: transform 0.3s ease;
  display: inline-block;
  
  &.expanded {
    transform: rotate(180deg);
  }
}

.skill-brief {
  display: flex;
  align-items: flex-start;
  gap: 12rpx;
  padding: 20rpx;
  background: #F9F9F9;
  border-radius: 12rpx;
  margin-bottom: 16rpx;
}

.brief-icon {
  font-size: 28rpx;
  flex-shrink: 0;
}

.brief-text {
  font-size: 26rpx;
  color: vars.$text-color;
  line-height: 1.6;
  flex: 1;
}

.skill-details {
  padding-top: 16rpx;
  border-top: 1rpx solid #E5E5EA;
  animation: slideDown 0.3s ease-out;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-10rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.detail-section {
  margin-bottom: 24rpx;
  
  &:last-child {
    margin-bottom: 0;
  }
}

.section-label {
  font-size: 24rpx;
  color: vars.$text-muted;
  font-weight: 600;
  display: block;
  margin-bottom: 12rpx;
}

.section-content {
  font-size: 26rpx;
  color: vars.$text-color;
  line-height: 1.7;
  display: block;
}

.learning-tips {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.tip-item {
  display: flex;
  gap: 12rpx;
  padding: 16rpx;
  background: #F5F5F7;
  border-radius: 12rpx;
}

.tip-bullet {
  font-size: 24rpx;
  color: vars.$primary-color;
  font-weight: 700;
  flex-shrink: 0;
  min-width: 32rpx;
}

.tip-text {
  font-size: 26rpx;
  color: vars.$text-color;
  line-height: 1.6;
  flex: 1;
}

.fallback-message {
  font-size: 28rpx;
  color: vars.$text-muted;
  text-align: center;
}

.missing-skills-list {
  display: flex;
  flex-wrap: wrap;
  gap: vars.$spacing-sm;
  justify-content: center;
}

.missing-skill-tag {
  background: rgba(255, 59, 48, 0.1);
  border-radius: 999rpx;
  padding: 8rpx 16rpx;
}

.missing-skill-text {
  font-size: 24rpx;
  color: #FF3B30;
  font-weight: 500;
}

.interview-prep-card {
  background: vars.$surface-color;
  border-radius: vars.$border-radius;
  padding: vars.$spacing-lg;
  margin: vars.$spacing-md;
  margin-bottom: 64rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);
}

.interview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;

  &:active {
    opacity: 0.6;
  }
}

.interview-title {
  font-size: 32rpx;
  font-weight: 600;
  color: vars.$text-color;
}

.collapse-icon {
  font-size: 24rpx;
  color: vars.$text-muted;
  transition: transform 0.3s ease-in-out;

  &.expanded {
    transform: rotate(180deg);
  }
}

.interview-content-wrapper {
  margin-top: vars.$spacing-lg;
  padding-top: vars.$spacing-lg;
  border-top: 1rpx solid #E5E5EA;
  animation: expandContent 0.3s ease-in-out;
}

@keyframes expandContent {
  from {
    opacity: 0;
    max-height: 0;
  }
  to {
    opacity: 1;
    max-height: 5000rpx;
  }
}

.interview-initial {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: vars.$spacing-lg;
  padding: vars.$spacing-xl;
}

.initial-text {
  font-size: 26rpx;
  color: vars.$text-muted;
  text-align: center;
}

.initial-button {
  padding: vars.$spacing-md vars.$spacing-lg;
  background: vars.$primary-color;
  border-radius: 999rpx;
  cursor: pointer;

  &:active {
    opacity: 0.6;
    transform: scale(0.98);
  }

  .button-text {
    font-size: 28rpx;
    color: #FFFFFF;
    font-weight: 500;
  }
}

.interview-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: vars.$spacing-md;
  padding: vars.$spacing-xl;
}

.loading-dots {
  display: flex;
  gap: 8rpx;
}

.dot {
  width: 12rpx;
  height: 12rpx;
  background: vars.$primary-color;
  border-radius: 50%;
  animation: dot-bounce 1.4s infinite ease-in-out both;
  
  &:nth-child(1) {
    animation-delay: -0.32s;
  }
  
  &:nth-child(2) {
    animation-delay: -0.16s;
  }
}

@keyframes dot-bounce {
  0%, 80%, 100% {
    transform: scale(0);
    opacity: 0.5;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}

.loading-text {
  font-size: 24rpx;
  color: vars.$text-muted;
}

.interview-result {
  display: flex;
  flex-direction: column;
  gap: vars.$spacing-md;
}

.regenerate-button {
  margin-top: vars.$spacing-md;
  padding: vars.$spacing-sm vars.$spacing-lg;
  background: transparent;
  border: 2rpx solid vars.$primary-color;
  border-radius: 999rpx;
  cursor: pointer;
  align-self: flex-end;

  &:active {
    opacity: 0.6;
    transform: scale(0.98);
  }

  .regenerate-text {
    font-size: 28rpx;
    color: vars.$primary-color;
    font-weight: 500;
  }
}

.interview-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: vars.$spacing-md;
  padding: vars.$spacing-xl;
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
  color: #FF3B30;
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
    color: #FFFFFF;
    font-weight: 500;
  }
}
</style>