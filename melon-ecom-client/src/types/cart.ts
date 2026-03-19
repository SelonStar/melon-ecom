export type CartItemVO = {
  skuId: number
  skuName: string
  imageUrl?: string
  price: string        // BigDecimal -> 前端用 string 最安全
  quantity: number
  checked: number      // 0/1
  lineAmount: string  // BigDecimal -> string
}

export type CartVO = {
  items: CartItemVO[]
  totalAmount: string // BigDecimal -> string
}
