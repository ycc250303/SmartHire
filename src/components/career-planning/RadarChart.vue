<template>
  <view class="radar-container">
    <view class="radar-wrapper" :style="{ width: size + 'rpx', height: size + 'rpx' }">
      <canvas 
        :canvas-id="canvasId"
        :id="canvasId"
        class="radar-canvas"
        :style="{ width: size + 'rpx', height: size + 'rpx' }"
      ></canvas>
    </view>
    <view class="radar-labels">
      <view
        v-for="(label, index) in labels"
        :key="'label-' + index"
        class="radar-label-item"
        :style="getLabelStyle(index)"
      >
        <text class="radar-label-text">{{ label }}</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, onMounted, watch, nextTick } from 'vue';

interface Props {
  data: number[];
  labels: string[];
  max?: number;
  size?: number;
  color?: string;
}

const props = withDefaults(defineProps<Props>(), {
  max: 1,
  size: 400,
  color: '#4ba3ff',
});

const canvasId = 'radar-canvas-' + Date.now() + '-' + Math.floor(Math.random() * 1000);
const center = computed(() => props.size / 2);
const radius = computed(() => props.size / 2 - 80);
const numPoints = computed(() => props.labels.length);

function getAngle(index: number): number {
  return (index * 2 * Math.PI) / numPoints.value - Math.PI / 2;
}

function getPointPosition(index: number, value: number) {
  const normalizedValue = Math.min(Math.max(value / props.max, 0), 1);
  const angle = getAngle(index);
  const r = radius.value * normalizedValue;
  const x = center.value + Math.cos(angle) * r;
  const y = center.value + Math.sin(angle) * r;
  return { x, y };
}

function drawRadarChart() {
  nextTick(() => {
    // #ifdef H5
    setTimeout(() => {
      const canvas = document.getElementById(canvasId) as HTMLCanvasElement;
      if (!canvas) {
        console.warn('Canvas not found:', canvasId);
        return;
      }
      
      const ctx = canvas.getContext('2d');
      if (!ctx) {
        console.warn('Canvas context not available');
        return;
      }
      
      const dpr = window.devicePixelRatio || 1;
      const cssWidth = props.size;
      const cssHeight = props.size;
      
      canvas.width = cssWidth * dpr;
      canvas.height = cssHeight * dpr;
      canvas.style.width = cssWidth + 'px';
      canvas.style.height = cssHeight + 'px';
      ctx.scale(dpr, dpr);
      
      ctx.clearRect(0, 0, cssWidth, cssHeight);
      
      const centerX = center.value;
      const centerY = center.value;
      const rad = radius.value;
      
      for (let level = 1; level <= 5; level++) {
        const r = (rad / 5) * level;
        ctx.beginPath();
        for (let i = 0; i < numPoints.value; i++) {
          const angle = getAngle(i);
          const x = centerX + Math.cos(angle) * r;
          const y = centerY + Math.sin(angle) * r;
          if (i === 0) {
            ctx.moveTo(x, y);
          } else {
            ctx.lineTo(x, y);
          }
        }
        ctx.closePath();
        ctx.strokeStyle = '#f0f0f0';
        ctx.lineWidth = 2;
        ctx.stroke();
      }
      
      for (let i = 0; i < numPoints.value; i++) {
        const angle = getAngle(i);
        const x = centerX + Math.cos(angle) * rad;
        const y = centerY + Math.sin(angle) * rad;
        
        ctx.beginPath();
        ctx.moveTo(centerX, centerY);
        ctx.lineTo(x, y);
        ctx.strokeStyle = '#e0e0e0';
        ctx.lineWidth = 1;
        ctx.stroke();
      }
      
      if (props.data && props.data.length > 0) {
        ctx.beginPath();
        for (let i = 0; i < props.data.length; i++) {
          const { x, y } = getPointPosition(i, props.data[i]);
          if (i === 0) {
            ctx.moveTo(x, y);
          } else {
            ctx.lineTo(x, y);
          }
        }
        ctx.closePath();
        ctx.fillStyle = 'rgba(75, 163, 255, 0.3)';
        ctx.fill();
        ctx.strokeStyle = props.color;
        ctx.lineWidth = 2;
        ctx.stroke();
        
        for (let i = 0; i < props.data.length; i++) {
          const { x, y } = getPointPosition(i, props.data[i]);
          ctx.beginPath();
          ctx.arc(x, y, 6, 0, Math.PI * 2);
          ctx.fillStyle = props.color;
          ctx.fill();
          ctx.strokeStyle = '#ffffff';
          ctx.lineWidth = 2;
          ctx.stroke();
        }
      }
    }, 100);
    // #endif
    
    // #ifndef H5
    setTimeout(() => {
      const ctx = uni.createCanvasContext(canvasId);
      
      const cssWidth = props.size;
      const cssHeight = props.size;
      
      ctx.clearRect(0, 0, cssWidth, cssHeight);
      
      const centerX = center.value;
      const centerY = center.value;
      const rad = radius.value;
      
      for (let level = 1; level <= 5; level++) {
        const r = (rad / 5) * level;
        ctx.beginPath();
        for (let i = 0; i < numPoints.value; i++) {
          const angle = getAngle(i);
          const x = centerX + Math.cos(angle) * r;
          const y = centerY + Math.sin(angle) * r;
          if (i === 0) {
            ctx.moveTo(x, y);
          } else {
            ctx.lineTo(x, y);
          }
        }
        ctx.closePath();
        ctx.setStrokeStyle('#f0f0f0');
        ctx.setLineWidth(2);
        ctx.stroke();
      }
      
      for (let i = 0; i < numPoints.value; i++) {
        const angle = getAngle(i);
        const x = centerX + Math.cos(angle) * rad;
        const y = centerY + Math.sin(angle) * rad;
        
        ctx.beginPath();
        ctx.moveTo(centerX, centerY);
        ctx.lineTo(x, y);
        ctx.setStrokeStyle('#e0e0e0');
        ctx.setLineWidth(1);
        ctx.stroke();
      }
      
      if (props.data && props.data.length > 0) {
        ctx.beginPath();
        for (let i = 0; i < props.data.length; i++) {
          const { x, y } = getPointPosition(i, props.data[i]);
          if (i === 0) {
            ctx.moveTo(x, y);
          } else {
            ctx.lineTo(x, y);
          }
        }
        ctx.closePath();
        ctx.setFillStyle('rgba(75, 163, 255, 0.3)');
        ctx.fill();
        ctx.setStrokeStyle(props.color);
        ctx.setLineWidth(2);
        ctx.stroke();
        
        for (let i = 0; i < props.data.length; i++) {
          const { x, y } = getPointPosition(i, props.data[i]);
          ctx.beginPath();
          ctx.arc(x, y, 6, 0, Math.PI * 2);
          ctx.setFillStyle(props.color);
          ctx.fill();
          ctx.setStrokeStyle('#ffffff');
          ctx.setLineWidth(2);
          ctx.stroke();
        }
      }
      
      ctx.draw();
    }, 100);
    // #endif
  });
}

function getLabelStyle(index: number) {
  const angle = getAngle(index);
  const labelRadius = radius.value + 60;
  const x = Math.cos(angle) * labelRadius;
  const y = Math.sin(angle) * labelRadius;
  
  return {
    position: 'absolute',
    left: `calc(50% + ${x}rpx)`,
    top: `calc(50% + ${y}rpx)`,
    transform: 'translate(-50%, -50%)',
  };
}

watch(() => [props.data, props.labels, props.size], () => {
  drawRadarChart();
}, { deep: true });

onMounted(() => {
  drawRadarChart();
});
</script>

<style lang="scss" scoped>
.radar-container {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  min-height: 400rpx;
}

.radar-wrapper {
  position: relative;
}

.radar-canvas {
  display: block;
}

.radar-labels {
  position: absolute;
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
  pointer-events: none;
}

.radar-label-item {
  pointer-events: none;
}

.radar-label-text {
  font-size: 24rpx;
  color: #666;
  white-space: nowrap;
  text-align: center;
}
</style>
