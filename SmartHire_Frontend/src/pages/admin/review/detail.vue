<template>
  <view class="review-detail" v-if="jobDetail">
    <!-- 职位基本信息 -->
    <view class="card">
      <view class="section-title">职位信息</view>
      <view class="job-header">
        <view class="job-title">{{ jobDetail.title }}</view>
        <view class="job-status" :class="jobDetail.status">
          {{ getStatusText(jobDetail.status) }}
        </view>
      </view>

      <view class="info-group">
        <view class="info-item">
          <text class="info-label">公司名称</text>
          <text class="info-value">{{ jobDetail.company }}</text>
        </view>
        <view class="info-item">
          <text class="info-label">工作地点</text>
          <text class="info-value">{{ jobDetail.location }}</text>
        </view>
        <view class="info-item">
          <text class="info-label">薪资范围</text>
          <text class="info-value">{{ jobDetail.salary }}</text>
        </view>
        <view class="info-item">
          <text class="info-label">工作类型</text>
          <text class="info-value">{{ jobDetail.workType }}</text>
        </view>
      </view>
    </view>

    <!-- 职位要求 -->
    <view class="card">
      <view class="section-title">职位要求</view>
      <view class="requirements">
        <view class="requirement-item" v-for="(req, index) in jobDetail.requirements" :key="index">
          <text class="requirement-text">{{ req }}</text>
        </view>
      </view>
    </view>

    <!-- 岗位职责 -->
    <view class="card">
      <view class="section-title">岗位职责</view>
      <view class="responsibilities">
        <view class="responsibility-item" v-for="(resp, index) in jobDetail.responsibilities" :key="index">
          <text class="responsibility-text">{{ resp }}</text>
        </view>
      </view>
    </view>

    <!-- 公司信息 -->
    <view class="card">
      <view class="section-title">公司信息</view>
      <view class="company-info">
        <view class="info-item">
          <text class="info-label">公司规模</text>
          <text class="info-value">{{ jobDetail.companyInfo.size }}</text>
        </view>
        <view class="info-item">
          <text class="info-label">所属行业</text>
          <text class="info-value">{{ jobDetail.companyInfo.industry }}</text>
        </view>
        <view class="info-item">
          <text class="info-label">公司简介</text>
          <text class="info-value">{{ jobDetail.companyInfo.description }}</text>
        </view>
      </view>
    </view>

    <!-- 发布者信息 -->
    <view class="card">
      <view class="section-title">发布者信息</view>
      <view class="submitter-info">
        <view class="submitter-profile">
          <view class="avatar">
            <image :src="jobDetail.submitter.avatar" mode="aspectFill" />
          </view>
          <view class="submitter-details">
            <text class="submitter-name">{{ jobDetail.submitter.name }}</text>
            <text class="submitter-title">{{ jobDetail.submitter.title }}</text>
            <text class="submitter-contact">{{ jobDetail.submitter.contact }}</text>
          </view>
        </view>
        <view class="submit-time">
          <text class="time-label">发布时间</text>
          <text class="time-value">{{ formatDateTime(jobDetail.submitTime) }}</text>
        </view>
      </view>
    </view>

    <!-- 审核历史 -->
    <view class="card" v-if="jobDetail.auditHistory && jobDetail.auditHistory.length > 0">
      <view class="section-title">审核历史</view>
      <view class="audit-history">
        <view class="history-item" v-for="history in jobDetail.auditHistory" :key="history.id">
          <view class="history-header">
            <text class="history-action" :class="history.action">{{ getActionText(history.action) }}</text>
            <text class="history-time">{{ formatDateTime(history.time) }}</text>
          </view>
          <text class="history-admin">审核员: {{ history.adminName }}</text>
          <text class="history-reason" v-if="history.reason">原因: {{ history.reason }}</text>
        </view>
      </view>
    </view>

    <!-- 审核操作 -->
    <view class="review-actions" v-if="jobDetail.status === 'pending'">
      <button class="action-btn approve" @click="showApproveModal">
        通过审核
      </button>
      <button class="action-btn reject" @click="showRejectModal">
        拒绝
      </button>
      <button class="action-btn modify" @click="showModifyModal">
        要求修改
      </button>
    </view>
  </view>

  <!-- 审核拒绝弹窗 -->
  <uni-popup ref="rejectPopup" type="dialog">
    <uni-popup-dialog
      title="拒绝原因"
      placeholder="请输入拒绝原因..."
      @confirm="handleReject"
    />
  </uni-popup>

  <!-- 要求修改弹窗 -->
  <uni-popup ref="modifyPopup" type="dialog">
    <uni-popup-dialog
      title="修改要求"
      placeholder="请输入需要修改的内容..."
      @confirm="handleModify"
    />
  </uni-popup>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';

interface SubmitterInfo {
  name: string;
  title: string;
  contact: string;
  avatar: string;
}

interface CompanyInfo {
  size: string;
  industry: string;
  description: string;
}

interface AuditHistory {
  id: string;
  action: 'approve' | 'reject' | 'modify';
  adminName: string;
  time: Date;
  reason?: string;
}

interface JobDetail {
  id: string;
  title: string;
  company: string;
  location: string;
  salary: string;
  workType: string;
  status: 'pending' | 'approved' | 'rejected' | 'modified';
  requirements: string[];
  responsibilities: string[];
  companyInfo: CompanyInfo;
  submitter: SubmitterInfo;
  submitTime: Date;
  auditHistory?: AuditHistory[];
}

const jobId = ref('');
const jobDetail = ref<JobDetail | null>(null);
const rejectPopup = ref(null);
const modifyPopup = ref(null);

// 模拟职位详情数据
const mockJobDetail: JobDetail = {
  id: '1',
  title: '高级前端开发工程师',
  company: '科技有限公司',
  location: '北京市朝阳区',
  salary: '20-35K',
  workType: '全职',
  status: 'pending',
  requirements: [
    '本科及以上学历，计算机相关专业',
    '5年以上前端开发经验',
    '精通Vue.js/React等主流框架',
    '熟悉前端工程化和性能优化',
    '良好的沟通能力和团队协作精神'
  ],
  responsibilities: [
    '负责公司产品的前端开发工作',
    '参与产品需求讨论和技术方案设计',
    '优化前端性能，提升用户体验',
    '负责前端技术选型和架构设计',
    '指导和培养初级开发人员'
  ],
  companyInfo: {
    size: '100-500人',
    industry: '互联网/软件服务',
    description: '一家专注于企业服务的科技公司，致力于为客户提供优质的技术解决方案。'
  },
  submitter: {
    name: '张经理',
    title: 'HR经理',
    contact: 'zhang@company.com',
    avatar: '/static/user-avatar.png'
  },
  submitTime: new Date(Date.now() - 2 * 60 * 60 * 1000),
  auditHistory: []
};

const loadJobDetail = (id: string) => {
  // 模拟API调用
  jobDetail.value = mockJobDetail;
};

const showApproveModal = () => {
  uni.showModal({
    title: '确认通过',
    content: '确定要通过这个职位的审核吗？',
    success: (res) => {
      if (res.confirm) {
        handleApprove();
      }
    }
  });
};

const handleApprove = async () => {
  if (!jobDetail.value) return;

  // 调用审核通过API
  uni.showLoading({ title: '处理中...' });

  setTimeout(() => {
    jobDetail.value!.status = 'approved';
    uni.hideLoading();
    uni.showToast({ title: '审核通过', icon: 'success' });

    // 添加到审核历史
    addAuditHistory('approve', '审核通过');
  }, 1500);
};

const showRejectModal = () => {
  (rejectPopup.value as any).open();
};

const handleReject = async (reason: string) => {
  if (!jobDetail.value || !reason.trim()) {
    uni.showToast({ title: '请输入拒绝原因', icon: 'none' });
    return;
  }

  uni.showLoading({ title: '处理中...' });

  setTimeout(() => {
    jobDetail.value!.status = 'rejected';
    uni.hideLoading();
    uni.showToast({ title: '已拒绝', icon: 'success' });

    addAuditHistory('reject', reason);
    (rejectPopup.value as any).close();
  }, 1500);
};

const showModifyModal = () => {
  (modifyPopup.value as any).open();
};

const handleModify = async (requirement: string) => {
  if (!jobDetail.value || !requirement.trim()) {
    uni.showToast({ title: '请输入修改要求', icon: 'none' });
    return;
  }

  uni.showLoading({ title: '处理中...' });

  setTimeout(() => {
    jobDetail.value!.status = 'modified';
    uni.hideLoading();
    uni.showToast({ title: '已要求修改', icon: 'success' });

    addAuditHistory('modify', requirement);
    (modifyPopup.value as any).close();
  }, 1500);
};

const addAuditHistory = (action: string, reason: string) => {
  if (!jobDetail.value) return;

  const history: AuditHistory = {
    id: Date.now().toString(),
    action: action as any,
    adminName: '当前管理员',
    time: new Date(),
    reason: reason
  };

  if (!jobDetail.value.auditHistory) {
    jobDetail.value.auditHistory = [];
  }
  jobDetail.value.auditHistory.unshift(history);
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

const getActionText = (action: string): string => {
  const actionMap = {
    approve: '通过',
    reject: '拒绝',
    modify: '要求修改'
  };
  return actionMap[action] || action;
};

const formatDateTime = (date: Date): string => {
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  });
};

onMounted(() => {
  // 获取页面参数
  const pages = getCurrentPages();
  const currentPage = pages[pages.length - 1];
  const options = currentPage.options as any;

  if (options.id) {
    jobId.value = options.id;
    loadJobDetail(options.id);
  }
});
</script>

<style scoped lang="scss">
.review-detail {
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

.job-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24rpx;
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

.info-group {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.info-item {
  display: flex;
  align-items: flex-start;
}

.info-label {
  min-width: 160rpx;
  font-size: 28rpx;
  color: #7a869a;
}

.info-value {
  flex: 1;
  font-size: 28rpx;
  color: #2f3542;
  line-height: 1.5;
}

.requirements,
.responsibilities {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.requirement-item,
.responsibility-item {
  display: flex;
  align-items: flex-start;
}

.requirement-item::before {
  content: '•';
  color: #2f7cff;
  font-weight: bold;
  margin-right: 12rpx;
}

.responsibility-item::before {
  content: '•';
  color: #28a745;
  font-weight: bold;
  margin-right: 12rpx;
}

.requirement-text,
.responsibility-text {
  flex: 1;
  font-size: 28rpx;
  line-height: 1.5;
}

.submitter-profile {
  display: flex;
  gap: 24rpx;
  margin-bottom: 24rpx;
}

.avatar {
  width: 120rpx;
  height: 120rpx;
  border-radius: 20rpx;
  overflow: hidden;
}

.avatar image {
  width: 100%;
  height: 100%;
}

.submitter-details {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.submitter-name {
  font-size: 30rpx;
  font-weight: 600;
}

.submitter-title {
  font-size: 26rpx;
  color: #7a869a;
}

.submitter-contact {
  font-size: 26rpx;
  color: #2f7cff;
}

.submit-time {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.time-label {
  font-size: 26rpx;
  color: #7a869a;
}

.time-value {
  font-size: 28rpx;
  color: #2f3542;
}

.audit-history {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.history-item {
  padding: 20rpx;
  background: #f8faff;
  border-radius: 16rpx;
  border-left: 6rpx solid #e0e6ed;
}

.history-item .history-action.approve {
  color: #28a745;
}

.history-item .history-action.reject {
  color: #ff5f5f;
}

.history-item .history-action.modify {
  color: #e91e63;
}

.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8rpx;
}

.history-action {
  font-size: 28rpx;
  font-weight: 500;
}

.history-time {
  font-size: 24rpx;
  color: #7a869a;
}

.history-admin,
.history-reason {
  font-size: 26rpx;
  color: #6b758a;
  margin-bottom: 4rpx;
}

.review-actions {
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

.action-btn.approve {
  background: #28a745;
  color: #ffffff;
}

.action-btn.reject {
  background: #ff5f5f;
  color: #ffffff;
}

.action-btn.modify {
  background: #2f7cff;
  color: #ffffff;
}
</style>