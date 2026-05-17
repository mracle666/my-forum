<template>
  <div class="pagination" v-if="total > pageSize">
    <el-pagination
      v-model:current-page="currentPage"
      v-model:page-size="currentPageSize"
      :page-sizes="[10, 20, 30, 50]"
      :total="total"
      layout="total, sizes, prev, pager, next"
      background
      small
      @size-change="(size: number) => $emit('change', currentPage, size)"
      @current-change="(page: number) => $emit('change', page, currentPageSize)"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'

const props = defineProps<{
  total: number
  page: number
  pageSize: number
}>()

defineEmits<{
  change: [page: number, pageSize: number]
}>()

const currentPage = ref(props.page)
const currentPageSize = ref(props.pageSize)

watch(() => props.page, (v) => { currentPage.value = v })
watch(() => props.pageSize, (v) => { currentPageSize.value = v })
</script>

<style scoped>
.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>
