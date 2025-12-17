import { http } from '../http';

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
  applicationId: number;
  messageType: number;
  content: string;
  fileUrl: null;
  replyTo: number | null;
}

// Get conversations list
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

// Get chat history
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

// Send message
export function sendMessage(params: SendMessageParams): Promise<Message> {
  const url = '/api/message/send-text';
  const requestData = {
    receiverId: params.receiverId,
    applicationId: params.applicationId,
    messageType: params.messageType,
    content: params.content,
    fileUrl: params.fileUrl,
    replyTo: params.replyTo,
  };
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

// Mark messages as read
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

// Pin or unpin conversation
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

// Delete conversation
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

// Get unread message count
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
