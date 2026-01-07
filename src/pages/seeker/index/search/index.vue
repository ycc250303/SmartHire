<template>
  <view class="search-page">
    <view class="search-container">
      <view class="search-input-wrapper">
        <view class="search-input-box">
          <input
            class="search-input"
            v-model="searchKeyword"
            :placeholder="t('pages.search.placeholder')"
            @confirm="handleSearch"
            confirm-type="search"
          />
          <view class="search-clear" v-if="searchKeyword" @click="clearSearch">
            <text class="clear-icon">✕</text>
          </view>
        </view>
        <view class="search-button" @click="handleSearch">
          <text class="search-button-text">{{ t('pages.search.search') }}</text>
        </view>
      </view>

      <view class="filters-section">
        <view class="filter-group">
          <text class="filter-group-label">{{ t('pages.search.city') }}</text>
          <view class="filter-options">
            <view
              class="filter-option"
              :class="{ active: selectedCity === null }"
              @click="handleCitySelect(null)"
            >
              <text class="filter-option-text">{{ t('pages.search.all') }}</text>
            </view>
            <view
              class="filter-option"
              :class="{ active: isCitySelected(cityKey) }"
              v-for="cityKey in popularCityKeys"
              :key="cityKey"
              @click="handleCitySelect(cityKey)"
            >
              <text class="filter-option-text">{{ getCityName(cityKey) }}</text>
            </view>
            <view
              class="filter-option filter-option-other"
              :class="{ active: isOtherCitySelected() }"
              @click="handleCityPickerTrigger"
            >
              <text class="filter-option-text">{{ t('pages.search.other') }}</text>
            </view>
          </view>
        </view>

        <view class="filter-group">
          <text class="filter-group-label">{{ t('pages.search.jobType') }}</text>
          <view class="filter-options">
            <view
              class="filter-option"
              :class="{ active: selectedJobType === null }"
              @click="handleJobTypeSelect(null)"
            >
              <text class="filter-option-text">{{ t('pages.search.all') }}</text>
            </view>
            <view
              class="filter-option"
              :class="{ active: selectedJobType === 0 }"
              @click="handleJobTypeSelect(0)"
            >
              <text class="filter-option-text">{{ t('pages.jobs.fulltime') }}</text>
            </view>
            <view
              class="filter-option"
              :class="{ active: selectedJobType === 1 }"
              @click="handleJobTypeSelect(1)"
            >
              <text class="filter-option-text">{{ t('pages.jobs.internship') }}</text>
            </view>
          </view>
        </view>

        <view class="filter-group">
          <text class="filter-group-label">{{ t('pages.search.salary') }}</text>
          <view class="filter-options">
            <view
              class="filter-option"
              :class="{ active: !minSalary && !maxSalary }"
              @click="handleSalarySelect(null, null)"
            >
              <text class="filter-option-text">{{ t('pages.search.all') }}</text>
            </view>
            <view
              class="filter-option"
              :class="{ active: minSalary === 5000 && maxSalary === 10000 }"
              @click="handleSalarySelect(5000, 10000)"
            >
              <text class="filter-option-text">5K-10K</text>
            </view>
            <view
              class="filter-option"
              :class="{ active: minSalary === 10000 && maxSalary === 15000 }"
              @click="handleSalarySelect(10000, 15000)"
            >
              <text class="filter-option-text">10K-15K</text>
            </view>
            <view
              class="filter-option"
              :class="{ active: minSalary === 15000 && maxSalary === 20000 }"
              @click="handleSalarySelect(15000, 20000)"
            >
              <text class="filter-option-text">15K-20K</text>
            </view>
            <view
              class="filter-option"
              :class="{ active: minSalary === 20000 && maxSalary === 30000 }"
              @click="handleSalarySelect(20000, 30000)"
            >
              <text class="filter-option-text">20K-30K</text>
            </view>
            <view
              class="filter-option"
              :class="{ active: minSalary === 30000 && maxSalary === 50000 }"
              @click="handleSalarySelect(30000, 50000)"
            >
              <text class="filter-option-text">30K-50K</text>
            </view>
            <view
              class="filter-option"
              :class="{ active: minSalary === 50000 && maxSalary === null }"
              @click="handleSalarySelect(50000, null)"
            >
              <text class="filter-option-text">50K+</text>
            </view>
          </view>
        </view>

        <view class="filter-group">
          <text class="filter-group-label">{{ t('pages.search.education') }}</text>
          <view class="filter-options">
            <view
              class="filter-option"
              :class="{ active: selectedEducation === null }"
              @click="handleEducationSelect(null)"
            >
              <text class="filter-option-text">{{ t('pages.search.all') }}</text>
            </view>
            <view
              class="filter-option"
              :class="{ active: selectedEducation === 0 }"
              @click="handleEducationSelect(0)"
            >
              <text class="filter-option-text">{{ t('degree.highSchoolOrBelow') }}</text>
            </view>
            <view
              class="filter-option"
              :class="{ active: selectedEducation === 1 }"
              @click="handleEducationSelect(1)"
            >
              <text class="filter-option-text">{{ t('degree.associateDegree') }}</text>
            </view>
            <view
              class="filter-option"
              :class="{ active: selectedEducation === 2 }"
              @click="handleEducationSelect(2)"
            >
              <text class="filter-option-text">{{ t('degree.bachelorDegree') }}</text>
            </view>
            <view
              class="filter-option"
              :class="{ active: selectedEducation === 3 }"
              @click="handleEducationSelect(3)"
            >
              <text class="filter-option-text">{{ t('degree.masterDegree') }}</text>
            </view>
            <view
              class="filter-option"
              :class="{ active: selectedEducation === 4 }"
              @click="handleEducationSelect(4)"
            >
              <text class="filter-option-text">{{ t('degree.doctorDegree') }}</text>
            </view>
          </view>
        </view>
      </view>

      <view class="results-section">
        <view v-if="loading" class="loading-state">
          <text class="loading-text">{{ t('pages.search.loading') }}</text>
        </view>
        <view v-else-if="error" class="error-state">
          <text class="error-text">{{ error }}</text>
          <view class="retry-button" @click="handleSearch">
            <text class="retry-text">{{ t('pages.search.retry') }}</text>
          </view>
        </view>
        <view v-else-if="jobs.length === 0 && hasSearched" class="empty-state">
          <text class="empty-text">{{ t('pages.search.noResults') }}</text>
        </view>
        <view v-else-if="jobs.length > 0" class="job-list">
          <SearchJobCard
            v-for="job in jobs"
            :key="job.jobId"
            :job="job"
          />
        </view>
        <view v-else class="empty-state">
          <text class="empty-text">{{ t('pages.search.placeholder') }}</text>
        </view>
      </view>
    </view>

    <view class="city-picker-hidden">
      <CityPicker
        v-model="cityPickerValue"
        :placeholder="t('pages.search.selectCity')"
        @update:modelValue="handleCityPickerChange"
      />
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { t } from '@/locales';
import { searchJobPositions, type JobSearchParams, type JobSearchResult } from '@/services/api/recruitment';
import SearchJobCard from '@/components/common/SearchJobCard.vue';
import CityPicker from '@/components/common/CityPicker.vue';
import { useCityData } from '@/utils/useCityData';

onLoad(() => {
  uni.setNavigationBarTitle({
    title: t('navigation.search')
  });
});

const { getCities, parseCityString } = useCityData();

const searchKeyword = ref('');
const selectedCity = ref<string | null>(null);
const selectedJobType = ref<number | null>(null);
const selectedEducation = ref<number | null>(null);
const minSalary = ref<number | null>(null);
const maxSalary = ref<number | null>(null);

const jobs = ref<JobSearchResult[]>([]);
const loading = ref(false);
const error = ref<string | null>(null);
const hasSearched = ref(false);

const cityPickerValue = ref<string | null>(null);
const showCityPicker = ref(false);

const popularCityKeys = ['beijing', 'shanghai', 'guangzhou', 'shenzhen', 'hangzhou', 'chengdu', 'nanjing', 'wuhan'];

const cityKeyToNameMap: Record<string, string> = {
  beijing: '北京',
  shanghai: '上海',
  guangzhou: '广州',
  shenzhen: '深圳',
  hangzhou: '杭州',
  chengdu: '成都',
  nanjing: '南京',
  wuhan: '武汉',
};

function clearSearch() {
  searchKeyword.value = '';
  jobs.value = [];
  hasSearched.value = false;
}

function handleCitySelect(cityKey: string | null) {
  if (cityKey === null) {
    selectedCity.value = null;
  } else if (popularCityKeys.includes(cityKey)) {
    selectedCity.value = cityKeyToNameMap[cityKey] || cityKey;
  }
}

function getCityName(cityKey: string): string {
  const keyPath = `pages.search.cities.${cityKey}`;
  const result = t(keyPath);
  if (result === keyPath || !result) {
    const fallback = cityKeyToNameMap[cityKey];
    if (fallback) {
      return fallback;
    }
    console.warn(`Translation not found for: ${keyPath}`);
    return cityKey;
  }
  return result;
}

function isCitySelected(cityKey: string): boolean {
  if (!selectedCity.value) return false;
  const cityName = cityKeyToNameMap[cityKey];
  return selectedCity.value === cityName;
}

function isOtherCitySelected(): boolean {
  if (!selectedCity.value) return false;
  const cityValues = Object.values(cityKeyToNameMap);
  return !cityValues.includes(selectedCity.value);
}

function handleCityPickerTrigger() {
  const currentCityValue = selectedCity.value;
  if (currentCityValue && !isOtherCitySelected()) {
    cityPickerValue.value = null;
  } else {
    cityPickerValue.value = currentCityValue;
  }
  showCityPicker.value = true;
  setTimeout(() => {
    const query = uni.createSelectorQuery();
    query.select('.city-picker-hidden .picker-trigger').boundingClientRect();
    query.exec((res) => {
      if (res && res[0]) {
        const trigger = uni.createSelectorQuery().select('.city-picker-hidden .picker-trigger');
        trigger.node((nodeRes: any) => {
          if (nodeRes && nodeRes.node) {
            nodeRes.node.click();
          }
        }).exec();
      }
    });
  }, 100);
}

function handleCityPickerChange(value: string | null) {
  if (value) {
    const parsed = parseCityString(value);
    if (parsed) {
      const cities = getCities(parsed.provinceIndex);
      if (cities && cities[parsed.cityIndex]) {
        const city = cities[parsed.cityIndex];
        if (city && city.name) {
          selectedCity.value = city.name;
        }
      }
    }
  } else {
    selectedCity.value = null;
  }
  cityPickerValue.value = value;
  showCityPicker.value = false;
}

function handleJobTypeSelect(jobType: number | null) {
  selectedJobType.value = jobType;
}

function handleSalarySelect(min: number | null, max: number | null) {
  minSalary.value = min;
  maxSalary.value = max;
}

function handleEducationSelect(education: number | null) {
  selectedEducation.value = education;
}

async function handleSearch() {
  if (!searchKeyword.value.trim() && !selectedCity.value && selectedJobType.value === null && 
      selectedEducation.value === null && !minSalary.value && !maxSalary.value) {
    uni.showToast({
      title: t('pages.search.enterKeyword'),
      icon: 'none'
    });
    return;
  }

  loading.value = true;
  error.value = null;
  hasSearched.value = true;

  try {
    const params: JobSearchParams = {
      keyword: searchKeyword.value.trim() || undefined,
      city: selectedCity.value || undefined,
      jobType: selectedJobType.value ?? undefined,
      educationRequired: selectedEducation.value ?? undefined,
      minSalary: minSalary.value ?? undefined,
      maxSalary: maxSalary.value ?? undefined,
      page: 1,
      size: 20,
    };

    const results = await searchJobPositions(params);
    jobs.value = results;
  } catch (err) {
    error.value = err instanceof Error ? err.message : t('pages.search.loadError');
    jobs.value = [];
  } finally {
    loading.value = false;
  }
}
</script>

<style lang="scss" scoped>
@use "@/styles/variables.scss" as vars;

.search-page {
  min-height: 100vh;
  background: vars.$surface-color;
  padding-top: calc(var(--status-bar-height) + vars.$spacing-lg);
}

.search-container {
  padding: vars.$spacing-md;
}

.search-input-wrapper {
  display: flex;
  gap: vars.$spacing-sm;
  margin-bottom: vars.$spacing-lg;
}

.search-input-box {
  flex: 1;
  display: flex;
  align-items: center;
  background-color: #f5f5f5;
  border-radius: vars.$border-radius;
  padding: vars.$spacing-sm vars.$spacing-md;
  gap: vars.$spacing-sm;
}

.search-input {
  flex: 1;
  font-size: 30rpx;
  color: vars.$text-color;
  background: transparent;
  border: none;
}

.search-clear {
  padding: 4rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.clear-icon {
  font-size: 28rpx;
  color: vars.$text-muted;
}

.search-button {
  padding: vars.$spacing-sm vars.$spacing-lg;
  background-color: vars.$primary-color;
  border-radius: vars.$border-radius;
  display: flex;
  align-items: center;
  justify-content: center;
}

.search-button-text {
  font-size: 30rpx;
  color: #ffffff;
  font-weight: 500;
}

.filters-section {
  background-color: vars.$surface-color;
  border-radius: vars.$border-radius;
  padding: vars.$spacing-lg;
  margin-bottom: vars.$spacing-lg;
}

.filter-group {
  margin-bottom: vars.$spacing-xl;
  
  &:last-child {
    margin-bottom: 0;
  }
}

.filter-group-label {
  display: block;
  font-size: 28rpx;
  font-weight: 600;
  color: vars.$text-color;
  margin-bottom: vars.$spacing-md;
}

.filter-options {
  display: flex;
  flex-wrap: wrap;
  gap: vars.$spacing-sm;
}

.filter-option {
  padding: vars.$spacing-sm vars.$spacing-md;
  background-color: #f5f5f5;
  border-radius: vars.$border-radius-sm;
  border: 2rpx solid transparent;
  transition: all 0.2s;
}

.filter-option.active {
  background-color: vars.$primary-color-soft;
  border-color: vars.$primary-color;
}

.filter-option-text {
  font-size: 26rpx;
  color: vars.$text-color;
}

.filter-option.active .filter-option-text {
  color: vars.$primary-color;
  font-weight: 600;
}

.filter-option-other {
  border-style: dashed;
}

.results-section {
  min-height: 400rpx;
}

.loading-state,
.error-state,
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: vars.$spacing-xl;
  min-height: 400rpx;
}

.loading-text,
.error-text,
.empty-text {
  font-size: 28rpx;
  color: vars.$text-muted;
}

.error-text {
  color: #ff6b6b;
  margin-bottom: vars.$spacing-md;
}

.retry-button {
  padding: vars.$spacing-sm vars.$spacing-lg;
  background-color: vars.$primary-color;
  border-radius: vars.$border-radius;
}

.retry-text {
  font-size: 28rpx;
  color: #ffffff;
}

.job-list {
  display: flex;
  flex-direction: column;
}

.city-picker-hidden {
  position: fixed;
  top: -9999px;
  left: -9999px;
  opacity: 0;
  pointer-events: none;
  z-index: -1;
}
</style>
