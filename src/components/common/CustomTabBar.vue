<template>
  <view class="custom-tab-bar">
    <view 
      v-for="(item, index) in tabList" 
      :key="index"
      class="tab-item"
      :class="{ active: currentIndex === index }"
      @click="switchTab(index, item.pagePath)"
    >
      <image 
        class="tab-icon"
        :src="currentIndex === index ? item.selectedIconPath : item.iconPath"
        mode="aspectFit"
      />
      <text class="tab-text">{{ item.text }}</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import { getLocaleRef } from '@/locales';
import zhCN from '@/locales/zh-CN';
import enUS from '@/locales/en-US';

const messages = {
  'zh-CN': zhCN,
  'en-US': enUS
};

const localeRef = getLocaleRef();

const currentIndex = ref(0);

const tabList = computed(() => {
  const localeMessages = messages[localeRef.value];
  return [
    {
      pagePath: 'pages/index/index',
      text: localeMessages.tabBar.jobs,
      iconPath: '/static/tab-jobs.png',
      selectedIconPath: '/static/tab-jobs-active.png'
    },
    {
      pagePath: 'pages/chat/index',
      text: localeMessages.tabBar.chat,
      iconPath: '/static/tab-chat.png',
      selectedIconPath: '/static/tab-chat-active.png'
    },
    {
      pagePath: 'pages/profile/index',
      text: localeMessages.tabBar.profile,
      iconPath: '/static/tab-profile.png',
      selectedIconPath: '/static/tab-profile-active.png'
    }
  ];
});

const tabPages = [
  'pages/index/index',
  'pages/chat/index',
  'pages/profile/index'
];

function switchTab(index: number, url: string) {
  if (currentIndex.value === index) {
    return;
  }
  
  currentIndex.value = index;
  uni.switchTab({
    url: '/' + url
  });
}

function updateSelectedIndex() {
  const pages = getCurrentPages();
  if (pages.length === 0) return;
  
  const currentPage = pages[pages.length - 1];
  if (!currentPage || !currentPage.route) return;
  
  const route = currentPage.route;
  const index = tabPages.findIndex(page => page === route);
  
  if (index !== -1) {
    currentIndex.value = index;
  }
}

onMounted(() => {
  updateSelectedIndex();
});

onShow(() => {
  updateSelectedIndex();
});

watch(localeRef, () => {
  updateSelectedIndex();
});
</script>

<style lang="scss" scoped>
@use "@/styles/variables.scss" as vars;

.custom-tab-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  justify-content: space-around;
  align-items: center;
  height: 100rpx;
  background-color: vars.$surface-color;
  border-top: 1rpx solid vars.$light-blue;
  padding-bottom: env(safe-area-inset-bottom);
  z-index: 999;
}

.tab-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  flex: 1;
  cursor: pointer;
  transition: all 0.3s;
}

.tab-icon {
  width: 48rpx;
  height: 48rpx;
  margin-bottom: vars.$spacing-xs;
}

.tab-text {
  font-size: 22rpx;
  color: vars.$text-muted;
  transition: color 0.3s;
}

.tab-item.active .tab-text {
  color: vars.$primary-color;
  font-weight: 500;
}
</style>

