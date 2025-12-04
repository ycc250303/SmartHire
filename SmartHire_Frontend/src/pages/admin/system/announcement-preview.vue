<template>
  <view class="announcement-preview-page">
    <!-- 顶部导航栏 -->
    <view class="header-nav">
      <view class="nav-left" @click="goBack">
        <text class="back-icon">←</text>
      </view>
      <view class="nav-title">
        <text>公告预览</text>
      </view>
      <view class="nav-right">
        <text class="edit-btn" @click="goToEdit">编辑</text>
      </view>
    </view>

    <!-- 预览内容 -->
    <view class="preview-container">
      <view class="announcement-item">
        <view class="announcement-header">
          <view class="announcement-title">{{ announcement.title }}</view>
          <view class="announcement-status published">
            {{ getStatusText('published') }}
          </view>
        </view>

        <view class="announcement-content">
          <text class="content-text">{{ announcement.content }}</text>
        </view>

        <view class="announcement-meta">
          <view class="meta-left">
            <view class="announcement-type" :class="announcement.type">
              {{ getTypeText(announcement.type) }}
            </view>
          </view>
          <view class="meta-right">
            <text class="create-time">创建时间: {{ formatDateTime(announcement.createTime) }}</text>
            <text class="publish-time" v-if="announcement.publishTime">
              发布时间: {{ formatDateTime(announcement.publishTime) }}
            </text>
          </view>
        </view>

        <view class="announcement-stats" v-if="announcement.status === 'published'">
          <text class="stat-item">阅读量: {{ announcement.readCount || 0 }}</text>
          <text class="stat-item">点赞: {{ announcement.likeCount || 0 }}</text>
          <text class="stat-item">评论: {{ announcement.commentCount || 0 }}</text>
        </view>
      </view>
    </view>

    <!-- 底部操作 -->
    <view class="bottom-actions">
      <button class="cancel-btn" @click="goBack">返回编辑</button>
      <button class="confirm-btn" @click="confirmPublish">
        {{ isScheduled ? '确认定时发布' : '确认发布' }}
      </button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';

interface Announcement {
  title: string;
  content: string;
  type: 'system' | 'maintenance' | 'feature' | 'urgent';
  status: 'draft' | 'published' | 'expired';
  createTime: Date;
  publishTime?: Date;
  readCount?: number;
  likeCount?: number;
  commentCount?: number;
}

const announcement = ref<Announcement>({
  title: '',
  content: '',
  type: 'system',
  status: 'published',
  createTime: new Date(),
  publishTime: new Date()
});

const isScheduled = ref(false);

const loadPreviewData = () => {
  try {
    const data = uni.getStorageSync('announcement_preview');
    if (data) {
      announcement.value = data;
      // 判断是否为定时发布
      isScheduled.value = !!(announcement.value.publishTime &&
        announcement.value.publishTime > announcement.value.createTime &&
        announcement.value.publishTime > new Date());
    } else {
      uni.showToast({ title: '预览数据不存在', icon: 'none' });
      setTimeout(() => {
        goBack();
      }, 1500);
    }
  } catch (error) {
    console.error('加载预览数据失败:', error);
    uni.showToast({ title: '加载失败', icon: 'none' });
    setTimeout(() => {
      goBack();
    }, 1500);
  }
};

const goBack = () => {
  uni.navigateBack();
};

const goToEdit = () => {
  goBack();
};

const confirmPublish = () => {
  const pages = getCurrentPages();
  const currentPage = pages[pages.length - 2]; // 返回到编辑页面

  uni.showModal({
    title: '确认操作',
    content: `确定要${isScheduled.value ? '定时发布' : '立即发布'}这条公告吗？`,
    success: (res) => {
      if (res.confirm) {
        // 清除预览数据
        uni.removeStorageSync('announcement_preview');

        // 显示成功提示
        uni.showLoading({ title: '发布中...' });

        setTimeout(() => {
          uni.hideLoading();
          uni.showToast({
            title: `${isScheduled.value ? '定时发布' : '发布'}成功`,
            icon: 'success'
          });

          // 返回到上一级页面（公告列表）
          uni.navigateBack({
            delta: 2
          });
        }, 1500);
      }
    }
  });
};

const getStatusText = (status: string): string => {
  const statusMap = {
    draft: '草稿',
    published: '已发布',
    expired: '已过期'
  };
  return statusMap[status] || status;
};

const getTypeText = (type: string): string => {
  const typeMap = {
    system: '系统通知',
    maintenance: '维护通知',
    feature: '功能更新',
    urgent: '紧急公告'
  };
  return typeMap[type] || type;
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
  loadPreviewData();
});
</script>

<style scoped lang="scss">
.announcement-preview-page {
  min-height: 100vh;
  background: #f6f7fb;
  padding-bottom: 120rpx;
}

// 顶部导航栏
.header-nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #ffffff;
  padding: 24rpx 32rpx;
  box-shadow: 0 2rpx 8rpx rgba(31, 55, 118, 0.08);
}

.nav-left {
  width: 80rpx;
  display: flex;
  align-items: center;
}

.back-icon {
  font-size: 36rpx;
  font-weight: 600;
  color: #2f7cff;
}

.nav-title {
  flex: 1;
  text-align: center;
  font-size: 32rpx;
  font-weight: 600;
  color: #2f3542;
}

.nav-right {
  width: 80rpx;
  display: flex;
  justify-content: flex-end;
}

.edit-btn {
  font-size: 28rpx;
  color: #2f7cff;
  font-weight: 500;
}

// 预览容器
.preview-container {
  padding: 32rpx;
}

.announcement-item {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 32rpx;
  box-shadow: 0 12rpx 32rpx rgba(31, 55, 118, 0.08);
}

.announcement-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16rpx;
}

.announcement-title {
  flex: 1;
  font-size: 30rpx;
  font-weight: 600;
  color: #2f3542;
  margin-right: 16rpx;
}

.announcement-status {
  padding: 6rpx 16rpx;
  border-radius: 12rpx;
  font-size: 22rpx;
  font-weight: 500;
}

.announcement-status.published {
  background: #f0f9ff;
  color: #28a745;
}

.announcement-content {
  margin-bottom: 20rpx;
}

.content-text {
  font-size: 28rpx;
  line-height: 1.6;
  color: #6b758a;
}

.announcement-meta {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16rpx;
  flex-wrap: wrap;
  gap: 12rpx;
}

.meta-left {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.announcement-type {
  font-size: 24rpx;
  color: #ffffff;
  padding: 6rpx 16rpx;
  border-radius: 12rpx;
}

.announcement-type.system {
  background: #2f7cff;
}

.announcement-type.maintenance {
  background: #ff8c00;
}

.announcement-type.feature {
  background: #28a745;
}

.announcement-type.urgent {
  background: #ff5f5f;
}

.meta-right {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.create-time,
.publish-time {
  font-size: 24rpx;
  color: #97a0b3;
}

.announcement-stats {
  display: flex;
  gap: 20rpx;
  padding-top: 16rpx;
  border-top: 2rpx solid #f0f2f7;
}

.stat-item {
  font-size: 24rpx;
  color: #7a869a;
}

// 底部操作
.bottom-actions {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  gap: 16rpx;
  padding: 24rpx 32rpx;
  background: #ffffff;
  box-shadow: 0 -8rpx 24rpx rgba(31, 55, 118, 0.08);
}

.cancel-btn,
.confirm-btn {
  flex: 1;
  padding: 24rpx 0;
  border-radius: 20rpx;
  font-size: 28rpx;
  font-weight: 500;
  border: none;
}

.cancel-btn {
  background: #f0f2f7;
  color: #7a869a;
}

.confirm-btn {
  background: #2f7cff;
  color: #ffffff;
}
</style>