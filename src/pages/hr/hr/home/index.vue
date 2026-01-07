<template>
  <view class="home-page">
    <view class="hero">
      <view class="hero-top">
        <view class="welcome-title">SmartHire+</view>
        <view class="avatar" @click="goProfile">
          <image :src="avatarImg" mode="aspectFill" />
        </view>
      </view>
      <view class="hero-sub">欢迎回来，开始筛选候选人</view>
    </view>

    <view class="recommend-section">
      <view class="recommend-header">
        <view class="section-title">推荐求职者</view>
        <view class="action-link" @click="refreshSeekerCards">刷新</view>
      </view>

      <view v-if="seekerLoading" class="state">加载中...</view>
      <view v-else-if="seekerError" class="state">{{ seekerError }}</view>
      <view v-else class="seeker-list">
        <view v-if="seekerCards.length === 0" class="state">暂无推荐</view>
        <SeekerRecommendCard
          v-for="card in seekerCards"
          :key="card.userId ?? card.id ?? card.username"
          :seeker="card"
          :matchScore="getMatchScore(card)"
          @click="openSeekerDetail"
        />
      </view>
    </view>

    <CustomTabBar />
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import avatarImg from '@/static/user-avatar.png';
import CustomTabBar from '@/components/common/CustomTabBar.vue';
import { getApplicationDetail, getPublicSeekerCards, type PublicSeekerCard } from '@/services/api/hr';
import { getCandidateMatchAnalysis } from '@/services/api/hr-ai';
import SeekerRecommendCard from '@/pages/hr/hr/seeker/components/SeekerRecommendCard.vue';

const seekerCards = ref<PublicSeekerCard[]>([]);
const seekerLoading = ref(false);
const seekerError = ref('');
const matchScoreByUserId = ref<Record<number, number>>({});

const refreshSeekerCards = async () => {
  seekerLoading.value = true;
  seekerError.value = '';
  try {
    const data = await getPublicSeekerCards();
    seekerCards.value = Array.isArray(data) ? data : [];
    await loadMatchScores(seekerCards.value);
  } catch (err) {
    console.error('Failed to load public seeker cards:', err);
    seekerError.value = '推荐加载失败';
  } finally {
    seekerLoading.value = false;
  }
};

const goProfile = () => {
  uni.navigateTo({ url: '/pages/hr/hr/profile/index' });
};

const openSeekerDetail = (card: PublicSeekerCard) => {
  const userId = (card.userId ?? card.id) as number | undefined;
  if (!userId || Number.isNaN(Number(userId))) {
    uni.showToast({ title: '缺少用户ID', icon: 'none' });
    return;
  }
  uni.navigateTo({
    url: `/pages/hr/hr/seeker/detail?userId=${encodeURIComponent(userId)}&username=${encodeURIComponent(card.username || '')}`,
  });
};

const getMatchScore = (card: PublicSeekerCard): number | null => {
  const userId = Number(card.userId ?? card.id);
  if (!Number.isFinite(userId) || userId <= 0) return null;
  const score = matchScoreByUserId.value[userId];
  if (!Number.isFinite(score)) return null;
  return score;
};

const loadMatchScores = async (cards: PublicSeekerCard[]) => {
  const top = (cards || []).slice(0, 6);
  const nextScores: Record<number, number> = { ...matchScoreByUserId.value };

  for (const card of top) {
    const userId = Number(card.userId ?? card.id);
    if (!Number.isFinite(userId) || userId <= 0) continue;
    if (Number.isFinite(nextScores[userId])) continue;

    const cachedAppId = Number(uni.getStorageSync(`hr_chat_app_by_user_${userId}`));
    if (!Number.isFinite(cachedAppId) || cachedAppId <= 0) continue;

    try {
      const detail = await getApplicationDetail(cachedAppId);
      const analysis = await getCandidateMatchAnalysis(detail.jobSeekerId, detail.jobId);
      const rawScore =
        (analysis as any)?.match_analysis?.overall_score ??
        (analysis as any)?.match_score ??
        (analysis as any)?.matchScore;
      const parsed = Number(rawScore);
      if (Number.isFinite(parsed)) {
        nextScores[userId] = Math.round(parsed);
        matchScoreByUserId.value = { ...nextScores };
      }
    } catch (e) {
      // ignore per-card errors (no record / no permission / not generated yet)
      continue;
    }
  }
};

onMounted(() => {
  refreshSeekerCards();
});

onShow(() => {
  uni.hideTabBar({ fail: () => {} });
  refreshSeekerCards();
});
</script>

<style scoped lang="scss">
.home-page {
  min-height: 100vh;
  padding: 0 32rpx 32rpx;
  background: linear-gradient(180deg, #e5f0ff 0%, #f6f7fb 40%, #f6f7fb 100%);
  box-sizing: border-box;
}

.hero {
  padding-top: calc(var(--status-bar-height) + 64rpx);
  padding-bottom: 32rpx;
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.hero-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.welcome-title {
  font-size: 38rpx;
  font-weight: 700;
  color: #0b1c33;
}

.avatar {
  width: 88rpx;
  height: 88rpx;
  border-radius: 16rpx;
  background: #eef2ff;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.avatar image {
  width: 100%;
  height: 100%;
}

.hero-sub {
  font-size: 26rpx;
  color: #2f4b76;
}

.section-title {
  font-size: 30rpx;
  font-weight: 600;
  margin-bottom: 24rpx;
}

.recommend-section {
  padding: 0 8rpx 24rpx;
}

.recommend-header {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
}

.action-link {
  font-size: 24rpx;
  color: #2f7cff;
}

.state {
  color: #8a92a7;
  font-size: 24rpx;
  padding: 16rpx 0;
}

.seeker-list {
  padding-top: 6rpx;
}

</style>
