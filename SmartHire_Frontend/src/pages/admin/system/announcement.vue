<template>
  <view class="announcement-page">
    <!-- 头部操作栏 -->
    <view class="header-actions">
      <button class="create-btn" @click="goToCreatePage">发布公告</button>
    </view>

    <!-- 最近公告 -->
    <view class="recent-section">
      <view class="section-title">最近公告</view>
      <view class="announcement-list">
      <view
        class="announcement-item"
        v-for="announcement in announcements"
        :key="announcement.id"
      >
        <view class="announcement-header">
          <view class="announcement-title">{{ announcement.title }}</view>
          <view class="announcement-actions">
            <button
              class="action-btn"
              :class="announcement.status === 'published' ? 'unpublish' : 'publish'"
              @click="toggleStatus(announcement)"
            >
              {{ announcement.status === 'published' ? '下线' : '发布' }}
            </button>
            <button class="action-btn edit" @click="editAnnouncement(announcement)">
              编辑
            </button>
            <button class="action-btn delete" @click="deleteAnnouncement(announcement)">
              删除
            </button>
          </view>
        </view>

        <view class="announcement-content">
          <text class="content-text">{{ announcement.content }}</text>
        </view>

        <view class="announcement-meta">
          <view class="meta-left">
            <view class="announcement-status" :class="announcement.status">
              {{ getStatusText(announcement.status) }}
            </view>
            <text class="announcement-type">{{ getTypeText(announcement.type) }}</text>
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
    </view>

    <!-- 空状态 -->
    <view class="empty-state" v-if="announcements.length === 0">
      <view class="empty-icon">📢</view>
      <text class="empty-title">暂无公告</text>
      <text class="empty-desc">点击上方按钮发布第一条公告</text>
    </view>

    </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';

interface Announcement {
  id: string;
  title: string;
  content: string;
  type: 'system' | 'maintenance' | 'feature' | 'urgent';
  status: 'draft' | 'published' | 'expired';
  publishType?: 'immediate' | 'scheduled';
  createTime: Date;
  publishTime?: Date;
  readCount?: number;
  likeCount?: number;
  commentCount?: number;
}

const announcements = ref<Announcement[]>([]);

// 模拟公告数据
const mockAnnouncements: Announcement[] = [
  {
    id: '1',
    title: '系统维护通知',
    content: '亲爱的用户，我们将于今晚23:00-01:00进行系统维护，维护期间部分功能可能无法正常使用，敬请谅解。',
    type: 'maintenance',
    status: 'published',
    createTime: new Date(Date.now() - 2 * 60 * 60 * 1000),
    publishTime: new Date(Date.now() - 2 * 60 * 60 * 1000),
    readCount: 1250,
    likeCount: 45,
    commentCount: 12
  },
  {
    id: '2',
    title: '新功能上线公告',
    content: '很高兴地通知大家，我们最新上线了简历优化功能，可以帮助用户更好地展示自己的能力和经验。',
    type: 'feature',
    status: 'published',
    createTime: new Date(Date.now() - 1 * 24 * 60 * 60 * 1000),
    publishTime: new Date(Date.now() - 1 * 24 * 60 * 60 * 1000),
    readCount: 890,
    likeCount: 32,
    commentCount: 8
  },
  {
    id: '3',
    title: '平台使用规范更新',
    content: '为了维护良好的平台环境，我们更新了用户使用规范，请大家仔细阅读并遵守。',
    type: 'system',
    status: 'draft',
    createTime: new Date(Date.now() - 3 * 24 * 60 * 60 * 1000)
  }
];

const loadAnnouncements = () => {
  announcements.value = mockAnnouncements;
};

const goToCreatePage = () => {
  uni.navigateTo({
    url: '/pages/admin/system/announcement-edit'
  });
};

const editAnnouncement = (announcement: Announcement) => {
  uni.navigateTo({
    url: `/pages/admin/system/announcement-edit?id=${announcement.id}`
  });
};

const toggleStatus = (announcement: Announcement) => {
  const action = announcement.status === 'published' ? '下线' : '发布';
  uni.showModal({
    title: `确认${action}`,
    content: `确定要${action}公告《${announcement.title}》吗？`,
    success: (res) => {
      if (res.confirm) {
        announcement.status = announcement.status === 'published' ? 'draft' : 'published';
        if (announcement.status === 'published') {
          announcement.publishTime = new Date();
        }
        uni.showToast({ title: `${action}成功`, icon: 'success' });
      }
    }
  });
};

const deleteAnnouncement = (announcement: Announcement) => {
  uni.showModal({
    title: '删除公告',
    content: `确定要删除公告《${announcement.title}》吗？此操作不可恢复！`,
    success: (res) => {
      if (res.confirm) {
        const index = announcements.value.findIndex(a => a.id === announcement.id);
        if (index !== -1) {
          announcements.value.splice(index, 1);
          uni.showToast({ title: '删除成功', icon: 'success' });
        }
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
  loadAnnouncements();
});
</script>

<style scoped lang="scss">
.announcement-page {
  min-height: 100vh;
  background: #f6f7fb;
  padding: 32rpx;
}

.header-actions {
  margin-bottom: 24rpx;
}

.recent-section {
  margin-bottom: 32rpx;
}

.section-title {
  font-size: 30rpx;
  font-weight: 600;
  margin-bottom: 24rpx;
  color: #2f3542;
}

.create-btn {
  width: 100%;
  padding: 24rpx 0;
  background: #2f7cff;
  color: #ffffff;
  border: none;
  border-radius: 20rpx;
  font-size: 28rpx;
  font-weight: 500;
}

.announcement-list {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
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

.announcement-actions {
  display: flex;
  gap: 12rpx;
}

.action-btn {
  padding: 8rpx 16rpx;
  border-radius: 16rpx;
  font-size: 22rpx;
  font-weight: 500;
  border: none;
}

.action-btn.publish {
  background: #28a745;
  color: #ffffff;
}

.action-btn.unpublish {
  background: #ff8c00;
  color: #ffffff;
}

.action-btn.edit {
  background: #2f7cff;
  color: #ffffff;
}

.action-btn.delete {
  background: #ff5f5f;
  color: #ffffff;
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

.announcement-status {
  padding: 6rpx 16rpx;
  border-radius: 12rpx;
  font-size: 22rpx;
  font-weight: 500;
}

.announcement-status.draft {
  background: #f0f2f7;
  color: #7a869a;
}

.announcement-status.published {
  background: #f0f9ff;
  color: #28a745;
}

.announcement-status.expired {
  background: #fff5f5;
  color: #ff5f5f;
}

.announcement-type {
  font-size: 24rpx;
  color: #2f7cff;
  background: #e5edff;
  padding: 6rpx 16rpx;
  border-radius: 12rpx;
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

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 0;
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

.popup-content {
  background: #ffffff;
  border-radius: 24rpx;
  width: 600rpx;
  max-height: 80vh;
  overflow-y: auto;
}

.popup-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 32rpx;
  border-bottom: 2rpx solid #f0f2f7;
}

.popup-title {
  font-size: 32rpx;
  font-weight: 600;
}

.close-btn {
  font-size: 48rpx;
  color: #7a869a;
  line-height: 1;
}

.form-content {
  padding: 32rpx;
}

.form-item {
  margin-bottom: 32rpx;
}

.form-label {
  display: block;
  font-size: 28rpx;
  font-weight: 500;
  color: #2f3542;
  margin-bottom: 12rpx;
}

.form-input {
  width: 100%;
  height: 80rpx;
  padding: 0 24rpx;
  border: 2rpx solid #e5edff;
  border-radius: 16rpx;
  font-size: 28rpx;
  box-sizing: border-box;
}

.form-textarea {
  width: 100%;
  min-height: 200rpx;
  padding: 24rpx;
  border: 2rpx solid #e5edff;
  border-radius: 16rpx;
  font-size: 28rpx;
  box-sizing: border-box;
}

.char-count {
  text-align: right;
  font-size: 24rpx;
  color: #97a0b3;
  margin-top: 8rpx;
}

.picker-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx;
  border: 2rpx solid #e5edff;
  border-radius: 16rpx;
  font-size: 28rpx;
}

.picker-arrow {
  color: #7a869a;
  font-size: 20rpx;
}

.publish-options {
  display: flex;
  gap: 32rpx;
}

.publish-option {
  display: flex;
  align-items: center;
  gap: 12rpx;
  font-size: 28rpx;
}

.popup-actions {
  display: flex;
  gap: 16rpx;
  padding: 32rpx;
  border-top: 2rpx solid #f0f2f7;
}

.cancel-btn,
.confirm-btn {
  flex: 1;
  padding: 24rpx 0;
  border-radius: 16rpx;
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