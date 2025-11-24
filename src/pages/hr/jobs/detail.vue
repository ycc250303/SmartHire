<template>
  <view class="job-detail">
    <view class="card basic-card" v-if="job">
      <view class="header">
        <view>
          <view class="job-title">{{ job.title }}</view>
          <view class="job-meta">{{ job.city }} · {{ job.salary }}</view>
        </view>
        <view class="status-tag" :class="job.status">{{ statusText(job.status) }}</view>
      </view>
      <view class="job-actions">
        <button class="ghost" @click="editJob">编辑</button>
        <button class="primary" @click="toggleJobStatus">
          {{ job.status === 'open' ? '暂停招聘' : '重新开启' }}
        </button>
      </view>
    </view>

    <view class="card pipeline-card">
      <view class="section-title">招聘进度</view>
      <view class="pipeline-grid">
        <view class="pipeline-item" v-for="item in pipeline" :key="item.label">
          <view class="label">{{ item.label }}</view>
          <view class="value">{{ item.value }}</view>
        </view>
      </view>
      <button class="ghost" @click="goCandidates">查看候选人列表</button>
    </view>

    <view class="card candidate-card">
      <view class="section-title">智能匹配候选人</view>
      <view class="candidate-item" v-for="candidate in candidates" :key="candidate.id">
        <view>
          <view class="candidate-name">{{ candidate.name }} · {{ candidate.score }} 分</view>
          <view class="candidate-meta">{{ candidate.intentPosition }} · {{ candidate.status }}</view>
          <view class="tag-list">
            <text class="tag" v-for="tag in candidate.tags" :key="tag">{{ tag }}</text>
          </view>
          <view class="candidate-desc">匹配说明：{{ candidate.matchExplain }}</view>
        </view>
        <button class="ghost" @click.stop="viewCandidate(candidate.id)">详情</button>
      </view>
    </view>

    <view class="card jd-card" v-if="jdQuality">
      <view class="section-title">JD 质量与合规</view>
      <view class="score">质量评分 {{ jdQuality.score }}/100</view>
      <view class="risk-list">
        <view class="risk-item" v-for="riskItem in jdQuality.risks" :key="riskItem.message">
          <text class="risk-type">{{ riskLabel(riskItem.type) }}</text>
          <text class="risk-msg">{{ riskItem.message }}</text>
        </view>
      </view>
      <view class="suggestions">
        <view class="suggest-title">优化建议</view>
        <view class="suggest-item" v-for="suggest in jdQuality.suggestions" :key="suggest">{{ suggest }}</view>
      </view>
      <button class="ghost" @click="viewReport">查看完整报告</button>
    </view>

    <view class="card risk-card" v-if="risk">
      <view class="section-title">风控提示</view>
      <view class="risk-level" :class="risk.level">{{ risk.level === 'high' ? '高风险' : '中风险' }}</view>
      <view class="risk-message">{{ risk.message }}</view>
      <view class="risk-detail">{{ risk.detail }}</view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import {
  fetchJobDetail,
  type JobSummary,
  type CandidateMatch,
  type JDQualityReport,
  type RiskAlert,
} from '@/mock/hr';

const job = ref<JobSummary | null>(null);
const candidates = ref<CandidateMatch[]>([]);
const jdQuality = ref<JDQualityReport | null>(null);
const risk = ref<RiskAlert | null>(null);
const jobId = ref<number>(0);

const pipeline = computed(() => [
  { label: '新投递', value: job.value?.applied ?? 0 },
  { label: '面试中', value: job.value?.interviewing ?? 0 },
  { label: 'Offer', value: job.value?.offer ?? 0 },
  { label: '已入职', value: job.value?.onboarded ?? 0 },
]);

const loadDetail = async (id: number) => {
  // TODO: GET /api/hr/jobs/:id
  const data = await fetchJobDetail(id);
  job.value = data.job;
  candidates.value = data.candidates;
  jdQuality.value = data.jdQuality;
  risk.value = data.risk;
};

const statusText = (status: JobSummary['status']) => {
  const map = { open: '招聘中', paused: '已暂停', closed: '已关闭' } as const;
  return map[status];
};

const editJob = () => {
  if (!job.value) return;
  uni.navigateTo({ url: `/pages/hr/jobs/edit?jobId=${job.value.jobId}` });
};

const toggleJobStatus = () => {
  if (!job.value) return;
  // TODO: PUT /api/hr/jobs/:id/status
  uni.showToast({ title: '状态已更新', icon: 'success' });
};

const goCandidates = () => {
  if (!job.value) return;
  uni.navigateTo({ url: `/pages/hr/jobs/detail?jobId=${job.value.jobId}&section=candidates` });
};

const viewCandidate = (candidateId: string) => {
  uni.navigateTo({ url: `/pages/hr/candidate/detail?id=${candidateId}` });
};

const riskLabel = (type: JDQualityReport['risks'][number]['type']) => {
  const map = {
    discrimination: '歧视用语',
    ambiguity: '描述模糊',
    other: '其他',
  } as const;
  return map[type];
};

const viewReport = () => {
  uni.showModal({
    title: 'JD 分析报告',
    content: 'TODO: 接入 JD 质量报告弹窗',
    showCancel: false,
  });
};

onLoad((options) => {
  jobId.value = Number(options?.jobId) || 101;
  loadDetail(jobId.value);
});
</script>

<style scoped lang="scss">
.job-detail {
  padding: 24rpx;
  background: #f6f7fb;
  min-height: 100vh;
  box-sizing: border-box;
}

.card {
  background: #fff;
  border-radius: 24rpx;
  padding: 28rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 10rpx 32rpx rgba(0, 34, 90, 0.08);
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.job-title {
  font-size: 34rpx;
  font-weight: 600;
}

.job-meta {
  font-size: 26rpx;
  color: #8a92a7;
  margin-top: 6rpx;
}

.status-tag {
  padding: 10rpx 24rpx;
  border-radius: 999rpx;
  font-size: 24rpx;
}

.status-tag.open {
  background: #e8f3ff;
  color: #1f6bff;
}

.status-tag.paused {
  background: #fff1db;
  color: #ff8f2a;
}

.status-tag.closed {
  background: #f4f5f6;
  color: #9da6b8;
}

.job-actions {
  display: flex;
  gap: 16rpx;
  margin-top: 24rpx;
}

button.ghost {
  flex: 1;
  border-radius: 16rpx;
  border: 2rpx solid #d6def2;
  height: 80rpx;
  background: transparent;
  color: #2f7cff;
}

button.primary {
  flex: 1;
  border-radius: 16rpx;
  border: none;
  background: #2f7cff;
  color: #fff;
  height: 80rpx;
}

.pipeline-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16rpx;
  margin-bottom: 24rpx;
}

.pipeline-item {
  background: #f7f9ff;
  border-radius: 20rpx;
  padding: 20rpx;
  text-align: center;
}

.pipeline-item .label {
  color: #9aa3b7;
  font-size: 24rpx;
}

.pipeline-item .value {
  font-size: 32rpx;
  font-weight: 600;
  margin-top: 8rpx;
}

.candidate-item {
  border-bottom: 2rpx solid #f1f2f6;
  padding: 24rpx 0;
  display: flex;
  justify-content: space-between;
  gap: 20rpx;
}

.candidate-item:last-child {
  border-bottom: none;
}

.candidate-name {
  font-size: 28rpx;
  font-weight: 600;
}

.candidate-meta {
  font-size: 24rpx;
  color: #929ab0;
  margin: 6rpx 0;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
}

.tag {
  background: #eef2ff;
  border-radius: 12rpx;
  padding: 6rpx 16rpx;
  font-size: 22rpx;
  color: #2f7cff;
}

.candidate-desc {
  margin-top: 10rpx;
  font-size: 24rpx;
  color: #6b748e;
}

.score {
  font-size: 32rpx;
  font-weight: 600;
  margin-bottom: 16rpx;
}

.risk-list {
  margin-bottom: 16rpx;
}

.risk-item {
  display: flex;
  gap: 12rpx;
  padding: 12rpx 0;
}

.risk-type {
  color: #ff8f2a;
}

.risk-card {
  border: 2rpx solid #ff4d4f;
}

.risk-level {
  font-size: 26rpx;
  margin-bottom: 8rpx;
  color: #ff8f2a;
}

.risk-level.high {
  color: #ff4d4f;
}

.risk-message {
  font-size: 26rpx;
  margin-bottom: 8rpx;
}

.risk-detail {
  color: #8a92a7;
  font-size: 24rpx;
}
</style>
