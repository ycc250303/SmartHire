<template>
  <view class="page">
    <view class="candidate-header" v-if="detail">
      <view class="candidate-info">
        <text class="candidate-name">{{ detail.name }}·{{ detail.position }}</text>
        <text class="candidate-meta">{{ detail.location }} · {{ detail.salary }}</text>
      </view>
      <view class="overall-match">
        <view class="match-circle">
          <text class="match-value">{{ detail.overallMatch }}%</text>
        </view>
        <text class="match-label">总体匹配</text>
      </view>
    </view>
    
    <view class="content" v-if="detail">
      <!-- 技术匹配 -->
      <view class="info-card">
        <view class="card-title">技术匹配</view>
        <text class="card-content">{{ detail.technicalMatch.description }}</text>
      </view>
      
      <!-- 经验对齐 -->
      <view class="info-card">
        <view class="card-title">经验对齐</view>
        <text class="card-content">{{ detail.experienceAlignment.description }}</text>
      </view>
      
      <!-- 差距提示 -->
      <view class="info-card">
        <view class="card-title">差距提示</view>
        <text class="card-content">{{ detail.gapReminder.description }}</text>
      </view>
      
      <!-- 技能矩阵 -->
      <view class="skill-matrix-card">
        <view class="card-header-row">
          <text class="card-title">技能矩阵</text>
          <text class="card-subtitle">关键技能对齐</text>
        </view>
        <view class="skill-items">
          <view 
            class="skill-item" 
            v-for="skill in detail.skillMatrix" 
            :key="skill.skillName"
          >
            <view class="skill-header">
              <text class="skill-name">{{ skill.skillName }}</text>
              <text class="skill-match">{{ skill.overallMatch }}%</text>
            </view>
            <view class="skill-progress">
              <view class="progress-row">
                <text class="progress-label">候选人</text>
                <view class="progress-bar">
                  <view 
                    class="progress-fill" 
                    :style="{ width: skill.candidateProficiency + '%' }"
                  ></view>
                </view>
                <text class="progress-value">{{ skill.candidateProficiency }}%</text>
              </view>
              <view class="progress-row">
                <text class="progress-label">目标</text>
                <view class="progress-bar">
                  <view 
                    class="progress-fill target" 
                    :style="{ width: skill.targetProficiency + '%' }"
                  ></view>
                </view>
                <text class="progress-value">{{ skill.targetProficiency }}%</text>
              </view>
            </view>
          </view>
        </view>
      </view>
      
      <!-- AI分析 -->
      <view class="ai-analysis-card">
        <view class="card-header-row">
          <text class="card-title">AI 分析</text>
          <text class="card-subtitle">生成式摘要</text>
        </view>
        <text class="ai-content">{{ detail.aiAnalysis.summary }}</text>
      </view>
    </view>
    
    <view class="loading-state" v-if="loading">
      <text class="loading-text">加载中...</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { getCandidateDetail, type CandidateDetail } from '@/services/api/hr';

const detail = ref<CandidateDetail | null>(null);
const loading = ref(false);
const candidateId = ref<number | null>(null);

const loadDetail = async () => {
  if (!candidateId.value) return;
  
  loading.value = true;
  try {
    const data = await getCandidateDetail(candidateId.value);
    detail.value = data;
  } catch (error) {
    console.error('Failed to load candidate detail:', error);
    uni.showToast({
      title: '加载失败',
      icon: 'none',
    });
  } finally {
    loading.value = false;
  }
};

onLoad((options) => {
  if (options.candidateId) {
    candidateId.value = parseInt(options.candidateId as string, 10);
    loadDetail();
  }
});
</script>

<style lang="scss" scoped>
@use "@/styles/variables.scss" as vars;

.page {
  min-height: 100vh;
  background-color: vars.$bg-color;
  padding: vars.$spacing-md;
}

.candidate-header {
  background-color: vars.$surface-color;
  border-radius: vars.$border-radius;
  padding: vars.$spacing-lg;
  margin-bottom: vars.$spacing-md;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.candidate-info {
  flex: 1;
}

.candidate-name {
  font-size: 36rpx;
  font-weight: 600;
  color: vars.$text-color;
  display: block;
  margin-bottom: 8rpx;
}

.candidate-meta {
  font-size: 24rpx;
  color: vars.$text-muted;
}

.overall-match {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex-shrink: 0;
}

.match-circle {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  background-color: vars.$primary-color-soft;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 8rpx;
}

.match-value {
  font-size: 48rpx;
  font-weight: 600;
  color: vars.$primary-color;
}

.match-label {
  font-size: 24rpx;
  color: vars.$text-muted;
}

.content {
  display: flex;
  flex-direction: column;
  gap: vars.$spacing-md;
}

.info-card {
  background-color: vars.$surface-color;
  border-radius: vars.$border-radius;
  padding: vars.$spacing-lg;
}

.card-title {
  font-size: 32rpx;
  font-weight: 600;
  color: vars.$text-color;
  margin-bottom: vars.$spacing-sm;
}

.card-content {
  font-size: 28rpx;
  color: vars.$text-color;
  line-height: 1.6;
}

.card-header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: vars.$spacing-md;
}

.card-subtitle {
  font-size: 24rpx;
  color: vars.$text-muted;
}

.skill-matrix-card {
  background-color: vars.$surface-color;
  border-radius: vars.$border-radius;
  padding: vars.$spacing-lg;
}

.skill-items {
  display: flex;
  flex-direction: column;
  gap: vars.$spacing-lg;
}

.skill-item {
  display: flex;
  flex-direction: column;
  gap: vars.$spacing-sm;
}

.skill-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.skill-name {
  font-size: 28rpx;
  font-weight: 600;
  color: vars.$text-color;
}

.skill-match {
  font-size: 32rpx;
  font-weight: 600;
  color: vars.$primary-color;
}

.skill-progress {
  display: flex;
  flex-direction: column;
  gap: vars.$spacing-sm;
}

.progress-row {
  display: flex;
  align-items: center;
  gap: vars.$spacing-sm;
}

.progress-label {
  font-size: 24rpx;
  color: vars.$text-muted;
  width: 100rpx;
  flex-shrink: 0;
}

.progress-bar {
  flex: 1;
  height: 8rpx;
  background-color: #f0f0f0;
  border-radius: 4rpx;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background-color: vars.$primary-color;
  border-radius: 4rpx;
  transition: width 0.3s ease;
}

.progress-fill.target {
  background-color: #90caf9;
}

.progress-value {
  font-size: 24rpx;
  color: vars.$primary-color;
  font-weight: 600;
  width: 60rpx;
  text-align: right;
  flex-shrink: 0;
}

.ai-analysis-card {
  background-color: vars.$surface-color;
  border-radius: vars.$border-radius;
  padding: vars.$spacing-lg;
}

.ai-content {
  font-size: 28rpx;
  color: vars.$text-color;
  line-height: 1.6;
}

.loading-state {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400rpx;
  padding: vars.$spacing-xl;
}

.loading-text {
  font-size: 28rpx;
  color: vars.$text-muted;
}
</style>






