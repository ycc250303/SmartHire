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
  return http<Conversation[]>({
    url: '/api/message/get-conversations',
    method: 'GET',
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
  
  return http<Message[]>({
    url: `/api/message/get-chat-history?${queryString}`,
    method: 'GET',
  });
}

// Send message
export function sendMessage(params: SendMessageParams): Promise<Message> {
  return http<Message>({
    url: '/api/message/send-text',
    method: 'POST',
    data: {
      receiverId: params.receiverId,
      applicationId: params.applicationId,
      messageType: params.messageType,
      content: params.content,
      fileUrl: params.fileUrl,
      replyTo: params.replyTo,
    },
  });
}

// Mark messages as read
export function markAsRead(conversationId: number): Promise<null> {
  const queryString = `conversationId=${encodeURIComponent(conversationId)}`;
  return http<null>({
    url: `/api/message/read?${queryString}`,
    method: 'PATCH',
  });
}

// Pin or unpin conversation
export function pinConversation(id: number, pinned: boolean): Promise<null> {
  return http<null>({
    url: `/api/message/pin-conversation/${id}?pinned=${pinned}`,
    method: 'PATCH',
  });
}

// Delete conversation
export function deleteConversation(id: number): Promise<null> {
  return http<null>({
    url: `/api/message/delete-conversation/${id}`,
    method: 'DELETE',
  });
}

// Get unread message count
export function getUnreadCount(): Promise<number> {
  return http<number>({
    url: '/api/message/get-unread-count',
    method: 'GET',
  });
}
