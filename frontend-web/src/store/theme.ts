import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { ThemeSettings } from '@/types'

export const useThemeStore = defineStore('theme', () => {
  const settings = ref<ThemeSettings>({
    isDark: false,
    primaryColor: '#2f7cff',
    compact: false,
    followSystem: true
  })

  // 响应式计算
  const isDark = computed(() => {
    if (settings.value.followSystem) {
      return window.matchMedia('(prefers-color-scheme: dark)').matches
    }
    return settings.value.isDark
  })

  // 初始化主题
  const initTheme = () => {
    const savedSettings = localStorage.getItem('theme-settings')
    if (savedSettings) {
      try {
        settings.value = { ...settings.value, ...JSON.parse(savedSettings) }
      } catch (error) {
        console.error('Failed to parse theme settings:', error)
      }
    }

    // 应用主题
    applyTheme()

    // 监听系统主题变化
    if (settings.value.followSystem) {
      window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', applyTheme)
    }
  }

  // 应用主题
  const applyTheme = () => {
    const dark = isDark.value
    document.documentElement.setAttribute('data-theme', dark ? 'dark' : 'light')

    // 设置CSS变量
    if (settings.value.primaryColor) {
      document.documentElement.style.setProperty('--primary-color', settings.value.primaryColor)
    }

    // 更新 body 类名
    document.body.classList.toggle('dark-theme', dark)
  }

  // 切换主题
  const toggleTheme = () => {
    settings.value.isDark = !settings.value.isDark
    settings.value.followSystem = false
    applyTheme()
    saveSettings()
  }

  // 设置主题颜色
  const setPrimaryColor = (color: string) => {
    settings.value.primaryColor = color
    applyTheme()
    saveSettings()
  }

  // 设置跟随系统
  const setFollowSystem = (follow: boolean) => {
    settings.value.followSystem = follow
    if (follow) {
      settings.value.isDark = window.matchMedia('(prefers-color-scheme: dark)').matches
    }
    applyTheme()
    saveSettings()
  }

  // 设置紧凑模式
  const setCompact = (compact: boolean) => {
    settings.value.compact = compact
    document.documentElement.classList.toggle('compact', compact)
    saveSettings()
  }

  // 保存设置
  const saveSettings = () => {
    localStorage.setItem('theme-settings', JSON.stringify(settings.value))
  }

  // 重置设置
  const resetSettings = () => {
    settings.value = {
      isDark: false,
      primaryColor: '#2f7cff',
      compact: false,
      followSystem: true
    }
    applyTheme()
    localStorage.removeItem('theme-settings')
  }

  return {
    settings,
    isDark,
    initTheme,
    toggleTheme,
    setPrimaryColor,
    setFollowSystem,
    setCompact,
    resetSettings
  }
})