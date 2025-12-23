<template>
  <view class="login-page">
    <view class="login-container">
      <view class="header-section">
        <text class="main-title">{{ t('auth.login.accountLogin') }}</text>
        <text class="subtitle">{{ t('auth.login.subtitle') }}</text>
      </view>

      <view class="form-section">
        <!-- Username -->
        <view class="form-field">
          <input
            class="field-input"
            v-model="formData.username"
            :placeholder="t('auth.login.username')"
          />
        </view>

        <!-- Password -->
        <view class="form-field">
          <view class="password-wrapper">
            <input
              class="field-input"
              :type="showPassword ? 'text' : 'password'"
              v-model="formData.password"
              :placeholder="t('auth.login.password')"
            />
            <view class="eye-icon" @click="togglePassword">
              <view :class="['eye-svg', { 'eye-closed': !showPassword }] "></view>
            </view>
          </view>
        </view>

        <!-- Error Message -->
        <view v-if="errorMessage" class="error-message">
          <text class="error-text">{{ errorMessage }}</text>
        </view>

        <!-- Login Button -->
        <button class="login-btn" :disabled="loading" @click="handleLogin">
          <text v-if="loading">{{ t('auth.login.loggingIn') }}</text>
          <text v-else>{{ t('auth.login.loginButton') }}</text>
        </button>

        <!-- Bottom Links -->
        <view class="bottom-links">
          <view class="link-item" @click="goToHelp">
            <text class="link-text link-muted">{{ t('auth.login.loginIssues') }}</text>
          </view>
          <view class="link-item" @click="goToRegister">
            <text class="link-text link-muted">{{ t('auth.login.noAccount') }} </text>
            <text class="link-text link-primary">{{ t('auth.login.goToRegister') }}</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { t } from '@/locales';
import { useNavigationTitle } from '@/utils/useNavigationTitle';
import { login, type LoginParams, UserType } from '@/services/api/auth';
import { ApiError, setTokens } from '@/services/http';
import { useUserStore } from '@/store/user';

useNavigationTitle('navigation.login');

const userStore = useUserStore();
const loading = ref(false);
const errorMessage = ref('');
const showPassword = ref(false);

const formData = ref<LoginParams>({
  username: '',
  password: '',
});

onLoad(() => {
  uni.setNavigationBarTitle({
    title: t('auth.login.title') 
  });
});

function togglePassword() {
  showPassword.value = !showPassword.value;
}

function validateForm(): string | null {
  if (!formData.value.username.trim()) {
    return t('auth.login.validation.usernameRequired');
  }
  if (!formData.value.password) {
    return t('auth.login.validation.passwordRequired');
  }
  return null;
}

async function handleLogin() {
  errorMessage.value = '';

  const validationError = validateForm();
  if (validationError) {
    errorMessage.value = validationError;
    return;
  }

  loading.value = true;

  try {
    const loginResponse = await login({
      username: formData.value.username.trim(),
      password: formData.value.password,
    });

    if (!loginResponse || !loginResponse.accessToken) {
      throw new Error('Login failed: empty response');
    }

    setTokens(
      loginResponse.accessToken,
      loginResponse.refreshToken,
      loginResponse.expiresIn
    );

    // Load user info
    await userStore.loadUserInfo();

    uni.showToast({
      title: t('auth.login.loginSuccess'),
      icon: 'success',
      duration: 1000,
    });

    setTimeout(() => {
      const userType = userStore.userInfo?.userType;
      const targetUrl =
        userType === UserType.HR
          ? '/pages/hr/hr/home/index'
          : '/pages/seeker/index/index';

      uni.switchTab({ url: targetUrl });
    }, 1000);
  } catch (err) {
    if (err instanceof ApiError) {
      errorMessage.value = err.message || t('auth.login.loginFailed');
    } else if (err instanceof Error) {
      errorMessage.value = err.message || t('auth.login.loginFailed');
    } else {
      errorMessage.value = t('auth.login.loginFailed');
    }
  } finally {
    loading.value = false;
  }
}

function goToRegister() {
  uni.navigateTo({
    url: '/pages/auth/register',
  });
}

function goToHelp() {
  uni.navigateTo({
    url: '/pages/auth/help',
  });
}
</script>

<style lang="scss" scoped>
@use "@/styles/variables.scss" as vars;

.login-page {
  min-height: 100vh;
  background: linear-gradient(to bottom, #e3f2fd 0%, #ffffff 25%);
  padding: vars.$spacing-xl;
  padding-top: 300rpx; // down
}

.login-container {
  max-width: 650rpx;
  margin: 0 auto;
  padding-top: 0;
}

.header-section {
  margin-bottom: 60rpx;
  text-align: left;
}

.main-title {
  display: block;
  font-size: 56rpx;
  font-weight: 700;
  color: vars.$text-color;
  margin-bottom: vars.$spacing-sm;
}

.subtitle {
  display: block;
  font-size: 28rpx;
  color: vars.$text-muted;
}

.form-section {
  background-color: vars.$surface-color;
  border-radius: vars.$border-radius-lg;
  padding: vars.$spacing-xl;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.05);
}

.form-field {
  margin-bottom: vars.$spacing-lg;
}

.field-input {
  width: 100%;
  height: 114rpx;
  background-color: vars.$bg-color;
  border-radius: vars.$border-radius;
  padding: 0 vars.$spacing-md;
  font-size: 30rpx;
  color: vars.$text-color;
  box-sizing: border-box;

  &::placeholder {
    color: vars.$text-muted;
  }
}

.password-wrapper {
  position: relative;
  
  .field-input {
    padding-right: 100rpx;
  }
}

.eye-icon {
  position: absolute;
  right: 0;
  top: 0;
  height: 114rpx;
  width: 100rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.eye-svg {
  width: 44rpx;
  height: 44rpx;
  position: relative;
  
  &::before {
    content: '';
    position: absolute;
    width: 44rpx;
    height: 28rpx;
    border: 3rpx solid #6b778c;
    border-radius: 50% 50% 50% 50% / 60% 60% 40% 40%;
    top: 8rpx;
    left: 0;
  }
  
  &::after {
    content: '';
    position: absolute;
    width: 12rpx;
    height: 12rpx;
    background-color: #6b778c;
    border-radius: 50%;
    top: 16rpx;
    left: 16rpx;
  }
  
  &.eye-closed::before {
    border-radius: 0;
    height: 0;
    border-bottom: 3rpx solid #6b778c;
    top: 20rpx;
  }
  
  &.eye-closed::after {
    display: none;
  }
}

.error-message {
  background-color: #ffebee;
  border-radius: vars.$border-radius;
  padding: vars.$spacing-md;
  margin-bottom: vars.$spacing-lg;
}

.error-text {
  font-size: 24rpx;
  color: #d32f2f;
  line-height: 1.5;
}

.login-btn {
  width: 100%;
  height: 100rpx;
  background-color: vars.$primary-color;
  color: #ffffff;
  border: none;
  border-radius: vars.$border-radius-lg;
  font-size: 32rpx;
  font-weight: 500;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: vars.$spacing-lg;
  padding: 0;

  &:disabled {
    opacity: 0.6;
  }

  &::after {
    border: none;
  }
}

.bottom-links {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: vars.$spacing-sm;
}

.link-item {
  display: flex;
  flex-wrap: wrap;
}

.link-text {
  font-size: 26rpx;
}

.link-muted {
  color: vars.$text-muted;
}

.link-primary {
  color: vars.$primary-color;
  font-weight: 500;
}
</style>
