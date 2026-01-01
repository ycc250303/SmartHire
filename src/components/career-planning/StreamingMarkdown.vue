<template>
  <!-- #ifdef H5 -->
  <view class="streaming-markdown-content">
    <rich-text :nodes="htmlContent" class="markdown-html"></rich-text>
    <text v-if="isStreaming" class="streaming-cursor">|</text>
  </view>
  <!-- #endif -->
  
  <!-- #ifndef H5 -->
  <view class="streaming-markdown-content">
    <rich-text :nodes="htmlContent" class="markdown-html"></rich-text>
    <text v-if="isStreaming" class="streaming-cursor">|</text>
  </view>
  <!-- #endif -->
</template>

<script setup lang="ts">
import { computed } from 'vue';

interface Props {
  content: string;
  isStreaming?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  isStreaming: false,
});

function markdownToHtml(markdown: string): string {
  if (!markdown) return '';
  
  let html = markdown;
  
  html = html.replace(/^#### (.*$)/gim, '<h4>$1</h4>');
  html = html.replace(/^### (.*$)/gim, '<h3>$1</h3>');
  html = html.replace(/^## (.*$)/gim, '<h2>$1</h2>');
  html = html.replace(/^# (.*$)/gim, '<h1>$1</h1>');
  
  html = html.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>');
  html = html.replace(/\*(.*?)\*/g, '<em>$1</em>');
  
  html = html.replace(/```[\s\S]*?```/g, (match) => {
    const code = match.replace(/```/g, '').trim();
    return `<pre><code>${escapeHtml(code)}</code></pre>`;
  });
  
  html = html.replace(/`([^`]+)`/g, '<code>$1</code>');
  
  const lines = html.split('\n');
  const processedLines: string[] = [];
  let inList = false;
  let inOrderedList = false;
  
  for (let i = 0; i < lines.length; i++) {
    const line = lines[i];
    const trimmedLine = line.trim();
    
    if (!trimmedLine) {
      if (inList) {
        processedLines.push('</ul>');
        inList = false;
      }
      if (inOrderedList) {
        processedLines.push('</ol>');
        inOrderedList = false;
      }
      continue;
    }
    
    const orderedMatch = trimmedLine.match(/^(\d+)\.\s+(.*)$/);
    if (orderedMatch) {
      if (!inOrderedList) {
        if (inList) {
          processedLines.push('</ul>');
          inList = false;
        }
        processedLines.push('<ol>');
        inOrderedList = true;
      }
      processedLines.push(`<li>${orderedMatch[2]}</li>`);
      continue;
    }
    
    const listMatch = trimmedLine.match(/^[\-\*\+]\s+(.*)$/);
    if (listMatch) {
      if (!inList) {
        if (inOrderedList) {
          processedLines.push('</ol>');
          inOrderedList = false;
        }
        processedLines.push('<ul>');
        inList = true;
      }
      processedLines.push(`<li>${listMatch[1]}</li>`);
      continue;
    }
    
    if (trimmedLine.match(/^<h[1-4]>/)) {
      if (inList) {
        processedLines.push('</ul>');
        inList = false;
      }
      if (inOrderedList) {
        processedLines.push('</ol>');
        inOrderedList = false;
      }
      processedLines.push(trimmedLine);
      continue;
    }
    
    if (inList) {
      processedLines.push('</ul>');
      inList = false;
    }
    if (inOrderedList) {
      processedLines.push('</ol>');
      inOrderedList = false;
    }
    
    if (trimmedLine.startsWith('<pre>')) {
      processedLines.push(trimmedLine);
    } else {
      processedLines.push('<p>' + trimmedLine + '</p>');
    }
  }
  
  if (inList) {
    processedLines.push('</ul>');
  }
  if (inOrderedList) {
    processedLines.push('</ol>');
  }
  
  return processedLines.join('');
}

function escapeHtml(text: string): string {
  const map: Record<string, string> = {
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;',
    "'": '&#039;',
  };
  return text.replace(/[&<>"']/g, (m) => map[m]);
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
@use "@/styles/variables.scss" as vars;

.streaming-markdown-content {
  position: relative;
  display: flex;
  flex-direction: row;
  align-items: flex-start;
  width: 100%;
}

.markdown-html {
  flex: 1;
  word-wrap: break-word;
}

.markdown-text {
  flex: 1;
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.8;
}

.streaming-cursor {
  display: inline-block;
  font-weight: bold;
  color: vars.$primary-color;
  animation: blink 1s infinite;
  margin-left: 4rpx;
  flex-shrink: 0;
}

@keyframes blink {
  0%, 50% {
    opacity: 1;
  }
  51%, 100% {
    opacity: 0;
  }
}

:deep(h1) {
  font-size: 36rpx;
  font-weight: 600;
  margin: 32rpx 0 16rpx;
  color: vars.$text-color;
  display: block;
  line-height: 1.4;
}

:deep(h2) {
  font-size: 28rpx;
  font-weight: 600;
  margin: 24rpx 0 12rpx;
  color: vars.$text-color;
  display: block;
  line-height: 1.4;
}

:deep(h3) {
  font-size: 26rpx;
  font-weight: 600;
  margin: 20rpx 0 10rpx;
  color: vars.$text-color;
  display: block;
  line-height: 1.4;
}

:deep(h4) {
  font-size: 24rpx;
  font-weight: 600;
  margin: 16rpx 0 8rpx;
  color: vars.$text-color;
  display: block;
  line-height: 1.4;
}

:deep(p) {
  margin: 16rpx 0;
  display: block;
  line-height: 1.6;
  font-size: 24rpx;
  color: vars.$text-color;
}

:deep(strong) {
  font-weight: 600;
  color: vars.$text-color;
}

:deep(em) {
  font-style: italic;
}

:deep(ul),
:deep(ol) {
  margin: 16rpx 0;
  padding-left: 40rpx;
  display: block;
}

:deep(li) {
  margin: 8rpx 0;
  display: list-item;
  line-height: 1.6;
  font-size: 24rpx;
  color: vars.$text-color;
}

:deep(ul li) {
  list-style-type: disc;
}

:deep(ol li) {
  list-style-type: decimal;
}

:deep(code) {
  background-color: #f5f5f5;
  padding: 4rpx 8rpx;
  border-radius: 4rpx;
  font-family: 'Courier New', monospace;
  font-size: 22rpx;
  display: inline;
  color: #e83e8c;
}

:deep(pre) {
  background-color: #f5f5f5;
  border-radius: 8rpx;
  padding: 16rpx;
  margin: 16rpx 0;
  overflow-x: auto;
  display: block;
}

:deep(pre code) {
  background-color: transparent;
  padding: 0;
  color: vars.$text-color;
  display: block;
  white-space: pre;
}
</style>
