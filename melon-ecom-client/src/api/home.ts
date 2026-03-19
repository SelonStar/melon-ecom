import { request } from '@/utils/http'
import type { Result } from '@/types/api'

export type HomeNavCardItemVO = {
  itemId?: number
  keyword?: string
  title?: string
  imageUrl?: string
  sort?: number
}

export type HomeNavCardVO = {
  cardId?: number
  title?: string
  sort?: number
  items?: HomeNavCardItemVO[]
}

export const getHomeNavCards = (): Promise<Result<HomeNavCardVO[]>> =>
  request<HomeNavCardVO[]>({
    method: 'get',
    url: '/home/navCards',
  })
