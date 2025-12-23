<template>
  <view class="online-resume-page">
    <view v-if="loading" class="state-card">
      <text class="state-text">{{ t('pages.resume.online.loading') }}</text>
    </view>

    <view v-else-if="errorMessage" class="state-card">
      <text class="state-text">{{ errorMessage }}</text>
      <view class="retry-button" @tap="handleRetry">
        {{ t('pages.resume.online.retry') }}
      </view>
    </view>

    <scroll-view v-else-if="resume" class="resume-content" scroll-y>
      <view class="resume-card header-card">
        <view class="header-left">
          <view class="header-avatar">
            <image
              v-if="userInfo?.avatarUrl"
              class="avatar-image"
              :src="userInfo.avatarUrl"
              mode="aspectFill"
            />
            <view v-else class="avatar-placeholder">
              <text class="avatar-initial">{{ fallbackInitial }}</text>
            </view>
          </view>
          <view class="header-info">
            <view class="name-row">
              <text class="candidate-name">
                {{ displayName }}
              </text>
              <view class="edit-icon" @tap="handleEditBasicInfo">
                <text class="icon-text">✎</text>
              </view>
            </view>
            <text class="candidate-meta">{{ displayMeta }}</text>
            <view v-if="displayPhone" class="candidate-contact">
              <text class="candidate-phone">{{ displayPhone }}</text>
            </view>
          </view>
        </view>
        <view v-if="displayStudentBadge" class="student-badge">
          <text class="badge-text">{{ t('pages.resume.online.graduate') }}</text>
        </view>
      </view>

      <view class="resume-card section" @tap="handleEditAdvantages">
        <view class="section-header">
          <text class="section-title">{{ t('pages.resume.online.advantagesTitle') }}</text>
          <view class="edit-icon-small">
            <text class="icon-text-small">✎</text>
          </view>
        </view>
        <text class="section-content">{{ displayAdvantages }}</text>
      </view>

      <view class="resume-card section">
        <view class="section-header">
          <text class="section-title">{{ t('pages.resume.online.expectationsTitle') }}</text>
          <view class="add-button" @tap="handleAddExpectation">
            <text class="add-icon">+</text>
          </view>
        </view>
        <view v-if="expectationsList.length" class="expectations-list">
          <view
            v-for="expectation in expectationsList"
            :key="expectation.id"
            class="expectation-item"
            @tap="handleEditExpectation(expectation)"
          >
            <view class="expectation-main">
              <text class="expectation-position">{{ expectation.expectedPosition || t('pages.resume.online.infoFallback') }}</text>
              <text class="expectation-salary">{{ formatSalaryRange(expectation) }}</text>
            </view>
            <text v-if="formatExpectationDetail(expectation) !== t('pages.resume.online.infoFallback')" class="expectation-detail">
              {{ formatExpectationDetail(expectation) }}
            </text>
            <text class="expectation-location">{{ formatExpectationLocation(expectation) }}</text>
          </view>
        </view>
        <view v-else class="empty-block">
          <text class="empty-text">{{ t('pages.resume.online.expectationsEmpty') }}</text>
        </view>
      </view>

      <view class="resume-card section">
        <view class="section-header">
          <text class="section-title">{{ t('pages.resume.online.workTitle') }}</text>
          <view class="add-button" @tap="handleAddWork">
            <text class="add-icon">+</text>
          </view>
        </view>
        <view v-if="workList.length" class="entry-list">
          <view
            v-for="work in workList"
            :key="work.id"
            class="entry-card"
            @tap="handleEditWork(work)"
          >
            <view class="entry-header">
              <text class="entry-title">{{ work.companyName }}</text>
              <text class="entry-duration">{{ formatWorkPeriod(work) }}</text>
            </view>
            <view class="entry-meta">
              <text class="entry-meta-text">{{ work.position }}</text>
              <text v-if="work.isInternship === 1" class="entry-meta-badge">
                {{ t('pages.resume.online.internship') }}
              </text>
            </view>
            <text v-if="work.description" class="entry-description">{{ work.description }}</text>
          </view>
        </view>
        <view v-else class="empty-block">
          <text class="empty-text">{{ t('pages.resume.online.workEmpty') }}</text>
        </view>
      </view>

      <view class="resume-card section">
        <view class="section-header">
          <text class="section-title">{{ t('pages.resume.online.projectsTitle') }}</text>
          <view class="add-button" @tap="handleAddProject">
            <text class="add-icon">+</text>
          </view>
        </view>
        <view v-if="projectList.length" class="entry-list">
          <view
            v-for="(project, index) in projectList"
            :key="project.id || index"
            class="entry-card"
            @tap="handleEditProject(project)"
          >
            <view class="entry-header">
              <text class="entry-title">{{ project.projectName }}</text>
              <text class="entry-duration">{{ formatProjectPeriod(project) }}</text>
            </view>
            <view class="entry-meta">
              <text class="entry-meta-text">{{ project.projectRole }}</text>
            </view>
            <text v-if="project.description" class="entry-description">{{ project.description }}</text>
          </view>
        </view>
        <view v-else class="empty-block">
          <text class="empty-text">{{ t('pages.resume.online.projectsEmpty') }}</text>
        </view>
      </view>

      <view class="resume-card section">
        <view class="section-header">
          <text class="section-title">{{ t('pages.resume.online.educationTitle') }}</text>
          <view class="add-button" @tap="handleAddEducation">
            <text class="add-icon">+</text>
          </view>
        </view>
        <view v-if="educationList.length" class="entry-list">
          <view
            v-for="education in educationList"
            :key="education.id"
            class="entry-card"
            @tap="handleEditEducation(education)"
          >
            <view class="entry-header">
              <text class="entry-title">{{ education.schoolName }}</text>
              <text class="entry-duration">{{ formatEducationPeriod(education) }}</text>
            </view>
            <view class="entry-meta">
              <text class="entry-meta-text">{{ education.major }}</text>
              <text class="entry-meta-badge">{{ getDegreeLabel(education.education) }}</text>
            </view>
          </view>
        </view>
        <view v-else class="empty-block">
          <text class="empty-text">{{ t('pages.resume.online.educationEmpty') }}</text>
        </view>
      </view>

      <view class="resume-card section">
        <view class="section-header">
          <text class="section-title">{{ t('pages.resume.online.skillsTitle') }}</text>
          <view class="add-button" @tap="handleAddSkill">
            <text class="add-icon">+</text>
          </view>
        </view>
        <view v-if="skillList.length" class="skill-grid">
          <view
            v-for="skill in skillList"
            :key="skill.id"
            class="skill-chip"
            @tap="handleEditSkill(skill)"
          >
            <text class="skill-name">{{ skill.skillName }}</text>
            <text class="skill-level">{{ getSkillLevelText(skill.level) }}</text>
          </view>
        </view>
        <view v-else class="empty-block">
          <text class="empty-text">{{ t('pages.resume.online.skillsEmpty') }}</text>
        </view>
      </view>
    </scroll-view>

    <view v-else class="state-card">
      <text class="state-text">{{ t('pages.resume.online.empty') }}</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { onLoad, onShow, onPullDownRefresh } from '@dcloudio/uni-app';
import { t } from '@/locales';
import { useNavigationTitle } from '@/utils/useNavigationTitle';
import type { 
  EducationExperience, 
  CompositeResumeData, 
  Skill, 
  WorkExperience, 
  ProjectExperience,
  JobSeekerExpectation,
  SeekerInfo
} from '@/services/api/seeker';
import { fetchCompositeResume } from '@/services/api/seeker';
import { getCurrentUserInfo, type UserInfo } from '@/services/api/user';

useNavigationTitle('navigation.onlineResume');

const loading = ref(true);
const errorMessage = ref('');
const userInfo = ref<UserInfo | null>(null);
const resume = ref<CompositeResumeData | null>(null);

const educationList = computed(() => resume.value?.educationExperiences || []);
const projectList = computed(() => resume.value?.projectExperiences || []);
const workList = computed(() => resume.value?.workExperiences || []);
const skillList = computed<Skill[]>(() => resume.value?.skills || []);
const expectationsList = computed(() => resume.value?.jobSeekerExpectations || []);

const displayName = computed(() => {
  const realName = resume.value?.seekerInfo?.realName;
  const username = userInfo.value?.username;
  return realName || username || t('pages.resume.online.usernamePlaceholder');
});

const displayMeta = computed(() => {
  const info = resume.value?.seekerInfo;
  const parts: string[] = [];
  
  if (info?.birthDate) {
    const age = calculateAge(info.birthDate);
    if (age > 0) {
      parts.push(`${age}${t('pages.resume.online.ageUnit')}`);
    }
  }
  
  if (info?.currentCity) {
    parts.push(info.currentCity);
  }
  
  if (educationList.value.length > 0) {
    const currentYear = new Date().getFullYear();
    const currentEducation = educationList.value.find(edu => {
      if (edu.endYear) {
        const endYear = parseInt(edu.endYear);
        return endYear >= currentYear;
      }
      return false;
    });
    if (currentEducation && currentEducation.endYear) {
      const endYear = parseInt(currentEducation.endYear);
      if (endYear >= currentYear) {
        parts.push(`${endYear}${t('pages.resume.online.graduateYear')}`);
      }
    }
    
    const highestEducation = educationList.value[0];
    if (highestEducation) {
      parts.push(getDegreeLabel(highestEducation.education));
    }
  }
  
  return parts.length > 0 ? parts.join(' · ') : t('pages.resume.online.infoFallback');
});

const displayPhone = computed(() => {
  const phone = userInfo.value?.phone;
  if (!phone) return null;
  
  if (phone.length === 11) {
    return `${phone.substring(0, 3)} ${phone.substring(3, 7)} ${phone.substring(7)}`;
  }
  return phone;
});

const displayStudentBadge = computed(() => {
  const educations = educationList.value;
  if (educations.length === 0) return false;
  
  const currentYear = new Date().getFullYear();
  const currentEducation = educations.find(edu => {
    if (edu.endYear) {
      const endYear = parseInt(edu.endYear);
      return endYear >= currentYear;
    }
    return false;
  });
  if (!currentEducation) return false;
  
  const endYear = parseInt(currentEducation.endYear);
  return endYear >= currentYear;
});

const displayAdvantages = computed(() => {
  return '';
});

const fallbackInitial = computed(() => {
  const name = displayName.value;
  if (name && name.length > 0) {
    return name[0];
  }
  return t('pages.resume.online.placeholderInitial');
});

onLoad(() => {
  fetchResume();
});

onShow(() => {
  fetchResume();
});

onPullDownRefresh(async () => {
  await fetchResume();
  uni.stopPullDownRefresh();
});

async function fetchResume() {
  loading.value = true;
  errorMessage.value = '';
  try {
    const [userInfoData, resumeData] = await Promise.all([
      getCurrentUserInfo(),
      fetchCompositeResume()
    ]);
    
    userInfo.value = userInfoData;
    resume.value = resumeData;
  } catch (error) {
    const errorMsg = error instanceof Error ? error.message : String(error);
    console.error('Failed to load resume:', errorMsg);
    errorMessage.value = `${t('pages.resume.online.loadError')}: ${errorMsg}`;
  } finally {
    loading.value = false;
  }
}

function handleRetry() {
  fetchResume();
}

function handleEditBasicInfo() {
  uni.navigateTo({
    url: '/pages/seeker/profile/resume/edit/basic-info',
  });
}

function handleEditAdvantages() {
  uni.navigateTo({
    url: '/pages/seeker/profile/resume/edit/basic-info',
  });
}

function handleAddExpectation() {
  uni.navigateTo({
    url: '/pages/seeker/profile/resume/edit/expectation',
  });
}

function handleEditExpectation(expectation: JobSeekerExpectation) {
  uni.navigateTo({
    url: `/pages/seeker/profile/resume/edit/expectation?id=${expectation.id}`,
  });
}

function handleAddWork() {
  uni.navigateTo({
    url: '/pages/seeker/profile/resume/edit/work-experience',
  });
}

function handleEditWork(work: WorkExperience) {
  uni.navigateTo({
    url: `/pages/seeker/profile/resume/edit/work-experience?id=${work.id}`,
  });
}

function handleAddProject() {
  uni.navigateTo({
    url: '/pages/seeker/profile/resume/edit/project-experience',
  });
}

function handleEditProject(project: ProjectExperience) {
  if (project.id) {
    uni.navigateTo({
      url: `/pages/seeker/profile/resume/edit/project-experience?id=${project.id}`,
    });
  }
}

function handleAddEducation() {
  uni.navigateTo({
    url: '/pages/seeker/profile/resume/edit/education-experience',
  });
}

function handleEditEducation(education: EducationExperience) {
  uni.navigateTo({
    url: `/pages/seeker/profile/resume/edit/education-experience?id=${education.id}`,
  });
}

function handleAddSkill() {
  uni.navigateTo({
    url: '/pages/seeker/profile/resume/edit/skill',
  });
}

function handleEditSkill(skill: Skill) {
  uni.navigateTo({
    url: `/pages/seeker/profile/resume/edit/skill?id=${skill.id}`,
  });
}

function calculateAge(birthDate: string): number {
  const birth = new Date(birthDate);
  const today = new Date();
  let age = today.getFullYear() - birth.getFullYear();
  const monthDiff = today.getMonth() - birth.getMonth();
  
  if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birth.getDate())) {
    age--;
  }
  
  return age;
}

function formatEducationPeriod(education: EducationExperience): string {
  if (!education.startYear) {
    return t('pages.resume.online.infoFallback');
  }
  
  const currentYear = new Date().getFullYear();
  const endYear = education.endYear ? parseInt(education.endYear) : null;
  const isCurrent = endYear !== null && endYear >= currentYear;
  
  const endLabel = isCurrent 
    ? t('pages.resume.online.presentLabel') 
    : (education.endYear || '');
  
  if (!endLabel) {
    return education.startYear;
  }
  return `${education.startYear} - ${endLabel}`;
}

function formatWorkPeriod(work: WorkExperience): string {
  if (!work.startMonth) {
    return t('pages.resume.online.infoFallback');
  }
  
  const endLabel = work.endMonth || t('pages.resume.online.presentLabel');
  return `${work.startMonth} - ${endLabel}`;
}

function formatProjectPeriod(project: ProjectExperience): string {
  if (!project.startMonth) {
    return t('pages.resume.online.infoFallback');
  }
  
  const endLabel = project.endMonth || t('pages.resume.online.presentLabel');
  return `${project.startMonth} - ${endLabel}`;
}

function formatSalaryRange(expectation: JobSeekerExpectation): string {
  const min = expectation.salaryMin || 0;
  const max = expectation.salaryMax || 0;
  
  if (min === 0 && max === 0) {
    return t('pages.resume.online.infoFallback');
  }
  
  const minK = Math.round(min / 1000);
  const maxK = Math.round(max / 1000);
  
  return `${minK}${t('pages.resume.online.salaryTo')}${maxK}${t('pages.resume.online.salaryUnit')}`;
}

function formatExpectationDetail(expectation: JobSeekerExpectation): string {
  const parts: string[] = [];
  
  if (expectation.expectedPosition) {
    parts.push(expectation.expectedPosition);
  }
  
  return parts.length > 0 ? parts.join('，') : t('pages.resume.online.infoFallback');
}

function formatExpectationLocation(expectation: JobSeekerExpectation): string {
  const parts: string[] = [];
  
  if (expectation.workCity) {
    parts.push(expectation.workCity);
  }
  
  if (expectation.expectedIndustry) {
    parts.push(expectation.expectedIndustry);
  } else {
    parts.push(t('pages.resume.online.infoFallback'));
  }
  
  return parts.join(' | ');
}

function getDegreeLabel(value?: number | null): string {
  if (value === undefined || value === null) {
    return t('pages.resume.online.infoFallback');
  }
  
  const degreeMap: Record<number, string> = {
    0: t('degree.highSchoolOrBelow'),
    1: t('degree.associateDegree'),
    2: t('degree.bachelorDegree'),
    3: t('degree.masterDegree'),
    4: t('degree.doctorDegree'),
  };
  
  return degreeMap[value] || t('pages.resume.online.infoFallback');
}

function getSkillLevelText(level?: number | null): string {
  if (level === undefined || level === null) {
    return t('pages.resume.online.infoFallback');
  }
  
  const levelMap: Record<number, string> = {
    0: '生疏',
    1: '熟悉',
    2: '熟练',
  };
  
  return levelMap[level] || t('pages.resume.online.infoFallback');
}
</script>

<style lang="scss" scoped>
@use "@/styles/variables.scss" as vars;

.online-resume-page {
  min-height: 100vh;
  background-color: #F2F2F7;
  display: flex;
  flex-direction: column;
}

.resume-content {
  padding: 2;
  flex: 1;
}

.resume-card {
  background-color: #FFFFFF;
  border-radius: 0;
  padding: 0;
  margin-bottom: 0;
  box-shadow: none;
}

.header-card {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  position: relative;
  padding: 40rpx 32rpx 32rpx;
  margin-bottom: 20rpx;
  background-color: #FFFFFF;
}

.header-left {
  display: flex;
  gap: vars.$spacing-md;
  flex: 1;
}

.header-avatar {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  background-color: vars.$soft-blue;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  flex-shrink: 0;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.08);
}

.avatar-image {
  width: 100%;
  height: 100%;
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-initial {
  font-size: 44rpx;
  font-weight: 600;
  color: vars.$primary-color;
}

.header-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.name-row {
  display: flex;
  align-items: center;
  gap: vars.$spacing-sm;
}

.candidate-name {
  font-size: 40rpx;
  font-weight: 600;
  color: #000000;
  letter-spacing: -0.5rpx;
}

.edit-icon {
  width: 44rpx;
  height: 44rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: background-color 0.2s;
  
  &:active {
    background-color: rgba(0, 0, 0, 0.05);
  }
}

.icon-text {
  font-size: 28rpx;
  color: vars.$text-muted;
}

.candidate-meta {
  font-size: 28rpx;
  color: #8E8E93;
  line-height: 1.5;
  margin-top: 8rpx;
}

.candidate-contact {
  margin-top: 4rpx;
}

.candidate-phone {
  font-size: 28rpx;
  color: #8E8E93;
  line-height: 1.5;
  margin-top: 4rpx;
}

.student-badge {
  position: absolute;
  top: 40rpx;
  right: 32rpx;
  background: linear-gradient(135deg, vars.$primary-color, vars.$soft-blue);
  border-radius: 20rpx;
  padding: 8rpx 16rpx;
  box-shadow: 0 2rpx 8rpx rgba(75, 163, 255, 0.3);
}

.badge-text {
  font-size: 22rpx;
  color: #fff;
  font-weight: 500;
}

.section {
  display: flex;
  flex-direction: column;
  padding: 32rpx;
  margin-bottom: 20rpx;
  background-color: #FFFFFF;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: vars.$spacing-md;
}

.section-title {
  font-size: 34rpx;
  font-weight: 600;
  color: #000000;
  letter-spacing: -0.5rpx;
}

.section-edit {
  font-size: 40rpx;
  color: #C6C6C8;
  font-weight: 300;
}

.edit-icon-small {
  width: 40rpx;
  height: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: background-color 0.2s;
  
  &:active {
    background-color: rgba(0, 0, 0, 0.05);
  }
}

.icon-text-small {
  font-size: 24rpx;
  color: vars.$text-muted;
}

.add-button {
  width: 48rpx;
  height: 48rpx;
  border-radius: 50%;
  background-color: vars.$primary-color;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2rpx 8rpx rgba(75, 163, 255, 0.3);
  transition: transform 0.2s;
  
  &:active {
    transform: scale(0.95);
  }
}

.add-icon {
  font-size: 32rpx;
  color: #fff;
  line-height: 1;
}

.section-content {
  font-size: 28rpx;
  color: vars.$text-color;
  line-height: 1.6;
}

.expectations-list {
  display: flex;
  flex-direction: column;
  gap: vars.$spacing-md;
}

.expectation-item {
  background-color: #F9F9F9;
  border-radius: 16rpx;
  padding: 28rpx;
  transition: all 0.2s;
  border: 1rpx solid #E5E5EA;
  
  &:active {
    background-color: #F2F2F7;
    transform: scale(0.98);
  }
}

.expectation-main {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: vars.$spacing-sm;
}

.expectation-position {
  font-size: 30rpx;
  font-weight: 600;
  color: vars.$text-color;
}

.expectation-salary {
  font-size: 28rpx;
  color: vars.$primary-color;
  font-weight: 500;
}

.expectation-detail,
.expectation-location {
  font-size: 24rpx;
  color: vars.$text-muted;
  line-height: 1.5;
  margin-top: 4rpx;
}

.entry-list {
  display: flex;
  flex-direction: column;
  gap: vars.$spacing-md;
}

.entry-card {
  background-color: #F9F9F9;
  border-radius: 16rpx;
  padding: 28rpx;
  transition: all 0.2s;
  border: 1rpx solid #E5E5EA;
  
  &:active {
    background-color: #F2F2F7;
    transform: scale(0.98);
  }
}

.entry-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: vars.$spacing-sm;
  gap: vars.$spacing-sm;
}

.entry-title {
  font-size: 30rpx;
  font-weight: 600;
  color: vars.$text-color;
  flex: 1;
}

.entry-duration {
  font-size: 24rpx;
  color: vars.$text-muted;
  white-space: nowrap;
}

.entry-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: vars.$spacing-sm;
}

.entry-meta-text {
  font-size: 26rpx;
  color: vars.$text-muted;
}

.entry-meta-badge {
  font-size: 22rpx;
  color: vars.$primary-color;
  background-color: vars.$primary-color-soft;
  padding: 4rpx 16rpx;
  border-radius: vars.$border-radius-sm;
}

.entry-description {
  font-size: 24rpx;
  color: vars.$text-muted;
  line-height: 1.5;
}

.skill-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}

.skill-chip {
  padding: 20rpx 28rpx;
  border-radius: 16rpx;
  background-color: #F9F9F9;
  display: flex;
  flex-direction: column;
  min-width: 160rpx;
  gap: 8rpx;
  transition: all 0.2s;
  border: 1rpx solid #E5E5EA;
  
  &:active {
    background-color: #F2F2F7;
    transform: scale(0.95);
  }
}

.skill-name {
  font-size: 28rpx;
  color: #000000;
  font-weight: 600;
}

.skill-level {
  font-size: 24rpx;
  color: vars.$primary-color;
  font-weight: 500;
}

.empty-block {
  padding: 40rpx 32rpx;
  background-color: #F9F9F9;
  border-radius: 16rpx;
  text-align: center;
}

.empty-text {
  font-size: 28rpx;
  color: #8E8E93;
  font-weight: 400;
}

.state-card {
  margin: 64rpx 32rpx;
  padding: 48rpx;
  background-color: #FFFFFF;
  border-radius: 20rpx;
  text-align: center;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
}

.state-text {
  font-size: 32rpx;
  color: #8E8E93;
  font-weight: 400;
}

.retry-button {
  margin-top: 32rpx;
  background-color: vars.$primary-color;
  color: #fff;
  border-radius: 16rpx;
  padding: 20rpx 48rpx;
  font-size: 30rpx;
  font-weight: 500;
  transition: opacity 0.2s;
  
  &:active {
    opacity: 0.8;
  }
}
</style>
