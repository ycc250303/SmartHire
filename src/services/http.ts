import apiConfig from './config.json';

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

export class ApiError<T = unknown> extends Error {
  code: number;
  data?: T;

  constructor(code: number, message?: string, data?: T) {
    super(message || "Request failed");
    this.code = code;
    this.data = data;
    this.name = "ApiError";
    Object.setPrototypeOf(this, ApiError.prototype);
  }
}

const DEFAULT_TIMEOUT = 15000;
const TOKEN_KEY = "auth_token";
const REFRESH_TOKEN_KEY = "auth_refresh_token";
const TOKEN_EXPIRE_KEY = "auth_token_expire";
const SUCCESS_CODE = 0;

export function getApiPathFromUrl(url: string): string {
  if (url.startsWith('http://') || url.startsWith('https://')) {
    try {
      const urlObj = new URL(url);
      return urlObj.pathname;
    } catch {
      return url;
    }
  }
  
  const queryIndex = url.indexOf('?');
  if (queryIndex !== -1) {
    return url.substring(0, queryIndex);
  }
  
  return url;
}

function matchApiPath(apiPath: string, pattern: string): boolean {
  if (pattern === apiPath) {
    return true;
  }
  if (pattern.endsWith('/*')) {
    const basePattern = pattern.slice(0, -2);
    return apiPath.startsWith(basePattern + '/') || apiPath === basePattern;
  }
  return false;
}

function getEnvironmentForApi(apiPath: string): 'dev' | 'dev-py' | 'prod' | null {
  const mappings = apiConfig.apiMappings;
  
  for (const [pattern, env] of Object.entries(mappings)) {
    if (matchApiPath(apiPath, pattern)) {
      return env as 'dev' | 'dev-py' | 'prod';
    }
  }
  
  return null;
}

export function getApiBaseUrl(apiPath: string): string {
  const env = getEnvironmentForApi(apiPath);
  
  let baseUrl = "";
  if (env === 'dev') {
    baseUrl = import.meta.env.VITE_API_BASE_URL_DEV || import.meta.env.VITE_API_BASE_URL || "";
  } else if (env === 'dev-py') {
    baseUrl = import.meta.env.VITE_API_BASE_URL_PY || import.meta.env.VITE_API_BASE_URL || "";
  } else if (env === 'prod') {
    baseUrl = import.meta.env.VITE_API_BASE_URL_PROD || import.meta.env.VITE_API_BASE_URL || "";
  } else {
    baseUrl = import.meta.env.VITE_API_BASE_URL || "";
  }
  
  if (baseUrl && !baseUrl.startsWith('http://') && !baseUrl.startsWith('https://')) {
    baseUrl = `http://${baseUrl}`;
  }
  
  return baseUrl;
}

/**
 * Get stored access token
 */
export function getToken(): string | null {
  try {
    return uni.getStorageSync(TOKEN_KEY) || null;
  } catch {
    return null;
  }
}

/**
 * Get stored refresh token
 */
export function getRefreshToken(): string | null {
  try {
    return uni.getStorageSync(REFRESH_TOKEN_KEY) || null;
  } catch {
    return null;
  }
}

/**
 * Set access token
 */
export function setToken(token: string): void {
  try {
    uni.setStorageSync(TOKEN_KEY, token);
  } catch (error) {
    console.error("Failed to set token:", error);
  }
}

/**
 * Set refresh token
 */
export function setRefreshToken(token: string): void {
  try {
    uni.setStorageSync(REFRESH_TOKEN_KEY, token);
  } catch (error) {
    console.error("Failed to set refresh token:", error);
  }
}

/**
 * Set both access token and refresh token with expiry time
 */
export function setTokens(accessToken: string, refreshToken: string, expiresIn: number): void {
  try {
    const expireTime = Date.now() + expiresIn * 1000;
    uni.setStorageSync(TOKEN_KEY, accessToken);
    uni.setStorageSync(REFRESH_TOKEN_KEY, refreshToken);
    uni.setStorageSync(TOKEN_EXPIRE_KEY, expireTime.toString());
  } catch (error) {
    console.error("Failed to set tokens:", error);
  }
}

/**
 * Set auth token with expiry time (deprecated, use setTokens instead)
 */
export function setTokenWithExpiry(token: string): void {
  try {
    const expireTime = Date.now() + 3 * 24 * 60 * 60 * 1000;
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
 * Clear all auth tokens
 */
export function clearToken(): void {
  try {
    uni.removeStorageSync(TOKEN_KEY);
    uni.removeStorageSync(REFRESH_TOKEN_KEY);
    uni.removeStorageSync(TOKEN_EXPIRE_KEY);
  } catch (error) {
    console.error("Failed to clear tokens:", error);
  }
}

let refreshTokenPromise: Promise<void> | null = null;

/**
 * Refresh access token using refresh token
 */
async function refreshAccessToken(): Promise<void> {
  if (refreshTokenPromise) {
    return refreshTokenPromise;
  }

  refreshTokenPromise = (async () => {
    try {
      const refreshToken = getRefreshToken();
      if (!refreshToken) {
        throw new Error("No refresh token available");
      }

      const apiPath = '/api/user-auth/refresh-token';
      const baseUrl = getApiBaseUrl(apiPath);
      let normalizedBaseUrl = baseUrl.endsWith('/') ? baseUrl.slice(0, -1) : baseUrl;
      let normalizedPath = apiPath;
      
      if (normalizedBaseUrl.endsWith('/api') && normalizedPath.startsWith('/api')) {
        normalizedPath = normalizedPath.replace(/^\/api/, '');
      }
      
      const fullUrl = `${normalizedBaseUrl}${normalizedPath}`;

      const requestData = { refreshToken: refreshToken };
      console.log('[Params]', apiPath, requestData);

      const response = await new Promise<UniApp.RequestSuccessCallbackResult>((resolve, reject) => {
        uni.request({
          url: fullUrl,
          method: 'POST',
          data: requestData,
          header: {
            "Content-Type": "application/json",
          },
          timeout: DEFAULT_TIMEOUT,
          success: resolve,
          fail: reject,
        });
      });

      if (response.statusCode >= 200 && response.statusCode < 300) {
        const apiResponse = response.data as ApiResponse<{
          accessToken: string;
          refreshToken: string;
          expiresIn: number;
        }>;

        if (apiResponse.code === SUCCESS_CODE && apiResponse.data) {
          console.log('[Response]', apiPath, apiResponse.data);
          setTokens(
            apiResponse.data.accessToken,
            apiResponse.data.refreshToken,
            apiResponse.data.expiresIn
          );
        } else {
          throw new Error(apiResponse.message || "Token refresh failed");
        }
      } else {
        throw new Error(`Token refresh failed with status ${response.statusCode}`);
      }
    } catch (error) {
      clearToken();
      throw error;
    } finally {
      refreshTokenPromise = null;
    }
  })();

  return refreshTokenPromise;
}

/**
 * HTTP request wrapper with auth token management and auto-refresh
 */
export function http<TResponse = unknown, TData extends RequestData = RequestData>(
  config: HttpRequestConfig<TData>,
): Promise<TResponse> {
  const apiPath = getApiPathFromUrl(config.url);
  const baseUrl = getApiBaseUrl(apiPath);

  return new Promise((resolve, reject) => {
    // Ensure we only retry once after a 401 to avoid infinite loops
    let retryCount = 0;

    const makeRequest = async (): Promise<void> => {
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
      
      const fullUrl = normalizedBaseUrl ? `${normalizedBaseUrl}${normalizedPath}` : normalizedPath;

      uni.request({
        url: fullUrl,
        method: (config.method ?? "GET") as UniApp.RequestOptions["method"],
        data: config.data,
        header: headers,
        timeout: config.timeout ?? DEFAULT_TIMEOUT,
        success({ statusCode, data }) {
          if (statusCode >= 200 && statusCode < 300) {
            const response = data as ApiResponse<TResponse>;

            if (response && typeof response === 'object' && 'code' in response) {
              if (response.code === SUCCESS_CODE) {
                resolve(response.data as TResponse);
              } else {
                console.error(`API Error [${config.url}]:`, response.message);
                reject(new ApiError(response.code, response.message, response.data));
              }
            } else {
              resolve(response as TResponse);
            }
          } else if (statusCode === 401 && !config.skipAuth) {
            if (retryCount >= 1 || apiPath === '/api/user-auth/logout') {
              // Already retried once, or logout explicitly: fail fast to avoid loops
              clearToken();
              uni.redirectTo({
                url: '/pages/auth/login',
                fail: () => {
                  console.error('Failed to redirect to login page');
                }
              });
              reject(new Error("Unauthorized"));
              return;
            }

            retryCount += 1;

            refreshAccessToken()
              .then(() => {
                makeRequest();
              })
              .catch((error) => {
                console.error(`Token refresh failed [${config.url}]:`, error);
                clearToken();
                uni.redirectTo({
                  url: '/pages/auth/login',
                  fail: () => {
                    console.error('Failed to redirect to login page');
                  }
                });
                reject(new Error("Unauthorized"));
              });
          } else {
            console.error(`HTTP Error [${config.url}]: ${statusCode}`, data);
            const resp = data as Partial<ApiResponse<TResponse>>;
            if (resp && typeof resp === 'object' && 'code' in resp && 'message' in resp) {
              reject(new ApiError(resp.code as number, resp.message as string, resp.data as TResponse));
            } else {
              reject(new Error(`Request failed with status ${statusCode}`));
            }
          }
        },
        fail(error) {
          console.error(`Network Error [${config.url}]:`, error);
          reject(new Error(error.errMsg || "Network error"));
        },
      });
    };

    makeRequest();
  });
}
