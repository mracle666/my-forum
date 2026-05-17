<template>
  <div class="markdown-viewer" v-html="renderedHtml"></div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { marked } from 'marked'

const props = defineProps<{
  content: string
  isRaw?: boolean
}>()

const renderedHtml = computed(() => {
  const text = props.isRaw ? props.content : props.content
  if (!props.isRaw) {
    return text
  }
  return marked(text) as string
})
</script>

<style scoped>
.markdown-viewer {
  font-size: 15px;
  line-height: 1.8;
  color: #303133;
  word-break: break-word;
}
.markdown-viewer :deep(h1),
.markdown-viewer :deep(h2),
.markdown-viewer :deep(h3) {
  margin: 16px 0 8px;
  font-weight: 600;
}
.markdown-viewer :deep(h1) { font-size: 24px; }
.markdown-viewer :deep(h2) { font-size: 20px; }
.markdown-viewer :deep(h3) { font-size: 16px; }
.markdown-viewer :deep(p) {
  margin: 8px 0;
}
.markdown-viewer :deep(pre) {
  background: #f5f7fa;
  padding: 12px;
  border-radius: 4px;
  overflow-x: auto;
  margin: 8px 0;
}
.markdown-viewer :deep(code) {
  font-family: 'Fira Code', monospace;
  font-size: 13px;
}
.markdown-viewer :deep(blockquote) {
  border-left: 4px solid #409eff;
  padding-left: 12px;
  margin: 8px 0;
  color: #606266;
}
.markdown-viewer :deep(img) {
  max-width: 100%;
  border-radius: 4px;
}
.markdown-viewer :deep(a) {
  color: #409eff;
}
.markdown-viewer :deep(ul),
.markdown-viewer :deep(ol) {
  padding-left: 20px;
  margin: 8px 0;
}
.markdown-viewer :deep(table) {
  border-collapse: collapse;
  width: 100%;
  margin: 8px 0;
}
.markdown-viewer :deep(th),
.markdown-viewer :deep(td) {
  border: 1px solid #dcdfe6;
  padding: 8px 12px;
  text-align: left;
}
.markdown-viewer :deep(th) {
  background: #f5f7fa;
}
</style>
