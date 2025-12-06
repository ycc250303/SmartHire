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

export function getHrInfo(): Promise<HrInfo> {
  return http<HrInfo>({
    url: "/api/hr/info",
    method: "GET",
  });
}

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
  description?: string;
  responsibilities?: string;
  requirements?: string;
  status: number; // 0-已下线 1-招聘中 2-已暂停
  viewCount?: number;
  applicationCount?: number;
  publishedAt?: string;
  createdAt?: string;
  updatedAt?: string;
  skills?: JobSkill[];
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
  description: string;
  responsibilities?: string;
  requirements?: string;
  status?: number;
  skills?: JobSkill[];
}

export type JobPositionUpdatePayload = Partial<JobPositionCreatePayload>;

export function createJobPosition(payload: JobPositionCreatePayload): Promise<number> {
  return http<number>({
    url: "/api/hr/job-position",
    method: "POST",
    data: payload,
  });
}

export function updateJobPosition(jobId: number, payload: JobPositionUpdatePayload): Promise<null> {
  return http<null>({
    url: `/api/hr/job-position/${jobId}`,
    method: "PATCH",
    data: payload,
  });
}

export function getJobPositionList(status?: number): Promise<JobPosition[]> {
  return http<JobPosition[]>({
    url: "/api/hr/job-position",
    method: "GET",
    data: typeof status === "number" ? { status } : undefined,
  });
}

export function getJobPositionById(jobId: number): Promise<JobPosition> {
  return http<JobPosition>({
    url: `/api/hr/job-position/${jobId}`,
    method: "GET",
  });
}

export function updateJobPositionStatus(jobId: number, status: number): Promise<null> {
  return http<null>({
    url: `/api/hr/job-position/${jobId}/status`,
    method: "PATCH",
    data: { status },
  });
}

export function offlineJobPosition(jobId: number): Promise<null> {
  return http<null>({
    url: `/api/hr/job-position/${jobId}/offline`,
    method: "PATCH",
  });
}

// ============ HR 侧求职卡片 ============
export interface SeekerCard {
  username: string;
  graduationYear?: string;
  age?: number;
  highestEducation?: string;
  major?: string;
  jobStatus?: number;
  userId?: number;
}

export function getSeekerCards(params?: { userId?: number }): Promise<SeekerCard[]> {
  return http<SeekerCard[]>({
    url: "/api/recruitment/hr/seeker-card",
    method: "GET",
    data: params,
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

export function getPriorityList(): Promise<PriorityListResponse> {
  return http<PriorityListResponse>({
    url: "/api/hr/candidates/priority-list",
    method: "GET",
  });
}

export interface CandidateDetail {
  candidateId: number;
  name: string;
  position: string;
  location: string;
  salary: string;
  overallMatch: number;
  technicalMatch: {
    score: number;
    description: string;
  };
  experienceAlignment: {
    score: number;
    description: string;
  };
  gapReminder: {
    description: string;
  };
  skillMatrix: Array<{
    skillName: string;
    candidateProficiency: number;
    targetProficiency: number;
    overallMatch: number;
  }>;
  aiAnalysis: {
    summary: string;
  };
}

export function getCandidateDetail(candidateId: number): Promise<CandidateDetail> {
  return http<CandidateDetail>({
    url: `/api/hr/candidates/${candidateId}/detail`,
    method: "GET",
  });
}

// ============ 旧命名兼容（新接口别名） ============
export type JobPosting = JobPosition;

export function getJobPosting(jobId: number): Promise<JobPosting> {
  return getJobPositionById(jobId);
}

export function getJobList(params?: { status?: number | "all" }): Promise<JobPosting[]> {
  const status = typeof params?.status === "number" ? params.status : undefined;
  return getJobPositionList(status);
}

export function createJobPosting(payload: JobPositionCreatePayload): Promise<number> {
  return createJobPosition(payload);
}

export function updateJobPosting(jobId: number, payload: JobPositionUpdatePayload): Promise<null> {
  return updateJobPosition(jobId, payload);
}

export function publishJobPosting(jobId: number): Promise<null> {
  // 后端未提供单独“发布”接口，使用状态更新为招聘中（1）
  return updateJobPositionStatus(jobId, 1);
}

export function syncPlacementChannels(): Promise<null> {
  // 未提供渠道同步接口，占位返回
  return Promise.resolve(null);
}
