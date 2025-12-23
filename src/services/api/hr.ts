import { http } from "../http";

// ============ HR 信息 ============
export interface HrInfo {
  id: number;
  userId: number;
  companyId: number;
  companyName: string;
  realName: string;
  position: string;
  workPhone: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface HrInfoUpdatePayload {
  realName?: string;
  position?: string;
  workPhone?: string;
}

export interface RegisterHrParams {
  realName: string;
  position: string;
  workPhone: string;
}

interface PageResult<T> {
  records: T[];
  total?: number;
  size?: number;
  current?: number;
  pages?: number;
}

/**
 * Register HR information
 * @returns Operation result
 */
export function registerHr(params: RegisterHrParams): Promise<null> {
  const url = '/api/hr/info/register';
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
 * Get current HR information
 * @returns HR information
 */
export function getHrInfo(): Promise<HrInfo> {
  return http<HrInfo>({
    url: "/api/hr/info",
    method: "GET",
  });
}

/**
 * Update HR information
 * @returns Operation result
 */
export function updateHrInfo(payload: HrInfoUpdatePayload): Promise<null> {
  return http<null>({
    url: "/api/hr/info",
    method: "PATCH",
    data: payload,
  });
}

// ============ 职位相关 ============
export interface JobSkill {
  skillName: string;
  isRequired?: number; // 0/1
}

export interface JobPosition {
  id: number;
  companyId: number;
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
  internshipDaysPerWeek?: number;
  internshipDurationMonths?: number;
  description?: string;
  responsibilities?: string;
  requirements?: string;
  status: number; // 0-已下线 1-招聘中 2-已暂停
  viewCount?: number;
  applicationCount?: number;
  publishedAt?: string;
  createdAt?: string;
  updatedAt?: string;
  skills?: Array<JobSkill | string>;
}

export interface JobPositionCreatePayload {
  companyId: number;
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
  internshipDaysPerWeek?: number;
  internshipDurationMonths?: number;
  description: string;
  responsibilities?: string;
  requirements?: string;
  status?: number;
  skills?: JobSkill[];
}

export type JobPositionUpdatePayload = Partial<JobPositionCreatePayload>;

/**
 * Create job position
 * @returns Created job position ID
 */
export function createJobPosition(payload: JobPositionCreatePayload): Promise<number> {
  return http<number>({
    url: "/api/hr/job-position",
    method: "POST",
    data: payload,
  });
}

/**
 * Update job position
 * @returns Operation result
 */
export function updateJobPosition(jobId: number, payload: JobPositionUpdatePayload): Promise<null> {
  return http<null>({
    url: `/api/hr/job-position/${jobId}`,
    method: "PATCH",
    data: payload,
  });
}

/**
 * Get job position list
 * @returns List of job positions
 */
export function getJobPositionList(status?: number): Promise<JobPosition[]> {
  return http<JobPosition[]>({
    url: "/api/hr/job-position",
    method: "GET",
    data: typeof status === "number" ? { status } : undefined,
  });
}

/**
 * Get job position by ID
 * @returns Job position data
 */
export function getJobPositionById(jobId: number): Promise<JobPosition> {
  return http<JobPosition>({
    url: `/api/hr/job-position/${jobId}`,
    method: "GET",
  });
}

/**
 * Update job position status
 * @returns Operation result
 */
export function updateJobPositionStatus(jobId: number, status: number): Promise<null> {
  return http<null>({
    url: `/api/hr/job-position/${jobId}/status?status=${encodeURIComponent(status)}`,
    method: "PATCH",
  });
}

/**
 * Offline job position
 * @returns Operation result
 */
export function offlineJobPosition(jobId: number): Promise<null> {
  return http<null>({
    url: `/api/hr/job-position/${jobId}/offline`,
    method: "PATCH",
  });
}

// ============ HR 侧求职卡片 ============
export interface SeekerCard {
  userId?: number;
  username: string;
  graduationYear?: string;
  age?: number;
  highestEducation?: string;
  major?: string;
  jobStatus?: number;
  workExperienceYear?: number;
  internshipExperience?: boolean;
  university?: string;
  city?: string;
  avatarUrl?: string;
}

/**
 * Get seeker card by user ID
 * @returns Seeker card
 */
export function getSeekerCard(userId: number): Promise<SeekerCard> {
  return http<SeekerCard>({
    url: `/api/recruitment/hr/seeker-card?userId=${encodeURIComponent(userId)}`,
    method: "GET",
  });
}

// ============ HR 推荐求职者/推荐岗位 ============
export type PublicSeekerCard = SeekerCard & {
  id?: number;
};

/**
 * Get all seeker cards (for HR recommendations)
 * Backend route: GET /seeker/cards
 */
export function getPublicSeekerCards(): Promise<PublicSeekerCard[]> {
  return http<PublicSeekerCard[] | { records?: PublicSeekerCard[] }>({
    url: "/api/seeker/cards",
    method: "GET",
  }).then((resp) => {
    if (Array.isArray(resp)) return resp;
    return resp?.records ?? [];
  });
}

export interface RecommendJobPayload {
  jobId: number;
  seekerUserId: number;
  resumeId?: number;
  note?: string;
}

export interface ApplicationExistsPayload {
  jobId: number;
  seekerUserId: number;
}

export interface ApplicationExistsResult {
  exists: boolean;
  applicationId?: number;
}

/**
 * Check whether an application/recommend record already exists for this job & seeker.
 * Backend route: GET /recruitment/public/application/exists
 */
export function checkApplicationExists(payload: ApplicationExistsPayload): Promise<ApplicationExistsResult> {
  return http<unknown>({
    url: "/api/recruitment/public/application/exists",
    method: "GET",
    data: payload,
  }).then((resp) => {
    if (typeof resp === "boolean") return { exists: resp };
    if (typeof resp === "number") return { exists: resp > 0, applicationId: resp > 0 ? resp : undefined };
    if (typeof resp === "string") {
      const lowered = resp.trim().toLowerCase();
      if (lowered === "true" || lowered === "false") return { exists: lowered === "true" };
      const parsed = Number(resp);
      if (Number.isFinite(parsed)) return { exists: parsed > 0, applicationId: parsed > 0 ? parsed : undefined };
    }

    const maybeExists = (resp as any)?.exists ?? (resp as any)?.data ?? (resp as any)?.result;
    if (typeof maybeExists === "boolean") {
      const applicationIdRaw = (resp as any)?.applicationId ?? (resp as any)?.id;
      const parsed = Number(applicationIdRaw);
      return {
        exists: maybeExists,
        applicationId: Number.isFinite(parsed) && parsed > 0 ? parsed : undefined,
      };
    }

    const parsed = Number((resp as any)?.applicationId ?? (resp as any)?.id);
    if (Number.isFinite(parsed) && parsed > 0) return { exists: true, applicationId: parsed };

    return { exists: false };
  });
}

/**
 * Recommend a job to a seeker (HR action)
 */
export function recommendJob(payload: RecommendJobPayload): Promise<number> {
  return http<unknown>({
    url: "/api/recruitment/hr/recommend",
    method: "POST",
    data: payload,
  }).then((resp) => {
    if (typeof resp === "number") return resp;
    if (typeof resp === "string") {
      const parsed = Number(resp);
      if (Number.isFinite(parsed) && parsed > 0) return parsed;
    }

    const maybeId = (resp as any)?.applicationId ?? (resp as any)?.id ?? (resp as any)?.data;
    const parsed = Number(maybeId);
    if (Number.isFinite(parsed) && parsed > 0) return parsed;

    throw new Error("Invalid recommend response");
  });
}

// ============ 兼容旧页面的占位接口（仍可接入后端时复用） ============
export interface CandidateListItem {
  candidateId: number;
  name: string;
  position: string;
  status: string;
  overallMatch: number;
  skillMatch: number;
  experienceFit: number;
  cultureMatch: number;
  skills: string[];
  avatar?: string;
}

export interface PriorityListResponse {
  candidates: CandidateListItem[];
  summary: {
    newApplications: number;
    highMatch: number;
    awaitingFeedback: number;
    awaitingInterview: number;
  };
}

/**
 * Get priority list
 * @returns Priority list response
 */
export function getPriorityList(): Promise<PriorityListResponse> {
  return getApplications({ pageNum: 1, pageSize: 50 }).then((items) => {
    const summary = {
      newApplications: 0,
      highMatch: 0,
      awaitingFeedback: 0,
      awaitingInterview: 0,
    };
    const candidates = items.map((item) => {
      const score = typeof item.matchScore === "number" ? item.matchScore : Number(item.matchScore ?? 0);
      if (item.status === 0) summary.newApplications += 1;
      if (item.status === 1) summary.awaitingFeedback += 1;
      if (item.status === 2) summary.awaitingInterview += 1;
      if (!Number.isNaN(score) && score >= 80) summary.highMatch += 1;

      return {
        candidateId: item.id,
        name: item.jobSeekerName || "Candidate",
        position: item.jobTitle || "Position",
        status: getApplicationStatusLabel(item.status),
        overallMatch: Number.isNaN(score) ? 0 : Math.round(score),
        skillMatch: 0,
        experienceFit: 0,
        cultureMatch: 0,
        skills: [],
      };
    });

    return { candidates, summary };
  });
}

// ============ Applications ============ 
export interface ApplicationItem {
  id: number;
  jobId: number;
  jobTitle: string;
  jobSeekerId: number;
  jobSeekerName: string;
  resumeId: number;
  status: number;
  matchScore?: number;
  matchAnalysis?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface ApplicationQueryParams {
  pageNum?: number;
  pageSize?: number;
  jobId?: number;
  status?: number;
  keyword?: string;
}

/**
 * Get application list
 * @returns List of applications
 */
export function getApplications(params?: ApplicationQueryParams): Promise<ApplicationItem[]> {
  return http<PageResult<ApplicationItem>>({
    url: "/api/hr/application",
    method: "GET",
    data: params,
  }).then((page) => page?.records ?? []);
}

/**
 * Get application detail
 * @returns Application detail data
 */
export function getApplicationDetail(applicationId: number): Promise<ApplicationItem> {
  return http<ApplicationItem>({
    url: `/api/hr/application/${applicationId}`,
    method: "GET",
  });
}

/**
 * Update application status
 * @returns Operation result
 */
export function updateApplicationStatus(applicationId: number, status: number): Promise<null> {
  return http<null>({
    url: `/api/hr/application/${applicationId}/status`,
    method: "PATCH",
    data: { status },
  });
}

function getApplicationStatusLabel(status?: number): string {
  const map: Record<number, string> = {
    0: "Submitted",
    1: "Viewed",
    2: "Interview",
    3: "Interviewed",
    4: "Hired",
    5: "Rejected",
    6: "Withdrawn",
  };
  if (status === undefined || status === null) return "Unknown";
  return map[status] || "Unknown";
}

export type JobPosting = JobPosition;

/**
 * Get job posting by ID (alias)
 * @returns Job posting data
 */
export function getJobPosting(jobId: number): Promise<JobPosting> {
  return getJobPositionById(jobId);
}

/**
 * Get job list (alias)
 * @returns List of job postings
 */
export function getJobList(params?: { status?: number | "all" }): Promise<JobPosting[]> {
  const status = typeof params?.status === "number" ? params.status : undefined;
  return getJobPositionList(status);
}

/**
 * Create job posting (alias)
 * @returns Created job posting ID
 */
export function createJobPosting(payload: JobPositionCreatePayload): Promise<number> {
  return createJobPosition(payload);
}

/**
 * Update job posting (alias)
 * @returns Operation result
 */
export function updateJobPosting(jobId: number, payload: JobPositionUpdatePayload): Promise<null> {
  return updateJobPosition(jobId, payload);
}

/**
 * Publish job posting
 * @returns Operation result
 */
export function publishJobPosting(jobId: number): Promise<null> {
  // 后端未提供单独"发布"接口，使用状态更新为招聘中（1）
  return updateJobPositionStatus(jobId, 1);
}

/**
 * Sync placement channels
 * @returns Operation result
 */
export function syncPlacementChannels(): Promise<null> {
  // 未提供渠道同步接口，占位返回
  return Promise.resolve(null);
}
