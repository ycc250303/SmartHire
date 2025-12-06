<template>
  <view class="profile-page">
    <view class="card" v-if="hrInfo">
      <view class="name-row">
        <view class="profile-info">
          <view class="avatar-wrapper">
            <image :src="avatarImg" mode="aspectFill" />
          </view>
          <view>
            <view class="name">{{ hrInfo.realName || '-' }}</view>
            <view class="role">{{ hrInfo.position || '招聘者' }} · {{ hrInfo.companyName || '公司未填写' }}</view>
          </view>
        </view>
        <view class="badge">HR</view>
      </view>
      <view class="info">工号/UID：{{ hrInfo.userId }}</view>
      <view class="info">公司ID：{{ hrInfo.companyId }}</view>
    </view>

    <view class="card">
      <view class="section-title">基础信息</view>
      <view class="form-item">
        <text class="label">真实姓名</text>
        <input v-model="form.realName" placeholder="请输入真实姓名" />
      </view>
      <view class="form-item">
        <text class="label">职位</text>
        <input v-model="form.position" placeholder="如：资深招聘/HRBP" />
      </view>
      <view class="form-item">
        <text class="label">工作电话</text>
        <input v-model="form.workPhone" placeholder="手机或座机" />
      </view>
      <button class="primary" :disabled="saving" @click="handleSave">保存</button>
    </view>

    <view class="card logout-card">
      <button class="logout" @click="logout">退出登录</button>
    </view>

    <view class="loading" v-if="loading">
      <text>加载中...</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { getHrInfo, updateHrInfo, type HrInfoUpdatePayload, type HrInfo } from '@/services/api/hr';
import { useHrStore } from '@/store/hr';
import avatarImg from '@/static/user-avatar.png';

const hrInfo = ref<HrInfo | null>(null);
const form = ref<HrInfoUpdatePayload>({
  realName: '',
  position: '',
  workPhone: '',
});
const loading = ref(false);
const saving = ref(false);
const hrStore = useHrStore();

const loadProfile = async () => {
  loading.value = true;
  try {
    const data = await getHrInfo();
    hrInfo.value = data;
    form.value = {
      realName: data.realName,
      position: data.position,
      workPhone: data.workPhone,
    };
    hrStore.setCompanyName(data.companyName);
    hrStore.setCompanyId(data.companyId);
  } catch (error) {
    console.error('Failed to load HR info:', error);
    uni.showToast({ title: '加载失败', icon: 'none' });
  } finally {
    loading.value = false;
  }
};

const handleSave = async () => {
  saving.value = true;
  try {
    await updateHrInfo(form.value);
    await loadProfile();
    uni.showToast({ title: '已保存', icon: 'success' });
  } catch (error) {
    console.error('Failed to update HR info:', error);
    uni.showToast({ title: '保存失败', icon: 'none' });
  } finally {
    saving.value = false;
  }
};

const logout = () => {
  uni.showModal({
    title: '提示',
    content: '确认退出当前账号吗？',
    success: (res) => {
      if (res.confirm) {
        uni.reLaunch({ url: '/pages/hr/auth/login' });
      }
    },
  });
};

onMounted(() => {
  loadProfile();
});
</script>

<style scoped lang="scss">
.profile-page {
  padding: 24rpx;
  background: #f6f7fb;
  min-height: 100vh;
}

.card {
  background: #fff;
  border-radius: 24rpx;
  padding: 28rpx;
  margin-bottom: 24rpx;
}

.name-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.profile-info {
  display: flex;
  align-items: center;
}

.avatar-wrapper {
  width: 112rpx;
  height: 112rpx;
  border-radius: 16rpx;
  background: #eef2ff;
  margin-right: 24rpx;
  overflow: hidden;
}

.avatar-wrapper image {
  width: 100%;
  height: 100%;
}

.name {
  font-size: 34rpx;
  font-weight: 600;
}

.role {
  color: #8a92a7;
  margin-top: 8rpx;
}

.badge {
  padding: 8rpx 20rpx;
  background: #e8f3ff;
  color: #2f7cff;
  border-radius: 999rpx;
}

.info {
  margin-top: 10rpx;
  color: #6f788f;
}

.section-title {
  font-size: 30rpx;
  font-weight: 600;
  margin-bottom: 16rpx;
}

.form-item {
  margin-bottom: 20rpx;
}

.label {
  display: block;
  margin-bottom: 8rpx;
  color: #6f788f;
}

input {
  width: 100%;
  height: 80rpx;
  padding: 0 20rpx;
  background: #f5f7fb;
  border-radius: 16rpx;
  border: none;
  box-sizing: border-box;
}

button.primary {
  width: 100%;
  height: 90rpx;
  background: #2f7cff;
  color: #fff;
  border: none;
  border-radius: 18rpx;
  margin-top: 12rpx;
}

.logout-card {
  text-align: center;
}

.logout {
  width: 100%;
  height: 90rpx;
  background: #ff4d4f;
  color: #fff;
  border: none;
  border-radius: 18rpx;
}

.loading {
  text-align: center;
  color: #8a92a7;
  margin-top: 20rpx;
}
</style>
