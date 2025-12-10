<template>
  <view class="major-selector-page">
    <view class="search-container">
      <view class="search-box">
        <text class="search-icon">🔍</text>
        <input
          class="search-input"
          v-model="searchKeyword"
          :placeholder="t('pages.resume.edit.major.searchPlaceholder')"
          placeholder-class="search-placeholder"
          @input="handleSearchInput"
        />
        <view v-if="searchKeyword" class="clear-icon" @tap="clearSearch">
          <text class="clear-text">✕</text>
        </view>
      </view>
    </view>

    <scroll-view v-if="!searchKeyword" class="content" scroll-y>
      <view
        v-for="(category, categoryIndex) in categories"
        :key="categoryIndex"
        class="category-section"
      >
        <view class="category-header" @tap="toggleCategory(categoryIndex)">
          <text class="category-name">{{ category.name }}</text>
          <text class="category-count">({{ category.majors.length }})</text>
          <text class="expand-icon" :class="{ expanded: expandedCategories.has(categoryIndex) }">
            ›
          </text>
        </view>
        <view
          v-if="expandedCategories.has(categoryIndex)"
          class="majors-list"
        >
          <view
            v-for="(major, majorIndex) in category.majors"
            :key="majorIndex"
            class="major-item"
            @tap="selectMajor(major)"
          >
            <text class="major-name">{{ major.name }}</text>
            <text class="major-category2">{{ major.category2 }}</text>
          </view>
        </view>
      </view>
    </scroll-view>

    <scroll-view v-else class="content" scroll-y>
      <view v-if="searchResults.length === 0" class="empty-state">
        <text class="empty-text">{{ t('pages.resume.edit.major.noResults') }}</text>
      </view>
      <view
        v-for="(major, index) in searchResults"
        :key="index"
        class="major-item"
        @tap="selectMajor(major)"
      >
        <text class="major-name">{{ major.name }}</text>
        <view class="major-info">
          <text class="major-category1">{{ major.category1 }}</text>
          <text class="major-category2">{{ major.category2 }}</text>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { t } from '@/locales';
import { useNavigationTitle } from '@/utils/useNavigationTitle';
import { useMajorData, type MajorItem } from '@/utils/useMajorData';

useNavigationTitle('navigation.selectMajor');

const { getCategories, searchMajors } = useMajorData();

const categories = ref(getCategories());
const searchKeyword = ref('');
const expandedCategories = ref<Set<number>>(new Set());
const searchResults = ref<MajorItem[]>([]);
let eventChannel: any = null;

onLoad(() => {
  // Get event channel from current page instance
  const pages = getCurrentPages();
  const currentPage = pages[pages.length - 1];
  if (currentPage && typeof (currentPage as any).getOpenerEventChannel === 'function') {
    eventChannel = (currentPage as any).getOpenerEventChannel();
  }
});


function toggleCategory(categoryIndex: number) {
  if (expandedCategories.value.has(categoryIndex)) {
    expandedCategories.value.delete(categoryIndex);
  } else {
    expandedCategories.value.add(categoryIndex);
  }
  expandedCategories.value = new Set(expandedCategories.value);
}

function handleSearchInput() {
  if (searchKeyword.value.trim()) {
    searchResults.value = searchMajors(searchKeyword.value);
  } else {
    searchResults.value = [];
  }
}

function clearSearch() {
  searchKeyword.value = '';
  searchResults.value = [];
}

function selectMajor(major: MajorItem) {
  if (eventChannel) {
    eventChannel.emit('selectMajor', major.name);
  } else {
    const pages = getCurrentPages();
    if (pages.length >= 2) {
      const prevPage = pages[pages.length - 2];
      if (prevPage && (prevPage as any).$vm) {
        (prevPage as any).$vm.selectedMajor = major.name;
      }
    }
  }
  uni.navigateBack();
}
</script>

<style lang="scss" scoped>
@use "@/styles/variables.scss" as vars;

.major-selector-page {
  min-height: 100vh;
  background-color: #F2F2F7;
  display: flex;
  flex-direction: column;
}

.search-container {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  padding: 24rpx 32rpx;
  background-color: #FFFFFF;
  border-bottom: 1rpx solid #E5E5EA;
  z-index: 100;
}

.search-box {
  display: flex;
  align-items: center;
  background-color: #F2F2F7;
  border-radius: 20rpx;
  padding: 20rpx 24rpx;
}

.search-icon {
  font-size: 32rpx;
  color: #8E8E93;
  margin-right: 16rpx;
}

.search-input {
  flex: 1;
  font-size: 30rpx;
  color: #000000;
  padding: 0;
}

.search-placeholder {
  color: #8E8E93;
  font-size: 30rpx;
}

.clear-icon {
  width: 40rpx;
  height: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-left: 16rpx;
}

.clear-text {
  font-size: 28rpx;
  color: #8E8E93;
  line-height: 1;
}

.content {
  flex: 1;
  margin-top: 120rpx;
}

.category-section {
  background-color: #FFFFFF;
  margin-bottom: 16rpx;
}

.category-header {
  display: flex;
  align-items: center;
  padding: 32rpx;
  border-bottom: 1rpx solid #F2F2F7;
}

.category-name {
  font-size: 32rpx;
  font-weight: 600;
  color: #000000;
  margin-right: 16rpx;
}

.category-count {
  font-size: 28rpx;
  color: #8E8E93;
  flex: 1;
}

.expand-icon {
  font-size: 48rpx;
  color: #C6C6C8;
  font-weight: 300;
  transform: rotate(0deg);
  transition: transform 0.2s;
  
  &.expanded {
    transform: rotate(90deg);
  }
}

.majors-list {
  background-color: #FFFFFF;
}

.major-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 32rpx;
  border-bottom: 1rpx solid #F2F2F7;
  
  &:last-child {
    border-bottom: none;
  }
  
  &:active {
    background-color: #F2F2F7;
  }
}

.major-name {
  font-size: 30rpx;
  color: #000000;
  flex: 1;
}

.major-category2 {
  font-size: 26rpx;
  color: #8E8E93;
  margin-left: 16rpx;
}

.major-info {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.major-category1 {
  font-size: 26rpx;
  color: #8E8E93;
}

.empty-state {
  padding: 120rpx 32rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.empty-text {
  font-size: 28rpx;
  color: #8E8E93;
}
</style>

