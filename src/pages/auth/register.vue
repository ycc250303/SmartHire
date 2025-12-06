<template>
  <view class="register-page">
    <!-- Step 1: User Type Selection -->
    <view v-if="step === 1" class="identity-selection">
      <view class="identity-title">{{ t('auth.register.selectIdentity') }}</view>
      <view class="identity-cards">
        <view class="identity-card" @click="selectUserType(UserType.Seeker)">
          <text class="card-text">{{ t('auth.register.jobSeeker') }}</text>
        </view>
        <view class="identity-card" @click="selectUserType(UserType.HR)">
          <text class="card-text">{{ t('auth.register.hr') }}</text>
        </view>
      </view>
    </view>

    <!-- Step 2: Registration Form -->
    <view v-if="step === 2" class="registration-form">
        <!-- Username and Gender in one row -->
        <view class="form-field form-field-row">
          <view class="field-col field-col-2">
            <input
              class="field-input"
              v-model="formData.username"
              :placeholder="t('auth.register.username')"
            />
          </view>
          <view class="field-col field-col-1">
            <picker
              mode="selector"
              :range="genderOptions"
              :range-key="'label'"
              @change="onGenderChange"
              :value="formData.gender"
            >
              <view class="picker-input">
                <text class="picker-text" :class="{ 'placeholder': formData.gender === null }">
                  {{ selectedGenderLabel }}
                </text>
              </view>
            </picker>
          </view>
        </view>

        <!-- Password -->
        <view class="form-field">
          <view class="password-wrapper">
            <input
              class="field-input"
              :type="showPassword ? 'text' : 'password'"
              v-model="formData.password"
              :placeholder="t('auth.register.password')"
            />
            <view class="eye-icon" @click="togglePassword">
              <view :class="['eye-svg', { 'eye-closed': !showPassword }]"></view>
            </view>
          </view>
        </view>

        <!-- Email -->
        <view class="form-field">
          <input
            class="field-input"
            v-model="formData.email"
            :placeholder="t('auth.register.email')"
          />
        </view>

        <!-- Verification Code -->
        <view class="form-field">
          <view class="code-input-wrapper">
            <input
              class="field-input code-input"
              v-model="formData.verifyCode"
              :placeholder="t('auth.register.verifyCode')"
            />
            <button
              class="send-code-btn"
              :disabled="isSendingCode || countdown > 0"
              @click="handleSendCode"
            >
              <text v-if="countdown > 0">{{ countdown }}s</text>
              <text v-else-if="isSendingCode">{{ t('auth.register.sending') }}</text>
              <text v-else>{{ t('auth.register.sendCode') }}</text>
            </button>
          </view>
        </view>

        <!-- Phone -->
        <view class="form-field">
          <input
            class="field-input"
            v-model="formData.phone"
            :placeholder="t('auth.register.phone')"
          />
        </view>

        <!-- Error Message -->
        <view v-if="errorMessage" class="error-message">
          <text class="error-text">{{ errorMessage }}</text>
        </view>

        <!-- Submit Button -->
        <button class="submit-btn" :disabled="loading" @click="handleRegister">
          <text v-if="loading">{{ t('auth.register.registering') }}</text>
          <text v-else>{{ t('auth.register.registerButton') }}</text>
        </button>

        <!-- Bottom Links -->
        <view class="bottom-links">
          <view class="link-item" @click="goBack">
            <text class="link-text link-muted">{{ t('auth.register.backToPrevious') }}</text>
          </view>
          <view class="link-item" @click="goToHelp">
            <text class="link-text link-muted">{{ t('auth.register.registerIssues') }}</text>
          </view>
        </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { t } from '@/locales';
import { useNavigationTitle } from '@/utils/useNavigationTitle';
import { Gender, UserType, register, sendVerificationCode, login, type RegisterParams } from '@/services/api/auth';
import { setTokens } from '@/services/http';

useNavigationTitle('navigation.register');

const step = ref(1);
const loading = ref(false);
const isSendingCode = ref(false);
const countdown = ref(0);
const errorMessage = ref('');
const showPassword = ref(false);
let countdownTimer: number | null = null;

const formData = ref<{
  username: string;
  password: string;
  email: string;
  phone: string;
  gender: Gender | null;
  verifyCode: string;
  userType: UserType;
}>({
  username: '',
  password: '',
  email: '',
  phone: '',
  gender: null,
  verifyCode: '',
  userType: UserType.Seeker,
});

const genderOptions = computed(() => [
  { value: Gender.Male, label: t('auth.register.male') },
  { value: Gender.Female, label: t('auth.register.female') },
  { value: Gender.PreferNotToSay, label: t('auth.register.preferNotToSay') },
]);

const selectedGenderLabel = computed(() => {
  if (formData.value.gender === null) {
    return t('auth.register.gender');
  }
  const option = genderOptions.value.find(item => item.value === formData.value.gender);
  return option ? option.label : t('auth.register.gender');
});

onLoad(() => {
  uni.setNavigationBarTitle({
    title: t('auth.register.title')
  });
});

function togglePassword() {
  showPassword.value = !showPassword.value;
}

function selectUserType(type: UserType) {
  formData.value.userType = type;
  step.value = 2;
}

function goBack() {
  if (step.value === 2) {
    step.value = 1;
    errorMessage.value = '';
  }
}

function goToHelp() {
  uni.navigateTo({
    url: '/pages/auth/help',
  });
}

function onGenderChange(e: any) {
  const option = genderOptions.value[e.detail.value];
  if (option) {
    formData.value.gender = option.value;
  }
}

function isValidEmail(email: string): boolean {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailRegex.test(email);
}

function isValidPhone(phone: string): boolean {
  return /^\d{11}$/.test(phone);
}

function validateForm(): string | null {
  if (!formData.value.username.trim()) {
    return t('auth.register.validation.usernameRequired');
  }
  if (!formData.value.password) {
    return t('auth.register.validation.passwordRequired');
  }
  if (formData.value.password.length < 6) {
    return t('auth.register.validation.passwordMinLength');
  }
  if (!formData.value.email.trim()) {
    return t('auth.register.validation.emailRequired');
  }
  if (!isValidEmail(formData.value.email)) {
    return t('auth.register.validation.emailInvalid');
  }
  if (!formData.value.phone.trim()) {
    return t('auth.register.validation.phoneRequired');
  }
  if (!isValidPhone(formData.value.phone)) {
    return t('auth.register.validation.phoneInvalid');
  }
  if (formData.value.gender === null) {
    return t('auth.register.validation.genderRequired');
  }
  if (!formData.value.verifyCode.trim()) {
    return t('auth.register.validation.verifyCodeRequired');
  }
  return null;
}

function startCountdown() {
  countdown.value = 60;
  countdownTimer = setInterval(() => {
    countdown.value--;
    if (countdown.value <= 0 && countdownTimer) {
      clearInterval(countdownTimer);
      countdownTimer = null;
    }
  }, 1000) as unknown as number;
}

async function handleSendCode() {
  errorMessage.value = '';

  if (!formData.value.email.trim()) {
    errorMessage.value = t('auth.register.validation.emailRequired');
    return;
  }

  if (!isValidEmail(formData.value.email)) {
    errorMessage.value = t('auth.register.validation.emailInvalid');
    return;
  }

  isSendingCode.value = true;

  try {
    await sendVerificationCode({ email: formData.value.email });
    uni.showToast({
      title: t('auth.register.codeSent'),
      icon: 'success',
      duration: 1000,
    });
    startCountdown();
  } catch (err) {
    errorMessage.value = err instanceof Error ? err.message : 'Failed to send code';
  } finally {
    isSendingCode.value = false;
  }
}

async function handleRegister() {
  errorMessage.value = '';

  const validationError = validateForm();
  if (validationError) {
    errorMessage.value = validationError;
    return;
  }

  loading.value = true;

  try {
    const params: RegisterParams = {
      username: formData.value.username.trim(),
      password: formData.value.password,
      email: formData.value.email.trim(),
      phone: formData.value.phone.trim(),
      gender: formData.value.gender!,
      userType: formData.value.userType,
      verifyCode: formData.value.verifyCode.trim(),
    };

    await register(params);

    uni.showToast({
      title: t('auth.register.registerSuccess'),
      icon: 'success',
      duration: 1000,
    });

    const loginResponse = await login({
      username: params.username,
      password: params.password,
    });

    setTokens(
      loginResponse.accessToken,
      loginResponse.refreshToken,
      loginResponse.expiresIn
    );

    uni.showToast({
      title: t('auth.login.loginSuccess'),
      icon: 'success',
      duration: 1000,
    });

    setTimeout(() => {
      if (formData.value.userType === UserType.Seeker) {
        uni.switchTab({
          url: '/pages/seeker/index/index',
        });
      } else {
        uni.switchTab({
          url: '/pages/hr/hr/home/index',
        });
      }
    }, 1000);
  } catch (err) {
    errorMessage.value = err instanceof Error ? err.message : 'Registration failed';
  } finally {
    loading.value = false;
  }
}
</script>

<style lang="scss" scoped>
@use "@/styles/variables.scss" as vars;

.register-page {
  min-height: 100vh;
}

.identity-selection {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  padding: vars.$spacing-xl;
  background: linear-gradient(to bottom, #e3f2fd 0%, #ffffff 25%);
}

.identity-title {
  font-size: 48rpx;
  font-weight: 600;
  color: vars.$text-color;
  margin-bottom: 80rpx;
  text-align: center;
}

.identity-cards {
  display: flex;
  flex-direction: column;
  gap: vars.$spacing-lg;
  width: 100%;
}

.identity-card {
  background-color: vars.$surface-color;
  border-radius: vars.$border-radius-lg;
  padding: 60rpx vars.$spacing-xl;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 20rpx rgba(75, 163, 255, 0.1);
  transition: all 0.3s ease;

  &:active {
    transform: scale(0.98);
    background-color: vars.$primary-color-soft;
  }
}

.card-text {
  font-size: 36rpx;
  font-weight: 500;
  color: vars.$text-color;
}

.registration-form {
  padding: vars.$spacing-xl vars.$spacing-lg;
  min-height: 100vh;
  background-color: #ffffff;
}

.form-field {
  margin-bottom: vars.$spacing-lg;
}

.form-field-row {
  display: flex;
  gap: vars.$spacing-sm;
}

.field-col {
  flex: 1;
}

.field-col-2 {
  flex: 2;
}

.field-col-1 {
  flex: 1;
}

.field-input {
  width: 100%;
  height: 106rpx;
  background-color: #f5f7fa;
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
  height: 106rpx;
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

.code-input-wrapper {
  display: flex;
  gap: vars.$spacing-sm;
  align-items: center;
}

.code-input {
  flex: 1;
}

.send-code-btn {
  flex-shrink: 0;
  width: 180rpx;
  height: 106rpx;
  background-color: vars.$primary-color;
  color: #ffffff;
  border: none;
  border-radius: vars.$border-radius;
  font-size: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  
  &:disabled {
    background-color: vars.$text-muted;
    opacity: 0.6;
  }

  &::after {
    border: none;
  }
}

.picker-input {
  width: 100%;
  height: 106rpx;
  background-color: #f5f7fa;
  border-radius: vars.$border-radius;
  padding: 0 vars.$spacing-md;
  display: flex;
  align-items: center;
}

.picker-text {
  font-size: 30rpx;
  color: vars.$text-color;

  &.placeholder {
    color: vars.$text-muted;
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

.submit-btn {
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
  margin-bottom: vars.$spacing-md;
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