export type HttpMethod = "GET" | "POST" | "PUT" | "DELETE";

type RequestData = UniApp.RequestOptions["data"];

export interface HttpRequestConfig<TData extends RequestData = RequestData> {
  url: string;
  method?: HttpMethod;
  data?: TData;
  header?: UniApp.RequestOptions["header"];
  timeout?: number;
  skipAuth?: boolean;
}

export interface ApiResponse<T = unknown> {
  code: number;
  data: T;
  message: string;
}

const DEFAULT_TIMEOUT = 15000;
const TOKEN_KEY = "auth_token";
const SUCCESS_CODE = 0;

/**
 * Get stored auth token
 */
export function getToken(): string | null {
  try {
    return uni.getStorageSync(TOKEN_KEY) || null;
  } catch {
    return null;
  }
}

/**
 * Set auth token
 */
export function setToken(token: string): void {
  try {
    uni.setStorageSync(TOKEN_KEY, token);
  } catch (error) {
    console.error("Failed to set token:", error);
  }
}

/**
 * Clear auth token
 */
export function clearToken(): void {
  try {
    uni.removeStorageSync(TOKEN_KEY);
  } catch (error) {
    console.error("Failed to clear token:", error);
  }
}

/**
 * HTTP request wrapper with auth token management
 */
export function http<TResponse = unknown, TData extends RequestData = RequestData>(
  config: HttpRequestConfig<TData>,
): Promise<TResponse> {
  const baseUrl = import.meta.env.VITE_API_BASE_URL || "";

  return new Promise((resolve, reject) => {
    const headers: Record<string, string> = {
      "Content-Type": "application/json",
      ...config.header,
    };

    if (!config.skipAuth) {
      const token = getToken();
      if (token) {
        headers.Authorization = `Bearer ${token}`;
      }
    }


    let normalizedBaseUrl = baseUrl.endsWith('/') ? baseUrl.slice(0, -1) : baseUrl;
    let normalizedPath = config.url.startsWith('/') ? config.url : `/${config.url}`;
    
    if (normalizedBaseUrl.endsWith('/api') && normalizedPath.startsWith('/api')) {
      normalizedPath = normalizedPath.replace(/^\/api/, '');
    }
    
    const fullUrl = `${normalizedBaseUrl}${normalizedPath}`;

    
    uni.request({
      url: fullUrl,
      method: config.method ?? "GET",
      data: config.data,
      header: headers,
      timeout: config.timeout ?? DEFAULT_TIMEOUT,
      success({ statusCode, data }) {
        if (statusCode >= 200 && statusCode < 300) {
          const response = data as ApiResponse<TResponse>;
          
          if (response.code === SUCCESS_CODE) {
            resolve(response.data);
          } else {
            reject(new Error(response.message || "Request failed"));
          }
        } else if (statusCode === 401) {
          clearToken();
          reject(new Error("Unauthorized"));
        } else {
          reject(new Error(`Request failed with status ${statusCode}`));
        }
      },
      fail(error) {
        reject(new Error(error.errMsg || "Network error"));
      },
    });
  });
}
