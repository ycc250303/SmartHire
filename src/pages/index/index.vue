<template>
  <view class="page">
    <view class="category-section">
      <view 
        v-for="category in categories" 
        :key="category.value"
        class="category-item"
        :class="{ active: currentCategory === category.value }"
        @click="handleCategoryChange(category.value)"
      >
        <text class="category-text">{{ category.label }}</text>
        <view v-if="currentCategory === category.value" class="category-indicator"></view>
      </view>
    </view>

    <view class="content-section">
      <component :is="currentComponent" />
    </view>
    
    <CustomTabBar />
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { onLoad, onShow } from '@dcloudio/uni-app';
import { t } from '@/locales';
import InternshipPage from './internship.vue';
import ParttimePage from './parttime.vue';
import FulltimePage from './fulltime.vue';
import CustomTabBar from '@/components/common/CustomTabBar.vue';

type CategoryType = 'internship' | 'parttime' | 'fulltime';

const currentCategory = ref<CategoryType>('internship');

const categories = computed(() => [
  { value: 'internship' as CategoryType, label: t('pages.jobs.internship') },
  { value: 'parttime' as CategoryType, label: t('pages.jobs.parttime') },
  { value: 'fulltime' as CategoryType, label: t('pages.jobs.fulltime') },
]);

const currentComponent = computed(() => {
  switch (currentCategory.value) {
    case 'internship':
      return InternshipPage;
    case 'parttime':
      return ParttimePage;
    case 'fulltime':
      return FulltimePage;
    default:
      return InternshipPage;
  }
});

const handleCategoryChange = (category: CategoryType) => {
  currentCategory.value = category;
};

onLoad(() => {
  uni.setNavigationBarTitle({
    title: t('navigation.jobs')
  });
});

onShow(() => {
  uni.hideTabBar();
});
</script>

<style lang="scss" scoped>
@use "@/styles/variables.scss" as vars;

.page {
  min-height: 100vh;
  background: linear-gradient(to bottom, vars.$light-blue 0%, vars.$surface-color 10%);
  padding-top: calc(var(--status-bar-height) + vars.$spacing-lg);
}

.category-section {
  display: flex;
  gap: vars.$spacing-xl;
  padding: vars.$spacing-lg vars.$spacing-xl;
  margin-bottom: vars.$spacing-md;
}

.category-item {
  position: relative;
  cursor: pointer;
  padding-bottom: vars.$spacing-xs;
}

.category-text {
  font-size: 36rpx;
  color: vars.$text-muted;
  transition: all 0.3s;
}

.category-item.active .category-text {
  color: vars.$text-color;
  font-weight: 700;
}

.category-indicator {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 6rpx;
  background-color: vars.$primary-color;
  border-radius: vars.$border-radius;
}

.content-section {
  flex: 1;
  padding-bottom: 120rpx;
}
</style>
