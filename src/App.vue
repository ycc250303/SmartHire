<template>
  <view></view>
</template>

<script setup lang="ts">
import { onLaunch, onShow, onHide } from "@dcloudio/uni-app";
import "@/styles/index.scss";

onLaunch(() => {
  console.log("App launched");
});

onShow(() => {
  console.log("App shown");
});

onHide(() => {
  console.log("App hidden");
});
</script>
