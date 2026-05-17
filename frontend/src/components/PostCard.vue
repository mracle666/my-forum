<template>
  <div class="post-card" @click="$router.push(`/post/${post.id}`)">
    <div class="card-header">
      <div class="user-info">
        <el-avatar :size="36" :src="post.user?.avatar">
          {{ post.user?.username?.charAt(0)?.toUpperCase() || 'U' }}
        </el-avatar>
        <span class="username">{{ post.user?.username }}</span>
      </div>
      <span class="time">{{ formatDate(post.created_at) }}</span>
    </div>
    <h3 class="title">{{ post.title }}</h3>
    <p class="content-preview">{{ stripHtml(post.content) }}</p>
    <div class="card-footer">
      <div class="tags" v-if="post.category">
        <el-tag size="small" type="info">{{ post.category.name }}</el-tag>
      </div>
      <div class="stats">
        <span class="stat"><el-icon><View /></el-icon> {{ formatNumber(post.view_count || 0) }}</span>
        <span class="stat"><el-icon><CaretTop /></el-icon> {{ formatNumber(post.like_count || 0) }}</span>
        <span class="stat"><el-icon><Star /></el-icon> {{ formatNumber(post.favorite_count || 0) }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { View, CaretTop, Star } from '@element-plus/icons-vue'
import { formatDate, formatNumber } from '@/utils/format'
import type { PostItem } from '@/api/post'

defineProps<{
  post: PostItem
}>()

function stripHtml(html: string): string {
  const text = html.replace(/<[^>]*>/g, '')
  return text.length > 200 ? text.slice(0, 200) + '...' : text
}
</script>

<style scoped>
.post-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 16px;
  cursor: pointer;
  transition: box-shadow 0.2s;
}
.post-card:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}
.username {
  font-size: 14px;
  color: #606266;
}

.time {
  font-size: 12px;
  color: #909399;
}

.title {
  font-size: 18px;
  color: #303133;
  margin-bottom: 8px;
}

.content-preview {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  margin-bottom: 12px;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stats {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: #909399;
}
.stat {
  display: flex;
  align-items: center;
  gap: 4px;
}
</style>
