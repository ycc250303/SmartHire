<template>
  <view class="statistics-page">
    <!-- 时间筛选器 -->
    <view class="card">
      <view class="time-filter">
        <view
          class="time-item"
          v-for="period in timePeriods"
          :key="period.value"
          :class="{ active: selectedPeriod === period.value }"
          @click="selectTimePeriod(period.value)"
        >
          {{ period.label }}
        </view>
      </view>
    </view>

    <!-- 用户统计 -->
    <view class="card">
      <view class="section-title">用户统计</view>
      <view class="stats-overview">
        <view class="overview-item">
          <text class="overview-value">{{ formatNumber(totalUsers) }}</text>
          <text class="overview-label">总用户数</text>
          <text class="overview-change" :class="userTrend">
            {{ getChangeText(userChange) }}
          </text>
        </view>
        <view class="overview-item">
          <text class="overview-value">{{ formatNumber(totalCandidates) }}</text>
          <text class="overview-label">求职者</text>
          <text class="overview-change" :class="candidateTrend">
            {{ getChangeText(candidateChange) }}
          </text>
        </view>
        <view class="overview-item">
          <text class="overview-value">{{ formatNumber(totalHRs) }}</text>
          <text class="overview-label">HR用户</text>
          <text class="overview-change" :class="hrTrend">
            {{ getChangeText(hrChange) }}
          </text>
        </view>
      </view>

      <!-- 用户增长图表 -->
      <view class="chart-container">
        <view class="chart-title">用户增长趋势</view>
        <qiun-data-charts
          type="line"
          :chartData="userChartData"
          :opts="userChartOpts"
        />
      </view>
    </view>

    <!-- 招聘数据统计 -->
    <view class="card">
      <view class="section-title">招聘数据</view>
      <view class="stats-grid">
        <view class="stat-item">
          <text class="stat-value">{{ formatNumber(totalJobs) }}</text>
          <text class="stat-label">总职位数</text>
        </view>
        <view class="stat-item">
          <text class="stat-value">{{ formatNumber(activeJobs) }}</text>
          <text class="stat-label">活跃职位</text>
        </view>
        <view class="stat-item">
          <text class="stat-value">{{ formatNumber(totalApplications) }}</text>
          <text class="stat-label">总申请数</text>
        </view>
        <view class="stat-item">
          <text class="stat-value">{{ formatNumber(todayApplications) }}</text>
          <text class="stat-label">今日申请</text>
        </view>
      </view>
    </view>

    <!-- 职位申请图表 -->
    <view class="card">
      <view class="section-title">职位申请趋势</view>
      <qiun-data-charts
        type="column"
        :chartData="applicationChartData"
        :opts="applicationChartOpts"
      />
    </view>

    <!-- 平台活跃度 -->
    <view class="card">
      <view class="section-title">平台活跃度</view>
      <view class="activity-stats">
        <view class="activity-item">
          <text class="activity-value">{{ formatNumber(dau) }}</text>
          <text class="activity-label">日活用户 (DAU)</text>
          <text class="activity-desc">较昨日 {{ dauChange >= 0 ? '+' : '' }}{{ dauChange }}%</text>
        </view>
        <view class="activity-item">
          <text class="activity-value">{{ formatNumber(mau) }}</text>
          <text class="activity-label">月活用户 (MAU)</text>
          <text class="activity-desc">较上月 {{ mauChange >= 0 ? '+' : '' }}{{ mauChange }}%</text>
        </view>
        <view class="activity-item">
          <text class="activity-value">{{ retentionRate }}%</text>
          <text class="activity-label">用户留存率</text>
          <text class="activity-desc">7日留存</text>
        </view>
      </view>
    </view>

    <!-- 热门职位类型 -->
    <view class="card">
      <view class="section-title">热门职位类型</view>
      <view class="job-categories">
        <view class="category-item" v-for="category in jobCategories" :key="category.name">
          <view class="category-info">
            <text class="category-name">{{ category.name }}</text>
            <text class="category-count">{{ category.count }} 个职位</text>
          </view>
          <view class="category-progress">
            <view class="progress-bar">
              <view
                class="progress-fill"
                :style="{ width: category.percentage + '%' }"
              ></view>
            </view>
            <text class="category-percentage">{{ category.percentage }}%</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue';

interface TimePeriod {
  label: string;
  value: string;
}

interface JobCategory {
  name: string;
  count: number;
  percentage: number;
}

// 时间周期
const timePeriods: TimePeriod[] = [
  { label: '今日', value: 'today' },
  { label: '本周', value: 'week' },
  { label: '本月', value: 'month' },
  { label: '本年', value: 'year' }
];

const selectedPeriod = ref('week');

// 用户统计数据
const totalUsers = ref(2847);
const totalCandidates = ref(1923);
const totalHRs = ref(924);
const userChange = ref(8.5);
const candidateChange = ref(6.2);
const hrChange = ref(12.8);
const userTrend = ref('up');
const candidateTrend = ref('up');
const hrTrend = ref('up');

// 招聘数据
const totalJobs = ref(1567);
const activeJobs = ref(456);
const totalApplications = ref(12478);
const todayApplications = ref(127);

// 平台活跃度
const dau = ref(892);
const dauChange = ref(5.2);
const mau = ref(5234);
const mauChange = ref(8.7);
const retentionRate = ref(68);

// 热门职位类型
const jobCategories = ref<JobCategory[]>([
  { name: '技术开发', count: 234, percentage: 28 },
  { name: '产品设计', count: 189, percentage: 23 },
  { name: '市场营销', count: 156, percentage: 19 },
  { name: '人力资源', count: 123, percentage: 15 },
  { name: '财务管理', count: 98, percentage: 12 }
]);

// 图表数据
const userChartData = ref({
  categories: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
  series: [
    {
      name: '新增用户',
      data: [120, 150, 180, 200, 170, 230, 260]
    },
    {
      name: '活跃用户',
      data: [80, 120, 150, 180, 140, 200, 220]
    }
  ]
});

const applicationChartData = ref({
  categories: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
  series: [
    {
      name: '技术类',
      data: [45, 67, 82, 93, 78, 120, 95]
    },
    {
      name: '设计类',
      data: [23, 45, 65, 78, 65, 85, 72]
    },
    {
      name: '市场类',
      data: [12, 34, 45, 56, 45, 78, 65]
    }
  ]
});

// 图表配置
const userChartOpts = ref({
  color: ['#2f7cff', '#28a745'],
  padding: [15, 10, 0, 15],
  legend: {
    show: true
  },
  dataLabel: false,
  xAxis: {
    disableGrid: true
  },
  yAxis: {
    gridType: 'dash',
    dashLength: 2
  },
  extra: {
    line: {
      type: 'curve'
    }
  }
});

const applicationChartOpts = ref({
  color: ['#2f7cff', '#ff8c00', '#28a745'],
  padding: [15, 10, 0, 15],
  legend: {
    show: true,
    position: 'top'
  },
  dataLabel: true,
  xAxis: {
    disableGrid: true
  },
  yAxis: {
    gridType: 'dash',
    dashLength: 2
  }
});

const selectTimePeriod = (period: string) => {
  selectedPeriod.value = period;
  // 重新加载对应时间段的数据
  loadStatisticsData(period);
};

const loadStatisticsData = (period: string) => {
  // 根据时间段加载不同的数据
  // 这里是模拟数据，实际应该调用API
  console.log('加载统计数据:', period);

  // 根据时间段更新图表数据
  if (period === 'today') {
    userChartData.value.series[0].data = [20, 25, 30, 35, 32, 45, 50];
    userChartData.value.series[1].data = [15, 20, 25, 30, 25, 35, 40];
  } else if (period === 'week') {
    userChartData.value.series[0].data = [120, 150, 180, 200, 170, 230, 260];
    userChartData.value.series[1].data = [80, 120, 150, 180, 140, 200, 220];
  }
  // 其他时间段的数据...
};


const formatNumber = (num: number): string => {
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + 'w';
  }
  if (num >= 1000) {
    return (num / 1000).toFixed(1) + 'k';
  }
  return num.toString();
};

const getChangeText = (change: number): string => {
  if (change > 0) {
    return `↑ ${change}%`;
  } else if (change < 0) {
    return `↓ ${Math.abs(change)}%`;
  }
  return '持平';
};

onMounted(() => {
  loadStatisticsData(selectedPeriod.value);
});
</script>

<style scoped lang="scss">
.statistics-page {
  min-height: 100vh;
  padding: 32rpx;
  background: #f6f7fb;
  box-sizing: border-box;
}

.card {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 32rpx;
  margin-bottom: 28rpx;
  box-shadow: 0 12rpx 32rpx rgba(31, 55, 118, 0.08);
}

.section-title {
  font-size: 30rpx;
  font-weight: 600;
  margin-bottom: 24rpx;
}

.time-filter {
  display: flex;
  gap: 16rpx;
}

.time-item {
  flex: 1;
  padding: 20rpx 0;
  text-align: center;
  border-radius: 20rpx;
  font-size: 26rpx;
  color: #7a869a;
  background: #f0f2f7;
  transition: all 0.3s;
}

.time-item.active {
  background: #2f7cff;
  color: #ffffff;
}

.stats-overview {
  display: flex;
  gap: 20rpx;
  margin-bottom: 32rpx;
}

.overview-item {
  flex: 1;
  background: #f8faff;
  border-radius: 20rpx;
  padding: 32rpx 24rpx;
  text-align: center;
}

.overview-value {
  display: block;
  font-size: 40rpx;
  font-weight: 700;
  color: #2f7cff;
  margin-bottom: 8rpx;
}

.overview-label {
  display: block;
  font-size: 24rpx;
  color: #7a869a;
  margin-bottom: 8rpx;
}

.overview-change {
  display: block;
  font-size: 22rpx;
  font-weight: 500;
}

.overview-change.up {
  color: #28a745;
}

.overview-change.down {
  color: #ff5f5f;
}

.chart-container {
  margin-top: 32rpx;
}

.chart-title {
  font-size: 26rpx;
  font-weight: 500;
  margin-bottom: 16rpx;
}

.chart {
  width: 100%;
  height: 300rpx;
  border-radius: 16rpx;
}

.chart-large {
  width: 100%;
  height: 500rpx;
  border-radius: 16rpx;
}

.stats-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 20rpx;
}

.stat-item {
  flex: 1;
  min-width: calc(50% - 10rpx);
  background: #f8faff;
  border-radius: 20rpx;
  padding: 32rpx 24rpx;
  text-align: center;
}

.stat-value {
  display: block;
  font-size: 36rpx;
  font-weight: 600;
  color: #2f7cff;
  margin-bottom: 8rpx;
}

.stat-label {
  display: block;
  font-size: 24rpx;
  color: #7a869a;
}

.activity-stats {
  display: flex;
  gap: 20rpx;
}

.activity-item {
  flex: 1;
  background: #f8faff;
  border-radius: 20rpx;
  padding: 32rpx 24rpx;
  text-align: center;
}

.activity-value {
  display: block;
  font-size: 36rpx;
  font-weight: 600;
  color: #2f7cff;
  margin-bottom: 8rpx;
}

.activity-label {
  display: block;
  font-size: 24rpx;
  color: #7a869a;
  margin-bottom: 4rpx;
}

.activity-desc {
  font-size: 22rpx;
  color: #97a0b3;
}

.job-categories {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.category-item {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.category-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.category-name {
  font-size: 28rpx;
  font-weight: 500;
}

.category-count {
  font-size: 24rpx;
  color: #7a869a;
}

.category-progress {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.progress-bar {
  flex: 1;
  height: 12rpx;
  background: #e5edff;
  border-radius: 6rpx;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: #2f7cff;
  border-radius: 6rpx;
  transition: width 0.3s ease;
}

.category-percentage {
  font-size: 24rpx;
  color: #2f7cff;
  font-weight: 500;
  min-width: 60rpx;
  text-align: right;
}
</style>