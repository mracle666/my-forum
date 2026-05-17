<template>
  <div class="post-create">
    <div class="create-card">
      <h2>{{ isEdit ? '编辑帖子' : '发布帖子' }}</h2>
      <el-form :model="form" label-position="top">
        <el-form-item label="板块">
          <el-select v-model="form.category_id" placeholder="选择板块" style="width: 240px">
            <el-option
              v-for="cat in categories"
              :key="(cat as any).id"
              :label="(cat as any).name"
              :value="(cat as any).id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="标题">
          <el-input v-model="form.title" placeholder="输入帖子标题" maxlength="256" show-word-limit />
        </el-form-item>
        <el-form-item label="内容">
          <MarkdownEditor v-model="form.content" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">
            {{ isEdit ? '保存修改' : '发布帖子' }}
          </el-button>
          <el-button @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { postAPI } from '@/api/post'
import MarkdownEditor from '@/components/MarkdownEditor.vue'
import request from '@/api/request'

const route = useRoute()
const router = useRouter()

const isEdit = computed(() => !!route.params.id)

const form = ref({
  category_id: 1,
  title: '',
  content: '',
})

const categories = ref<any[]>([])
const submitting = ref(false)

onMounted(async () => {
  try {
    const data = await request.get('/categories') as any[]
    categories.value = data
  } catch {}

  if (isEdit.value) {
    await loadPost()
  }
})

async function loadPost() {
  try {
    const id = Number(route.params.id)
    const post = await postAPI.getDetail(id) as any
    form.value = {
      category_id: post.category_id,
      title: post.title,
      content: post.content_raw || post.content,
    }
  } catch {
    ElMessage.error('帖子不存在')
    router.push('/')
  }
}

async function handleSubmit() {
  if (!form.value.title.trim()) {
    ElMessage.warning('请输入标题')
    return
  }
  if (!form.value.content.trim()) {
    ElMessage.warning('请输入内容')
    return
  }

  submitting.value = true
  try {
    if (isEdit.value) {
      await postAPI.update(Number(route.params.id), {
        title: form.value.title,
        content: form.value.content,
      })
      ElMessage.success('修改成功')
      router.push(`/post/${route.params.id}`)
    } else {
      const result = await postAPI.create(form.value) as any
      ElMessage.success('发布成功')
      router.push(`/post/${result.id}`)
    }
  } catch {} finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.post-create {
  max-width: 800px;
  margin: 0 auto;
}

.create-card {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
}

.create-card h2 {
  margin-bottom: 24px;
  color: #303133;
}
</style>
