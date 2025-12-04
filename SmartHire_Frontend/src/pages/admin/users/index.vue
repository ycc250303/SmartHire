<template>
  <view class="users-page">
    <!-- 用户类型筛选 -->
    <view class="filter-bar">
      <view class="filter-tabs">
        <view
          class="filter-tab"
          v-for="tab in userTabs"
          :key="tab.value"
          :class="{ active: currentUserType === tab.value }"
          @click="changeUserType(tab.value)"
        >
          {{ tab.label }}
          <view class="tab-count" v-if="tab.count > 0">{{ formatNumber(tab.count) }}</view>
        </view>
      </view>
    </view>

    <!-- 筛选条件 -->
    <view class="advanced-filters">
      <view class="filter-section">
        <view class="section-title">注册时间</view>
        <view class="time-options">
          <view
            class="time-option"
            v-for="option in timeOptions"
            :key="option.value"
            :class="{ active: filters.registerTime === option.value }"
            @click="selectTimeFilter(option.value)"
          >
            {{ option.label }}
          </view>
        </view>
      </view>

      <view class="filter-section">
        <view class="section-title">用户状态</view>
        <view class="status-options">
          <view
            class="status-option"
            v-for="status in statusOptions"
            :key="status.value"
            :class="{ active: filters.status === status.value }"
            @click="selectStatusFilter(status.value)"
          >
            {{ status.label }}
          </view>
        </view>
      </view>
    </view>

    <!-- 搜索和筛选栏 -->
    <view class="search-filter-bar">
      <view class="search-input">
        <input
          type="text"
          placeholder="搜索用户姓名、手机号、邮箱..."
          v-model="searchKeyword"
          @confirm="handleSearch"
        />
        <view class="search-icon" @click="handleSearch">🔍</view>
      </view>

      <view class="clear-btn" @click="clearAllFilters">
        <text class="clear-text">清除</text>
        <text class="clear-icon">✕</text>
      </view>
    </view>

    <!-- 用户统计概览 -->
    <view class="stats-overview">
      <view class="stat-item">
        <text class="stat-value">{{ formatNumber(totalUsers) }}</text>
        <text class="stat-label">总用户数</text>
      </view>
      <view class="stat-item">
        <text class="stat-value">{{ formatNumber(activeUsers) }}</text>
        <text class="stat-label">活跃用户</text>
      </view>
      <view class="stat-item">
        <text class="stat-value">{{ formatNumber(newUsers) }}</text>
        <text class="stat-label">今日新增</text>
      </view>
    </view>

    <!-- 用户列表 -->
    <view class="user-list">
      <view
        class="user-item"
        v-for="user in filteredUsers"
        :key="user.id"
        @click="goToUserDetail(user.id)"
      >
        <view class="user-avatar">
          <image :src="user.avatar || '/static/default-avatar.png'" mode="aspectFill" />
          <view class="user-status" :class="user.status"></view>
        </view>

        <view class="user-info">
          <view class="user-header">
            <text class="user-name">{{ user.name }}</text>
            <view class="user-type" :class="user.type">{{ getUserTypeText(user.type) }}</view>
          </view>

          <view class="user-contact">
            <text class="contact-item">{{ user.phone }}</text>
            <text class="contact-item">{{ user.email }}</text>
          </view>

          <view class="user-meta">
            <text class="meta-item">{{ user.company || '个人' }}</text>
            <text class="meta-item">{{ user.location }}</text>
            <text class="meta-item">{{ formatTime(user.registerTime) }}</text>
          </view>

          <view class="user-stats" v-if="user.type === 'hr'">
            <text class="stat-text">发布职位: {{ user.postedJobs || 0 }}</text>
            <text class="stat-text">收到申请: {{ user.receivedApplications || 0 }}</text>
          </view>
          <view class="user-stats" v-else>
            <text class="stat-text">投递简历: {{ user.submittedResumes || 0 }}</text>
            <text class="stat-text">面试次数: {{ user.interviews || 0 }}</text>
          </view>

          <view class="user-actions" @click.stop>
            <button
              class="action-btn"
              :class="user.status === 'active' ? 'disable' : 'enable'"
              @click="toggleUserStatus(user)"
            >
              {{ user.status === 'active' ? '禁用' : '启用' }}
            </button>
            <button class="action-btn notify" @click="notifyUser(user)">
              通知
            </button>
          </view>
        </view>
      </view>
    </view>

    <!-- 加载更多 -->
    <view class="load-more" v-if="hasMore" @click="loadMore">
      <text>{{ loading ? '加载中...' : '加载更多' }}</text>
    </view>

    <!-- 空状态 -->
    <view class="empty-state" v-if="filteredUsers.length === 0 && !loading">
      <view class="empty-icon">👥</view>
      <text class="empty-title">暂无相关用户</text>
      <text class="empty-desc">没有找到符合条件的用户</text>
    </view>

    </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';

interface UserTab {
  label: string;
  value: string;
  count: number;
}

interface UserInfo {
  id: string;
  name: string;
  phone: string;
  email: string;
  type: 'candidate' | 'hr';
  status: 'active' | 'disabled' | 'pending';
  avatar?: string;
  company?: string;
  location: string;
  registerTime: Date;
  lastActiveTime: Date;
  postedJobs?: number;
  receivedApplications?: number;
  submittedResumes?: number;
  interviews?: number;
}

interface FilterOptions {
  registerTime: string;
  status: string;
}

const userTabs = ref<UserTab[]>([
  { label: '全部', value: 'all', count: 2847 },
  { label: '求职者', value: 'candidate', count: 1923 },
  { label: 'HR', value: 'hr', count: 924 }
]);

const currentUserType = ref('all');
const searchKeyword = ref('');
const users = ref<UserInfo[]>([]);
const loading = ref(false);
const hasMore = ref(true);
// 筛选条件
const filters = ref<FilterOptions>({
  registerTime: '',
  status: ''
});

const timeOptions = [
  { label: '全部', value: '' },
  { label: '今天', value: 'today' },
  { label: '本周', value: 'week' },
  { label: '本月', value: 'month' },
  { label: '三月内', value: 'quarter' }
];

const statusOptions = [
  { label: '全部', value: '' },
  { label: '正常', value: 'active' },
  { label: '禁用', value: 'disabled' },
  { label: '待审核', value: 'pending' }
];

// 统计数据
const totalUsers = ref(2847);
const activeUsers = ref(1892);
const newUsers = ref(38);

// 模拟用户数据
const mockUsers: UserInfo[] = [
  {
    id: '1',
    name: '张三',
    phone: '138****1234',
    email: 'zhang***@email.com',
    type: 'candidate',
    status: 'active',
    avatar: '/static/user-avatar.png',
    location: '北京',
    registerTime: new Date(Date.now() - 30 * 24 * 60 * 60 * 1000),
    lastActiveTime: new Date(Date.now() - 2 * 60 * 60 * 1000),
    submittedResumes: 15,
    interviews: 3
  },
  {
    id: '2',
    name: '李四',
    phone: '139****5678',
    email: 'li***@company.com',
    type: 'hr',
    status: 'active',
    avatar: '/static/user-avatar.png',
    company: '科技有限公司',
    location: '上海',
    registerTime: new Date(Date.now() - 60 * 24 * 60 * 60 * 1000),
    lastActiveTime: new Date(Date.now() - 1 * 60 * 60 * 1000),
    postedJobs: 8,
    receivedApplications: 45
  },
  {
    id: '3',
    name: '王五',
    phone: '137****9012',
    email: 'wang***@email.com',
    type: 'candidate',
    status: 'disabled',
    avatar: '/static/user-avatar.png',
    location: '深圳',
    registerTime: new Date(Date.now() - 15 * 24 * 60 * 60 * 1000),
    lastActiveTime: new Date(Date.now() - 5 * 24 * 60 * 60 * 1000),
    submittedResumes: 7,
    interviews: 1
  },
  {
    id: '4',
    name: '赵六',
    phone: '136****3456',
    email: 'zhao***@tech.com',
    type: 'hr',
    status: 'pending',
    avatar: '/static/user-avatar.png',
    company: '互联网公司',
    location: '杭州',
    registerTime: new Date(Date.now() - 3 * 24 * 60 * 60 * 1000),
    lastActiveTime: new Date(Date.now() - 3 * 60 * 60 * 1000),
    postedJobs: 2,
    receivedApplications: 12
  }
];

const filteredUsers = computed(() => {
  let filtered = users.value;

  // 按用户类型筛选
  if (currentUserType.value !== 'all') {
    filtered = filtered.filter(user => user.type === currentUserType.value);
  }

  // 按关键词搜索
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase();
    filtered = filtered.filter(user =>
      user.name.toLowerCase().includes(keyword) ||
      user.phone.includes(keyword) ||
      user.email.toLowerCase().includes(keyword) ||
      (user.company && user.company.toLowerCase().includes(keyword))
    );
  }

  // 按筛选条件过滤
  if (filters.value.registerTime) {
    filtered = filterByRegisterTime(filtered, filters.value.registerTime);
  }

  if (filters.value.status) {
    filtered = filtered.filter(user => user.status === filters.value.status);
  }

  return filtered;
});

const changeUserType = (type: string) => {
  currentUserType.value = type;
  loadUsers();
};

const handleSearch = () => {
  loadUsers();
};

const loadUsers = () => {
  loading.value = true;
  // 模拟API调用
  setTimeout(() => {
    users.value = mockUsers;
    loading.value = false;
    hasMore.value = false;
  }, 1000);
};

const loadMore = () => {
  if (loading.value) return;
  loading.value = true;
  setTimeout(() => {
    hasMore.value = false;
    loading.value = false;
  }, 1000);
};

const goToUserDetail = (userId: string) => {
  uni.navigateTo({
    url: `/pages/admin/users/detail?id=${userId}`
  });
};

const toggleUserStatus = (user: UserInfo) => {
  const action = user.status === 'active' ? '禁用' : '启用';
  uni.showModal({
    title: `确认${action}`,
    content: `确定要${action}用户 ${user.name} 吗？`,
    success: (res) => {
      if (res.confirm) {
        user.status = user.status === 'active' ? 'disabled' : 'active';
        uni.showToast({
          title: `${action}成功`,
          icon: 'success'
        });
      }
    }
  });
};

const notifyUser = (user: UserInfo) => {
  uni.showModal({
    title: '发送通知',
    content: `确定要给用户 ${user.name} 发送系统通知吗？`,
    success: (res) => {
      if (res.confirm) {
        // 调用发送通知API
        uni.showToast({
          title: '通知已发送',
          icon: 'success'
        });
      }
    }
  });
};

const selectTimeFilter = (value: string) => {
  filters.value.registerTime = value;
  loadUsers();
};

const selectStatusFilter = (value: string) => {
  filters.value.status = value;
  loadUsers();
};

const clearAllFilters = () => {
  // 清除所有筛选条件
  filters.value = {
    registerTime: '',
    status: ''
  };
  currentUserType.value = 'all';
  searchKeyword.value = '';

  // 重新加载数据
  loadUsers();

  uni.showToast({ title: '已清除所有筛选条件', icon: 'success' });
};

const filterByRegisterTime = (users: UserInfo[], timeRange: string): UserInfo[] => {
  const now = new Date();
  return users.filter(user => {
    const registerTime = new Date(user.registerTime);
    const diffDays = Math.floor((now.getTime() - registerTime.getTime()) / (1000 * 60 * 60 * 24));

    switch (timeRange) {
      case 'today':
        return diffDays === 0;
      case 'week':
        return diffDays <= 7;
      case 'month':
        return diffDays <= 30;
      case 'quarter':
        return diffDays <= 90;
      default:
        return true;
    }
  });
};

const getUserTypeText = (type: string): string => {
  const typeMap = {
    candidate: '求职者',
    hr: 'HR'
  };
  return typeMap[type] || type;
};

const formatNumber = (num: number): string => {
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + 'w';
  }
  if (num >= 1000) {
    return (num / 1000).toFixed(1) + 'k';
  }
  return num.toString();
};

const formatTime = (date: Date): string => {
  const now = new Date();
  const diff = now.getTime() - date.getTime();
  const days = Math.floor(diff / (1000 * 60 * 60 * 24));

  if (days === 0) return '今天';
  if (days === 1) return '昨天';
  if (days < 7) return `${days}天前`;
  if (days < 30) return `${Math.floor(days / 7)}周前`;
  if (days < 365) return `${Math.floor(days / 30)}个月前`;
  return `${Math.floor(days / 365)}年前`;
};

onMounted(() => {
  loadUsers();
});
</script>

<style scoped lang="scss">
.users-page {
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

.search-filter-bar {
  display: flex;
  gap: 16rpx;
  padding: 24rpx 32rpx;
  background: #ffffff;
}

// 筛选条件区域
.advanced-filters {
  background: #ffffff;
  padding: 24rpx 32rpx;
  border-top: 2rpx solid #f0f2f7;
  border-bottom: 2rpx solid #f0f2f7;
  position: relative;
  z-index: 1;
}

.search-input {
  flex: 1;
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

.clear-btn {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 0 24rpx;
  background: #ff5f5f;
  border-radius: 32rpx;
}

.clear-text {
  font-size: 28rpx;
  color: #ffffff;
}

.clear-icon {
  font-size: 24rpx;
  color: #ffffff;
}

.advanced-filters .filter-section {
  margin-bottom: 24rpx;
}

.advanced-filters .filter-section:last-child {
  margin-bottom: 0;
}

.advanced-filters .section-title {
  font-size: 26rpx;
  font-weight: 500;
  color: #2f3542;
  margin-bottom: 16rpx;
}

.advanced-filters .time-options,
.advanced-filters .status-options {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
}

.advanced-filters .time-option,
.advanced-filters .status-option {
  padding: 12rpx 20rpx;
  border-radius: 20rpx;
  font-size: 24rpx;
  color: #7a869a;
  background: #f0f2f7;
  transition: all 0.2s;
  border: 2rpx solid transparent;
}

.advanced-filters .time-option.active,
.advanced-filters .status-option.active {
  background: #2f7cff;
  color: #ffffff;
}

.stats-overview {
  display: flex;
  gap: 16rpx;
  padding: 24rpx 32rpx;
}

.stat-item {
  flex: 1;
  background: #ffffff;
  border-radius: 16rpx;
  padding: 24rpx;
  text-align: center;
  box-shadow: 0 4rpx 12rpx rgba(31, 55, 118, 0.08);
}

.stat-value {
  display: block;
  font-size: 32rpx;
  font-weight: 600;
  color: #2f7cff;
  margin-bottom: 8rpx;
}

.stat-label {
  font-size: 24rpx;
  color: #7a869a;
}

.user-list {
  padding: 0 32rpx;
}

.user-item {
  display: flex;
  background: #ffffff;
  border-radius: 24rpx;
  padding: 24rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 12rpx 32rpx rgba(31, 55, 118, 0.08);
}

.user-avatar {
  position: relative;
  margin-right: 24rpx;
}

.user-avatar image {
  width: 120rpx;
  height: 120rpx;
  border-radius: 20rpx;
}

.user-status {
  position: absolute;
  bottom: 4rpx;
  right: 4rpx;
  width: 24rpx;
  height: 24rpx;
  border-radius: 50%;
  border: 4rpx solid #ffffff;
}

.user-status.active {
  background: #28a745;
}

.user-status.disabled {
  background: #ff5f5f;
}

.user-status.pending {
  background: #ff8c00;
}

.user-info {
  flex: 1;
}

.user-header {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-bottom: 8rpx;
}

.user-name {
  font-size: 30rpx;
  font-weight: 600;
}

.user-type {
  padding: 6rpx 16rpx;
  border-radius: 16rpx;
  font-size: 22rpx;
  font-weight: 500;
}

.user-type.candidate {
  background: #e5edff;
  color: #2f7cff;
}

.user-type.hr {
  background: #f0f9ff;
  color: #28a745;
}

.user-contact {
  display: flex;
  gap: 16rpx;
  margin-bottom: 8rpx;
}

.contact-item {
  font-size: 26rpx;
  color: #7a869a;
}

.user-meta {
  display: flex;
  gap: 16rpx;
  margin-bottom: 12rpx;
}

.meta-item {
  font-size: 24rpx;
  color: #97a0b3;
}

.user-stats {
  display: flex;
  gap: 16rpx;
}

.stat-text {
  font-size: 24rpx;
  color: #2f7cff;
  background: #f0f7ff;
  padding: 4rpx 12rpx;
  border-radius: 12rpx;
}

.user-actions {
  display: flex;
  gap: 12rpx;
  margin-top: 12rpx;
}

.action-btn {
  padding: 8rpx 16rpx;
  border-radius: 12rpx;
  font-size: 22rpx;
  font-weight: 500;
  border: none;
  min-width: 80rpx;
  transition: all 0.2s;
}

.action-btn:active {
  transform: scale(0.95);
}

.action-btn.disable {
  background: #ff5f5f;
  color: #ffffff;
}

.action-btn.enable {
  background: #28a745;
  color: #ffffff;
}

.action-btn.notify {
  background: #2f7cff;
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