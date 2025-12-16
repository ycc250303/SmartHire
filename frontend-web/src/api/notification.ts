import request from '@/utils/request'

/**
 * 发送通知给指定用户
 */
export const sendNotification = (userId: number, type: number, title: string, content: string) => {
  return request({
    url: '/admin/notifications/send',
    method: 'post',
    params: {
      userId,
      type,
      title,
      content
    }
  })
}

/**
 * 发送带关联信息的通知
 */
export const sendNotificationWithRelated = (
  userId: number,
  type: number,
  title: string,
  content: string,
  relatedId?: number,
  relatedType?: string
) => {
  return request({
    url: '/admin/notifications/send-with-related',
    method: 'post',
    params: {
      userId,
      type,
      title,
      content,
      relatedId,
      relatedType
    }
  })
}