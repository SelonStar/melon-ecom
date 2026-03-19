export type TryOnPhotoType = 'FULL_BODY' | 'HALF_BODY'

export type TryOnSlotType = 'TOP' | 'BOTTOM' | 'DRESS' | 'SHOES'

export type TryOnTaskStatus = 'SUBMITTED' | 'PROCESSING' | 'SUCCESS' | 'FAILED'

export type TryOnCandidateSkuVO = {
  skuId: number
  productId: number
  productName: string
  productSubTitle?: string
  skuName: string
  imageUrl?: string
  tryOnImageUrl?: string
  tryOnCategory: TryOnSlotType
  price?: string
  stock?: number
  tryOnSort?: number
}

export type UploadTryOnPhotoResp = {
  photoUrl: string
  photoType: TryOnPhotoType
  originalFileName?: string
}

export type CreateTryOnTaskReq = {
  photoUrl: string
  photoType: TryOnPhotoType
  topSkuId?: number | null
  bottomSkuId?: number | null
  dressSkuId?: number | null
  shoesSkuId?: number | null
  currentResultImageUrl?: string
  providerCode?: string
}

export type TryOnTaskVO = {
  taskId: number
  status: TryOnTaskStatus
  photoUrl: string
  photoType: TryOnPhotoType
  topSkuId?: number | null
  bottomSkuId?: number | null
  dressSkuId?: number | null
  shoesSkuId?: number | null
  providerCode?: string
  providerTaskId?: string
  resultImageUrl?: string
  errorMessage?: string
}

export type TryOnCandidatePageResp = {
  total: number
  records: TryOnCandidateSkuVO[]
}

export type TryOnSelectionMap = Record<TryOnSlotType, TryOnCandidateSkuVO | null>
