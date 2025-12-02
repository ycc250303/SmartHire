<template>
  <view class="page">
    <view class="login-container">
      <view class="header">
        <text class="title">账号登录</text>
        <text class="subtitle">登录发现更多精彩</text>
      </view>
      
      <view class="form">
        <view class="input-group">
          <text class="label">用户名</text>
          <input 
            class="input" 
            v-model="form.username" 
            placeholder="请输入用户名"
            placeholder-style="color: #c0c0c0"
          />
        </view>
        
        <view class="input-group">
          <text class="label">密码</text>
          <input 
            class="input" 
            v-model="form.password" 
            type="password"
            placeholder="请输入密码"
            placeholder-style="color: #c0c0c0"
          />
        </view>
        
        <button 
          class="login-btn" 
          :disabled="loading"
          @click="handleLogin"
        >
          {{ loading ? '登录中...' : '登录' }}
        </button>
        
        <view class="footer-links">
          <text class="link" @click="handleHelp">登录遇到问题</text>
          <text class="link" @click="handleRegister">还没有账号？去注册</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { login } from '@/services/api/auth';
import { setTokenWithExpiry } from '@/services/http';
import { useUserStore } from '@/store/user';

const form = ref({
  username: '',
  password: '',
});

const loading = ref(false);

const handleLogin = async () => {
  if (!form.value.username || !form.value.password) {
    uni.showToast({
      title: '请输入用户名和密码',
      icon: 'none',
    });
    return;
  }
  
  loading.value = true;
  try {
    const token = await login(form.value);
    setTokenWithExpiry(token);
    
    const userStore = useUserStore();
    await userStore.loadUserInfo();
    
    uni.showToast({
      title: '登录成功',
      icon: 'success',
    });
    
    setTimeout(() => {
      uni.switchTab({
        url: '/pages/dashboard/index',
      });
    }, 1500);
  } catch (error) {
    console.error('Login failed:', error);
    uni.showToast({
      title: error instanceof Error ? error.message : '登录失败',
      icon: 'none',
    });
  } finally {
    loading.value = false;
  }
};

const handleHelp = () => {
  uni.navigateTo({
    url: '/pages/auth/help',
  });
};

const handleRegister = () => {
  uni.navigateTo({
    url: '/pages/auth/register',
  });
};
</script>

<style lang="scss" scoped>
@use "@/styles/variables.scss" as vars;

.page {
  min-height: 100vh;
  background-color: vars.$bg-color;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: vars.$spacing-lg;
}

.login-container {
  width: 100%;
  max-width: 600rpx;
}

.header {
  text-align: center;
  margin-bottom: vars.$spacing-xl;
}

.title {
  font-size: 48rpx;
  font-weight: 600;
  color: vars.$text-color;
  display: block;
  margin-bottom: vars.$spacing-sm;
}

.subtitle {
  font-size: 28rpx;
  color: vars.$text-muted;
}

.form {
  background-color: vars.$surface-color;
  border-radius: vars.$border-radius-lg;
  padding: vars.$spacing-xl;
}

.input-group {
  margin-bottom: vars.$spacing-lg;
}

.label {
  font-size: 28rpx;
  color: vars.$text-color;
  display: block;
  margin-bottom: vars.$spacing-sm;
}

.input {
  width: 100%;
  height: 88rpx;
  background-color: #f5f5f5;
  border-radius: vars.$border-radius-sm;
  padding: 0 vars.$spacing-md;
  font-size: 28rpx;
  color: vars.$text-color;
}

.login-btn {
  width: 100%;
  height: 88rpx;
  background-color: vars.$primary-color;
  color: #ffffff;
  border-radius: vars.$border-radius;
  font-size: 32rpx;
  border: none;
  margin-top: vars.$spacing-lg;
}

.footer-links {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: vars.$spacing-sm;
  margin-top: vars.$spacing-lg;
}

.link {
  font-size: 24rpx;
  color: vars.$primary-color;
}
</style>

