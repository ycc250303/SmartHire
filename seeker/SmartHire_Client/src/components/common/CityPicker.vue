<template>
  <view class="city-picker">
    <view class="picker-trigger" @tap="openPicker">
      <text :class="displayValue ? 'picker-text' : 'picker-placeholder'">
        {{ displayValue || placeholder }}
      </text>
      <text class="picker-arrow">›</text>
    </view>

    <view v-if="showPicker" class="picker-mask" @tap="closePicker">
      <view class="picker-container" @tap.stop>
        <view class="picker-header">
          <view class="picker-cancel" @tap="closePicker">
            <text class="picker-button-text">{{ t('common.cancel') }}</text>
          </view>
          <view class="picker-title">
            <text class="picker-title-text">{{ t('common.selectCity') }}</text>
          </view>
          <view class="picker-confirm" @tap="handleConfirm">
            <text class="picker-button-text confirm-text">{{ t('common.confirm') }}</text>
          </view>
        </view>

        <view class="picker-content">
          <!-- Center indicator line -->
          <view class="picker-indicator"></view>

          <scroll-view
            class="picker-column"
            scroll-y
            :scroll-top="provinceScrollTop"
            :scroll-with-animation="true"
            @scroll="handleProvinceScroll"
          >
            <view class="picker-padding"></view>
            <view
              v-for="(province, index) in provinces"
              :key="index"
              :id="`province-${index}`"
              class="picker-item"
              @tap="handleProvinceClick(index)"
            >
              <text class="picker-item-text">{{ province.name }}</text>
            </view>
            <view class="picker-padding"></view>
          </scroll-view>

          <scroll-view
            class="picker-column"
            scroll-y
            :scroll-top="cityScrollTop"
            :scroll-with-animation="true"
            @scroll="handleCityScroll"
          >
            <view class="picker-padding"></view>
            <view
              v-for="(city, index) in cities"
              :key="index"
              :id="`city-${index}`"
              class="picker-item"
              @tap="handleCityClick(index)"
            >
              <text class="picker-item-text">{{ city.name }}</text>
            </view>
            <view class="picker-padding"></view>
          </scroll-view>

          <scroll-view
            class="picker-column"
            scroll-y
            :scroll-top="districtScrollTop"
            :scroll-with-animation="true"
            @scroll="handleDistrictScroll"
          >
            <view class="picker-padding"></view>
            <view
              v-for="(district, index) in districts"
              :key="index"
              :id="`district-${index}`"
              class="picker-item"
              @tap="handleDistrictClick(index)"
            >
              <text class="picker-item-text">{{ district.name }}</text>
            </view>
            <view class="picker-padding"></view>
          </scroll-view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick, onBeforeUnmount } from 'vue';
import { t } from '@/locales';
import { useCityData, type CityIndex, type CityItem, type DistrictItem } from '@/utils/useCityData';

interface Props {
  modelValue?: string | null;
  placeholder?: string;
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: null,
  placeholder: '',
});

const emit = defineEmits<{
  'update:modelValue': [value: string | null];
}>();

const { getProvinces, getCities, getDistricts, parseCityString, getCitySelectionFromIndex, formatCityString } =
  useCityData();

const provinces = ref(getProvinces());
const cities = ref<CityItem[]>([]);
const districts = ref<DistrictItem[]>([]);

const showPicker = ref(false);

const selectedIndex = ref<CityIndex>({
  provinceIndex: 0,
  cityIndex: 0,
  districtIndex: 0,
});

const provinceScrollTop = ref(0);
const cityScrollTop = ref(0);
const districtScrollTop = ref(0);

// Flag to ignore scroll events during programmatic scrolling
const isProgrammaticScroll = ref(false);
let programmaticScrollTimer: ReturnType<typeof setTimeout> | null = null;

type ColumnKey = 'province' | 'city' | 'district';

const scrollTimers: Record<ColumnKey, ReturnType<typeof setTimeout> | null> = {
  province: null,
  city: null,
  district: null,
};

function rpxToPx(value: number) {
  if (typeof uni !== 'undefined' && typeof uni.upx2px === 'function') {
    return uni.upx2px(value);
  }
  return value;
}

const ITEM_HEIGHT_RPX = 88;
// SCROLL_PADDING_RPX is NOT needed for calculation if we want item at index to be centered.
// With 256rpx padding at top, scrollTop=0 means Item 0 is centered (256rpx offset matches ~half view height).
// So scrollTop = index * ITEM_HEIGHT.
const ITEM_HEIGHT = rpxToPx(ITEM_HEIGHT_RPX);
const SCROLL_DEBOUNCE = 100;

const displayValue = computed(() => {
  if (!props.modelValue) return '';
  const parts = props.modelValue.split('-');
  if (parts.length === 3) {
    return `${parts[0]} ${parts[1]} ${parts[2]}`;
  }
  return props.modelValue;
});

function clampIndex(index: number, length: number) {
  if (length <= 0) {
    return 0;
  }
  return Math.max(0, Math.min(index, length - 1));
}

function refreshDistricts(resetDistrictIndex = false) {
  const nextDistricts =
    cities.value.length > 0
      ? getDistricts(selectedIndex.value.provinceIndex, selectedIndex.value.cityIndex) || []
      : [];

  districts.value = nextDistricts;

  if (!nextDistricts.length) {
    selectedIndex.value.districtIndex = 0;
    return;
  }

  const nextDistrictIndex = resetDistrictIndex
    ? 0
    : clampIndex(selectedIndex.value.districtIndex, nextDistricts.length);

  selectedIndex.value.districtIndex = nextDistrictIndex;
}

function refreshCities(resetCityIndex = false) {
  selectedIndex.value.provinceIndex = clampIndex(selectedIndex.value.provinceIndex, provinces.value.length);

  const nextCities = getCities(selectedIndex.value.provinceIndex) || [];
  cities.value = nextCities;

  if (!nextCities.length) {
    selectedIndex.value.cityIndex = 0;
    refreshDistricts(true);
    return;
  }

  const nextCityIndex = resetCityIndex ? 0 : clampIndex(selectedIndex.value.cityIndex, nextCities.length);
  const cityChanged = nextCityIndex !== selectedIndex.value.cityIndex;
  selectedIndex.value.cityIndex = nextCityIndex;

  refreshDistricts(resetCityIndex || cityChanged);
}

// Set scroll positions based on current selection indexes
function alignScrollToSelection(columns: ColumnKey[] = ['province', 'city', 'district']) {
  // Set flag to prevent scroll event from interfering
  isProgrammaticScroll.value = true;
  if (programmaticScrollTimer) clearTimeout(programmaticScrollTimer);
  // Allow some time for scroll animation to complete
  programmaticScrollTimer = setTimeout(() => {
    isProgrammaticScroll.value = false;
  }, 300);

  nextTick(() => {
    columns.forEach((column) => {
      const index =
        column === 'province'
          ? selectedIndex.value.provinceIndex
          : column === 'city'
          ? selectedIndex.value.cityIndex
          : selectedIndex.value.districtIndex;

      // Simple formula: Index * ItemHeight
      // Because of the top padding, scrollTop=0 places the first item in the center.
      const offset = Math.max(index, 0) * ITEM_HEIGHT;

      if (column === 'province') {
        provinceScrollTop.value = offset;
      } else if (column === 'city') {
        cityScrollTop.value = offset;
      } else {
        districtScrollTop.value = offset;
      }
    });
  });
}

// Calculate index from scrollTop
function getIndexFromScroll(scrollTop: number, listLength: number) {
  if (listLength <= 0) {
    return 0;
  }
  // Just round the scrollTop / ITEM_HEIGHT
  const approxIndex = Math.round(Math.max(0, scrollTop) / ITEM_HEIGHT);
  return clampIndex(approxIndex, listLength);
}

function openPicker() {
  const parsed = props.modelValue ? parseCityString(props.modelValue) : null;

  if (parsed) {
    selectedIndex.value = { ...parsed };
    refreshCities(false);
  } else {
    selectedIndex.value = {
      provinceIndex: 0,
      cityIndex: 0,
      districtIndex: 0,
    };
    refreshCities(true);
  }

  showPicker.value = true;
  // Align immediately
  alignScrollToSelection();
}

function closePicker() {
  showPicker.value = false;
}

function handleProvinceClick(index: number) {
  // Always re-align on click to ensure snapping even if index hasn't changed
  const changed = index !== selectedIndex.value.provinceIndex;
  selectedIndex.value.provinceIndex = index;
  
  if (changed) {
    refreshCities(true);
    alignScrollToSelection(['province', 'city', 'district']);
  } else {
    alignScrollToSelection(['province']);
  }
}

function handleCityClick(index: number) {
  const changed = index !== selectedIndex.value.cityIndex;
  selectedIndex.value.cityIndex = index;

  if (changed) {
    refreshDistricts(true);
    alignScrollToSelection(['city', 'district']);
  } else {
    alignScrollToSelection(['city']);
  }
}

function handleDistrictClick(index: number) {
  selectedIndex.value.districtIndex = index;
  alignScrollToSelection(['district']);
}

// Generic scroll handler
function handleScroll(e: any, column: ColumnKey) {
  if (isProgrammaticScroll.value) return;

  const scrollTop = Math.max(0, e.detail?.scrollTop || 0);
  
  if (scrollTimers[column]) clearTimeout(scrollTimers[column]!);

  scrollTimers[column] = setTimeout(() => {
    // User stopped scrolling, snap to nearest
    let listLength = 0;
    if (column === 'province') listLength = provinces.value.length;
    else if (column === 'city') listLength = cities.value.length;
    else if (column === 'district') listLength = districts.value.length;

    const newIndex = getIndexFromScroll(scrollTop, listLength);
    
    if (column === 'province') {
      if (newIndex !== selectedIndex.value.provinceIndex) {
        selectedIndex.value.provinceIndex = newIndex;
        refreshCities(true);
        alignScrollToSelection(['province', 'city', 'district']);
      } else {
        // Snap back if same index (e.g. partial scroll)
        alignScrollToSelection(['province']);
      }
    } else if (column === 'city') {
      if (newIndex !== selectedIndex.value.cityIndex) {
        selectedIndex.value.cityIndex = newIndex;
        refreshDistricts(true);
        alignScrollToSelection(['city', 'district']);
      } else {
        alignScrollToSelection(['city']);
      }
    } else if (column === 'district') {
      if (newIndex !== selectedIndex.value.districtIndex) {
        selectedIndex.value.districtIndex = newIndex;
      }
      alignScrollToSelection(['district']);
    }
  }, SCROLL_DEBOUNCE);
}

function handleProvinceScroll(e: any) {
  handleScroll(e, 'province');
}

function handleCityScroll(e: any) {
  handleScroll(e, 'city');
}

function handleDistrictScroll(e: any) {
  handleScroll(e, 'district');
}

function handleConfirm() {
  const selection = getCitySelectionFromIndex(selectedIndex.value);
  if (selection) {
    const cityString = formatCityString(selection);
    emit('update:modelValue', cityString);
  }
  closePicker();
}

watch(
  () => props.modelValue,
  (newValue) => {
    if (!newValue && showPicker.value) {
      // Reset if cleared while open
      selectedIndex.value = {
        provinceIndex: 0,
        cityIndex: 0,
        districtIndex: 0,
      };
      refreshCities(true);
      alignScrollToSelection();
    }
  }
);

onBeforeUnmount(() => {
  if (programmaticScrollTimer) clearTimeout(programmaticScrollTimer);
  (Object.keys(scrollTimers) as ColumnKey[]).forEach((key) => {
    const timer = scrollTimers[key];
    if (timer) {
      clearTimeout(timer);
    }
  });
});
</script>

<style lang="scss" scoped>
@use "@/styles/variables.scss" as vars;

.city-picker {
  width: 100%;
}

.picker-trigger {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.picker-text {
  font-size: 30rpx;
  color: #000000;
  flex: 1;
}

.picker-placeholder {
  font-size: 30rpx;
  color: #C7C7CC;
  flex: 1;
}

.picker-arrow {
  font-size: 48rpx;
  color: #C6C6C8;
  font-weight: 300;
  margin-left: 16rpx;
}

.picker-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.4);
  z-index: 9999;
  display: flex;
  align-items: flex-end;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.picker-container {
  width: 100%;
  background-color: #FFFFFF;
  border-radius: 32rpx 32rpx 0 0;
  max-height: 80vh;
  display: flex;
  flex-direction: column;
  animation: slideUp 0.3s ease;
}

@keyframes slideUp {
  from {
    transform: translateY(100%);
  }
  to {
    transform: translateY(0);
  }
}

.picker-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx 32rpx;
  border-bottom: 1rpx solid #F2F2F7;
}

.picker-cancel,
.picker-confirm {
  min-width: 120rpx;
  padding: 8rpx 16rpx;
}

.picker-button-text {
  font-size: 32rpx;
  color: #8E8E93;
}

.confirm-text {
  color: vars.$primary-color;
  font-weight: 600;
}

.picker-title {
  flex: 1;
  text-align: center;
}

.picker-title-text {
  font-size: 34rpx;
  font-weight: 600;
  color: #000000;
}

.picker-content {
  display: flex;
  overflow: hidden;
  height: 600rpx;
  position: relative;
}

.picker-indicator {
  position: absolute;
  left: 0;
  right: 0;
  top: 50%;
  transform: translateY(-50%);
  height: 88rpx;
  background-color: rgba(75, 163, 255, 0.08);
  border-top: 1rpx solid rgba(75, 163, 255, 0.2);
  border-bottom: 1rpx solid rgba(75, 163, 255, 0.2);
  pointer-events: none;
  z-index: 1;
}

.picker-column {
  flex: 1;
  height: 600rpx;
  position: relative;
}

.picker-padding {
  height: 256rpx;
  flex-shrink: 0;
}

.picker-item {
  padding: 24rpx 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 88rpx;
  box-sizing: border-box;
}

.picker-item-text {
  font-size: 26rpx;
  color: #666666;
  transition: all 0.2s;
  text-align: center;
  line-height: 36rpx;
}
</style>
