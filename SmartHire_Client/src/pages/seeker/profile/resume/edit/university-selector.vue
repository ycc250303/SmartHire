<template>
  <view class="university-selector-page">
    <view class="search-container">
      <view class="search-box">
        <text class="search-icon">🔍</text>
        <input
          class="search-input"
          v-model="searchKeyword"
          :placeholder="t('pages.resume.edit.university.searchPlaceholder')"
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
        v-for="(province, provinceIndex) in provinces"
        :key="provinceIndex"
        class="province-section"
      >
        <view class="province-header" @tap="toggleProvince(provinceIndex)">
          <text class="province-name">{{ province.name }}</text>
          <text class="province-count">({{ province.universities.length }})</text>
          <text class="expand-icon" :class="{ expanded: expandedProvinces.has(provinceIndex) }">
            ›
          </text>
        </view>
        <view
          v-if="expandedProvinces.has(provinceIndex)"
          class="universities-list"
        >
          <view
            v-for="(university, uniIndex) in province.universities"
            :key="uniIndex"
            class="university-item"
            @tap="selectUniversity(university)"
          >
            <text class="university-name">{{ university.name }}</text>
            <text class="university-level">{{ university.level }}</text>
          </view>
        </view>
      </view>
    </scroll-view>

    <scroll-view v-else class="content" scroll-y>
      <view v-if="searchResults.length === 0" class="empty-state">
        <text class="empty-text">{{ t('pages.resume.edit.university.noResults') }}</text>
      </view>
      <view
        v-for="(university, index) in searchResults"
        :key="index"
        class="university-item"
        @tap="selectUniversity(university)"
      >
        <text class="university-name">{{ university.name }}</text>
        <view class="university-info">
          <text class="university-level">{{ university.level }}</text>
          <text class="university-city">{{ university.city }}</text>
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
import { useUniversityData, type UniversityItem } from '@/utils/useUniversityData';

useNavigationTitle('navigation.selectUniversity');

const { getProvinces, searchUniversities } = useUniversityData();

const provinces = ref(getProvinces());
const searchKeyword = ref('');
const expandedProvinces = ref<Set<number>>(new Set());
const searchResults = ref<UniversityItem[]>([]);
let eventChannel: any = null;

onLoad(() => {
  // Get event channel from current page instance
  const pages = getCurrentPages();
  const currentPage = pages[pages.length - 1];
  if (currentPage && typeof (currentPage as any).getOpenerEventChannel === 'function') {
    eventChannel = (currentPage as any).getOpenerEventChannel();
  }
});


function toggleProvince(provinceIndex: number) {
  if (expandedProvinces.value.has(provinceIndex)) {
    expandedProvinces.value.delete(provinceIndex);
  } else {
    expandedProvinces.value.add(provinceIndex);
  }
  expandedProvinces.value = new Set(expandedProvinces.value);
}

function handleSearchInput() {
  if (searchKeyword.value.trim()) {
    searchResults.value = searchUniversities(searchKeyword.value);
  } else {
    searchResults.value = [];
  }
}

function clearSearch() {
  searchKeyword.value = '';
  searchResults.value = [];
}

function selectUniversity(university: UniversityItem) {
  if (eventChannel) {
    eventChannel.emit('selectUniversity', university.name);
  } else {
    const pages = getCurrentPages();
    if (pages.length >= 2) {
      const prevPage = pages[pages.length - 2];
      if (prevPage && (prevPage as any).$vm) {
        (prevPage as any).$vm.selectedUniversity = university.name;
      }
    }
  }
  uni.navigateBack();
}
</script>

<style lang="scss" scoped>
@use "@/styles/variables.scss" as vars;

.university-selector-page {
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

.province-section {
  background-color: #FFFFFF;
  margin-bottom: 16rpx;
}

.province-header {
  display: flex;
  align-items: center;
  padding: 32rpx;
  border-bottom: 1rpx solid #F2F2F7;
}

.province-name {
  font-size: 32rpx;
  font-weight: 600;
  color: #000000;
  margin-right: 16rpx;
}

.province-count {
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

.universities-list {
  background-color: #FFFFFF;
}

.university-item {
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

.university-name {
  font-size: 30rpx;
  color: #000000;
  flex: 1;
}

.university-level {
  font-size: 26rpx;
  color: #8E8E93;
  margin-left: 16rpx;
}

.university-info {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.university-city {
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

