<template>
  <view class="markdown-content">
    <rich-text :nodes="htmlContent"></rich-text>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue';

interface Props {
  content: string;
}

const props = defineProps<Props>();

function markdownToHtml(markdown: string): string {
  if (!markdown) return '';
  
  let html = markdown;
  
  html = html.replace(/^### (.*$)/gim, '<h3>$1</h3>');
  html = html.replace(/^## (.*$)/gim, '<h2>$1</h2>');
  html = html.replace(/^# (.*$)/gim, '<h1>$1</h1>');
  
  html = html.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>');
  html = html.replace(/\*(.*?)\*/g, '<em>$1</em>');
  
  const lines = html.split('\n');
  const processedLines: string[] = [];
  let inList = false;
  
  for (let i = 0; i < lines.length; i++) {
    const line = lines[i];
    if (!line || !line.trim()) {
      if (inList) {
        processedLines.push('</ul>');
        inList = false;
      }
      continue;
    }
    
    const listItemMatch = line.match(/^[\-\*] (.*)$/);
    const numberedMatch = line.match(/^\d+\. (.*)$/);
    
    if (listItemMatch || numberedMatch) {
      if (!inList) {
        processedLines.push('<ul>');
        inList = true;
      }
      const content = listItemMatch ? listItemMatch[1] : numberedMatch![1];
      processedLines.push(`<li>${content}</li>`);
    } else {
      if (inList) {
        processedLines.push('</ul>');
        inList = false;
      }
      processedLines.push('<p>' + line + '</p>');
    }
  }
  
  if (inList) {
    processedLines.push('</ul>');
  }
  
  html = processedLines.join('');
  
  html = html.replace(/`([^`]+)`/g, '<code>$1</code>');
  
  return html;
}

const htmlContent = computed(() => {
  if (!props.content) return '';
  try {
    return markdownToHtml(props.content);
  } catch (e) {
    console.warn('Failed to convert markdown to HTML:', e);
    return props.content.replace(/\n/g, '<br/>');
  }
});
</script>

<style lang="scss" scoped>
.markdown-content {
  font-size: 28rpx;
  color: #122038;
  line-height: 1.8;
  word-wrap: break-word;
}

:deep(h1) {
  font-size: 36rpx;
  font-weight: 600;
  margin: 24rpx 0 16rpx;
  color: #122038;
  display: block;
}

:deep(h2) {
  font-size: 32rpx;
  font-weight: 600;
  margin: 20rpx 0 12rpx;
  color: #122038;
  display: block;
}

:deep(h3) {
  font-size: 30rpx;
  font-weight: 600;
  margin: 16rpx 0 8rpx;
  color: #122038;
  display: block;
}

:deep(p) {
  margin: 12rpx 0;
  display: block;
}

:deep(strong) {
  font-weight: 600;
  color: #122038;
}

:deep(em) {
  font-style: italic;
}

:deep(ul) {
  margin: 12rpx 0;
  padding-left: 32rpx;
  display: block;
}

:deep(li) {
  margin: 8rpx 0;
  list-style-type: disc;
  display: list-item;
}

:deep(code) {
  background-color: #f5f5f5;
  padding: 4rpx 8rpx;
  border-radius: 4rpx;
  font-family: monospace;
  font-size: 26rpx;
  display: inline;
}
</style>
