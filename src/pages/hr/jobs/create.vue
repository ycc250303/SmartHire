<template>
  <view class="page">
    <text>正在跳转到新版岗位编辑页...</text>
  </view>
</template>

<script setup lang="ts">
import { onLoad } from '@dcloudio/uni-app';

onLoad((options) => {
  const query = [];
  if (options?.jobId) query.push(`jobId=${options.jobId}`);
  const suffix = query.length ? `?${query.join('&')}` : '';
  uni.redirectTo({ url: `/pages/hr/hr/jobs/edit${suffix}` });
});
</script>

<style scoped>
.page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #7a869a;
}
</style>
