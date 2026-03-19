import  http  from '@/utils/http'
import type { Result } from '@/types/api'

export type LoginReq = {
  email: string
  password: string
}

export type LoginResp = {
  token: string
  userId: number
  nickname?: string
}

export type RegisterReq = {
  username: string
  email: string
  password: string
  verificationCode: string
}

export type RegisterResp = {
  userId?: number
  token?: string
}

/** 用户登录 */
export const login = (data: LoginReq) => {
  return http<Result<LoginResp>>('post', '/user/login', { data })
}

/** 用户登出 */
export const logout = () => {
  return http<Result>('post', '/user/logout')
}

/** 发送邮箱验证码 */
export const sendEmailCode = (email: string) => {
  return http<Result>('get', '/user/sendVerificationCode', {
    params: { email },
  })
}


/** 重置密码 */
export const resetPassword = (data: object) => {
  return http<Result>('patch', '/user/resetUserPassword', { data })
}

/** 获取用户信息 */
export const getUserInfo = () => {
  return http<Result>('get', '/user/getUserInfo')
}

/** 更新用户信息 */
export const updateUserInfo = (data: object) => {
  return http<Result>('put', '/user/updateUserInfo', { data })
}

/** 更新用户头像 */
export const updateUserAvatar = (formData: FormData) => {
  return http<Result>('patch', '/user/updateUserAvatar', {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
    data: formData,
    transformRequest: [(data) => data], // 防止 axios 处理 FormData
  })
}

/** 注销账号 */
export const deleteUser = () => {
  return http<Result>('delete', '/user/deleteAccount')
}

/** 用户注册 */
export const register = (data: RegisterReq) => {
  return http<Result<RegisterResp>>('post', '/user/register', { data })
}