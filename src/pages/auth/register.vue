<template>
  <view class="page">
    <view class="register-container">
      <view class="header">
        <text class="title">注册</text>
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
            placeholder="请输入密码（至少6位）"
            placeholder-style="color: #c0c0c0"
          />
        </view>
        
        <view class="input-group">
          <text class="label">邮箱</text>
          <input 
            class="input" 
            v-model="form.email" 
            type="email"
            placeholder="请输入邮箱"
            placeholder-style="color: #c0c0c0"
          />
        </view>
        
        <view class="input-group">
          <text class="label">手机号</text>
          <input 
            class="input" 
            v-model="form.phone" 
            type="number"
            placeholder="请输入手机号"
            placeholder-style="color: #c0c0c0"
          />
        </view>
        
        <view class="input-group">
          <text class="label">验证码</text>
          <view class="verify-code-row">
            <input 
              class="input verify-input" 
              v-model="form.verifyCode" 
              placeholder="请输入验证码"
              placeholder-style="color: #c0c0c0"
            />
            <button 
              class="send-code-btn" 
              :disabled="codeSending || countdown > 0"
              @click="handleSendCode"
            >
              {{ countdown > 0 ? `${countdown}秒` : (codeSending ? '发送中...' : '发送验证码') }}
            </button>
          </view>
        </view>
        
        <button 
          class="register-btn" 
          :disabled="loading"
          @click="handleRegister"
        >
          {{ loading ? '注册中...' : '注册' }}
        </button>
        
        <view class="footer-links">
          <text class="link" @click="handleBack">← 返回登录</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { register, sendVerificationCode, UserType } from '@/services/api/auth';
import { setTokenWithExpiry } from '@/services/http';
import { useUserStore } from '@/store/user';

const form = ref({
  username: '',
  password: '',
  email: '',
  phone: '',
  verifyCode: '',
});

const loading = ref(false);
const codeSending = ref(false);
const countdown = ref(0);

const handleSendCode = async () => {
  if (!form.value.email) {
    uni.showToast({
      title: '请输入邮箱',
      icon: 'none',
    });
    return;
  }
  
  codeSending.value = true;
  try {
    await sendVerificationCode({ email: form.value.email });
    uni.showToast({
      title: '验证码已发送',
      icon: 'success',
    });
    
    countdown.value = 60;
    const timer = setInterval(() => {
      countdown.value--;
      if (countdown.value <= 0) {
        clearInterval(timer);
      }
    }, 1000);
  } catch (error) {
    console.error('Failed to send code:', error);
    uni.showToast({
      title: '发送失败',
      icon: 'none',
    });
  } finally {
    codeSending.value = false;
  }
};

const handleRegister = async () => {
  if (!form.value.username || !form.value.password || !form.value.email || !form.value.phone || !form.value.verifyCode) {
    uni.showToast({
      title: '请填写完整信息',
      icon: 'none',
    });
    return;
  }
  
  loading.value = true;
  try {
    await register({
      ...form.value,
      gender: 2, // PreferNotToSay
      userType: UserType.HR,
    });
    
    uni.showToast({
      title: '注册成功',
      icon: 'success',
    });
    
    setTimeout(() => {
      uni.navigateBack();
    }, 1500);
  } catch (error) {
    console.error('Register failed:', error);
    uni.showToast({
      title: error instanceof Error ? error.message : '注册失败',
      icon: 'none',
    });
  } finally {
    loading.value = false;
  }
};

const handleBack = () => {
  uni.navigateBack();
};
</script>

<style lang="scss" scoped>
@use "@/styles/variables.scss" as vars;

.page {
  min-height: 100vh;
  background-color: vars.$bg-color;
  padding: vars.$spacing-lg;
}

.register-container {
  width: 100%;
  max-width: 600rpx;
  margin: 0 auto;
}

.header {
  text-align: center;
  margin-bottom: vars.$spacing-xl;
}

.title {
  font-size: 48rpx;
  font-weight: 600;
  color: vars.$text-color;
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

.verify-code-row {
  display: flex;
  gap: vars.$spacing-sm;
}

.verify-input {
  flex: 1;
}

.send-code-btn {
  width: 200rpx;
  height: 88rpx;
  background-color: vars.$primary-color-soft;
  color: vars.$primary-color;
  border-radius: vars.$border-radius-sm;
  font-size: 24rpx;
  border: none;
  flex-shrink: 0;
}

.register-btn {
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
  justify-content: center;
  margin-top: vars.$spacing-lg;
}

.link {
  font-size: 24rpx;
  color: vars.$primary-color;
}
</style>






