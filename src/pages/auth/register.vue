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
        <view class="header-section">
          <text class="main-title">{{ t('auth.register.accountRegister') }}</text>
        </view>
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
        <button class="submit-btn" :disabled="loading" @click="handleNext">
          <text>{{ t('auth.register.nextStep') }}</text>
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

    <!-- Step 3: Additional Information Form -->
    <view v-if="step === 3" class="registration-form">
      <view class="header-section">
        <text class="main-title">{{ t('auth.register.additionalInfo') }}</text>
      </view>

      <!-- Seeker Form -->
      <template v-if="formData.userType === UserType.Seeker">
        <view class="form-field">
          <input
            class="field-input"
            v-model="additionalFormData.realName"
            :placeholder="t('auth.register.realName')"
          />
        </view>

        <view class="form-field">
          <picker
            mode="date"
            :value="additionalFormData.birthDate"
            @change="onBirthDateChange"
          >
            <view class="picker-input">
              <text class="picker-text" :class="{ 'placeholder': !additionalFormData.birthDate }">
                {{ additionalFormData.birthDate || t('auth.register.birthDate') }}
              </text>
              <text class="picker-arrow">›</text>
            </view>
          </picker>
        </view>

        <view class="form-field">
          <view class="city-picker-wrapper">
            <CityPicker
              v-model="additionalFormData.currentCity"
              :placeholder="t('auth.register.currentCity')"
            />
          </view>
        </view>

        <view class="form-field">
          <picker
            mode="selector"
            :range="jobStatusOptions"
            :range-key="'label'"
            @change="onJobStatusChange"
            :value="jobStatusIndex"
          >
            <view class="picker-input">
              <text class="picker-text" :class="{ 'placeholder': additionalFormData.jobStatus === undefined }">
                {{ selectedJobStatusLabel }}
              </text>
              <text class="picker-arrow">›</text>
            </view>
          </picker>
        </view>
      </template>

      <!-- HR Form -->
      <template v-else>
        <view class="form-field">
          <input
            class="field-input"
            v-model="additionalFormData.realName"
            :placeholder="t('auth.register.realName')"
          />
        </view>

        <view class="form-field">
          <input
            class="field-input"
            v-model="additionalFormData.position"
            :placeholder="t('auth.register.position')"
          />
        </view>

        <view class="form-field">
          <input
            class="field-input"
            v-model="additionalFormData.workPhone"
            :placeholder="t('auth.register.workPhone')"
          />
        </view>
      </template>

      <!-- Error Message -->
      <view v-if="errorMessage" class="error-message">
        <text class="error-text">{{ errorMessage }}</text>
      </view>

      <!-- Submit Button -->
      <button class="submit-btn" :disabled="loading" @click="handleCompleteRegistration">
        <text v-if="loading">{{ t('auth.register.completing') }}</text>
        <text v-else>{{ t('auth.register.completeRegistration') }}</text>
      </button>

      <!-- Bottom Links -->
      <view class="bottom-links">
        <view class="link-item" @click="goBackToStep2">
          <text class="link-text link-muted">{{ t('auth.register.backToPrevious') }}</text>
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
import { Gender, UserType, register, sendVerificationCode, login, type RegisterParams, type LoginResponse } from '@/services/api/auth';
import { setTokens, ApiError } from '@/services/http';
import { registerSeeker, type RegisterSeekerParams } from '@/services/api/seeker';
import { registerHr, type RegisterHrParams } from '@/services/api/hr';
import CityPicker from '@/components/common/CityPicker.vue';

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

const additionalFormData = ref<{
  realName: string;
  birthDate: string;
  currentCity: string;
  jobStatus: number | undefined;
  position: string;
  workPhone: string;
}>({
  realName: '',
  birthDate: '',
  currentCity: '',
  jobStatus: undefined,
  position: '',
  workPhone: '',
});

const jobStatusOptions = [
  { label: t('auth.register.jobStatusOptions.0'), value: 0 },
  { label: t('auth.register.jobStatusOptions.1'), value: 1 },
  { label: t('auth.register.jobStatusOptions.2'), value: 2 },
  { label: t('auth.register.jobStatusOptions.3'), value: 3 },
];

const jobStatusIndex = computed(() => {
  if (additionalFormData.value.jobStatus === undefined) return 0;
  return jobStatusOptions.findIndex(opt => opt.value === additionalFormData.value.jobStatus);
});

const selectedJobStatusLabel = computed(() => {
  if (additionalFormData.value.jobStatus === undefined) {
    return t('auth.register.jobStatus');
  }
  const option = jobStatusOptions.find(item => item.value === additionalFormData.value.jobStatus);
  return option ? option.label : t('auth.register.jobStatus');
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

function goBackToStep2() {
  if (step.value === 3) {
    step.value = 2;
    errorMessage.value = '';
  }
}

function onBirthDateChange(e: any) {
  additionalFormData.value.birthDate = e.detail.value;
}

function onJobStatusChange(e: any) {
  const option = jobStatusOptions[e.detail.value];
  if (option) {
    additionalFormData.value.jobStatus = option.value;
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

async function handleNext() {
  errorMessage.value = '';

  const validationError = validateForm();
  if (validationError) {
    errorMessage.value = validationError;
    return;
  }

  loading.value = true;

  try {
    const registerParams: RegisterParams = {
      username: formData.value.username.trim(),
      password: formData.value.password,
      email: formData.value.email.trim(),
      phone: formData.value.phone.trim(),
      gender: formData.value.gender!,
      userType: formData.value.userType,
      verifyCode: formData.value.verifyCode.trim(),
    };

    await register(registerParams);

    step.value = 3;
  } catch (err) {
    if (err instanceof ApiError) {
      console.error('Unexpected API Error:', err.code, err.message);
      errorMessage.value = err.message || 'An error occurred';
    } else if (err instanceof Error) {
      console.error('Unexpected Error:', err.message);
      errorMessage.value = err.message || 'An error occurred';
    } else {
      console.error('Unexpected Error:', err);
      errorMessage.value = 'An error occurred';
    }
  } finally {
    loading.value = false;
  }
}

function validateAdditionalForm(): string | null {
  if (!additionalFormData.value.realName.trim()) {
    return t('auth.register.validation.realNameRequired');
  }

  if (formData.value.userType === UserType.Seeker) {
    if (!additionalFormData.value.birthDate) {
      return t('auth.register.validation.birthDateRequired');
    }
    if (!additionalFormData.value.currentCity) {
      return t('auth.register.validation.currentCityRequired');
    }
    if (additionalFormData.value.jobStatus === undefined) {
      return t('auth.register.validation.jobStatusRequired');
    }
  } else {
    if (!additionalFormData.value.position.trim()) {
      return t('auth.register.validation.positionRequired');
    }
    if (!additionalFormData.value.workPhone.trim()) {
      return t('auth.register.validation.workPhoneRequired');
    }
    if (!isValidPhone(additionalFormData.value.workPhone)) {
      return t('auth.register.validation.workPhoneInvalid');
    }
  }

  return null;
}

async function handleCompleteRegistration() {
  errorMessage.value = '';

  const validationError = validateAdditionalForm();
  if (validationError) {
    errorMessage.value = validationError;
    return;
  }

  loading.value = true;

  try {
    await new Promise(resolve => setTimeout(resolve, 1000));

    let loginResponse: LoginResponse | null = null;
    let loginAttempts = 0;
    const maxAttempts = 5;
    const loginDelay = 1000;

    const loginParams = {
      username: formData.value.username.trim(),
      password: formData.value.password,
    };

    while (loginAttempts < maxAttempts && !loginResponse) {
      try {
        loginResponse = await login(loginParams);
        break;
      } catch (err) {
        loginAttempts++;
        if (loginAttempts < maxAttempts) {
          await new Promise(resolve => setTimeout(resolve, loginDelay));
        } else {
          const errorMsg = err instanceof Error ? err.message : 'Login failed';
          errorMessage.value = `Login failed: ${errorMsg}. Please try logging in manually.`;
          uni.showToast({
            title: 'Login failed, please try manually',
            icon: 'none',
            duration: 2000,
          });
          setTimeout(() => {
            uni.redirectTo({
              url: '/pages/auth/login',
            });
          }, 2000);
          return;
        }
      }
    }

    if (!loginResponse || !loginResponse.accessToken || !loginResponse.refreshToken) {
      uni.showToast({
        title: 'Auto login failed, please login manually',
        icon: 'none',
        duration: 1500,
      });
      uni.redirectTo({
        url: '/pages/auth/login',
      });
      return;
    }

    setTokens(
      loginResponse.accessToken,
      loginResponse.refreshToken,
      loginResponse.expiresIn
    );

    if (formData.value.userType === UserType.Seeker) {
      const seekerParams: RegisterSeekerParams = {
        realName: additionalFormData.value.realName.trim(),
        birthDate: additionalFormData.value.birthDate,
        currentCity: additionalFormData.value.currentCity,
        jobStatus: additionalFormData.value.jobStatus!,
      };
      await registerSeeker(seekerParams);
    } else {
      const hrParams: RegisterHrParams = {
        realName: additionalFormData.value.realName.trim(),
        position: additionalFormData.value.position.trim(),
        workPhone: additionalFormData.value.workPhone.trim(),
      };
      await registerHr(hrParams);
    }

    uni.showToast({
      title: t('auth.register.completeSuccess'),
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
    if (err instanceof ApiError) {
      console.error('Registration API Error:', err.code, err.message);
      errorMessage.value = err.message || 'Registration failed';
    } else if (err instanceof Error) {
      console.error('Registration Error:', err.message);
      errorMessage.value = err.message || 'Registration failed';
    } else {
      console.error('Registration Error:', err);
      errorMessage.value = 'Registration failed';
    }
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
  padding-top: 150rpx;
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
  padding-top: 150rpx;
  min-height: 100vh;
  background-color: #ffffff;
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
  justify-content: space-between;
}

.picker-text {
  font-size: 30rpx;
  color: vars.$text-color;

  &.placeholder {
    color: vars.$text-muted;
  }
}

.picker-arrow {
  font-size: 36rpx;
  color: vars.$text-muted;
  margin-left: vars.$spacing-sm;
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

.city-picker-wrapper {
  width: 100%;
  
  :deep(.city-picker) {
    width: 100%;
  }
  
  :deep(.picker-trigger) {
    width: 100%;
    height: 106rpx;
    background-color: #f5f7fa;
    border-radius: vars.$border-radius;
    padding: 0 vars.$spacing-md;
    box-sizing: border-box;
  }
  
  :deep(.picker-text),
  :deep(.picker-placeholder) {
    font-size: 30rpx;
    color: vars.$text-color;
  }
  
  :deep(.picker-placeholder) {
    color: vars.$text-muted;
  }
  
  :deep(.picker-arrow) {
    font-size: 36rpx;
    color: vars.$text-muted;
    margin-left: vars.$spacing-sm;
  }
}
</style>