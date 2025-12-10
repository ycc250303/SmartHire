<template>
  <div class="announcement-edit">
    <NCard title="发布公告" :bordered="false">
      <template #header-extra>
        <NSpace>
          <NButton type="tertiary" @click="handlePreview">
            <template #icon>
              <span class="icon">👁️</span>
            </template>
            预览
          </NButton>
          <NButton type="primary" :loading="publishing" @click="handlePublish">
            <template #icon>
              <span class="icon">📢</span>
            </template>
            发布公告
          </NButton>
        </NSpace>
      </template>

      <NForm
        ref="formRef"
        :model="formData"
        :rules="rules"
        label-placement="left"
        label-width="120px"
        class="form"
      >
        <!-- 基本信息 -->
        <NCard title="基本信息" size="small" :bordered="false" class="form-card">
          <NGrid :cols="2" :x-gap="24" responsive="screen">
            <NFormItemGi label="公告标题" path="title">
              <NInput
                v-model:value="formData.title"
                placeholder="请输入公告标题"
                maxlength="100"
                show-count
              />
            </NFormItemGi>

            <NFormItemGi label="公告类型" path="type">
              <NSelect
                v-model:value="formData.type"
                :options="typeOptions"
                placeholder="请选择公告类型"
              />
            </NFormItemGi>

            <NFormItemGi label="优先级" path="priority">
              <NRadioGroup v-model:value="formData.priority">
                <NRadio value="low">普通</NRadio>
                <NRadio value="medium">重要</NRadio>
                <NRadio value="high">紧急</NRadio>
              </NRadioGroup>
            </NFormItemGi>

            <NFormItemGi label="发布方式" path="publishMethod">
              <NRadioGroup v-model:value="formData.publishMethod">
                <NRadio value="immediate">立即发布</NRadio>
                <NRadio value="scheduled">定时发布</NRadio>
              </NRadioGroup>
            </NFormItemGi>

            <NFormItemGi
              v-if="formData.publishMethod === 'scheduled'"
              label="发布时间"
              path="publishTime"
            >
              <NDatePicker
                v-model:value="formData.publishTime"
                type="datetime"
                placeholder="选择发布时间"
                format="yyyy-MM-dd HH:mm:ss"
              />
            </NFormItemGi>

            <NFormItemGi label="目标用户" path="targetAudience">
              <NCheckboxGroup v-model:value="formData.targetAudience">
                <NSpace>
                  <NCheckbox value="jobseeker">求职者</NCheckbox>
                  <NCheckbox value="hr">HR用户</NCheckbox>
                  <NCheckbox value="all">全部用户</NCheckbox>
                </NSpace>
              </NCheckboxGroup>
            </NFormItemGi>
          </NGrid>
        </NCard>

        <!-- 公告内容 -->
        <NCard title="公告内容" size="small" :bordered="false" class="form-card">
          <NFormItem :show-feedback="false">
            <div class="content-editor">
              <!-- 工具栏 -->
              <div class="editor-toolbar">
                <NSpace>
                  <NButtonGroup size="small">
                    <NButton @click="insertText('**', '**')">加粗</NButton>
                    <NButton @click="insertText('*', '*')">斜体</NButton>
                    <NButton @click="insertText('~~', '~~')">删除线</NButton>
                  </NButtonGroup>

                  <NButtonGroup size="small">
                    <NButton @click="insertText('# ', '')">标题1</NButton>
                    <NButton @click="insertText('## ', '')">标题2</NButton>
                    <NButton @click="insertText('### ', '')">标题3</NButton>
                  </NButtonGroup>

                  <NButtonGroup size="small">
                    <NButton @click="insertText('[链接文字](', ')')">链接</NButton>
                    <NButton @click="insertText('- ', '')">列表</NButton>
                    <NButton @click="insertText('> ', '')">引用</NButton>
                  </NButtonGroup>
                </NSpace>
              </div>

              <!-- 编辑器 -->
              <NInput
                v-model:value="formData.content"
                type="textarea"
                :rows="15"
                placeholder="请输入公告内容（支持Markdown格式）"
                show-count
                maxlength="5000"
                class="editor-textarea"
              />
            </div>
          </NFormItem>

          <!-- 字数统计 -->
          <div class="content-stats">
            <NSpace justify="space-between">
              <span class="char-count">字数：{{ formData.content.length }}/5000</span>
              <span class="preview-hint">支持 Markdown 格式</span>
            </NSpace>
          </div>
        </NCard>

        <!-- 附加设置 -->
        <NCard title="附加设置" size="small" :bordered="false" class="form-card">
          <NGrid :cols="2" :x-gap="24" responsive="screen">
            <NFormItemGi label="是否置顶" path="isPinned">
              <NSwitch v-model:value="formData.isPinned">
                <template #checked>是</template>
                <template #unchecked>否</template>
              </NSwitch>
            </NFormItemGi>

            <NFormItemGi label="允许评论" path="allowComments">
              <NSwitch v-model:value="formData.allowComments">
                <template #checked>允许</template>
                <template #unchecked>禁止</template>
              </NSwitch>
            </NFormItemGi>

            <NFormItemGi label="到期时间" path="expireTime">
              <NDatePicker
                v-model:value="formData.expireTime"
                type="datetime"
                placeholder="选择到期时间（可选）"
                format="yyyy-MM-dd HH:mm:ss"
                clearable
              />
            </NFormItemGi>

            <NFormItemGi label="推送通知" path="sendNotification">
              <NSwitch v-model:value="formData.sendNotification">
                <template #checked>发送</template>
                <template #unchecked>不发送</template>
              </NSwitch>
            </NFormItemGi>
          </NGrid>
        </NCard>

        <!-- 附件上传 -->
        <NCard title="相关附件" size="small" :bordered="false" class="form-card">
          <NUpload
            :file-list="fileList"
            :max="5"
            @update:file-list="handleFileListChange"
          >
            <NButton>
              <template #icon>
                <span class="icon">📎</span>
              </template>
              上传附件
            </NButton>
          </NUpload>
          <template #footer>
            <NText depth="3" style="font-size: 12px;">
              支持上传图片、文档等文件，单个文件不超过10MB，最多5个文件
            </NText>
          </template>
        </NCard>
      </NForm>

      <!-- 操作按钮 -->
      <div class="form-actions">
        <NSpace>
          <NButton @click="handleSaveDraft">保存草稿</NButton>
          <NButton type="primary" :loading="publishing" @click="handlePublish">
            发布公告
          </NButton>
        </NSpace>
      </div>
    </NCard>

    <!-- 预览弹窗 -->
    <NModal
      v-model:show="previewVisible"
      :mask-closable="true"
      preset="card"
      title="公告预览"
      style="width: 800px; max-height: 80vh;"
    >
      <div class="preview-content">
        <div class="preview-header">
          <h2>{{ formData.title || '未填写标题' }}</h2>
          <div class="preview-meta">
            <NTag :type="getTypeTagType(formData.type)" size="small">
              {{ getTypeLabel(formData.type) }}
            </NTag>
            <NTag
              :type="getPriorityTagType(formData.priority)"
              size="small"
              style="margin-left: 8px;"
            >
              {{ getPriorityLabel(formData.priority) }}
            </NTag>
            <span class="preview-time">{{ formatTime(new Date()) }}</span>
          </div>
        </div>

        <NDivider />

        <div class="preview-body">
          <div v-html="formatContent(formData.content)"></div>
        </div>

        <NDivider />

        <div class="preview-footer">
          <NSpace justify="space-between">
            <span>目标用户：{{ getTargetAudienceLabel(formData.targetAudience) }}</span>
            <span v-if="formData.isPinned">📌 置顶显示</span>
          </NSpace>
        </div>
      </div>

      <template #footer>
        <NSpace justify="end">
          <NButton @click="previewVisible = false">继续编辑</NButton>
          <NButton type="primary" @click="handlePublishFromPreview">
            确认发布
          </NButton>
        </NSpace>
      </template>
    </NModal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  NCard, NForm, NFormItem, NFormItemGi, NGrid, NInput, NInputNumber,
  NSelect, NRadio, NRadioGroup, NCheckbox, NCheckboxGroup, NSwitch,
  NButton, NButtonGroup, NSpace, NUpload, NTag, NDivider, NModal,
  NDatePicker, NText, useMessage
} from 'naive-ui'
import type { FormRules, UploadFileInfo } from 'naive-ui'

const router = useRouter()
const route = useRoute()
const message = useMessage()

// 表单引用
const formRef = ref()

// 状态
const publishing = ref(false)
const previewVisible = ref(false)
const fileList = ref<UploadFileInfo[]>([])

// 表单数据
const formData = reactive({
  title: '',
  type: 'system',
  priority: 'medium',
  content: '',
  publishMethod: 'immediate',
  publishTime: null,
  targetAudience: ['all'],
  isPinned: false,
  allowComments: true,
  expireTime: null,
  sendNotification: true
})

// 表单验证规则
const rules: FormRules = {
  title: [
    { required: true, message: '请输入公告标题', trigger: 'blur' },
    { min: 5, max: 100, message: '标题长度应在5-100字符之间', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择公告类型', trigger: 'change' }
  ],
  content: [
    { required: true, message: '请输入公告内容', trigger: 'blur' },
    { min: 20, max: 5000, message: '内容长度应在20-5000字符之间', trigger: 'blur' }
  ],
  publishTime: [
    {
      required: true,
      message: '请选择发布时间',
      trigger: 'change',
      validator: (rule, value) => {
        if (formData.publishMethod === 'scheduled' && !value) {
          return new Error('定时发布必须选择发布时间')
        }
        return true
      }
    }
  ]
}

// 选项数据
const typeOptions = [
  { label: '系统公告', value: 'system' },
  { label: '功能更新', value: 'feature' },
  { label: '活动通知', value: 'activity' },
  { label: '维护通知', value: 'maintenance' },
  { label: '政策变更', value: 'policy' }
]

// 工具方法
const getTypeLabel = (type: string) => {
  const option = typeOptions.find(opt => opt.value === type)
  return option?.label || type
}

const getTypeTagType = (type: string) => {
  const typeMap: Record<string, any> = {
    system: 'default',
    feature: 'info',
    activity: 'success',
    maintenance: 'warning',
    policy: 'error'
  }
  return typeMap[type] || 'default'
}

const getPriorityLabel = (priority: string) => {
  const priorityMap: Record<string, string> = {
    low: '普通',
    medium: '重要',
    high: '紧急'
  }
  return priorityMap[priority] || priority
}

const getPriorityTagType = (priority: string) => {
  const priorityMap: Record<string, any> = {
    low: 'default',
    medium: 'warning',
    high: 'error'
  }
  return priorityMap[priority] || 'default'
}

const getTargetAudienceLabel = (audience: string[]) => {
  if (audience.includes('all')) return '全部用户'
  const labels: string[] = []
  if (audience.includes('jobseeker')) labels.push('求职者')
  if (audience.includes('hr')) labels.push('HR用户')
  return labels.join('、')
}

const formatTime = (date: Date) => {
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const formatContent = (content: string) => {
  // 简单的Markdown格式化
  return content
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/\*(.*?)\*/g, '<em>$1</em>')
    .replace(/~~(.*?)~~/g, '<del>$1</del>')
    .replace(/# (.*$)/gim, '<h1>$1</h1>')
    .replace(/## (.*$)/gim, '<h2>$1</h2>')
    .replace(/### (.*$)/gim, '<h3>$1</h3>')
    .replace(/\[([^\]]+)\]\(([^)]+)\)/g, '<a href="$2" target="_blank">$1</a>')
    .replace(/^- (.*$)/gim, '<li>$1</li>')
    .replace(/^> (.*$)/gim, '<blockquote>$1</blockquote>')
    .replace(/\n/g, '<br>')
}

// 文本插入
const insertText = (before: string, after: string) => {
  const textarea = document.querySelector('.editor-textarea textarea') as HTMLTextAreaElement
  if (!textarea) return

  const start = textarea.selectionStart
  const end = textarea.selectionEnd
  const selectedText = formData.content.substring(start, end)
  const newText = before + selectedText + after

  formData.content = formData.content.substring(0, start) + newText + formData.content.substring(end)

  // 重新设置光标位置
  setTimeout(() => {
    textarea.focus()
    textarea.setSelectionRange(start + before.length, start + before.length + selectedText.length)
  })
}

// 文件列表变化
const handleFileListChange = (files: UploadFileInfo[]) => {
  fileList.value = files
}

// 预览
const handlePreview = () => {
  // 验证必填字段
  if (!formData.title || !formData.content) {
    message.warning('请先填写公告标题和内容')
    return
  }
  previewVisible.value = true
}

// 保存草稿
const handleSaveDraft = () => {
  message.success('草稿保存成功')
  router.push('/dashboard/announcement')
}

// 发布公告
const handlePublish = async () => {
  try {
    await formRef.value?.validate()

    publishing.value = true

    // 模拟发布请求
    await new Promise(resolve => setTimeout(resolve, 1500))

    message.success('公告发布成功')
    router.push('/dashboard/announcement')
  } catch (error) {
    message.error('请完善必填信息')
  } finally {
    publishing.value = false
  }
}

// 从预览发布
const handlePublishFromPreview = async () => {
  previewVisible.value = false
  await handlePublish()
}
</script>

<style scoped lang="scss">
.announcement-edit {
  .form-card {
    margin-bottom: 16px;

    :deep(.n-card__content) {
      padding-top: 16px;
    }
  }

  .content-editor {
    width: 100%;

    .editor-toolbar {
      padding: 12px;
      background: var(--bg-secondary);
      border: 1px solid var(--border-color);
      border-bottom: none;
      border-radius: 6px 6px 0 0;
    }

    .editor-textarea {
      :deep(.n-input__textarea) {
        border-radius: 0 0 6px 6px;
        font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
        font-size: 14px;
        line-height: 1.6;
      }
    }
  }

  .content-stats {
    margin-top: 8px;
    padding: 8px 12px;
    background: var(--bg-secondary);
    border-radius: 4px;

    .char-count {
      font-size: 12px;
      color: var(--text-color-3);
    }

    .preview-hint {
      font-size: 12px;
      color: var(--primary-color);
    }
  }

  .form-actions {
    margin-top: 24px;
    padding-top: 24px;
    border-top: 1px solid var(--border-color);
    justify-content: flex-end;
  }

  .preview-content {
    .preview-header {
      h2 {
        margin: 0 0 12px 0;
        font-size: 24px;
        font-weight: 600;
      }

      .preview-meta {
        display: flex;
        align-items: center;
        gap: 8px;

        .preview-time {
          margin-left: 16px;
          color: var(--text-color-3);
          font-size: 14px;
        }
      }
    }

    .preview-body {
      min-height: 200px;
      line-height: 1.8;

      :deep(h1) {
        font-size: 20px;
        font-weight: 600;
        margin: 16px 0 12px 0;
      }

      :deep(h2) {
        font-size: 18px;
        font-weight: 600;
        margin: 14px 0 10px 0;
      }

      :deep(h3) {
        font-size: 16px;
        font-weight: 600;
        margin: 12px 0 8px 0;
      }

      :deep(strong) {
        font-weight: 600;
      }

      :deep(em) {
        font-style: italic;
      }

      :deep(del) {
        text-decoration: line-through;
      }

      :deep(blockquote) {
        border-left: 4px solid var(--primary-color);
        padding-left: 16px;
        margin: 12px 0;
        color: var(--text-color-2);
      }

      :deep(li) {
        margin: 4px 0;
        list-style: disc;
        margin-left: 20px;
      }
    }

    .preview-footer {
      padding-top: 16px;
      border-top: 1px solid var(--border-color);
      font-size: 14px;
      color: var(--text-color-2);
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .announcement-edit {
    .form-card {
      :deep(.n-grid) {
        grid-template-columns: 1fr;
      }
    }

    .editor-toolbar {
      :deep(.n-space) {
        flex-wrap: wrap;
        gap: 8px;
      }

      :deep(.n-button-group) {
        display: flex;
        flex-wrap: wrap;
        gap: 4px;
      }
    }
  }
}
</style>