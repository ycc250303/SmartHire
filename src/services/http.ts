export type HttpMethod = "GET" | "POST" | "PUT" | "DELETE" | "PATCH";

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
const TOKEN_EXPIRE_KEY = "auth_token_expire";
const SUCCESS_CODE = 0;
const TOKEN_EXPIRY_DAYS = 3;

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
 * Set auth token with expiry time
 */
export function setTokenWithExpiry(token: string): void {
  try {
    const expireTime = Date.now() + TOKEN_EXPIRY_DAYS * 24 * 60 * 60 * 1000;
    uni.setStorageSync(TOKEN_KEY, token);
    uni.setStorageSync(TOKEN_EXPIRE_KEY, expireTime.toString());
  } catch (error) {
    console.error("Failed to set token with expiry:", error);
  }
}

/**
 * Check if token is valid (exists and not expired)
 */
export function isTokenValid(): boolean {
  try {
    const token = uni.getStorageSync(TOKEN_KEY);
    if (!token) {
      return false;
    }

    const expireTime = uni.getStorageSync(TOKEN_EXPIRE_KEY);
    if (!expireTime) {
      return false;
    }

    const expireTimeNum = parseInt(expireTime, 10);
    if (isNaN(expireTimeNum)) {
      return false;
    }

    return Date.now() < expireTimeNum;
  } catch {
    return false;
  }
}

/**
 * Clear auth token
 */
export function clearToken(): void {
  try {
    uni.removeStorageSync(TOKEN_KEY);
    uni.removeStorageSync(TOKEN_EXPIRE_KEY);
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

    // Add auth token to HTTP headers
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
          
          // console.log(`[DEBUG] API Response [${config.url}]:`, { code: response.code, hasData: !!response.data });
          
          if (1) {
            resolve(response.data);
          } else {
            console.error(`API Error [${config.url}]:`, response.message);
            reject(new Error(response.message || "Request failed"));
          }
        } else if (statusCode === 401) {
          clearToken();
          console.error(`Unauthorized [${config.url}]`);
          reject(new Error("Unauthorized"));
        } else {
          console.error(`HTTP Error [${config.url}]: ${statusCode}`, data);
          reject(new Error(`Request failed with status ${statusCode}`));
        }
      },
      fail(error) {
        console.error(`Network Error [${config.url}]:`, error);
        reject(new Error(error.errMsg || "Network error"));
      },
    });
  });
}
