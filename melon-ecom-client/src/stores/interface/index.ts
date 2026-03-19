/* =========================
 * User
 * ========================= */

export interface UserState {
  /** 登录用户信息（未登录时为空对象） */
  userInfo: Partial<UserModel>
  /** 是否登录 */
  isLoggedIn: boolean
}

/** 电商用户基础模型（前端用，不等同于后端实体） */
export interface UserModel {
  userId: number
  username: string
  email?: string
  phone?: string
  avatarUrl?: string
  introduction?: string

  /** JWT / session token */
  token?: string

  /** 可选：角色/权限（你如果没做 RBAC 可先不管） */
  roles?: string[]
}

/* =========================
 * Cart
 * ========================= */

export interface CartState {
  /** 购物车条目列表 */
  items: CartItemModel[]
  /** 购物车合计信息（后端可能会直接返回 cartSummary） */
  summary: CartSummaryModel
  /** 是否已从后端加载过（避免重复拉取） */
  loaded: boolean
}

export interface CartItemModel {
  cartItemId: number
  productId: number
  skuId: number

  title: string
  coverUrl?: string

  /** SKU 规格文本，例如 "黑色 / 256G" */
  skuSpecText?: string

  /** 单价（分 or 元：建议和后端一致；如果后端用 BigDecimal 字符串，这里用 string 更安全） */
  unitPrice: string

  quantity: number
  checked: boolean

  /** 库存相关（可选） */
  stockAvailable?: number
}

export interface CartSummaryModel {
  /** 选中商品数量 */
  checkedCount: number
  /** 选中商品金额（同 unitPrice 的单位/类型） */
  checkedAmount: string

  /** 全部商品数量（可选） */
  totalCount?: number
  /** 全部商品金额（可选） */
  totalAmount?: string
}

/* =========================
 * Category
 * ========================= */

export interface CategoryState {
  /** 类目树 */
  tree: CategoryModel[]
  /** 当前选中类目 id */
  currentCategoryId: number | null
}

export interface CategoryModel {
  categoryId: number
  name: string
  parentId?: number
  sort?: number
  iconUrl?: string
  children?: CategoryModel[]
}

/* =========================
 * Search / Filter (optional)
 * ========================= */

export interface SearchState {
  /** 搜索关键字 */
  keyword: string
  /** 搜索历史 */
  history: string[]
  /** 排序：综合/销量/价格等 */
  sortBy: 'default' | 'sales' | 'priceAsc' | 'priceDesc' | 'newest'
}

/* =========================
 * Menu / Layout
 * ========================= */

export interface MenuState {
  /** 顶部/侧边栏当前激活菜单 */
  menuIndex: string
}

/* =========================
 * Setting
 * ========================= */

export interface SettingState {
  /** 是否覆盖抽屉（如果你电商也有 drawer） */
  isDrawerCover: boolean
  /** 当前语言 */
  language: string | null
}

/* =========================
 * Theme
 * ========================= */

export interface ThemeState {
  isDark: boolean
  primary: string
}