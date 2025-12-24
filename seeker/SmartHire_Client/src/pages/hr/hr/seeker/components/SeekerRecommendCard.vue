<template>
  <view class="seeker-card" @click="emit('click', seeker)">
    <view class="card-header">
      <view class="name-wrap">
        <text class="name">{{ seeker.username || '求职者' }}</text>
        <view class="status-pill">{{ jobStatusText(seeker.jobStatus) }}</view>
      </view>
      <view class="right-meta">
        <text class="age">{{ ageText(seeker.age) }}</text>
        <text class="city">{{ seeker.city || '城市未知' }}</text>
      </view>
    </view>

    <view class="meta-line">
      <text class="meta">{{ seeker.highestEducation || '学历未知' }}</text>
      <text class="dot">·</text>
      <text class="meta">{{ seeker.major || '专业未知' }}</text>
      <text class="dot">·</text>
      <text class="meta">{{ seeker.graduationYear || '毕业时间未知' }}</text>
    </view>

    <view class="tags">
      <view class="tag tag-exp">{{ experienceText(seeker.workExperienceYear) }}</view>
      <view class="tag tag-intern" v-if="seeker.internshipExperience">有实习</view>
      <view class="tag tag-univ" v-if="seeker.university">{{ seeker.university }}</view>
    </view>
  </view>
</template>

<script setup lang="ts">
import type { PublicSeekerCard } from '@/services/api/hr';

interface Props {
  seeker: PublicSeekerCard;
}

defineProps<Props>();

const emit = defineEmits<{
  click: [seeker: PublicSeekerCard];
}>();

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
  if (year === undefined || year === null) return '经验未知';
  if (year <= 0) return '应届/0年';
  return `${year}年经验`;
};

const ageText = (age?: number) => {
  if (age === undefined || age === null) return '--';
  return `${age}岁`;
};
</script>

<style lang="scss" scoped>
@use "@/styles/variables.scss" as vars;

.seeker-card {
  background-color: vars.$surface-color;
  border-radius: vars.$border-radius;
  padding: vars.$spacing-lg;
  margin-bottom: vars.$spacing-md;
  box-shadow: 0 12rpx 32rpx rgba(31, 55, 118, 0.08);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: vars.$spacing-sm;
  gap: vars.$spacing-md;
}

.name-wrap {
  display: flex;
  align-items: center;
  gap: vars.$spacing-sm;
  min-width: 0;
}

.name {
  font-size: 36rpx;
  font-weight: 700;
  color: vars.$text-color;
  max-width: 360rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.status-pill {
  font-size: 22rpx;
  padding: 6rpx 14rpx;
  border-radius: 999rpx;
  background: rgba(75, 163, 255, 0.14);
  color: vars.$primary-color;
  flex-shrink: 0;
}

.right-meta {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 6rpx;
  flex-shrink: 0;
}

.age {
  font-size: 26rpx;
  font-weight: 600;
  color: vars.$text-color;
  line-height: 1.1;
}

.city {
  font-size: 22rpx;
  color: vars.$text-muted;
}

.meta-line {
  font-size: 24rpx;
  color: vars.$text-muted;
  line-height: 1.5;
  margin-bottom: vars.$spacing-md;
}

.dot {
  margin: 0 10rpx;
  opacity: 0.6;
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: vars.$spacing-sm;
}

.tag {
  padding: 8rpx 16rpx;
  border-radius: vars.$border-radius-sm;
  font-size: 24rpx;
}

.tag-exp {
  background-color: #e3f2fd;
  color: #1976d2;
}

.tag-intern {
  background-color: #fff3e0;
  color: #f57c00;
}

.tag-univ {
  background-color: #f5f5f5;
  color: vars.$text-color;
}
</style>

