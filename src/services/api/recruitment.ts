import { http } from '../http';

export interface SubmitResumeParams {
  jobId: number;
  resumeId: number;
}

export interface SubmitResumeResponse {
  conversationId: number;
  applicationId: number;
}

export interface OfferRespondParams {
  messageId: number;
  response: number; // 1 accept, 2 reject
}

export interface InterviewRespondParams {
  messageId: number;
  response: number; // 1 accept, 2 reject
}

/**
 * Submit resume for job application
 * @returns Conversation and application info
 */
export function submitResume(params: SubmitResumeParams): Promise<SubmitResumeResponse> {
  const url = '/api/recruitment/seeker/submit-resume';
  console.log('[Params]', url, params);
  return http<SubmitResumeResponse>({
    url,
    method: 'POST',
    data: params,
  }).then(response => {
    console.log('[Response]', url, response);
    return response;
  });
}

/**
 * Respond to offer (accept / reject)
 */
export function respondOffer(params: OfferRespondParams): Promise<null> {
  const url = '/api/recruitment/seeker/offer/respond';
  console.log('[Params]', url, params);
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
 * Respond to interview invitation (accept / reject)
 */
export function respondInterview(params: InterviewRespondParams): Promise<null> {
  const url = '/api/recruitment/seeker/interview/respond';
  console.log('[Params]', url, params);
  return http<null>({
    url,
    method: 'POST',
    data: params,
  }).then(response => {
    console.log('[Response]', url, response);
    return response;
  });
}

export interface JobSearchParams {
  city?: string;
  jobType?: number;
  educationRequired?: number;
  minSalary?: number;
  maxSalary?: number;
  keyword?: string;
  skills?: string[];
  companyId?: number;
  page?: number;
  size?: number;
}

export interface JobSearchResult {
  jobId: number;
  jobTitle: string;
  salaryMin: number;
  salaryMax: number;
  city: string;
  address: string;
  companyName: string;
  companyScale: number;
  financingStage: number;
  hrName: string;
  hrAvatarUrl: string;
  jobType: number;
  educationRequired: number;
}

/**
 * Search job positions
 * @returns List of job search results
 */
export function searchJobPositions(params: JobSearchParams): Promise<JobSearchResult[]> {
  const url = '/api/public/job-position/search';
  console.log('[Params]', url, params);
  return http<JobSearchResult[]>({
    url,
    method: 'POST',
    data: params,
    // skipAuth: true,
  }).then(response => {
    console.log('[Response]', url, response);
    return response;
  });
}

export interface JobDetailCompany {
  companyId: number;
  companyName: string;
  companyLogo?: string;
  companyScale?: number;
  financingStage?: number;
  industry?: string;
  description?: string;
  // mainBusiness?: string;
  website?: string;
}

export interface JobDetailHr {
  hrId: number;
  realName: string;
  position?: string;
  avatarUrl?: string;
}

export interface JobDetailApplication {
  hasApplied: boolean;
  applicationId?: number;
  conversationId?: number;
  status?: number;
  appliedAt?: string;
}

export interface JobDetail {
  jobId: number;
  jobTitle: string;
  jobCategory?: string;
  department?: string;
  city: string;
  address?: string;
  salaryMin?: number;
  salaryMax?: number;
  salaryMonths?: number;
  educationRequired?: number;
  jobType: number;
  experienceRequired?: number;
  description?: string;
  responsibilities?: string;
  requirements?: string;
  skills?: string[];
  publishedAt?: string;
  company?: JobDetailCompany;
  hr?: JobDetailHr;
  application?: JobDetailApplication;
}

export interface SubmitReportParams {
  targetId: number;
  targetType: number;
  reportType: number;
  reason: string;
  evidenceImage?: string;
}

/**
 * Get job detail by ID
 * @returns Job detail data
 */
export function getJobDetail(jobId: number): Promise<JobDetail> {
  const url = `/api/recruitment/seeker/job-position/${jobId}`;
  console.log('[Params]', url, { jobId });
  return http<JobDetail>({
    url,
    method: 'GET',
  }).then(response => {
    console.log('[Response]', url, response);
    return response;
  });
}

/**
 * Submit report by seeker
 * @returns Operation result
 */
export function submitSeekerReport(params: SubmitReportParams): Promise<null> {
  const url = '/api/recruitment/seeker/report';
  console.log('[Params]', url, params);
  return http<null>({
    url,
    method: 'POST',
    data: params,
  }).then(response => {
    console.log('[Response]', url, response);
    return response;
  });
}

export interface ApplicationListItem {
  applicationId: number;
  jobId: number;
  jobTitle: string;
  companyName: string;
  companyLogo?: string;
  status: number;
  appliedAt: string;
  conversationId?: number;
  hrName?: string;
  hrAvatar?: string;
}

export interface ApplicationListResponse {
  total: number;
  page: number;
  size: number;
  list: ApplicationListItem[];
}

export interface GetApplicationsParams {
  status?: number;
  page?: number;
  size?: number;
}

/**
 * Get seeker's application list
 * @returns Application list
 */
export function getApplications(params?: GetApplicationsParams): Promise<ApplicationListResponse> {
  const queryParams: Record<string, any> = {};
  if (params?.status !== undefined) {
    queryParams.status = params.status;
  }
  if (params?.page !== undefined) {
    queryParams.page = params.page;
  }
  if (params?.size !== undefined) {
    queryParams.size = params.size;
  }
  
  const queryString = Object.keys(queryParams)
    .map(key => `${key}=${encodeURIComponent(queryParams[key])}`)
    .join('&');
  
  const url = `/api/recruitment/seeker/job-list${queryString ? `?${queryString}` : ''}`;
  console.log('[Params]', url, params);
  return http<ApplicationListResponse>({
    url,
    method: 'GET',
  }).then(response => {
    console.log('[Response]', url, response);
    return response;
  });
}

export interface AddApplicationCommentParams {
  comment: string;
}

/**
 * Add HR comment to application
 * @returns Operation result
 */
export function addApplicationComment(applicationId: number, params: AddApplicationCommentParams): Promise<null> {
  const url = `/api/recruitment/hr/application/${applicationId}/comment?comment=${encodeURIComponent(params.comment)}`;
  console.log('[Params]', url, params);
  return http<null>({
    url,
    method: 'PATCH',
    data: params,
  }).then(response => {
    console.log('[Response]', url, response);
    return response;
  });
}

