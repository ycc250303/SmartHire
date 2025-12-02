import { http } from '../http';

// ============ 候选人相关接口 ============

export interface CandidateListItem {
  candidateId: number;
  name: string;
  position: string;
  status: string; // 候选人池A、面试邀约、待经理沟通等
  overallMatch: number; // 总体匹配度
  skillMatch: number; // 技能匹配
  experienceFit: number; // 经验契合
  cultureMatch: number; // 文化匹配
  skills: string[]; // 技能标签
  avatar?: string; // 头像字符
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
 * 获取优先处理列表
 */
export function getPriorityList(): Promise<PriorityListResponse> {
  return http<PriorityListResponse>({
    url: '/api/hr/candidates/priority-list',
    method: 'GET',
  });
}

// ============ 候选人详情相关接口 ============

export interface CandidateDetail {
  candidateId: number;
  name: string;
  position: string;
  location: string;
  salary: string; // 如 "25-45K · 16薪"
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

/**
 * 获取候选人匹配详情
 */
export function getCandidateDetail(candidateId: number): Promise<CandidateDetail> {
  return http<CandidateDetail>({
    url: `/api/hr/candidates/${candidateId}/detail`,
    method: 'GET',
  });
}

// ============ 岗位相关接口 ============

export interface JobPosting {
  jobId: number;
  version: string; // 发布版本
  title: string;
  description: string;
  location: string; // 如 "上海·张江/远程"
  salary: string; // 如 "30-45K · 16薪"
  headcount: number; // HC数量
  exposureStatus: string; // 如 "金牌曝光位已锁定"
  status: 'draft' | 'published' | 'closed';
  autoSaved: boolean;
  coreFields: {
    jobTitle: {
      value: string;
      keywordHitRate: string; // 如 "4/5"
    };
    experienceRequirement: {
      value: string; // 如 "5-8年・ToB 产品策略"
      candidateHeat: string; // 如 "+12%"
    };
    salaryRange: {
      value: string;
      transparencyIndex: number;
    };
    jobSummary: {
      value: string;
      isAiGenerated: boolean;
    };
  };
  placementChannels: string[]; // 投放渠道
  keySkills: Array<{
    name: string;
    selected: boolean;
  }>;
  aiSuggestions: Array<{
    type: 'exposure' | 'salary' | 'other';
    title: string;
    content: string;
    linkText?: string;
    linkUrl?: string;
  }>;
  complianceReminder?: string;
}

/**
 * 获取岗位详情（用于编辑）
 */
export function getJobPosting(jobId: number): Promise<JobPosting> {
  return http<JobPosting>({
    url: `/api/hr/jobs/${jobId}`,
    method: 'GET',
  });
}

export interface JobListFilter {
  status?: JobPosting['status'] | 'all';
  keyword?: string;
  channel?: string;
}

/**
 * 获取岗位列表
 */
export function getJobList(params?: JobListFilter): Promise<JobPosting[]> {
  return http<JobPosting[]>({
    url: '/api/hr/jobs',
    method: 'GET',
    data: params,
  });
}

/**
 * 创建新岗位（草稿）
 */
export function createJobPosting(data: Partial<JobPosting>): Promise<JobPosting> {
  return http<JobPosting>({
    url: '/api/hr/jobs',
    method: 'POST',
    data,
  });
}

/**
 * 更新岗位
 */
export function updateJobPosting(jobId: number, data: Partial<JobPosting>): Promise<JobPosting> {
  return http<JobPosting>({
    url: `/api/hr/jobs/${jobId}`,
    method: 'PUT',
    data,
  });
}

/**
 * 发布岗位
 */
export function publishJobPosting(jobId: number): Promise<JobPosting> {
  return http<JobPosting>({
    url: `/api/hr/jobs/${jobId}/publish`,
    method: 'POST',
  });
}

export interface UpdateJobStatusPayload {
  status: JobPosting['status'];
  reason?: string;
}

/**
 * 更新岗位状态（如关闭、重新激活）
 */
export function updateJobStatus(jobId: number, payload: UpdateJobStatusPayload): Promise<JobPosting> {
  return http<JobPosting>({
    url: `/api/hr/jobs/${jobId}/status`,
    method: 'PUT',
    data: payload,
  });
}

/**
 * 同步投放渠道
 */
export function syncPlacementChannels(jobId: number, channels: string[]): Promise<null> {
  return http<null>({
    url: `/api/hr/jobs/${jobId}/sync-channels`,
    method: 'POST',
    data: { channels },
  });
}




