<template>
  <view class="custom-tab-bar" v-if="currentRole !== null">
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
import { useUserStore } from '@/store/user';
import { UserType } from '@/services/api/auth';

const messages = {
  'zh-CN': zhCN,
  'en-US': enUS
};

const localeRef = getLocaleRef();
const userStore = useUserStore();

const currentIndex = ref(0);
const currentRole = ref<'seeker' | 'hr' | null>(null);

const seekerTabList = computed(() => {
  const localeMessages = messages[localeRef.value];
  return [
    {
      pagePath: 'pages/seeker/index/index',
      text: localeMessages.tabBar.jobs,
      iconPath: '/static/tab-jobs.png',
      selectedIconPath: '/static/tab-jobs-active.png'
    },
    {
      pagePath: 'pages/seeker/chat/index',
      text: localeMessages.tabBar.chat,
      iconPath: '/static/tab-chat.png',
      selectedIconPath: '/static/tab-chat-active.png'
    },
    {
      pagePath: 'pages/seeker/profile/index',
      text: localeMessages.tabBar.profile,
      iconPath: '/static/tab-profile.png',
      selectedIconPath: '/static/tab-profile-active.png'
    }
  ];
});

const hrTabList = computed(() => {
  const localeMessages = messages[localeRef.value];
  return [
    {
      pagePath: 'pages/hr/hr/home/index',
      text: '工作台',
      iconPath: '/static/tab-home.png',
      selectedIconPath: '/static/tab-home-active.png'
    },
    {
      pagePath: 'pages/hr/hr/jobs/index',
      text: '岗位',
      iconPath: '/static/tab-jobs.png',
      selectedIconPath: '/static/tab-jobs-active.png'
    },
    {
      pagePath: 'pages/hr/hr/messages/index',
      text: '消息',
      iconPath: '/static/tab-chat.png',
      selectedIconPath: '/static/tab-chat-active.png'
    }
  ];
});

const tabList = computed(() => {
  if (currentRole.value === 'hr') {
    return hrTabList.value;
  } else {
    return seekerTabList.value;
  }
});

const tabPages = computed(() => {
  if (currentRole.value === 'hr') {
    return [
      'pages/hr/hr/home/index',
      'pages/hr/hr/jobs/index',
      'pages/hr/hr/messages/index'
    ];
  } else {
    return [
      'pages/seeker/index/index',
      'pages/seeker/chat/index',
      'pages/seeker/profile/index'
    ];
  }
});

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
  
  // Determine role based on user's actual role (userType)
  // UserType: Seeker = 1, HR = 2
  // userType is number, UserType is enum
  const userRole = userStore.userInfo?.userType;
  
  if (userRole === UserType.HR || userRole === 2) {
    currentRole.value = 'hr';
    // Prevent HR from accessing seeker pages
    if (route.startsWith('pages/seeker/')) {
      uni.reLaunch({
        url: '/pages/hr/hr/home/index'
      });
      return;
    }
  } else if (userRole === UserType.Seeker || userRole === 1) {
    currentRole.value = 'seeker';
    // Prevent Seeker from accessing HR pages
    if (route.startsWith('pages/hr/hr/') || route.startsWith('pages/hr/')) {
      uni.reLaunch({
        url: '/pages/seeker/index/index'
      });
      return;
    }
  } else {
    // Fallback: determine role based on route if userInfo not loaded yet
    if (route.startsWith('pages/hr/hr/') || route.startsWith('pages/hr/')) {
      currentRole.value = 'hr';
    } else if (route.startsWith('pages/seeker/')) {
      currentRole.value = 'seeker';
    } else {
      currentRole.value = null;
      return;
    }
  }
  
  const index = tabPages.value.findIndex(page => page === route);
  
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

// Watch userInfo changes to update role
watch(() => userStore.userInfo, () => {
  updateSelectedIndex();
}, { deep: true });
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
