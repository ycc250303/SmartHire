<template>
  <view class="review-page">
    <!-- 筛选栏 -->
    <view class="filter-bar">
      <view class="filter-tabs">
        <view
          class="filter-tab"
          v-for="tab in statusTabs"
          :key="tab.value"
          :class="{ active: currentStatus === tab.value }"
          @click="changeStatus(tab.value)"
        >
          {{ tab.label }}
          <view class="tab-count" v-if="tab.count > 0">{{ tab.count }}</view>
        </view>
      </view>
    </view>

    <!-- 搜索栏 -->
    <view class="search-bar">
      <view class="search-input">
        <input
          type="text"
          placeholder="搜索职位名称、公司名称..."
          v-model="searchKeyword"
          @confirm="handleSearch"
        />
        <view class="search-icon" @click="handleSearch">🔍</view>
      </view>
    </view>

    <!-- 职位列表 -->
    <view class="job-list">
      <view
        class="job-item"
        v-for="job in filteredJobs"
        :key="job.id"
        @click="goToDetail(job.id)"
      >
        <view class="job-header">
          <view class="job-title">{{ job.title }}</view>
          <view class="job-status" :class="job.status">
            {{ getStatusText(job.status) }}
          </view>
        </view>

        <view class="company-info">
          <text class="company-name">{{ job.company }}</text>
          <text class="job-location">· {{ job.location }}</text>
        </view>

        <view class="job-info">
          <view class="info-tags">
            <text class="tag" v-for="tag in job.tags" :key="tag">{{ tag }}</text>
          </view>
          <text class="salary">{{ job.salary }}</text>
        </view>

        <view class="submitter-info">
          <text class="submitter">发布者: {{ job.submitter }}</text>
          <text class="submit-time">{{ formatTime(job.submitTime) }}</text>
        </view>

        <view class="job-actions" v-if="job.status === 'pending'">
          <button class="action-btn approve" @click.stop="quickApprove(job.id)">
            通过
          </button>
          <button class="action-btn reject" @click.stop="quickReject(job.id)">
            拒绝
          </button>
        </view>
      </view>
    </view>

    <!-- 加载更多 -->
    <view class="load-more" v-if="hasMore" @click="loadMore">
      <text>{{ loading ? '加载中...' : '加载更多' }}</text>
    </view>

    <!-- 空状态 -->
    <view class="empty-state" v-if="filteredJobs.length === 0 && !loading">
      <view class="empty-icon">📋</view>
      <text class="empty-title">暂无相关职位</text>
      <text class="empty-desc">没有找到符合条件的招聘信息</text>
    </view>

    </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';

interface StatusTab {
  label: string;
  value: string;
  count: number;
}

interface JobItem {
  id: string;
  title: string;
  company: string;
  location: string;
  salary: string;
  tags: string[];
  status: 'pending' | 'approved' | 'rejected' | 'modified';
  submitter: string;
  submitTime: Date;
}

const statusTabs = ref<StatusTab[]>([
  { label: '待审核', value: 'pending', count: 12 },
  { label: '已通过', value: 'approved', count: 156 },
  { label: '已拒绝', value: 'rejected', count: 23 },
  { label: '需修改', value: 'modified', count: 5 }
]);

const currentStatus = ref('pending');
const searchKeyword = ref('');
const jobs = ref<JobItem[]>([]);
const loading = ref(false);
const hasMore = ref(true);

// 模拟数据
const mockJobs: JobItem[] = [
  {
    id: '1',
    title: '前端开发工程师',
    company: '科技有限公司',
    location: '北京',
    salary: '15-25K',
    tags: ['3-5年', '本科', 'Vue.js', 'React'],
    status: 'pending',
    submitter: '张HR',
    submitTime: new Date(Date.now() - 2 * 60 * 60 * 1000)
  },
  {
    id: '2',
    title: '产品经理',
    company: '互联网公司',
    location: '上海',
    salary: '20-35K',
    tags: ['5-10年', '本科', 'B端产品'],
    status: 'pending',
    submitter: '李经理',
    submitTime: new Date(Date.now() - 4 * 60 * 60 * 1000)
  },
  {
    id: '3',
    title: 'UI设计师',
    company: '设计工作室',
    location: '深圳',
    salary: '12-20K',
    tags: ['2-3年', '专科', 'Figma'],
    status: 'approved',
    submitter: '王设计',
    submitTime: new Date(Date.now() - 1 * 24 * 60 * 60 * 1000)
  },
  {
    id: '4',
    title: '后端开发工程师',
    company: '金融科技公司',
    location: '杭州',
    salary: '18-30K',
    tags: ['3-5年', '本科', 'Java', 'Spring'],
    status: 'rejected',
    submitter: '赵HR',
    submitTime: new Date(Date.now() - 6 * 60 * 60 * 1000)
  },
  {
    id: '5',
    title: '运营专员',
    company: '电商平台',
    location: '广州',
    salary: '8-15K',
    tags: ['1-3年', '专科', '用户运营'],
    status: 'modified',
    submitter: '陈运营',
    submitTime: new Date(Date.now() - 8 * 60 * 60 * 1000)
  }
];

const filteredJobs = computed(() => {
  let filtered = jobs.value.filter(job => job.status === currentStatus.value);

  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase();
    filtered = filtered.filter(job =>
      job.title.toLowerCase().includes(keyword) ||
      job.company.toLowerCase().includes(keyword)
    );
  }

  return filtered;
});

const changeStatus = (status: string) => {
  currentStatus.value = status;
  loadJobs();
};

const handleSearch = () => {
  loadJobs();
};

const loadJobs = () => {
  loading.value = true;
  // 模拟API调用
  setTimeout(() => {
    jobs.value = mockJobs;
    loading.value = false;
    hasMore.value = false;
  }, 1000);
};

const loadMore = () => {
  if (loading.value) return;
  loading.value = true;
  // 模拟加载更多数据
  setTimeout(() => {
    // jobs.value.push(...moreJobs);
    hasMore.value = false;
    loading.value = false;
  }, 1000);
};

const goToDetail = (jobId: string) => {
  uni.navigateTo({
    url: `/pages/admin/review/detail?id=${jobId}`
  });
};

const quickApprove = async (jobId: string) => {
  uni.showModal({
    title: '确认通过',
    content: '确定要通过这个职位的审核吗？',
    success: async (res) => {
      if (res.confirm) {
        // 调用审核通过API
        const job = jobs.value.find(j => j.id === jobId);
        if (job) {
          job.status = 'approved';
          uni.showToast({ title: '审核通过', icon: 'success' });
          updateStatusCount();
        }
      }
    }
  });
};

const quickReject = async (jobId: string) => {
  uni.showModal({
    title: '确认拒绝',
    content: '确定要拒绝这个职位的审核吗？',
    success: async (res) => {
      if (res.confirm) {
        // 调用审核拒绝API
        const job = jobs.value.find(j => j.id === jobId);
        if (job) {
          job.status = 'rejected';
          uni.showToast({ title: '已拒绝', icon: 'success' });
          updateStatusCount();
        }
      }
    }
  });
};


const getStatusText = (status: string): string => {
  const statusMap = {
    pending: '待审核',
    approved: '已通过',
    rejected: '已拒绝',
    modified: '需修改'
  };
  return statusMap[status] || status;
};

const formatTime = (date: Date): string => {
  const now = new Date();
  const diff = now.getTime() - date.getTime();
  const hours = Math.floor(diff / (1000 * 60 * 60));
  const days = Math.floor(diff / (1000 * 60 * 60 * 24));

  if (days > 0) {
    return `${days}天前`;
  }
  if (hours > 0) {
    return `${hours}小时前`;
  }
  return '刚刚';
};

const updateStatusCount = () => {
  const counts = jobs.value.reduce((acc, job) => {
    acc[job.status] = (acc[job.status] || 0) + 1;
    return acc;
  }, {} as Record<string, number>);

  statusTabs.value.forEach(tab => {
    tab.count = counts[tab.value] || 0;
  });
};

onMounted(() => {
  loadJobs();
});
</script>

<style scoped lang="scss">
.review-page {
  min-height: 100vh;
  background: #f6f7fb;
}

.filter-bar {
  background: #ffffff;
  padding: 24rpx 32rpx;
  border-bottom: 2rpx solid #f0f2f7;
}

.filter-tabs {
  display: flex;
  gap: 24rpx;
}

.filter-tab {
  display: flex;
  align-items: center;
  padding: 16rpx 24rpx;
  border-radius: 32rpx;
  font-size: 28rpx;
  color: #7a869a;
  background: #f0f2f7;
  position: relative;
}

.filter-tab.active {
  background: #2f7cff;
  color: #ffffff;
}

.tab-count {
  margin-left: 8rpx;
  padding: 4rpx 12rpx;
  background: rgba(255, 255, 255, 0.3);
  border-radius: 16rpx;
  font-size: 22rpx;
  font-weight: 500;
}

.search-bar {
  padding: 24rpx 32rpx;
  background: #ffffff;
}

.search-input {
  display: flex;
  align-items: center;
  background: #f0f2f7;
  border-radius: 32rpx;
  padding: 0 32rpx;
}

.search-input input {
  flex: 1;
  height: 72rpx;
  font-size: 28rpx;
  padding: 0 16rpx;
}

.search-icon {
  font-size: 32rpx;
  color: #7a869a;
}

.job-list {
  padding: 0 32rpx;
}

.job-item {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 32rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 12rpx 32rpx rgba(31, 55, 118, 0.08);
}

.job-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16rpx;
}

.job-title {
  font-size: 32rpx;
  font-weight: 600;
  flex: 1;
  margin-right: 16rpx;
}

.job-status {
  padding: 8rpx 20rpx;
  border-radius: 20rpx;
  font-size: 24rpx;
  font-weight: 500;
}

.job-status.pending {
  background: #fff7e6;
  color: #ff8c00;
}

.job-status.approved {
  background: #f0f9ff;
  color: #28a745;
}

.job-status.rejected {
  background: #fff5f5;
  color: #ff5f5f;
}

.job-status.modified {
  background: #fff0f6;
  color: #e91e63;
}

.company-info {
  margin-bottom: 16rpx;
}

.company-name {
  font-size: 28rpx;
  color: #2f7cff;
  font-weight: 500;
}

.job-location {
  font-size: 26rpx;
  color: #7a869a;
}

.job-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.info-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
}

.tag {
  padding: 8rpx 16rpx;
  background: #f0f2f7;
  border-radius: 16rpx;
  font-size: 22rpx;
  color: #7a869a;
}

.salary {
  font-size: 30rpx;
  font-weight: 600;
  color: #ff6b35;
}

.submitter-info {
  display: flex;
  justify-content: space-between;
  font-size: 24rpx;
  color: #97a0b3;
  margin-bottom: 20rpx;
}

.job-actions {
  display: flex;
  gap: 16rpx;
}

.action-btn {
  flex: 1;
  padding: 20rpx 0;
  border-radius: 20rpx;
  font-size: 28rpx;
  font-weight: 500;
  border: none;
}

.action-btn.approve {
  background: #28a745;
  color: #ffffff;
}

.action-btn.reject {
  background: #ff5f5f;
  color: #ffffff;
}

.load-more {
  padding: 32rpx;
  text-align: center;
  color: #7a869a;
  font-size: 28rpx;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 32rpx;
}

.empty-icon {
  font-size: 80rpx;
  margin-bottom: 24rpx;
}

.empty-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #7a869a;
  margin-bottom: 8rpx;
}

.empty-desc {
  font-size: 26rpx;
  color: #97a0b3;
}

</style>