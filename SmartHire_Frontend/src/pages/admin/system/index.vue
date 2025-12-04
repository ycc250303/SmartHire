<template>
  <view class="system-page">
    <!-- 顶部状态概览 -->
    <view class="status-overview">
      <view class="status-card">
        <view class="status-indicator online"></view>
        <text class="status-title">系统运行正常</text>
        <text class="status-uptime">运行时间：15天 8小时</text>
      </view>
    </view>

    <!-- 功能入口 -->
    <view class="function-grid">
      <view class="function-item" @click="goToAnnouncement">
        <view class="function-icon">📢</view>
        <text class="function-title">公告管理</text>
        <text class="function-desc">发布和管理系统公告</text>
        <view class="function-badge" v-if="announcementCount > 0">{{ announcementCount }}</view>
      </view>

      <view class="function-item" @click="goToLogs">
        <view class="function-icon">📋</view>
        <text class="function-title">操作日志</text>
        <text class="function-desc">查看系统操作记录</text>
      </view>

      <view class="function-item" @click="goToReports">
        <view class="function-icon">⚠️</view>
        <text class="function-title">举报处理</text>
        <text class="function-desc">处理用户举报内容</text>
        <view class="function-badge" v-if="reportCount > 0">{{ reportCount }}</view>
      </view>

      <view class="function-item" @click="goToSettings">
        <view class="function-icon">⚙️</view>
        <text class="function-title">系统设置</text>
        <text class="function-desc">配置平台参数</text>
      </view>
    </view>

    <!-- 系统监控数据 -->
    <view class="monitor-section">
      <view class="monitor-title">系统监控</view>
      <view class="monitor-grid">
        <view class="monitor-item">
          <text class="monitor-label">服务状态</text>
          <text class="monitor-value normal">正常</text>
        </view>
        <view class="monitor-item">
          <text class="monitor-label">API响应</text>
          <text class="monitor-value normal">120ms</text>
        </view>
        <view class="monitor-item">
          <text class="monitor-label">服务器负载</text>
          <text class="monitor-value warning">65%</text>
        </view>
        <view class="monitor-item">
          <text class="monitor-label">磁盘空间</text>
          <text class="monitor-value normal">78%</text>
        </view>
      </view>
    </view>

    <!-- 待处理事项 -->
    <view class="pending-section">
      <view class="section-title">待处理事项</view>
      <view class="pending-list">
        <view class="pending-item" @click="goToReview">
          <view class="pending-icon">📋</view>
          <view class="pending-content">
            <text class="pending-title">待审核职位</text>
            <text class="pending-desc">{{ pendingJobs }} 个职位待审核</text>
          </view>
          <view class="pending-count">{{ pendingJobs }}</view>
        </view>

        <view class="pending-item" @click="goToReports">
          <view class="pending-icon">⚠️</view>
          <view class="pending-content">
            <text class="pending-title">待处理举报</text>
            <text class="pending-desc">{{ reportCount }} 条举报待处理</text>
          </view>
          <view class="pending-count">{{ reportCount }}</view>
        </view>
      </view>
    </view>

    <!-- 快捷操作 -->
    <view class="action-section">
      <view class="section-title">快捷操作</view>
      <view class="action-buttons">
        <button class="action-btn primary" @click="showCacheModal">
          <text class="action-icon">🧹</text>
          <text class="action-text">清理缓存</text>
        </button>
        <button class="action-btn secondary" @click="showBackupModal">
          <text class="action-icon">💾</text>
          <text class="action-text">数据备份</text>
        </button>
        <button class="action-btn secondary" @click="showMaintenanceModal">
          <text class="action-icon">🔧</text>
          <text class="action-text">维护模式</text>
        </button>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';

// 统计数据
const announcementCount = ref(5);
const reportCount = ref(12);
const pendingJobs = ref(8);

const goToAnnouncement = () => {
  uni.navigateTo({ url: '/pages/admin/system/announcement' });
};

const goToReports = () => {
  // 可以扩展举报处理页面
  uni.showToast({ title: '举报处理功能开发中', icon: 'none' });
};

const goToLogs = () => {
  uni.navigateTo({ url: '/pages/admin/system/logs' });
};

const goToReview = () => {
  uni.switchTab({ url: '/pages/admin/review/index' });
};

const goToSettings = () => {
  uni.showToast({ title: '系统设置功能开发中', icon: 'none' });
};

const showCacheModal = () => {
  uni.showModal({
    title: '清理缓存',
    content: '确定要清理系统缓存吗？',
    success: (res) => {
      if (res.confirm) {
        uni.showLoading({ title: '清理中...' });
        setTimeout(() => {
          uni.hideLoading();
          uni.showToast({ title: '缓存清理完成', icon: 'success' });
        }, 2000);
      }
    }
  });
};

const showBackupModal = () => {
  uni.showModal({
    title: '数据备份',
    content: '确定要开始数据备份吗？',
    success: (res) => {
      if (res.confirm) {
        uni.showLoading({ title: '备份中...' });
        setTimeout(() => {
          uni.hideLoading();
          uni.showToast({ title: '备份完成', icon: 'success' });
        }, 3000);
      }
    }
  });
};

const showMaintenanceModal = () => {
  uni.showActionSheet({
    itemList: ['启用维护模式', '关闭维护模式'],
    success: (res) => {
      const action = res.tapIndex === 0 ? '启用' : '关闭';
      uni.showModal({
        title: `${action}维护模式`,
        content: `确定要${action}维护模式吗？`,
        success: (modalRes) => {
          if (modalRes.confirm) {
            uni.showLoading({ title: '处理中...' });
            setTimeout(() => {
              uni.hideLoading();
              uni.showToast({ title: `已${action}维护模式`, icon: 'success' });
            }, 1500);
          }
        }
      });
    }
  });
};


onMounted(() => {
  // 加载系统数据
});
</script>

<style scoped lang="scss">
.system-page {
  min-height: 100vh;
  padding: 32rpx;
  background: #f6f7fb;
}

// 顶部状态概览
.status-overview {
  margin-bottom: 32rpx;
}

.status-card {
  background: linear-gradient(135deg, #2f7cff 0%, #4a8aff 100%);
  border-radius: 24rpx;
  padding: 40rpx 32rpx;
  display: flex;
  align-items: center;
  color: white;
  box-shadow: 0 12rpx 32rpx rgba(47, 124, 255, 0.3);
}

.status-indicator {
  width: 24rpx;
  height: 24rpx;
  border-radius: 50%;
  background: #4ade80;
  margin-right: 20rpx;
  box-shadow: 0 0 12rpx rgba(74, 222, 128, 0.5);
}

.status-title {
  font-size: 32rpx;
  font-weight: 600;
  margin-right: 20rpx;
}

.status-uptime {
  font-size: 24rpx;
  opacity: 0.8;
}

// 功能网格
.function-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20rpx;
  margin-bottom: 32rpx;
}

.function-item {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 32rpx 24rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  box-shadow: 0 8rpx 24rpx rgba(31, 55, 118, 0.08);
  position: relative;
  transition: transform 0.2s;
}

.function-item:active {
  transform: scale(0.95);
}

.function-icon {
  font-size: 48rpx;
  margin-bottom: 16rpx;
}

.function-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #2f3542;
  margin-bottom: 8rpx;
}

.function-desc {
  font-size: 22rpx;
  color: #7a869a;
  line-height: 1.4;
}

.function-badge {
  position: absolute;
  top: 20rpx;
  right: 20rpx;
  padding: 6rpx 12rpx;
  background: #ff5f5f;
  color: #ffffff;
  border-radius: 16rpx;
  font-size: 20rpx;
  font-weight: 500;
  min-width: 32rpx;
  text-align: center;
}

// 监控数据
.monitor-section {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 32rpx;
  margin-bottom: 32rpx;
  box-shadow: 0 8rpx 24rpx rgba(31, 55, 118, 0.08);
}

.monitor-title {
  font-size: 30rpx;
  font-weight: 600;
  margin-bottom: 24rpx;
  color: #2f3542;
}

.monitor-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24rpx;
}

.monitor-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: 20rpx 0;
}

.monitor-label {
  font-size: 24rpx;
  color: #7a869a;
  margin-bottom: 12rpx;
}

.monitor-value {
  font-size: 28rpx;
  font-weight: 600;
  color: #2f3542;
}

.monitor-value.normal {
  color: #28a745;
}

.monitor-value.warning {
  color: #ff8c00;
}

.monitor-value.error {
  color: #ff5f5f;
}

// 待处理事项
.pending-section {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 32rpx;
  margin-bottom: 32rpx;
  box-shadow: 0 8rpx 24rpx rgba(31, 55, 118, 0.08);
}

.section-title {
  font-size: 30rpx;
  font-weight: 600;
  margin-bottom: 24rpx;
  color: #2f3542;
}

.pending-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.pending-item {
  display: flex;
  align-items: center;
  padding: 20rpx;
  background: #f8faff;
  border-radius: 16rpx;
  border-left: 6rpx solid #2f7cff;
  transition: background-color 0.2s;
}

.pending-item:active {
  background: #eef2ff;
}

.pending-icon {
  font-size: 32rpx;
  margin-right: 16rpx;
}

.pending-content {
  flex: 1;
}

.pending-title {
  font-size: 28rpx;
  font-weight: 500;
  color: #2f3542;
  margin-bottom: 4rpx;
}

.pending-desc {
  font-size: 24rpx;
  color: #7a869a;
}

.pending-count {
  font-size: 28rpx;
  font-weight: 600;
  color: #2f7cff;
  min-width: 48rpx;
  text-align: center;
}

// 快捷操作
.action-section {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 32rpx;
  box-shadow: 0 8rpx 24rpx rgba(31, 55, 118, 0.08);
}

.action-buttons {
  display: flex;
  gap: 16rpx;
}

.action-btn {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24rpx 16rpx;
  border-radius: 20rpx;
  font-size: 24rpx;
  font-weight: 500;
  border: none;
  transition: transform 0.2s;
}

.action-btn:active {
  transform: scale(0.95);
}

.action-btn.primary {
  background: #2f7cff;
  color: #ffffff;
}

.action-btn.secondary {
  background: #e5edff;
  color: #2f7cff;
}

.action-icon {
  font-size: 32rpx;
  margin-bottom: 8rpx;
}

.action-text {
  font-size: 24rpx;
}
</style>