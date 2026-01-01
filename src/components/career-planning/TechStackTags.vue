<template>
  <view class="tech-stack-section">
    <text class="section-title">{{ title }}</text>
    <view class="tech-stack-list">
      <view 
        v-for="(stack, index) in normalizedStacks" 
        :key="index"
        class="tech-stack-item"
      >
        <view class="stack-header">
          <view 
            class="category-indicator" 
            :style="{ backgroundColor: getCategoryColor(index) }"
          ></view>
          <text class="category-name">{{ stack.category }}</text>
        </view>
        <view class="skills-tags" v-if="stack.skills && stack.skills.length > 0">
          <view 
            v-for="(skill, skillIndex) in stack.skills" 
            :key="skillIndex"
            class="skill-tag"
          >
            <text class="skill-tag-text">{{ skill }}</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { t } from '@/locales';

interface TechnologyStack {
  category: string;
  skills: string[] | string;
}

interface Props {
  technologyStacks: TechnologyStack[];
  title?: string;
}

const props = withDefaults(defineProps<Props>(), {
  title: () => t('pages.jobs.careerPlanningPage.technologyStacks'),
});

const categoryColors = ['#4ba3ff', '#8E44AD', '#34C759', '#FF9500'];

function getCategoryColor(index: number): string {
  return categoryColors[index % categoryColors.length];
}

// 处理 skills 数据，确保是数组格式
function normalizeSkills(skills: string[] | string): string[] {
  if (Array.isArray(skills)) {
    return skills.filter(skill => skill && skill.trim().length > 0);
  }
  if (typeof skills === 'string') {
    // 如果是字符串，尝试按逗号、分号或空格分割
    return skills
      .split(/[,，;；\s]+/)
      .map(s => s.trim())
      .filter(s => s.length > 0);
  }
  return [];
}

// 计算处理后的技术栈数据
const normalizedStacks = computed(() => {
  return props.technologyStacks.map(stack => ({
    ...stack,
    skills: normalizeSkills(stack.skills),
  }));
});
</script>

<style lang="scss" scoped>
@use "@/styles/variables.scss" as vars;

.tech-stack-section {
  background: vars.$surface-color;
  border-radius: 32rpx;
  padding: vars.$spacing-lg;
  margin-bottom: vars.$spacing-md;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);
}

.section-title {
  font-size: 34rpx;
  font-weight: 600;
  color: vars.$text-color;
  display: block;
  margin-bottom: vars.$spacing-lg;
}

.tech-stack-list {
  display: flex;
  flex-direction: column;
  gap: vars.$spacing-lg;
}

.tech-stack-item {
  display: flex;
  flex-direction: column;
  gap: vars.$spacing-sm;
  
  &:not(:last-child) {
    margin-bottom: vars.$spacing-md;
  }
}

.stack-header {
  display: flex;
  align-items: center;
  gap: vars.$spacing-sm;
}

.category-indicator {
  width: 4rpx;
  height: 100%;
  border-radius: 2rpx;
  flex-shrink: 0;
}

.category-name {
  font-size: 28rpx;
  color: vars.$text-muted;
  font-weight: 500;
  margin-left: 12rpx;
}

.skills-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  padding-left: 16rpx;
}

.skill-tag {
  padding: 10rpx 20rpx;
  border-radius: 999rpx;
  background-color: rgba(75, 163, 255, 0.1);
}

.skill-tag-text {
  font-size: 24rpx;
  color: #4ba3ff;
}
</style>

