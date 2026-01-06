<template>
  <CareerPlanningRadarChart
    :data="props.data"
    :labels="props.labels"
    :max="props.max"
    :size="props.size"
    :color="props.color"
  />
</template>

<script setup lang="ts">
import CareerPlanningRadarChart from '@/components/career-planning/RadarChart.vue';

const props = withDefaults(
  defineProps<{
    data: number[];
    labels: string[];
    max?: number;
    size?: number;
    color?: string;
  }>(),
  {
    max: 100,
    size: 300,
    color: '#4ba3ff',
  },
);
</script>
