<template>
  <view></view>
</template>

<script setup lang="ts">
import { onLaunch, onShow, onHide } from "@dcloudio/uni-app";
import "@/styles/index.scss";
import { setTokenWithExpiry } from "@/services/http";
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
 * 启动时直接跳过登录，写入本地 token 并设置一个本地用户，方便开发
 */
async function checkAuthStatus() {
  const userStore = useUserStore();
  try {
    // 写入一个开发用 token，避免鉴权拦截
    setTokenWithExpiry("dev-bypass-token");
    // 直接设置本地用户信息，避免请求后端
    userStore.setUserInfo({
      userId: 0,
      username: "demo",
      email: "demo@smarthire.cn",
    } as any);
  } catch (error) {
    console.error("Failed to bypass auth:", error);
  }
}
</script>


