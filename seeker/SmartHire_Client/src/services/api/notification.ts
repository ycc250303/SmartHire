import { http } from '../http';

export interface SystemNotification {
  id: number;
  type: number;
  typeName: string;
  title: string;
  content: string;
  isRead: number;
  createdAt: string;
  readAt: string | null;
}

export interface GetSystemNotificationsParams {
  page?: number;
  size?: number;
}

export interface SystemNotificationsResponse {
  code: number;
  message: string;
  data: SystemNotification[];
}

export interface SystemNotificationUnreadCountResponse {
  code: number;
  message: string;
  data: number;
}

/**
 * Get system notifications list
 * @returns List of system notifications
 */
export function getSystemNotifications(params?: GetSystemNotificationsParams): Promise<SystemNotification[]> {
  const url = '/api/system-notification/list';
  const requestData = params || { page: 1, size: 20 };
  return http<SystemNotification[]>({
    url,
    method: 'GET',
    data: requestData,
  }).then(response => {
    return Array.isArray(response) ? response : [];
  });
}

/**
 * Get system notification unread count
 * @returns Unread count
 */
export function getSystemNotificationUnreadCount(): Promise<number> {
  const url = '/api/system-notification/unread-count';
  return http<number>({
    url,
    method: 'GET',
    data: {},
  }).then(response => {
    return typeof response === 'number' ? response : 0;
  });
}

/**
 * Mark notification as read
 * @returns Operation result
 */
export function markNotificationAsRead(notificationId: number): Promise<null> {
  const url = `/api/system-notification/read/${notificationId}`;
  return http<null>({
    url,
    method: 'PATCH',
  });
}

/**
 * Mark all notifications as read
 * @returns Operation result
 */
export function markAllNotificationsAsRead(): Promise<null> {
  const url = '/api/system-notification/read-all';
  return http<null>({
    url,
    method: 'PATCH',
  });
}

/**
 * Delete notification
 * @returns Operation result
 */
export function deleteNotification(notificationId: number): Promise<null> {
  const url = `/api/system-notification/${notificationId}`;
  return http<null>({
    url,
    method: 'DELETE',
  });
}

