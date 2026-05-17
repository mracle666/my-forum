<template>
  <div class="user-profile">
    <div class="profile-card" v-if="profile">
      <div class="profile-header">
        <el-avatar :size="80" :src="profile.avatar">
          {{ profile.username?.charAt(0)?.toUpperCase() || 'U' }}
        </el-avatar>
        <div class="profile-info">
          <h2>{{ profile.username }}</h2>
          <p class="bio">{{ profile.bio || '这个人很懒，什么都没写...' }}</p>
          <p class="joined">加入于 {{ profile.created_at }}</p>
        </div>
      </div>
      <div class="profile-stats">
        <div class="stat-item">
          <span class="stat-num">{{ profile.post_count || 0 }}</span>
          <span class="stat-label">帖子</span>
        </div>
        <div class="stat-item">
          <span class="stat-num">{{ profile.comment_count || 0 }}</span>
          <span class="stat-label">回复</span>
        </div>
        <div class="stat-item">
          <span class="stat-num">{{ profile.like_count || 0 }}</span>
          <span class="stat-label">获赞</span>
        </div>
        <div class="stat-item stat-action" v-if="canMessage" @click="openChat">
          <el-icon :size="22"><ChatDotSquare /></el-icon>
          <span class="stat-label">发私信</span>
        </div>
      </div>
    </div>

    <div class="posts-section" v-if="posts.length">
      <h3>TA 发布的帖子</h3>
      <div
        v-for="post in posts"
        :key="post.id"
        class="post-item"
        @click="$router.push(`/post/${post.id}`)"
      >
        <div class="post-left">
          <h4>{{ post.title }}</h4>
          <div class="post-meta">
            <el-tag v-if="post.category" size="small" type="info">{{ post.category.name }}</el-tag>
            <span>{{ post.created_at }}</span>
          </div>
        </div>
        <div class="post-right">
          <span class="mini-stat"><el-icon><View /></el-icon> {{ post.view_count || 0 }}</span>
          <span class="mini-stat"><el-icon><CaretTop /></el-icon> {{ post.like_count || 0 }}</span>
        </div>
      </div>
    </div>

    <!-- 私信弹窗 -->
    <el-dialog
      v-model="chatVisible"
      :title="'与 ' + (profile?.username || '') + ' 的私信'"
      width="500px"
      :close-on-click-modal="false"
    >
      <div class="chat-area" ref="chatAreaRef">
        <div v-if="messages.length === 0" class="chat-empty">暂无消息，发送第一条私信吧</div>
        <div
          v-for="msg in messages"
          :key="msg.id"
          class="chat-msg"
          :class="{ 'is-mine': msg.from_id === userStore.userInfo?.id }"
        >
          <div class="chat-msg-meta">
            <span class="chat-msg-user">{{ msg.from_user?.username }}</span>
            <span class="chat-msg-time">{{ msg.created_at }}</span>
          </div>
          <div class="chat-msg-content">{{ msg.content }}</div>
        </div>
      </div>
      <template #footer>
        <div class="chat-input-row">
          <EmojiPicker @select="e => msgContent += e" />
          <el-input
            v-model="msgContent"
            placeholder="输入私信内容..."
            maxlength="500"
            @keyup.enter.ctrl="sendMessage"
          />
          <el-button type="primary" :disabled="!msgContent.trim()" @click="sendMessage">发送</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { View, CaretTop, ChatDotSquare } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { userAPI } from '@/api/user'
import { messageAPI } from '@/api/message'
import type { MessageItem } from '@/api/message'
import { useUserStore } from '@/stores/user'
import EmojiPicker from '@/components/EmojiPicker.vue'

const route = useRoute()
const userStore = useUserStore()
const profile = ref<any>(null)
const posts = computed(() => profile.value?.posts || [])

const chatVisible = ref(false)
const messages = ref<MessageItem[]>([])
const msgContent = ref('')
const chatAreaRef = ref<HTMLElement | null>(null)

const canMessage = computed(() => {
  return userStore.isLoggedIn && userStore.userInfo?.id !== profile.value?.id
})

onMounted(async () => {
  try {
    const id = Number(route.params.id)
    profile.value = await userAPI.getPublicProfile(id)
  } catch {
    profile.value = null
  }
})

async function openChat() {
  chatVisible.value = true
  await loadMessages()
  await nextTick()
  scrollToBottom()
}

async function loadMessages() {
  try {
    const data = await messageAPI.conversation(profile.value.id) as any
    messages.value = data.list || []
  } catch {}
}

function scrollToBottom() {
  if (chatAreaRef.value) {
    chatAreaRef.value.scrollTop = chatAreaRef.value.scrollHeight
  }
}

async function sendMessage() {
  if (!msgContent.value.trim()) return
  try {
    await messageAPI.send(profile.value.id, msgContent.value.trim())
    msgContent.value = ''
    await loadMessages()
    await nextTick()
    scrollToBottom()
  } catch {}
}
</script>

<style scoped>
.user-profile {
  max-width: 700px;
  margin: 0 auto;
}

.profile-card {
  background: #fff;
  border-radius: 8px;
  padding: 32px;
  margin-bottom: 20px;
}

.profile-header {
  display: flex;
  align-items: center;
  gap: 24px;
  margin-bottom: 24px;
}

.profile-info h2 {
  font-size: 22px;
  color: #303133;
  margin-bottom: 8px;
}

.bio {
  color: #606266;
  font-size: 14px;
  margin-bottom: 8px;
}

.joined {
  color: #909399;
  font-size: 13px;
}

.profile-stats {
  display: flex;
  gap: 40px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}
.stat-num {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}
.stat-label {
  font-size: 13px;
  color: #909399;
}

.stat-action {
  cursor: pointer;
  color: #409eff;
  transition: opacity 0.2s;
}
.stat-action:hover {
  opacity: 0.75;
}

/* 帖子列表 */
.posts-section {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
}

.posts-section h3 {
  font-size: 16px;
  color: #303133;
  margin-bottom: 16px;
}

.post-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 0;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
}
.post-item:last-child {
  border-bottom: none;
}
.post-item:hover h4 {
  color: #409eff;
}

.post-left h4 {
  font-size: 15px;
  color: #303133;
  margin-bottom: 6px;
}

.post-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 12px;
  color: #909399;
}

.post-right {
  display: flex;
  gap: 16px;
  flex-shrink: 0;
}

.mini-stat {
  display: flex;
  align-items: center;
  gap: 2px;
  font-size: 13px;
  color: #909399;
}

/* 私信弹窗 */
.chat-area {
  max-height: 350px;
  overflow-y: auto;
  padding: 8px;
  background: #f5f7fa;
  border-radius: 4px;
  min-height: 200px;
}

.chat-empty {
  text-align: center;
  color: #909399;
  padding: 40px 0;
  font-size: 14px;
}

.chat-msg {
  margin-bottom: 12px;
  display: flex;
  flex-direction: column;
}
.chat-msg.is-mine {
  align-items: flex-end;
}
.chat-msg.is-mine .chat-msg-content {
  background: #409eff;
  color: #fff;
}

.chat-msg-meta {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
  display: flex;
  gap: 8px;
}

.chat-msg-content {
  background: #fff;
  padding: 10px 14px;
  border-radius: 8px;
  font-size: 14px;
  line-height: 1.5;
  max-width: 80%;
  word-break: break-word;
}

.chat-input-row {
  display: flex;
  gap: 8px;
  width: 100%;
}
</style>
