<template>
  <view></view>
</template>

<script setup lang="ts">
import { onLaunch, onShow, onHide } from "@dcloudio/uni-app";
import "@/styles/index.scss";
import { isTokenValid, clearToken } from "@/services/http";
import { useUserStore } from "@/store/user";

onLaunch(() => {
  console.log("App launched");
  checkAuthStatus();
});

onShow(() => {
  console.log("App shown");
});

onHide(() => {
  console.log("App hidden");
});

/**
 * Check authentication status on app launch
 * 暂时跳过登录检查，直接进入应用
 */
async function checkAuthStatus() {
  // 跳过登录检查，直接尝试加载用户信息（如果有token）
  const userStore = useUserStore();
  try {
    const valid = isTokenValid();
    if (valid) {
      await userStore.loadUserInfo();
    }
  } catch (error) {
    console.error('Failed to load user info:', error);
    // 不强制跳转，允许继续使用应用
  }
}
</script>



