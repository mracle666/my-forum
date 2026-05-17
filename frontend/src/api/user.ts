import request from './request'

export interface UserInfo {
  id: number
  username: string
  email: string
  avatar: string
  bio: string
  role: number
  status: number
  created_at: string
}

export interface LoginParams {
  username: string
  password: string
}

export interface RegisterParams {
  username: string
  email: string
  password: string
}

export interface LoginResult {
  access_token: string
  refresh_token: string
  user: UserInfo
}

export const userAPI = {
  register(params: RegisterParams) {
    return request.post('/user/register', params)
  },

  login(params: LoginParams): Promise<LoginResult> {
    return request.post('/user/login', params)
  },

  getProfile(): Promise<UserInfo> {
    return request.get('/user/profile')
  },

  updateProfile(fields: Record<string, any>) {
    return request.put('/user/profile', fields)
  },

  changePassword(oldPassword: string, newPassword: string) {
    return request.put('/user/password', { old_password: oldPassword, new_password: newPassword })
  },

  uploadAvatar(file: FormData) {
    return request.post('/user/avatar', file, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
  },

  getPublicProfile(id: number) {
    return request.get(`/user/${id}`)
  },
}
