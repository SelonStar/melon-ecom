// src/api/user.ts
import { request, requestTable } from '@/utils/http'
import type { Result, ResultTable } from '@/types/api'

/** 用户管理列表项 VO */
export type UserManagementVO = {
  id: number
  username: string
  email?: string
  phone?: string
  avatar?: string
  status?: number
  createTime?: string
  updateTime?: string
}

/** 用户搜索 DTO */
export type UserSearchDTO = {
  currentPage?: number
  pageSize?: number
  keyword?: string
  username?: string
  email?: string
  phone?: string
  status?: number
}

/** 新增用户 DTO */
export type UserAddDTO = {
  username: string
  password: string
  email?: string
  phone?: string
  avatar?: string
  status?: number
}

/** 更新用户 DTO */
export type UserDTO = {
  id: number
  username?: string
  email?: string
  phone?: string
  avatar?: string
  status?: number
}

/** 获取所有用户数量 */
export const getAllUsersCount = (): Promise<Result<number>> => {
  return request<number>({
    method: 'get',
    url: '/admin/getAllUsersCount',
  })
}

/** 获取所有用户信息（分页） */
export const getAllUsers = (
  data: UserSearchDTO
): Promise<ResultTable<UserManagementVO>> => {
  return requestTable<UserManagementVO>({
    method: 'post',
    url: '/admin/getAllUsers',
    data,
  })
}

/** 新增用户 */
export const addUser = (data: UserAddDTO): Promise<Result<any>> => {
  return request({
    method: 'post',
    url: '/admin/addUser',
    data,
  })
}

/** 更新用户信息 */
export const updateUser = (data: UserDTO): Promise<Result<any>> => {
  return request({
    method: 'put',
    url: '/admin/updateUser',
    data,
  })
}

/** 更新用户状态 */
export const updateUserStatus = (
  id: number,
  status: number
): Promise<Result<any>> => {
  return request({
    method: 'patch',
    url: `/admin/updateUserStatus/${id}/${status}`,
  })
}

/** 删除单个用户 */
export const deleteUser = (id: number): Promise<Result<any>> => {
  return request({
    method: 'delete',
    url: `/admin/deleteUser/${id}`,
  })
}

/** 批量删除用户 */
export const deleteUsers = (data: number[]): Promise<Result<any>> => {
  return request({
    method: 'delete',
    url: '/admin/deleteUsers',
    data,
  })
}