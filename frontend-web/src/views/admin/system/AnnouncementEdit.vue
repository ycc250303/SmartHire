<template>
  <div class="announcement-edit">
    <div class="page-header">
      <h1 class="page-title">编辑公告</h1>
      <div class="page-actions">
        <NButton @click="handleBack">返回</NButton>
        <NButton type="primary" @click="handleSave">保存</NButton>
      </div>
    </div>

    <NCard title="公告信息" :bordered="false">
      <NForm :model="formData" label-placement="left">
        <NFormItem label="标题">
          <NInput v-model:value="formData.title" placeholder="请输入公告标题" />
        </NFormItem>
        <NFormItem label="类型">
          <NSelect v-model:value="formData.type" :options="typeOptions" />
        </NFormItem>
        <NFormItem label="内容">
          <NInput
            v-model:value="formData.content"
            type="textarea"
            placeholder="请输入公告内容"
            :rows="8"
          />
        </NFormItem>
      </NForm>
    </NCard>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'

const router = useRouter()
const message = useMessage()

const formData = ref({
  title: '',
  type: 'system',
  content: ''
})

const typeOptions = [
  { label: '系统通知', value: 'system' },
  { label: '维护通知', value: 'maintenance' },
  { label: '功能更新', value: 'feature' },
  { label: '紧急公告', value: 'urgent' }
]

const handleBack = () => {
  router.back()
}

const handleSave = () => {
  message.success('保存成功')
  router.push('/dashboard/system/announcement')
}
</script>

<style scoped>
.announcement-edit {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
  }

  .page-actions {
    display: flex;
    gap: 12px;
  }
}
</style>