import { http } from '../http';

export enum Gender {
  Male = 0,
  Female = 1,
  PreferNotToSay = 2,
}

export enum UserType {
  Seeker = 0,
  HR = 1,
}

export interface SendVerificationCodeParams {
  email: string;
}

export interface VerifyCodeParams {
  email: string;
  code: string;
}

export interface RegisterParams {
  username: string;
  password: string;
  email: string;
  phone: string;
  gender: Gender;
  userType: UserType;
  verifyCode: string;
}

export interface LoginParams {
  username: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
}

export interface RefreshTokenResponse {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
}

const AUTH_SERVER_BASE_URL = 'http://111.229.81.45/smarthire';
const DEFAULT_TIMEOUT = 15000;
const SUCCESS_CODE = 0;

interface ApiResponse<T = unknown> {
  code: number;
  data: T;
  message: string;
}

export function sendVerificationCode(params: SendVerificationCodeParams): Promise<null> {
  const queryString = `email=${encodeURIComponent(params.email)}`;
  const url = `${AUTH_SERVER_BASE_URL}/api/user-auth/send-verification-code?${queryString}`;
  
  return new Promise((resolve, reject) => {
    uni.request({
      url,
      method: 'POST',
      header: {
        'Content-Type': 'application/json',
      },
      timeout: DEFAULT_TIMEOUT,
      success({ statusCode, data }) {
        if (statusCode >= 200 && statusCode < 300) {
          const response = data as ApiResponse<null>;
          if (response.code === SUCCESS_CODE || statusCode === 200) {
            resolve(response.data);
          } else {
            reject(new Error(response.message || 'Request failed'));
          }
        } else {
          reject(new Error(`Request failed with status ${statusCode}`));
        }
      },
      fail(error) {
        reject(new Error(error.errMsg || 'Network error'));
      },
    });
  });
}

export function verifyCode(params: VerifyCodeParams): Promise<null> {
  const queryString = `email=${encodeURIComponent(params.email)}&code=${encodeURIComponent(params.code)}`;
  const url = `${AUTH_SERVER_BASE_URL}/api/user-auth/verify-code?${queryString}`;
  
  return new Promise((resolve, reject) => {
    uni.request({
      url,
      method: 'POST',
      header: {
        'Content-Type': 'application/json',
      },
      timeout: DEFAULT_TIMEOUT,
      success({ statusCode, data }) {
        if (statusCode >= 200 && statusCode < 300) {
          const response = data as ApiResponse<null>;
          if (response.code === SUCCESS_CODE || statusCode === 200) {
            resolve(response.data);
          } else {
            reject(new Error(response.message || 'Request failed'));
          }
        } else {
          reject(new Error(`Request failed with status ${statusCode}`));
        }
      },
      fail(error) {
        reject(new Error(error.errMsg || 'Network error'));
      },
    });
  });
}

export function register(params: RegisterParams): Promise<null> {
  const url = `${AUTH_SERVER_BASE_URL}/api/user-auth/register`;
  
  return new Promise((resolve, reject) => {
    uni.request({
      url,
      method: 'POST',
      data: params,
      header: {
        'Content-Type': 'application/json',
      },
      timeout: DEFAULT_TIMEOUT,
      success({ statusCode, data }) {
        if (statusCode >= 200 && statusCode < 300) {
          const response = data as ApiResponse<null>;
          if (response.code === SUCCESS_CODE || statusCode === 200) {
            resolve(response.data);
          } else {
            reject(new Error(response.message || 'Request failed'));
          }
        } else {
          reject(new Error(`Request failed with status ${statusCode}`));
        }
      },
      fail(error) {
        reject(new Error(error.errMsg || 'Network error'));
      },
    });
  });
}

export function login(params: LoginParams): Promise<LoginResponse> {
  const url = `${AUTH_SERVER_BASE_URL}/api/user-auth/login`;
  
  return new Promise((resolve, reject) => {
    uni.request({
      url,
      method: 'POST',
      data: params,
      header: {
        'Content-Type': 'application/json',
      },
      timeout: DEFAULT_TIMEOUT,
      success({ statusCode, data }) {
        if (statusCode >= 200 && statusCode < 300) {
          const response = data as ApiResponse<LoginResponse>;
          if (response.code === SUCCESS_CODE || statusCode === 200) {
            resolve(response.data);
          } else {
            reject(new Error(response.message || 'Request failed'));
          }
        } else {
          reject(new Error(`Request failed with status ${statusCode}`));
        }
      },
      fail(error) {
        reject(new Error(error.errMsg || 'Network error'));
      },
    });
  });
}

/**
 * User logout
 */
export function logout(): Promise<null> {
  return http<null>({
    url: '/api/user-auth/logout',
    method: 'POST',
  });
}

/**
 * Refresh access token using refresh token
 * @returns New tokens with accessToken, refreshToken and expiresIn
 */
export function refreshToken(): Promise<RefreshTokenResponse> {
  return http<RefreshTokenResponse>({
    url: '/api/user-auth/refresh-token',
    method: 'POST',
    skipAuth: true,
  });
}
