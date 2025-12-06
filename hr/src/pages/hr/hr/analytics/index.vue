<template>
  <view class="analytics-page">
    <view class="card">
      <view class="title">招聘漏斗分析</view>
      <view class="desc">后续将接入 ECharts 展示投递、筛选、转化率等数据。</view>
    </view>
    <view class="card">
      <view class="title">智能洞察</view>
      <view class="desc">结合 LLM 输出招聘策略建议，目前为岗位示例。</view>
    </view>
  </view>
</template>

<script setup lang="ts">
// TODO: 接入可视化图表与分析接口
</script>

<style scoped lang="scss">
.analytics-page {
  padding: 24rpx;
  background: #f6f7fb;
  min-height: 100vh;
}

.card {
  background: #fff;
  border-radius: 24rpx;
  padding: 28rpx;
  margin-bottom: 24rpx;
}

.title {
  font-size: 30rpx;
  font-weight: 600;
}

.desc {
  margin-top: 12rpx;
  color: #8c94a8;
}
</style>
