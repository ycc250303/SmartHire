// 后端UserManagementDTO使用字符串userType，但JWT和User使用数字
export const USER_TYPE_STRING_TO_NUMBER: Record<string, number> = {
    'job_seeker': 1,
    'hr': 2,
    'admin': 3
}

export const USER_TYPE_NUMBER_TO_STRING: Record<number, string> = {
    1: 'job_seeker',
    2: 'hr',
    3: 'admin'
}

export const USER_TYPE_LABELS: Record<number, string> = {
    1: '求职者',
    2: 'HR',
    3: '管理员'
}

/**
 * 转换userType从字符串到数字
 * 用于处理后端UserManagementDTO返回的数据
 */
export function convertUserTypeToNumber(userType: string | number): number {
    if (typeof userType === 'number') return userType
    return USER_TYPE_STRING_TO_NUMBER[userType] || 1
}

/**
 * 转换userType从数字到字符串
 * 用于发送给后端的请求
 */
export function convertUserTypeToString(userType: string | number): string {
    if (typeof userType === 'string') return userType
    return USER_TYPE_NUMBER_TO_STRING[userType] || 'job_seeker'
}

/**
 * 获取用户类型的显示标签
 */
export function getUserTypeLabel(userType: string | number): string {
    const numType = typeof userType === 'string'
        ? convertUserTypeToNumber(userType)
        : userType
    return USER_TYPE_LABELS[numType] || '未知'
}

/**
 * 根据userType生成权限列表
 * @param userType 用户类型 (1=求职者, 2=HR, 3=管理员)
 * @returns 权限列表
 */
export function generatePermissionsByUserType(userType: number): string[] {
    switch (userType) {
        case 3: // 管理员
            return [
                'admin',
                'user:read',
                'user:write',
                'job:read',
                'job:write',
                'application:read',
                'application:write',
                'ban:read',
                'ban:write'
            ]
        case 2: // HR
            return ['job:read', 'job:write', 'application:read', 'application:write']
        case 1: // 求职者
            return ['profile:read', 'application:write', 'job:read']
        default:
            return []
    }
}
