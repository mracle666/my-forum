import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

export function useAuth() {
  const router = useRouter()
  const userStore = useUserStore()

  const isLoggedIn = computed(() => userStore.isLoggedIn)
  const userInfo = computed(() => userStore.userInfo)
  const isAdmin = computed(() => userStore.userInfo?.role === 1)

  function requireAuth() {
    if (!isLoggedIn.value) {
      router.push({ name: 'Login', query: { redirect: router.currentRoute.value.fullPath } })
      return false
    }
    return true
  }

  function requireAdmin() {
    if (!requireAuth()) return false
    if (!isAdmin.value) {
      router.push({ name: 'Home' })
      return false
    }
    return true
  }

  function logout() {
    userStore.logout()
    router.push('/')
  }

  return { isLoggedIn, userInfo, isAdmin, requireAuth, requireAdmin, logout }
}
