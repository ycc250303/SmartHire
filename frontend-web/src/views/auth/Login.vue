<template>
  <div class="login-page">
    <div class="login-container">
      <!-- 左侧背景 -->
      <div class="login-bg">
        <div class="bg-content">
          <div class="logo-section">
            <div class="logo-emoji">💼</div>
            <h1 class="title">SmartHire</h1>
            <p class="subtitle">智能招聘管理平台</p>
          </div>
          <div class="features">
            <div class="feature-item">
              <div class="feature-icon">🚀</div>
              <div class="feature-text">高效招聘</div>
            </div>
            <div class="feature-item">
              <div class="feature-icon">📊</div>
              <div class="feature-text">数据分析</div>
            </div>
            <div class="feature-item">
              <div class="feature-icon">🛡️</div>
              <div class="feature-text">安全可靠</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧登录表单 -->
      <div class="login-form-container">
        <div class="login-form-wrapper">
          <div class="form-header">
            <h2 class="form-title">欢迎回来</h2>
            <p class="form-subtitle">请登录您的管理员账号</p>
          </div>

          <NForm
            ref="formRef"
            :model="formData"
            :rules="rules"
            size="large"
            class="login-form"
          >
            <NFormItem path="username" label="用户名">
              <NInput
                v-model:value="formData.username"
                placeholder="请输入用户名"
                :maxlength="50"
                clearable
                @keydown.enter="handleSubmit"
              >
                <template #prefix>
                  <span class="input-icon">👤</span>
                </template>
              </NInput>
            </NFormItem>

            <NFormItem path="password" label="密码">
              <NInput
                v-model:value="formData.password"
                type="password"
                placeholder="请输入密码"
                :maxlength="50"
                show-password-on="click"
                @keydown.enter="handleSubmit"
              >
                <template #prefix>
                  <span class="input-icon">🔒</span>
                </template>
              </NInput>
            </NFormItem>

            <NFormItem>
              <div class="form-options">
                <NCheckbox v-model:checked="rememberMe">
                  记住我
                </NCheckbox>
                <NButton text type="primary" @click="handleForgotPassword">
                  忘记密码？
                </NButton>
              </div>
            </NFormItem>

            <NFormItem>
              <NButton
                type="primary"
                size="large"
                :loading="loading"
                :disabled="!canSubmit"
                @click="handleSubmit"
                class="submit-btn"
              >
                登录
              </NButton>
            </NFormItem>
          </NForm>

          <div class="form-footer">
            <p class="footer-text">
              还没有账号？
              <NButton text type="primary">联系管理员</NButton>
            </p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { NForm, NFormItem, NInput, NButton, NCheckbox, FormInst, useMessage } from 'naive-ui'
import { useUserStore } from '@/store/user'
import { useThemeStore } from '@/store/theme'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const themeStore = useThemeStore()
const message = useMessage()

// 表单相关
const formRef = ref<FormInst | null>(null)
const loading = ref(false)
const rememberMe = ref(false)

const formData = ref({
  username: '',
  password: ''
})

// 表单验证规则
const rules = {
  username: [
    {
      required: true,
      message: '请输入用户名',
      trigger: ['input', 'blur']
    },
    {
      min: 3,
      max: 50,
      message: '用户名长度应在 3-50 个字符之间',
      trigger: ['input', 'blur']
    }
  ],
  password: [
    {
      required: true,
      message: '请输入密码',
      trigger: ['input', 'blur']
    },
    {
      min: 6,
      max: 50,
      message: '密码长度应在 6-50 个字符之间',
      trigger: ['input', 'blur']
    }
  ]
}

// 计算是否可以提交
const canSubmit = computed(() => {
  return formData.value.username.trim() && formData.value.password.trim() && !loading.value
})

// 初始化
onMounted(() => {
  // 初始化主题
  themeStore.initTheme()

  // 恢复记住的用户名
  const savedUsername = localStorage.getItem('remembered-username')
  if (savedUsername) {
    formData.value.username = savedUsername
    rememberMe.value = true
  }

  // 如果已经登录，直接跳转
  if (userStore.isLoggedIn()) {
    router.push('/dashboard')
  }
})

// 处理登录
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
  } catch (error) {
    return
  }

  loading.value = true

  try {
    // 模拟登录请求
    await simulateLogin(formData.value.username, formData.value.password)

    message.success('登录成功')

    // 处理记住我
    if (rememberMe.value) {
      localStorage.setItem('remembered-username', formData.value.username)
    } else {
      localStorage.removeItem('remembered-username')
    }

    // 跳转到目标页面
    const redirect = route.query.redirect as string
    router.push(redirect || '/dashboard')
  } catch (error: any) {
    message.error(error.message || '登录失败，请检查用户名和密码')
  } finally {
    loading.value = false
  }
}

// 模拟登录请求
const simulateLogin = async (username: string, password: string) => {
  return new Promise((resolve, reject) => {
    setTimeout(() => {
      // 简单的模拟验证
      if (username === 'admin' && password === 'admin123') {
        const user = {
          id: '1',
          username: 'admin',
          nickname: '系统管理员',
          email: 'admin@smarthire.com',
          phone: '13800138000',
          avatar: '',
          status: 'active' as const,
          role: 'admin' as const,
          createTime: new Date().toISOString()
        }

        const token = 'mock-jwt-token-' + Date.now()
        const permissions = ['admin', 'user:read', 'user:write', 'job:read', 'job:write']

        userStore.login(token, user, permissions)
        resolve(user)
      } else {
        reject(new Error('用户名或密码错误'))
      }
    }, 1000)
  })
}

// 忘记密码
const handleForgotPassword = () => {
  message.info('请联系系统管理员重置密码')
}
</script>

<style scoped lang="scss">
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.login-container {
  display: flex;
  width: 100%;
  max-width: 1200px;
  min-height: 600px;
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.login-bg {
  flex: 1;
  background: linear-gradient(135deg, var(--primary-color), #6366f1);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px 40px;
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: -50%;
    right: -50%;
    width: 200%;
    height: 200%;
    background: radial-gradient(circle, rgba(255,255,255,0.1) 1px, transparent 1px);
    background-size: 20px 20px;
    animation: float 20s linear infinite;
  }

  @keyframes float {
    0% { transform: translate(0, 0) rotate(0deg); }
    100% { transform: translate(-50px, -50px) rotate(360deg); }
  }
}

.bg-content {
  position: relative;
  z-index: 1;
  text-align: center;
  color: white;
}

.logo-section {
  margin-bottom: 60px;

  .logo-emoji {
    font-size: 80px;
    margin-bottom: 20px;
  }

  .title {
    font-size: 36px;
    font-weight: 700;
    margin: 0 0 8px 0;
  }

  .subtitle {
    font-size: 18px;
    opacity: 0.9;
    margin: 0;
  }
}

.features {
  display: flex;
  gap: 30px;
}

.feature-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;

  .feature-icon {
    font-size: 32px;
    margin-bottom: 4px;
  }

  .feature-text {
    font-size: 14px;
    opacity: 0.9;
  }
}

.login-form-container {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  background: var(--bg-primary);
}

.login-form-wrapper {
  width: 100%;
  max-width: 400px;
}

.form-header {
  text-align: center;
  margin-bottom: 40px;

  .form-title {
    font-size: 28px;
    font-weight: 600;
    color: var(--text-primary);
    margin: 0 0 8px 0;
  }

  .form-subtitle {
    font-size: 16px;
    color: var(--text-secondary);
    margin: 0;
  }
}

.login-form {
  .input-icon {
    color: var(--text-disabled);
  }

  .form-options {
    display: flex;
    justify-content: space-between;
    align-items: center;
    width: 100%;
  }

  .submit-btn {
    width: 100%;
    height: 48px;
    font-size: 16px;
    font-weight: 500;
  }
}

.form-footer {
  text-align: center;
  margin-top: 24px;

  .footer-text {
    font-size: 14px;
    color: var(--text-secondary);
    margin: 0;
  }
}

// 响应式设计
@media (max-width: 768px) {
  .login-container {
    flex-direction: column;
    max-width: 400px;
  }

  .login-bg {
    padding: 40px 20px;
    min-height: 200px;

    .features {
      gap: 20px;
    }

    .feature-item {
      .feature-icon {
        font-size: 24px;
      }

      .feature-text {
        font-size: 12px;
      }
    }
  }

  .logo-section {
    margin-bottom: 30px;

    .logo-emoji {
      font-size: 60px;
    }

    .title {
      font-size: 28px;
    }

    .subtitle {
      font-size: 16px;
    }
  }

  .login-form-container {
    padding: 30px 20px;
  }
}
</style>