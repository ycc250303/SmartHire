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
    url: '/api/user-auth/user-info',
    method: 'GET',
  });
}

/**
 * Get public user information by userId
 */
export function getPublicUserInfo(userId: number): Promise<PublicUserInfo> {
  return http<PublicUserInfo>({
    url: `/api/user-auth/public-user-info/${userId}`,
    method: 'GET',
    skipAuth: true,
  });
}

/**
 * Update user avatar with file
 */
export function updateUserAvatarWithFile(filePath: string): Promise<UserInfo> {
  const baseUrl = import.meta.env.VITE_API_BASE_URL || "";
  let normalizedBaseUrl = baseUrl.endsWith('/') ? baseUrl.slice(0, -1) : baseUrl;
  let normalizedPath = '/api/user-auth/update-user-avatar';
  
  if (normalizedBaseUrl.endsWith('/api') && normalizedPath.startsWith('/api')) {
    normalizedPath = normalizedPath.replace(/^\/api/, '');
  }
  
  const fullUrl = `${normalizedBaseUrl}${normalizedPath}`;
  const token = uni.getStorageSync('auth_token');

  console.log('Uploading avatar to:', fullUrl);

  return new Promise((resolve, reject) => {
    // #ifdef H5
    fetch(filePath)
      .then(res => res.blob())
      .then(blob => {
        const formData = new FormData();
        formData.append('avatarFile', blob);
        
        const xhr = new XMLHttpRequest();
        xhr.open('PATCH', fullUrl, true);
        xhr.setRequestHeader('Authorization', token ? `Bearer ${token}` : '');
        
        xhr.onload = () => {
          try {
            let data: any;
            if (typeof xhr.responseText === 'string') {
              data = JSON.parse(xhr.responseText);
            } else {
              data = xhr.responseText;
            }
            
            if (xhr.status >= 200 && xhr.status < 300) {
              if (data && data.code === 0) {
                resolve(data.data);
              } else if (data && !data.code) {
                resolve(data);
              } else {
                reject(new Error(data?.message || 'Update failed'));
              }
            } else {
              reject(new Error(`Request failed with status ${xhr.status}`));
            }
          } catch (error) {
            console.error('Avatar upload response:', xhr.responseText);
            reject(new Error('Failed to parse response: ' + (error instanceof Error ? error.message : String(error))));
          }
        };
        
        xhr.onerror = () => {
          reject(new Error('Network error'));
        };
        
        xhr.send(formData);
      })
      .catch(error => {
        reject(new Error('Failed to read file: ' + (error instanceof Error ? error.message : String(error))));
      });
    // #endif
    
    // #ifndef H5
    uni.uploadFile({
      url: fullUrl,
      filePath: filePath,
      name: 'avatarFile',
      header: {
        'Authorization': token ? `Bearer ${token}` : '',
      },
      success: (res) => {
        try {
          let data: any;
          if (typeof res.data === 'string') {
            data = JSON.parse(res.data);
          } else {
            data = res.data;
          }
          
          if (res.statusCode >= 200 && res.statusCode < 300) {
            if (data && data.code === 0) {
              resolve(data.data);
            } else if (data && !data.code) {
              resolve(data);
            } else {
              reject(new Error(data?.message || 'Update failed'));
            }
          } else {
            reject(new Error(`Request failed with status ${res.statusCode}`));
          }
        } catch (error) {
          reject(new Error('Failed to parse response: ' + (error instanceof Error ? error.message : String(error))));
        }
      },
      fail: (error) => {
        reject(new Error(error.errMsg || 'Upload failed'));
      },
    });
    // #endif
  });
}

/**
 * Update user avatar with URL
 */
export function updateUserAvatar(avatarUrl: string): Promise<UserInfo> {
  return http<UserInfo>({
    url: '/api/user-auth/update-user-avatar',
    method: 'PATCH',
    data: { avatarUrl },
  });
}

