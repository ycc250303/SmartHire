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
  hideNativeTabBar();
});

function hideNativeTabBar() {
  try {
    uni.hideTabBar();
  } catch (error) {
    console.error('Failed to hide native tabBar:', error);
  }
}

onShow(() => {
  console.log("App shown");
});

onHide(() => {
  console.log("App hidden");
});

/**
 * Check authentication status on app launch
 */
async function checkAuthStatus() {
  const valid = isTokenValid();

  if (!valid) {
    clearToken();
    uni.redirectTo({
      url: '/pages/auth/login',
      fail: () => {
        console.error('Failed to redirect to login page');
      }
    });
    return;
  }

  const userStore = useUserStore();
  try {
    await userStore.loadUserInfo();
  } catch (error) {
    console.error('Failed to load user info:', error);
    clearToken();
    uni.redirectTo({
      url: '/pages/auth/login',
      fail: () => {
        console.error('Failed to redirect to login page');
      }
    });
  }
}
</script>
