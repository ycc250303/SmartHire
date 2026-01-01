<template>
  <view class="favorites-page">
    <view v-if="loading && favorites.length === 0" class="loading-container">
      <text class="loading-text">{{ t('pages.jobs.favorites.loading') }}</text>
    </view>

    <view v-else-if="error && favorites.length === 0" class="error-container">
      <text class="error-text">{{ error }}</text>
      <button class="retry-button" @click="loadFavorites">
        {{ t('pages.jobs.favorites.retry') }}
      </button>
    </view>

    <scroll-view
      v-else
      class="favorites-list"
      scroll-y
      refresher-enabled
      :refresher-triggered="refreshing"
      @refresherrefresh="handleRefresh"
    >
      <view v-if="favorites.length === 0" class="empty-container">
        <text class="empty-text">{{ t('pages.jobs.favorites.empty') }}</text>
      </view>

      <view v-for="fav in favorites" :key="fav.favoriteId" class="favorite-item">
        <FavoriteJobCard :job="fav" />
      </view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { onLoad, onShow } from '@dcloudio/uni-app';
import { t } from '@/locales';
import { getFavoriteJobs, type FavoriteJob } from '@/services/api/seeker';
import FavoriteJobCard from '@/components/common/FavoriteJobCard.vue';

const favorites = ref<FavoriteJob[]>([]);
const loading = ref(false);
const error = ref<string | null>(null);
const refreshing = ref(false);

onLoad(() => {
  uni.setNavigationBarTitle({
    title: t('pages.jobs.favorites.title')
  });
  loadFavorites();
});

onShow(() => {
  loadFavorites();
});

async function loadFavorites() {
  loading.value = true;
  error.value = null;

  try {
    favorites.value = await getFavoriteJobs();
  } catch (err) {
    error.value = err instanceof Error ? err.message : t('pages.jobs.favorites.loadError');
    console.error('Failed to load favorites:', err);
  } finally {
    loading.value = false;
    refreshing.value = false;
  }
}

async function handleRefresh() {
  refreshing.value = true;
  await loadFavorites();
}
</script>

<style lang="scss" scoped>
@use "@/styles/variables.scss" as vars;

.favorites-page {
  min-height: 100vh;
  background: vars.$surface-color;
  display: flex;
  flex-direction: column;
}

.loading-container,
.error-container,
.empty-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 400rpx;
  padding: vars.$spacing-xl;
}

.loading-text,
.error-text,
.empty-text {
  font-size: 28rpx;
  color: vars.$text-muted;
  margin-bottom: vars.$spacing-lg;
}

.error-text {
  color: #ff6b6b;
}

.retry-button {
  padding: vars.$spacing-sm vars.$spacing-lg;
  background: vars.$primary-color;
  color: #ffffff;
  border-radius: vars.$border-radius-sm;
  font-size: 28rpx;
}

.favorites-list {
  flex: 1;
  height: calc(100vh);
  padding: vars.$spacing-md;
}

.favorite-item {
  margin-bottom: vars.$spacing-md;
}
</style>

