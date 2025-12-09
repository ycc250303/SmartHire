<template>
  <view class="page">
    <view class="header-container">
      <view class="category-section">
        <view class="category-list">
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
        <view class="search-icon" @click="handleSearchClick">
          <text class="search-icon-text">⌕</text>
        </view>
      </view>

      <view class="filter-section">
        <view 
          v-for="filter in filters" 
          :key="filter.value"
          class="filter-item"
          :class="{ active: currentFilter === filter.value }"
          @click="handleFilterChange(filter.value)"
        >
          <text class="filter-text">{{ filter.label }}</text>
          <view v-if="currentFilter === filter.value" class="filter-indicator"></view>
        </view>
      </view>
    </view>

    <view class="content-section">
      <component :is="currentComponent" :filter="currentFilter" />
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
type FilterType = 'recommended' | 'nearby' | 'latest';

const currentCategory = ref<CategoryType>('internship');
const currentFilter = ref<FilterType>('recommended');

const categories = computed(() => [
  { value: 'internship' as CategoryType, label: t('pages.jobs.internship') },
  { value: 'parttime' as CategoryType, label: t('pages.jobs.parttime') },
  { value: 'fulltime' as CategoryType, label: t('pages.jobs.fulltime') },
]);

const filters = computed(() => [
  { value: 'recommended' as FilterType, label: t('pages.jobs.recommended') },
  { value: 'nearby' as FilterType, label: t('pages.jobs.nearby') },
  { value: 'latest' as FilterType, label: t('pages.jobs.latest') },
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

const handleFilterChange = (filter: FilterType) => {
  currentFilter.value = filter;
};

const handleSearchClick = () => {
  uni.navigateTo({
    url: '/pages/seeker/index/search/index'
  });
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
  background: vars.$surface-color;
}

.header-container {
  background: linear-gradient(to bottom, vars.$light-blue 0%, vars.$surface-color 100%);
  position: sticky;
  top: 0;
  z-index: 1000;
  padding-top: calc(var(--status-bar-height) + vars.$spacing-lg);
}

.category-section {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: vars.$spacing-lg vars.$spacing-xl;
  margin-bottom: vars.$spacing-md;
  background: transparent;
}

.category-list {
  display: flex;
  gap: vars.$spacing-xl;
  flex: 1;
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

.filter-section {
  display: flex;
  gap: vars.$spacing-lg;
  padding: vars.$spacing-lg vars.$spacing-xl;
  margin-bottom: vars.$spacing-sm;
  background: transparent;
}

.filter-item {
  position: relative;
  cursor: pointer;
  padding-bottom: vars.$spacing-xs;
}

.filter-text {
  font-size: 28rpx;
  color: vars.$text-muted;
  transition: all 0.3s;
}

.filter-item.active .filter-text {
  color: vars.$text-color;
  font-weight: 600;
}

.filter-indicator {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 6rpx;
  background-color: vars.$primary-color;
  border-radius: 3rpx;
}

.search-icon {
  width: 44rpx;
  height: 44rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  flex-shrink: 0;
  margin-left: vars.$spacing-md;
}

.search-icon-text {
  font-size: 60rpx;
  line-height: 1;
  color: vars.$text-muted;
}

.content-section {
  flex: 1;
  padding-bottom: 120rpx;
  position: relative;
  z-index: 1;
}
</style>
