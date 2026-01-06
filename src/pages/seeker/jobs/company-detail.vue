<template>
  <view class="company-detail-page">
    <view v-if="loading" class="loading-container">
      <text class="loading-text">{{ t('pages.jobs.jobDetail.loading') }}</text>
    </view>

    <view v-else-if="error" class="error-container">
      <text class="error-text">{{ error }}</text>
    </view>

    <scroll-view v-else-if="company" class="content-scroll" scroll-y>
      <view class="company-header">
        <view class="company-logo-wrapper" v-if="company.companyLogo">
          <image :src="company.companyLogo" class="company-logo" mode="aspectFit" />
        </view>
        <text class="company-name">{{ company.companyName }}</text>
        <view class="company-meta-row">
          <text v-if="company.industry" class="company-meta-text">
            {{ company.industry }}
          </text>
        </view>
      </view>

      <view class="company-section">
        <text class="section-title">{{ t('pages.jobs.jobDetail.company') }}</text>
        <view class="company-info-list">
          <view
            class="info-item"
            v-if="company.companyScale !== undefined"
          >
            <text class="info-label">
              {{ t('pages.jobs.companyDetail.scale') }}
            </text>
            <text class="info-value">
              {{ formatCompanyScale(company.companyScale) }}
            </text>
          </view>
          <view
            class="info-item"
            v-if="company.financingStage !== undefined"
          >
            <text class="info-label">
              {{ t('pages.jobs.companyDetail.financing') }}
            </text>
            <text class="info-value">
              {{ formatFinancingStage(company.financingStage) }}
            </text>
          </view>
          <view
            class="info-item"
            v-if="company.website"
          >
            <text class="info-label">
              {{ t('pages.jobs.companyDetail.website') }}
            </text>
            <text class="info-value">
              {{ company.website }}
            </text>
          </view>
        </view>
      </view>

      <view
        class="company-section"
        v-if="company.mainBusiness"
      >
        <text class="section-title">
          {{ t('pages.jobs.companyDetail.mainBusiness') }}
        </text>
        <text class="section-content">
          {{ company.mainBusiness }}
        </text>
      </view>

      <view
        class="company-section"
        v-if="company.description"
      >
        <text class="section-title">
          {{ t('pages.jobs.companyDetail.description') }}
        </text>
        <text class="section-content">
          {{ company.description }}
        </text>
      </view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { t } from '@/locales';
import { getJobDetail, type JobDetailCompany } from '@/services/api/recruitment';

const company = ref<JobDetailCompany | null>(null);
const loading = ref(false);
const error = ref<string | null>(null);
const jobId = ref<number | null>(null);

onLoad((options: any) => {
  if (options?.jobId) {
    jobId.value = parseInt(options.jobId);
    loadCompanyDetail();
  } else {
    error.value = t('pages.jobs.jobDetail.invalidJobId');
  }
});

async function loadCompanyDetail() {
  if (!jobId.value) return;

  loading.value = true;
  error.value = null;

  try {
    const job = await getJobDetail(jobId.value);
    if (job.company) {
      company.value = job.company;
      if (job.company.companyName) {
        uni.setNavigationBarTitle({
          title: job.company.companyName,
        });
      }
    } else {
      error.value = t('pages.jobs.companyDetail.noCompany');
    }
  } catch (err) {
    error.value = err instanceof Error ? err.message : t('pages.jobs.jobDetail.loadError');
    console.error('Failed to load company detail:', err);
  } finally {
    loading.value = false;
  }
}

function formatCompanyScale(scale: number): string {
  const map: Record<number, string> = {
    1: t('pages.jobs.companyDetail.scaleSmall'),
    2: t('pages.jobs.companyDetail.scaleMedium'),
    3: t('pages.jobs.companyDetail.scaleLarge'),
    4: t('pages.jobs.companyDetail.scaleExtraLarge'),
    5: t('pages.jobs.companyDetail.scaleGiant'),
    6: t('pages.jobs.companyDetail.scaleUnknown'),
  };
  return map[scale] || t('pages.jobs.companyDetail.scaleUnknown');
}

function formatFinancingStage(stage: number): string {
  const map: Record<number, string> = {
    1: t('pages.jobs.companyDetail.financingAngel'),
    2: t('pages.jobs.companyDetail.financingA'),
    3: t('pages.jobs.companyDetail.financingB'),
    4: t('pages.jobs.companyDetail.financingC'),
    5: t('pages.jobs.companyDetail.financingListed'),
    6: t('pages.jobs.companyDetail.financingNone'),
  };
  return map[stage] || t('pages.jobs.companyDetail.financingUnknown');
}
</script>

<style lang="scss" scoped>
@use "@/styles/variables.scss" as vars;

.company-detail-page {
  min-height: 100vh;
  background: vars.$surface-color;
  display: flex;
  flex-direction: column;
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

.content-scroll {
  flex: 1;
  height: 100vh;
}

.company-header {
  background: linear-gradient(to bottom, vars.$light-blue 0%, vars.$surface-color 100%);
  padding: vars.$spacing-xl;
  padding-top: calc(var(--status-bar-height) + vars.$spacing-lg);
  padding-bottom: vars.$spacing-xl;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: vars.$spacing-md;
}

.company-logo-wrapper {
  width: 120rpx;
  height: 120rpx;
  border-radius: vars.$border-radius-sm;
  background: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
}

.company-logo {
  width: 96rpx;
  height: 96rpx;
  border-radius: vars.$border-radius-sm;
}

.company-name {
  font-size: 40rpx;
  font-weight: 700;
  color: vars.$text-color;
}

.company-meta-row {
  display: flex;
  align-items: center;
  gap: vars.$spacing-sm;
}

.company-meta-text {
  font-size: 28rpx;
  color: vars.$text-muted;
}

.company-section {
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

.company-info-list {
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
  min-width: 160rpx;
}

.info-value {
  font-size: 28rpx;
  color: vars.$text-color;
  flex: 1;
}
</style>


