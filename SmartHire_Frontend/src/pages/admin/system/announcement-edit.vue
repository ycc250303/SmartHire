<template>
  <view class="announcement-edit-page">
    <!-- 顶部导航栏 -->
    <view class="header-nav">
      <view class="nav-left" @click="goBack">
        <text class="back-icon">←</text>
      </view>
      <view class="nav-title">
        <text>{{ isEdit ? '编辑公告' : '发布公告' }}</text>
      </view>
      <view class="nav-right">
        <text class="save-btn" @click="saveAnnouncement">保存</text>
      </view>
    </view>

    <!-- 表单内容 -->
    <view class="form-container">
      <!-- 公告标题 -->
      <view class="form-item">
        <text class="form-label">公告标题 <text class="required">*</text></text>
        <input
          type="text"
          class="form-input"
          placeholder="请输入公告标题"
          v-model="formData.title"
          maxlength="50"
        />
        <text class="char-count">{{ formData.title.length }}/50</text>
      </view>

      <!-- 公告类型 -->
      <view class="form-item">
        <text class="form-label">公告类型 <text class="required">*</text></text>
        <picker
          :value="typeIndex"
          :range="typeOptions"
          range-key="label"
          @change="onTypeChange"
        >
          <view class="picker-content">
            <text>{{ formData.type ? getTypeText(formData.type) : '请选择公告类型' }}</text>
            <text class="picker-arrow">▼</text>
          </view>
        </picker>
      </view>

      <!-- 公告内容 -->
      <view class="form-item">
        <text class="form-label">公告内容 <text class="required">*</text></text>
        <textarea
          class="form-textarea"
          placeholder="请输入公告内容"
          v-model="formData.content"
          maxlength="1000"
          auto-height
        />
        <text class="char-count">{{ formData.content.length }}/1000</text>
      </view>

      <!-- 发布方式 -->
      <view class="form-item" v-if="!isEdit">
        <text class="form-label">发布方式</text>
        <view class="publish-options">
          <label
            class="publish-option"
            v-for="option in publishOptions"
            :key="option.value"
          >
            <radio
              :value="option.value"
              :checked="formData.publishType === option.value"
              @click="formData.publishType = option.value"
              color="#2f7cff"
            />
            <text>{{ option.label }}</text>
          </label>
        </view>
      </view>

      <!-- 定时发布时间 -->
      <view class="form-item" v-if="!isEdit && formData.publishType === 'scheduled'">
        <text class="form-label">发布时间</text>
        <picker
          mode="datetime"
          :value="formData.scheduledTime"
          @change="onDateTimeChange"
        >
          <view class="picker-content">
            <text>{{ formData.scheduledTime || '请选择发布时间' }}</text>
            <text class="picker-arrow">▼</text>
          </view>
        </picker>
      </view>
    </view>

    <!-- 底部操作按钮 -->
    <view class="bottom-actions">
      <button class="preview-btn" @click="previewAnnouncement">
        <text class="btn-icon">👁️</text>
        <text>预览</text>
      </button>
      <button class="submit-btn" @click="submitAnnouncement">
        <text>{{ isEdit ? '保存修改' : (formData.publishType === 'scheduled' ? '定时发布' : '立即发布') }}</text>
      </button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';

interface FormData {
  title: string;
  content: string;
  type: string;
  publishType: string;
  scheduledTime?: string;
}

const typeOptions = [
  { label: '系统通知', value: 'system' },
  { label: '维护通知', value: 'maintenance' },
  { label: '功能更新', value: 'feature' },
  { label: '紧急公告', value: 'urgent' }
];

const publishOptions = [
  { label: '立即发布', value: 'immediate' },
  { label: '定时发布', value: 'scheduled' }
];

const formData = ref<FormData>({
  title: '',
  content: '',
  type: '',
  publishType: 'immediate',
  scheduledTime: ''
});

const typeIndex = ref(0);
const isEdit = ref(false);
const editId = ref('');

const loadAnnouncementData = () => {
  // 获取页面参数
  const pages = getCurrentPages();
  const currentPage = pages[pages.length - 1];
  const options = currentPage.options;

  if (options && options.id) {
    isEdit.value = true;
    editId.value = options.id;

    // 模拟加载公告数据
    const mockData = {
      id: options.id,
      title: '系统维护通知',
      content: '亲爱的用户，我们将于今晚23:00-01:00进行系统维护，维护期间部分功能可能无法正常使用，敬请谅解。',
      type: 'maintenance',
      publishType: 'immediate'
    };

    formData.value = {
      title: mockData.title,
      content: mockData.content,
      type: mockData.type,
      publishType: mockData.publishType
    };

    typeIndex.value = typeOptions.findIndex(option => option.value === mockData.type);
  }
};

const goBack = () => {
  uni.navigateBack();
};

const onTypeChange = (e: any) => {
  typeIndex.value = e.detail.value;
  formData.value.type = typeOptions[typeIndex.value].value;
};

const onDateTimeChange = (e: any) => {
  formData.value.scheduledTime = e.detail.value;
};

const validateForm = (): boolean => {
  if (!formData.value.title.trim()) {
    uni.showToast({ title: '请输入公告标题', icon: 'none' });
    return false;
  }

  if (!formData.value.content.trim()) {
    uni.showToast({ title: '请输入公告内容', icon: 'none' });
    return false;
  }

  if (!formData.value.type) {
    uni.showToast({ title: '请选择公告类型', icon: 'none' });
    return false;
  }

  if (formData.value.publishType === 'scheduled' && !formData.value.scheduledTime) {
    uni.showToast({ title: '请选择发布时间', icon: 'none' });
    return false;
  }

  return true;
};

const saveAnnouncement = () => {
  if (!validateForm()) return;

  uni.showLoading({ title: '保存中...' });

  setTimeout(() => {
    uni.hideLoading();
    uni.showToast({
      title: isEdit.value ? '修改成功' : '保存成功',
      icon: 'success'
    });

    setTimeout(() => {
      goBack();
    }, 1500);
  }, 1500);
};

const submitAnnouncement = () => {
  if (!validateForm()) return;

  const actionText = isEdit.value
    ? '保存修改'
    : (formData.value.publishType === 'scheduled' ? '定时发布' : '立即发布');

  uni.showModal({
    title: '确认操作',
    content: `确定要${actionText}吗？`,
    success: (res) => {
      if (res.confirm) {
        uni.showLoading({ title: '处理中...' });

        setTimeout(() => {
          uni.hideLoading();
          uni.showToast({
            title: `${actionText}成功`,
            icon: 'success'
          });

          setTimeout(() => {
            goBack();
          }, 1500);
        }, 2000);
      }
    }
  });
};

const previewAnnouncement = () => {
  if (!validateForm()) return;

  // 构造预览数据
  const previewData = {
    title: formData.value.title,
    content: formData.value.content,
    type: formData.value.type,
    status: 'published',
    createTime: new Date(),
    publishTime: formData.value.publishType === 'immediate' ? new Date() : new Date(formData.value.scheduledTime || Date.now())
  };

  // 存储预览数据并跳转到预览页面
  uni.setStorageSync('announcement_preview', previewData);
  uni.navigateTo({
    url: '/pages/admin/system/announcement-preview'
  });
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

onMounted(() => {
  loadAnnouncementData();
});
</script>

<style scoped lang="scss">
.announcement-edit-page {
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

.save-btn {
  font-size: 28rpx;
  color: #2f7cff;
  font-weight: 500;
}

// 表单容器
.form-container {
  padding: 32rpx;
}

.form-item {
  background: #ffffff;
  border-radius: 20rpx;
  padding: 32rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 8rpx 24rpx rgba(31, 55, 118, 0.08);
}

.form-label {
  display: block;
  font-size: 28rpx;
  font-weight: 500;
  color: #2f3542;
  margin-bottom: 16rpx;
}

.required {
  color: #ff5f5f;
}

.form-input {
  width: 100%;
  height: 80rpx;
  padding: 0 24rpx;
  border: 2rpx solid #e5edff;
  border-radius: 16rpx;
  font-size: 28rpx;
  box-sizing: border-box;
  background: #f8faff;
}

.form-textarea {
  width: 100%;
  min-height: 200rpx;
  padding: 24rpx;
  border: 2rpx solid #e5edff;
  border-radius: 16rpx;
  font-size: 28rpx;
  box-sizing: border-box;
  background: #f8faff;
  line-height: 1.6;
}

.char-count {
  text-align: right;
  font-size: 24rpx;
  color: #97a0b3;
  margin-top: 12rpx;
  display: block;
}

.picker-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx;
  border: 2rpx solid #e5edff;
  border-radius: 16rpx;
  font-size: 28rpx;
  background: #f8faff;
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

// 底部操作按钮
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

.preview-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  padding: 24rpx 32rpx;
  background: #f0f2f7;
  color: #7a869a;
  border: none;
  border-radius: 20rpx;
  font-size: 28rpx;
  font-weight: 500;
  flex: 1;
}

.submit-btn {
  flex: 2;
  padding: 24rpx 0;
  background: #2f7cff;
  color: #ffffff;
  border: none;
  border-radius: 20rpx;
  font-size: 28rpx;
  font-weight: 500;
}

.btn-icon {
  font-size: 24rpx;
}
</style>