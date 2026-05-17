import request from './request'

export interface PostItem {
  id: number
  title: string
  content: string
  content_raw?: string
  status: number
  view_count: number
  like_count: number
  favorite_count: number
  category_id: number
  user_id: number
  created_at: string
  updated_at: string
  user?: { id: number; username: string; avatar: string }
  category?: { id: number; name: string; slug: string }
  liked?: boolean
  favorited?: boolean
}

export interface PostListResult {
  list: PostItem[]
  total: number
  page: number
  page_size: number
}

export interface PostQuery {
  page?: number
  page_size?: number
  category_id?: number
  sort?: string
  keyword?: string
  user_id?: number
}

export const postAPI = {
  list(params?: PostQuery): Promise<PostListResult> {
    return request.get('/posts', { params })
  },

  getDetail(id: number): Promise<PostItem> {
    return request.get(`/post/${id}`)
  },

  create(data: { category_id: number; title: string; content: string }) {
    return request.post('/post', data)
  },

  update(id: number, data: { title?: string; content?: string }) {
    return request.put(`/post/${id}`, data)
  },

  delete(id: number) {
    return request.delete(`/post/${id}`)
  },

  search(params: { keyword: string; page?: number; page_size?: number }): Promise<PostListResult> {
    return request.get('/posts/search', { params })
  },
}
