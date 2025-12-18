import { http, getApiBaseUrl } from '../http';

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
  fileName?: string;
}

export interface UpdateResumeParams {
  resumeName?: string;
  privacyLevel?: number;
}

/**
 * Upload resume file
 * @returns Uploaded resume data
 */
export function uploadResume(params: UploadResumeParams): Promise<Resume> {
  const apiPath = '/api/seeker/upload-resume';
  const baseUrl = getApiBaseUrl(apiPath);
  let normalizedBaseUrl = baseUrl.endsWith('/') ? baseUrl.slice(0, -1) : baseUrl;
  let normalizedPath = '/api/seeker/upload-resume';
  
  if (normalizedBaseUrl.endsWith('/api') && normalizedPath.startsWith('/api')) {
    normalizedPath = normalizedPath.replace(/^\/api/, '');
  }
  
  const fullUrl = `${normalizedBaseUrl}${normalizedPath}`;
  const token = uni.getStorageSync('auth_token');

  console.log('[Params]', fullUrl, params);

  return new Promise((resolve, reject) => {
    // #ifdef H5
    if (params.filePath.startsWith('data:') || params.filePath.startsWith('blob:')) {
      fetch(params.filePath)
        .then(res => res.blob())
        .then(blob => {
          const formData = new FormData();
          const fileName = params.fileName || 'resume.pdf';
          formData.append('resumeFile', blob, fileName);
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
                  console.log('[Response]', fullUrl, data.data);
                  resolve(data.data);
                } else if (data && !data.code) {
                  console.log('[Response]', fullUrl, data);
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
                console.log('[Response]', fullUrl, data.data);
                resolve(data.data);
              } else if (data && !data.code) {
                console.log('[Response]', fullUrl, data);
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
              console.log('[Response]', fullUrl, data.data);
              resolve(data.data);
            } else if (data && !data.code) {
              console.log('[Response]', fullUrl, data);
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
 * @returns List of resumes
 */
export function getResumes(): Promise<Resume[]> {
  const url = '/api/seeker/get-resumes';
  console.log('[Params]', url, null);
  return http<Resume[]>({
    url,
    method: 'GET',
  }).then(response => {
    console.log('[Response]', url, response);
    return response;
  });
}

/**
 * Update resume
 * @returns Updated resume data
 */
export function updateResume(id: number, params: UpdateResumeParams): Promise<Resume> {
  const url = `/api/seeker/update-resume/${id}`;
  console.log('[Params]', url, { id, ...params });
  return http<Resume>({
    url,
    method: 'PATCH',
    data: params,
  }).then(response => {
    console.log('[Response]', url, response);
    return response;
  });
}

/**
 * Delete resume
 * @returns Operation result
 */
export function deleteResume(id: number): Promise<null> {
  const url = `/api/seeker/delete-resume/${id}`;
  console.log('[Params]', url, { id });
  return http<null>({
    url,
    method: 'DELETE',
  }).then(response => {
    console.log('[Response]', url, response);
    return response;
  });
}

