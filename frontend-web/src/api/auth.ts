import { authRequest } from '@/utils/request'

// 登录请求参数
export interface LoginParams {
  username: string
  password: string
}

// 登录响应
export interface LoginResponse {
  accessToken: string
  refreshToken: string
  user: {
    id: number
    username: string
    email?: string
    phone?: string
    userType: number // 1-求职者 2-HR 3-管理员
    status: number // 0-禁用 1-正常
    avatarUrl?: string
    createTime?: string
    lastLoginTime?: string
  }
}

// 刷新Token请求参数
export interface RefreshTokenParams {
  refreshToken: string
}

// 用户登录
export const login = (params: LoginParams): Promise<LoginResponse> => {
  return authRequest.post('/user-auth/login', params)
}

// 刷新Token
export const refreshToken = (params: RefreshTokenParams): Promise<LoginResponse> => {
  return authRequest.post('/user-auth/refresh-token', params)
}

// 用户登出
export const logout = (): Promise<void> => {
  return authRequest.post('/user-auth/logout')
}

// 获取当前用户信息
export const getUserInfo = (): Promise<LoginResponse['user']> => {
  return authRequest.get('/user-auth/user-info')
}