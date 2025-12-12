<template>
  <div class="statistics-page">
    <!-- 页面标题和时间筛选 -->
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">数据统计</h1>
        <p class="page-description">查看平台运营数据和统计分析</p>
      </div>
      <div class="time-filter">
        <NRadioGroup v-model:value="timePeriod" @update:value="handleTimeChange">
          <NRadio v-for="period in timePeriods" :key="period.value" :value="period.value">
            {{ period.label }}
          </NRadio>
        </NRadioGroup>
      </div>
    </div>

    <!-- 数据概览卡片 -->
    <div class="overview-section">
      <NGrid :x-gap="24" :y-gap="24" responsive="screen">
        <NGi v-for="stat in overviewStats" :key="stat.key" span="24 s:12 m:12 l:6">
          <div class="overview-card" :class="stat.type">
            <div class="card-header">
              <div class="card-icon">{{ stat.icon }}</div>
              <div class="card-trend" :class="stat.trend">
                <span>{{ stat.trend === 'up' ? '↑' : stat.trend === 'down' ? '↓' : '→' }}</span>
              </div>
            </div>
            <div class="card-content">
              <div class="card-value">{{ formatNumber(stat.value) }}</div>
              <div class="card-label">{{ stat.label }}</div>
              <div class="card-change">{{ stat.changeText }}</div>
            </div>
          </div>
        </NGi>
      </NGrid>
    </div>

    <!-- 图表区域 -->
    <div class="charts-section">
      <NGrid :x-gap="24" :y-gap="24" responsive="screen">
        <!-- 用户增长趋势 -->
        <NGi span="24 s:24 m:24 l:12">
          <NCard title="用户增长趋势" :bordered="false" class="chart-card">
            <div ref="userChartRef" class="chart-container"></div>
          </NCard>
        </NGi>

        <!-- 职位申请趋势 -->
        <NGi span="24 s:24 m:24 l:12">
          <NCard title="职位申请趋势" :bordered="false" class="chart-card">
            <div ref="jobChartRef" class="chart-container"></div>
          </NCard>
        </NGi>

        <!-- 平台活跃度 -->
        <NGi span="24 s:24 m:12 l:8">
          <NCard title="平台活跃度" :bordered="false" class="chart-card">
            <div ref="activityChartRef" class="chart-container small"></div>
          </NCard>
        </NGi>

        <!-- 热门职位类型 -->
        <NGi span="24 s:24 m:12 l:8">
          <NCard title="热门职位类型" :bordered="false" class="chart-card">
            <div ref="categoryChartRef" class="chart-container small"></div>
          </NCard>
        </NGi>

        <!-- 用户留存率 -->
        <NGi span="24 s:24 m:12 l:8">
          <NCard title="用户留存率" :bordered="false" class="chart-card">
            <div ref="retentionChartRef" class="chart-container small"></div>
          </NCard>
        </NGi>
      </NGrid>
    </div>

    <!-- 详细数据表格 -->
    <div class="table-section">
      <NCard title="详细数据" :bordered="false">
        <div class="table-header">
          <div class="table-title">{{ currentPeriodData.title }}</div>
          <div class="table-actions">
            <NButton @click="exportData" type="primary" ghost>
              导出数据
            </NButton>
          </div>
        </div>

        <NDataTable
          :columns="tableColumns"
          :data="tableData"
          :pagination="{ pageSize: 10 }"
          striped
        />
      </NCard>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick, watch } from 'vue'
import { NGrid, NGi, NCard, NRadioGroup, NRadio, NButton, NDataTable, useMessage } from 'naive-ui'
import * as echarts from 'echarts'
import dayjs from 'dayjs'

const message = useMessage()

// 时间筛选
const timePeriod = ref('today')
const timePeriods = [
  { label: '今日', value: 'today' },
  { label: '本周', value: 'week' },
  { label: '本月', value: 'month' },
  { label: '本年', value: 'year' }
]

// 图表引用
const userChartRef = ref<HTMLElement>()
const jobChartRef = ref<HTMLElement>()
const activityChartRef = ref<HTMLElement>()
const categoryChartRef = ref<HTMLElement>()
const retentionChartRef = ref<HTMLElement>()

// 数据概览统计
const overviewStats = ref([
  {
    key: 'total-users',
    label: '总用户数',
    value: 1024,
    icon: '👥',
    type: 'primary',
    trend: 'up',
    changeText: '较昨日 +12.5%'
  },
  {
    key: 'active-users',
    label: '活跃用户',
    value: 428,
    icon: '🔥',
    type: 'success',
    trend: 'up',
    changeText: '较昨日 +8.9%'
  },
  {
    key: 'total-jobs',
    label: '活跃职位',
    value: 568,
    icon: '💼',
    type: 'warning',
    trend: 'up',
    changeText: '较昨日 +5.2%'
  },
  {
    key: 'total-applications',
    label: '申请总数',
    value: 3420,
    icon: '📝',
    type: 'info',
    trend: 'down',
    changeText: '较昨日 -2.1%'
  }
])

// 图表数据
const currentPeriodData = ref({
  title: '今日数据',
  userGrowthData: [] as any[],
  jobApplicationData: [] as any[],
  activityData: [] as any[],
  categoryData: [] as any[],
  retentionData: [] as any[]
})

// 表格数据
const tableColumns = [
  {
    title: '日期',
    key: 'date',
    render: (row: any) => dayjs(row.date).format('YYYY-MM-DD')
  },
  {
    title: '新增用户',
    key: 'newUsers'
  },
  {
    title: '活跃用户',
    key: 'activeUsers'
  },
  {
    title: '新增职位',
    key: 'newJobs'
  },
  {
    title: '新增申请',
    key: 'newApplications'
  },
  {
    title: '用户留存率',
    key: 'retentionRate',
    render: (row: any) => `${row.retentionRate}%`
  }
]

const tableData = ref([
  {
    date: '2024-01-15',
    newUsers: 45,
    activeUsers: 428,
    newJobs: 12,
    newApplications: 234,
    retentionRate: 85.2
  },
  {
    date: '2024-01-14',
    newUsers: 38,
    activeUsers: 412,
    newJobs: 15,
    newApplications: 198,
    retentionRate: 83.5
  }
])

// 生成模拟数据
const generateMockData = (period: string) => {
  const labels = getLabels(period)
  const userGrowth = labels.map(() => Math.floor(Math.random() * 50) + 20)
  const jobApplications = labels.map(() => Math.floor(Math.random() * 200) + 100)
  const activityData = [
    Math.floor(Math.random() * 100) + 300, // DAU
    Math.floor(Math.random() * 200) + 800, // MAU
    Math.floor(Math.random() * 50) + 75    // 留存率
  ]
  const categoryData = [
    { value: Math.floor(Math.random() * 200) + 100, name: '技术开发' },
    { value: Math.floor(Math.random() * 150) + 80, name: '产品设计' },
    { value: Math.floor(Math.random() * 120) + 60, name: '市场营销' },
    { value: Math.floor(Math.random() * 100) + 50, name: '人力资源' },
    { value: Math.floor(Math.random() * 80) + 40, name: '财务管理' }
  ]
  const retentionData = [85, 78, 72, 68, 65, 62, 58].map(val => val + Math.floor(Math.random() * 10) - 5)

  return {
    labels,
    userGrowth,
    jobApplications,
    activityData,
    categoryData,
    retentionData
  }
}

// 获取时间标签
const getLabels = (period: string) => {
  const now = dayjs()
  const labels = []

  switch (period) {
    case 'today':
      for (let i = 0; i < 24; i++) {
        labels.push(`${i}:00`)
      }
      break
    case 'week':
      for (let i = 6; i >= 0; i--) {
        labels.push(now.subtract(i, 'day').format('MM-DD'))
      }
      break
    case 'month':
      for (let i = 29; i >= 0; i--) {
        labels.push(now.subtract(i, 'day').format('MM-DD'))
      }
      break
    case 'year':
      for (let i = 11; i >= 0; i--) {
        labels.push(now.subtract(i, 'month').format('YYYY-MM'))
      }
      break
    default:
      for (let i = 6; i >= 0; i--) {
        labels.push(now.subtract(i, 'day').format('MM-DD'))
      }
  }

  return labels
}

// 初始化图表
const initCharts = () => {
  const data = generateMockData(timePeriod.value)

  // 用户增长趋势图
  if (userChartRef.value) {
    const userChart = echarts.init(userChartRef.value)
    userChart.setOption({
      tooltip: { trigger: 'axis' },
      legend: { data: ['新增用户', '活跃用户'] },
      xAxis: { type: 'category', data: data.labels },
      yAxis: { type: 'value' },
      series: [
        {
          name: '新增用户',
          type: 'line',
          data: data.userGrowth,
          smooth: true,
          itemStyle: { color: '#2f7cff' }
        },
        {
          name: '活跃用户',
          type: 'line',
          data: data.userGrowth.map(v => v * 8),
          smooth: true,
          itemStyle: { color: '#52c41a' }
        }
      ]
    })
  }

  // 职位申请趋势图
  if (jobChartRef.value) {
    const jobChart = echarts.init(jobChartRef.value)
    jobChart.setOption({
      tooltip: { trigger: 'axis' },
      legend: { data: ['职位发布', '简历投递'] },
      xAxis: { type: 'category', data: data.labels },
      yAxis: { type: 'value' },
      series: [
        {
          name: '职位发布',
          type: 'bar',
          data: data.labels.map(() => Math.floor(Math.random() * 20) + 5),
          itemStyle: { color: '#faad14' }
        },
        {
          name: '简历投递',
          type: 'bar',
          data: data.jobApplications,
          itemStyle: { color: '#722ed1' }
        }
      ]
    })
  }

  // 平台活跃度饼图
  if (activityChartRef.value) {
    const activityChart = echarts.init(activityChartRef.value)
    activityChart.setOption({
      tooltip: { trigger: 'item' },
      legend: { orient: 'vertical', right: 10 },
      series: [{
        type: 'pie',
        radius: '60%',
        data: [
          { value: data.activityData[0], name: 'DAU' },
          { value: data.activityData[1] - data.activityData[0], name: '非DAU用户' }
        ]
      }]
    })
  }

  // 热门职位类型图
  if (categoryChartRef.value) {
    const categoryChart = echarts.init(categoryChartRef.value)
    categoryChart.setOption({
      tooltip: { trigger: 'item' },
      series: [{
        type: 'pie',
        radius: ['40%', '70%'],
        data: data.categoryData
      }]
    })
  }

  // 用户留存率图
  if (retentionChartRef.value) {
    const retentionChart = echarts.init(retentionChartRef.value)
    retentionChart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: {
        type: 'category',
        data: ['1日', '3日', '7日', '14日', '21日', '30日', '90日']
      },
      yAxis: { type: 'value', max: 100 },
      series: [{
        type: 'line',
        data: data.retentionData,
        smooth: true,
        itemStyle: { color: '#f5222d' },
        areaStyle: { opacity: 0.3 }
      }]
    })
  }
}

// 时间切换处理
const handleTimeChange = (value: string) => {
  const periodMap: Record<string, string> = {
    today: '今日数据',
    week: '本周数据',
    month: '本月数据',
    year: '本年数据'
  }

  currentPeriodData.value.title = periodMap[value] || '数据统计'
  nextTick(() => {
    initCharts()
  })
}

// 格式化数字
const formatNumber = (num: number) => {
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + 'w'
  } else if (num >= 1000) {
    return (num / 1000).toFixed(1) + 'k'
  }
  return num.toString()
}

// 导出数据
const exportData = () => {
  message.success('数据导出功能开发中')
}

// 监听时间变化
watch(timePeriod, () => {
  handleTimeChange(timePeriod.value)
})

// 窗口大小变化时重新渲染图表
const handleResize = () => {
  nextTick(() => {
    if (userChartRef.value) echarts.getInstanceByDom(userChartRef.value)?.resize()
    if (jobChartRef.value) echarts.getInstanceByDom(jobChartRef.value)?.resize()
    if (activityChartRef.value) echarts.getInstanceByDom(activityChartRef.value)?.resize()
    if (categoryChartRef.value) echarts.getInstanceByDom(categoryChartRef.value)?.resize()
    if (retentionChartRef.value) echarts.getInstanceByDom(retentionChartRef.value)?.resize()
  })
}

onMounted(() => {
  nextTick(() => {
    initCharts()
    window.addEventListener('resize', handleResize)
  })
})
</script>

<style scoped lang="scss">
.statistics-page {
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

    .time-filter {
      .n-radio-group {
        .n-radio {
          margin-right: 16px;
          color: rgba(255, 255, 255, 0.8);

          &.n-radio--checked {
            color: white;
          }
        }
      }
    }
  }

  // 数据概览
  .overview-section {
    margin-bottom: 32px;

    .overview-card {
      padding: 24px;
      border-radius: 12px;
      background: var(--bg-primary);
      box-shadow: var(--shadow-sm);
      transition: all 0.3s ease;
      position: relative;
      overflow: hidden;

      &:hover {
        transform: translateY(-4px);
        box-shadow: var(--shadow-md);
      }

      &::before {
        content: '';
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        height: 4px;
      }

      &.primary::before {
        background: linear-gradient(90deg, #2f7cff, #1e5fcc);
      }

      &.success::before {
        background: linear-gradient(90deg, #52c41a, #389e0d);
      }

      &.warning::before {
        background: linear-gradient(90deg, #faad14, #d48806);
      }

      &.info::before {
        background: linear-gradient(90deg, #2080f0, #1a73e8);
      }

      .card-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 16px;

        .card-icon {
          font-size: 32px;
        }

        .card-trend {
          font-size: 18px;
          font-weight: 600;

          &.up {
            color: var(--success-color);
          }

          &.down {
            color: var(--error-color);
          }

          &.stable {
            color: var(--text-disabled);
          }
        }
      }

      .card-content {
        .card-value {
          font-size: 28px;
          font-weight: 700;
          color: var(--text-primary);
          margin-bottom: 8px;
        }

        .card-label {
          font-size: 16px;
          color: var(--text-secondary);
          margin-bottom: 4px;
        }

        .card-change {
          font-size: 14px;
          color: var(--text-disabled);
        }
      }
    }
  }

  // 图表区域
  .charts-section {
    margin-bottom: 32px;

    .chart-card {
      .chart-container {
        height: 400px;
        min-height: 300px;

        &.small {
          height: 300px;
        }
      }
    }
  }

  // 数据表格
  .table-section {
    .table-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 24px;
      padding: 20px 24px;
      background: var(--bg-secondary);
      border-radius: 8px;

      .table-title {
        font-size: 18px;
        font-weight: 600;
        color: var(--text-primary);
      }
    }
  }
}

// 响应式设计
@media (max-width: 1200px) {
  .statistics-page {
    .page-header {
      flex-direction: column;
      gap: 16px;
      text-align: center;
    }
  }
}

@media (max-width: 768px) {
  .statistics-page {
    .page-header {
      padding: 16px;

      .time-filter {
        .n-radio-group {
          .n-radio {
            margin-right: 8px;
            font-size: 14px;
          }
        }
      }
    }

    .overview-section {
      .overview-card {
        padding: 16px;

        .card-header {
          .card-icon {
            font-size: 24px;
          }
        }

        .card-content {
          .card-value {
            font-size: 24px;
          }
        }
      }
    }

    .charts-section {
      .chart-card {
        .chart-container {
          height: 250px;

          &.small {
            height: 200px;
          }
        }
      }
    }

    .table-section {
      .table-header {
        flex-direction: column;
        gap: 12px;
        padding: 16px;
        text-align: center;
      }
    }
  }
}
</style>