<template>
  <view class="chat-page">
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
import CustomTabBar from '@/components/common/CustomTabBar.vue';
import MessagesComponent from './messages.vue';
import NotificationsComponent from './notifications.vue';

type CategoryType = 'messages' | 'notifications';

const currentCategory = ref<CategoryType>('messages');

const categories = computed(() => [
  { value: 'messages' as CategoryType, label: t('pages.chat.messages') },
  { value: 'notifications' as CategoryType, label: t('pages.chat.notifications') },
]);

const currentComponent = computed(() => {
  switch (currentCategory.value) {
    case 'messages':
      return MessagesComponent;
    case 'notifications':
      return NotificationsComponent;
    default:
      return MessagesComponent;
  }
});

const handleCategoryChange = (category: CategoryType) => {
  currentCategory.value = category;
};

onLoad(() => {
  uni.setNavigationBarTitle({
    title: t('navigation.chat')
  });
});

onShow(() => {
  uni.hideTabBar();
});
</script>

<style lang="scss" scoped>
@use "@/styles/variables.scss" as vars;

.chat-page {
  min-height: 100vh;
  background: vars.$surface-color;
  display: flex;
  flex-direction: column;
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
  justify-content: flex-start;
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
  font-size: 40rpx;
  font-weight: 700;
  color: vars.$text-muted;
  transition: all 0.3s;
}

.category-item.active .category-text {
  color: vars.$text-color;
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
  position: relative;
  z-index: 1;
  overflow: hidden;
}
</style>
