<template>
  <view class="internship-page">
    <view class="filter-section">
      <view 
        v-for="filter in filters" 
        :key="filter.value"
        class="filter-item"
        :class="{ active: currentFilter === filter.value }"
        @click="handleFilterChange(filter.value)"
      >
        <text class="filter-text">{{ filter.label }}</text>
      </view>
    </view>

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
      <text class="error-text">{{ t('pages.jobs.loadError') }}</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { t } from '@/locales';
import { getInternJobRecommendations, type InternJobItem } from '@/services/api/recommendations';
import JobCard from '@/components/common/JobCard.vue';

type FilterType = 'recommended' | 'nearby' | 'latest';

const jobs = ref<InternJobItem[]>([]);
const loading = ref(false);
const error = ref<string | null>(null);
const currentFilter = ref<FilterType>('recommended');

const filters = computed(() => [
  { value: 'recommended' as FilterType, label: t('pages.jobs.recommended') },
  { value: 'nearby' as FilterType, label: t('pages.jobs.nearby') },
  { value: 'latest' as FilterType, label: t('pages.jobs.latest') },
]);

const loadJobs = async () => {
  loading.value = true;
  error.value = null;
  try {
    const response = await getInternJobRecommendations();
    jobs.value = response.jobs;
  } catch (err) {
    error.value = err instanceof Error ? err.message : 'Unknown error';
  } finally {
    loading.value = false;
  }
};

const handleFilterChange = (filter: FilterType) => {
  currentFilter.value = filter;
  // Filter logic will be implemented when API is ready
};

onMounted(() => {
  loadJobs();
});
</script>

<style lang="scss" scoped>
@use "@/styles/variables.scss" as vars;

.internship-page {
  display: flex;
  flex-direction: column;
}

.filter-section {
  display: flex;
  gap: vars.$spacing-lg;
  padding: vars.$spacing-md vars.$spacing-xl;
  margin-bottom: vars.$spacing-sm;
}

.filter-item {
  position: relative;
  cursor: pointer;
}

.filter-text {
  font-size: 28rpx;
  color: vars.$text-muted;
  transition: color 0.3s;
}

.filter-item.active .filter-text {
  color: vars.$text-color;
  font-weight: 600;
}

.filter-item.active::after {
  content: '';
  position: absolute;
  bottom: -8rpx;
  left: 0;
  right: 0;
  height: 6rpx;
  background-color: vars.$primary-color;
  border-radius: 3rpx;
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

