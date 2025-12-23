<template>
  <view class="applications-page">
    <view v-if="loading && applications.length === 0" class="loading-container">
      <text class="loading-text">{{ t('pages.jobs.applications.loading') }}</text>
    </view>

    <view v-else-if="error && applications.length === 0" class="error-container">
      <text class="error-text">{{ error }}</text>
      <button class="retry-button" @click="loadApplications">
        {{ t('pages.jobs.applications.retry') }}
      </button>
    </view>

    <scroll-view
      v-else
      class="applications-list"
      scroll-y
      refresher-enabled
      :refresher-triggered="refreshing"
      @refresherrefresh="handleRefresh"
      @scrolltolower="loadMore"
    >
      <view v-if="applications.length === 0" class="empty-container">
        <text class="empty-text">{{ t('pages.jobs.applications.empty') }}</text>
      </view>

      <view
        v-for="app in applications"
        :key="app.applicationId"
        class="application-item"
        @click="handleItemClick(app)"
      >
        <image
          v-if="app.companyLogo"
          class="company-logo"
          :src="app.companyLogo"
          mode="aspectFit"
        />
        <view v-else class="company-logo-placeholder"></view>
        <view class="application-info">
          <view class="application-header">
            <text class="job-title">{{ app.jobTitle }}</text>
            <text class="status-badge" :class="getStatusClass(app.status)">
              {{ getStatusText(app.status) }}
            </text>
          </view>
          <text class="company-name">{{ app.companyName }}</text>
          <view class="application-footer">
            <text class="applied-time">{{ formatTime(app.appliedAt) }}</text>
            <text v-if="app.hrName" class="hr-name">{{ app.hrName }}</text>
          </view>
        </view>
        <text class="arrow">›</text>
      </view>

      <view v-if="loadingMore" class="loading-more">
        <text class="loading-more-text">{{ t('pages.jobs.applications.loadingMore') }}</text>
      </view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { onLoad, onShow } from '@dcloudio/uni-app';
import { t } from '@/locales';
import { getApplications, type ApplicationListItem } from '@/services/api/recruitment';
import dayjs from 'dayjs';

const applications = ref<ApplicationListItem[]>([]);
const loading = ref(false);
const loadingMore = ref(false);
const error = ref<string | null>(null);
const refreshing = ref(false);
const currentPage = ref(1);
const pageSize = 20;
const total = ref(0);
const noMore = ref(false);

onLoad(() => {
  uni.setNavigationBarTitle({
    title: t('pages.jobs.applications.title')
  });
  loadApplications();
});

onShow(() => {
  loadApplications();
});

async function loadApplications(reset = false) {
  if (reset) {
    currentPage.value = 1;
    applications.value = [];
    noMore.value = false;
  }

  if (loading.value || loadingMore.value) return;

  const pageToLoad = reset ? 1 : currentPage.value;

  if (pageToLoad === 1) {
    loading.value = true;
  } else {
    loadingMore.value = true;
  }

  error.value = null;

  try {
    const response = await getApplications({
      page: pageToLoad,
      size: pageSize,
    });

    if (reset || pageToLoad === 1) {
      applications.value = response.list;
      currentPage.value = 2;
    } else {
      applications.value = [...applications.value, ...response.list];
      currentPage.value++;
    }

    total.value = response.total;

    if (applications.value.length >= response.total || response.list.length < pageSize) {
      noMore.value = true;
    }
  } catch (err) {
    error.value = err instanceof Error ? err.message : t('pages.jobs.applications.loadError');
    console.error('Failed to load applications:', err);
  } finally {
    loading.value = false;
    loadingMore.value = false;
    refreshing.value = false;
  }
}

async function handleRefresh() {
  refreshing.value = true;
  await loadApplications(true);
}

async function loadMore() {
  if (noMore.value || loadingMore.value) return;
  await loadApplications();
}

function handleItemClick(app: ApplicationListItem) {
  uni.navigateTo({
    url: `/pages/seeker/jobs/detail?jobId=${app.jobId}`
  });
}

function getStatusText(status: number | undefined): string {
  if (status === undefined) return '';
  const statusMap: Record<number, string> = {
    0: t('pages.jobs.applications.statusApplied'),
    1: t('pages.jobs.applications.statusViewed'),
    2: t('pages.jobs.applications.statusInterview'),
    3: t('pages.jobs.applications.statusInterviewed'),
    4: t('pages.jobs.applications.statusOffered'),
    5: t('pages.jobs.applications.statusRejected'),
    6: t('pages.jobs.applications.statusWithdrawn'),
  };
  return statusMap[status] || '';
}

function getStatusClass(status: number | undefined): string {
  if (status === undefined) return 'status-applied';
  const classMap: Record<number, string> = {
    0: 'status-applied',
    1: 'status-viewed',
    2: 'status-interview',
    3: 'status-interviewed',
    4: 'status-offered',
    5: 'status-rejected',
    6: 'status-withdrawn',
  };
  return classMap[status] || 'status-applied';
}

function formatTime(timeStr: string | undefined): string {
  if (!timeStr) return '';
  
  const time = dayjs(timeStr);
  if (!time.isValid()) return '';

  const now = dayjs();
  const diffDays = now.diff(time, 'day');

  if (diffDays === 0) {
    return t('pages.chat.list.today');
  } else if (diffDays === 1) {
    return t('pages.chat.list.yesterday');
  } else if (diffDays < 7) {
    return `${diffDays}${t('pages.jobs.applications.daysAgo')}`;
  } else {
    return time.format('MM-DD');
  }
}
</script>

<style lang="scss" scoped>
@use "@/styles/variables.scss" as vars;

.applications-page {
  min-height: 100vh;
  background: vars.$surface-color;
  display: flex;
  flex-direction: column;
}

.loading-container,
.error-container,
.empty-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 400rpx;
  padding: vars.$spacing-xl;
}

.loading-text,
.error-text,
.empty-text {
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

.applications-list {
  flex: 1;
  height: calc(100vh);
}

.application-item {
  display: flex;
  align-items: center;
  gap: vars.$spacing-md;
  padding: vars.$spacing-lg vars.$spacing-xl;
  background: vars.$surface-color;
  border-bottom: 1rpx solid vars.$bg-color;
  cursor: pointer;
  position: relative;
}

.company-logo {
  width: 100rpx;
  height: 100rpx;
  border-radius: vars.$border-radius-sm;
  background: vars.$bg-color;
  flex-shrink: 0;
}

.company-logo-placeholder {
  width: 100rpx;
  height: 100rpx;
  border-radius: vars.$border-radius-sm;
  background: vars.$bg-color;
  flex-shrink: 0;
}

.application-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: vars.$spacing-xs;
  min-width: 0;
  padding-right: 60rpx;
}

.application-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: vars.$spacing-sm;
}

.job-title {
  font-size: 32rpx;
  font-weight: 600;
  color: vars.$text-color;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.status-badge {
  padding: 4rpx 12rpx;
  border-radius: vars.$border-radius-sm;
  font-size: 22rpx;
  flex-shrink: 0;
}

.status-applied {
  background: vars.$primary-color-soft;
  color: vars.$primary-color;
}

.status-viewed {
  background: #e3f2fd;
  color: #1976d2;
}

.status-interview {
  background: #fff3e0;
  color: #f57c00;
}

.status-interviewed {
  background: #f3e5f5;
  color: #7b1fa2;
}

.status-offered {
  background: #e8f5e9;
  color: #388e3c;
}

.status-rejected {
  background: #ffebee;
  color: #d32f2f;
}

.status-withdrawn {
  background: vars.$bg-color;
  color: vars.$text-muted;
}

.company-name {
  font-size: 26rpx;
  color: vars.$text-muted;
}

.application-footer {
  display: flex;
  align-items: center;
  gap: vars.$spacing-md;
  flex-wrap: nowrap;
}

.applied-time {
  font-size: 24rpx;
  color: vars.$text-muted;
}

.hr-name {
  font-size: 24rpx;
  color: vars.$text-muted;
  flex-shrink: 0;
}

.arrow {
  font-size: 48rpx;
  color: vars.$text-muted;
  font-weight: 300;
  flex-shrink: 0;
  position: absolute;
  right: vars.$spacing-xl;
  top: 50%;
  transform: translateY(-50%);
}

.loading-more {
  display: flex;
  justify-content: center;
  padding: vars.$spacing-lg;
}

.loading-more-text {
  font-size: 26rpx;
  color: vars.$text-muted;
}
</style>

