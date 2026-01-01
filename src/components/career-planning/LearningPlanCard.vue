<template>
  <view 
    class="learning-plan-card" 
    :class="`priority-${getPriorityClass(skill.priority)}`"
  >
    <view class="card-header">
      <view class="header-left">
        <text class="skill-name">{{ skill.skill_name }}</text>
        <view class="priority-badge" :class="`priority-${getPriorityClass(skill.priority)}`">
          <text class="badge-text">{{ skill.priority }}</text>
        </view>
      </view>
      <view class="duration-badge" :class="`priority-${getPriorityClass(skill.priority)}`">
        <text class="duration-icon">⏱</text>
        <text class="duration-text">{{ skill.estimated_weeks }}{{ t('common.weeks') }}</text>
      </view>
    </view>

    <view class="reason-section">
      <text class="reason-icon">💡</text>
      <text class="reason-text">{{ skill.reason }}</text>
    </view>

    <view class="meta-row">
      <view class="difficulty-tag" :class="`difficulty-${getDifficultyClass(skill.difficulty)}`">
        <text class="difficulty-icon">{{ getDifficultyIcon(skill.difficulty) }}</text>
        <text class="difficulty-text">{{ skill.difficulty }}</text>
      </view>
      <text class="step-count">{{ skill.learning_steps.length }} {{ t('pages.jobs.careerPlanningPage.learningSteps') }}</text>
      <text class="resource-count">{{ skill.resources.length }} {{ t('pages.jobs.careerPlanningPage.resources') }}</text>
    </view>

    <view class="collapsible-section">
      <view class="section-header" @click="toggleSteps">
        <text class="section-icon">📚</text>
        <text class="section-title">{{ t('pages.jobs.careerPlanningPage.learningSteps') }}</text>
        <text class="arrow-icon" :class="{ expanded: stepsExpanded }">▼</text>
      </view>
      <view v-if="stepsExpanded" class="section-content">
        <view class="timeline-steps">
          <view 
            v-for="(step, index) in skill.learning_steps" 
            :key="index" 
            class="timeline-step"
          >
            <view class="step-marker">
              <view class="marker-dot" :class="{ first: index === 0, last: index === skill.learning_steps.length - 1 }"></view>
              <view v-if="index < skill.learning_steps.length - 1" class="marker-line"></view>
            </view>
            <view class="step-content">
              <text class="step-text">{{ step }}</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <view class="collapsible-section">
      <view class="section-header" @click="toggleResources">
        <text class="section-icon">🔗</text>
        <text class="section-title">{{ t('pages.jobs.careerPlanningPage.recommendedResources') }}</text>
        <text class="arrow-icon" :class="{ expanded: resourcesExpanded }">▼</text>
      </view>
      <view v-if="resourcesExpanded" class="section-content">
        <view class="resources-list">
          <view 
            v-for="(resource, index) in skill.resources" 
            :key="index" 
            class="resource-item"
            :class="{ clickable: !!resource.url }"
            @click="resource.url && openUrl(resource.url)"
          >
            <text class="resource-icon">{{ getResourceIcon(resource.type) }}</text>
            <view class="resource-info">
              <text class="resource-name" :class="{ 'has-link': !!resource.url }">
                {{ resource.name }}
              </text>
              <text class="resource-type">{{ resource.type }}</text>
            </view>
            <text v-if="resource.url" class="link-icon">→</text>
          </view>
        </view>
      </view>
    </view>

    <view class="action-buttons">
      <button class="action-button start-button" @click="handleStart">
        <text class="button-icon">🚀</text>
        <text class="button-text">{{ t('pages.jobs.careerPlanningPage.startLearning') }}</text>
      </button>
      <button class="action-button complete-button" @click="handleComplete">
        <text class="button-icon">✓</text>
        <text class="button-text">{{ t('pages.jobs.careerPlanningPage.markComplete') }}</text>
      </button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { t } from '@/locales';
import type { StructuredSkillLearning } from '@/services/api/recommendations';

interface Props {
  skill: StructuredSkillLearning;
  isFirst?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  isFirst: false
});

const emit = defineEmits<{
  start: [skill: StructuredSkillLearning];
  complete: [skill: StructuredSkillLearning];
}>();

const stepsExpanded = ref(false);
const resourcesExpanded = ref(false);

onMounted(() => {
  if (props.isFirst || props.skill.priority === '高') {
    stepsExpanded.value = true;
  }
});

function toggleSteps() {
  stepsExpanded.value = !stepsExpanded.value;
}

function toggleResources() {
  resourcesExpanded.value = !resourcesExpanded.value;
}

function getPriorityClass(priority: string): string {
  const map: Record<string, string> = {
    '高': 'high',
    '中': 'medium',
    '低': 'low',
  };
  return map[priority] || 'medium';
}

function getDifficultyClass(difficulty: string): string {
  const map: Record<string, string> = {
    '简单': 'easy',
    '中等': 'medium',
    '困难': 'hard',
  };
  return map[difficulty] || 'medium';
}

function getDifficultyIcon(difficulty: string): string {
  const map: Record<string, string> = {
    '简单': '⭐',
    '中等': '⭐⭐',
    '困难': '⭐⭐⭐',
  };
  return map[difficulty] || '⭐⭐';
}

function getResourceIcon(type: string): string {
  const map: Record<string, string> = {
    '文档': '📄',
    '视频': '🎬',
    '书籍': '📚',
    '教程': '📖',
    '课程': '🎓',
  };
  return map[type] || '🔗';
}

function openUrl(url: string) {
  // #ifdef H5
  window.open(url, '_blank');
  // #endif
  
  // #ifndef H5
  uni.navigateTo({
    url: `/pages/common/webview?url=${encodeURIComponent(url)}`
  });
  // #endif
}

function handleStart() {
  emit('start', props.skill);
}

function handleComplete() {
  emit('complete', props.skill);
}
</script>

<style lang="scss" scoped>
@use "@/styles/variables.scss" as vars;

.learning-plan-card {
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
  
  &.priority-low::before {
    background: linear-gradient(180deg, #007AFF 0%, #4BA3FF 100%);
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20rpx;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12rpx;
  flex: 1;
  flex-wrap: wrap;
}

.skill-name {
  font-size: 34rpx;
  font-weight: 700;
  color: vars.$text-color;
}

.priority-badge {
  padding: 6rpx 16rpx;
  border-radius: 999rpx;
  flex-shrink: 0;
  
  &.priority-high {
    background: rgba(255, 59, 48, 0.12);
  }
  
  &.priority-medium {
    background: rgba(255, 149, 0, 0.12);
  }
  
  &.priority-low {
    background: rgba(0, 122, 255, 0.12);
  }
}

.badge-text {
  font-size: 22rpx;
  font-weight: 700;
  
  .priority-high & {
    color: #FF3B30;
  }
  
  .priority-medium & {
    color: #FF9500;
  }
  
  .priority-low & {
    color: #007AFF;
  }
}

.duration-badge {
  display: flex;
  align-items: center;
  gap: 6rpx;
  padding: 8rpx 20rpx;
  border-radius: 999rpx;
  flex-shrink: 0;
  
  &.priority-high {
    background: rgba(255, 59, 48, 0.08);
  }
  
  &.priority-medium {
    background: rgba(255, 149, 0, 0.08);
  }
  
  &.priority-low {
    background: rgba(0, 122, 255, 0.08);
  }
}

.duration-icon {
  font-size: 24rpx;
}

.duration-text {
  font-size: 26rpx;
  font-weight: 600;
  
  .priority-high & {
    color: #FF3B30;
  }
  
  .priority-medium & {
    color: #FF9500;
  }
  
  .priority-low & {
    color: #007AFF;
  }
}

.reason-section {
  display: flex;
  align-items: flex-start;
  gap: 12rpx;
  padding: 20rpx;
  background: #F9F9F9;
  border-radius: 12rpx;
  margin-bottom: 20rpx;
}

.reason-icon {
  font-size: 28rpx;
  flex-shrink: 0;
}

.reason-text {
  font-size: 28rpx;
  color: vars.$text-color;
  line-height: 1.6;
  flex: 1;
}

.meta-row {
  display: flex;
  align-items: center;
  gap: 20rpx;
  margin-bottom: 24rpx;
  flex-wrap: wrap;
}

.difficulty-tag {
  display: flex;
  align-items: center;
  gap: 6rpx;
  padding: 6rpx 16rpx;
  border-radius: 999rpx;
  
  &.difficulty-easy {
    background: rgba(52, 199, 89, 0.1);
  }
  
  &.difficulty-medium {
    background: rgba(255, 149, 0, 0.1);
  }
  
  &.difficulty-hard {
    background: rgba(255, 59, 48, 0.1);
  }
}

.difficulty-icon {
  font-size: 20rpx;
}

.difficulty-text {
  font-size: 24rpx;
  font-weight: 600;
  
  .difficulty-easy & {
    color: #34C759;
  }
  
  .difficulty-medium & {
    color: #FF9500;
  }
  
  .difficulty-hard & {
    color: #FF3B30;
  }
}

.step-count,
.resource-count {
  font-size: 24rpx;
  color: vars.$text-muted;
}

.collapsible-section {
  margin-bottom: 20rpx;
  
  &:last-of-type {
    margin-bottom: 0;
  }
}

.section-header {
  display: flex;
  align-items: center;
  padding: 16rpx 20rpx;
  background: #F5F5F7;
  border-radius: 12rpx;
  cursor: pointer;
  user-select: none;
  transition: background 0.2s;
  
  &:active {
    background: #E8E8ED;
  }
}

.section-icon {
  font-size: 28rpx;
  margin-right: 12rpx;
}

.section-title {
  font-size: 28rpx;
  font-weight: 600;
  color: vars.$text-color;
  flex: 1;
}

.arrow-icon {
  font-size: 20rpx;
  color: vars.$text-muted;
  transition: transform 0.3s ease;
  
  &.expanded {
    transform: rotate(180deg);
  }
}

.section-content {
  padding: 20rpx 0 0 0;
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

.timeline-steps {
  padding-left: 20rpx;
}

.timeline-step {
  display: flex;
  gap: 24rpx;
  margin-bottom: 24rpx;
  
  &:last-child {
    margin-bottom: 0;
  }
}

.step-marker {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex-shrink: 0;
  width: 32rpx;
}

.marker-dot {
  width: 24rpx;
  height: 24rpx;
  border-radius: 50%;
  background: #E5E5EA;
  border: 4rpx solid vars.$surface-color;
  box-shadow: 0 0 0 2rpx #E5E5EA;
  flex-shrink: 0;
  
  &.first {
    background: vars.$primary-color;
    box-shadow: 0 0 0 2rpx vars.$primary-color;
  }
  
  &.last {
    background: #34C759;
    box-shadow: 0 0 0 2rpx #34C759;
  }
}

.marker-line {
  width: 3rpx;
  flex: 1;
  background: #E5E5EA;
  margin-top: 4rpx;
  min-height: 40rpx;
}

.step-content {
  flex: 1;
  padding-top: 2rpx;
}

.step-text {
  font-size: 26rpx;
  color: vars.$text-color;
  line-height: 1.6;
}

.resources-list {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.resource-item {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 20rpx;
  background: #F9F9F9;
  border-radius: 12rpx;
  transition: all 0.2s;
  
  &.clickable {
    cursor: pointer;
    
    &:active {
      background: #F0F0F0;
      transform: scale(0.98);
    }
  }
}

.resource-icon {
  font-size: 32rpx;
  flex-shrink: 0;
}

.resource-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.resource-name {
  font-size: 26rpx;
  color: vars.$text-color;
  line-height: 1.5;
  
  &.has-link {
    color: vars.$primary-color;
    font-weight: 500;
  }
}

.resource-type {
  font-size: 22rpx;
  color: vars.$text-muted;
}

.link-icon {
  font-size: 32rpx;
  color: vars.$text-muted;
  flex-shrink: 0;
}

.action-buttons {
  display: flex;
  gap: 16rpx;
  margin-top: 28rpx;
  padding-top: 28rpx;
  border-top: 1rpx solid #E5E5EA;
}

.action-button {
  flex: 1;
  height: 72rpx;
  border-radius: 999rpx;
  border: none;
  font-size: 28rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  cursor: pointer;
  transition: all 0.2s;
  
  &:active {
    transform: scale(0.96);
  }
}

.button-icon {
  font-size: 28rpx;
}

.button-text {
  font-weight: 600;
}

.start-button {
  background: linear-gradient(135deg, vars.$primary-color 0%, #4BA3FF 100%);
  box-shadow: 0 4rpx 12rpx rgba(75, 163, 255, 0.3);
  
  .button-icon,
  .button-text {
    color: #FFFFFF;
  }
}

.complete-button {
  background: transparent;
  border: 2rpx solid #E5E5EA;
  
  .button-icon,
  .button-text {
    color: vars.$text-muted;
  }
}
</style>
