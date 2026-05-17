<template>
  <div class="home-page">
    <div class="home-layout">
      <aside class="sidebar">
        <div class="sidebar-card">
          <h3>板块</h3>
          <ul class="category-list">
            <li
              v-for="cat in categories"
              :key="cat.id"
              :class="{ active: currentCategory === (cat as any).id }"
              @click="currentCategory = (cat as any).id"
            >
              {{ (cat as any).name }}
            </li>
          </ul>
          <el-button v-if="currentCategory !== null" size="small" text @click="currentCategory = null">
            清除筛选
          </el-button>
        </div>
        <div class="sidebar-card">
          <h3>排序</h3>
          <el-radio-group v-model="sortBy" size="small">
            <el-radio-button value="latest">最新</el-radio-button>
            <el-radio-button value="hot">最热</el-radio-button>
          </el-radio-group>
        </div>
        <el-button type="primary" class="create-btn" @click="$router.push('/create')">
          <el-icon><Plus /></el-icon> 发布帖子
        </el-button>
      </aside>

      <div class="content-area">
        <div class="content-header">
          <el-input
            v-model="keyword"
            placeholder="搜索帖子..."
            clearable
            :prefix-icon="Search"
            @keyup.enter="doSearch"
            class="search-input"
          />
        </div>

        <div v-if="loading" class="loading">
          <el-skeleton :rows="3" animated />
        </div>

        <template v-else>
          <PostCard v-for="post in posts" :key="post.id" :post="post" />
          <div v-if="posts.length === 0" class="empty">暂无帖子</div>
        </template>

        <Pagination
          :total="total"
          :page="page"
          :page-size="pageSize"
          @change="handlePageChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { Search, Plus } from '@element-plus/icons-vue'
import { postAPI } from '@/api/post'
import type { PostItem } from '@/api/post'
import PostCard from '@/components/PostCard.vue'
import Pagination from '@/components/Pagination.vue'
import request from '@/api/request'

const categories = ref<any[]>([])
const currentCategory = ref<number | null>(null)
const sortBy = ref('latest')
const keyword = ref('')
const posts = ref<PostItem[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(20)
const loading = ref(false)

onMounted(() => {
  loadCategories()
  loadPosts()
})

async function loadCategories() {
  try {
    const data = await request.get('/categories') as any[]
    categories.value = data
  } catch {}
}

async function loadPosts() {
  loading.value = true
  try {
    const data = await postAPI.list({
      page: page.value,
      page_size: pageSize.value,
      category_id: currentCategory.value || undefined,
      sort: sortBy.value,
      keyword: keyword.value || undefined,
    })
    posts.value = data.list
    total.value = data.total
  } catch {
    posts.value = []
  } finally {
    loading.value = false
  }
}

function doSearch() {
  page.value = 1
  loadPosts()
}

function handlePageChange(p: number, ps: number) {
  page.value = p
  pageSize.value = ps
  loadPosts()
}

watch(currentCategory, () => {
  page.value = 1
  loadPosts()
})

watch(sortBy, () => {
  page.value = 1
  loadPosts()
})
</script>

<style scoped>
.home-page {
  padding: 0;
}

.home-layout {
  display: flex;
  gap: 20px;
}

.sidebar {
  width: 220px;
  flex-shrink: 0;
}

.sidebar-card {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
}

.sidebar-card h3 {
  font-size: 15px;
  margin-bottom: 12px;
  color: #303133;
}

.category-list {
  list-style: none;
}
.category-list li {
  padding: 8px 12px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  color: #606266;
  margin-bottom: 4px;
}
.category-list li:hover {
  background: #ecf5ff;
  color: #409eff;
}
.category-list li.active {
  background: #409eff;
  color: #fff;
}

.create-btn {
  width: 100%;
}

.content-area {
  flex: 1;
  min-width: 0;
}

.content-header {
  margin-bottom: 16px;
}

.search-input {
  max-width: 400px;
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
</style>
