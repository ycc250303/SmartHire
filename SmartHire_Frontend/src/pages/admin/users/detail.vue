<template>
  <view class="user-detail" v-if="userInfo">
    <!-- 用户基本信息 -->
    <view class="card">
      <view class="user-header">
        <view class="user-avatar">
          <image :src="userInfo.avatar || '/static/default-avatar.png'" mode="aspectFill" />
          <view class="user-status" :class="userInfo.status">{{ getStatusText(userInfo.status) }}</view>
        </view>
        <view class="user-info">
          <text class="user-name">{{ userInfo.name }}</text>
          <view class="user-type" :class="userInfo.type">{{ getUserTypeText(userInfo.type) }}</view>
          <text class="user-id">ID: {{ userInfo.id }}</text>
        </view>
        <button class="status-btn" :class="userInfo.status" @click="toggleUserStatus">
          {{ userInfo.status === 'active' ? '禁用' : '启用' }}
        </button>
      </view>

      <view class="contact-info">
        <view class="info-item">
          <text class="info-label">手机号</text>
          <text class="info-value">{{ userInfo.phone }}</text>
        </view>
        <view class="info-item">
          <text class="info-label">邮箱</text>
          <text class="info-value">{{ userInfo.email }}</text>
        </view>
        <view class="info-item" v-if="userInfo.company">
          <text class="info-label">{{ userInfo.type === 'hr' ? '公司' : '学校' }}</text>
          <text class="info-value">{{ userInfo.company }}</text>
        </view>
        <view class="info-item">
          <text class="info-label">所在地</text>
          <text class="info-value">{{ userInfo.location }}</text>
        </view>
      </view>
    </view>

    <!-- 账户统计 -->
    <view class="card">
      <view class="section-title">账户统计</view>
      <view class="stats-grid">
        <view class="stat-item">
          <text class="stat-value">{{ formatDateTime(userInfo.registerTime) }}</text>
          <text class="stat-label">注册时间</text>
        </view>
        <view class="stat-item">
          <text class="stat-value">{{ formatDateTime(userInfo.lastActiveTime) }}</text>
          <text class="stat-label">最后活跃</text>
        </view>
        <view class="stat-item">
          <text class="stat-value">{{ userInfo.loginCount || 0 }}</text>
          <text class="stat-label">登录次数</text>
        </view>
      </view>
    </view>

    <!-- HR用户信息 -->
    <view class="card" v-if="userInfo.type === 'hr'">
      <view class="section-title">HR工作统计</view>
      <view class="hr-stats">
        <view class="hr-stat-item">
          <text class="hr-stat-value">{{ userInfo.postedJobs || 0 }}</text>
          <text class="hr-stat-label">发布职位</text>
        </view>
        <view class="hr-stat-item">
          <text class="hr-stat-value">{{ userInfo.receivedApplications || 0 }}</text>
          <text class="hr-stat-label">收到申请</text>
        </view>
        <view class="hr-stat-item">
          <text class="hr-stat-value">{{ userInfo.viewedResumes || 0 }}</text>
          <text class="hr-stat-label">查看简历</text>
        </view>
        <view class="hr-stat-item">
          <text class="hr-stat-value">{{ userInfo.interviewCount || 0 }}</text>
          <text class="hr-stat-label">面试安排</text>
        </view>
      </view>

      <view class="recent-jobs" v-if="userInfo.recentJobs && userInfo.recentJobs.length > 0">
        <view class="subsection-title">最近发布的职位</view>
        <view class="job-list">
          <view class="job-item" v-for="job in userInfo.recentJobs" :key="job.id">
            <text class="job-title">{{ job.title }}</text>
            <text class="job-status" :class="job.status">{{ getJobStatusText(job.status) }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 求职者信息 -->
    <view class="card" v-if="userInfo.type === 'candidate'">
      <view class="section-title">求职统计</view>
      <view class="candidate-stats">
        <view class="candidate-stat-item">
          <text class="candidate-stat-value">{{ userInfo.submittedResumes || 0 }}</text>
          <text class="candidate-stat-label">投递简历</text>
        </view>
        <view class="candidate-stat-item">
          <text class="candidate-stat-value">{{ userInfo.interviews || 0 }}</text>
          <text class="candidate-stat-label">面试次数</text>
        </view>
        <view class="candidate-stat-item">
          <text class="candidate-stat-value">{{ userInfo.savedJobs || 0 }}</text>
          <text class="candidate-stat-label">收藏职位</text>
        </view>
        <view class="candidate-stat-item">
          <text class="candidate-stat-value">{{ userInfo.profileViews || 0 }}</text>
          <text class="candidate-stat-label">简历查看</text>
        </view>
      </view>

      <view class="resume-info" v-if="userInfo.resume">
        <view class="subsection-title">简历信息</view>
        <view class="resume-details">
          <view class="resume-item">
            <text class="resume-label">最高学历</text>
            <text class="resume-value">{{ userInfo.resume.education || '未填写' }}</text>
          </view>
          <view class="resume-item">
            <text class="resume-label">工作经验</text>
            <text class="resume-value">{{ userInfo.resume.experience || '未填写' }}</text>
          </view>
          <view class="resume-item">
            <text class="resume-label">期望薪资</text>
            <text class="resume-value">{{ userInfo.resume.expectedSalary || '未填写' }}</text>
          </view>
          <view class="resume-item">
            <text class="resume-label">求职状态</text>
            <text class="resume-value">{{ userInfo.resume.jobStatus || '未填写' }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 操作日志 -->
    <view class="card">
      <view class="section-title">操作记录</view>
      <view class="operation-logs" v-if="userInfo.operationLogs && userInfo.operationLogs.length > 0">
        <view class="log-item" v-for="log in userInfo.operationLogs" :key="log.id">
          <view class="log-header">
            <text class="log-action">{{ log.action }}</text>
            <text class="log-time">{{ formatDateTime(log.time) }}</text>
          </view>
          <text class="log-desc" v-if="log.description">{{ log.description }}</text>
        </view>
      </view>
      <view class="empty-logs" v-else>
        <text class="empty-text">暂无操作记录</text>
      </view>
    </view>

    <!-- 操作按钮 -->
    <view class="action-buttons">
      <button class="action-btn notify" @click="showNotifyModal">发送通知</button>
      <button class="action-btn reset" @click="resetPassword">重置密码</button>
      <button class="action-btn delete" @click="deleteUser">删除账号</button>
    </view>

    <!-- 通知弹窗 -->
    <uni-popup ref="notifyPopup" type="dialog">
      <uni-popup-dialog
        title="发送通知"
        placeholder="请输入通知内容..."
        @confirm="handleNotify"
      />
    </uni-popup>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';

interface ResumeInfo {
  education?: string;
  experience?: string;
  expectedSalary?: string;
  jobStatus?: string;
}

interface RecentJob {
  id: string;
  title: string;
  status: string;
}

interface OperationLog {
  id: string;
  action: string;
  time: Date;
  description?: string;
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
  loginCount?: number;
  postedJobs?: number;
  receivedApplications?: number;
  viewedResumes?: number;
  interviewCount?: number;
  submittedResumes?: number;
  interviews?: number;
  savedJobs?: number;
  profileViews?: number;
  recentJobs?: RecentJob[];
  resume?: ResumeInfo;
  operationLogs?: OperationLog[];
}

const userId = ref('');
const userInfo = ref<UserInfo | null>(null);
const notifyPopup = ref(null);

// 模拟用户详情数据
const mockUserInfo: UserInfo = {
  id: '1',
  name: '张三',
  phone: '138****1234',
  email: 'zhang***@email.com',
  type: 'candidate',
  status: 'active',
  avatar: '/static/user-avatar.png',
  location: '北京市朝阳区',
  registerTime: new Date(Date.now() - 30 * 24 * 60 * 60 * 1000),
  lastActiveTime: new Date(Date.now() - 2 * 60 * 60 * 1000),
  loginCount: 45,
  submittedResumes: 15,
  interviews: 3,
  savedJobs: 8,
  profileViews: 23,
  resume: {
    education: '本科',
    experience: '2-3年',
    expectedSalary: '15-20K',
    jobStatus: '求职中'
  },
  operationLogs: [
    {
      id: '1',
      action: '用户注册',
      time: new Date(Date.now() - 30 * 24 * 60 * 60 * 1000),
      description: '通过手机号注册成为求职者'
    },
    {
      id: '2',
      action: '完善简历',
      time: new Date(Date.now() - 25 * 24 * 60 * 60 * 1000),
      description: '填写了个人信息和工作经历'
    },
    {
      id: '3',
      action: '投递简历',
      time: new Date(Date.now() - 2 * 24 * 60 * 60 * 1000),
      description: '投递了前端开发工程师职位'
    }
  ]
};

const loadUserDetail = (id: string) => {
  // 模拟API调用
  userInfo.value = mockUserInfo;
};

const toggleUserStatus = () => {
  if (!userInfo.value) return;

  const action = userInfo.value.status === 'active' ? '禁用' : '启用';
  uni.showModal({
    title: `确认${action}`,
    content: `确定要${action}用户 ${userInfo.value.name} 吗？`,
    success: (res) => {
      if (res.confirm) {
        userInfo.value!.status = userInfo.value!.status === 'active' ? 'disabled' : 'active';
        uni.showToast({
          title: `${action}成功`,
          icon: 'success'
        });
      }
    }
  });
};

const showNotifyModal = () => {
  (notifyPopup.value as any).open();
};

const handleNotify = (content: string) => {
  if (!content.trim()) {
    uni.showToast({ title: '请输入通知内容', icon: 'none' });
    return;
  }

  uni.showLoading({ title: '发送中...' });

  setTimeout(() => {
    uni.hideLoading();
    uni.showToast({ title: '通知已发送', icon: 'success' });
    (notifyPopup.value as any).close();

    // 添加操作记录
    if (userInfo.value && userInfo.value.operationLogs) {
      userInfo.value.operationLogs.unshift({
        id: Date.now().toString(),
        action: '系统通知',
        time: new Date(),
        description: content
      });
    }
  }, 1500);
};

const resetPassword = () => {
  if (!userInfo.value) return;

  uni.showModal({
    title: '重置密码',
    content: `确定要重置用户 ${userInfo.value.name} 的密码吗？`,
    success: (res) => {
      if (res.confirm) {
        uni.showLoading({ title: '处理中...' });

        setTimeout(() => {
          uni.hideLoading();
          uni.showToast({ title: '密码重置成功', icon: 'success' });

          // 添加操作记录
          if (userInfo.value && userInfo.value.operationLogs) {
            userInfo.value.operationLogs.unshift({
              id: Date.now().toString(),
              action: '密码重置',
              time: new Date(),
              description: '管理员重置了用户密码'
            });
          }
        }, 1500);
      }
    }
  });
};

const deleteUser = () => {
  if (!userInfo.value) return;

  uni.showModal({
    title: '删除账号',
    content: `确定要删除用户 ${userInfo.value.name} 的账号吗？此操作不可恢复！`,
    success: (res) => {
      if (res.confirm) {
        uni.showLoading({ title: '处理中...' });

        setTimeout(() => {
          uni.hideLoading();
          uni.showToast({ title: '账号已删除', icon: 'success' });

          // 返回用户列表
          setTimeout(() => {
            uni.navigateBack();
          }, 1500);
        }, 1500);
      }
    }
  });
};

const getStatusText = (status: string): string => {
  const statusMap = {
    active: '正常',
    disabled: '禁用',
    pending: '待审核'
  };
  return statusMap[status] || status;
};

const getUserTypeText = (type: string): string => {
  const typeMap = {
    candidate: '求职者',
    hr: 'HR'
  };
  return typeMap[type] || type;
};

const getJobStatusText = (status: string): string => {
  const statusMap = {
    active: '招聘中',
    paused: '已暂停',
    closed: '已结束',
    pending: '待审核'
  };
  return statusMap[status] || status;
};

const formatDateTime = (date: Date): string => {
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
  const pages = getCurrentPages();
  const currentPage = pages[pages.length - 1];
  const options = currentPage.options as any;

  if (options.id) {
    userId.value = options.id;
    loadUserDetail(options.id);
  }
});
</script>

<style scoped lang="scss">
.user-detail {
  min-height: 100vh;
  padding: 32rpx;
  background: #f6f7fb;
  padding-bottom: 200rpx;
}

.card {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 32rpx;
  margin-bottom: 28rpx;
  box-shadow: 0 12rpx 32rpx rgba(31, 55, 118, 0.08);
}

.section-title {
  font-size: 30rpx;
  font-weight: 600;
  margin-bottom: 24rpx;
}

.subsection-title {
  font-size: 28rpx;
  font-weight: 500;
  margin: 24rpx 0 16rpx;
}

.user-header {
  display: flex;
  align-items: flex-start;
  margin-bottom: 32rpx;
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
  padding: 4rpx 12rpx;
  border-radius: 12rpx;
  font-size: 20rpx;
  font-weight: 500;
  color: #ffffff;
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
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.user-name {
  font-size: 32rpx;
  font-weight: 600;
}

.user-type {
  display: inline-block;
  padding: 6rpx 16rpx;
  border-radius: 16rpx;
  font-size: 22rpx;
  font-weight: 500;
  width: fit-content;
}

.user-type.candidate {
  background: #e5edff;
  color: #2f7cff;
}

.user-type.hr {
  background: #f0f9ff;
  color: #28a745;
}

.user-id {
  font-size: 24rpx;
  color: #97a0b3;
}

.status-btn {
  padding: 16rpx 24rpx;
  border-radius: 20rpx;
  font-size: 24rpx;
  font-weight: 500;
  border: none;
}

.status-btn.active {
  background: #ff5f5f;
  color: #ffffff;
}

.status-btn.disabled {
  background: #28a745;
  color: #ffffff;
}

.contact-info {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.info-item {
  display: flex;
  align-items: center;
}

.info-label {
  min-width: 120rpx;
  font-size: 28rpx;
  color: #7a869a;
}

.info-value {
  flex: 1;
  font-size: 28rpx;
  color: #2f3542;
}

.stats-grid {
  display: flex;
  gap: 20rpx;
}

.stat-item {
  flex: 1;
  background: #f8faff;
  border-radius: 16rpx;
  padding: 24rpx;
  text-align: center;
}

.stat-value {
  display: block;
  font-size: 28rpx;
  font-weight: 600;
  color: #2f7cff;
  margin-bottom: 8rpx;
}

.stat-label {
  font-size: 24rpx;
  color: #7a869a;
}

.hr-stats {
  display: flex;
  gap: 16rpx;
  margin-bottom: 24rpx;
}

.hr-stat-item {
  flex: 1;
  background: #f8faff;
  border-radius: 16rpx;
  padding: 32rpx 16rpx;
  text-align: center;
}

.hr-stat-value {
  display: block;
  font-size: 36rpx;
  font-weight: 600;
  color: #2f7cff;
  margin-bottom: 8rpx;
}

.hr-stat-label {
  font-size: 24rpx;
  color: #7a869a;
}

.recent-jobs {
  margin-top: 32rpx;
}

.job-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.job-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx;
  background: #f8faff;
  border-radius: 16rpx;
}

.job-title {
  font-size: 28rpx;
  color: #2f3542;
}

.job-status {
  padding: 6rpx 16rpx;
  border-radius: 12rpx;
  font-size: 22rpx;
  font-weight: 500;
}

.job-status.active {
  background: #f0f9ff;
  color: #28a745;
}

.job-status.paused {
  background: #fff7e6;
  color: #ff8c00;
}

.job-status.closed {
  background: #fff5f5;
  color: #ff5f5f;
}

.candidate-stats {
  display: flex;
  gap: 16rpx;
  margin-bottom: 24rpx;
}

.candidate-stat-item {
  flex: 1;
  background: #f8faff;
  border-radius: 16rpx;
  padding: 32rpx 16rpx;
  text-align: center;
}

.candidate-stat-value {
  display: block;
  font-size: 36rpx;
  font-weight: 600;
  color: #2f7cff;
  margin-bottom: 8rpx;
}

.candidate-stat-label {
  font-size: 24rpx;
  color: #7a869a;
}

.resume-info {
  margin-top: 32rpx;
}

.resume-details {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.resume-item {
  display: flex;
  align-items: center;
  padding: 20rpx;
  background: #f8faff;
  border-radius: 16rpx;
}

.resume-label {
  min-width: 120rpx;
  font-size: 28rpx;
  color: #7a869a;
}

.resume-value {
  flex: 1;
  font-size: 28rpx;
  color: #2f3542;
}

.operation-logs {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.log-item {
  padding: 20rpx;
  background: #f8faff;
  border-radius: 16rpx;
  border-left: 6rpx solid #2f7cff;
}

.log-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8rpx;
}

.log-action {
  font-size: 28rpx;
  font-weight: 500;
  color: #2f7cff;
}

.log-time {
  font-size: 24rpx;
  color: #97a0b3;
}

.log-desc {
  font-size: 26rpx;
  color: #6b758a;
}

.empty-logs {
  text-align: center;
  padding: 60rpx 0;
}

.empty-text {
  font-size: 28rpx;
  color: #97a0b3;
}

.action-buttons {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #ffffff;
  padding: 24rpx 32rpx;
  border-top: 2rpx solid #f0f2f7;
  display: flex;
  gap: 16rpx;
}

.action-btn {
  flex: 1;
  padding: 24rpx 0;
  border-radius: 20rpx;
  font-size: 28rpx;
  font-weight: 500;
  border: none;
}

.action-btn.notify {
  background: #2f7cff;
  color: #ffffff;
}

.action-btn.reset {
  background: #ff8c00;
  color: #ffffff;
}

.action-btn.delete {
  background: #ff5f5f;
  color: #ffffff;
}
</style>