<template>
  <div class="my-posts-page">
    <h2>我的帖子</h2>
    <div v-if="loading" class="loading">
      <el-skeleton :rows="3" animated />
    </div>
    <template v-else>
      <PostCard v-for="post in posts" :key="post.id" :post="post" />
      <div v-if="posts.length === 0" class="empty">暂无帖子，<router-link to="/create">去发布</router-link></div>
    </template>
    <Pagination
      :total="total"
      :page="page"
      :page-size="pageSize"
      @change="handlePageChange"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { postAPI } from '@/api/post'
import type { PostItem } from '@/api/post'
import { useUserStore } from '@/stores/user'
import PostCard from '@/components/PostCard.vue'
import Pagination from '@/components/Pagination.vue'

const userStore = useUserStore()
const posts = ref<PostItem[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(20)
const loading = ref(false)

async function loadPosts() {
  if (!userStore.userInfo) return
  loading.value = true
  try {
    const data = await postAPI.list({
      page: page.value,
      page_size: pageSize.value,
      user_id: userStore.userInfo.id,
    })
    posts.value = data.list
    total.value = data.total
  } catch {
    posts.value = []
  } finally {
    loading.value = false
  }
}

function handlePageChange(p: number, ps: number) {
  page.value = p
  pageSize.value = ps
  loadPosts()
}

onMounted(loadPosts)
</script>

<style scoped>
.my-posts-page {
  max-width: 800px;
  margin: 0 auto;
}

.my-posts-page h2 {
  margin-bottom: 20px;
  color: #303133;
}

.loading {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
}

.empty {
  text-align: center;
  padding: 60px 20px;
  color: #909399;
  background: #fff;
  border-radius: 8px;
}
.empty a {
  color: #409eff;
}
</style>
