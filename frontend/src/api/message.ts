import request from './request'

export interface MessageItem {
  id: number
  from_id: number
  to_id: number
  content: string
  is_read: number
  created_at: string
  from_user?: { id: number; username: string; avatar: string }
}

export interface ConversationItem {
  user: { id: number; username: string; avatar: string }
  last_content: string
  last_time: string
  unread: number
}

export const messageAPI = {
  send(toId: number, content: string) {
    return request.post('/message', { to_id: toId, content })
  },

  conversation(withUserId: number, page = 1, pageSize = 50) {
    return request.get('/messages', { params: { with_user_id: withUserId, page, page_size: pageSize } })
  },

  conversations() {
    return request.get('/messages/conversations')
  },

  unreadCount(): Promise<{ count: number }> {
    return request.get('/messages/unread')
  },
}
