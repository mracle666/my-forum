<template>
  <header class="app-header">
    <div class="header-inner">
      <router-link to="/" class="logo">MyForum</router-link>
      <div class="header-nav">
        <router-link to="/" class="nav-item">首页</router-link>
        <router-link v-if="userStore.isLoggedIn" to="/my-posts" class="nav-item">我的帖子</router-link>
        <router-link v-if="userStore.isLoggedIn" to="/my-favorites" class="nav-item">我的收藏</router-link>
        <template v-if="userStore.isLoggedIn">
          <router-link to="/notifications" class="notification-bell">
            <el-badge :value="notifUnread" :hidden="notifUnread === 0" :max="99">
              <el-icon :size="20"><Bell /></el-icon>
            </el-badge>
          </router-link>
          <el-dropdown trigger="click">
            <span class="user-menu">
              <el-avatar :size="32" :src="userStore.userInfo?.avatar">
                {{ userStore.userInfo?.username?.charAt(0)?.toUpperCase() || 'U' }}
              </el-avatar>
              <span class="username">{{ userStore.userInfo?.username }}</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="$router.push(`/user/${userStore.userInfo?.id}`)">
                  个人主页
                </el-dropdown-item>
                <el-dropdown-item @click="$router.push('/settings')">
                  个人设置
                </el-dropdown-item>
                <el-dropdown-item @click="$router.push('/messages')">
                  私信
                  <el-badge v-if="msgUnread > 0" :value="msgUnread" class="dropdown-badge" />
                </el-dropdown-item>
                <el-dropdown-item v-if="userStore.userInfo?.role === 1" @click="$router.push('/admin')">
                  管理后台
                </el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else>
          <router-link to="/login" class="nav-item">登录</router-link>
          <router-link to="/register">
            <el-button type="primary" size="small">注册</el-button>
          </router-link>
        </template>
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Bell } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { useAppStore } from '@/stores/app'
import { messageAPI } from '@/api/message'
import { notificationAPI } from '@/api/notification'
import { useWebSocket } from '@/composables/useWebSocket'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const appStore = useAppStore()
const { connect, subscribe } = useWebSocket()

const notifUnread = computed(() => appStore.notificationUnread)
const msgUnread = ref(0)

async function fetchCounts() {
  if (!userStore.isLoggedIn) return
  try {
    const [msg, notif] = await Promise.all([
      messageAPI.unreadCount(),
      notificationAPI.unreadCount(),
    ])
    msgUnread.value = msg.count
    appStore.setNotificationUnread(notif.count)
  } catch {}
}

function connectWS() {
  if (!userStore.userInfo) return
  connect()
  subscribe('/topic/user/' + userStore.userInfo.id, () => {
    fetchCounts()
  })
  subscribe('/topic/message/' + userStore.userInfo.id, () => {
    fetchCounts()
  })
}

onMounted(() => {
  fetchCounts()
  if (userStore.isLoggedIn) connectWS()
})

function handleLogout() {
  userStore.logout()
  ElMessage.success('已退出登录')
  router.push('/')
}
</script>

<style scoped>
.app-header {
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-inner {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  height: 60px;
}

.logo {
  font-size: 22px;
  font-weight: 700;
  color: #409eff;
  text-decoration: none;
}

.header-nav {
  display: flex;
  align-items: center;
  gap: 20px;
}

.nav-item {
  color: #606266;
  text-decoration: none;
  font-size: 14px;
}
.nav-item:hover,
.nav-item.router-link-active {
  color: #409eff;
}

.user-menu {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}
.username {
  font-size: 14px;
  color: #303133;
}

.notification-bell {
  cursor: pointer;
  padding: 4px;
  display: flex;
  align-items: center;
  color: #606266;
  text-decoration: none;
}
.notification-bell:hover { color: #409eff; }

.dropdown-badge {
  margin-left: 8px;
}
</style>
