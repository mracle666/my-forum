<template>
  <div class="markdown-editor">
    <div class="editor-toolbar">
      <el-button-group class="tool-group">
        <el-button size="small" @click="wrapText('**', '**')" title="加粗 Ctrl+B">
          <strong>B</strong>
        </el-button>
        <el-button size="small" @click="wrapText('*', '*')" title="斜体 Ctrl+I">
          <em>I</em>
        </el-button>
        <el-button size="small" @click="wrapText('`', '`')" title="行内代码">
          &lt;/&gt;
        </el-button>
        <el-button size="small" @click="wrapText('~~', '~~')" title="删除线">
          <s>S</s>
        </el-button>
      </el-button-group>
      <el-button-group class="tool-group">
        <el-button size="small" @click="insertAtCursor('\n# ')" title="标题">H</el-button>
        <el-button size="small" @click="wrapLine('- ')" title="无序列表">-</el-button>
        <el-button size="small" @click="wrapLine('> ')" title="引用">&gt;</el-button>
      </el-button-group>
      <el-button-group class="tool-group">
        <el-button size="small" @click="insertLink" title="插入链接">
          <el-icon style="font-size:14px"><Link /></el-icon>
        </el-button>
        <el-button size="small" @click="triggerUpload" :loading="uploading" title="上传图片">
          <el-icon style="font-size:14px"><Picture /></el-icon>
        </el-button>
      </el-button-group>
      <input
        ref="fileInputRef"
        type="file"
        accept="image/*"
        style="display:none"
        @change="handleUpload"
      />
    </div>
    <textarea
      ref="textareaRef"
      class="editor-textarea"
      :value="modelValue"
      @input="onInput"
      placeholder="支持 Markdown 语法..."
    ></textarea>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick } from 'vue'
import { Link, Picture } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '@/api/request'

const props = defineProps<{
  modelValue: string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: string]
}>()

const textareaRef = ref<HTMLTextAreaElement>()
const fileInputRef = ref<HTMLInputElement>()
const uploading = ref(false)

function onInput(e: Event) {
  emit('update:modelValue', (e.target as HTMLTextAreaElement).value)
}

function ta(): HTMLTextAreaElement | undefined {
  return textareaRef.value
}

function getText(): string {
  return ta()?.value ?? props.modelValue
}

function setText(value: string) {
  emit('update:modelValue', value)
}

function applyChange(newValue: string, selStart: number, selEnd: number) {
  setText(newValue)
  nextTick(() => {
    const el = ta()
    if (el) {
      el.selectionStart = selStart
      el.selectionEnd = selEnd
      el.focus()
    }
  })
}

function wrapText(before: string, after: string) {
  const el = ta()
  if (!el) return
  const start = el.selectionStart
  const end = el.selectionEnd
  const text = getText()
  const selected = text.slice(start, end)
  const newText = text.slice(0, start) + before + selected + after + text.slice(end)
  applyChange(newText, start + before.length, end + before.length)
}

function wrapLine(prefix: string) {
  const el = ta()
  if (!el) return
  const start = el.selectionStart
  const text = getText()
  const lineStart = text.lastIndexOf('\n', start - 1) + 1
  const newText = text.slice(0, lineStart) + prefix + text.slice(lineStart)
  setText(newText)
  nextTick(() => {
    const e = ta()
    if (e) {
      const pos = lineStart + prefix.length
      e.selectionStart = pos
      e.selectionEnd = pos
      e.focus()
    }
  })
}

function insertAtCursor(str: string) {
  const el = ta()
  if (!el) return
  const start = el.selectionStart
  const text = getText()
  const newText = text.slice(0, start) + str + text.slice(start)
  setText(newText)
  nextTick(() => {
    const e = ta()
    if (e) {
      const pos = start + str.length
      e.selectionStart = pos
      e.selectionEnd = pos
      e.focus()
    }
  })
}

function insertLink() {
  const el = ta()
  if (!el) return
  const start = el.selectionStart
  const text = getText()
  const selected = text.slice(start, el.selectionEnd)
  const newText = text.slice(0, start) + `[${selected || '链接文字'}](url)` + text.slice(el.selectionEnd)
  applyChange(newText, start + 1, start + 1 + (selected || '链接文字').length)
}

function insertImage(url: string, alt?: string) {
  const el = ta()
  if (!el) return
  const start = el.selectionStart
  const text = getText()
  const selected = text.slice(start, el.selectionEnd)
  const altText = selected || alt || '图片'
  const newText = text.slice(0, start) + `![${altText}](${url})` + text.slice(el.selectionEnd)
  applyChange(newText, start, start)
}

function triggerUpload() {
  fileInputRef.value?.click()
}

async function handleUpload(e: Event) {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return

  uploading.value = true
  try {
    const formData = new FormData()
    formData.append('file', file)
    const result = await request.post('/upload/image', formData) as any
    if (result?.url) {
      insertImage(result.url, file.name)
      ElMessage.success('图片上传成功')
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '图片上传失败')
  } finally {
    uploading.value = false
    input.value = ''
  }
}
</script>

<style scoped>
.markdown-editor {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  overflow: hidden;
}

.editor-toolbar {
  display: flex;
  gap: 8px;
  padding: 8px;
  background: #f5f7fa;
  border-bottom: 1px solid #dcdfe6;
  flex-wrap: wrap;
}

.tool-group {
  margin-right: 4px;
}

.editor-textarea {
  width: 100%;
  min-height: 300px;
  padding: 12px;
  border: none;
  outline: none;
  resize: vertical;
  font-family: 'Fira Code', 'Consolas', monospace;
  font-size: 14px;
  line-height: 1.6;
  background: #fafafa;
}
</style>
