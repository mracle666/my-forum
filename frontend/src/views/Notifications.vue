<template>
  <div class="notif-page">
    <div class="notif-card">
      <div class="notif-header">
        <h2>通知</h2>
        <el-button v-if="notifications.length" size="small" text @click="markAllRead">全部已读</el-button>
      </div>

      <div v-if="notifications.length === 0" class="empty">
        <el-empty description="暂无通知" />
      </div>

      <div
        v-for="n in notifications"
        :key="n.id"
        class="notif-item"
        :class="{ unread: n.is_read === 0 }"
        @click="handleClick(n)"
      >
        <el-avatar :size="36" :src="n.from_user?.avatar">
          {{ n.from_user?.username?.charAt(0)?.toUpperCase() || 'S' }}
        </el-avatar>
        <div class="notif-body">
          <p class="notif-text">{{ n.content }}</p>
          <span class="notif-time">{{ n.created_at }}</span>
        </div>
        <div class="notif-dot" v-if="n.is_read === 0"></div>
      </div>

      <Pagination
        :total="total"
        :page="page"
        :page-size="pageSize"
        @change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { notificationAPI } from '@/api/notification'
import type { NotificationItem } from '@/api/notification'
import { useAppStore } from '@/stores/app'
import Pagination from '@/components/Pagination.vue'

const router = useRouter()
const appStore = useAppStore()
const notifications = ref<NotificationItem[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(20)

function handleClick(n: NotificationItem) {
  // mark as read
  if (n.is_read === 0) {
    notificationAPI.markRead(n.id)
    n.is_read = 1
    appStore.decrementNotificationUnread()
  }
  // navigate to target
  if (n.type === 4) {
    router.push('/messages')
  } else if (n.target_id) {
    router.push(`/post/${n.target_id}`)
  }
}

async function loadNotifications() {
  try {
    const data = await notificationAPI.list(page.value, pageSize.value)
    notifications.value = data.list
    total.value = data.total
  } catch {}
}

async function markAllRead() {
  try {
    await notificationAPI.markAllRead()
    notifications.value.forEach(n => n.is_read = 1)
    appStore.clearNotificationUnread()
    ElMessage.success('已全部标为已读')
  } catch {}
}

function handlePageChange(p: number, ps: number) {
  page.value = p
  pageSize.value = ps
  loadNotifications()
}

onMounted(loadNotifications)
</script>

<style scoped>
.notif-page {
  max-width: 700px;
  margin: 0 auto;
}

.notif-card {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
}

.notif-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.notif-header h2 {
  color: #303133;
}

.notif-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 12px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  border-radius: 4px;
  transition: background 0.2s;
}
.notif-item:hover { background: #f5f7fa; }
.notif-item:last-child { border-bottom: none; }
.notif-item.unread { background: #ecf5ff; }
.notif-item.unread:hover { background: #d9ecff; }

.notif-body {
  flex: 1;
  min-width: 0;
}
.notif-text {
  font-size: 14px;
  color: #303133;
  margin-bottom: 4px;
}
.notif-time {
  font-size: 12px;
  color: #909399;
}

.notif-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #409eff;
  flex-shrink: 0;
}

.empty {
  padding: 40px;
}
</style>
