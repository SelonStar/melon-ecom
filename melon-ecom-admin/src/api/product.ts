// src/api/product.ts
import { request } from '@/utils/http'
import type { Result } from '@/types/api'

/** 商品后台列表 VO */
export type ProductAdminVO = {
  productId: number
  name: string
  subTitle?: string
  brandId?: number
  categoryId?: number
  mainImageUrl?: string
  status?: number
}

/** 商品后台详情 VO */
export type ProductAdminDetailVO = {
  productId: number
  name: string
  subTitle?: string
  brandId?: number
  categoryId?: number
  mainImageUrl?: string
  detailHtml?: string
  status?: number
  imageUrls?: string[]
  skus?: SkuVO[]
}

/** SKU VO */
export type SkuVO = {
  skuId: number
  productId: number
  skuCode?: string
  name?: string
  specJson?: string
  price?: number
  costPrice?: number
  weight?: number
  stock?: number
  imageUrl?: string
  aiTryonEnabled?: number
  tryonCategory?: string
  tryonImageUrl?: string
  tryonMaskUrl?: string
  tryonSort?: number
  status?: number
}

/** 商品搜索 DTO */
export type ProductSearchDTO = {
  page?: number
  pageSize?: number
  keyword?: string
  status?: number
  categoryId?: number
  brandId?: number
}

/** 新增商品 DTO */
export type ProductAddDTO = {
  name: string
  subTitle?: string
  brandId?: number
  categoryId?: number
  mainImageUrl?: string
  detailHtml?: string
  status?: number
  imageUrls?: string[]
  skus?: SkuAddDTO[]
}

/** 更新商品 DTO */
export type ProductUpdateDTO = {
  productId: number
  name?: string
  subTitle?: string
  brandId?: number
  categoryId?: number
  mainImageUrl?: string
  detailHtml?: string
  status?: number
  imageUrls?: string[]
  skus?: SkuAddDTO[]
}

/** 新增 SKU DTO */
export type SkuAddDTO = {
  productId: number
  skuCode?: string
  name?: string
  specJson?: string
  price?: number
  costPrice?: number
  stock?: number
  imageUrl?: string
  aiTryonEnabled?: number
  tryonCategory?: string
  tryonImageUrl?: string
  tryonMaskUrl?: string
  tryonSort?: number
  weight?: number
  status?: number
}

/** 更新 SKU DTO */
export type SkuUpdateDTO = {
  id: number
  productId?: number
  skuCode?: string
  skuName?: string
  price?: number
  stock?: number
  imageUrl?: string
  status?: number
}

/** 获取商品总数 */
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

/** 获取商品分页列表 */
export const getAllProducts = (
  data: ProductSearchDTO
): Promise<Result<any>> => {
  return request({
    method: 'post',
    url: '/admin/product/getAllProducts',
    data,
  })
}

/** 获取商品详情 */
export const getProductDetail = (
  id: number
): Promise<Result<ProductAdminDetailVO>> => {
  return request<ProductAdminDetailVO>({
    method: 'get',
    url: `/admin/product/detail/${id}`,
  })
}

/** 新增商品 */
export const addProduct = (data: ProductAddDTO): Promise<Result<any>> => {
  return request({
    method: 'post',
    url: '/admin/product/addProduct',
    data,
  })
}

/** 更新商品 */
export const updateProduct = (data: ProductUpdateDTO): Promise<Result<any>> => {
  return request({
    method: 'put',
    url: '/admin/product/updateProduct',
    data,
  })
}

/** 更新商品状态 */
export const updateProductStatus = (
  id: number,
  status: number
): Promise<Result<any>> => {
  return request({
    method: 'patch',
    url: `/admin/product/updateProductStatus/${id}`,
    params: { status },
  })
}

/** 删除商品 */
export const deleteProduct = (id: number): Promise<Result<any>> => {
  return request({
    method: 'delete',
    url: `/admin/product/deleteProduct/${id}`,
  })
}

/** 新增 SKU */
export const addSku = (data: SkuAddDTO): Promise<Result<any>> => {
  return request({
    method: 'post',
    url: '/admin/product/addSku',
    data,
  })
}

/** 更新 SKU */
export const updateSku = (data: SkuUpdateDTO): Promise<Result<any>> => {
  return request({
    method: 'put',
    url: '/admin/product/updateSku',
    data,
  })
}

/** 更新 SKU 状态 */
export const updateSkuStatus = (
  id: number,
  status: number
): Promise<Result<any>> => {
  return request({
    method: 'patch',
    url: `/admin/product/updateSkuStatus/${id}`,
    params: { status },
  })
}

/** 删除 SKU */
export const deleteSku = (id: number): Promise<Result<any>> => {
  return request({
    method: 'delete',
    url: `/admin/product/deleteSku/${id}`,
  })
}

/** 上传商品图片到 MinIO */
export const uploadImage = (
  file: File,
  folder: string = "product"
): Promise<Result<string>> => {
  const formData = new FormData();
  formData.append("file", file);
  formData.append("folder", folder);
  return request<string>({
    method: "post",
    url: "/admin/product/uploadMainImage",
    data: formData,
    headers: { "Content-Type": undefined }, // 清掉默认的 application/json，让浏览器自动设 multipart/form-data; boundary=...
  });
}
