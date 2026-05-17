<template>
  <div class="comment-list">
    <div v-if="comments.length === 0" class="empty">暂无回复</div>
    <div
      v-for="comment in comments"
      :key="comment.id"
      :id="'comment-' + comment.id"
      class="comment-item"
      :class="{ 'is-child': depth > 0, 'is-highlight': highlightId === comment.id }"
    >
      <div class="comment-header">
        <div class="comment-user">
          <el-avatar :size="28" :src="comment.user?.avatar">
            {{ comment.user?.username?.charAt(0)?.toUpperCase() || 'U' }}
          </el-avatar>
          <router-link :to="`/user/${comment.user_id}`" class="comment-username">
            {{ comment.user?.username }}
          </router-link>
          <span class="time">{{ formatDate(comment.created_at) }}</span>
        </div>
        <span class="comment-num">#{{ depth > 0 ? '' : comment.id }}</span>
      </div>
      <div class="comment-content">{{ comment.content }}</div>
      <div class="comment-actions">
        <span
          class="action-btn"
          :class="{ active: comment.liked }"
          @click="$emit('like', comment)"
        >
          <el-icon><CaretTop v-if="comment.liked" /><CaretTop v-else /></el-icon>
          {{ comment.like_count || 0 }}
        </span>
        <span class="action-btn" @click="$emit('reply', comment)">
          <el-icon><ChatDotRound /></el-icon> 回复
        </span>
        <span
          v-if="canDelete(comment)"
          class="action-btn delete"
          @click="$emit('delete', comment)"
        >
          删除
        </span>
      </div>
      <CommentList
        v-if="comment.children?.length"
        :comments="comment.children"
        :depth="depth + 1"
        :current-user-id="currentUserId"
        :highlight-id="highlightId"
        @like="$emit('like', $event)"
        @reply="$emit('reply', $event)"
        @delete="$emit('delete', $event)"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { CaretTop, ChatDotRound } from '@element-plus/icons-vue'
import { formatDate } from '@/utils/format'
import type { CommentItem } from '@/api/comment'
import { useUserStore } from '@/stores/user'

const props = withDefaults(defineProps<{
  comments: CommentItem[]
  depth?: number
  currentUserId?: number
  highlightId?: number
}>(), {
  depth: 0,
  currentUserId: 0,
  highlightId: 0,
})

defineEmits<{
  like: [comment: CommentItem]
  reply: [comment: CommentItem]
  delete: [comment: CommentItem]
}>()

function canDelete(comment: CommentItem): boolean {
  const userStore = useUserStore()
  return userStore.userInfo?.id === comment.user_id || userStore.userInfo?.role === 1
}
</script>

<style scoped>
.comment-list {
  margin-top: 12px;
}

.empty {
  text-align: center;
  color: #909399;
  padding: 20px;
}

.comment-item {
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}
.comment-item.is-child {
  margin-left: 40px;
  border-bottom: none;
}
.comment-item.is-highlight {
  background: #ecf5ff;
  border-radius: 6px;
  padding: 12px;
  margin: 0 -12px;
  transition: background 0.3s;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.comment-user {
  display: flex;
  align-items: center;
  gap: 8px;
}

.comment-username {
  font-size: 13px;
  font-weight: 500;
  color: #303133;
  text-decoration: none;
}
.comment-username:hover {
  color: #409eff;
}

.time {
  font-size: 12px;
  color: #909399;
}

.comment-num {
  font-size: 12px;
  color: #c0c4cc;
}

.comment-content {
  font-size: 14px;
  color: #303133;
  line-height: 1.6;
  margin-bottom: 8px;
}

.comment-actions {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: #909399;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 2px;
  cursor: pointer;
}
.action-btn:hover,
.action-btn.active {
  color: #409eff;
}
.action-btn.delete:hover {
  color: #f56c6c;
}
</style>
