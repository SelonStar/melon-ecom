import http from '@/utils/http'
import type { Result } from '@/types/api'
import type { CartVO } from '@/types/cart'

/** 修改购物车商品数量 */
export type UpdateCartItemReq = {
  quantity: number
}
export const updateCartItemQuantity = (skuId: number, data: UpdateCartItemReq) => {
  return http<Result>('put', `/cart/items/${skuId}`, { data })
}

/** 删除购物车商品 */
export const deleteCartItem = (skuId: number) => {
  return http<Result>('delete', `/cart/items/${skuId}`)
}

/** 加入购物车 */
export type AddCartItemReq = {
  skuId: number
  quantity: number
}
export const addCartItem = (data: AddCartItemReq) => {
  return http<Result>('post', '/cart/items', { data })
}

export type BatchAddCartItemReq = {
  skuId: number
  quantity: number
}

export type BatchAddCartItemResp = {
  skuId?: number
  quantity?: number
  success: boolean
  message?: string
}

export type BatchAddCartResp = {
  successCount: number
  failCount: number
  items: BatchAddCartItemResp[]
}

export const batchAddCartItems = (items: BatchAddCartItemReq[]) => {
  return http<Result<BatchAddCartResp>>('post', '/cart/items/batch', { data: { items } })
}

/** 结算 */
export const checkout = () => {
  return http<Result>('post', '/cart/checkout')
}

/** 获取购物车 */
export const getCart = () => {
  return http<Result<CartVO>>('get', '/cart')
}
