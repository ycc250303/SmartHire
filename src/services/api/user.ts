import { http } from '../http';

export interface UserInfo {
  username: string;
  email: string;
  phone: string;
  userType: number;
  avatarUrl: string;
}

export interface PublicUserInfo {
  id: number;
  username: string;
  userType: number;
  avatarUrl: string;
}

/**
 * Get current user information
 */
export function getCurrentUserInfo(): Promise<UserInfo> {
  return http<UserInfo>({
    url: '/user-auth/user-info',
    method: 'GET',
  });
}

/**
 * Get public user information by userId
 */
export function getPublicUserInfo(userId: number): Promise<PublicUserInfo> {
  return http<PublicUserInfo>({
    url: `/user-auth/public-user-info/${userId}`,
    method: 'GET',
    skipAuth: true,
  });
}

