<template>
  <view class="radar-box" :style="{ width: size + 'rpx', height: size + 'rpx' }">
    <!-- Chart Background (Grid & Data) -->
    <view 
      class="radar-chart"
      :style="{ backgroundImage: `url('${chartSvgUrl}')` }"
    ></view>
    
    <!-- Labels Layer (HTML Elements for better text control) -->
    <view class="labels-layer">
      <view
        v-for="(label, index) in labels"
        :key="index"
        class="label-item"
        :style="getLabelPos(index)"
      >
        <text class="label-text">{{ label }}</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue';

const props = withDefaults(defineProps<{
  data: number[];
  labels: string[];
  max?: number;
  size?: number;
  color?: string;
}>(), {
  max: 100,
  size: 300,
  color: '#4ba3ff'
});

// Calculate SVG Path Data
const chartSvgUrl = computed(() => {
  const size = 100;
  const cx = size / 2;
  const cy = size / 2;
  const radius = 35;
  const count = props.labels.length || 3;
  
  // Helper to calculate points
  const getPoint = (idx: number, r: number) => {
    const angle = (idx * 2 * Math.PI) / count - Math.PI / 2;
    return {
      x: cx + Math.cos(angle) * r,
      y: cy + Math.sin(angle) * r
    };
  };

  // 1. Generate Grid (5 levels)
  let gridPaths = '';
  for (let level = 1; level <= 5; level++) {
    const r = (radius / 5) * level;
    let path = '';
    for (let i = 0; i < count; i++) {
      const p = getPoint(i, r);
      path += `${i === 0 ? 'M' : 'L'} ${p.x.toFixed(2)} ${p.y.toFixed(2)} `;
    }
    path += 'Z';
    gridPaths += `<path d="${path}" fill="none" stroke="#e8e8e8" stroke-width="0.5" />`;
  }

  // 2. Generate Axes
  let axesPaths = '';
  for (let i = 0; i < count; i++) {
    const p = getPoint(i, radius);
    axesPaths += `<line x1="${cx}" y1="${cy}" x2="${p.x.toFixed(2)}" y2="${p.y.toFixed(2)}" stroke="#d0d0d0" stroke-width="0.5" />`;
  }

  // 3. Generate Data Polygon
  let dataPath = '';
  let dataPoints = '';
  if (props.data && props.data.length) {
    for (let i = 0; i < count; i++) {
      let val = props.data[i] || 0;
      
      const rawRatio = Math.min(Math.max(val / props.max, 0), 1);
      const minRatio = 0.2;
      const ratio = minRatio + rawRatio * (1 - minRatio);
      
      const r = radius * ratio;
      const p = getPoint(i, r);
      dataPath += `${i === 0 ? 'M' : 'L'} ${p.x.toFixed(2)} ${p.y.toFixed(2)} `;
      
      dataPoints += `<circle cx="${p.x.toFixed(2)}" cy="${p.y.toFixed(2)}" r="1.5" fill="${props.color}" stroke="#fff" stroke-width="0.5" />`;
    }
    dataPath += 'Z';
  }

  const svgString = `
    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 ${size} ${size}">
      ${gridPaths}
      ${axesPaths}
      <path d="${dataPath}" fill="rgba(75, 163, 255, 0.2)" stroke="${props.color}" stroke-width="1" />
      ${dataPoints}
    </svg>
  `.replace(/\s+/g, ' ').trim();

  const encoded = encodeURIComponent(svgString)
    .replace(/'/g, '%27')
    .replace(/"/g, '%22');

  return `data:image/svg+xml;charset=utf-8,${encoded}`;
});

function getLabelPos(index: number) {
  const count = props.labels.length || 3;
  const angle = (index * 2 * Math.PI) / count - Math.PI / 2;
  const r = 45; 
  
  const x = 50 + Math.cos(angle) * r;
  const y = 50 + Math.sin(angle) * r;
  
  return {
    left: x + '%',
    top: y + '%',
    transform: 'translate(-50%, -50%)'
  };
}
</script>

<style lang="scss" scoped>
.radar-box {
  position: relative;
  margin: 0 auto;
}

.radar-chart {
  width: 100%;
  height: 100%;
  background-position: center;
  background-repeat: no-repeat;
  background-size: contain;
}

.labels-layer {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.label-item {
  position: absolute;
  white-space: nowrap; 
  width: auto;
  display: flex;
  align-items: center;
  justify-content: center;
}

.label-text {
  font-size: 24rpx;
  color: #666;
  text-align: center;
  text-shadow: 0 1px 2px rgba(255,255,255,0.9);
  white-space: nowrap;
}
</style>