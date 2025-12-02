<template>
  <view class="page">
    <view class="header">
      <text class="subtitle">HR 发布中心</text>
      <view class="title-row">
        <text class="title">AI 辅助岗位创建</text>
        <view class="status-badge" v-if="jobPosting">
          <text class="status-text">{{ jobPosting.status === 'draft' ? '草稿状态' : '已发布' }}</text>
          <text class="auto-save" v-if="jobPosting.autoSaved">已自动保存</text>
        </view>
      </view>
      <text class="description">同步语义匹配策略与曝光计划</text>
    </view>
    
    <scroll-view class="content" scroll-y>
      <!-- 岗位概览 -->
      <view class="job-overview-card" v-if="jobPosting">
        <text class="version">发布版本 {{ jobPosting.version }}</text>
        <text class="job-title">{{ jobPosting.title }}</text>
        <text class="job-description">{{ jobPosting.description }}</text>
        <view class="job-tags">
          <view class="job-tag">{{ jobPosting.location }}</view>
          <view class="job-tag">{{ jobPosting.salary }}</view>
          <view class="job-tag">{{ jobPosting.headcount }}个HC</view>
          <view class="job-tag highlight">{{ jobPosting.exposureStatus }}</view>
        </view>
      </view>
      
      <!-- 核心字段 -->
      <view class="section" v-if="jobPosting">
        <view class="section-header">
          <text class="section-title">核心字段</text>
          <text class="batch-import">批量导入</text>
        </view>
        
        <view class="field-card">
          <view class="field-row">
            <text class="field-label">岗位名称</text>
            <text class="field-meta">关键词命中率 {{ jobPosting.coreFields.jobTitle.keywordHitRate }}</text>
          </view>
          <text class="field-value">{{ jobPosting.coreFields.jobTitle.value }}</text>
        </view>
        
        <view class="field-card">
          <view class="field-row">
            <text class="field-label">经验要求</text>
            <text class="field-meta">候选热度 {{ jobPosting.coreFields.experienceRequirement.candidateHeat }}</text>
          </view>
          <text class="field-value">{{ jobPosting.coreFields.experienceRequirement.value }}</text>
        </view>
        
        <view class="field-card">
          <view class="field-row">
            <text class="field-label">薪酬范围</text>
            <text class="field-meta">透明度指数 {{ jobPosting.coreFields.salaryRange.transparencyIndex }}</text>
          </view>
          <text class="field-value">{{ jobPosting.coreFields.salaryRange.value }}</text>
        </view>
        
        <view class="field-card">
          <view class="field-row">
            <text class="field-label">职责摘要</text>
            <text class="field-meta" v-if="jobPosting.coreFields.jobSummary.isAiGenerated">AI 摘要,可编辑</text>
          </view>
          <text class="field-value">{{ jobPosting.coreFields.jobSummary.value }}</text>
        </view>
      </view>
      
      <!-- 投放渠道 -->
      <view class="section" v-if="jobPosting">
        <view class="section-header">
          <text class="section-title">投放渠道</text>
          <text class="sync-link" @click="handleSyncChannels">同步</text>
        </view>
        <view class="channels">
          <text 
            class="channel-item" 
            v-for="(channel, index) in jobPosting.placementChannels" 
            :key="index"
          >
            {{ channel }}<text v-if="index < jobPosting.placementChannels.length - 1"> / </text>
          </text>
        </view>
        <text class="channel-count">{{ jobPosting.placementChannels.length }}个渠道</text>
      </view>
      
      <!-- 关键技能 -->
      <view class="section" v-if="jobPosting">
        <view class="section-header">
          <text class="section-title">关键技能</text>
          <text class="section-subtitle">语义标签</text>
        </view>
        <view class="skills">
          <view 
            class="skill-tag" 
            :class="{ selected: skill.selected }"
            v-for="skill in jobPosting.keySkills" 
            :key="skill.name"
            @click="toggleSkill(skill)"
          >
            {{ skill.name }}
          </view>
        </view>
      </view>
      
      <!-- AI建议 -->
      <view class="section" v-if="jobPosting && jobPosting.aiSuggestions.length > 0">
        <view class="section-header">
          <text class="section-title">AI 建议</text>
          <text class="section-subtitle">可执行洞察</text>
        </view>
        <view 
          class="suggestion-card" 
          v-for="(suggestion, index) in jobPosting.aiSuggestions" 
          :key="index"
        >
          <text class="suggestion-title">{{ suggestion.title }}</text>
          <text class="suggestion-content">{{ suggestion.content }}</text>
          <text 
            class="suggestion-link" 
            v-if="suggestion.linkText"
            @click="handleSuggestionLink(suggestion)"
          >
            {{ suggestion.linkText }}
          </text>
        </view>
      </view>
      
      <!-- 合规提醒 -->
      <view class="section" v-if="jobPosting && jobPosting.complianceReminder">
        <view class="compliance-card">
          <text class="compliance-text">{{ jobPosting.complianceReminder }}</text>
        </view>
      </view>
    </scroll-view>
    
    <view class="footer-actions">
      <button class="btn-secondary" @click="handleSaveDraft">保存草稿</button>
      <button class="btn-primary" @click="handlePublish">发布岗位</button>
    </view>
    
    <view class="loading-state" v-if="loading">
      <text class="loading-text">加载中...</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { 
  getJobPosting, 
  createJobPosting, 
  updateJobPosting, 
  publishJobPosting,
  syncPlacementChannels,
  type JobPosting 
} from '@/services/api/hr';

const jobPosting = ref<JobPosting | null>(null);
const loading = ref(false);
const jobId = ref<number | null>(null);

const loadJobPosting = async () => {
  if (jobId.value) {
    loading.value = true;
    try {
      const data = await getJobPosting(jobId.value);
      jobPosting.value = data;
    } catch (error) {
      console.error('Failed to load job posting:', error);
      uni.showToast({
        title: '加载失败',
        icon: 'none',
      });
    } finally {
      loading.value = false;
    }
  } else {
    // 创建新岗位草稿
    loading.value = true;
    try {
      const data = await createJobPosting({
        version: 'V1.2',
        status: 'draft',
        autoSaved: true,
        title: '智能匹配产品经理',
        description: '负责 SmartHire+ 语义推荐引擎的岗位配置、反馈闭环与曝光策略,联动算法团队提升 HR 端转化。',
        location: '上海·张江/远程',
        salary: '30-45K・16薪',
        headcount: 3,
        exposureStatus: '金牌曝光位已锁定',
        coreFields: {
          jobTitle: {
            value: '智能匹配产品经理(L5)',
            keywordHitRate: '4/5',
          },
          experienceRequirement: {
            value: '5-8年・ToB 产品策略',
            candidateHeat: '+12%',
          },
          salaryRange: {
            value: '30-45K・16薪+年终激励',
            transparencyIndex: 92,
          },
          jobSummary: {
            value: '负责语义检索、反馈闭环与AI',
            isAiGenerated: true,
          },
        },
        placementChannels: ['SmartHire+ 高匹配池', 'LinkedIn', '智联精选'],
        keySkills: [
          { name: '语义检索', selected: true },
          { name: 'LLM Prompt', selected: true },
          { name: '数据闭环', selected: true },
          { name: '跨团队协作', selected: true },
          { name: 'B端流程设计', selected: false },
          { name: '增长策略', selected: false },
        ],
        aiSuggestions: [
          {
            type: 'exposure',
            title: '曝光策略',
            content: '发布时间选择 10:00-10:30 可覆盖280位高匹配候选,预计2小时内完成首轮触达。',
            linkText: '匹配池:资深产品·L4-L6',
          },
          {
            type: 'salary',
            title: 'AI 建议',
            content: '上调薪资上限至 48K 可额外提升7% 投递率,建议同步强调远程灵活度。',
            linkText: '来源:近60天A/B 实验',
          },
        ],
        complianceReminder: '请确认隐私条款已更新至 2025/Q4 版本,否则将限制发布。',
      });
      jobPosting.value = data;
      jobId.value = data.jobId;
    } catch (error) {
      console.error('Failed to create job posting:', error);
      uni.showToast({
        title: '创建失败',
        icon: 'none',
      });
    } finally {
      loading.value = false;
    }
  }
};

const toggleSkill = (skill: { name: string; selected: boolean }) => {
  if (jobPosting.value) {
    const skillItem = jobPosting.value.keySkills.find(s => s.name === skill.name);
    if (skillItem) {
      skillItem.selected = !skillItem.selected;
    }
  }
};

const handleSyncChannels = async () => {
  if (!jobId.value || !jobPosting.value) return;
  
  try {
    await syncPlacementChannels(jobId.value, jobPosting.value.placementChannels);
    uni.showToast({
      title: '同步成功',
      icon: 'success',
    });
  } catch (error) {
    console.error('Failed to sync channels:', error);
    uni.showToast({
      title: '同步失败',
      icon: 'none',
    });
  }
};

const handleSuggestionLink = (suggestion: { linkUrl?: string }) => {
  if (suggestion.linkUrl) {
    // 处理链接点击
    console.log('Open link:', suggestion.linkUrl);
  }
};

const handleSaveDraft = async () => {
  if (!jobId.value || !jobPosting.value) return;
  
  try {
    await updateJobPosting(jobId.value, {
      ...jobPosting.value,
      autoSaved: true,
    });
    uni.showToast({
      title: '保存成功',
      icon: 'success',
    });
  } catch (error) {
    console.error('Failed to save draft:', error);
    uni.showToast({
      title: '保存失败',
      icon: 'none',
    });
  }
};

const handlePublish = async () => {
  if (!jobId.value) return;
  
  try {
    await publishJobPosting(jobId.value);
    uni.showToast({
      title: '发布成功',
      icon: 'success',
    });
    setTimeout(() => {
      uni.navigateBack();
    }, 1500);
  } catch (error) {
    console.error('Failed to publish:', error);
    uni.showToast({
      title: '发布失败',
      icon: 'none',
    });
  }
};

onLoad((options) => {
  if (options.jobId) {
    jobId.value = parseInt(options.jobId as string, 10);
  }
  loadJobPosting();
});
</script>

<style lang="scss" scoped>
@use "@/styles/variables.scss" as vars;

.page {
  min-height: 100vh;
  background-color: vars.$bg-color;
  display: flex;
  flex-direction: column;
}

.header {
  padding: vars.$spacing-md;
  background-color: vars.$surface-color;
}

.subtitle {
  font-size: 24rpx;
  color: vars.$text-muted;
  display: block;
  margin-bottom: 8rpx;
}

.title-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 8rpx;
}

.title {
  font-size: 48rpx;
  font-weight: 600;
  color: vars.$text-color;
  flex: 1;
}

.status-badge {
  background-color: vars.$primary-color-soft;
  border-radius: vars.$border-radius-sm;
  padding: 8rpx 16rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  flex-shrink: 0;
}

.status-text {
  font-size: 24rpx;
  color: vars.$primary-color;
  font-weight: 600;
}

.auto-save {
  font-size: 20rpx;
  color: vars.$text-muted;
  margin-top: 4rpx;
}

.description {
  font-size: 24rpx;
  color: vars.$text-muted;
}

.content {
  flex: 1;
  padding: vars.$spacing-md;
}

.job-overview-card {
  background-color: vars.$surface-color;
  border-radius: vars.$border-radius;
  padding: vars.$spacing-lg;
  margin-bottom: vars.$spacing-md;
}

.version {
  font-size: 24rpx;
  color: vars.$text-muted;
  display: block;
  margin-bottom: vars.$spacing-sm;
}

.job-title {
  font-size: 40rpx;
  font-weight: 600;
  color: vars.$text-color;
  display: block;
  margin-bottom: vars.$spacing-md;
}

.job-description {
  font-size: 28rpx;
  color: vars.$text-color;
  line-height: 1.6;
  display: block;
  margin-bottom: vars.$spacing-md;
}

.job-tags {
  display: flex;
  flex-wrap: wrap;
  gap: vars.$spacing-sm;
}

.job-tag {
  padding: 8rpx 16rpx;
  border-radius: vars.$border-radius-sm;
  font-size: 24rpx;
  background-color: vars.$primary-color-soft;
  color: vars.$primary-color;
}

.job-tag.highlight {
  background-color: #fff3e0;
  color: #f57c00;
}

.section {
  background-color: vars.$surface-color;
  border-radius: vars.$border-radius;
  padding: vars.$spacing-lg;
  margin-bottom: vars.$spacing-md;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: vars.$spacing-md;
}

.section-title {
  font-size: 32rpx;
  font-weight: 600;
  color: vars.$text-color;
}

.section-subtitle {
  font-size: 24rpx;
  color: vars.$text-muted;
}

.batch-import,
.sync-link {
  font-size: 24rpx;
  color: vars.$primary-color;
}

.field-card {
  margin-bottom: vars.$spacing-md;
  padding-bottom: vars.$spacing-md;
  border-bottom: 1rpx solid #f0f0f0;
}

.field-card:last-child {
  border-bottom: none;
  margin-bottom: 0;
  padding-bottom: 0;
}

.field-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: vars.$spacing-sm;
}

.field-label {
  font-size: 28rpx;
  font-weight: 600;
  color: vars.$text-color;
}

.field-meta {
  font-size: 24rpx;
  color: vars.$text-muted;
}

.field-value {
  font-size: 28rpx;
  color: vars.$text-color;
  line-height: 1.6;
}

.channels {
  font-size: 28rpx;
  color: vars.$text-color;
  margin-bottom: vars.$spacing-sm;
}

.channel-item {
  color: vars.$text-color;
}

.channel-count {
  font-size: 24rpx;
  color: vars.$text-muted;
}

.skills {
  display: flex;
  flex-wrap: wrap;
  gap: vars.$spacing-sm;
}

.skill-tag {
  padding: 12rpx 24rpx;
  border-radius: vars.$border-radius-sm;
  font-size: 28rpx;
  background-color: #f5f5f5;
  color: vars.$text-color;
  transition: all 0.3s ease;
}

.skill-tag.selected {
  background-color: vars.$primary-color;
  color: #ffffff;
}

.suggestion-card {
  background-color: vars.$primary-color-soft;
  border-radius: vars.$border-radius-sm;
  padding: vars.$spacing-md;
  margin-bottom: vars.$spacing-md;
}

.suggestion-card:last-child {
  margin-bottom: 0;
}

.suggestion-title {
  font-size: 28rpx;
  font-weight: 600;
  color: vars.$text-color;
  display: block;
  margin-bottom: vars.$spacing-sm;
}

.suggestion-content {
  font-size: 28rpx;
  color: vars.$text-color;
  line-height: 1.6;
  display: block;
  margin-bottom: vars.$spacing-sm;
}

.suggestion-link {
  font-size: 24rpx;
  color: vars.$primary-color;
  display: block;
}

.compliance-card {
  background-color: #fff3e0;
  border-radius: vars.$border-radius-sm;
  padding: vars.$spacing-md;
}

.compliance-text {
  font-size: 28rpx;
  color: #f57c00;
  line-height: 1.6;
}

.footer-actions {
  display: flex;
  gap: vars.$spacing-md;
  padding: vars.$spacing-md;
  background-color: vars.$surface-color;
  border-top: 1rpx solid #f0f0f0;
}

.btn-secondary,
.btn-primary {
  flex: 1;
  height: 88rpx;
  border-radius: vars.$border-radius;
  font-size: 32rpx;
  border: none;
}

.btn-secondary {
  background-color: #f5f5f5;
  color: vars.$text-color;
}

.btn-primary {
  background-color: vars.$primary-color;
  color: #ffffff;
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




