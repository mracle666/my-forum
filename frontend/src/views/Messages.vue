<template>
  <div class="messages-page">
    <div class="messages-card">
      <h2>私信</h2>

      <div v-if="conversations.length === 0" class="empty">
        <el-empty description="暂无私信" />
      </div>

      <div
        v-for="conv in conversations"
        :key="conv.user.id"
        class="conv-item"
        @click="openConv(conv)"
      >
        <el-badge :value="conv.unread" :hidden="conv.unread === 0">
          <el-avatar :size="44" :src="conv.user.avatar">
            {{ conv.user.username?.charAt(0)?.toUpperCase() || 'U' }}
          </el-avatar>
        </el-badge>
        <div class="conv-info">
          <div class="conv-top">
            <span class="conv-name">{{ conv.user.username }}</span>
            <span class="conv-time">{{ conv.last_time }}</span>
          </div>
          <p class="conv-preview">{{ conv.last_content }}</p>
        </div>
      </div>
    </div>

    <!-- 对话弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="'与 ' + (activeConv?.user.username || '') + ' 的私信'"
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
          <el-input v-model="msgContent" placeholder="输入私信..." maxlength="500" @keyup.enter.ctrl="send" />
          <el-button type="primary" :disabled="!msgContent.trim()" @click="send">发送</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { messageAPI } from '@/api/message'
import type { ConversationItem, MessageItem } from '@/api/message'
import { useUserStore } from '@/stores/user'
import EmojiPicker from '@/components/EmojiPicker.vue'

const userStore = useUserStore()
const conversations = ref<ConversationItem[]>([])
const dialogVisible = ref(false)
const activeConv = ref<ConversationItem | null>(null)
const messages = ref<MessageItem[]>([])
const msgContent = ref('')
const chatAreaRef = ref<HTMLElement | null>(null)

onMounted(loadConversations)

async function loadConversations() {
  try {
    const data = await messageAPI.conversations() as any
    conversations.value = data.list || []
  } catch {}
}

async function openConv(conv: ConversationItem) {
  activeConv.value = conv
  dialogVisible.value = true
  await loadMessages()
  await nextTick()
  scrollToBottom()
}

async function loadMessages() {
  if (!activeConv.value) return
  try {
    const data = await messageAPI.conversation(activeConv.value.user.id) as any
    messages.value = data.list || []
  } catch {}
}

function scrollToBottom() {
  if (chatAreaRef.value) {
    chatAreaRef.value.scrollTop = chatAreaRef.value.scrollHeight
  }
}

async function send() {
  if (!msgContent.value.trim() || !activeConv.value) return
  try {
    await messageAPI.send(activeConv.value.user.id, msgContent.value.trim())
    msgContent.value = ''
    await loadMessages()
    await nextTick()
    scrollToBottom()
    loadConversations() // refresh list
  } catch {}
}
</script>

<style scoped>
.messages-page {
  max-width: 700px;
  margin: 0 auto;
}

.messages-card {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
}

.messages-card h2 {
  margin-bottom: 20px;
  color: #303133;
}

.conv-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 0;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
}
.conv-item:last-child { border-bottom: none; }
.conv-item:hover { background: #f5f7fa; margin: 0 -16px; padding: 14px 16px; border-radius: 6px; }

.conv-info { flex: 1; min-width: 0; }
.conv-top { display: flex; justify-content: space-between; margin-bottom: 4px; }
.conv-name { font-size: 15px; font-weight: 500; color: #303133; }
.conv-time { font-size: 12px; color: #909399; }
.conv-preview { font-size: 13px; color: #909399; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

.empty { padding: 40px; }

.chat-area {
  max-height: 350px;
  overflow-y: auto;
  padding: 8px;
  background: #f5f7fa;
  border-radius: 4px;
  min-height: 200px;
}

.chat-empty { text-align: center; color: #909399; padding: 40px 0; }

.chat-msg { margin-bottom: 12px; }
.chat-msg.is-mine { display: flex; flex-direction: column; align-items: flex-end; }
.chat-msg.is-mine .chat-msg-content { background: #409eff; color: #fff; }

.chat-msg-meta { font-size: 12px; color: #909399; margin-bottom: 4px; display: flex; gap: 8px; }
.chat-msg-content {
  background: #fff;
  padding: 10px 14px;
  border-radius: 8px;
  font-size: 14px;
  line-height: 1.5;
  max-width: 80%;
  word-break: break-word;
}

.chat-input-row { display: flex; gap: 8px; width: 100%; }
</style>
