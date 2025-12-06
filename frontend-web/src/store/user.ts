import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { User } from '@/types'

interface AuthState {
  token: string | null
  user: User | null
  permissions: string[]
}

export const useUserStore = defineStore('user', () => {
  const auth = ref<AuthState>({
    token: null,
    user: null,
    permissions: []
  })

  // 初始化认证状态
  const initAuth = () => {
    const savedToken = localStorage.getItem('auth-token')
    const savedUser = localStorage.getItem('auth-user')
    const savedPermissions = localStorage.getItem('auth-permissions')

    if (savedToken) {
      auth.value.token = savedToken
    }

    if (savedUser) {
      try {
        auth.value.user = JSON.parse(savedUser)
      } catch (error) {
        console.error('Failed to parse saved user:', error)
      }
    }

    if (savedPermissions) {
      try {
        auth.value.permissions = JSON.parse(savedPermissions)
      } catch (error) {
        console.error('Failed to parse saved permissions:', error)
      }
    }
  }

  // 登录
  const login = (token: string, user: User, permissions: string[] = []) => {
    auth.value = {
      token,
      user,
      permissions
    }

    // 保存到本地存储
    localStorage.setItem('auth-token', token)
    localStorage.setItem('auth-user', JSON.stringify(user))
    localStorage.setItem('auth-permissions', JSON.stringify(permissions))
  }

  // 登出
  const logout = () => {
    auth.value = {
      token: null,
      user: null,
      permissions: []
    }

    // 清除本地存储
    localStorage.removeItem('auth-token')
    localStorage.removeItem('auth-user')
    localStorage.removeItem('auth-permissions')
  }

  // 更新用户信息
  const updateUser = (user: Partial<User>) => {
    if (auth.value.user) {
      auth.value.user = { ...auth.value.user, ...user }
      localStorage.setItem('auth-user', JSON.stringify(auth.value.user))
    }
  }

  // 检查是否已登录
  const isLoggedIn = () => {
    return !!auth.value.token
  }

  // 检查是否有权限
  const hasPermission = (permission: string) => {
    if (!auth.value.permissions.length) return true // 如果没有配置权限，则认为有权限
    return auth.value.permissions.includes(permission)
  }

  // 检查是否有角色
  const hasRole = (role: string) => {
    return auth.value.user?.role === role
  }

  // 获取用户显示名称
  const displayName = () => {
    const user = auth.value.user
    if (!user) return '未登录'
    return user.nickname || user.username || '未知用户'
  }

  return {
    auth,
    initAuth,
    login,
    logout,
    updateUser,
    isLoggedIn,
    hasPermission,
    hasRole,
    displayName
  }
})