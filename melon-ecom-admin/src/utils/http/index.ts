import Axios, {
  type AxiosInstance,
  type AxiosRequestConfig,
  type CustomParamsSerializer
} from "axios";
import type {
  PureHttpError,
  RequestMethods,
  PureHttpResponse,
  PureHttpRequestConfig,
  PureHttpInternalConfig
} from "./types.d";
import type { Result, ResultTable } from "@/types/api";
import { stringify } from "qs";
import NProgress from "../progress";
import { getToken, formatToken, removeToken } from "@/utils/auth";

// 默认配置
const defaultConfig: AxiosRequestConfig = {
  baseURL: "http://localhost:8080",
  timeout: 10000,
  headers: {
    Accept: "application/json, text/plain, */*",
    "Content-Type": "application/json",
    "X-Requested-With": "XMLHttpRequest"
  },
  paramsSerializer: {
    serialize: stringify as unknown as CustomParamsSerializer
  }
};

class PureHttp {
  constructor() {
    this.httpInterceptorsRequest();
    this.httpInterceptorsResponse();
  }

  /** 初始化配置对象 */
  private static initConfig: PureHttpRequestConfig = {};

  /** Axios 实例 */
  private static axiosInstance: AxiosInstance = Axios.create(defaultConfig);

  /** 请求拦截 */
  private httpInterceptorsRequest(): void {
    PureHttp.axiosInstance.interceptors.request.use(
      (config: PureHttpInternalConfig) => {
        NProgress.start();

        // 关键修复：FormData 请求不要走 application/json
        if (config.data instanceof FormData) {
          config.headers.delete("Content-Type");
        }

        if (typeof config.beforeRequestCallback === "function") {
          config.beforeRequestCallback(config);
          return config;
        }

        if (PureHttp.initConfig.beforeRequestCallback) {
          PureHttp.initConfig.beforeRequestCallback(config);
          return config;
        }

        /** 不需要 token 的白名单 */
        const whiteList = [
          "/admin/login",
          "/admin/register",
          "/doc.html",
          "/swagger-ui.html",
          "/swagger-ui/",
          "/v3/api-docs",
          "/webjars/",
          "/swagger-resources/"
        ];

        const requestUrl = config.url || "";
        const isWhiteList = whiteList.some(url => requestUrl.includes(url));

        if (isWhiteList) {
          return config;
        }

        const tokenData = getToken();
        if (tokenData?.accessToken) {
          config.headers.set("Authorization", formatToken(tokenData.accessToken));
        }

        return config;
      },
      error => Promise.reject(error)
    );
  }

  /** 响应拦截 */
  private httpInterceptorsResponse(): void {
    PureHttp.axiosInstance.interceptors.response.use(
      (response: PureHttpResponse) => {
        NProgress.done();

        const $config = response.config;

        if (typeof $config.beforeResponseCallback === "function") {
          $config.beforeResponseCallback(response);
          return response.data;
        }

        if (PureHttp.initConfig.beforeResponseCallback) {
          PureHttp.initConfig.beforeResponseCallback(response);
          return response.data;
        }

        return response.data;
      },
      (error: PureHttpError) => {
        NProgress.done();

        const status = error.response?.status;

        if (status === 401) {
          removeToken();
        }

        $message(error, status);

        error.isCancelRequest = Axios.isCancel(error);
        return Promise.reject(error);
      }
    );
  }

  /** 通用请求 */
  public request<T>(
    method: RequestMethods,
    url: string,
    param?: AxiosRequestConfig,
    axiosConfig?: PureHttpRequestConfig
  ): Promise<T> {
    const config = {
      method,
      url,
      ...param,
      ...axiosConfig
    } as PureHttpRequestConfig;

    return new Promise((resolve, reject) => {
      PureHttp.axiosInstance
        .request<any, T>(config)
        .then((response: T) => {
          resolve(response);
        })
        .catch(error => {
          reject(error);
        });
    });
  }

  /** post */
  public post<T, P>(
    url: string,
    params?: AxiosRequestConfig<P>,
    config?: PureHttpRequestConfig
  ): Promise<T> {
    return this.request<T>("post", url, params, config);
  }

  /** get */
  public get<T, P>(
    url: string,
    params?: AxiosRequestConfig<P>,
    config?: PureHttpRequestConfig
  ): Promise<T> {
    return this.request<T>("get", url, params, config);
  }
}

export const http = new PureHttp();

/** 标准 Result<T> */
export const request = <T = any>(config: AxiosRequestConfig): Promise<Result<T>> => {
  return http.request<Result<T>>(
    (config.method || "get") as RequestMethods,
    config.url || "",
    config
  );
};

/** 标准 ResultTable<T> */
export const requestTable = <T = any>(
  config: AxiosRequestConfig
): Promise<ResultTable<T>> => {
  return http.request<ResultTable<T>>(
    (config.method || "get") as RequestMethods,
    config.url || "",
    config
  );
};

/** 错误提示 */
function $message(error: PureHttpError, status?: number) {
  const message = error.message || "请求失败";

  switch (status) {
    case 400:
      console.error("请求错误(400)");
      break;
    case 401:
      console.error("未授权或登录过期(401)");
      break;
    case 403:
      console.error("拒绝访问(403)");
      break;
    case 404:
      console.error("请求地址出错(404)");
      break;
    case 500:
      console.error("服务器内部错误(500)");
      break;
    default:
      console.error(message);
      break;
  }
}