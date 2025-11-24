import { http } from '../http';

export interface UserInfo {
  userId: number;
  username: string;
  email: string;
  phone: string;
  userType: number;
  gender?: number;
}

/**
 * Get current user info
 */
export function getCurrentUserInfo(): Promise<UserInfo> {
  return http<UserInfo>({
    url: '/api/user-auth/current-user',
    method: 'GET',
  });
}




