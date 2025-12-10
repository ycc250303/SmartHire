<template>
  <div class="announcement-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">公告管理</h1>
        <p class="page-description">发布和管理系统公告，支持多种发布方式</p>
      </div>
      <div class="header-actions">
        <NButton type="primary" @click="createAnnouncement">
          <template #icon>➕</template>
          新建公告
        </NButton>
      </div>
    </div>

    <!-- 筛选和搜索 -->
    <NCard :bordered="false" class="filter-card">
      <div class="filter-section">
        <div class="filter-row">
          <div class="filter-item">
            <label>公告类型</label>
            <NSelect
              v-model:value="filters.type"
              :options="typeOptions"
              placeholder="全部类型"
              clearable
              style="width: 150px"
              @update:value="handleFilter"
            />
          </div>
          <div class="filter-item">
            <label>发布状态</label>
            <NSelect
              v-model:value="filters.status"
              :options="statusOptions"
              placeholder="全部状态"
              clearable
              style="width: 150px"
              @update:value="handleFilter"
            />
          </div>
          <div class="filter-item">
            <label>发布时间</label>
            <NDatePicker
              v-model:value="filters.publishTime"
              type="daterange"
              clearable
              style="width: 240px"
              @update:value="handleFilter"
            />
          </div>
        </div>
        <div class="search-row">
          <div class="search-input">
            <NInput
              v-model:value="searchKeyword"
              placeholder="搜索公告标题、内容"
              clearable
              @update:value="handleSearch"
            >
              <template #prefix>
                <span class="search-icon">🔍</span>
              </template>
            </NInput>
          </div>
          <div class="search-actions">
            <NButton @click="handleRefresh">
              <template #icon>🔄</template>
              刷新
            </NButton>
            <NButton @click="resetFilters">
              <template #icon>🔄</template>
              重置
            </NButton>
          </div>
        </div>
      </div>
    </NCard>

    <!-- 公告列表 -->
    <NCard :bordered="false" class="list-card">
      <div class="list-header">
        <span class="list-title">公告列表</span>
        <div class="list-actions">
          <span class="total-count">共 {{ filteredAnnouncements.length }} 条公告</span>
        </div>
      </div>

      <div class="announcement-list">
        <div
          v-for="announcement in paginatedAnnouncements"
          :key="announcement.id"
          class="announcement-item"
          @click="viewAnnouncement(announcement)"
        >
          <div class="announcement-header">
            <div class="announcement-title">
              <h3>{{ announcement.title }}</h3>
              <div class="announcement-badges">
                <NTag :type="getTypeType(announcement.type)" size="small">
                  {{ getTypeText(announcement.type) }}
                </NTag>
                <NTag :type="getStatusType(announcement.status)" size="small">
                  {{ getStatusText(announcement.status) }}
                </NTag>
                <NTag v-if="announcement.isPinned" type="warning" size="small">
                  置顶
                </NTag>
              </div>
            </div>
            <div class="announcement-actions">
              <NButton
                size="small"
                type="info"
                ghost
                @click.stop="previewAnnouncement(announcement)"
              >
                预览
              </NButton>
              <NButton
                size="small"
                type="primary"
                ghost
                @click.stop="editAnnouncement(announcement)"
              >
                编辑
              </NButton>
              <NButton
                size="small"
                type="error"
                ghost
                @click.stop="deleteAnnouncement(announcement)"
              >
                删除
              </NButton>
            </div>
          </div>

          <div class="announcement-summary">
            <p>{{ announcement.summary }}</p>
          </div>

          <div class="announcement-meta">
            <div class="meta-left">
              <span class="meta-item">👤 {{ announcement.author }}</span>
              <span class="meta-item">👁️ {{ formatNumber(announcement.views) }}</span>
              <span class="meta-item">👍 {{ formatNumber(announcement.likes) }}</span>
            </div>
            <div class="meta-right">
              <span class="meta-item">创建时间：{{ formatTime(announcement.createTime) }}</span>
              <span class="meta-item" v-if="announcement.publishTime">
                发布时间：{{ formatTime(announcement.publishTime) }}
              </span>
            </div>
          </div>

          <div class="announcement-publish-info" v-if="announcement.publishMethod">
            <span class="publish-method">发布方式：{{ getPublishMethodText(announcement.publishMethod) }}</span>
            <span class="publish-target" v-if="announcement.publishTarget">
              目标用户：{{ getPublishTargetText(announcement.publishTarget) }}
            </span>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="filteredAnnouncements.length === 0" class="empty-state">
        <div class="empty-icon">📢</div>
        <h3 class="empty-title">暂无公告</h3>
        <p class="empty-description">当前没有符合条件的公告</p>
        <NButton type="primary" @click="createAnnouncement">
          创建第一个公告
        </NButton>
      </div>

      <!-- 分页 -->
      <div v-if="filteredAnnouncements.length > 0" class="pagination-wrapper">
        <NPagination
          v-model:page="currentPage"
          :page-size="pageSize"
          :item-count="filteredAnnouncements.length"
          show-size-picker
          :page-sizes="[10, 20, 50, 100]"
          @update:page="handlePageChange"
          @update:page-size="handlePageSizeChange"
        />
      </div>
    </NCard>

    <!-- 预览弹窗 -->
    <NModal v-model:show="showPreviewModal" :mask-closable="false">
      <NCard
        style="max-width: 800px"
        title="公告预览"
        :bordered="false"
        size="huge"
        role="dialog"
        aria-modal
      >
        <template #header-extra>
          <NButton
            quaternary
            circle
            @click="showPreviewModal = false"
          >
            <template #icon>
              <span class="close-icon">×</span>
            </template>
          </NButton>
        </template>

        <div v-if="selectedAnnouncement" class="announcement-preview">
          <div class="preview-header">
            <h2 class="preview-title">{{ selectedAnnouncement.title }}</h2>
            <div class="preview-badges">
              <NTag :type="getTypeType(selectedAnnouncement.type)" size="small">
                {{ getTypeText(selectedAnnouncement.type) }}
              </NTag>
              <NTag :type="getStatusType(selectedAnnouncement.status)" size="small">
                {{ getStatusText(selectedAnnouncement.status) }}
              </NTag>
              <NTag v-if="selectedAnnouncement.isPinned" type="warning" size="small">
                置顶
              </NTag>
            </div>
          </div>

          <div class="preview-meta">
            <span>作者：{{ selectedAnnouncement.author }}</span>
            <span>创建时间：{{ formatTime(selectedAnnouncement.createTime) }}</span>
            <span v-if="selectedAnnouncement.publishTime">
              发布时间：{{ formatTime(selectedAnnouncement.publishTime) }}
            </span>
          </div>

          <div class="preview-content">
            <div class="content-summary" v-if="selectedAnnouncement.summary">
              <h4>摘要</h4>
              <p>{{ selectedAnnouncement.summary }}</p>
            </div>

            <div class="content-body" v-html="selectedAnnouncement.content"></div>
          </div>

          <div class="preview-stats">
            <div class="stat-item">
              <span class="stat-icon">👁️</span>
              <span class="stat-label">阅读量</span>
              <span class="stat-value">{{ formatNumber(selectedAnnouncement.views) }}</span>
            </div>
            <div class="stat-item">
              <span class="stat-icon">👍</span>
              <span class="stat-label">点赞数</span>
              <span class="stat-value">{{ formatNumber(selectedAnnouncement.likes) }}</span>
            </div>
            <div class="stat-item">
              <span class="stat-icon">💬</span>
              <span class="stat-label">评论数</span>
              <span class="stat-value">{{ formatNumber(selectedAnnouncement.comments) }}</span>
            </div>
          </div>
        </div>

        <template #footer>
          <div class="modal-actions">
            <NButton @click="showPreviewModal = false">关闭</NButton>
            <NButton v-if="selectedAnnouncement" type="primary" @click="editAnnouncement(selectedAnnouncement)">
              编辑公告
            </NButton>
          </div>
        </template>
      </NCard>
    </NModal>

    <!-- 删除确认弹窗 -->
    <NModal v-model:show="showDeleteModal" :mask-closable="false">
      <NCard
        style="max-width: 500px"
        title="删除确认"
        :bordered="false"
        size="huge"
        role="dialog"
        aria-modal
      >
        <template #header-extra>
          <NButton
            quaternary
            circle
            @click="showDeleteModal = false"
          >
            <template #icon>
              <span class="close-icon">×</span>
            </template>
          </NButton>
        </template>

        <div class="delete-confirm">
          <div class="delete-icon">⚠️</div>
          <p class="delete-message">
            确定要删除公告"{{ selectedAnnouncement?.title }}"吗？删除后将无法恢复。
          </p>
        </div>

        <template #footer>
          <div class="modal-actions">
            <NButton @click="showDeleteModal = false">取消</NButton>
            <NButton
              type="error"
              :loading="deleteLoading"
              @click="confirmDelete"
            >
              确认删除
            </NButton>
          </div>
        </template>
      </NCard>
    </NModal>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  NCard,
  NSelect,
  NDatePicker,
  NInput,
  NButton,
  NTag,
  NPagination,
  NModal,
  useMessage,
  useDialog
} from 'naive-ui'
import dayjs from 'dayjs'

interface Announcement {
  id: string
  title: string
  summary: string
  content: string
  type: 'system' | 'maintenance' | 'feature' | 'security' | 'other'
  status: 'draft' | 'published' | 'scheduled' | 'archived'
  isPinned: boolean
  author: string
  createTime: string
  publishTime?: string
  publishMethod?: 'immediate' | 'scheduled'
  publishTarget?: 'all' | 'jobseekers' | 'hrs' | 'admins'
  views: number
  likes: number
  comments: number
}

interface Filters {
  type: string | null
  status: string | null
  publishTime: [number, number] | null
}

const router = useRouter()
const message = useMessage()
const dialog = useDialog()

// 筛选选项
const typeOptions = [
  { label: '系统公告', value: 'system' },
  { label: '维护通知', value: 'maintenance' },
  { label: '功能更新', value: 'feature' },
  { label: '安全提醒', value: 'security' },
  { label: '其他', value: 'other' }
]

const statusOptions = [
  { label: '草稿', value: 'draft' },
  { label: '已发布', value: 'published' },
  { label: '定时发布', value: 'scheduled' },
  { label: '已归档', value: 'archived' }
]

// 状态管理
const searchKeyword = ref('')
const filters = ref<Filters>({
  type: null,
  status: null,
  publishTime: null
})
const currentPage = ref(1)
const pageSize = ref(20)

// 弹窗状态
const showPreviewModal = ref(false)
const showDeleteModal = ref(false)
const selectedAnnouncement = ref<Announcement | null>(null)
const deleteLoading = ref(false)

// 模拟公告数据
const announcementsData = ref<Announcement[]>([
  {
    id: '1',
    title: '系统升级维护通知',
    summary: '为了提供更好的服务体验，系统将于本周六凌晨进行升级维护',
    content: '<p>尊敬的用户：</p><p>为了提供更好的服务体验，系统将于<strong>2024年1月20日（周六）凌晨2:00-6:00</strong>进行升级维护。</p><p>维护期间，系统将暂时无法访问，请您提前做好相关准备。</p><p>给您带来的不便，敬请谅解！</p>',
    type: 'maintenance',
    status: 'published',
    isPinned: true,
    author: 'admin',
    createTime: '2024-01-15 10:00:00',
    publishTime: '2024-01-15 10:30:00',
    publishMethod: 'immediate',
    publishTarget: 'all',
    views: 1520,
    likes: 45,
    comments: 12
  },
  {
    id: '2',
    title: '新功能上线：简历优化助手',
    summary: '全新推出的AI简历优化功能，帮助用户打造专业简历',
    content: '<p>很高兴地通知您，我们的AI简历优化助手功能正式上线了！</p><p>该功能能够：</p><ul><li>智能分析简历内容</li><li>提供专业优化建议</li><li>一键生成优化版本</li></ul>',
    type: 'feature',
    status: 'published',
    isPinned: false,
    author: 'admin',
    createTime: '2024-01-14 15:30:00',
    publishTime: '2024-01-14 16:00:00',
    publishMethod: 'immediate',
    publishTarget: 'jobseekers',
    views: 890,
    likes: 78,
    comments: 23
  },
  {
    id: '3',
    title: '账号安全提醒',
    summary: '请定期更新密码，开启双重验证保护账号安全',
    content: '<p>为了保护您的账号安全，建议您：</p><ul><li>定期更新密码</li><li>开启双重验证</li><li>不要在公共设备上保存登录信息</li></ul>',
    type: 'security',
    status: 'draft',
    isPinned: false,
    author: 'admin',
    createTime: '2024-01-13 09:15:00',
    views: 0,
    likes: 0,
    comments: 0
  },
  {
    id: '4',
    title: '春节放假安排',
    summary: '平台客服春节期间放假安排及相关服务调整通知',
    content: '<p>各位用户：</p><p>根据国家法定假期安排，现将春节放假安排通知如下：</p><p>放假时间：2024年2月9日-2月17日</p><p>2月18日（正月初九）正常上班。</p>',
    type: 'system',
    status: 'scheduled',
    isPinned: true,
    author: 'admin',
    createTime: '2024-01-12 14:20:00',
    publishTime: '2024-02-01 09:00:00',
    publishMethod: 'scheduled',
    publishTarget: 'all',
    views: 245,
    likes: 18,
    comments: 5
  }
])

// 计算属性
const filteredAnnouncements = computed(() => {
  let filtered = announcementsData.value

  // 类型筛选
  if (filters.value.type) {
    filtered = filtered.filter(item => item.type === filters.value.type)
  }

  // 状态筛选
  if (filters.value.status) {
    filtered = filtered.filter(item => item.status === filters.value.status)
  }

  // 发布时间筛选
  if (filters.value.publishTime) {
    const [start, end] = filters.value.publishTime
    filtered = filtered.filter(item => {
      if (!item.publishTime) return false
      const publishTime = dayjs(item.publishTime).valueOf()
      return publishTime >= start && publishTime <= end
    })
  }

  // 关键词搜索
  if (searchKeyword.value.trim()) {
    const keyword = searchKeyword.value.toLowerCase()
    filtered = filtered.filter(item =>
      item.title.toLowerCase().includes(keyword) ||
      item.summary.toLowerCase().includes(keyword) ||
      item.content.toLowerCase().includes(keyword)
    )
  }

  // 按创建时间倒序排列
  return filtered.sort((a, b) => dayjs(b.createTime).valueOf() - dayjs(a.createTime).valueOf())
})

const paginatedAnnouncements = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredAnnouncements.value.slice(start, end)
})

// 辅助方法
const formatNumber = (num: number) => {
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + 'w'
  } else if (num >= 1000) {
    return (num / 1000).toFixed(1) + 'k'
  }
  return num.toString()
}

const formatTime = (time: string) => {
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}

const getTypeType = (type: string) => {
  const typeMap: Record<string, string> = {
    system: 'info',
    maintenance: 'warning',
    feature: 'success',
    security: 'error',
    other: 'default'
  }
  return typeMap[type] || 'default'
}

const getTypeText = (type: string) => {
  const textMap: Record<string, string> = {
    system: '系统公告',
    maintenance: '维护通知',
    feature: '功能更新',
    security: '安全提醒',
    other: '其他'
  }
  return textMap[type] || type
}

const getStatusType = (status: string) => {
  const typeMap: Record<string, string> = {
    draft: 'default',
    published: 'success',
    scheduled: 'warning',
    archived: 'info'
  }
  return typeMap[status] || 'default'
}

const getStatusText = (status: string) => {
  const textMap: Record<string, string> = {
    draft: '草稿',
    published: '已发布',
    scheduled: '定时发布',
    archived: '已归档'
  }
  return textMap[status] || status
}

const getPublishMethodText = (method: string) => {
  const textMap: Record<string, string> = {
    immediate: '立即发布',
    scheduled: '定时发布'
  }
  return textMap[method] || method
}

const getPublishTargetText = (target: string) => {
  const textMap: Record<string, string> = {
    all: '全部用户',
    jobseekers: '求职者',
    hrs: 'HR用户',
    admins: '管理员'
  }
  return textMap[target] || target
}

// 事件处理
const handleFilter = () => {
  currentPage.value = 1
}

const handleSearch = (value: string) => {
  searchKeyword.value = value
  currentPage.value = 1
}

const handleRefresh = () => {
  message.success('数据已刷新')
}

const resetFilters = () => {
  filters.value = {
    type: null,
    status: null,
    publishTime: null
  }
  searchKeyword.value = ''
  currentPage.value = 1
}

const handlePageChange = (page: number) => {
  currentPage.value = page
}

const handlePageSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
}

// 操作方法
const createAnnouncement = () => {
  router.push('/dashboard/announcement/edit')
}

const viewAnnouncement = (announcement: Announcement) => {
  selectedAnnouncement.value = announcement
  showPreviewModal.value = true
}

const previewAnnouncement = (announcement: Announcement) => {
  selectedAnnouncement.value = announcement
  showPreviewModal.value = true
}

const editAnnouncement = (announcement: Announcement) => {
  router.push(`/dashboard/announcement/edit?id=${announcement.id}`)
}

const deleteAnnouncement = (announcement: Announcement) => {
  selectedAnnouncement.value = announcement
  showDeleteModal.value = true
}

const confirmDelete = async () => {
  if (!selectedAnnouncement.value) return

  deleteLoading.value = true

  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1500))

    // 从数据中移除
    const index = announcementsData.value.findIndex(item => item.id === selectedAnnouncement.value!.id)
    if (index !== -1) {
      announcementsData.value.splice(index, 1)
    }

    message.success('公告删除成功')
    showDeleteModal.value = false
    selectedAnnouncement.value = null

  } catch (error) {
    message.error('删除失败，请重试')
  } finally {
    deleteLoading.value = false
  }
}

// 页面初始化
onMounted(() => {
  // 这里可以调用API获取真实数据
})
</script>

<style scoped lang="scss">
.announcement-page {
  // 页面头部
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 32px;
    padding: 24px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 16px;
    color: white;

    .header-content {
      .page-title {
        font-size: 28px;
        font-weight: 600;
        margin: 0 0 8px 0;
      }

      .page-description {
        font-size: 16px;
        opacity: 0.9;
        margin: 0;
      }
    }

    .header-actions {
      .n-button {
        min-width: 120px;
      }
    }
  }

  // 筛选卡片
  .filter-card {
    margin-bottom: 24px;

    .filter-section {
      .filter-row {
        display: flex;
        gap: 24px;
        margin-bottom: 16px;
        flex-wrap: wrap;

        .filter-item {
          display: flex;
          align-items: center;
          gap: 8px;

          label {
            font-size: 14px;
            color: var(--text-secondary);
            white-space: nowrap;
          }
        }
      }

      .search-row {
        display: flex;
        gap: 16px;
        align-items: center;

        .search-input {
          flex: 1;

          .search-icon {
            color: var(--text-disabled);
          }
        }

        .search-actions {
          display: flex;
          gap: 12px;

          .n-button {
            min-width: 80px;
          }
        }
      }
    }
  }

  // 公告列表卡片
  .list-card {
    .list-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 24px;
      padding: 20px 24px;
      background: var(--bg-secondary);
      border-radius: 8px;

      .list-title {
        font-size: 18px;
        font-weight: 600;
        color: var(--text-primary);
      }

      .total-count {
        font-size: 14px;
        color: var(--text-secondary);
      }
    }

    .announcement-list {
      .announcement-item {
        background: var(--bg-primary);
        border: 1px solid var(--border-color);
        border-radius: 12px;
        padding: 24px;
        margin-bottom: 16px;
        cursor: pointer;
        transition: all 0.3s ease;

        &:hover {
          transform: translateY(-2px);
          box-shadow: var(--shadow-md);
          border-color: var(--primary-color);
        }

        &:last-child {
          margin-bottom: 0;
        }

        .announcement-header {
          display: flex;
          justify-content: space-between;
          align-items: flex-start;
          margin-bottom: 12px;

          .announcement-title {
            flex: 1;
            margin-right: 16px;

            h3 {
              margin: 0 0 8px 0;
              font-size: 18px;
              font-weight: 600;
              color: var(--text-primary);
            }

            .announcement-badges {
              display: flex;
              gap: 8px;
              flex-wrap: wrap;

              .n-tag {
                font-size: 12px;
              }
            }
          }

          .announcement-actions {
            display: flex;
            gap: 8px;
            flex-shrink: 0;
          }
        }

        .announcement-summary {
          margin-bottom: 12px;

          p {
            margin: 0;
            font-size: 14px;
            color: var(--text-secondary);
            line-height: 1.5;
            display: -webkit-box;
            -webkit-line-clamp: 2;
            -webkit-box-orient: vertical;
            overflow: hidden;
          }
        }

        .announcement-meta {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 8px;

          .meta-left {
            display: flex;
            gap: 16px;

            .meta-item {
              font-size: 13px;
              color: var(--text-secondary);
            }
          }

          .meta-right {
            display: flex;
            gap: 16px;

            .meta-item {
              font-size: 13px;
              color: var(--text-disabled);
            }
          }
        }

        .announcement-publish-info {
          display: flex;
          gap: 16px;
          padding-top: 8px;
          border-top: 1px solid var(--border-color);

          .publish-method, .publish-target {
            font-size: 12px;
            color: var(--text-disabled);
          }
        }
      }
    }

    .empty-state {
      text-align: center;
      padding: 60px 24px;

      .empty-icon {
        font-size: 64px;
        margin-bottom: 16px;
        opacity: 0.5;
      }

      .empty-title {
        font-size: 18px;
        font-weight: 600;
        color: var(--text-primary);
        margin: 0 0 8px 0;
      }

      .empty-description {
        font-size: 14px;
        color: var(--text-secondary);
        margin: 0 0 24px 0;
      }
    }

    .pagination-wrapper {
      display: flex;
      justify-content: center;
      margin-top: 24px;
      padding: 20px;
    }
  }

  // 预览弹窗
  .announcement-preview {
    .preview-header {
      margin-bottom: 20px;
      padding-bottom: 16px;
      border-bottom: 1px solid var(--border-color);

      .preview-title {
        margin: 0 0 12px 0;
        font-size: 20px;
        font-weight: 600;
        color: var(--text-primary);
      }

      .preview-badges {
        display: flex;
        gap: 8px;
        flex-wrap: wrap;
      }
    }

    .preview-meta {
      display: flex;
      gap: 16px;
      margin-bottom: 20px;
      padding-bottom: 16px;
      border-bottom: 1px solid var(--border-color);
      font-size: 13px;
      color: var(--text-secondary);
    }

    .preview-content {
      margin-bottom: 20px;

      .content-summary {
        margin-bottom: 16px;
        padding: 12px;
        background: var(--bg-secondary);
        border-radius: 8px;

        h4 {
          margin: 0 0 8px 0;
          font-size: 14px;
          font-weight: 600;
          color: var(--text-primary);
        }

        p {
          margin: 0;
          font-size: 14px;
          color: var(--text-secondary);
        }
      }

      .content-body {
        line-height: 1.6;
        color: var(--text-primary);

        :deep(p) {
          margin-bottom: 12px;
        }

        :deep(ul) {
          margin-bottom: 12px;
          padding-left: 20px;

          li {
            margin-bottom: 4px;
          }
        }
      }
    }

    .preview-stats {
      display: flex;
      gap: 24px;
      padding: 16px;
      background: var(--bg-secondary);
      border-radius: 8px;

      .stat-item {
        display: flex;
        align-items: center;
        gap: 8px;

        .stat-icon {
          font-size: 16px;
        }

        .stat-label {
          font-size: 13px;
          color: var(--text-secondary);
        }

        .stat-value {
          font-size: 14px;
          font-weight: 600;
          color: var(--text-primary);
        }
      }
    }
  }

  // 删除确认弹窗
  .delete-confirm {
    text-align: center;
    padding: 20px 0;

    .delete-icon {
      font-size: 48px;
      margin-bottom: 16px;
    }

    .delete-message {
      margin-bottom: 20px;
      color: var(--text-secondary);
      line-height: 1.5;
    }
  }

  .modal-actions {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
  }
}

// 响应式设计
@media (max-width: 768px) {
  .announcement-page {
    .page-header {
      flex-direction: column;
      gap: 16px;
      padding: 16px;

      .header-content {
        text-align: center;

        .page-title {
          font-size: 24px;
        }

        .page-description {
          font-size: 14px;
        }
      }

      .header-actions {
        align-self: stretch;

        .n-button {
          width: 100%;
        }
      }
    }

    .filter-card {
      .filter-section {
        .filter-row {
          flex-direction: column;
          gap: 12px;

          .filter-item {
            width: 100%;

            .n-select, .n-date-picker {
              width: 100% !important;
            }
          }
        }

        .search-row {
          flex-direction: column;
          gap: 12px;

          .search-actions {
            width: 100%;

            .n-button {
              flex: 1;
            }
          }
        }
      }
    }

    .announcement-list {
      .announcement-item {
        padding: 16px;

        .announcement-header {
          flex-direction: column;
          gap: 12px;
          align-items: flex-start;

          .announcement-title {
            margin-right: 0;
          }

          .announcement-actions {
            width: 100%;
            justify-content: flex-end;
          }
        }

        .announcement-meta {
          flex-direction: column;
          align-items: flex-start;
          gap: 8px;

          .meta-left, .meta-right {
            width: 100%;
            justify-content: flex-start;
          }
        }

        .announcement-publish-info {
          flex-direction: column;
          gap: 8px;
        }
      }
    }

    .announcement-preview {
      .preview-header {
        .preview-badges {
          width: 100%;
        }
      }

      .preview-meta {
        flex-direction: column;
        gap: 8px;
      }

      .preview-stats {
        flex-direction: column;
        gap: 12px;
      }
    }
  }
}
</style>