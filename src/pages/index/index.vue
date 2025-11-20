<template>
  <view class="page">
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
import { ref, onMounted } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { t } from '@/locales';
import { getInternJobRecommendations, type InternJobItem } from '@/services/api/recommendations';
import JobCard from '@/components/common/JobCard.vue';

const jobs = ref<InternJobItem[]>([]);
const loading = ref(false);
const error = ref<string | null>(null);

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

onLoad(() => {
  uni.setNavigationBarTitle({
    title: t('navigation.jobs')
  });
});

onMounted(() => {
  loadJobs();
});
</script>

<style lang="scss" scoped>
@use "@/styles/variables.scss" as vars;

.page {
  min-height: 100vh;
  background-color: vars.$bg-color;
  padding: vars.$spacing-md;
}

.job-list {
  display: flex;
  flex-direction: column;
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
