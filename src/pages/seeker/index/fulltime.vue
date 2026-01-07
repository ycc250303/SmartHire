<template>
  <view class="fulltime-page">
    <view class="job-list" v-if="jobs.length > 0">
      <JobCard
        v-for="job in jobs"
        :key="job.jobId"
        :job="job"
      />
    </view>
    <view class="empty-state" v-else-if="!loading && !error">
      <text class="empty-text">{{ t('pages.jobs.noJobs') }}</text>
    </view>
    <view class="loading-state" v-else-if="loading">
      <text class="loading-text">{{ t('pages.jobs.loading') }}</text>
    </view>
    <view class="error-state" v-else-if="error">
      <text class="error-text">{{ error }}</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';
import { t } from '@/locales';
import { getFullTimeJobRecommendationsWithSupplement, type FullTimeJobItem } from '@/services/api/recommendations';
import JobCard from '@/components/common/JobCard.vue';

type FilterType = 'recommended' | 'nearby' | 'latest';

interface Props {
  filter: FilterType;
}

const props = defineProps<Props>();

const jobs = ref<FullTimeJobItem[]>([]);
const loading = ref(false);
const error = ref<string | null>(null);

const loadJobs = async () => {
  loading.value = true;
  error.value = null;
  try {
    const response = await getFullTimeJobRecommendationsWithSupplement();
    jobs.value = response.jobs;
  } catch (err) {
    error.value = err instanceof Error ? err.message : 'Unknown error';
  } finally {
    loading.value = false;
  }
};

watch(() => props.filter, () => {
  loadJobs();
}, { immediate: false });

onMounted(() => {
  loadJobs();
});
</script>

<style lang="scss" scoped>
@use "@/styles/variables.scss" as vars;

.fulltime-page {
  display: flex;
  flex-direction: column;
}

.job-list {
  display: flex;
  flex-direction: column;
  padding: 0 vars.$spacing-md;
}

.empty-state,
.loading-state,
.error-state {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400rpx;
  padding: vars.$spacing-xl;
}

.empty-text,
.loading-text,
.error-text {
  font-size: 28rpx;
  color: vars.$text-muted;
}

.error-text {
  color: #ff6b6b;
}
</style>

