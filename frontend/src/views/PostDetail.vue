<template>
  <div class="post-detail">
    <div v-if="loading" class="loading">
      <el-skeleton :rows="5" animated />
    </div>

    <template v-else-if="post">
      <div class="post-main">
        <div class="post-header">
          <div class="post-meta">
            <router-link :to="`/user/${post.user_id}`" class="author">
              <el-avatar :size="40" :src="post.user?.avatar">
                {{ post.user?.username?.charAt(0)?.toUpperCase() || 'U' }}
              </el-avatar>
              <span class="author-name">{{ post.user?.username }}</span>
            </router-link>
            <span class="post-time">{{ post.created_at }}</span>
            <el-tag v-if="post.category" size="small" type="info">{{ post.category.name }}</el-tag>
          </div>
          <h1 class="post-title">{{ post.title }}</h1>
        </div>

        <div class="post-body">
          <MarkdownViewer :content="post.content_raw || post.content" :is-raw="true" />
        </div>

        <div class="post-actions">
          <el-button
            :type="post.liked ? 'primary' : 'default'"
            @click="handleLike"
            :icon="CaretTop"
          >
            {{ post.like_count || 0 }} 赞
          </el-button>
          <el-button
            :type="post.favorited ? 'warning' : 'default'"
            @click="handleFavorite"
            :icon="Star"
          >
            {{ post.favorited ? '已收藏' : '收藏' }}
          </el-button>
          <span class="view-count">
            <el-icon><View /></el-icon> {{ post.view_count }}
          </span>

          <div class="post-owner-actions" v-if="canEdit">
            <el-button size="small" @click="$router.push(`/edit/${post.id}`)">编辑</el-button>
            <el-popconfirm title="确定删除该帖子?" @confirm="handleDelete">
              <template #reference>
                <el-button size="small" type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </div>
        </div>
      </div>

      <div class="comments-section" :class="{ 'has-reply-bar': userStore.isLoggedIn }">
        <h3>回复 ({{ commentCount }})</h3>

        <CommentList
          :comments="comments"
          :current-user-id="userStore.userInfo?.id || 0"
          :highlight-id="replyTarget?.id"
          @like="handleCommentLike"
          @reply="handleReply"
          @delete="handleCommentDelete"
        />
      </div>
    </template>

    <div v-else class="error">
      <el-empty description="帖子不存在" />
    </div>

    <!-- 贴吧风格底部固定回复栏 -->
    <div class="reply-bar" v-if="userStore.isLoggedIn && post">
      <div class="reply-bar-inner">
        <div class="reply-target" v-if="replyTarget">
          回复 <strong>@{{ replyTarget.user?.username }}</strong>
          <el-button size="small" text type="danger" @click="replyTarget = null; commentContent = ''">取消</el-button>
        </div>
        <div class="reply-bar-row">
          <EmojiPicker @select="e => commentContent += e" />
          <el-input
            ref="replyInputRef"
            v-model="commentContent"
            placeholder="写下你的回复..."
            maxlength="2000"
            @keyup.enter.ctrl="handleSubmitComment"
          >
            <template #append>
              <el-button :disabled="!commentContent.trim()" @click="handleSubmitComment">发表</el-button>
            </template>
          </el-input>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { CaretTop, Star, View } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { postAPI } from '@/api/post'
import { commentAPI } from '@/api/comment'
import type { PostItem } from '@/api/post'
import type { CommentItem } from '@/api/comment'
import { likeAPI, favoriteAPI } from '@/api/interaction'
import { useUserStore } from '@/stores/user'
import MarkdownViewer from '@/components/MarkdownViewer.vue'
import CommentList from '@/components/CommentList.vue'
import EmojiPicker from '@/components/EmojiPicker.vue'
import { useWebSocket } from '@/composables/useWebSocket'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const { connect, subscribe } = useWebSocket()

const post = ref<PostItem | null>(null)
const comments = ref<CommentItem[]>([])
const loading = ref(true)
const commentContent = ref('')
const replyTarget = ref<CommentItem | null>(null)
const replyInputRef = ref<any>(null)

const commentCount = computed(() => {
  function count(arr: CommentItem[]): number {
    let n = arr.length
    for (const c of arr) {
      if (c.children) n += count(c.children)
    }
    return n
  }
  return count(comments.value)
})

const canEdit = computed(() => {
  if (!post.value || !userStore.userInfo) return false
  return userStore.userInfo.id === post.value.user_id || userStore.userInfo.role === 1
})

let pollTimer: ReturnType<typeof setInterval> | null = null

onMounted(() => {
  loadPost()
  loadComments()
  connectWebSocket()
})

onUnmounted(() => { /* cleanup handled by composable */ })

function connectWebSocket() {
  connect()
  const postId = route.params.id as string
  subscribe('/topic/post/' + postId, (event: any) => {
    if (event.type === 'new_comment') {
      loadComments()
    } else if (event.type === 'like_changed') {
      // Update like counts in-place for comments and post
      const p = event.payload
      if (p.target_type === 1 && post.value && post.value.id === p.target_id) {
        post.value.like_count = p.count
        if (userStore.userInfo?.id) post.value.liked = p.liked
      }
      // Update comment likes by reload (simpler, not too expensive)
      if (p.target_type === 2) loadComments()
    }
  })
}

async function loadPost() {
  try {
    const id = Number(route.params.id)
    post.value = await postAPI.getDetail(id)
  } catch {
    post.value = null
  } finally {
    loading.value = false
  }
}

async function loadComments() {
  try {
    const id = Number(route.params.id)
    comments.value = await commentAPI.list(id)
  } catch {
    comments.value = []
  }
}

async function handleLike() {
  if (!userStore.isLoggedIn) { ElMessage.warning('请先登录'); return }
  if (!post.value) return
  try {
    const result = await likeAPI.toggle(1, post.value.id)
    post.value.liked = result.liked
    post.value.like_count = (post.value.like_count || 0) + (result.liked ? 1 : -1)
  } catch {}
}

async function handleFavorite() {
  if (!userStore.isLoggedIn) { ElMessage.warning('请先登录'); return }
  if (!post.value) return
  try {
    const result = await favoriteAPI.toggle(post.value.id)
    post.value.favorited = result.favorited
    ElMessage.success(result.favorited ? '已收藏' : '已取消收藏')
  } catch {}
}

async function handleDelete() {
  if (!post.value) return
  try {
    await postAPI.delete(post.value.id)
    ElMessage.success('删除成功')
    router.push('/')
  } catch {}
}

async function handleSubmitComment() {
  if (!commentContent.value.trim()) return
  const postId = Number(route.params.id)
  try {
    await commentAPI.create({
      post_id: postId,
      parent_id: replyTarget.value?.parent_id === 0 ? replyTarget.value?.id : replyTarget.value?.parent_id || 0,
      reply_to: replyTarget.value?.user_id || 0,
      content: commentContent.value.trim(),
    })
    ElMessage.success('回复成功')
    commentContent.value = ''
    replyTarget.value = null
    loadComments()
  } catch {}
}

function handleReply(comment: CommentItem) {
  replyTarget.value = comment
  commentContent.value = ''
  // scroll the target comment into view
  nextTick(() => {
    const el = document.getElementById('comment-' + comment.id)
    el?.scrollIntoView({ behavior: 'smooth', block: 'center' })
    replyInputRef.value?.focus()
  })
}

async function handleCommentLike(comment: CommentItem) {
  if (!userStore.isLoggedIn) { ElMessage.warning('请先登录'); return }
  try {
    const result = await likeAPI.toggle(2, comment.id)
    comment.liked = result.liked
    comment.like_count += result.liked ? 1 : -1
  } catch {}
}

async function handleCommentDelete(comment: CommentItem) {
  try {
    await commentAPI.delete(comment.id)
    ElMessage.success('删除成功')
    loadComments()
  } catch {}
}
</script>

<style scoped>
.post-detail {
  max-width: 800px;
  margin: 0 auto;
}

.post-main {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  margin-bottom: 20px;
}

.post-header {
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f0f0f0;
}

.post-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 12px;
}

.author {
  display: flex;
  align-items: center;
  gap: 8px;
  text-decoration: none;
  color: #303133;
}

.author-name {
  font-weight: 500;
  font-size: 15px;
}

.post-time {
  font-size: 13px;
  color: #909399;
}

.post-title {
  font-size: 24px;
  color: #303133;
}

.post-body {
  min-height: 200px;
  margin-bottom: 20px;
}

.post-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

.view-count {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  color: #909399;
  margin-left: auto;
}

.post-owner-actions {
  margin-left: 12px;
}

.comments-section {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
}
.comments-section.has-reply-bar {
  padding-bottom: 80px;
}

.comments-section h3 {
  margin-bottom: 16px;
  color: #303133;
}

/* 底部固定回复栏 */
.reply-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #fff;
  border-top: 1px solid #e4e7ed;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.06);
  z-index: 50;
  padding: 10px 0;
}

.reply-bar-inner {
  max-width: 800px;
  margin: 0 auto;
  padding: 0 20px;
}

.reply-target {
  font-size: 13px;
  color: #606266;
  margin-bottom: 6px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.reply-bar-row {
  display: flex;
  gap: 0;
}

.loading,
.error {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
}
</style>
