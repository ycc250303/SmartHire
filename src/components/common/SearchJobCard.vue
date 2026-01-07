<template>
  <view class="search-job-card" @click="handleCardClick">
    <view class="card-header">
      <text class="job-title">{{ job.jobTitle }}</text>
    </view>
    
    <view class="job-meta">
      <text class="meta-text">
        {{ job.companyName || '' }} · {{ job.city || '' }}
      </text>
      <view class="tag tag-salary" v-if="job.salaryMin && job.salaryMax">
        {{ formatSalary(job.salaryMin, job.salaryMax) }}
      </view>
    </view>
    
    <view class="job-tags">
      <view class="tag tag-type" v-if="job.jobType !== undefined && job.jobType !== null">
        {{ getJobTypeText(job.jobType) }}
      </view>
      <view class="tag tag-degree" v-if="job.educationRequired !== undefined && job.educationRequired !== null">
        {{ getDegreeText(job.educationRequired) }}
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { t } from '@/locales';
import type { JobSearchResult } from '@/services/api/recruitment';

interface Props {
  job: JobSearchResult;
}

const props = defineProps<Props>();

function formatSalary(min: number, max: number): string {
  const minK = Math.round(min / 1000);
  const maxK = Math.round(max / 1000);
  return `${minK}-${maxK}K`;
}

function getJobTypeText(jobType: number): string {
  return jobType === 0 ? t('pages.jobs.fulltime') : t('pages.jobs.internship');
}

function getDegreeText(degree: number): string {
  const degreeMap: Record<number, string> = {
    0: t('degree.highSchoolOrBelow'),
    1: t('degree.associateDegree'),
    2: t('degree.bachelorDegree'),
    3: t('degree.masterDegree'),
    4: t('degree.doctorDegree'),
  };
  return degreeMap[degree] || '';
}

function handleCardClick() {
  uni.navigateTo({
    url: `/pages/seeker/jobs/detail?jobId=${props.job.jobId}`
  });
}
</script>

<style lang="scss" scoped>
@use "@/styles/variables.scss" as vars;

.search-job-card {
  background-color: vars.$surface-color;
  border-radius: vars.$border-radius;
  padding: vars.$spacing-lg;
  margin-bottom: vars.$spacing-md;
  cursor: pointer;
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
}

.job-meta {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: vars.$spacing-xs;
  margin-bottom: vars.$spacing-md;
}

.meta-text {
  font-size: 24rpx;
  color: vars.$text-muted;
  line-height: 1.5;
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

.tag-salary {
  background-color: #fff3e0;
  color: #f57c00;
}

.tag-type {
  background-color: #e8f5e9;
  color: #388e3c;
}

.tag-degree {
  background-color: #e3f2fd;
  color: #1976d2;
}
</style>

