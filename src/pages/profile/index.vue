<template>
  <view class="page">
    
    
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { t } from '@/locales';



onLoad(() => {
  uni.setNavigationBarTitle({
    title: t('navigation.profile')
  });
});


</script>

<style lang="scss" scoped>
@use "@/styles/variables.scss" as vars;

.page {
  min-height: 100vh;
  background-color: vars.$bg-color;
  padding: vars.$spacing-md;
}
</style>
