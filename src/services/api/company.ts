import { http } from '../http';

export interface CompanyDTO {
  companyName: string;
  description: string;
  companyScale: number;
  financingStage: number;
  industry: string;
  companyCreatedAt: string;
  registeredCapital: number;
}

export interface CompanyListItem {
  companyId: number;
  companyName: string;
}

export interface CompanySearchParams {
  current?: number;
  size?: number;
  keyword?: string;
}

export interface CompanySearchResponse {
  records: CompanyListItem[];
  total?: number;
  size?: number;
  current?: number;
  pages?: number;
}

/**
 * Create company
 * @returns Company ID
 */
export function createCompany(params: CompanyDTO): Promise<number> {
  const url = '/api/hr/company/company';
  console.log('[Params]', url, params);
  return http<number>({
    url,
    method: 'POST',
    data: params,
  }).then(response => {
    console.log('[Response]', url, response);
    return response;
  });
}

/**
 * Search companies
 * @returns Company list
 */
export function searchCompanies(params?: CompanySearchParams): Promise<CompanySearchResponse> {
  const queryParams: string[] = [];
  if (params?.current !== undefined) queryParams.push(`current=${params.current}`);
  if (params?.size !== undefined) queryParams.push(`size=${params.size}`);
  if (params?.keyword) queryParams.push(`keyword=${encodeURIComponent(params.keyword)}`);
  const queryString = queryParams.length > 0 ? `?${queryParams.join('&')}` : '';
  const url = `/api/hr/company/companies${queryString}`;
  console.log('[Params]', url, params);
  return http<CompanySearchResponse>({
    url,
    method: 'GET',
  }).then(response => {
    console.log('[Response]', url, response);
    return response;
  });
}

// Company management
export interface UpdateCompanyInfoParams {
  companyId?: number;
  companyName?: string;
  companyLogo?: string;
  companyScale?: number;
  financingStage?: number;
  industry?: string;
  description?: string;
  website?: string;
}

export interface CompanyInfo {
  companyId: number;
  companyName?: string;
  companyLogo?: string;
  companyScale?: number;
  financingStage?: number;
  industry?: string;
  description?: string;
  website?: string;
}

/**
 * Get company information
 * @returns Company information data
 */
export function getCompanyInfo(companyId: number): Promise<CompanyInfo> {
  const url = `/api/hr/company/${companyId}/info`;
  console.log('[Params]', url, { companyId });
  return http<CompanyInfo>({
    url,
    method: 'GET',
  }).then(response => {
    console.log('[Response]', url, response);
    return response;
  });
}

/**
 * Update company information
 * @returns Operation result
 */
export function updateCompanyInfo(companyId: number, params: UpdateCompanyInfoParams): Promise<null> {
  const url = `/api/hr/company/${companyId}/info`;
  console.log('[Params]', url, { companyId, ...params });
  return http<null>({
    url,
    method: 'PUT',
    data: params,
  }).then(response => {
    console.log('[Response]', url, response);
    return response;
  });
}

// HR list
export interface GetHrListParams {
  current?: number;
  size?: number;
  keyword?: string;
}

export interface HrListItem {
  id: number;
  userId: number;
  companyId: number;
  companyName: string;
  realName: string;
  position: string;
  workPhone: string;
  createdAt: string;
  updatedAt: string;
}

export interface HrListResponse {
  records: HrListItem[];
  total: number;
  size: number;
  current: number;
  pages: number;
}

/**
 * Get company HR list
 * @returns HR list response
 */
export function getCompanyHrList(params?: GetHrListParams): Promise<HrListResponse> {
  const queryParams: string[] = [];
  if (params?.current !== undefined) queryParams.push(`current=${params.current}`);
  if (params?.size !== undefined) queryParams.push(`size=${params.size}`);
  if (params?.keyword) queryParams.push(`keyword=${encodeURIComponent(params.keyword)}`);
  const queryString = queryParams.length > 0 ? `?${queryParams.join('&')}` : '';
  const url = `/hr/company/hr-list${queryString}`;
  console.log('[Params]', url, params);
  return http<HrListResponse>({
    url,
    method: 'GET',
  }).then(response => {
    console.log('[Response]', url, response);
    return response;
  });
}

/**
 * Approve HR
 * @returns Operation result
 */
export function approveHr(hrId: number): Promise<null> {
  const url = `/hr/company/hr/${hrId}/approve`;
  console.log('[Params]', url, { hrId });
  return http<null>({
    url,
    method: 'POST',
  }).then(response => {
    console.log('[Response]', url, response);
    return response;
  });
}

/**
 * Reject HR
 * @returns Operation result
 */
export function rejectHr(hrId: number, rejectReason: string): Promise<null> {
  const url = `/hr/company/hr/${hrId}/reject?rejectReason=${encodeURIComponent(rejectReason)}`;
  console.log('[Params]', url, { hrId, rejectReason });
  return http<null>({
    url,
    method: 'POST',
  }).then(response => {
    console.log('[Response]', url, response);
    return response;
  });
}

// Job audit
export interface GetJobAuditParams {
  current?: number;
  size?: number;
  status?: string;
  keyword?: string;
}

export interface JobAuditItem {
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
}

export interface JobAuditResponse {
  records: JobAuditItem[];
  total: number;
  size: number;
  current: number;
  pages: number;
}

/**
 * Get company job audit list
 * @returns Job audit list
 */
export function getCompanyJobAudit(params?: GetJobAuditParams): Promise<JobAuditResponse> {
  const queryParams: string[] = [];
  if (params?.current !== undefined) queryParams.push(`current=${params.current}`);
  if (params?.size !== undefined) queryParams.push(`size=${params.size}`);
  if (params?.status) queryParams.push(`status=${encodeURIComponent(params.status)}`);
  if (params?.keyword) queryParams.push(`keyword=${encodeURIComponent(params.keyword)}`);
  const queryString = queryParams.length > 0 ? `?${queryParams.join('&')}` : '';
  const url = `/hr/company/job-audit${queryString}`;
  console.log('[Params]', url, params);
  return http<JobAuditResponse>({
    url,
    method: 'GET',
  }).then(response => {
    console.log('[Response]', url, response);
    return response;
  });
}

/**
 * Approve job audit
 * @returns Operation result
 */
export function approveJobAudit(jobId: number): Promise<null> {
  const url = `/hr/company/job-audit/${jobId}/approve`;
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
 * Reject job audit
 * @returns Operation result
 */
export function rejectJobAudit(jobId: number, rejectReason?: string): Promise<null> {
  const queryString = rejectReason ? `?rejectReason=${encodeURIComponent(rejectReason)}` : '';
  const url = `/hr/company/job-audit/${jobId}/reject${queryString}`;
  console.log('[Params]', url, { jobId, rejectReason });
  return http<null>({
    url,
    method: 'POST',
  }).then(response => {
    console.log('[Response]', url, response);
    return response;
  });
}

/**
 * Request job modification
 * @returns Operation result
 */
export function requestJobModify(jobId: number, modifyReason: string): Promise<null> {
  const url = `/hr/company/job-audit/${jobId}/modify?modifyReason=${encodeURIComponent(modifyReason)}`;
  console.log('[Params]', url, { jobId, modifyReason });
  return http<null>({
    url,
    method: 'POST',
  }).then(response => {
    console.log('[Response]', url, response);
    return response;
  });
}

