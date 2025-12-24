<template>
  <view class="settings-page">
    <view class="settings-list">
      <view class="settings-item" @click="showLanguagePicker">
        <text class="item-text">{{ languageText }}</text>
        <view class="language-value">
          <text class="language-text">{{ currentLanguageDisplay }}</text>
          <text class="arrow">›</text>
        </view>
      </view>

      <view class="settings-item" @click="goToHelpCenter">
        <text class="item-text">{{ helpCenterText }}</text>
        <text class="arrow">›</text>
      </view>

      <view class="settings-item" @click="handleLogout">
        <text class="item-text">{{ logoutText }}</text>
        <text class="arrow">›</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, watch } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { t, setLocale, getLocaleRef } from '@/locales';
import { useNavigationTitle } from '@/utils/useNavigationTitle';
import { useUserStore } from '@/store/user';
import zhCN from '@/locales/zh-CN';
import enUS from '@/locales/en-US';

useNavigationTitle('navigation.settings');

const userStore = useUserStore();
const localeRef = getLocaleRef();

const messages = {
  'zh-CN': zhCN,
  'en-US': enUS
};

const languageText = computed(() => messages[localeRef.value].pages.settings.language);
const helpCenterText = computed(() => messages[localeRef.value].pages.settings.helpCenter);
const logoutText = computed(() => messages[localeRef.value].pages.settings.logout);

const currentLanguageDisplay = computed(() => {
  return localeRef.value === 'zh-CN' 
    ? messages['zh-CN'].pages.settings.languageChinese
    : messages['en-US'].pages.settings.languageEnglish;
});

onLoad(() => {
  updateNavigationTitle();
});

watch(localeRef, () => {
  updateNavigationTitle();
});

function updateNavigationTitle() {
  uni.setNavigationBarTitle({
    title: t('pages.settings.title')
  });
}

function showLanguagePicker() {
  const currentIndex = localeRef.value === 'zh-CN' ? 0 : 1;
  
  uni.showActionSheet({
    itemList: [
      messages['zh-CN'].pages.settings.languageChinese,
      messages['en-US'].pages.settings.languageEnglish
    ],
    success: (res) => {
      const newLocale = res.tapIndex === 0 ? 'zh-CN' : 'en-US';
      if (newLocale !== localeRef.value) {
        setLocale(newLocale);
      }
    }
  });
}

function goToHelpCenter() {
  uni.navigateTo({
    url: '/pages/auth/help'
  });
}

function handleLogout() {
  uni.showModal({
    title: t('pages.settings.logout'),
    content: t('pages.settings.logoutConfirm'),
    success: (res) => {
      if (res.confirm) {
        userStore.logout();
      }
    }
  });
}
</script>

<style lang="scss" scoped>
@use "@/styles/variables.scss" as vars;

.settings-page {
  min-height: 100vh;
  background-color: vars.$surface-color;
  padding: vars.$spacing-md;
}

.settings-list {
  display: flex;
  flex-direction: column;
  gap: vars.$spacing-md;
}

.settings-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: vars.$surface-color;
  border-radius: vars.$border-radius-lg;
  padding: vars.$spacing-lg vars.$spacing-xl;
  cursor: pointer;
  transition: opacity 0.2s;

  &:active {
    opacity: 0.7;
  }
}

.language-value {
  display: flex;
  align-items: center;
  gap: vars.$spacing-sm;
}

.language-text {
  font-size: 30rpx;
  color: vars.$text-muted;
}

.item-text {
  font-size: 30rpx;
  color: vars.$text-color;
  font-weight: 500;
}

.arrow {
  font-size: 48rpx;
  color: vars.$text-muted;
  font-weight: 300;
}
</style>

