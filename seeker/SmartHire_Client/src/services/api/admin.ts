import { http } from '../http';

// User ban
export interface BanUserParams {
  userId: number;
  banReason: string;
  banDurationType: string;
  banDays?: number;
  sendEmailNotification?: boolean;
  sendSystemNotification?: boolean;
  adminId: number;
  adminUsername: string;
}

export interface BanUserResponse {
  id: number;
  userId: number;
  username: string;
  email: string;
  userType: number;
  banReason: string;
  banType: string;
  banDays: number;
  banStartTime: string;
  banEndTime: string;
  banStatus: string;
  operatorId: number;
  operatorName: string;
  createdAt: string;
}

/**
 * Ban user account
 * @returns Ban user response data
 */
export function banUser(userId: number, params: BanUserParams): Promise<BanUserResponse> {
  const url = `/api/admin/users/${userId}/ban`;
  console.log('[Params]', url, params);
  return http<BanUserResponse>({
    url,
    method: 'POST',
    data: params,
  }).then(response => {
    console.log('[Response]', url, response);
    return response;
  });
}

/**
 * Unban user account
 * @returns Unban result
 */
export function unbanUser(userId: number, params?: { operatorId?: number; operatorName?: string; liftReason?: string }): Promise<boolean> {
  const queryParams: string[] = [];
  if (params?.operatorId !== undefined) queryParams.push(`operatorId=${params.operatorId}`);
  if (params?.operatorName) queryParams.push(`operatorName=${encodeURIComponent(params.operatorName)}`);
  if (params?.liftReason) queryParams.push(`liftReason=${encodeURIComponent(params.liftReason)}`);
  const queryString = queryParams.length > 0 ? `?${queryParams.join('&')}` : '';
  const url = `/api/admin/users/${userId}/unban${queryString}`;
  console.log('[Params]', url, { userId, ...params });
  return http<boolean>({
    url,
    method: 'POST',
  }).then(response => {
    console.log('[Response]', url, response);
    return response;
  });
}

/**
 * Check user ban status
 * @returns Ban status
 */
export function checkUserBanStatus(userId: number): Promise<boolean> {
  const url = `/api/admin/users/${userId}/ban-status`;
  console.log('[Params]', url, { userId });
  return http<boolean>({
    url,
    method: 'GET',
  }).then(response => {
    console.log('[Response]', url, response);
    return response;
  });
}

// User management
export interface GetUsersParams {
  current?: number;
  size?: number;
}

/**
 * Get paginated user list
 * @returns User list response
 */
export function getUsers(params?: GetUsersParams): Promise<any> {
  const url = '/api/admin/users';
  const requestData = params || { current: 1, size: 20 };
  console.log('[Params]', url, requestData);
  return http<any>({
    url,
    method: 'GET',
    data: requestData,
  }).then(response => {
    console.log('[Response]', url, response);
    return response;
  });
}

/**
 * Get user detail by userId
 * @returns User detail
 */
export function getUserDetail(userId: number): Promise<any> {
  const url = `/api/admin/users/${userId}`;
  console.log('[Params]', url, { userId });
  return http<any>({
    url,
    method: 'GET',
  }).then(response => {
    console.log('[Response]', url, response);
    return response;
  });
}

// Notifications
export interface SendNotificationParams {
  userId?: number;
  type?: number;
  title?: string;
  content?: string;
}

/**
 * Send notification to user
 * @returns Send result
 */
export function sendNotification(params: SendNotificationParams): Promise<boolean> {
  const queryParams: string[] = [];
  if (params.userId !== undefined) queryParams.push(`userId=${params.userId}`);
  if (params.type !== undefined) queryParams.push(`type=${params.type}`);
  if (params.title) queryParams.push(`title=${encodeURIComponent(params.title)}`);
  if (params.content) queryParams.push(`content=${encodeURIComponent(params.content)}`);
  const queryString = queryParams.length > 0 ? `?${queryParams.join('&')}` : '';
  const url = `/api/admin/notifications/send${queryString}`;
  console.log('[Params]', url, params);
  return http<boolean>({
    url,
    method: 'POST',
  }).then(response => {
    console.log('[Response]', url, response);
    return response;
  });
}

export interface SendNotificationWithRelatedParams {
  userId?: number;
  type?: number;
  title?: string;
  content?: string;
  relatedId?: number;
  relatedType?: string;
}

/**
 * Send notification with related information
 * @returns Send result
 */
export function sendNotificationWithRelated(params: SendNotificationWithRelatedParams): Promise<boolean> {
  const queryParams: string[] = [];
  if (params.userId !== undefined) queryParams.push(`userId=${params.userId}`);
  if (params.type !== undefined) queryParams.push(`type=${params.type}`);
  if (params.title) queryParams.push(`title=${encodeURIComponent(params.title)}`);
  if (params.content) queryParams.push(`content=${encodeURIComponent(params.content)}`);
  if (params.relatedId !== undefined) queryParams.push(`relatedId=${params.relatedId}`);
  if (params.relatedType) queryParams.push(`relatedType=${encodeURIComponent(params.relatedType)}`);
  const queryString = queryParams.length > 0 ? `?${queryParams.join('&')}` : '';
  const url = `/api/admin/notifications/send-with-related${queryString}`;
  console.log('[Params]', url, params);
  return http<boolean>({
    url,
    method: 'POST',
  }).then(response => {
    console.log('[Response]', url, response);
    return response;
  });
}

// Job review
export interface GetReviewJobsParams {
  current?: number;
  size?: number;
}

export interface ReviewJobItem {
  id: number;
  title: string;
  company: string;
  location: string;
  salary: string;
  experience: string;
  education: string;
  description: string;
  status: string;
  publisher: string;
  createTime: string;
  tags: string[];
  hrUserId?: number;
}

export interface ReviewJobsResponse {
  records: ReviewJobItem[];
  total: number;
  size: number;
  current: number;
  pages: number;
}

/**
 * Get job review list
 * @returns Job review list
 */
export function getReviewJobs(params?: GetReviewJobsParams): Promise<ReviewJobsResponse> {
  const url = '/api/admin/review/jobs';
  const requestData = params || { current: 1, size: 20 };
  console.log('[Params]', url, requestData);
  return http<ReviewJobsResponse>({
    url,
    method: 'GET',
    data: requestData,
  }).then(response => {
    console.log('[Response]', url, response);
    return response;
  });
}

/**
 * Approve job review
 * @returns Operation result
 */
export function approveJobReview(jobId: number, params?: { reason?: string }): Promise<null> {
  const url = `/api/admin/review/jobs/${jobId}/approve`;
  console.log('[Params]', url, { jobId, ...params });
  return http<null>({
    url,
    method: 'POST',
    data: params,
  }).then(response => {
    console.log('[Response]', url, response);
    return response;
  });
}

/**
 * Reject job review
 * @returns Operation result
 */
export function rejectJobReview(jobId: number, params: { reason: string }): Promise<null> {
  const url = `/api/admin/review/jobs/${jobId}/reject`;
  console.log('[Params]', url, { jobId, ...params });
  return http<null>({
    url,
    method: 'POST',
    data: params,
  }).then(response => {
    console.log('[Response]', url, response);
    return response;
  });
}

/**
 * Get job review detail
 * @returns Job review detail
 */
export function getJobReviewDetail(jobId: number): Promise<any> {
  const url = `/api/admin/review/jobs/${jobId}`;
  console.log('[Params]', url, { jobId });
  return http<any>({
    url,
    method: 'GET',
  }).then(response => {
    console.log('[Response]', url, response);
    return response;
  });
}

/**
 * Offline job
 * @returns Operation result
 */
export function offlineJob(jobId: number, params: { reason: string }): Promise<null> {
  const url = `/api/admin/review/jobs/${jobId}/offline`;
  console.log('[Params]', url, { jobId, ...params });
  return http<null>({
    url,
    method: 'POST',
    data: params,
  }).then(response => {
    console.log('[Response]', url, response);
    return response;
  });
}

/**
 * Get job review count by status
 * @returns Count number
 */
export function getJobReviewCount(status: string): Promise<number> {
  const url = `/api/admin/review/count/${status}`;
  console.log('[Params]', url, { status });
  return http<number>({
    url,
    method: 'GET',
  }).then(response => {
    console.log('[Response]', url, response);
    return response;
  });
}

// Reports
export interface GetReportsParams {
  current?: number;
  size?: number;
}

export interface ReportItem {
  id: number;
  reporterId: number;
  reporterName: string;
  reporterType: number;
  targetType: number;
  targetId: number;
  targetTitle: string;
  reportType: number;
  reason: string;
  status: number;
  handlerId: number;
  handleResult: number;
  handleReason: string;
  handleTime: string;
  evidenceImage: string | null;
  createdAt: string;
  updatedAt: string;
  targetUser: any | null;
  targetJob: any | null;
}

export interface ReportsResponse {
  records: ReportItem[];
  total: number;
  size: number;
  current: number;
  pages: number;
}

/**
 * Get reports list
 * @returns Reports list
 */
export function getReports(params?: GetReportsParams): Promise<ReportsResponse> {
  const url = '/api/admin/reports';
  const requestData = params || { current: 1, size: 20 };
  console.log('[Params]', url, requestData);
  return http<ReportsResponse>({
    url,
    method: 'GET',
    data: requestData,
  }).then(response => {
    console.log('[Response]', url, response);
    return response;
  });
}

/**
 * Get report detail by id
 * @returns Report detail
 */
export function getReportDetail(id: number): Promise<any> {
  const url = `/api/admin/reports/${id}`;
  console.log('[Params]', url, { id });
  return http<any>({
    url,
    method: 'GET',
  }).then(response => {
    console.log('[Response]', url, response);
    return response;
  });
}

export interface HandleReportParams {
  handleResult: number;
  handleReason: string;
  reporterNotificationContent?: string;
  targetNotificationContent?: string;
  banInfo?: {
    banType: string;
    banDays: number;
  };
}

/**
 * Handle report
 * @returns Operation result
 */
export function handleReport(id: number, params: HandleReportParams, operatorId?: string): Promise<null> {
  const queryString = operatorId ? `?operatorId=${operatorId}` : '';
  const url = `/api/admin/reports/${id}/handle${queryString}`;
  console.log('[Params]', url, { id, ...params, operatorId });
  return http<null>({
    url,
    method: 'PUT',
    data: params,
  }).then(response => {
    console.log('[Response]', url, response);
    return response;
  });
}

export interface ReportsStats {
  total: number;
  pendingCount: number;
  resolvedCount: number;
}

/**
 * Get reports statistics
 * @returns Reports statistics
 */
export function getReportsStats(): Promise<ReportsStats> {
  const url = '/api/admin/reports/stats';
  console.log('[Params]', url, null);
  return http<ReportsStats>({
    url,
    method: 'GET',
  }).then(response => {
    console.log('[Response]', url, response);
    return response;
  });
}

/**
 * Download report evidence image
 * @returns Evidence image data
 */
export function downloadReportEvidence(id: number): Promise<any> {
  const url = `/api/admin/reports/${id}/evidence`;
  console.log('[Params]', url, { id });
  return http<any>({
    url,
    method: 'GET',
  }).then(response => {
    console.log('[Response]', url, response);
    return response;
  });
}

// Alternative admin review endpoints (without /smarthire/api prefix)
export interface GetAdminReviewJobsParams {
  status?: string;
  keyword?: string;
  current?: number;
  size?: number;
}

/**
 * Get admin review jobs list (alternative endpoint)
 * @returns Job review list
 */
export function getAdminReviewJobs(params?: GetAdminReviewJobsParams): Promise<ReviewJobsResponse> {
  const queryParams: string[] = [];
  if (params?.status) queryParams.push(`status=${encodeURIComponent(params.status)}`);
  if (params?.keyword) queryParams.push(`keyword=${encodeURIComponent(params.keyword)}`);
  if (params?.current !== undefined) queryParams.push(`current=${params.current}`);
  if (params?.size !== undefined) queryParams.push(`size=${params.size}`);
  const queryString = queryParams.length > 0 ? `?${queryParams.join('&')}` : '';
  const url = `/admin/review/jobs${queryString}`;
  console.log('[Params]', url, params);
  return http<ReviewJobsResponse>({
    url,
    method: 'GET',
  }).then(response => {
    console.log('[Response]', url, response);
    return response;
  });
}

/**
 * Approve job review (alternative endpoint)
 * @returns Operation result
 */
export function approveAdminJobReview(jobId: number): Promise<null> {
  const url = `/admin/review/jobs/${jobId}/approve`;
  console.log('[Params]', url, { jobId });
  return http<null>({
    url,
    method: 'POST',
  }).then(response => {
    console.log('[Response]', url, response);
    return response;
  });
}

/**
 * Reject job review (alternative endpoint)
 * @returns Operation result
 */
export function rejectAdminJobReview(jobId: number, params: { reason: string }): Promise<null> {
  const url = `/admin/review/jobs/${jobId}/reject`;
  console.log('[Params]', url, { jobId, ...params });
  return http<null>({
    url,
    method: 'POST',
    data: params,
  }).then(response => {
    console.log('[Response]', url, response);
    return response;
  });
}

/**
 * Request job modification (alternative endpoint)
 * @returns Operation result
 */
export function requestAdminJobModify(jobId: number, params: { reason: string }): Promise<null> {
  const url = `/admin/review/jobs/${jobId}/modify`;
  console.log('[Params]', url, { jobId, ...params });
  return http<null>({
    url,
    method: 'POST',
    data: params,
  }).then(response => {
    console.log('[Response]', url, response);
    return response;
  });
}

/**
 * Get admin job review detail (alternative endpoint)
 * @returns Job review detail
 */
export function getAdminJobReviewDetail(jobId: number): Promise<any> {
  const url = `/admin/review/jobs/${jobId}`;
  console.log('[Params]', url, { jobId });
  return http<any>({
    url,
    method: 'GET',
  }).then(response => {
    console.log('[Response]', url, response);
    return response;
  });
}

/**
 * Get admin job review count by status (alternative endpoint)
 * @returns Count number
 */
export function getAdminJobReviewCount(status: string): Promise<number> {
  const url = `/admin/review/count/${status}`;
  console.log('[Params]', url, { status });
  return http<number>({
    url,
    method: 'GET',
  }).then(response => {
    console.log('[Response]', url, response);
    return response;
  });
}

