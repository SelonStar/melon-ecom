import  http  from '@/utils/http'
import type { Result } from '@/types/api'

// export type Result = {
//   code: number
//   message: string
//   data?: Array<any> | number | string | object
// }

// export type ResultTable = {
//   code: number
//   message: string
//   data?: {
//     /** 列表数据 */
//     items: Array<any>
//     /** 总条目数 */
//     total?: number
//     /** 每页显示条目个数 */
//     pageSize?: number
//     /** 当前页数 */
//     currentPage?: number
//   }
// }

// /** 用户登录 */
// export const login = (data: object) => {
//   return http<Result>('post', '/user/login', { data })
// }

// /** 用户登出 */
// export const logout = () => {
//   return http<Result>('post', '/user/logout')
// }

// /** 发送邮箱验证码 */
// export const sendEmailCode = (email: string) => {
//   return http<Result>('get', '/user/sendVerificationCode', {
//     params: { email },
//   })
// }

// /** 用户注册 */
// export const register = (data: object) => {
//   return http<Result>('post', '/user/register', { data })
// }

// /** 重置密码 */
// export const resetPassword = (data: object) => {
//   return http<Result>('patch', '/user/resetUserPassword', { data })
// }

// /** 获取用户信息 */
// export const getUserInfo = () => {
//   return http<Result>('get', '/user/getUserInfo')
// }

// /** 更新用户信息 */
// export const updateUserInfo = (data: object) => {
//   return http<Result>('put', '/user/updateUserInfo', { data })
// }

// /** 更新用户头像 */
// export const updateUserAvatar = (formData: FormData) => {
//   return http<Result>('patch', '/user/updateUserAvatar', {
//     headers: {
//       'Content-Type': 'multipart/form-data',
//     },
//     data: formData,
//     transformRequest: [(data) => data], // 防止 axios 处理 FormData
//   })
// }

// /** 注销账号 */
// export const deleteUser = () => {
//   return http<Result>('delete', '/user/deleteAccount')
// }

/** 获取轮播图 */
export const getBanner = () => {
  return http<Result>('get', '/banner/getBannerList')
}
