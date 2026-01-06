<template>
  <view class="candidate-card" @click="emit('click', seeker)">
    <view class="card-header">
      <view class="left">
        <view class="avatar-wrap">
          <image v-if="seeker.avatarUrl" class="avatar" :src="seeker.avatarUrl" mode="aspectFill" />
          <view v-else class="avatar-fallback">
            <text class="avatar-initial">{{ initial }}</text>
          </view>
        </view>

        <view class="title-wrap">
          <view class="name-row">
            <text class="name">{{ seeker.username || '求职者' }}</text>
            <view class="status-pill">
              <text class="status-text">{{ jobStatusText(seeker.jobStatus) }}</text>
            </view>
          </view>
          <text class="sub">{{ metaLine }}</text>
        </view>
      </view>

      <view class="right">
        <text class="age">{{ ageText(seeker.age) }}</text>
      </view>
    </view>

    <view class="tags">
      <view class="tag tag-exp">{{ experienceText(seeker.workExperienceYear) }}</view>
      <view class="tag tag-degree" v-if="seeker.highestEducation">{{ seeker.highestEducation }}</view>
      <view class="tag tag-intern" v-if="seeker.internshipExperience">有实习</view>
      <view class="tag tag-univ" v-if="seeker.university">{{ seeker.university }}</view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import type { PublicSeekerCard } from '@/services/api/hr';

interface Props {
  seeker: PublicSeekerCard;
}

const props = defineProps<Props>();

const emit = defineEmits<{
  click: [seeker: PublicSeekerCard];
}>();

const initial = computed(() => (props.seeker.username?.trim()?.[0] || 'S').toUpperCase());

const metaLine = computed(() => {
  const parts: string[] = [];
  if (props.seeker.city) parts.push(props.seeker.city);
  if (props.seeker.major) parts.push(props.seeker.major);
  if (props.seeker.graduationYear) parts.push(props.seeker.graduationYear);
  return parts.length ? parts.join(' · ') : '信息待完善';
});

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

.candidate-card {
  background-color: vars.$surface-color;
  border-radius: vars.$border-radius;
  padding: vars.$spacing-lg;
  margin-bottom: vars.$spacing-md;
  box-shadow: 0 12rpx 32rpx rgba(31, 55, 118, 0.08);
  width: 100%;
  box-sizing: border-box;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: vars.$spacing-md;
  margin-bottom: vars.$spacing-md;
}

.left {
  display: flex;
  gap: vars.$spacing-md;
  min-width: 0;
  flex: 1;
}

.avatar-wrap {
  width: 84rpx;
  height: 84rpx;
  border-radius: 50%;
  overflow: hidden;
  background: #eef2ff;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar {
  width: 84rpx;
  height: 84rpx;
  border-radius: 50%;
  display: block;
}

.avatar-fallback {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-initial {
  font-size: 34rpx;
  font-weight: 800;
  color: vars.$primary-color;
}

.title-wrap {
  flex: 1;
  min-width: 0;
}

.name-row {
  display: flex;
  align-items: center;
  gap: vars.$spacing-sm;
}

.name {
  font-size: 36rpx;
  font-weight: 800;
  color: vars.$text-color;
  max-width: 360rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.status-pill {
  padding: 6rpx 14rpx;
  border-radius: 999rpx;
  background: rgba(75, 163, 255, 0.14);
  flex-shrink: 0;
}

.status-text {
  font-size: 22rpx;
  color: vars.$primary-color;
  font-weight: 700;
}

.sub {
  display: block;
  margin-top: 10rpx;
  font-size: 24rpx;
  color: vars.$text-muted;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.right {
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.age {
  font-size: 30rpx;
  font-weight: 900;
  color: vars.$text-color;
  line-height: 1.1;
  margin-top: 6rpx;
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

.tag-degree {
  background-color: #f5f5f5;
  color: vars.$text-color;
}

.tag-intern {
  background-color: #fff3e0;
  color: #f57c00;
}

.tag-univ {
  background-color: rgba(75, 163, 255, 0.14);
  color: vars.$primary-color;
}
</style>
