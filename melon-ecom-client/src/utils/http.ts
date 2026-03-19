import type { Method } from 'axios'

import axios, {
  AxiosInstance,
  AxiosRequestConfig,
  InternalAxiosRequestConfig,
  AxiosRequestHeaders,
  AxiosResponse,
} from 'axios'
import { ElMessage } from 'element-plus'

import type { Result, ResultTable } from '@/types/api'
import { UserStore } from '@/stores/modules/user'
import { useAuthModalStore } from '@/stores/modules/authModal'

const instance: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  timeout: 20000,
  headers: {
    Accept: 'application/json, text/plain, */*',
    'Content-Type': 'application/json',
    'X-Requested-With': 'XMLHttpRequest',
  },
  withCredentials: false,
})

/** 业务成功码：你的 swagger 显示 code=0 */
const SUCCESS_CODE = 0

/**
 * 前端侧“无需 token”的接口前缀/路径
 * 注意：这是“前端不注入 token”的白名单，不等于后端放行。
 */
const NO_AUTH_PATHS: string[] = [
  // auth
  '/auth/',

  // swagger & static
  '/doc.html',
  '/favicon.ico',
  '/webjars/',
  '/swagger-ui/',
  '/swagger-ui.html',
  '/v3/api-docs',
  '/swagger-resources/',

  // ecommerce common public
  '/category/',
  '/product/',
  '/banner/',
  '/home/',
  '/search/',
  '/file/',
  '/oss/',
  '/minio/',
]

function isNoAuth(url?: string): boolean {
  if (!url) return false
  return NO_AUTH_PATHS.some((p) => url.startsWith(p) || url.includes(p))
}

/** ✅ 打开登录弹窗（防抖/去重） */
function openLoginModalOnce(message?: string) {
  const authModal = useAuthModalStore()
  if (!authModal.visible) {
    if (message) ElMessage.error(message)
    authModal.open('login')
  }
}

// 请求拦截器
instance.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    // 白名单接口不注入 token
    if (isNoAuth(config.url)) return config

    const userStore = UserStore()
    const token = userStore.userInfo?.token

    if (token) {
      if (!config.headers) config.headers = {} as AxiosRequestHeaders
      config.headers.Authorization = `Bearer ${token}`
    }

    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器
instance.interceptors.response.use(
  (response: AxiosResponse) => {
    const res = response.data as Result<any>

    // 兜底：万一不是 Result 包装（文件流等），直接返回原始数据
    if (!res || typeof res !== 'object' || !('code' in res)) {
      return response.data
    }

    if (res.code === SUCCESS_CODE) {
      return res
    }

    // ✅ 业务层未登录（根据你后端实际 code 调整）
    if (res.code === 401 || res.code === 1001 || res.code === 10001) {
      const userStore = UserStore()
      userStore.clearUserInfo()
      openLoginModalOnce(res.message || '登录已过期，请重新登录')
      return Promise.reject(res)
    }

    // 其他业务错误统一提示
    ElMessage.error(res.message || '请求失败')
    return Promise.reject(res)
  },
  (error) => {
    const resp = error?.response
    if (resp) {
      const status = resp.status as number

      switch (status) {
        case 401: {
          // 登录/注册接口失败：提示一次即可，不要清token、不要弹窗（避免死循环体验差）
          if (isNoAuth(error.config?.url)) {
            ElMessage.error('认证失败，请检查账号或密码')
            break
          }

          // ✅ 非白名单 401：清登录态 + 打开登录弹窗（不再跳 /login）
          const userStore = UserStore()
          userStore.clearUserInfo()
          openLoginModalOnce('登录已过期，请重新登录')
          break
        }

        case 403:
          ElMessage.error('没有权限')
          break
        case 404:
          ElMessage.error('请求的资源不存在')
          break
        case 500:
          ElMessage.error('服务器错误')
          break
        default:
          ElMessage.error(`网络错误(${status})`)
      }
    } else {
      ElMessage.error('网络连接失败')
    }

    return Promise.reject(error)
  }
)

/**
 * ✅ 通用：返回 Result<T>
 */
export const request = <T = any>(config: AxiosRequestConfig): Promise<Result<T>> => {
  return instance.request(config)
}

/**
 * ✅ 简化：直接拿 data => T
 */
export const requestData = async <T = any>(config: AxiosRequestConfig): Promise<T> => {
  const res = await request<T>(config)
  return res.data as T
}

/**
 * ✅ 分页：返回 ResultTable<T> 原样
 */
export const requestTable = <T = any>(
  config: AxiosRequestConfig
): Promise<ResultTable<T>> => {
  return instance.request(config)
}

/**
 * ✅ 分页简化：直接拿 data.items / total / currentPage / pageSize
 */
export const requestTableData = async <T = any>(
  config: AxiosRequestConfig
): Promise<NonNullable<ResultTable<T>['data']>> => {
  const res = await requestTable<T>(config)
  return (res.data || { items: [], total: 0 }) as NonNullable<ResultTable<T>['data']>
}

// 便捷方法（返回 T）
export const httpGet = <T = any>(url: string, params?: object): Promise<T> =>
  requestData<T>({ method: 'get', url, params })

export const httpPost = <T = any>(
  url: string,
  data?: object,
  headers?: object
): Promise<T> => requestData<T>({ method: 'post', url, data, headers })

export const httpPut = <T = any>(
  url: string,
  data?: object,
  headers?: object
): Promise<T> => requestData<T>({ method: 'put', url, data, headers })

export const httpDelete = <T = any>(url: string, params?: object): Promise<T> =>
  requestData<T>({ method: 'delete', url, params })

/**
 * ✅ 你原来的 http(method,url,config) 风格：返回 T
 */
const http = <T = any>(
  method: Method,
  url: string,
  config: AxiosRequestConfig = {}
): Promise<T> => {
  return instance.request<any, T>({
    method,
    url,
    ...config,
  })
}

export default http
