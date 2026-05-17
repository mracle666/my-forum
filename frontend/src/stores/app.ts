import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAppStore = defineStore('app', () => {
  const notificationUnread = ref(0)

  function setNotificationUnread(count: number) {
    notificationUnread.value = count
  }

  function decrementNotificationUnread(n: number = 1) {
    notificationUnread.value = Math.max(0, notificationUnread.value - n)
  }

  function clearNotificationUnread() {
    notificationUnread.value = 0
  }

  return { notificationUnread, setNotificationUnread, decrementNotificationUnread, clearNotificationUnread }
})
