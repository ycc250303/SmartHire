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

/**
 * Send verification code to email
 */
export function sendVerificationCode(params: SendVerificationCodeParams): Promise<null> {
  const queryString = `email=${encodeURIComponent(params.email)}`;
  return http<null>({
    url: `/api/user-auth/send-verification-code?${queryString}`,
    method: 'POST',
    skipAuth: true,
  });
}

/**
 * Verify email verification code
 */
export function verifyCode(params: VerifyCodeParams): Promise<null> {
  const queryString = `email=${encodeURIComponent(params.email)}&code=${encodeURIComponent(params.code)}`;
  return http<null>({
    url: `/api/user-auth/verify-code?${queryString}`,
    method: 'POST',
    skipAuth: true,
  });
}

/**
 * User registration
 */
export function register(params: RegisterParams): Promise<null> {
  return http<null>({
    url: '/api/user-auth/register',
    method: 'POST',
    data: params,
    skipAuth: true,
  });
}

/**
 * User login
 * @returns Login response with accessToken, refreshToken and expiresIn
 */
export function login(params: LoginParams): Promise<LoginResponse> {
  return http<LoginResponse>({
    url: '/api/user-auth/login',
    method: 'POST',
    data: params,
    skipAuth: true,
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
