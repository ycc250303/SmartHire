<template>
  <div class="logs-page">
    <div class="page-header">
      <h1 class="page-title">操作日志</h1>
      <p class="page-description">查看系统操作记录</p>
    </div>

    <NCard title="日志列表" :bordered="false">
      <NTable :columns="columns" :data="tableData" />
    </NCard>
  </div>
</template>

<script setup lang="ts">
const columns = [
  {
    title: '操作用户',
    key: 'user'
  },
  {
    title: '操作类型',
    key: 'action'
  },
  {
    title: '操作描述',
    key: 'description'
  },
  {
    title: 'IP地址',
    key: 'ip'
  },
  {
    title: '操作时间',
    key: 'time'
  }
]

const tableData = [
  {
    id: 1,
    user: 'admin',
    action: '登录系统',
    description: '用户登录成功',
    ip: '192.168.1.1',
    time: '2024-01-15 10:30:00'
  }
]
</script>

<style scoped>
.logs-page {
  /* 日志页面样式 */
}
</style>