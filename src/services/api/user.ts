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
    // Try to use plus.io for App platform, or fallback to uni API
    try {
      // #ifdef APP-PLUS
      const fs = (plus as any).io.resolveLocalFileSystemURL(filePath, (entry: any) => {
        entry.file((file: any) => {
          const reader = new FileReader();
          reader.onloadend = () => {
            const result = reader.result as string;
            if (result) {
              const base64Data = result.split(',')[1];
              if (base64Data) {
                sendPatchRequest(base64Data);
              } else {
                reject(new Error('Failed to parse file data'));
              }
            } else {
              reject(new Error('Failed to read file'));
            }
          };
          reader.onerror = () => {
            reject(new Error('Failed to read file'));
          };
          reader.readAsDataURL(file);
        });
      }, (error: any) => {
        // Fallback to uni API
        readFileWithUni();
      });
      // #endif
      
      // #ifndef APP-PLUS
      readFileWithUni();
      // #endif
    } catch (error) {
      readFileWithUni();
    }
    
    function readFileWithUni() {
      try {
        const fsManager = uni.getFileSystemManager();
        if (!fsManager) {
          reject(new Error('FileSystemManager is not available'));
          return;
        }
        
        fsManager.readFile({
          filePath: filePath,
          encoding: 'base64',
          success: (readRes) => {
            const data = readRes.data;
            if (typeof data === 'string') {
              sendPatchRequest(data);
            } else {
              reject(new Error('Failed to read file as base64'));
            }
          },
          fail: (error) => {
            reject(new Error(error.errMsg || 'Failed to read file'));
          },
        });
      } catch (error) {
        reject(new Error('FileSystemManager error: ' + (error instanceof Error ? error.message : String(error))));
      }
    }
    
    function sendPatchRequest(base64Data: string) {
      console.log('Sending PATCH request with base64 data, length:', base64Data.length);
      
      // Try to use uni.uploadFile first (POST), if that fails, use uni.request (PATCH)
      // Note: uni.uploadFile only supports POST, but it's more efficient for file uploads
      uni.uploadFile({
        url: fullUrl,
        filePath: filePath,
        name: 'avatarFile',
        header: {
          'Authorization': token ? `Bearer ${token}` : '',
        },
        success: (res) => {
          console.log('Upload response status:', res.statusCode);
          console.log('Upload response data:', res.data);
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
                // If POST fails, try PATCH with base64
                sendPatchWithBase64(base64Data);
              }
            } else {
              // If POST fails, try PATCH with base64
              sendPatchWithBase64(base64Data);
            }
          } catch (error) {
            console.error('Upload response parse error:', error);
            sendPatchWithBase64(base64Data);
          }
        },
        fail: (error) => {
          console.log('UploadFile failed, trying PATCH with base64:', error);
          sendPatchWithBase64(base64Data);
        },
      });
    }
    
    function sendPatchWithBase64(base64Data: string) {
      const base64Url = `data:image/jpeg;base64,${base64Data}`;
      
      (uni.request as any)({
        url: fullUrl,
        method: 'PATCH',
        header: {
          'Content-Type': 'application/json',
          'Authorization': token ? `Bearer ${token}` : '',
        },
        data: {
          avatarFile: base64Url,
        },
        timeout: 30000,
        success: (res: any) => {
          console.log('PATCH request response status:', res.statusCode);
          console.log('PATCH request response data:', res.data);
          try {
            const response = res.data as any;
            if (res.statusCode >= 200 && res.statusCode < 300) {
              if (response && response.code === 0) {
                resolve(response.data);
              } else if (response && !response.code) {
                resolve(response);
              } else {
                reject(new Error(response?.message || 'Update failed'));
              }
            } else {
              reject(new Error(`Request failed with status ${res.statusCode}`));
            }
          } catch (error) {
            console.error('PATCH request response parse error:', res.data);
            reject(new Error('Failed to parse response: ' + (error instanceof Error ? error.message : String(error))));
          }
        },
        fail: (error: any) => {
          console.error('PATCH request error:', error);
          console.error('Upload URL was:', fullUrl);
          reject(new Error(error.errMsg || 'Upload failed'));
        },
      });
    }
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

