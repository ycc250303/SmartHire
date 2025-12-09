import { http } from '../http';

export interface Conversation {
  id: string;
  userId: number;
  username: string;
  avatarUrl?: string;
  lastMessage?: string;
  lastMessageTime?: string;
  unreadCount: number;
  isPinned: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface Message {
  id: string;
  conversationId: string;
  senderId: number;
  receiverId: number;
  content: string;
  messageType: number;
  createdAt: string;
  isRead: number;
}

export interface GetChatHistoryParams {
  conversationId: string;
  page?: number;
  pageSize?: number;
}

export interface SendMessageParams {
  receiverId: number;
  content: string;
  messageType?: number;
}

export interface PinConversationParams {
  isPinned: number;
}

/**
 * Get conversations list
 */
export function getConversations(): Promise<Conversation[]> {
  return http<Conversation[]>({
    url: '/api/message/get-conversations',
    method: 'GET',
  });
}

/**
 * Get chat history
 */
export function getChatHistory(params: GetChatHistoryParams): Promise<Message[]> {
  const queryParams: Record<string, any> = {
    conversationId: params.conversationId,
  };
  
  if (params.page !== undefined) {
    queryParams.page = params.page;
  }
  
  if (params.pageSize !== undefined) {
    queryParams.pageSize = params.pageSize;
  }
  
  const queryString = Object.keys(queryParams)
    .map(key => `${key}=${encodeURIComponent(queryParams[key])}`)
    .join('&');
  
  return http<Message[]>({
    url: `/api/message/get-chat-history?${queryString}`,
    method: 'GET',
  });
}

/**
 * Send message
 */
export function sendMessage(params: SendMessageParams): Promise<Message> {
  return http<Message>({
    url: '/api/message/send-message',
    method: 'POST',
    data: {
      receiverId: params.receiverId,
      content: params.content,
      messageType: params.messageType || 0,
    },
  });
}

/**
 * Mark messages as read
 */
export function markAsRead(conversationId: string): Promise<null> {
  const queryString = `conversationId=${encodeURIComponent(conversationId)}`;
  return http<null>({
    url: `/api/message/read?${queryString}`,
    method: 'DELETE',
  });
}

/**
 * Pin or unpin conversation
 */
export function pinConversation(id: string, isPinned: number): Promise<null> {
  return http<null>({
    url: `/api/message/conversation/${id}/pin`,
    method: 'PATCH',
    data: { isPinned },
  });
}

/**
 * Delete conversation
 */
export function deleteConversation(id: string): Promise<null> {
  return http<null>({
    url: `/api/message/conversation/${id}`,
    method: 'PATCH',
  });
}

/**
 * Get unread message count
 */
export function getUnreadCount(): Promise<number> {
  return http<number>({
    url: '/api/message/unread-count',
    method: 'GET',
  });
}

