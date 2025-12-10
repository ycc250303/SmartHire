import { http } from '../http';

// Resume
export interface Resume {
  id: number;
  jobSeekerId: number;
  resumeName: string;
  privacyLevel: number;
  fileUrl: string;
  completeness: number;
  createdAt: string;
  updatedAt: string;
}

export interface UploadResumeParams {
  filePath: string;
  resumeName: string;
  privacyLevel: number;
}

export interface UpdateResumeParams {
  resumeName?: string;
  privacyLevel?: number;
}

/**
 * Upload resume file (PDF only)
 */
export function uploadResume(params: UploadResumeParams): Promise<Resume> {
  const baseUrl = import.meta.env.VITE_API_BASE_URL || "";
  let normalizedBaseUrl = baseUrl.endsWith('/') ? baseUrl.slice(0, -1) : baseUrl;
  let normalizedPath = '/api/seeker/upload-resume';
  
  if (normalizedBaseUrl.endsWith('/api') && normalizedPath.startsWith('/api')) {
    normalizedPath = normalizedPath.replace(/^\/api/, '');
  }
  
  const fullUrl = `${normalizedBaseUrl}${normalizedPath}`;
  const token = uni.getStorageSync('auth_token');

  return new Promise((resolve, reject) => {
    // #ifdef H5
    if (params.filePath.startsWith('data:') || params.filePath.startsWith('blob:')) {
      fetch(params.filePath)
        .then(res => res.blob())
        .then(blob => {
          const formData = new FormData();
          formData.append('resumeFile', blob);
          formData.append('resumeName', params.resumeName);
          formData.append('privacyLevel', params.privacyLevel.toString());
          
          const xhr = new XMLHttpRequest();
          xhr.open('POST', fullUrl, true);
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
                  reject(new Error(data?.message || 'Upload failed'));
                }
              } else {
                reject(new Error(`Request failed with status ${xhr.status}`));
              }
            } catch (error) {
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
    } else {
      uni.uploadFile({
        url: fullUrl,
        filePath: params.filePath,
        name: 'resumeFile',
        formData: {
          resumeName: params.resumeName,
          privacyLevel: params.privacyLevel.toString(),
        },
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
                reject(new Error(data?.message || 'Upload failed'));
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
    }
    // #endif
    
    // #ifndef H5
    uni.uploadFile({
      url: fullUrl,
      filePath: params.filePath,
      name: 'resumeFile',
      formData: {
        resumeName: params.resumeName,
        privacyLevel: params.privacyLevel.toString(),
      },
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
              reject(new Error(data?.message || 'Upload failed'));
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
 * Get all resumes
 */
export function getResumes(): Promise<Resume[]> {
  return http<Resume[]>({
    url: '/api/seeker/get-resumes',
    method: 'GET',
  });
}

/**
 * Update resume
 */
export function updateResume(id: number, params: UpdateResumeParams): Promise<Resume> {
  return http<Resume>({
    url: `/api/seeker/update-resume/${id}`,
    method: 'PATCH',
    data: params,
  });
}

/**
 * Delete resume
 */
export function deleteResume(id: number): Promise<null> {
  return http<null>({
    url: `/api/seeker/delete-resume/${id}`,
    method: 'DELETE',
  });
}

