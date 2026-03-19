// src/api/stock.ts
import { request, requestTable } from '@/utils/http'
import type { Result, ResultTable } from '@/types/api'

/** 仓库实体 */
export type Warehouse = {
  id: number
  warehouseName?: string
  name?: string
  code?: string
  contactName?: string
  contactPhone?: string
  province?: string
  city?: string
  district?: string
  detailAddress?: string
  status?: number
  createTime?: string
  updateTime?: string
}

/** 库存实体 */
export type Stock = {
  id: number
  warehouseId: number
  skuId: number
  available?: number
  locked?: number
  version?: number
  createTime?: string
  updateTime?: string
}

/** 锁库记录实体 */
export type StockReservation = {
  id: number
  orderId?: number
  orderItemId?: number
  skuId?: number
  warehouseId?: number
  quantity?: number
  status?: number
  expireTime?: string
  createTime?: string
  updateTime?: string
}

/** 分仓结果实体 */
export type WarehouseAllocation = {
  id: number
  orderId?: number
  orderItemId?: number
  skuId?: number
  warehouseId?: number
  quantity?: number
  createTime?: string
  updateTime?: string
}

/** 库存流水实体 */
export type StockTxn = {
  id: number
  skuId?: number
  warehouseId?: number
  bizType?: number | string
  changeQty?: number
  beforeAvailable?: number
  afterAvailable?: number
  beforeLocked?: number
  afterLocked?: number
  refId?: number
  remark?: string
  createTime?: string
}

/** 仓库搜索 DTO */
export type WarehouseSearchDTO = {
  currentPage?: number
  pageSize?: number
  keyword?: string
  status?: number
}

/** 新增仓库 DTO */
export type WarehouseAddDTO = {
  warehouseName?: string
  name?: string
  code?: string
  contactName?: string
  contactPhone?: string
  province?: string
  city?: string
  district?: string
  detailAddress?: string
  status?: number
}

/** 更新仓库 DTO */
export type WarehouseUpdateDTO = {
  id: number
  warehouseName?: string
  name?: string
  code?: string
  contactName?: string
  contactPhone?: string
  province?: string
  city?: string
  district?: string
  detailAddress?: string
  status?: number
}

/** 库存搜索 DTO */
export type StockSearchDTO = {
  currentPage?: number
  pageSize?: number
  skuId?: number
  warehouseId?: number
}

/** 库存调整 DTO */
export type StockAdjustDTO = {
  skuId: number
  warehouseId: number
  quantity: number
  bizType?: number | string
  remark?: string
}

/** 锁定记录搜索 DTO */
export type StockReservationSearchDTO = {
  currentPage?: number
  pageSize?: number
  orderId?: number
  skuId?: number
  warehouseId?: number
  status?: number
}

/** 分仓结果搜索 DTO */
export type WarehouseAllocationSearchDTO = {
  currentPage?: number
  pageSize?: number
  orderId?: number
  skuId?: number
  warehouseId?: number
}

/** 库存流水搜索 DTO */
export type StockTxnSearchDTO = {
  currentPage?: number
  pageSize?: number
  skuId?: number
  warehouseId?: number
  bizType?: number | string
  refId?: number
}

/** 获取仓库总数 */
export const getAllWarehousesCount = (params?: {
  status?: number
}): Promise<Result<number>> => {
  return request<number>({
    method: 'get',
    url: '/admin/stock/getAllWarehousesCount',
    params,
  })
}

/** 获取仓库列表 */
export const getAllWarehouses = (
  data: WarehouseSearchDTO
): Promise<ResultTable<Warehouse>> => {
  return requestTable<Warehouse>({
    method: 'post',
    url: '/admin/stock/getAllWarehouses',
    data,
  })
}

/** 新增仓库 */
export const addWarehouse = (data: WarehouseAddDTO): Promise<Result<any>> => {
  return request({
    method: 'post',
    url: '/admin/stock/addWarehouse',
    data,
  })
}

/** 更新仓库 */
export const updateWarehouse = (
  data: WarehouseUpdateDTO
): Promise<Result<any>> => {
  return request({
    method: 'put',
    url: '/admin/stock/updateWarehouse',
    data,
  })
}

/** 更新仓库状态 */
export const updateWarehouseStatus = (
  id: number,
  status: number
): Promise<Result<any>> => {
  return request({
    method: 'patch',
    url: `/admin/stock/updateWarehouseStatus/${id}`,
    params: { status },
  })
}

/** 删除仓库 */
export const deleteWarehouse = (id: number): Promise<Result<any>> => {
  return request({
    method: 'delete',
    url: `/admin/stock/deleteWarehouse/${id}`,
  })
}

/** 获取库存记录总数 */
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

/** 获取库存列表 */
export const getAllStocks = (
  data: StockSearchDTO
): Promise<ResultTable<Stock>> => {
  return requestTable<Stock>({
    method: 'post',
    url: '/admin/stock/getAllStocks',
    data,
  })
}

/** 库存调整 */
export const adjustStock = (data: StockAdjustDTO): Promise<Result<any>> => {
  return request({
    method: 'put',
    url: '/admin/stock/adjustStock',
    data,
  })
}

/** 获取锁定记录列表 */
export const getAllStockReservations = (
  data: StockReservationSearchDTO
): Promise<ResultTable<StockReservation>> => {
  return requestTable<StockReservation>({
    method: 'post',
    url: '/admin/stock/getAllStockReservations',
    data,
  })
}

/** 获取分仓结果列表 */
export const getAllWarehouseAllocations = (
  data: WarehouseAllocationSearchDTO
): Promise<ResultTable<WarehouseAllocation>> => {
  return requestTable<WarehouseAllocation>({
    method: 'post',
    url: '/admin/stock/getAllWarehouseAllocations',
    data,
  })
}

/** 获取库存流水列表 */
export const getAllStockTxns = (
  data: StockTxnSearchDTO
): Promise<ResultTable<StockTxn>> => {
  return requestTable<StockTxn>({
    method: 'post',
    url: '/admin/stock/getAllStockTxns',
    data,
  })
}