<template>
  <div class="admin-page">
    <div class="admin-card">
      <h2>管理后台</h2>

      <el-tabs v-model="activeTab" @tab-change="onTabChange">
        <!-- ========== 用户管理 ========== -->
        <el-tab-pane label="用户管理" name="users">
          <el-table :data="users" stripe>
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="username" label="用户名" />
            <el-table-column prop="email" label="邮箱" />
            <el-table-column label="角色" width="100">
              <template #default="{ row }">
                <el-tag :type="row.role === 1 ? 'danger' : 'info'" size="small">
                  {{ row.role === 1 ? '管理员' : '用户' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
                  {{ row.status === 1 ? '正常' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="created_at" label="注册时间" width="180" />
            <el-table-column label="操作" width="120">
              <template #default="{ row }">
                <el-button
                  :type="row.status === 1 ? 'danger' : 'success'"
                  size="small"
                  @click="handleToggleStatus(row)"
                >
                  {{ row.status === 1 ? '禁用' : '启用' }}
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination-wrap">
            <el-pagination
              v-model:current-page="userPage"
              :total="userTotal"
              :page-size="20"
              layout="prev, pager, next"
              background
              @current-change="loadUsers"
            />
          </div>
        </el-tab-pane>

        <!-- ========== 帖子管理 ========== -->
        <el-tab-pane label="帖子管理" name="posts">
          <div class="tab-toolbar">
            <el-input
              v-model="postKeyword"
              placeholder="搜索帖子标题..."
              clearable
              style="width: 260px"
              @keyup.enter="loadPosts"
            />
            <el-select v-model="postStatus" placeholder="状态筛选" clearable style="width: 140px" @change="loadPosts">
              <el-option label="全部" :value="undefined" />
              <el-option label="正常" :value="1" />
              <el-option label="置顶" :value="2" />
              <el-option label="已删除" :value="0" />
            </el-select>
            <el-button @click="loadPosts">搜索</el-button>
          </div>

          <el-table :data="posts" stripe>
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
            <el-table-column prop="username" label="作者" width="120" />
            <el-table-column prop="category_name" label="板块" width="100" />
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag
                  :type="row.status === 2 ? 'warning' : row.status === 0 ? 'danger' : 'success'"
                  size="small"
                >
                  {{ row.status === 2 ? '置顶' : row.status === 0 ? '已删' : '正常' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="view_count" label="浏览" width="80" />
            <el-table-column prop="like_count" label="点赞" width="80" />
            <el-table-column prop="created_at" label="发布时间" width="180" />
            <el-table-column label="操作" width="180" fixed="right">
              <template #default="{ row }">
                <el-button size="small" @click="handleViewPost(row)">查看</el-button>
                <el-button
                  v-if="row.status !== 0"
                  size="small"
                  :type="row.status === 2 ? 'default' : 'warning'"
                  @click="handleTogglePin(row)"
                >
                  {{ row.status === 2 ? '取消置顶' : '置顶' }}
                </el-button>
                <el-popconfirm
                  v-if="row.status !== 0"
                  title="确定删除该帖子?"
                  @confirm="handleDeletePost(row)"
                >
                  <template #reference>
                    <el-button size="small" type="danger">删除</el-button>
                  </template>
                </el-popconfirm>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination-wrap">
            <el-pagination
              v-model:current-page="postPage"
              :total="postTotal"
              :page-size="20"
              layout="prev, pager, next"
              background
              @current-change="loadPosts"
            />
          </div>
        </el-tab-pane>

        <!-- ========== 板块管理 ========== -->
        <el-tab-pane label="板块管理" name="categories">
          <div class="tab-toolbar">
            <el-button type="primary" @click="handleAddCategory">新增板块</el-button>
          </div>

          <el-table :data="categories" stripe>
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="name" label="名称" width="150" />
            <el-table-column prop="slug" label="标识" width="150" />
            <el-table-column prop="description" label="描述" min-width="200" />
            <el-table-column prop="sort" label="排序" width="80" />
            <el-table-column label="操作" width="160">
              <template #default="{ row }">
                <el-button size="small" @click="handleEditCategory(row)">编辑</el-button>
                <el-popconfirm title="确定删除该板块? (需板块下无帖子)" @confirm="handleDeleteCategory(row)">
                  <template #reference>
                    <el-button size="small" type="danger">删除</el-button>
                  </template>
                </el-popconfirm>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 板块编辑弹窗 -->
    <el-dialog v-model="categoryDialogVisible" :title="editingCategory ? '编辑板块' : '新增板块'" width="500px">
      <el-form :model="categoryForm" label-position="top">
        <el-form-item label="名称">
          <el-input v-model="categoryForm.name" placeholder="板块名称" />
        </el-form-item>
        <el-form-item label="标识">
          <el-input v-model="categoryForm.slug" placeholder="英文标识，如 tech" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="categoryForm.description" placeholder="板块描述" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="categoryForm.sort" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="categoryDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveCategory">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/api/request'

const router = useRouter()
const activeTab = ref('users')

// ========== 用户管理 ==========
const users = ref<any[]>([])
const userPage = ref(1)
const userTotal = ref(0)

async function loadUsers() {
  try {
    const data = await request.get('/admin/users', {
      params: { page: userPage.value, page_size: 20 },
    }) as any
    users.value = data.list
    userTotal.value = data.total
  } catch {}
}

async function handleToggleStatus(row: any) {
  const newStatus = row.status === 1 ? 0 : 1
  try {
    await request.put(`/admin/user/${row.id}/status`, { status: newStatus })
    row.status = newStatus
    ElMessage.success(newStatus === 1 ? '已启用' : '已禁用')
  } catch {}
}

// ========== 帖子管理 ==========
const posts = ref<any[]>([])
const postPage = ref(1)
const postTotal = ref(0)
const postKeyword = ref('')
const postStatus = ref<number | undefined>(undefined)

async function loadPosts() {
  try {
    const data = await request.get('/admin/posts', {
      params: {
        page: postPage.value,
        page_size: 20,
        keyword: postKeyword.value || undefined,
        status: postStatus.value,
      },
    }) as any
    posts.value = data.list
    postTotal.value = data.total
  } catch {}
}

function handleViewPost(row: any) {
  router.push(`/post/${row.id}`)
}

async function handleTogglePin(row: any) {
  try {
    await request.put(`/admin/post/${row.id}/pin`)
    row.status = row.status === 2 ? 1 : 2
    ElMessage.success(row.status === 2 ? '已置顶' : '已取消置顶')
  } catch {}
}

async function handleDeletePost(row: any) {
  try {
    await request.delete(`/admin/post/${row.id}`)
    row.status = 0
    ElMessage.success('已删除')
  } catch {}
}

// ========== 板块管理 ==========
const categories = ref<any[]>([])
const categoryDialogVisible = ref(false)
const editingCategory = ref<any>(null)
const categoryForm = ref({ name: '', slug: '', description: '', sort: 0 })

async function loadCategories() {
  try {
    const data = await request.get('/admin/categories') as any
    categories.value = data
  } catch {}
}

function handleAddCategory() {
  editingCategory.value = null
  categoryForm.value = { name: '', slug: '', description: '', sort: 0 }
  categoryDialogVisible.value = true
}

function handleEditCategory(row: any) {
  editingCategory.value = row
  categoryForm.value = {
    name: row.name,
    slug: row.slug,
    description: row.description || '',
    sort: row.sort || 0,
  }
  categoryDialogVisible.value = true
}

async function handleSaveCategory() {
  try {
    if (editingCategory.value) {
      await request.put(`/admin/category/${editingCategory.value.id}`, categoryForm.value)
      ElMessage.success('板块已更新')
    } else {
      await request.post('/admin/category', categoryForm.value)
      ElMessage.success('板块已创建')
    }
    categoryDialogVisible.value = false
    loadCategories()
  } catch {}
}

async function handleDeleteCategory(row: any) {
  try {
    await request.delete(`/admin/category/${row.id}`)
    ElMessage.success('板块已删除')
    loadCategories()
  } catch {}
}

// ========== 初始化 ==========
const loadedTabs = ref(new Set<string>())

function onTabChange(tab: string) {
  if (loadedTabs.value.has(tab)) return
  loadedTabs.value.add(tab)
  if (tab === 'users') loadUsers()
  else if (tab === 'posts') loadPosts()
  else if (tab === 'categories') loadCategories()
}

onMounted(() => {
  loadUsers()
  loadedTabs.value.add('users')
})
</script>

<style scoped>
.admin-page {
  max-width: 1100px;
  margin: 0 auto;
}

.admin-card {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
}

.admin-card h2 {
  margin-bottom: 24px;
  color: #303133;
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.tab-toolbar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  align-items: center;
}
</style>
