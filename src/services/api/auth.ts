import { http } from '../http';

export enum Gender {
  Male = 0,
  Female = 1,
  PreferNotToSay = 2,
}

export enum UserType {
  Seeker = 1,
  HR = 2,
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

export function sendVerificationCode(params: SendVerificationCodeParams): Promise<null> {
  const queryString = `email=${encodeURIComponent(params.email)}`;
  const url = `/api/user-auth/send-verification-code?${queryString}`;
  console.log('[Params]', url, params);
  return http<null>({
    url,
    method: 'POST',
    skipAuth: true,
  }).then(response => {
    console.log('[Response]', url, response);
    return response;
  });
}

export function verifyCode(params: VerifyCodeParams): Promise<null> {
  const queryString = `email=${encodeURIComponent(params.email)}&code=${encodeURIComponent(params.code)}`;
  const url = `/api/user-auth/verify-code?${queryString}`;
  console.log('[Params]', url, params);
  return http<null>({
    url,
    method: 'POST',
    skipAuth: true,
  }).then(response => {
    console.log('[Response]', url, response);
    return response;
  });
}

export function register(params: RegisterParams): Promise<null> {
  const url = '/api/user-auth/register';
  console.log('[Params]', url, params);
  return http<null>({
    url,
    method: 'POST',
    data: params,
    skipAuth: true,
  }).then(response => {
    console.log('[Response]', url, response);
    return response;
  });
}

export function login(params: LoginParams): Promise<LoginResponse> {
  const url = '/api/user-auth/login';
  console.log('[Params]', url, params);
  return http<LoginResponse>({
    url,
    method: 'POST',
    data: params,
    skipAuth: true,
  }).then(response => {
    console.log('[Response]', url, response);
    return response;
  });
}

/**
 * User logout
 */
export function logout(): Promise<null> {
  const url = '/api/user-auth/logout';
  console.log('[Params]', url, null);
  return http<null>({
    url,
    method: 'POST',
  }).then(response => {
    console.log('[Response]', url, response);
    return response;
  });
}

/**
 * Refresh access token using refresh token
 * @returns New tokens with accessToken, refreshToken and expiresIn
 */
export function refreshToken(): Promise<RefreshTokenResponse> {
  const url = '/api/user-auth/refresh-token';
  console.log('[Params]', url, null);
  return http<RefreshTokenResponse>({
    url,
    method: 'POST',
    skipAuth: true,
  }).then(response => {
    console.log('[Response]', url, response);
    return response;
  });
}
