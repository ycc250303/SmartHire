import { http, getApiBaseUrl } from '../http';

export interface Conversation {
  id: number;
  otherUserId: number;
  otherUserName: string;
  otherUserAvatar: string;
  lastMessage: string;
  lastMessageTime: string;
  unreadCount: number;
  pinned: number;
  hasNotification: number;
  applicationId?: number;
}

export interface Message {
  id: number;
  conversationId: number;
  senderId: number;
  receiverId: number;
  messageType: number;
  content: string;
  fileUrl: string | null;
  replyTo: number | null;
  isRead: number;
  createdAt: string;
  replyContent: string | null;
  replyMessageType: number | null;
}

export interface GetChatHistoryParams {
  conversationId: number;
  page?: number;
  size?: number;
}

export interface SendMessageParams {
  receiverId: number;
  applicationId?: number | null;
  messageType: number;
  content: string;
  fileUrl: null;
  replyTo: number | null;
}

/**
 * Get conversations list
 * @returns List of conversations
 */
export function getConversations(): Promise<Conversation[]> {
  const url = '/api/message/get-conversations';
  console.log('[Params]', url, null);
  return http<Conversation[]>({
    url,
    method: 'GET',
  }).then(response => {
    console.log('[Response]', url, response);
    return response;
  });
}

/**
 * Get chat history
 * @returns List of messages
 */
export function getChatHistory(params: GetChatHistoryParams): Promise<Message[]> {
  const queryParams: Record<string, any> = {
    conversationId: params.conversationId,
  };
  
  if (params.page !== undefined) {
    queryParams.page = params.page;
  }
  
  if (params.size !== undefined) {
    queryParams.size = params.size;
  }
  
  const queryString = Object.keys(queryParams)
    .map(key => `${key}=${encodeURIComponent(queryParams[key])}`)
    .join('&');
  
  const url = `/api/message/get-chat-history?${queryString}`;
  console.log('[Params]', url, params);
  return http<Message[]>({
    url,
    method: 'GET',
  }).then(response => {
    console.log('[Response]', url, response);
    return response;
  });
}

/**
 * Send message
 * @returns Sent message data
 */
export function sendMessage(params: SendMessageParams): Promise<Message> {
  const url = '/api/message/send-text';
  const requestData: Record<string, any> = {
    receiverId: params.receiverId,
    messageType: params.messageType,
    content: params.content,
    fileUrl: params.fileUrl,
    replyTo: params.replyTo,
  };
  const applicationId = typeof params.applicationId === 'number' ? params.applicationId : null;
  if (applicationId && applicationId > 0) {
    requestData.applicationId = applicationId;
  }
  console.log('[Params]', url, requestData);
  return http<Message>({
    url,
    method: 'POST',
    data: requestData,
  }).then(response => {
    console.log('[Response]', url, response);
    return response;
  });
}

/**
 * Mark messages as read
 * @returns Operation result
 */
export function markAsRead(conversationId: number): Promise<null> {
  const queryString = `conversationId=${encodeURIComponent(conversationId)}`;
  const url = `/api/message/read?${queryString}`;
  console.log('[Params]', url, { conversationId });
  return http<null>({
    url,
    method: 'PATCH',
  }).then(response => {
    console.log('[Response]', url, response);
    return response;
  });
}

/**
 * Pin or unpin conversation
 * @returns Operation result
 */
export function pinConversation(id: number, pinned: boolean): Promise<null> {
  const url = `/api/message/pin-conversation/${id}?pinned=${pinned}`;
  console.log('[Params]', url, { id, pinned });
  return http<null>({
    url,
    method: 'PATCH',
  }).then(response => {
    console.log('[Response]', url, response);
    return response;
  });
}

/**
 * Delete conversation
 * @returns Operation result
 */
export function deleteConversation(id: number): Promise<null> {
  const url = `/api/message/delete-conversation/${id}`;
  console.log('[Params]', url, { id });
  return http<null>({
    url,
    method: 'DELETE',
  }).then(response => {
    console.log('[Response]', url, response);
    return response;
  });
}

/**
 * Get unread message count
 * @returns Unread message count
 */
export function getUnreadCount(): Promise<number> {
  const url = '/api/message/get-unread-count';
  console.log('[Params]', url, null);
  return http<number>({
    url,
    method: 'GET',
  }).then(response => {
    console.log('[Response]', url, response);
    return response;
  });
}

export interface SendMediaParams {
  receiverId: number;
  applicationId?: number | null;
  messageType: number;
  filePath: string;
  content?: string;
}

/**
 * Send media message (image, file)
 * @returns Sent message data
 */
export function sendMedia(params: SendMediaParams): Promise<Message> {
  const apiPath = '/api/message/send-media';
  const baseUrl = getApiBaseUrl(apiPath);
  let normalizedBaseUrl = baseUrl.endsWith('/') ? baseUrl.slice(0, -1) : baseUrl;
  let normalizedPath = '/api/message/send-media';
  
  if (normalizedBaseUrl.endsWith('/api') && normalizedPath.startsWith('/api')) {
    normalizedPath = normalizedPath.replace(/^\/api/, '');
  }
  
  const fullUrl = `${normalizedBaseUrl}${normalizedPath}`;
  const token = uni.getStorageSync('auth_token');

  return new Promise((resolve, reject) => {
    const dto: Record<string, any> = {
      receiverId: params.receiverId,
      messageType: params.messageType,
    };
    // 添加 content（如果提供）
    if (params.content !== undefined) {
      dto.content = params.content;
    } else if (params.messageType === 2) {
      dto.content = '[图片]';
    }
    // 添加 applicationId（如果提供且大于 0）
    if (params.applicationId !== undefined && params.applicationId !== null) {
      const appId = typeof params.applicationId === 'number' ? params.applicationId : Number(params.applicationId);
      if (appId > 0) {
        dto.applicationId = appId;
      }
    }
  
    const dtoString = JSON.stringify(dto);
    console.log('[Params]', fullUrl, { params, dto, dtoString });
    // #ifdef H5
    if (params.filePath.startsWith('data:') || params.filePath.startsWith('blob:')) {
      fetch(params.filePath)
        .then(res => res.blob())
        .then(blob => {
          const formData = new FormData();
          
          let fileName = 'file';
          if (params.messageType === 2) {
            fileName = 'image.png';
          } else if (params.filePath.includes('pdf')) {
            fileName = 'document.pdf';
          }
          
          formData.append('file', blob, fileName);
          formData.append('payload', dtoString);
          formData.append('dto', dtoString);
          // 对于 H5 环境的 FormData 请求，不在 URL 中添加查询参数，只在 FormData 中发送
          
          const xhr = new XMLHttpRequest();
          xhr.open('POST', fullUrl, true);
          xhr.setRequestHeader('Authorization', token ? `Bearer ${token}` : '');
          // 不要设置 Content-Type，让浏览器自动设置（包含 boundary）
          
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
                  const errorMsg = data?.message || data?.detail || 'Send failed';
                  reject(new Error(errorMsg));
                }
              } else {
                const errorMsg = data?.message || data?.detail || `Request failed with status ${xhr.status}`;
                reject(new Error(errorMsg));
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
      // 对于非 H5 环境，使用 uni.uploadFile，URL 中可以包含查询参数
      const dtoEncoded = encodeURIComponent(dtoString);
      const uploadUrlWithQuery = fullUrl.includes('?')
        ? `${fullUrl}&dto=${dtoEncoded}&payload=${dtoEncoded}`
        : `${fullUrl}?dto=${dtoEncoded}&payload=${dtoEncoded}`;
      
      uni.uploadFile({
        url: uploadUrlWithQuery,
        filePath: params.filePath,
        name: 'file',
        formData: {
          payload: dtoString,
          dto: dtoString,
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
                reject(new Error(data?.message || 'Send failed'));
              }
            } else {
              reject(new Error(`Request failed with status ${res.statusCode}`));
            }
          } catch (error) {
            reject(new Error('Failed to parse response: ' + (error instanceof Error ? error.message : String(error))));
          }
        },
        fail: (error) => {
          const errMsg = error.errMsg || 'Upload failed';
          // 如果错误信息包含"参数格式错误"，提供更详细的错误信息
          if (errMsg.includes('参数格式错误') || errMsg.includes('参数')) {
            reject(new Error(`参数格式错误: ${JSON.stringify(dto)}`));
          } else {
            reject(new Error(errMsg));
          }
        },
      });
    }
    // #endif
    
    // #ifndef H5
    // 对于非 H5 环境（小程序等），使用 uni.uploadFile，URL 中可以包含查询参数
    const dtoEncoded = encodeURIComponent(dtoString);
    const uploadUrlWithQuery = fullUrl.includes('?')
      ? `${fullUrl}&dto=${dtoEncoded}&payload=${dtoEncoded}`
      : `${fullUrl}?dto=${dtoEncoded}&payload=${dtoEncoded}`;
    
    uni.uploadFile({
      url: uploadUrlWithQuery,
      filePath: params.filePath,
      name: 'file',
      formData: {
        payload: dtoString,
        dto: dtoString,
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
              reject(new Error(data?.message || data?.detail || 'Send failed'));
            }
          } else {
            const errorMsg = data?.message || data?.detail || `Request failed with status ${res.statusCode}`;
            reject(new Error(errorMsg));
          }
        } catch (error) {
          reject(new Error('Failed to parse response: ' + (error instanceof Error ? error.message : String(error))));
        }
      },
      fail: (error) => {
        const errMsg = error.errMsg || 'Upload failed';
        // 如果错误信息包含"参数格式错误"，提供更详细的错误信息
        if (errMsg.includes('参数格式错误') || errMsg.includes('参数')) {
          reject(new Error(`参数格式错误: ${JSON.stringify(dto)}`));
        } else {
          reject(new Error(errMsg));
        }
      },
    });
    // #endif
  });
}
