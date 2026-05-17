<template>
  <div class="my-fav-page">
    <h2>我的收藏</h2>
    <div v-if="loading" class="loading">
      <el-skeleton :rows="3" animated />
    </div>
    <template v-else>
      <PostCard v-for="post in posts" :key="post.id" :post="post" />
      <div v-if="posts.length === 0" class="empty">
        <el-empty description="暂无收藏，去首页逛逛吧">
          <el-button type="primary" @click="$router.push('/')">去首页</el-button>
        </el-empty>
      </div>
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
import { favoriteAPI } from '@/api/interaction'
import type { PostItem } from '@/api/post'
import PostCard from '@/components/PostCard.vue'
import Pagination from '@/components/Pagination.vue'

const posts = ref<PostItem[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(20)
const loading = ref(false)

async function loadFavorites() {
  loading.value = true
  try {
    const data = await favoriteAPI.list(page.value, pageSize.value) as any
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
  loadFavorites()
}

onMounted(loadFavorites)
</script>

<style scoped>
.my-fav-page {
  max-width: 800px;
  margin: 0 auto;
}
.my-fav-page h2 {
  margin-bottom: 20px;
  color: #303133;
}
.loading {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
}
.empty {
  background: #fff;
  border-radius: 8px;
  padding: 40px;
}
</style>
