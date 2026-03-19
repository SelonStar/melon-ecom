// src/api/data.ts
import { request } from '@/utils/http'
import type { Result } from '@/types/api'

export type DashboardCountVO = {
  userCount: number
  productCount: number
  warehouseCount: number
  stockCount: number
}

export const getAllUsersCount = (): Promise<Result<number>> => {
  return request<number>({
    method: 'get',
    url: '/admin/getAllUsersCount',
  })
}

export const getAllProductsCount = (params?: {
  status?: number
  categoryId?: number
  brandId?: number
  keyword?: string
}): Promise<Result<number>> => {
  return request<number>({
    method: 'get',
    url: '/admin/product/getAllProductsCount',
    params,
  })
}

export const getAllWarehousesCount = (params?: {
  status?: number
}): Promise<Result<number>> => {
  return request<number>({
    method: 'get',
    url: '/admin/stock/getAllWarehousesCount',
    params,
  })
}

export const getAllStocksCount = (params?: {
  skuId?: number
  warehouseId?: number
}): Promise<Result<number>> => {
  return request<number>({
    method: 'get',
    url: '/admin/stock/getAllStocksCount',
    params,
  })
}

/** 前端聚合加载后台统计 */
export const getDashboardCounts = async (): Promise<DashboardCountVO> => {
  const [userRes, productRes, warehouseRes, stockRes] = await Promise.all([
    getAllUsersCount(),
    getAllProductsCount(),
    getAllWarehousesCount(),
    getAllStocksCount(),
  ])

  return {
    userCount: userRes.data ?? 0,
    productCount: productRes.data ?? 0,
    warehouseCount: warehouseRes.data ?? 0,
    stockCount: stockRes.data ?? 0,
  }
}