<template>
  <div class="users-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">用户管理</h1>
        <p class="page-description">管理平台所有用户信息，支持精细化用户操作</p>
      </div>
      </div>

    <!-- 筛选和搜索 -->
    <NCard :bordered="false" class="filter-card">
      <div class="filter-section">
        <div class="filter-row">
          <div class="filter-item">
            <label>用户类型</label>
            <NSelect
              v-model:value="filters.userType"
              :options="userTypeOptions"
              placeholder="全部类型"
              clearable
              style="width: 150px"
              @update:value="handleFilter"
            />
          </div>
          <div class="filter-item">
            <label>用户状态</label>
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
            <label>注册时间</label>
            <NDatePicker
              v-model:value="filters.registerTime"
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
              placeholder="搜索用户姓名、手机、邮箱、公司"
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

    <!-- 用户列表 -->
    <NCard :bordered="false" class="list-card">
      <div class="list-header">
        <span class="list-title">用户列表</span>
        <div class="list-actions">
          <span class="total-count">共 {{ filteredUsers.length }} 条记录</span>
        </div>
      </div>

      <div class="user-list">
        <div
          v-for="user in paginatedUsers"
          :key="user.userId"
          class="user-item"
        >
          <div class="user-avatar">
            <img
              v-if="user.avatarUrl"
              :src="user.avatarUrl"
              :alt="user.username"
              class="avatar-image"
              @error="handleAvatarError"
            />
            <div v-else class="avatar-circle" :class="user.userType">
              {{ getUserIcon(user.userType) }}
            </div>
          </div>

          <div class="user-info">
            <div class="user-basic">
              <div class="user-name">
                <h3>{{ user.username }}</h3>
                <NTag :type="getStatusType(user.status)" size="small">
                  {{ getStatusText(user.status) }}
                </NTag>
                <NTag :type="getUserTypeType(user.userType)" size="small">
                  {{ getUserTypeText(user.userType) }}
                </NTag>
              </div>
              <div class="user-contact">
                <span class="contact-item">📱 {{ user.phone || '未提供' }}</span>
                <span class="contact-item">📧 {{ user.email || '未提供' }}</span>
              </div>
            </div>

            <div class="user-company" v-if="user.company">
              <span class="company-name">🏢 {{ user.company }}</span>
              <span class="position" v-if="user.position">{{ user.position }}</span>
            </div>

            <div class="user-stats">
              <div class="stat-item">
                <span class="stat-label">注册时间</span>
                <span class="stat-value">{{ formatTime(user.createTime) }}</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">最后登录</span>
                <span class="stat-value">{{ formatTime(user.lastLoginTime) }}</span>
              </div>
            </div>

            </div>

          <div class="user-actions">
            <NButton
              size="small"
              type="info"
              ghost
              @click.stop="viewUserDetail(user)"
            >
              查看详情
            </NButton>
            <NButton
              size="small"
              :type="user.status === 1 ? 'warning' : 'success'"
              @click.stop="toggleUserStatus(user)"
            >
              {{ user.status === 1 ? '封禁' : '启用' }}
            </NButton>
            <NButton
              size="small"
              type="primary"
              ghost
              @click.stop="openNotificationModal(user)"
            >
              发送通知
            </NButton>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="filteredUsers.length === 0" class="empty-state">
        <div class="empty-icon">👥</div>
        <h3 class="empty-title">暂无用户数据</h3>
        <p class="empty-description">当前没有符合条件的用户</p>
      </div>

      <!-- 分页 -->
      <div v-if="filteredUsers.length > 0" class="pagination-wrapper">
        <NPagination
          v-model:page="currentPage"
          :page-size="pageSize"
          :item-count="filteredUsers.length"
          show-size-picker
          :page-sizes="[10, 20, 50, 100]"
          @update:page="handlePageChange"
          @update:page-size="handlePageSizeChange"
        />
      </div>
    </NCard>

    <!-- 用户详情弹窗 -->
    <NModal v-model:show="showDetailModal" :mask-closable="false">
      <NCard
        style="max-width: 800px"
        title="用户详情"
        :bordered="false"
        size="huge"
        role="dialog"
        aria-modal
      >
        <template #header-extra>
          <NButton
            quaternary
            circle
            @click="showDetailModal = false"
          >
            <template #icon>
              <span class="close-icon">×</span>
            </template>
          </NButton>
        </template>

        <div v-if="selectedUser" class="user-detail" style="background: white;">
          <!-- 用户信息头部 -->
          <div class="user-header" style="display: flex !important; align-items: flex-start !important; gap: 24px !important; padding: 8px 0 !important;">
            <div class="user-avatar-section">
              <div class="avatar-container" style="width: 48px !important; height: 48px !important; border-radius: 50% !important; overflow: hidden !important; flex-shrink: 0 !important;">
                <img
                  v-if="selectedUser.avatarUrl"
                  :src="selectedUser.avatarUrl"
                  :alt="selectedUser.username"
                  class="avatar-image"
                  style="width: 48px !important; height: 48px !important; object-fit: cover !important; border-radius: 50% !important;"
                  @error="handleAvatarError"
                />
                <div
                  v-else
                  class="avatar-circle"
                  :class="selectedUser.userType"
                  :style="{
                    width: '48px !important',
                    height: '48px !important',
                    borderRadius: '50% !important',
                    display: 'flex !important',
                    alignItems: 'center !important',
                    justifyContent: 'center !important',
                    fontSize: '18px !important',
                    fontWeight: '600 !important',
                    color: 'white !important',
                    background: selectedUser.userType === 'jobseeker' ? 'linear-gradient(135deg, #2f7cff, #1e5fcc)' : selectedUser.userType === 'hr' ? 'linear-gradient(135deg, #faad14, #d48806)' : 'linear-gradient(135deg, #f5222d, #cf1322)'
                  }"
                >
                  {{ getUserIcon(selectedUser.userType) }}
                </div>
              </div>
            </div>

            <div class="user-info-section" style="flex: 1 !important; min-width: 0 !important;">
              <div class="user-name-section">
                <h3 class="user-name" style="font-size: 18px !important; font-weight: 600 !important; margin: 0 0 8px 0 !important; color: #333 !important; line-height: 1.4 !important;">{{ selectedUser.username }}</h3>
                <div class="user-tags" style="display: flex !important; gap: 8px !important; flex-wrap: wrap !important;">
                  <NTag :type="getUserTypeType(selectedUser.userType)" size="small">
                    {{ getUserTypeText(selectedUser.userType) }}
                  </NTag>
                  <NTag :type="getStatusType(selectedUser.status)" size="small">
                    {{ getStatusText(selectedUser.status) }}
                  </NTag>
                </div>
              </div>
            </div>
          </div>

          <!-- 分隔线 -->
          <NDivider />

          <!-- 详细信息列表 -->
          <div class="detail-list" style="margin-top: 16px !important;">
            <div class="detail-group" style="margin-bottom: 24px !important;">
              <div class="group-title" style="display: flex !important; align-items: center !important; gap: 8px !important; margin-bottom: 16px !important; font-size: 14px !important; font-weight: 600 !important; color: #333 !important;">
                <span class="group-icon" style="font-size: 16px !important;">📱</span>
                <span>联系方式</span>
              </div>
              <div class="detail-item" style="display: flex !important; justify-content: space-between !important; align-items: flex-start !important; padding: 12px 0 !important; border-bottom: 1px solid #f5f5f5 !important;">
                <span class="detail-label" style="font-size: 14px !important; color: #666 !important; font-weight: 500 !important; flex-shrink: 0 !important; min-width: 80px !important;">手机号码</span>
                <span class="detail-value" style="font-size: 14px !important; color: #333 !important; font-weight: 400 !important; text-align: right !important; word-break: break-all !important; flex: 1 !important; margin-left: 16px !important;">{{ selectedUser.phone || '未提供' }}</span>
              </div>
              <div class="detail-item" style="display: flex !important; justify-content: space-between !important; align-items: flex-start !important; padding: 12px 0 !important; border-bottom: 1px solid #f5f5f5 !important; border-bottom: none !important; padding-bottom: 0 !important;">
                <span class="detail-label" style="font-size: 14px !important; color: #666 !important; font-weight: 500 !important; flex-shrink: 0 !important; min-width: 80px !important;">邮箱地址</span>
                <span class="detail-value" style="font-size: 14px !important; color: #333 !important; font-weight: 400 !important; text-align: right !important; word-break: break-all !important; flex: 1 !important; margin-left: 16px !important;">{{ selectedUser.email || '未提供' }}</span>
              </div>
            </div>

            <div class="detail-group" style="margin-bottom: 24px !important;">
              <div class="group-title" style="display: flex !important; align-items: center !important; gap: 8px !important; margin-bottom: 16px !important; font-size: 14px !important; font-weight: 600 !important; color: #333 !important;">
                <span class="group-icon" style="font-size: 16px !important;">⏰</span>
                <span>时间信息</span>
              </div>
              <div class="detail-item" style="display: flex !important; justify-content: space-between !important; align-items: flex-start !important; padding: 12px 0 !important; border-bottom: 1px solid #f5f5f5 !important;">
                <span class="detail-label" style="font-size: 14px !important; color: #666 !important; font-weight: 500 !important; flex-shrink: 0 !important; min-width: 80px !important;">注册时间</span>
                <span class="detail-value" style="font-size: 14px !important; color: #333 !important; font-weight: 400 !important; text-align: right !important; word-break: break-all !important; flex: 1 !important; margin-left: 16px !important;">{{ formatTime(selectedUser.createTime) }}</span>
              </div>
              <div class="detail-item" style="display: flex !important; justify-content: space-between !important; align-items: flex-start !important; padding: 12px 0 !important; border-bottom: 1px solid #f5f5f5 !important; border-bottom: none !important; padding-bottom: 0 !important;">
                <span class="detail-label" style="font-size: 14px !important; color: #666 !important; font-weight: 500 !important; flex-shrink: 0 !important; min-width: 80px !important;">最后登录</span>
                <span class="detail-value" style="font-size: 14px !important; color: #333 !important; font-weight: 400 !important; text-align: right !important; word-break: break-all !important; flex: 1 !important; margin-left: 16px !important;">{{ formatTime(selectedUser.lastLoginTime) }}</span>
              </div>
            </div>

            <div class="detail-group" v-if="selectedUser.company" style="margin-bottom: 0 !important;">
              <div class="group-title" style="display: flex !important; align-items: center !important; gap: 8px !important; margin-bottom: 16px !important; font-size: 14px !important; font-weight: 600 !important; color: #333 !important;">
                <span class="group-icon" style="font-size: 16px !important;">🏢</span>
                <span>公司信息</span>
              </div>
              <div class="detail-item" style="display: flex !important; justify-content: space-between !important; align-items: flex-start !important; padding: 12px 0 !important; border-bottom: 1px solid #f5f5f5 !important;">
                <span class="detail-label" style="font-size: 14px !important; color: #666 !important; font-weight: 500 !important; flex-shrink: 0 !important; min-width: 80px !important;">公司名称</span>
                <span class="detail-value" style="font-size: 14px !important; color: #333 !important; font-weight: 400 !important; text-align: right !important; word-break: break-all !important; flex: 1 !important; margin-left: 16px !important;">{{ selectedUser.company }}</span>
              </div>
              <div class="detail-item" v-if="selectedUser.position" style="display: flex !important; justify-content: space-between !important; align-items: flex-start !important; padding: 12px 0 !important; border-bottom: 1px solid #f5f5f5 !important; border-bottom: none !important; padding-bottom: 0 !important;">
                <span class="detail-label" style="font-size: 14px !important; color: #666 !important; font-weight: 500 !important; flex-shrink: 0 !important; min-width: 80px !important;">职位</span>
                <span class="detail-value" style="font-size: 14px !important; color: #333 !important; font-weight: 400 !important; text-align: right !important; word-break: break-all !important; flex: 1 !important; margin-left: 16px !important;">{{ selectedUser.position }}</span>
              </div>
            </div>
          </div>
        </div>

        <template #footer>
          <div class="modal-actions">
            <NButton @click="showDetailModal = false">关闭</NButton>
            <NButton
              :type="selectedUser?.status === 1 ? 'warning' : 'success'"
              @click="toggleUserStatus(selectedUser!)"
            >
              {{ selectedUser?.status === 1 ? '封禁用户' : '启用用户' }}
            </NButton>
            <NButton type="primary" @click="sendNotification(selectedUser!)">
              发送通知
            </NButton>
          </div>
        </template>
      </NCard>
    </NModal>

    <!-- 封禁用户弹窗 -->
    <NModal v-model:show="showBanModal" :mask-closable="false">
      <NCard
        style="max-width: 500px"
        title="封禁用户"
        :bordered="false"
        size="huge"
        role="dialog"
        aria-modal
      >
        <template #header-extra>
          <NButton
            quaternary
            circle
            @click="showBanModal = false"
          >
            <template #icon>
              <span class="close-icon">×</span>
            </template>
          </NButton>
        </template>

        <div v-if="currentUserForBan" class="ban-form">
          <div class="user-info">
            <p><strong>用户：</strong>{{ currentUserForBan.username }}</p>
            <p><strong>用户类型：</strong>{{ getUserTypeText(currentUserForBan.userType) }}</p>
          </div>

          <NForm
            ref="banFormRef"
            :model="banFormData"
            :rules="banRules"
            label-placement="top"
            style="margin-top: 20px"
          >
            <NFormItem label="封禁类型" path="banType">
              <NRadioGroup v-model:value="banFormData.banType">
                <NRadio value="temporary">临时封禁</NRadio>
                <NRadio value="permanent">永久封禁</NRadio>
              </NRadioGroup>
            </NFormItem>

            <NFormItem
              v-if="banFormData.banType === 'temporary'"
              label="封禁天数"
              path="banDays"
            >
              <NInputNumber
                v-model:value="banFormData.banDays"
                :min="1"
                :max="365"
                style="width: 100%"
                placeholder="请输入封禁天数"
                @update:value="(value) => {
                  console.log('NInputNumber value changed:', value, 'banFormData.banDays:', banFormData.banDays);
                }"
              />
            </NFormItem>

            <NFormItem label="封禁原因" path="banReason">
              <NInput
                v-model:value="banFormData.banReason"
                type="textarea"
                placeholder="请输入封禁原因"
                :rows="4"
                maxlength="500"
                show-count
              />
            </NFormItem>

            <NFormItem>
              <NCheckbox v-model:checked="banFormData.sendNotification">
                发送通知给用户
              </NCheckbox>
            </NFormItem>

            <!-- 封禁通知编辑区域 -->
            <template v-if="banFormData.sendNotification">
              <NFormItem label="通知标题" path="banNotificationTitle">
                <NInput
                  v-model:value="banFormData.notificationTitle"
                  placeholder="请输入封禁通知标题"
                  maxlength="100"
                  show-count
                />
              </NFormItem>

              <NFormItem label="通知内容" path="banNotificationContent">
                <NInput
                  v-model:value="banFormData.notificationContent"
                  type="textarea"
                  placeholder="请输入封禁通知内容"
                  :rows="4"
                  maxlength="500"
                  show-count
                />
              </NFormItem>
            </template>
          </NForm>
        </div>

        <template #footer>
          <div class="modal-actions">
            <NButton @click="showBanModal = false">取消</NButton>
            <NButton
              type="warning"
              :loading="loading"
              @click="handleBanUser"
            >
              确认封禁
            </NButton>
          </div>
        </template>
      </NCard>
    </NModal>

    <!-- 解封用户弹窗 -->
    <NModal v-model:show="showUnbanModal" :mask-closable="false">
      <NCard
        style="max-width: 500px"
        title="解封用户"
        :bordered="false"
        size="huge"
        role="dialog"
        aria-modal
      >
        <template #header-extra>
          <NButton
            quaternary
            circle
            @click="showUnbanModal = false"
          >
            <template #icon>
              <span class="close-icon">×</span>
            </template>
          </NButton>
        </template>

        <div v-if="currentUserForUnban" class="unban-form">
          <div class="user-info">
            <p><strong>用户：</strong>{{ currentUserForUnban.username }}</p>
            <p><strong>用户类型：</strong>{{ getUserTypeText(currentUserForUnban.userType) }}</p>
          </div>

          <NForm
            ref="unbanFormRef"
            :model="unbanFormData"
            :rules="unbanRules"
            label-placement="top"
            style="margin-top: 20px"
          >
            <NFormItem label="解封原因" path="liftReason">
              <NInput
                v-model:value="unbanFormData.liftReason"
                type="textarea"
                placeholder="请输入解封原因"
                :rows="4"
                maxlength="500"
                show-count
              />
            </NFormItem>

            <NFormItem>
              <NCheckbox v-model:checked="unbanFormData.sendNotification">
                发送通知给用户
              </NCheckbox>
            </NFormItem>

            <!-- 解封通知编辑区域 -->
            <template v-if="unbanFormData.sendNotification">
              <NFormItem label="通知标题" path="unbanNotificationTitle">
                <NInput
                  v-model:value="unbanFormData.notificationTitle"
                  placeholder="请输入解封通知标题"
                  maxlength="100"
                  show-count
                />
              </NFormItem>

              <NFormItem label="通知内容" path="unbanNotificationContent">
                <NInput
                  v-model:value="unbanFormData.notificationContent"
                  type="textarea"
                  placeholder="请输入解封通知内容"
                  :rows="4"
                  maxlength="500"
                  show-count
                />
              </NFormItem>
            </template>
          </NForm>
        </div>

        <template #footer>
          <div class="modal-actions">
            <NButton @click="showUnbanModal = false">取消</NButton>
            <NButton
              type="success"
              :loading="loading"
              @click="handleUnbanUser"
            >
              确认解封
            </NButton>
          </div>
        </template>
      </NCard>
    </NModal>

    <!-- 发送通知弹窗 -->
    <NModal v-model:show="showNotificationModal" :mask-closable="false">
      <NCard
        style="max-width: 600px"
        title="发送通知"
        :bordered="false"
        size="huge"
        role="dialog"
        aria-modal
      >
        <template #header-extra>
          <NButton
            quaternary
            circle
            @click="showNotificationModal = false"
          >
            <template #icon>
              <span class="close-icon">×</span>
            </template>
          </NButton>
        </template>

        <div v-if="currentUserForNotification" class="notification-form">
          <div class="user-info">
            <p><strong>发送给：</strong>{{ currentUserForNotification.username }}</p>
            <p><strong>用户类型：</strong>{{ getUserTypeText(currentUserForNotification.userType) }}</p>
            <p v-if="currentUserForNotification.email"><strong>邮箱：</strong>{{ currentUserForNotification.email }}</p>
          </div>

          <NForm
            ref="notificationFormRef"
            :model="notificationFormData"
            :rules="notificationRules"
            label-placement="top"
            style="margin-top: 20px"
          >
            <NFormItem label="通知标题" path="title">
              <NInput
                v-model:value="notificationFormData.title"
                placeholder="请输入通知标题"
                maxlength="100"
                show-count
              />
            </NFormItem>

            <NFormItem label="通知内容" path="content">
              <NInput
                v-model:value="notificationFormData.content"
                type="textarea"
                placeholder="请输入通知内容"
                :rows="6"
                maxlength="1000"
                show-count
              />
            </NFormItem>

            </NForm>
        </div>

        <template #footer>
          <div class="modal-actions">
            <NButton @click="showNotificationModal = false">取消</NButton>
            <NButton
              type="primary"
              :loading="notificationLoading"
              @click="handleSendNotification"
            >
              发送通知
            </NButton>
          </div>
        </template>
      </NCard>
    </NModal>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import {
  NCard,
  NSelect,
  NDatePicker,
  NInput,
  NButton,
  NTag,
  NPagination,
  NModal,
  NForm,
  NFormItem,
  NRadioGroup,
  NRadio,
  NInputNumber,
  NCheckbox,
  useMessage,
  useDialog
} from 'naive-ui'
import type { FormInst } from 'naive-ui'
import dayjs from 'dayjs'
import { getUserList, banUser, unbanUser, type User, type UserQueryParams } from '@/api/user'
import { sendNotification as sendNotificationApi, sendNotificationWithRelated } from '@/api/notification'

// 扩展User接口以支持前端特有的字段
interface ExtendedUser extends User {
  company?: string
  position?: string
}

interface Filters {
  userType: string | null
  status: string
  registerTime: [number, number] | null
}

const message = useMessage()
const dialog = useDialog()

// 表单引用
const banFormRef = ref<FormInst | null>(null)
const unbanFormRef = ref<FormInst | null>(null)
const notificationFormRef = ref<FormInst | null>(null)


const unbanRules = {
  liftReason: [
    {
      required: true,
      message: '请输入解封原因',
      trigger: ['input', 'blur']
    },
    {
      min: 5,
      max: 500,
      message: '解封原因长度应在 5-500 个字符之间',
      trigger: ['input', 'blur']
    }
  ]
}

// 筛选选项
const userTypeOptions = [
  { label: '求职者', value: 'jobseeker' },
  { label: 'HR', value: 'hr' },
  { label: '管理员', value: 'admin' }
]

const statusOptions = [
  { label: '正常', value: 'active', type: 'success' as const },
  { label: '封禁', value: 'banned', type: 'error' as const }
]


// 通知弹窗相关状态
const showNotificationModal = ref(false)
const notificationLoading = ref(false)
const currentUserForNotification = ref<ExtendedUser | null>(null)
const notificationFormData = ref({
  title: '',
  content: '',
  type: 1,
  relatedId: undefined as number | undefined,
  relatedType: 'system'
})

// 通知表单验证规则
const notificationRules = {
  title: [
    {
      required: true,
      message: '请输入通知标题',
      trigger: ['input', 'blur']
    },
    {
      min: 2,
      max: 100,
      message: '标题长度应在 2-100 个字符之间',
      trigger: ['input', 'blur']
    }
  ],
  content: [
    {
      required: true,
      message: '请输入通知内容',
      trigger: ['input', 'blur']
    },
    {
      min: 5,
      max: 1000,
      message: '内容长度应在 5-1000 个字符之间',
      trigger: ['input', 'blur']
    }
  ]
}

// 状态管理
const searchKeyword = ref('')
const filters = ref<Filters>({
  userType: null,
  status: '',
  registerTime: null
})
const currentPage = ref(1)
const pageSize = ref(20)

// 弹窗状态
const showDetailModal = ref(false)
const selectedUser = ref<ExtendedUser | null>(null)

// 用户数据和加载状态
const usersData = ref<ExtendedUser[]>([])
const loading = ref(false)
const total = ref(0)

// 计算属性
const filteredUsers = computed(() => {
  let filtered = usersData.value

  // 用户类型筛选
  if (filters.value.userType) {
    filtered = filtered.filter(user => user.userType === filters.value.userType)
  }

  // 状态筛选
  if (filters.value.status && filters.value.status !== '') {
    filtered = filtered.filter(user => {
      const statusValue = filters.value.status === 'active' ? 1 : 0
      return user.status === statusValue
    })
  }

  // 注册时间筛选
  if (filters.value.registerTime) {
    const [start, end] = filters.value.registerTime
    filtered = filtered.filter(user => {
      const registerTime = dayjs(user.createTime).valueOf()
      return registerTime >= start && registerTime <= end
    })
  }

  // 关键词搜索
  if (searchKeyword.value.trim()) {
    const keyword = searchKeyword.value.toLowerCase()
    filtered = filtered.filter(user =>
      user.username.toLowerCase().includes(keyword) ||
      (user.phone && user.phone.includes(keyword)) ||
      (user.email && user.email.toLowerCase().includes(keyword)) ||
      (user.company && user.company.toLowerCase().includes(keyword))
    )
  }

  return filtered
})

const paginatedUsers = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredUsers.value.slice(start, end)
})

// 辅助方法
const getUserIcon = (userType: string | number) => {
  const iconMap: Record<string, string> = {
    '1': '👤', 'jobseeker': '👤', // 求职者
    '2': '💼', 'hr': '💼',        // HR
    '3': '👑', 'admin': '👑'      // 管理员
  }
  return iconMap[userType.toString()] || '👤'
}

const getUserTypeType = (userType: string | number) => {
  const typeMap: Record<string, 'info' | 'warning' | 'error' | 'success' | 'primary' | 'default'> = {
    '1': 'info', 'jobseeker': 'info', // 求职者
    '2': 'warning', 'hr': 'warning',   // HR
    '3': 'error', 'admin': 'error'       // 管理员
  }
  return typeMap[userType.toString()] || 'default'
}

const getUserTypeText = (userType: string | number) => {
  const textMap: Record<string, string> = {
    '1': '求职者', 'jobseeker': '求职者',
    '2': 'HR', 'hr': 'HR',
    '3': '管理员', 'admin': '管理员'
  }
  return textMap[userType.toString()] || '未知'
}

const getStatusType = (status: number) => {
  const typeMap: Record<number, 'success' | 'error' | 'warning' | 'info' | 'primary' | 'default'> = {
    1: 'success', // 正常
    0: 'error'    // 禁用/封禁
  }
  return typeMap[status] || 'default'
}

const getStatusText = (status: number) => {
  const textMap: Record<number, string> = {
    1: '正常',
    0: '禁用'
  }
  return textMap[status] || '未知'
}


// 格式化时间
const formatTime = (time: string | undefined) => {
  if (!time) return '暂无'
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}

// 处理头像加载失败
const handleAvatarError = (event: Event) => {
  const img = event.target as HTMLImageElement
  // 隐藏图片，显示默认头像
  img.style.display = 'none'
  const parent = img.parentElement
  if (parent) {
    const fallback = parent.querySelector('.avatar-circle') as HTMLElement
    if (fallback) {
      fallback.style.display = 'flex'
    }
  }
}


// 事件处理
const handleFilter = () => {
  currentPage.value = 1
  loadUsers()
}

const handleSearch = (value: string) => {
  searchKeyword.value = value
  currentPage.value = 1
  loadUsers()
}

const handleRefresh = () => {
  loadUsers()
}

const resetFilters = () => {
  filters.value = {
    userType: null,
    status: '',
    registerTime: null
  }
  searchKeyword.value = ''
  currentPage.value = 1
  loadUsers()
}

const handlePageChange = (page: number) => {
  currentPage.value = page
  loadUsers()
}

const handlePageSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
  loadUsers()
}

// 查看用户详情
const viewUserDetail = (user: ExtendedUser) => {
  selectedUser.value = user
  showDetailModal.value = true
}

// 切换用户状态
const toggleUserStatus = (user: ExtendedUser) => {
  if (user.status === 1) {
    // 封禁用户 - 显示封禁表单
    showBanUserDialog(user)
  } else {
    // 解封用户 - 显示解封表单
    showUnbanUserDialog(user)
  }
}


// 封禁弹窗相关状态
const showBanModal = ref(false)
const banFormData = ref({
  banType: 'temporary' as 'permanent' | 'temporary',
  banDays: 7,
  banReason: '',
  sendNotification: true,
  notificationTitle: '',
  notificationContent: ''
})
const currentUserForBan = ref<ExtendedUser | null>(null)

// 解封弹窗相关状态
const showUnbanModal = ref(false)
const unbanFormData = ref({
  liftReason: '',
  sendNotification: true,
  notificationTitle: '',
  notificationContent: ''
})
const currentUserForUnban = ref<ExtendedUser | null>(null)

// 表单验证规则 - banRules 必须在 banFormData 定义之后
const banRules = computed(() => ({
  banReason: [
    {
      required: true,
      message: '请输入封禁原因',
      trigger: ['input', 'blur']
    },
    {
      min: 5,
      max: 500,
      message: '封禁原因长度应在 5-500 个字符之间',
      trigger: ['input', 'blur']
    }
  ],
  banDays: banFormData.value.banType === 'temporary' ? [
    {
      required: true,
      validator: (rule: any, value: number) => {
        console.log('Validator called with value:', value, 'type:', typeof value);
        console.log('banFormData.value:', banFormData.value);
        console.log('rule:', rule);

        if (value === null || value === undefined || value < 1) {
          console.log('Validation failed: value is invalid');
          return new Error('请输入封禁天数')
        }
        if (value > 365) {
          console.log('Validation failed: value exceeds 365');
          return new Error('封禁天数不能超过365天')
        }
        console.log('Validation passed');
        return true
      },
      trigger: ['input', 'blur', 'change']
    }
  ] : []
}))

// 显示封禁用户弹窗
const showBanUserDialog = (user: ExtendedUser) => {
  currentUserForBan.value = user
  banFormData.value = {
    banType: 'temporary',
    banDays: 7,
    banReason: '',
    sendNotification: true,
    notificationTitle: '账户封禁通知',
    notificationContent: `您的账户因违反社区规定已被封禁。封禁原因：${user.username}。如有疑问请联系客服。`
  }
  showBanModal.value = true
}

// 显示解封用户弹窗
const showUnbanUserDialog = (user: ExtendedUser) => {
  currentUserForUnban.value = user
  unbanFormData.value = {
    liftReason: '',
    sendNotification: true,
    notificationTitle: '账户解封通知',
    notificationContent: `您好，您的账户已被解封。感谢您的理解与配合，请遵守社区规范。如有疑问请联系客服。`
  }
  showUnbanModal.value = true
}

// 处理封禁用户
const handleBanUser = async () => {
  if (!banFormRef.value || !currentUserForBan.value) {
    return
  }

  try {
    await banFormRef.value.validate()
  } catch (error) {
    return
  }

  try {
    loading.value = true

    // 先执行封禁操作
    await banUser(currentUserForBan.value.userId, {
      banDurationType: banFormData.value.banType,
      banDays: banFormData.value.banType === 'temporary' ? banFormData.value.banDays : undefined,
      banReason: banFormData.value.banReason,
      sendEmailNotification: false,  // 暂时不发送邮件通知
      sendSystemNotification: false  // 这里我们手动发送通知
    })

    // 如果选择发送通知，则发送封禁通知
    if (banFormData.value.sendNotification) {
      try {
        await sendNotificationWithRelated(
          currentUserForBan.value.userId,
          3, // 封禁通知类型
          banFormData.value.notificationTitle,
          banFormData.value.notificationContent,
          currentUserForBan.value.userId,
          'user'
        )
      } catch (notificationError: any) {
        console.error('发送封禁通知失败:', notificationError)
        // 不影响封禁操作的完成
      }
    }

    message.success(`用户"${currentUserForBan.value.username}"已封禁`)
    showBanModal.value = false
    // 刷新用户列表
    await loadUsers()
  } catch (error: any) {
    message.error(error.message || '封禁用户失败')
  } finally {
    loading.value = false
  }
}

// 处理解封用户
const handleUnbanUser = async () => {
  if (!unbanFormRef.value || !currentUserForUnban.value) return

  try {
    await unbanFormRef.value.validate()
  } catch (error) {
    return
  }

  try {
    loading.value = true

    // 先执行解封操作
    await unbanUser(currentUserForUnban.value.userId, {
      liftReason: unbanFormData.value.liftReason,
      sendNotification: false  // 这里我们手动发送通知
    })

    // 如果选择发送通知，则发送解封通知
    if (unbanFormData.value.sendNotification) {
      try {
        await sendNotificationWithRelated(
          currentUserForUnban.value.userId,
          3, // 同样使用封禁通知类型（用于账户状态变更通知）
          unbanFormData.value.notificationTitle,
          unbanFormData.value.notificationContent,
          currentUserForUnban.value.userId,
          'user'
        )
      } catch (notificationError: any) {
        console.error('发送解封通知失败:', notificationError)
        // 不影响解封操作的完成
      }
    }

    message.success(`用户"${currentUserForUnban.value.username}"已解封`)
    showUnbanModal.value = false
    // 刷新用户列表
    await loadUsers()
  } catch (error: any) {
    message.error(error.message || '解封用户失败')
  } finally {
    loading.value = false
  }
}

// 加载用户列表
const loadUsers = async () => {
  try {
    loading.value = true
    const params: UserQueryParams = {
      current: currentPage.value,
      size: pageSize.value,
      keyword: searchKeyword.value || undefined,
      userType: filters.value.userType || undefined,
      status: filters.value.status !== '' ? filters.value.status : undefined
    }

    const result = await getUserList(params)
    console.log('API result:', result);
    console.log('First user:', result.records[0]);
    usersData.value = result.records
    total.value = result.total
    console.log('Mapped usersData:', usersData.value);
  } catch (error: any) {
    message.error(error.message || '加载用户列表失败')
  } finally {
    loading.value = false
  }
}

// 发送通知
const sendNotification = (user: ExtendedUser) => {
  openNotificationModal(user)
}

// 打开发送通知弹窗
const openNotificationModal = (user: ExtendedUser) => {
  currentUserForNotification.value = user
  notificationFormData.value = {
    title: '',
    content: '',
    type: 1,
    relatedId: undefined,
    relatedType: 'system'
  }
  showNotificationModal.value = true
}

// 处理发送通知
const handleSendNotification = async () => {
  if (!notificationFormRef.value || !currentUserForNotification.value) {
    return
  }

  try {
    await notificationFormRef.value.validate()
  } catch (error) {
    return
  }

  try {
    notificationLoading.value = true

    await sendNotificationApi(
      currentUserForNotification.value.userId,
      1, // 固定为系统消息类型
      notificationFormData.value.title,
      notificationFormData.value.content
    )

    message.success(`通知已发送给"${currentUserForNotification.value.username}"`)
    showNotificationModal.value = false
  } catch (error: any) {
    message.error(error.message || '发送通知失败')
  } finally {
    notificationLoading.value = false
  }
}

// 重置密码
const resetPassword = (user: ExtendedUser) => {
  dialog.warning({
    title: '确认重置密码',
    content: `确定要重置用户"${user.username}"的密码吗？`,
    positiveText: '确定重置',
    negativeText: '取消',
    onPositiveClick: () => {
      message.success(`已重置用户"${user.username}"的密码`)
    }
  })
}

// 查看操作记录
const viewUserLogs = (user: ExtendedUser) => {
  message.info(`查看用户记录功能开发中 - 用户：${user.username}`)
}


// 页面初始化
onMounted(() => {
  loadUsers()
})
</script>

<style scoped lang="scss">
.users-page {
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

  // 用户列表卡片
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

    .user-list {
      .user-item {
        display: flex;
        gap: 20px;
        padding: 24px;
        background: var(--bg-primary);
        border: 1px solid var(--border-color);
        border-radius: 12px;
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

        .user-avatar {
          flex-shrink: 0;

          .avatar-image {
            width: 64px;
            height: 64px;
            border-radius: 50%;
            object-fit: cover;
            border: 3px solid #f0f0f0;

            &.large {
              width: 80px;
              height: 80px;
            }
          }

          .avatar-circle {
            width: 64px;
            height: 64px;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 24px;
            font-weight: 600;

            &.large {
              width: 80px;
              height: 80px;
              font-size: 32px;
            }

            &.jobseeker {
              background: linear-gradient(135deg, #2f7cff, #1e5fcc);
              color: white;
            }

            &.hr {
              background: linear-gradient(135deg, #faad14, #d48806);
              color: white;
            }

            &.admin {
              background: linear-gradient(135deg, #f5222d, #cf1322);
              color: white;
            }
          }
        }

        .user-info {
          flex: 1;
          display: flex;
          flex-direction: column;
          gap: 12px;

          .user-basic {
            .user-name {
              display: flex;
              align-items: center;
              gap: 12px;
              margin-bottom: 8px;

              h3 {
                margin: 0;
                font-size: 18px;
                font-weight: 600;
                color: var(--text-primary);
              }

              .n-tag {
                font-size: 12px;
              }
            }

            .user-contact {
              display: flex;
              gap: 16px;

              .contact-item {
                font-size: 14px;
                color: var(--text-secondary);
              }
            }
          }

          .user-company {
            display: flex;
            gap: 12px;
            align-items: center;

            .company-name {
              font-size: 14px;
              font-weight: 500;
              color: var(--text-primary);
            }

            .position {
              font-size: 13px;
              color: var(--text-secondary);
            }
          }

          .user-stats {
            display: flex;
            gap: 24px;

            .stat-item {
              display: flex;
              flex-direction: column;
              gap: 4px;

              .stat-label {
                font-size: 12px;
                color: var(--text-disabled);
              }

              .stat-value {
                font-size: 14px;
                color: var(--text-secondary);
                font-weight: 500;
              }
            }
          }

          .user-activity {
            display: flex;
            gap: 16px;
            flex-wrap: wrap;

            .activity-item {
              font-size: 13px;
              color: var(--text-secondary);
              background: var(--bg-secondary);
              padding: 4px 8px;
              border-radius: 4px;
            }
          }
        }

        .user-actions {
          flex-shrink: 0;
          display: flex;
          flex-direction: column;
          gap: 8px;
          align-items: flex-end;
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
        margin: 0;
      }
    }

    .pagination-wrapper {
      display: flex;
      justify-content: center;
      margin-top: 24px;
      padding: 20px;
    }
  }

  // 用户详情弹窗
  .user-detail {
    // 用户头部区域
    .user-header {
      display: flex;
      align-items: flex-start;
      gap: 24px;
      padding: 8px 0;

      .user-avatar-section {
        flex-shrink: 0;

        .avatar-container {
          width: 48px;
          height: 48px;
          border-radius: 50%;
          overflow: hidden;

          .avatar-image {
            width: 48px !important;
            height: 48px !important;
            object-fit: cover;
            border-radius: 50%;
          }

          .avatar-circle {
            width: 48px !important;
            height: 48px !important;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 18px !important;
            font-weight: 600;
            color: white;

            &.jobseeker {
              background: linear-gradient(135deg, #2f7cff, #1e5fcc);
            }

            &.hr {
              background: linear-gradient(135deg, #faad14, #d48806);
            }

            &.admin {
              background: linear-gradient(135deg, #f5222d, #cf1322);
            }
          }
        }
      }

      .user-info-section {
        flex: 1;
        min-width: 0;

        .user-name-section {
          .user-name {
            font-size: 18px;
            font-weight: 600;
            margin: 0 0 8px 0;
            color: #333;
            line-height: 1.4;
          }

          .user-tags {
            display: flex;
            gap: 8px;
            flex-wrap: wrap;
          }
        }
      }
    }

    // 详细信息列表
    .detail-list {
      .detail-group {
        margin-bottom: 32px;

        &:last-child {
          margin-bottom: 0;
        }

        .group-title {
          display: flex;
          align-items: center;
          gap: 8px;
          margin-bottom: 16px;
          font-size: 14px;
          font-weight: 600;
          color: #333;

          .group-icon {
            font-size: 16px;
          }
        }

        .detail-item {
          display: flex;
          justify-content: space-between;
          align-items: flex-start;
          padding: 12px 0;
          border-bottom: 1px solid #f5f5f5;

          &:last-child {
            border-bottom: none;
            padding-bottom: 0;
          }

          &:first-child {
            padding-top: 0;
          }

          .detail-label {
            font-size: 14px;
            color: #666;
            font-weight: 500;
            flex-shrink: 0;
            min-width: 80px;
          }

          .detail-value {
            font-size: 14px;
            color: #333;
            font-weight: 400;
            text-align: right;
            word-break: break-all;
            flex: 1;
            margin-left: 16px;
          }
        }
      }
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
  .users-page {
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

    .user-list {
      .user-item {
        flex-direction: column;
        gap: 16px;
        padding: 16px;

        .user-avatar {
          align-self: center;

          .avatar-image {
            width: 48px;
            height: 48px;
            border-radius: 50%;
            object-fit: cover;
            border: 2px solid #f0f0f0;
          }

          .avatar-circle {
            width: 48px;
            height: 48px;
            font-size: 20px;
          }
        }

        .user-info {
          width: 100%;

          .user-basic {
            .user-name {
              flex-wrap: wrap;
              justify-content: center;
              text-align: center;
            }

            .user-contact {
              justify-content: center;
            }
          }

          .user-company {
            justify-content: center;
          }

          .user-stats {
            justify-content: center;
          }

          .user-activity {
            justify-content: center;
          }
        }

        .user-actions {
          flex-direction: row;
          justify-content: center;
          width: 100%;

          .n-button {
            flex: 1;
          }
        }
      }
    }
  }
}
</style>