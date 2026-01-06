<template>
  <view class="job-detail-page">
    <view v-if="loading" class="loading-container">
      <text class="loading-text">{{ t('pages.jobs.jobDetail.loading') }}</text>
    </view>

    <view v-else-if="error" class="error-container">
      <text class="error-text">{{ error }}</text>
      <button class="retry-button" @click="loadJobDetail">
        {{ t('pages.jobs.jobDetail.retry') }}
      </button>
    </view>

    <scroll-view v-else-if="job" class="content-scroll" scroll-y>
      <view class="job-header">
        <view class="nav-back-button" @click="handleBack">
          <text class="back-icon">‹</text>
        </view>
        <view class="nav-favorite-button" @click="toggleFavorite" v-if="!loading">
          <text class="favorite-icon" :class="{ 'favorited': isFavorite }">{{ isFavorite ? '♥' : '♡' }}</text>
        </view>
        <view class="job-title-section">
          <text class="job-title">{{ job.jobTitle }}</text>
          <view class="job-meta-row">
            <text class="job-salary">{{ formatSalary() }}</text>
            <text class="job-separator">·</text>
            <text class="job-location">{{ job.city }}</text>
            <text class="job-separator">·</text>
            <text class="job-type">{{ getJobTypeText() }}</text>
          </view>
        </view>
      </view>

      <view class="job-info-section">
        <view class="info-item" v-if="job.department">
          <text class="info-label">{{ t('pages.jobs.jobDetail.department') }}</text>
          <text class="info-value">{{ job.department }}</text>
        </view>
        <view class="info-item" v-if="job.address">
          <text class="info-label">{{ t('pages.jobs.jobDetail.address') }}</text>
          <text class="info-value">{{ job.address }}</text>
        </view>
        <view class="info-item" v-if="job.experienceRequired !== undefined">
          <text class="info-label">{{ t('pages.jobs.jobDetail.experience') }}</text>
          <text class="info-value">{{ formatExperience() }}</text>
        </view>
        <view class="info-item" v-if="job.educationRequired !== undefined">
          <text class="info-label">{{ t('pages.jobs.jobDetail.education') }}</text>
          <text class="info-value">{{ getEducationText() }}</text>
        </view>
      </view>

      <view class="job-description-section" v-if="job.description">
        <text class="section-title">{{ t('pages.jobs.jobDetail.description') }}</text>
        <text class="section-content">{{ job.description }}</text>
      </view>

      <view class="job-responsibilities-section" v-if="job.responsibilities">
        <text class="section-title">{{ t('pages.jobs.jobDetail.responsibilities') }}</text>
        <text class="section-content">{{ job.responsibilities }}</text>
      </view>

      <view class="job-requirements-section" v-if="job.requirements">
        <text class="section-title">{{ t('pages.jobs.jobDetail.requirements') }}</text>
        <text class="section-content">{{ job.requirements }}</text>
      </view>

      <view class="job-skills-section" v-if="job.skills && job.skills.length > 0">
        <text class="section-title">{{ t('pages.jobs.jobDetail.skills') }}</text>
        <view class="skills-tags">
          <view v-for="skill in job.skills" :key="skill" class="skill-tag">
            <text class="skill-text">{{ skill }}</text>
          </view>
        </view>
      </view>

      <view class="company-section" v-if="job.company">
        <text class="section-title">{{ t('pages.jobs.jobDetail.company') }}</text>
        <view class="company-card" @click="goToCompanyDetail">
          <image v-if="job.company.companyLogo" :src="job.company.companyLogo" class="company-logo" mode="aspectFit" />
          <view class="company-info">
            <text class="company-name">{{ job.company.companyName }}</text>
            <view class="company-meta" v-if="job.company.industry">
              <text class="company-meta-text">{{ job.company.industry }}</text>
            </view>
            <view class="company-meta" v-if="job.company.description">
              <text class="company-desc">{{ job.company.description }}</text>
            </view>
          </view>
        </view>
      </view>

      <view class="hr-section" v-if="job.hr">
        <text class="section-title">{{ t('pages.jobs.jobDetail.hr') }}</text>
        <view class="hr-card">
          <image v-if="job.hr.avatarUrl" :src="job.hr.avatarUrl" class="hr-avatar" mode="aspectFill" />
          <view class="hr-info">
            <text class="hr-name">{{ job.hr.realName }}</text>
            <text class="hr-position" v-if="job.hr.position">{{ job.hr.position }}</text>
          </view>
        </view>
      </view>

      <view class="career-planning-section" v-if="job">
        <text class="section-title">{{ t('pages.jobs.jobDetail.careerPlanning') }}</text>
        <CareerPlanningCard :jobId="job.jobId" />
      </view>

    </scroll-view>

    <view v-if="job && !loading && !error" class="bottom-actions">
      <button 
        class="action-button apply-button" 
        :class="{ 
          'applying': applying, 
          'applied': job?.application?.hasApplied && !canContactHr(),
          'contact-hr': canContactHr()
        }"
        :disabled="applying || (job?.application?.hasApplied && !canContactHr())"
        @click="handleApply"
      >
        <text class="button-text">{{ getApplyButtonText() }}</text>
      </button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { t } from '@/locales';
import { getJobDetail, submitResume, type JobDetail } from '@/services/api/recruitment';
import { getResumes, type Resume } from '@/services/api/resume';
import { favoriteJob, unfavoriteJob, getFavoriteJobs } from '@/services/api/seeker';
import CareerPlanningCard from '@/components/common/CareerPlanningCard.vue';

const job = ref<JobDetail | null>(null);
const loading = ref(false);
const error = ref<string | null>(null);
const applying = ref(false);
const jobId = ref<number | null>(null);
const isFavorite = ref(false);
const favoriting = ref(false);

onLoad((options: any) => {
  if (options?.jobId) {
    jobId.value = parseInt(options.jobId);
    loadJobDetail();
  } else {
    error.value = t('pages.jobs.jobDetail.invalidJobId');
  }
});

function handleBack() {
  uni.navigateBack();
}

async function loadJobDetail() {
  if (!jobId.value) return;

  loading.value = true;
  error.value = null;

  try {
    job.value = await getJobDetail(jobId.value);
    await checkFavoriteStatus();
  } catch (err) {
    error.value = err instanceof Error ? err.message : t('pages.jobs.jobDetail.loadError');
    console.error('Failed to load job detail:', err);
  } finally {
    loading.value = false;
  }
}

async function checkFavoriteStatus() {
  if (!jobId.value) return;
  
  try {
    const favorites = await getFavoriteJobs();
    isFavorite.value = favorites.some(fav => fav.jobId === jobId.value);
  } catch (err) {
    console.error('Failed to check favorite status:', err);
  }
}

async function toggleFavorite() {
  if (!jobId.value || favoriting.value) return;

  const previousState = isFavorite.value;
  isFavorite.value = !isFavorite.value;
  favoriting.value = true;

  try {
    if (previousState) {
      await unfavoriteJob(jobId.value);
      uni.showToast({
        title: t('pages.jobs.favorites.unfavoriteSuccess'),
        icon: 'success',
      });
    } else {
      await favoriteJob(jobId.value);
      uni.showToast({
        title: t('pages.jobs.favorites.favoriteSuccess'),
        icon: 'success',
      });
    }
  } catch (err) {
    isFavorite.value = previousState;
    const errorMessage = err instanceof Error ? err.message : '';
    if (previousState) {
      uni.showToast({
        title: t('pages.jobs.favorites.unfavoriteError'),
        icon: 'none',
      });
    } else {
      if (errorMessage.includes('已收藏') || errorMessage.includes('already')) {
        isFavorite.value = true;
        uni.showToast({
          title: t('pages.jobs.favorites.favoriteSuccess'),
          icon: 'success',
        });
      } else {
        uni.showToast({
          title: t('pages.jobs.favorites.favoriteError'),
          icon: 'none',
        });
      }
    }
  } finally {
    favoriting.value = false;
  }
}

function formatSalary(): string {
  if (!job.value) return '';
  if (job.value.salaryMin && job.value.salaryMax) {
    const minK = Math.round(job.value.salaryMin / 1000);
    const maxK = Math.round(job.value.salaryMax / 1000);
    const months = job.value.salaryMonths || 12;
    const unit = months === 12 ? 'k' : `${months}薪`;
    return `${minK}-${maxK}${unit}`;
  }
  return t('pages.jobs.jobDetail.salaryNegotiable');
}

function getJobTypeText(): string {
  if (!job.value) return '';
  const typeMap: Record<number, string> = {
    0: t('pages.jobs.fulltime'),
    1: t('pages.jobs.internship'),
    2: t('pages.jobs.parttime'),
  };
  return typeMap[job.value.jobType] || '';
}

function formatExperience(): string {
  if (!job.value || job.value.experienceRequired === undefined) return '';
  const expMap: Record<number, string> = {
    0: t('pages.jobs.jobDetail.experienceNone'),
    1: '1-3年',
    2: '3-5年',
    3: '5-10年',
    4: '10年以上',
  };
  return expMap[job.value.experienceRequired] || '';
}

function getEducationText(): string {
  if (!job.value || job.value.educationRequired === undefined) return '';
  const eduMap: Record<number, string> = {
    0: t('pages.jobs.jobDetail.educationNone'),
    1: t('degree.associateDegree'),
    2: t('degree.bachelorDegree'),
    3: t('degree.masterDegree'),
    4: t('degree.doctorDegree'),
  };
  return eduMap[job.value.educationRequired] || '';
}

function getApplyButtonText(): string {
  if (applying.value) {
    return t('pages.jobs.jobDetail.applying');
  }
  if (job.value?.application?.hasApplied) {
    if (job.value.application.conversationId) {
      return t('pages.jobs.jobDetail.contactHr');
    }
    return t('pages.jobs.jobDetail.applied');
  }
  return t('pages.jobs.jobDetail.apply');
}

function canApply(): boolean {
  return !job.value?.application?.hasApplied && !applying.value;
}

function canContactHr(): boolean {
  return job.value?.application?.hasApplied === true && 
         job.value.application.conversationId !== undefined &&
         !applying.value;
}

async function handleApply() {
  if (!jobId.value || applying.value) return;

  if (canContactHr()) {
    const jobData = job.value;
    if (!jobData || !jobData.application) return;
    
    const queryParams: string[] = [];
    queryParams.push(`id=${jobData.application.conversationId}`);
    
    if (jobData.application.applicationId) {
      queryParams.push(`applicationId=${jobData.application.applicationId}`);
    }
    if (jobData.hr?.hrId) {
      queryParams.push(`userId=${jobData.hr.hrId}`);
    }
    if (jobData.hr?.realName) {
      queryParams.push(`username=${encodeURIComponent(jobData.hr.realName)}`);
    }
    if (jobData.jobTitle) {
      queryParams.push(`jobTitle=${encodeURIComponent(jobData.jobTitle)}`);
    }
    if (jobData.city) {
      queryParams.push(`city=${encodeURIComponent(jobData.city)}`);
    }
    if (jobData.company?.companyName) {
      queryParams.push(`companyName=${encodeURIComponent(jobData.company.companyName)}`);
    }
    if (jobData.salaryMin && jobData.salaryMax) {
      const minK = Math.round(jobData.salaryMin / 1000);
      const maxK = Math.round(jobData.salaryMax / 1000);
      queryParams.push(`salary=${encodeURIComponent(`${minK}-${maxK}K`)}`);
    }
    if (jobData.jobType !== undefined) {
      queryParams.push(`jobType=${jobData.jobType}`);
    }
    
    const url = `/pages/seeker/chat/conversation?${queryParams.join('&')}`;
    console.log('Navigating to conversation:', url);
    uni.navigateTo({
      url: url
    });
    return;
  }

  if (!canApply()) return;

  try {
    const resumes = await getResumes();
    let resumeId: number;

    if (resumes.length > 0 && resumes[0]) {
      resumeId = resumes[0].id;
    } else {
      uni.showModal({
        title: t('pages.jobs.jobDetail.noResumeTitle'),
        content: t('pages.jobs.jobDetail.noResumeContent'),
        confirmText: t('common.confirm'),
        cancelText: t('common.cancel'),
        success: (res) => {
          if (res.confirm) {
            uni.navigateTo({
              url: '/pages/seeker/profile/resume/attachment-resume'
            });
          }
        }
      });
      return;
    }

    applying.value = true;

    const result = await submitResume({
      jobId: jobId.value,
      resumeId: resumeId,
    });

    await loadJobDetail();

    uni.showToast({
      title: t('pages.jobs.jobDetail.applySuccess'),
      icon: 'success',
    });

    setTimeout(() => {
      const jobData = job.value;
      if (!jobData) return;
      
      const queryParams: string[] = [];
      queryParams.push(`id=${result.conversationId}`);
      queryParams.push(`applicationId=${result.applicationId}`);
      
      if (jobData.hr?.hrId) {
        queryParams.push(`userId=${jobData.hr.hrId}`);
      }
      if (jobData.hr?.realName) {
        queryParams.push(`username=${encodeURIComponent(jobData.hr.realName)}`);
      }
      if (jobData.jobTitle) {
        queryParams.push(`jobTitle=${encodeURIComponent(jobData.jobTitle)}`);
      }
      if (jobData.city) {
        queryParams.push(`city=${encodeURIComponent(jobData.city)}`);
      }
      if (jobData.company?.companyName) {
        queryParams.push(`companyName=${encodeURIComponent(jobData.company.companyName)}`);
      }
      if (jobData.salaryMin && jobData.salaryMax) {
        const minK = Math.round(jobData.salaryMin / 1000);
        const maxK = Math.round(jobData.salaryMax / 1000);
        queryParams.push(`salary=${encodeURIComponent(`${minK}-${maxK}K`)}`);
      }
      if (jobData.jobType !== undefined) {
        queryParams.push(`jobType=${jobData.jobType}`);
      }
      
      const url = `/pages/seeker/chat/conversation?${queryParams.join('&')}`;
      console.log('Navigating to conversation:', url);
      uni.navigateTo({
        url: url
      });
    }, 1500);

  } catch (err) {
    console.error('Failed to apply:', err);
    const errorMessage = err instanceof Error ? err.message : t('pages.jobs.jobDetail.applyError');
    
    if (errorMessage.includes('已投递') || errorMessage.includes('already')) {
      uni.showModal({
        title: t('pages.jobs.jobDetail.alreadyAppliedTitle'),
        content: t('pages.jobs.jobDetail.alreadyAppliedContent'),
        showCancel: false,
        confirmText: t('common.confirm'),
      });
      loadJobDetail();
    } else {
      uni.showToast({
        title: errorMessage,
        icon: 'none',
        duration: 2000,
      });
    }
  } finally {
    applying.value = false;
  }
}

function goToCompanyDetail() {
  if (!job.value?.company || !jobId.value) return;

  const url = `/pages/seeker/jobs/company-detail?jobId=${jobId.value}`;
  uni.navigateTo({
    url,
  });
}
</script>

<style lang="scss" scoped>
@use "@/styles/variables.scss" as vars;

.job-detail-page {
  min-height: 100vh;
  background: vars.$surface-color;
  display: flex;
  flex-direction: column;
  padding-bottom: 120rpx;
}

.loading-container,
.error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 400rpx;
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
}

.content-scroll {
  flex: 1;
  height: calc(100vh - 120rpx);
}

.job-header {
  background: linear-gradient(to bottom, vars.$light-blue 0%, vars.$surface-color 100%);
  padding: vars.$spacing-xl;
  padding-top: calc(var(--status-bar-height) + vars.$spacing-lg);
  padding-bottom: vars.$spacing-xl;
  min-height: 400rpx;
  position: relative;
}

.nav-back-button,
.nav-favorite-button {
  position: absolute;
  top: calc(var(--status-bar-height) + vars.$spacing-md);
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
  cursor: pointer;
}

.nav-back-button {
  left: vars.$spacing-xl;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 50%;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
}

.nav-favorite-button {
  right: vars.$spacing-xl;
  background: transparent;
  padding: vars.$spacing-sm;
}

.back-icon {
  font-size: 48rpx;
  color: vars.$text-color;
  font-weight: 300;
  line-height: 1;
  margin-left: -4rpx;
}

.favorite-icon {
  font-size: 48rpx;
  color: rgba(255, 255, 255, 0.85);
  line-height: 1;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  text-shadow: 0 2rpx 4rpx rgba(0, 0, 0, 0.15);
  display: inline-block;
  transform: scale(1);
}

.favorite-icon.favorited {
  color: #ff6b6b;
  text-shadow: 0 2rpx 8rpx rgba(255, 107, 107, 0.4);
  transform: scale(1.1);
}

.nav-favorite-button:active .favorite-icon {
  transform: scale(0.95);
}

.nav-favorite-button:active .favorite-icon.favorited {
  transform: scale(1.05);
}

.job-title-section {
  display: flex;
  flex-direction: column;
  gap: vars.$spacing-md;
  margin-top: 120rpx;
}

.job-title {
  font-size: 44rpx;
  font-weight: 700;
  color: vars.$text-color;
  line-height: 1.4;
}

.job-meta-row {
  display: flex;
  align-items: center;
  gap: vars.$spacing-sm;
  font-size: 28rpx;
  color: vars.$text-muted;
}

.job-salary {
  color: #ff6b6b;
  font-weight: 600;
}

.job-separator {
  color: vars.$text-muted;
}

.job-info-section {
  background: vars.$surface-color;
  padding: vars.$spacing-xl;
  margin-top: vars.$spacing-md;
  border-radius: vars.$border-radius;
  display: flex;
  flex-direction: column;
  gap: vars.$spacing-md;
}

.info-item {
  display: flex;
  align-items: flex-start;
  gap: vars.$spacing-md;
}

.info-label {
  font-size: 28rpx;
  color: vars.$text-muted;
  min-width: 120rpx;
}

.info-value {
  font-size: 28rpx;
  color: vars.$text-color;
  flex: 1;
}

.job-description-section,
.job-responsibilities-section,
.job-requirements-section {
  background: vars.$surface-color;
  padding: vars.$spacing-xl;
  margin-top: vars.$spacing-md;
  border-radius: vars.$border-radius;
}

.section-title {
  font-size: 32rpx;
  font-weight: 600;
  color: vars.$text-color;
  display: block;
  margin-bottom: vars.$spacing-md;
}

.section-content {
  font-size: 28rpx;
  color: vars.$text-color;
  line-height: 1.8;
  white-space: pre-wrap;
  display: block;
}

.job-skills-section {
  background: vars.$surface-color;
  padding: vars.$spacing-xl;
  margin-top: vars.$spacing-md;
  border-radius: vars.$border-radius;
}

.skills-tags {
  display: flex;
  flex-wrap: wrap;
  gap: vars.$spacing-sm;
  margin-top: vars.$spacing-md;
}

.skill-tag {
  padding: vars.$spacing-xs vars.$spacing-md;
  background: vars.$primary-color-soft;
  border-radius: vars.$border-radius-sm;
}

.skill-text {
  font-size: 24rpx;
  color: vars.$primary-color;
}

.company-section,
.hr-section,
.career-planning-section {
  background: vars.$surface-color;
  padding: vars.$spacing-xl;
  margin-top: vars.$spacing-md;
  border-radius: vars.$border-radius;
}

.company-card {
  display: flex;
  gap: vars.$spacing-md;
  align-items: flex-start;
  margin-top: vars.$spacing-md;
}

.company-logo {
  width: 120rpx;
  height: 120rpx;
  border-radius: vars.$border-radius-sm;
  background: vars.$bg-color;
}

.company-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: vars.$spacing-xs;
}

.company-name {
  font-size: 32rpx;
  font-weight: 600;
  color: vars.$text-color;
}

.company-meta {
  display: flex;
  align-items: center;
  gap: vars.$spacing-sm;
}

.company-meta-text {
  font-size: 26rpx;
  color: vars.$text-muted;
}

.company-desc {
  font-size: 26rpx;
  color: vars.$text-muted;
  line-height: 1.6;
}

.hr-card {
  display: flex;
  gap: vars.$spacing-md;
  align-items: center;
  margin-top: vars.$spacing-md;
  padding: vars.$spacing-md;
  background: vars.$bg-color;
  border-radius: vars.$border-radius-sm;
}

.hr-avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  background: vars.$surface-color;
}

.hr-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: vars.$spacing-xs;
}

.hr-name {
  font-size: 28rpx;
  font-weight: 600;
  color: vars.$text-color;
}

.hr-position {
  font-size: 24rpx;
  color: vars.$text-muted;
}

.bottom-actions {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: vars.$surface-color;
  padding: vars.$spacing-lg vars.$spacing-xl;
  padding-bottom: calc(vars.$spacing-lg + env(safe-area-inset-bottom));
  box-shadow: 0 -2rpx 8rpx rgba(0, 0, 0, 0.1);
  z-index: 1000;
}

.action-button {
  width: 100%;
  height: 88rpx;
  border-radius: vars.$border-radius;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  font-size: 32rpx;
  font-weight: 600;
}

.apply-button {
  background: vars.$primary-color;
  color: #ffffff;
}

.apply-button.applying {
  background: vars.$text-muted;
  opacity: 0.6;
}

.apply-button.applied {
  background: vars.$text-muted;
  opacity: 0.8;
}

.apply-button.contact-hr {
  background: vars.$primary-color;
  opacity: 1;
}

.button-text {
  color: inherit;
}
</style>

