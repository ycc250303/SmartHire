import { http } from '../http';

export interface SubmitResumeParams {
  jobId: number;
  resumeId: number;
}

/**
 * Submit resume for job application
 * @returns Operation result
 */
export function submitResume(params: SubmitResumeParams): Promise<null> {
  const url = '/api/recruitment/seeker/submit-resume';
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
  const url = '/public/job-position/search';
  console.log('[Params]', url, params);
  return http<JobSearchResult[]>({
    url,
    method: 'POST',
    data: params,
    skipAuth: true,
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

