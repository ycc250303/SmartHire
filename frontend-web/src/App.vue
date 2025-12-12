<template>
  <div :class="{ 'dark-theme': themeStore.isDark }">
    <NConfigProvider :theme="theme" :theme-overrides="themeOverrides">
      <NMessageProvider>
        <NDialogProvider>
          <NNotificationProvider>
            <NLoadingBarProvider>
              <RouterView />
            </NLoadingBarProvider>
          </NNotificationProvider>
        </NDialogProvider>
      </NMessageProvider>
    </NConfigProvider>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { NConfigProvider, darkTheme, type GlobalThemeOverrides } from 'naive-ui'
import { useThemeStore } from '@/store/theme'

const themeStore = useThemeStore()

const theme = computed(() => {
  return themeStore.isDark ? darkTheme : null
})

const themeOverrides: GlobalThemeOverrides = {
  common: {
    primaryColor: '#2f7cff',
    primaryColorHover: '#1e5fcc',
    primaryColorPressed: '#1846b3',
    primaryColorSuppl: '#e5edff',
    borderRadius: '6px',
    borderRadiusSmall: '4px'
  },
  Card: {
    borderRadius: '6px',
    boxShadow: '0 2px 8px rgba(0, 0, 0, 0.08)'
  },
  Button: {
    borderRadius: '6px',
    borderRadiusSmall: '4px'
  },
  Input: {
    borderRadius: '6px'
  },
  Select: {
    borderRadius: '6px'
  },
  Table: {
    borderRadius: '6px'
  }
}
</script>

<style scoped>
.dark-theme {
  transition: background-color 0.3s ease, color 0.3s ease;
}

:deep(.n-layout) {
  transition: background-color 0.3s ease;
}

:deep(.n-card) {
  transition: background-color 0.3s ease, border-color 0.3s ease, box-shadow 0.3s ease;
}

:deep(.n-button) {
  transition: background-color 0.3s ease, border-color 0.3s ease, color 0.3s ease;
}

:deep(.n-input) {
  transition: background-color 0.3s ease, border-color 0.3s ease, color 0.3s ease;
}

:deep(.n-select) {
  transition: background-color 0.3s ease, border-color 0.3s ease, color 0.3s ease;
}

:deep(.n-menu) {
  transition: background-color 0.3s ease;
}

:deep(.n-data-table) {
  transition: background-color 0.3s ease;
}

:deep(.n-modal) {
  transition: background-color 0.3s ease;
}
</style>