import http from '@/utils/http'
import type { Result } from '@/types/api'
import type {
  CreateTryOnTaskReq,
  TryOnCandidatePageResp,
  TryOnPhotoType,
  TryOnSlotType,
  TryOnTaskVO,
  UploadTryOnPhotoResp,
} from '@/types/tryon'

export type GetTryOnCandidateSkusParams = {
  slotType: TryOnSlotType
  photoType?: TryOnPhotoType
  keyword?: string
  pageNum?: number
  pageSize?: number
}

export const uploadTryOnPhoto = (photoType: TryOnPhotoType, file: File) => {
  const formData = new FormData()
  formData.append('photoType', photoType)
  formData.append('file', file)
  return http<Result<UploadTryOnPhotoResp>>('post', '/tryon/photos', {
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

export const getTryOnCandidateSkus = (params: GetTryOnCandidateSkusParams) => {
  return http<Result<TryOnCandidatePageResp>>('get', '/tryon/skus', { params })
}

export const createTryOnTask = (data: CreateTryOnTaskReq) => {
  return http<Result<TryOnTaskVO>>('post', '/tryon/tasks', { data })
}

export const getTryOnTask = (taskId: number) => {
  return http<Result<TryOnTaskVO>>('get', `/tryon/tasks/${taskId}`)
}
