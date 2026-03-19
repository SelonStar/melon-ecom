export type Result<T = any> = {
  code: number
  message: string
  data?: T
}

export type ResultTable<T = any> = {
  code: number
  message: string
  data?: {
    items: T[]
    total?: number
    pageSize?: number
    currentPage?: number
  }
}
