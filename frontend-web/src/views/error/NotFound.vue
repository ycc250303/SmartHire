<template>
  <NResult status="404" title="404" description="抱歉，您访问的页面不存在">
    <template #footer>
      <NButton @click="goHome" type="primary">
        返回首页
      </NButton>
    </template>
  </NResult>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { NResult, NButton } from 'naive-ui'

const router = useRouter()

const goHome = () => {
  router.push('/dashboard')
}
</script>

<style scoped>
:deep(.n-result) {
  padding: 48px 24px;
}
</style>