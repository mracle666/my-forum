import request from './request'

export interface CommentItem {
  id: number
  post_id: number
  user_id: number
  parent_id: number
  reply_to: number
  content: string
  status: number
  created_at: string
  user?: { id: number; username: string; avatar: string }
  children?: CommentItem[]
  like_count: number
  liked: boolean
}

export const commentAPI = {
  list(postId: number): Promise<CommentItem[]> {
    return request.get('/comments', { params: { post_id: postId } })
  },

  create(data: { post_id: number; parent_id?: number; reply_to?: number; content: string }) {
    return request.post('/comment', data)
  },

  delete(id: number) {
    return request.delete(`/comment/${id}`)
  },
}
