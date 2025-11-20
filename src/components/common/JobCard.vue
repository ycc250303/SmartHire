<template>
  <view class="job-card">
    <view class="card-header">
      <view class="job-title">{{ job.title }}</view>
      <view class="match-score" v-if="job.matchScore !== undefined && job.matchScore !== null">
        <text class="score-value">{{ job.matchScore }}%</text>
        <text class="score-label">{{ t('pages.jobs.matchScore') }}</text>
      </view>
    </view>
    
    <view class="job-meta">
      <text class="meta-text">
        {{ job.companyName }} · {{ job.city }} · 
        <text class="salary-text">{{ formatSalary(job.salary_min, job.salary_max) }}</text>
      </text>
    </view>
    
    <view class="job-tags">
      <view class="tag tag-degree">{{ getDegreeText(job.degrees) }}</view>
      <view class="tag tag-duration">{{ formatDuration(job.work_months_min) }}</view>
      <view class="tag tag-skill" v-for="skill in job.skills" :key="skill">{{ skill }}</view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { t } from '@/locales';
import type { InternJobItem } from '@/services/api/recommendations';
import { Degree } from '@/services/api/recommendations';

interface Props {
  job: InternJobItem;
}

const props = defineProps<Props>();

function formatSalary(min: number, max: number): string {
  return `${min}-${max}K`;
}

function formatDuration(months: number): string {
  return `${months}${t('pages.jobs.months')}`;
}

function getDegreeText(degree: Degree): string {
  const degreeMap: Record<Degree, string> = {
    [Degree.HighSchoolOrBelow]: t('degree.highSchoolOrBelow'),
    [Degree.AssociateDegree]: t('degree.associateDegree'),
    [Degree.BachelorDegree]: t('degree.bachelorDegree'),
    [Degree.MasterDegree]: t('degree.masterDegree'),
    [Degree.DoctorDegree]: t('degree.doctorDegree'),
  };
  return degreeMap[degree] || '';
}
</script>

<style lang="scss" scoped>
@use "@/styles/variables.scss" as vars;

.job-card {
  background-color: vars.$surface-color;
  border-radius: vars.$border-radius;
  padding: vars.$spacing-lg;
  margin-bottom: vars.$spacing-md;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: vars.$spacing-sm;
}

.job-title {
  font-size: 36rpx;
  font-weight: 600;
  color: vars.$text-color;
  flex: 1;
  margin-right: vars.$spacing-md;
}

.match-score {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  flex-shrink: 0;
}

.score-value {
  font-size: 48rpx;
  font-weight: 600;
  color: vars.$primary-color;
  line-height: 1;
}

.score-label {
  font-size: 20rpx;
  color: vars.$text-muted;
  margin-top: 4rpx;
}

.job-meta {
  margin-bottom: vars.$spacing-md;
}

.meta-text {
  font-size: 24rpx;
  color: vars.$text-muted;
  line-height: 1.5;
}

.salary-text {
  font-weight: 600;
  color: vars.$text-color;
}

.job-tags {
  display: flex;
  flex-wrap: wrap;
  gap: vars.$spacing-sm;
}

.tag {
  padding: 8rpx 16rpx;
  border-radius: vars.$border-radius-sm;
  font-size: 24rpx;
}

.tag-degree {
  background-color: #e3f2fd;
  color: #1976d2;
}

.tag-duration {
  background-color: #fff3e0;
  color: #f57c00;
}

.tag-skill {
  background-color: #f5f5f5;
  color: vars.$text-color;
}
</style>

