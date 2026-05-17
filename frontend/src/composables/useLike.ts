import { ref } from 'vue'
import { likeAPI } from '@/api/interaction'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

export function useLike() {
  const userStore = useUserStore()

  async function toggleLike(targetType: number, targetId: number, state: { liked: boolean; like_count: number }) {
    if (!userStore.isLoggedIn) {
      ElMessage.warning('请先登录')
      return
    }
    try {
      const result = await likeAPI.toggle(targetType, targetId)
      state.liked = result.liked
      state.like_count += result.liked ? 1 : -1
    } catch {}
  }

  return { toggleLike }
}
