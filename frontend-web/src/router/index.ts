import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/store/user'

// 路由配置
const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: {
      title: '登录',
      requiresAuth: false,
      hideInMenu: true
    }
  },
  {
    path: '/',
    redirect: '/dashboard'
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('@/layouts/AdminLayout.vue'),
    meta: {
      title: '管理台',
      requiresAuth: true
    },
    children: [
      {
        path: '',
        name: 'DashboardHome',
        component: () => import('@/views/admin/home/Index.vue'),
        meta: {
          title: '管理台首页',
          icon: 'dashboard',
          requiresAuth: true
        }
      },
      {
        path: 'statistics',
        name: 'Statistics',
        component: () => import('@/views/admin/statistics/Index.vue'),
        meta: {
          title: '数据统计',
          icon: 'chart',
          requiresAuth: true
        }
      },
      {
        path: 'review',
        name: 'Review',
        component: () => import('@/views/admin/review/Index.vue'),
        meta: {
          title: '招聘审核',
          icon: 'audit',
          requiresAuth: true
        }
      },
      {
        path: 'review/:id',
        name: 'ReviewDetail',
        component: () => import('@/views/admin/review/Detail.vue'),
        meta: {
          title: '审核详情',
          requiresAuth: true,
          hideInMenu: true
        }
      },
      {
        path: 'users',
        name: 'Users',
        component: () => import('@/views/admin/users/Index.vue'),
        meta: {
          title: '用户管理',
          icon: 'users',
          requiresAuth: true
        }
      },
      {
        path: 'users/:id',
        name: 'UserDetail',
        component: () => import('@/views/admin/users/Detail.vue'),
        meta: {
          title: '用户详情',
          requiresAuth: true,
          hideInMenu: true
        }
      },
      {
        path: 'announcement',
        name: 'Announcement',
        component: () => import('@/views/admin/announcement/Index.vue'),
        meta: {
          title: '公告管理',
          icon: 'announcement',
          requiresAuth: true
        }
      },
      {
        path: 'announcement/edit',
        name: 'AnnouncementEdit',
        component: () => import('@/views/admin/announcement/Edit.vue'),
        meta: {
          title: '编辑公告',
          requiresAuth: true,
          hideInMenu: true
        }
      },
      {
        path: 'reports',
        name: 'Reports',
        component: () => import('@/views/admin/reports/Index.vue'),
        meta: {
          title: '举报处理',
          icon: 'warning',
          requiresAuth: true
        }
      },
      {
        path: 'reports/:id',
        name: 'ReportDetail',
        component: () => import('@/views/admin/reports/Detail.vue'),
        meta: {
          title: '举报详情',
          requiresAuth: true,
          hideInMenu: true
        }
      },
      {
        path: 'system',
        name: 'System',
        component: () => import('@/views/admin/system/Index.vue'),
        meta: {
          title: '系统管理',
          icon: 'settings',
          requiresAuth: true
        }
      },
      {
        path: 'system/logs',
        name: 'Logs',
        component: () => import('@/views/admin/system/Logs.vue'),
        meta: {
          title: '操作日志',
          requiresAuth: true,
          hideInMenu: true
        }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/NotFound.vue'),
    meta: {
      title: '页面不存在',
      requiresAuth: false,
      hideInMenu: true
    }
  }
]

// 创建路由实例
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    } else {
      return { top: 0 }
    }
  }
})

// 路由守卫
router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore()

  // 详细调试localStorage状态
  console.log('=== 路由守卫调试 ===')
  console.log('访问页面:', to.name)
  console.log('localStorage完整状态:')
  console.log('- auth-token:', localStorage.getItem('auth-token'))
  console.log('- auth-user:', localStorage.getItem('auth-user'))
  console.log('- auth-permissions:', localStorage.getItem('auth-permissions'))

  // 安全地获取用户信息
  const getUserType = () => {
    try {
      // 直接从localStorage获取用户数据，避免响应式系统问题
      const savedUser = localStorage.getItem('auth-user')
      if (savedUser) {
        const userData = JSON.parse(savedUser)
        console.log('- 解析的用户数据:', userData)
        return userData.userType
      }
      console.log('- localStorage中没有用户数据')
      return undefined
    } catch (error) {
      console.warn('获取用户类型时出错:', error)
      return undefined
    }
  }

  const userType = getUserType()
  const isLoggedIn = userStore.isLoggedIn()
  const isAdmin = userStore.isAdmin()

  console.log('计算结果:')
  console.log('- 用户类型:', userType)
  console.log('- 登录状态:', isLoggedIn)
  console.log('- 管理员权限:', isAdmin)
  console.log('=================')

  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - SmartHire Admin`
  }

  // 检查是否需要认证
  if (to.meta.requiresAuth !== false) {
    if (!isLoggedIn) {
      console.log('用户未登录，重定向到登录页面')
      // 未登录，重定向到登录页
      next({
        name: 'Login',
        query: { redirect: to.fullPath }
      })
      return
    }

    // 检查管理员权限 - 某些页面需要管理员权限
    const adminOnlyRoutes = ['Users', 'System', 'Reports', 'Announcement']
    if (adminOnlyRoutes.includes(to.name as string) && !isAdmin) {
      console.warn('访问管理员专用页面被拒绝:', to.name, '用户类型:', userType)
      next({ name: 'DashboardHome' })
      return
    }
  }

  // 已登录访问登录页，重定向到首页
  if (to.name === 'Login' && isLoggedIn) {
    console.log('已登录用户访问登录页，重定向到首页')
    next({ name: 'DashboardHome' })
    return
  }

  console.log('路由守卫通过，允许访问:', to.name)
  next()
})

// 路由错误处理
router.onError((error) => {
  console.error('Router error:', error)
})

export default router