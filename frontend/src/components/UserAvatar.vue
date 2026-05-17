<template>
  <el-avatar
    :size="size"
    :src="src"
    :style="{ cursor: clickable ? 'pointer' : 'default' }"
    @click="clickable && $router.push(`/user/${userId}`)"
  >
    {{ fallback }}
  </el-avatar>
</template>

<script setup lang="ts">
defineProps<{
  src?: string
  fallback?: string
  size?: number
  userId?: number
  clickable?: boolean
}>()
</script>
