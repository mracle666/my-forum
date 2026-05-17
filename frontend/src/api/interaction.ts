import request from './request'

export const likeAPI = {
  toggle(targetType: number, targetId: number): Promise<{ liked: boolean }> {
    return request.post('/like', { target_type: targetType, target_id: targetId })
  },
}

export const favoriteAPI = {
  toggle(postId: number): Promise<{ favorited: boolean }> {
    return request.post('/favorite', { post_id: postId })
  },

  list(page = 1, pageSize = 20) {
    return request.get('/favorites', { params: { page, page_size: pageSize } })
  },
}
