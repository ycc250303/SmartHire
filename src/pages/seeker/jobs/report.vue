<template>
  <view class="report-page">
    <view v-if="loading" class="status-box">
      <text class="status-text">{{ t('pages.jobs.report.loading') }}</text>
    </view>

    <view v-else-if="loadError" class="status-box">
      <text class="error-text">{{ loadError }}</text>
      <button class="retry-button" @click="fetchJobDetail">
        {{ t('pages.jobs.report.retry') }}
      </button>
    </view>

    <view v-else class="content">
      <view class="header-card">
        <view class="badge">{{ t('pages.jobs.report.title') }}</view>
        <text class="job-title">{{ job?.jobTitle }}</text>
        <text v-if="job?.company?.companyName" class="job-company">{{ job.company.companyName }}</text>
        <view class="job-meta" v-if="job?.city">
          <text class="meta-chip">{{ job.city }}</text>
          <text v-if="job?.jobType === 1" class="meta-chip">{{ t('pages.jobs.internship') }}</text>
          <text v-else-if="job?.jobType === 2" class="meta-chip">{{ t('pages.jobs.parttime') }}</text>
          <text v-else-if="job?.jobType === 0" class="meta-chip">{{ t('pages.jobs.fulltime') }}</text>
        </view>
      </view>

      <view class="card form-card">
        <text class="section-title">{{ t('pages.jobs.report.reportTypeLabel') }}</text>
        <picker
          mode="selector"
          :range="reportTypeOptions"
          range-key="label"
          :value="selectedReportTypeIndex"
          @change="onReportTypeChange"
        >
          <view class="picker-row">
            <text
              class="picker-value"
              :class="{ placeholder: !selectedReportTypeLabel }"
            >
              {{ selectedReportTypeLabel || t('pages.jobs.report.reportTypePlaceholder') }}
            </text>
          </view>
        </picker>
      </view>

      <view class="card form-card">
        <text class="section-title">{{ t('pages.jobs.report.reasonLabel') }}</text>
        <view class="textarea-box">
          <textarea
            class="textarea"
            :placeholder="t('pages.jobs.report.reasonPlaceholder')"
            v-model="form.reason"
            maxlength="500"
            auto-height
          />
          <text class="helper-text">{{ form.reason.length }}/500</text>
        </view>
      </view>

      <view class="actions">
        <button class="cancel-button" @click="handleCancel" :disabled="submitting">
          {{ t('common.cancel') }}
        </button>
        <button
          class="submit-button"
          :class="{ submitting }"
          @click="handleSubmit"
          :disabled="!canSubmit || submitting"
        >
          {{ submitting ? t('pages.jobs.report.submitting') : t('pages.jobs.report.submit') }}
        </button>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { t } from '@/locales';
import { getJobDetail, submitSeekerReport, type JobDetail } from '@/services/api/recruitment';

const jobId = ref<number | null>(null);
const job = ref<JobDetail | null>(null);
const loading = ref(false);
const loadError = ref<string | null>(null);
const submitting = ref(false);

const form = reactive({
  reportType: null as number | null,
  reason: '',
});

const reportTypeOptions = computed(() => [
  { value: 1, label: t('pages.jobs.report.types.spam') },
  { value: 2, label: t('pages.jobs.report.types.inappropriate') },
  { value: 3, label: t('pages.jobs.report.types.fakeJob') },
  { value: 4, label: t('pages.jobs.report.types.fraud') },
  { value: 5, label: t('pages.jobs.report.types.harassment') },
  { value: 6, label: t('pages.jobs.report.types.other') },
]);

const selectedReportTypeLabel = computed(() => {
  const option = reportTypeOptions.value.find(item => item.value === form.reportType);
  return option?.label || '';
});

const selectedReportTypeIndex = computed(() => {
  return reportTypeOptions.value.findIndex(item => item.value === form.reportType);
});

const canSubmit = computed(() => {
  return !!form.reportType && form.reason.trim().length > 0;
});

onLoad((options: Record<string, any>) => {
  if (options?.jobId) {
    jobId.value = Number(options.jobId);
    fetchJobDetail();
  } else {
    loadError.value = t('pages.jobs.report.invalidJobId');
  }
});

async function fetchJobDetail() {
  if (!jobId.value) return;

  loading.value = true;
  loadError.value = null;

  try {
    job.value = await getJobDetail(jobId.value);
  } catch (err) {
    console.error('Failed to load job for report:', err);
    loadError.value = err instanceof Error ? err.message : t('pages.jobs.report.loadError');
  } finally {
    loading.value = false;
  }
}

function onReportTypeChange(event: any) {
  const index = Number(event?.detail?.value);
  const option = reportTypeOptions.value[index];
  if (option) {
    form.reportType = option.value;
  }
}

function handleCancel() {
  uni.navigateBack();
}

async function handleSubmit() {
  if (!jobId.value) return;

  if (!canSubmit.value) {
    uni.showToast({
      title: t('pages.jobs.report.validationError'),
      icon: 'none',
    });
    return;
  }

  submitting.value = true;

  try {
    await submitSeekerReport({
      targetId: jobId.value,
      targetType: 2,
      reportType: form.reportType as number,
      reason: form.reason.trim(),
    });

    uni.showToast({
      title: t('pages.jobs.report.submitSuccess'),
      icon: 'success',
    });

    setTimeout(() => {
      uni.navigateBack();
    }, 600);
  } catch (err) {
    console.error('Failed to submit report:', err);
    const message = err instanceof Error ? err.message : t('pages.jobs.report.submitError');
    uni.showToast({
      title: message,
      icon: 'none',
    });
  } finally {
    submitting.value = false;
  }
}
</script>

<style lang="scss" scoped>
@use "@/styles/variables.scss" as vars;

.report-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #f5f8ff 0%, vars.$surface-color 38%);
  padding: vars.$spacing-xl vars.$spacing-xl vars.$spacing-lg;
  display: flex;
  flex-direction: column;
}

.content {
  display: flex;
  flex-direction: column;
  gap: vars.$spacing-md;
}

.status-box {
  min-height: 320rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  gap: vars.$spacing-md;
}

.status-text {
  font-size: 30rpx;
  color: vars.$text-muted;
}

.error-text {
  font-size: 30rpx;
  color: #ff6b6b;
}

.retry-button {
  padding: vars.$spacing-sm vars.$spacing-lg;
  background: vars.$primary-color;
  color: #ffffff;
  border-radius: vars.$border-radius-sm;
  font-size: 28rpx;
}

.header-card {
  background: #ffffff;
  border-radius: vars.$border-radius;
  padding: vars.$spacing-xl;
  box-shadow: 0 10rpx 22rpx rgba(75, 163, 255, 0.12);
  display: flex;
  flex-direction: column;
  gap: vars.$spacing-sm;
}

.badge {
  display: inline-flex;
  align-items: center;
  padding: 6rpx 16rpx;
  border-radius: 999rpx;
  background: vars.$primary-color-soft;
  color: vars.$primary-color;
  font-size: 24rpx;
  width: fit-content;
}

.job-title {
  font-size: 36rpx;
  color: vars.$text-color;
  font-weight: 700;
  line-height: 1.4;
}

.job-company {
  font-size: 28rpx;
  color: vars.$text-muted;
}

.job-meta {
  display: flex;
  flex-wrap: wrap;
  gap: vars.$spacing-xs;
}

.meta-chip {
  padding: 8rpx 16rpx;
  border-radius: vars.$border-radius-sm;
  background: vars.$bg-color;
  font-size: 24rpx;
  color: vars.$text-muted;
}

.card {
  background: #ffffff;
  border-radius: vars.$border-radius;
  padding: vars.$spacing-lg;
  box-shadow: 0 6rpx 16rpx rgba(0, 0, 0, 0.05);
  display: flex;
  flex-direction: column;
  gap: vars.$spacing-md;
}

.form-card {
  gap: vars.$spacing-sm;
}

.section-title {
  font-size: 30rpx;
  font-weight: 600;
  color: vars.$text-color;
}

.picker-row {
  background: vars.$bg-color;
  border-radius: vars.$border-radius-sm;
  padding: vars.$spacing-md vars.$spacing-lg;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border: 1rpx solid rgba(0, 0, 0, 0.04);
}

.picker-value {
  font-size: 28rpx;
  color: vars.$text-color;
  font-weight: 600;
}

.picker-value.placeholder {
  color: vars.$text-muted;
  font-weight: 400;
}

.textarea-box {
  background: vars.$bg-color;
  border-radius: vars.$border-radius-sm;
  padding: vars.$spacing-md vars.$spacing-lg;
  border: 1rpx solid rgba(0, 0, 0, 0.04);
}

.textarea {
  width: 100%;
  font-size: 28rpx;
  color: vars.$text-color;
  min-height: 200rpx;
}

.helper-text {
  font-size: 24rpx;
  color: vars.$text-muted;
  text-align: right;
  margin-top: vars.$spacing-xs;
}

.actions {
  display: flex;
  gap: vars.$spacing-md;
  position: sticky;
  bottom: vars.$spacing-lg;
  margin-top: vars.$spacing-md;
}

.cancel-button,
.submit-button {
  flex: 1;
  height: 88rpx;
  border-radius: vars.$border-radius;
  font-size: 32rpx;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cancel-button {
  background: vars.$bg-color;
  color: vars.$text-color;
}

.submit-button {
  background: vars.$primary-color;
  color: #ffffff;
}

.submit-button.submitting {
  opacity: 0.8;
}
</style>

