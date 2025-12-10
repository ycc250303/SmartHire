<template>
  <NLayout class="admin-layout" has-sider>
    <!-- 侧边栏 -->
    <NLayoutSider
      :collapsed="collapsed"
      :collapsed-width="64"
      :width="240"
      collapse-mode="width"
      show-trigger
      bordered
      @collapse="collapsed = true"
      @expand="collapsed = false"
    >
      <div class="logo">
        <div class="logo-emoji">💼</div>
        <span v-show="!collapsed" class="logo-text">SmartHire</span>
      </div>

      <NMenu
        :collapsed="collapsed"
        :collapsed-width="64"
        :collapsed-icon-size="22"
        :options="menuOptions"
        :value="activeMenu"
        @update:value="handleMenuSelect"
      />
    </NLayoutSider>

    <!-- 主内容区 -->
    <NLayout>
      <!-- 顶部导航栏 -->
      <NLayoutHeader bordered class="header">
        <div class="header-left">
          <NBreadcrumb>
            <NBreadcrumbItem
              v-for="item in breadcrumbs"
              :key="item.name"
              @click="navigateTo(item.path)"
            >
              {{ item.title }}
            </NBreadcrumbItem>
          </NBreadcrumb>
        </div>

        <div class="header-right">
          <!-- 主题切换 -->
          <NSwitch
            v-model:value="isDark"
            @update:value="handleThemeChange"
            class="theme-switch"
          >
            <template #checked>🌙</template>
            <template #unchecked>☀️</template>
          </NSwitch>

          <!-- 用户信息 -->
          <NDropdown
            :options="userDropdownOptions"
            @select="handleUserAction"
          >
            <div class="user-info">
              <NAvatar
                :size="32"
                :src="userStore.auth.user?.avatar"
                fallback-src="/default-avatar.png"
                round
              />
              <span class="username">{{ userStore.displayName() }}</span>
            </div>
          </NDropdown>
        </div>
      </NLayoutHeader>

      <!-- 页面内容 -->
      <NLayoutContent class="content">
        <div class="page-container">
          <RouterView />
        </div>
      </NLayoutContent>
    </NLayout>
  </NLayout>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { NLayout, NLayoutSider, NLayoutHeader, NLayoutContent, NMenu, NBreadcrumb, NBreadcrumbItem, NSwitch, NDropdown, NAvatar, useMessage } from 'naive-ui'
import { useThemeStore } from '@/store/theme'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const themeStore = useThemeStore()
const userStore = useUserStore()
const message = useMessage()

// 状态
const collapsed = ref(false)
const isDark = ref(themeStore.isDark)

// 计算当前激活的菜单
const activeMenu = computed(() => {
  const path = route.path
  if (path === '/dashboard' || path === '/') return 'DashboardHome'
  if (path.startsWith('/dashboard/statistics')) return 'Statistics'
  if (path.startsWith('/dashboard/review')) return 'Review'
  if (path.startsWith('/dashboard/users')) return 'Users'
  if (path.startsWith('/dashboard/announcement')) return 'Announcement'
  if (path.startsWith('/dashboard/reports')) return 'Reports'
  if (path.startsWith('/dashboard/system')) return 'System'
  return route.name as string
})

// 面包屑
const breadcrumbs = computed(() => {
  const items = [
    { name: 'DashboardHome', title: '管理台', path: '/dashboard' }
  ]

  if (route.meta.title !== '管理台') {
    items.push({
      name: route.name as string,
      title: route.meta.title as string,
      path: route.path
    })
  }

  return items
})

// 菜单配置
const menuOptions = computed(() => [
  {
    label: '管理台首页',
    key: 'DashboardHome',
    icon: () => '🏠',
    onClick: () => router.push('/dashboard')
  },
  {
    label: '数据统计',
    key: 'Statistics',
    icon: () => '📊',
    onClick: () => router.push('/dashboard/statistics')
  },
  {
    label: '招聘审核',
    key: 'Review',
    icon: () => '✅',
    onClick: () => router.push('/dashboard/review')
  },
  {
    label: '用户管理',
    key: 'Users',
    icon: () => '👥',
    onClick: () => router.push('/dashboard/users')
  },
  {
    label: '公告管理',
    key: 'Announcement',
    icon: () => '📢',
    onClick: () => router.push('/dashboard/announcement')
  },
  {
    label: '举报处理',
    key: 'Reports',
    icon: () => '⚠️',
    onClick: () => router.push('/dashboard/reports')
  },
  {
    label: '系统管理',
    key: 'System',
    icon: () => '⚙️',
    onClick: () => router.push('/dashboard/system')
  }
])

// 用户下拉菜单
const userDropdownOptions = [
  {
    label: '个人设置',
    key: 'profile',
    icon: () => '👤'
  },
  {
    type: 'divider'
  },
  {
    label: '退出登录',
    key: 'logout',
    icon: () => '🚪'
  }
]

// 事件处理
const handleMenuSelect = (key: string) => {
  const menu = menuOptions.value.find(item => item.key === key)
  if (menu && typeof menu.onClick === 'function') {
    menu.onClick()
  }
}

const handleThemeChange = (value: boolean) => {
  themeStore.toggleTheme()
}

const handleUserAction = (key: string) => {
  switch (key) {
    case 'profile':
      // TODO: 跳转到个人设置页面
      message.info('个人设置功能开发中')
      break
    case 'logout':
      handleLogout()
      break
  }
}

const handleLogout = () => {
  userStore.logout()
  message.success('已安全退出')
  router.push('/login')
}

const navigateTo = (path: string) => {
  if (path && path !== route.path) {
    router.push(path)
  }
}

// 监听主题变化
watch(() => themeStore.isDark, (newVal) => {
  isDark.value = newVal
})
</script>

<style scoped lang="scss">
.admin-layout {
  height: 100vh;
}

.logo {
  display: flex;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid var(--border-color);
  height: 64px;

  .logo-emoji {
    font-size: 32px;
    margin-right: 12px;
  }

  .logo-text {
    font-size: 18px;
    font-weight: 600;
    color: var(--primary-color);
  }
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  height: 64px;
  background: var(--bg-primary);

  .header-left {
    flex: 1;
  }

  .header-right {
    display: flex;
    align-items: center;
    gap: 16px;
  }
}

.theme-switch {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 6px;
  transition: background-color 0.2s;

  &:hover {
    background: var(--bg-secondary);
  }

  .username {
    font-size: 14px;
    font-weight: 500;
  }
}

.content {
  background: var(--bg-secondary);
}

.page-container {
  padding: 24px;
  min-height: calc(100vh - 64px);
}

// 响应式设计
@media (max-width: 768px) {
  .header {
    padding: 0 16px;
  }

  .page-container {
    padding: 16px;
  }

  .username {
    display: none;
  }
}
</style>