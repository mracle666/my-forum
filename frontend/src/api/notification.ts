import request from './request'

export interface NotificationItem {
  id: number
  user_id: number
  from_id: number
  type: number
  content: string
  target_id: number
  is_read: number
  created_at: string
  from_user?: { id: number; username: string; avatar: string }
}

export interface NotificationListResult {
  list: NotificationItem[]
  total: number
  unread_count: number
  page: number
  page_size: number
}

export const notificationAPI = {
  list(page = 1, pageSize = 20): Promise<NotificationListResult> {
    return request.get('/notifications', { params: { page, page_size: pageSize } })
  },

  markRead(id: number) {
    return request.put(`/notification/${id}/read`)
  },

  markAllRead() {
    return request.put('/notifications/read-all')
  },

  unreadCount(): Promise<{ count: number }> {
    return request.get('/notifications/unread')
  },
}
