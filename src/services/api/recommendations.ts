import { http, getApiPathFromUrl, getApiBaseUrl, getToken } from '../http';
import { getJobDetail, type JobDetail } from './recruitment';
import { getSeekerInfo, getSkills, getWorkExperiences, getProjectExperiences, getEducationExperiences, type SeekerInfo, type Skill, type WorkExperience, type ProjectExperience, type EducationExperience } from './seeker';

export enum Degree {
  HighSchoolOrBelow = 0,
  AssociateDegree = 1,
  BachelorDegree = 2,
  MasterDegree = 3,
  DoctorDegree = 4,
}

export interface InternJobItem {
  jobId: number;
  title: string;
  salary_min: number;
  salary_max: number;
  companyName?: string | null;
  city?: string | null;
  address?: string | null;
  work_months_min?: number | null;
  skills?: string[] | null;
  degrees?: Degree | null;
  matchScore?: number | null;
  jobCategory?: string | null;
  department?: string | null;
  salaryMonths?: number | null;
  educationRequired?: number | null;
  jobType?: number | null;
  experienceRequired?: number | null;
  description?: string | null;
  responsibilities?: string | null;
  requirements?: string | null;
  publishedAt?: string | null;
}

export interface InternJobRecommendationsResponse {
  jobs: InternJobItem[];
}

function mapJobDetailToInternJobItem(detail: JobDetail): Partial<InternJobItem> {
  const educationRequired = detail.educationRequired;
  let degrees: Degree | null = null;
  if (educationRequired !== null && educationRequired !== undefined) {
    if (educationRequired >= 0 && educationRequired <= 4) {
      degrees = educationRequired as Degree;
    }
  }
  
  return {
    title: detail.jobTitle,
    salary_min: detail.salaryMin ?? 0,
    salary_max: detail.salaryMax ?? 0,
    companyName: detail.company?.companyName ?? null,
    city: detail.city ?? null,
    address: detail.address ?? null,
    skills: detail.skills ?? null,
    degrees: degrees,
    jobCategory: detail.jobCategory ?? null,
    department: detail.department ?? null,
    salaryMonths: detail.salaryMonths ?? null,
    educationRequired: educationRequired ?? null,
    jobType: detail.jobType ?? null,
    experienceRequired: detail.experienceRequired ?? null,
    description: detail.description ?? null,
    responsibilities: detail.responsibilities ?? null,
    requirements: detail.requirements ?? null,
    publishedAt: detail.publishedAt ?? null,
  };
}

async function supplementJobItem(job: InternJobItem): Promise<InternJobItem> {
  const needsSupplement = 
    !job.companyName || 
    !job.city || 
    !job.address || 
    !job.skills || 
    job.skills.length === 0 ||
    job.degrees === null || 
    job.degrees === undefined;
  
  const needsMatchScore = job.matchScore === null || job.matchScore === undefined;

  if (!needsSupplement && !needsMatchScore) {
    return job;
  }

  try {
    const detail = await getJobDetail(job.jobId);
    const supplement = mapJobDetailToInternJobItem(detail);
    
    const javaMatchScore = (detail as any).matchScore ?? (detail as any).match_score ?? null;
    
    return {
      ...job,
      title: supplement.title ?? job.title,
      salary_min: supplement.salary_min ?? job.salary_min,
      salary_max: supplement.salary_max ?? job.salary_max,
      companyName: supplement.companyName ?? job.companyName,
      city: supplement.city ?? job.city,
      address: supplement.address ?? job.address,
      skills: supplement.skills ?? job.skills,
      degrees: supplement.degrees ?? job.degrees,
      jobCategory: supplement.jobCategory ?? job.jobCategory,
      department: supplement.department ?? job.department,
      salaryMonths: supplement.salaryMonths ?? job.salaryMonths,
      educationRequired: supplement.educationRequired ?? job.educationRequired,
      jobType: supplement.jobType ?? job.jobType,
      experienceRequired: supplement.experienceRequired ?? job.experienceRequired,
      description: supplement.description ?? job.description,
      responsibilities: supplement.responsibilities ?? job.responsibilities,
      requirements: supplement.requirements ?? job.requirements,
      publishedAt: supplement.publishedAt ?? job.publishedAt,
      matchScore: job.matchScore ?? javaMatchScore ?? null,
    };
  } catch (error) {
    console.warn(`Failed to supplement job ${job.jobId}:`, error);
    return job;
  }
}

interface BatchMatchScoreRequest {
  seekerProfile: {
    jobSeekerId?: number;
    major?: string;
    university?: string;
    highestEducation?: string;
    currentCity?: string;
    skills?: Array<{ name: string; level?: number }>;
    workExperiences?: Array<{ companyName?: string; position?: string; description?: string }>;
    projectExperiences?: Array<{ projectName?: string; description?: string; responsibilities?: string }>;
  };
  jobs: Array<{
    jobId: number;
    jobTitle?: string;
    description?: string;
    responsibilities?: string;
    requirements?: string;
    skills?: string[];
    educationRequired?: number;
  }>;
}

interface BatchMatchScoreResponse {
  scores: Array<{
    jobId: number;
    matchScore: number;
    similarity: number;
    details?: {
      skillMatch?: number;
      descriptionMatch?: number;
      educationMatch?: number;
    };
  }>;
}

async function batchCalculateMatchScores(
  jobs: InternJobItem[],
  seekerProfile: BatchMatchScoreRequest['seekerProfile']
): Promise<Map<number, number>> {
  if (jobs.length === 0) {
    return new Map();
  }

  try {
    const requestData: BatchMatchScoreRequest = {
      seekerProfile,
      jobs: jobs.map(job => ({
        jobId: job.jobId,
        jobTitle: job.title,
        description: job.description ?? undefined,
        responsibilities: job.responsibilities ?? undefined,
        requirements: job.requirements ?? undefined,
        skills: job.skills ?? undefined,
        educationRequired: job.educationRequired ?? undefined,
      })),
    };

    const url = '/api/recruitment/seeker/batch-calculate-match-scores';
    console.log('[Params]', url, { jobCount: jobs.length, seekerProfile });
    
    const response = await http<BatchMatchScoreResponse>({
      url,
      method: 'POST',
      data: requestData,
      timeout: 30000,
    });

    console.log('[Response]', url, { scoreCount: response.scores?.length || 0 });

    const scoreMap = new Map<number, number>();
    if (response.scores) {
      response.scores.forEach(score => {
        scoreMap.set(score.jobId, score.matchScore);
      });
    }
    return scoreMap;
  } catch (error) {
    console.warn('[Error] Failed to batch calculate match scores:', error);
    return new Map();
  }
}

export async function calculateMatchScore(jobId: number): Promise<number | null> {
  try {
    const seekerProfile = await buildSeekerProfile();
    const url = '/api/recruitment/seeker/calculate-match-score';
    console.log('[Params]', url, { jobId, seekerProfile });
    
    const response = await http<{ matchScore: number }>({
      url,
      method: 'POST',
      data: { jobId },
      timeout: 30000,
    });

    console.log('[Response]', url, response);
    return response.matchScore ?? null;
  } catch (error) {
    console.warn('[Error] Failed to calculate match score:', error);
    return null;
  }
}

async function buildSeekerProfile(): Promise<BatchMatchScoreRequest['seekerProfile']> {
  try {
    const [seekerInfo, skills, workExperiences, projectExperiences, educationExperiences] = await Promise.all([
      getSeekerInfo().catch(() => null),
      getSkills().catch(() => []),
      getWorkExperiences().catch(() => []),
      getProjectExperiences().catch(() => []),
      getEducationExperiences().catch(() => []),
    ]);

    const highestEducation = educationExperiences.length > 0
      ? educationExperiences.reduce((highest, edu) => 
          (edu.education > (highest?.education || 0)) ? edu : highest
        )
      : null;

    return {
      currentCity: seekerInfo?.currentCity,
      major: highestEducation?.major,
      university: highestEducation?.schoolName,
      highestEducation: highestEducation?.education?.toString(),
      skills: skills.map(skill => ({
        name: skill.skillName,
        level: skill.skillLevel,
      })),
      workExperiences: workExperiences.map(exp => ({
        companyName: exp.companyName,
        position: exp.position,
        description: exp.description,
      })),
      projectExperiences: projectExperiences.map(exp => ({
        projectName: exp.projectName,
        description: exp.description,
        responsibilities: exp.responsibilities,
      })),
    };
  } catch (error) {
    console.warn('[Error] Failed to build seeker profile:', error);
    return {};
  }
}

export async function getInternJobRecommendationsWithSupplement(userId?: number | null): Promise<InternJobRecommendationsResponse> {
  const response = await getInternJobRecommendations(userId);
  
  const seekerProfile = await buildSeekerProfile();
  const pyMatchScores = await batchCalculateMatchScores(response.jobs, seekerProfile);
  
  const jobsWithPyScores = response.jobs.map(job => ({
    ...job,
    matchScore: pyMatchScores.get(job.jobId) ?? job.matchScore ?? null,
  }));
  
  const supplementedJobs = await Promise.all(
    jobsWithPyScores.map(job => supplementJobItem(job))
  );
  
  return {
    jobs: supplementedJobs,
  };
}

export function getInternJobRecommendations(userId?: number | null): Promise<InternJobRecommendationsResponse> {
  const params: Record<string, any> = {};
  
  if (userId !== undefined && userId !== null) {
    params.userId = userId;
  }
  
  const url = '/api/recruitment/seeker/job-recommendations/intern';
  console.log('[Params]', url, params);
  return http<InternJobRecommendationsResponse>({
    url,
    method: 'GET',
    data: params,
  }).then(response => {
    console.log('[Response]', url, response);
    return response;
  });
}

export interface LearningResource {
  name: string;
  type: string;
  url?: string;
}

export interface StructuredSkillLearning {
  skill_name: string;
  priority: '高' | '中' | '低';
  reason: string;
  learning_steps: string[];
  resources: LearningResource[];
  estimated_weeks: number;
  difficulty: '简单' | '中等' | '困难';
}

export interface StructuredLearningPlan {
  skills: StructuredSkillLearning[];
}

export interface CareerPlanningAnalysisResponse {
  match_analysis: {
    overall_score: number;
    skill_match: number;
    education_match: number;
    experience_qualified: boolean;
  };
  gap_analysis: {
    skills: {
      required_missing: string[];
      optional_missing: string[];
      matched: Array<{
        name: string;
        your_level: number;
        is_required: boolean;
      }>;
      match_rate: number;
    };
    experience: {
      your_years: number;
      required_text: string;
      is_qualified: boolean;
      gap_years: number;
    };
    education: {
      your_text: string;
      required_text: string;
      is_qualified: boolean;
    };
  };
  career_roadmap?: {
    overview: {
      target_position: string;
      current_level: string;
      skill_gaps_count: number;
      plan_duration_days: number;
      milestones_count: number;
    };
    technology_stacks: Array<{
      category: string;
      skills: string[];
    }>;
    phase_roadmap: Array<{
      phase: number;
      title: string;
      description: string;
      duration_days: number;
      skills: string[];
      based_on_gap: string;
    }>;
    immediate_suggestions: Array<{
      title: string;
      description: string;
    }>;
  };
  learning_plan_structured?: StructuredLearningPlan;
}

export interface SSESteamCallbacks {
  onStart?: (message: string) => void;
  onChunk?: (content: string) => void;
  onDone?: () => void;
  onError?: (error: Error) => void;
}

export function getCareerPlanningAnalysis(jobId: number, forceRefresh?: boolean): Promise<CareerPlanningAnalysisResponse> {
  let url = `/api/ai/career-planning/${jobId}/analysis`;
  if (forceRefresh) {
    url += '?force_refresh=true';
  }
  console.log('[Params]', url, { jobId, forceRefresh });
  return http<CareerPlanningAnalysisResponse>({
    url,
    method: 'POST',
    timeout: 30000,
  }).then(response => {
    console.log('[Response]', url, response);
    return response;
  }).catch(error => {
    console.warn('[Error]', url, error);
    throw error;
  });
}

export function getCareerPlanningLearningPlanStream(
  jobId: number,
  callbacks: SSESteamCallbacks
): () => void {
  const url = `/api/ai/career-planning/${jobId}/learning-plan`;
  
  let cancelled = false;
  let abortController: AbortController | null = null;
  
  const cancel = () => {
    cancelled = true;
    if (abortController) {
      abortController.abort();
    }
  };
  
  const execute = async () => {
    console.log('[Params]', url, { jobId });
    // #ifdef H5
    try {
      const apiPath = getApiPathFromUrl(url);
      const baseUrl = getApiBaseUrl(apiPath);
      const token = getToken();
      
      const headers: Record<string, string> = {
        'Accept': 'text/event-stream',
      };
      if (token) {
        headers['Authorization'] = `Bearer ${token}`;
      }
      
      let normalizedBaseUrl = baseUrl.endsWith('/') ? baseUrl.slice(0, -1) : baseUrl;
      let normalizedPath = url.startsWith('/') ? url : `/${url}`;
      
      if (normalizedBaseUrl.endsWith('/api') && normalizedPath.startsWith('/api')) {
        normalizedPath = normalizedPath.replace(/^\/api/, '');
      }
      
      const fullUrl = normalizedBaseUrl ? `${normalizedBaseUrl}${normalizedPath}` : normalizedPath;
      
      abortController = new AbortController();
      
      const response = await fetch(fullUrl, {
        method: 'GET',
        headers,
        signal: abortController.signal,
      });
      
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      
      const reader = response.body?.getReader();
      if (!reader) {
        throw new Error('No response body reader available');
      }
      
      const decoder = new TextDecoder();
      let buffer = '';
      let fullContent = '';
      
      while (true) {
        if (cancelled) break;
        
        const { done, value } = await reader.read();
        if (done) break;
        
        buffer += decoder.decode(value, { stream: true });
        
        const lines = buffer.split('\n');
        buffer = lines.pop() || '';
        
        for (const line of lines) {
          if (!line.trim() || line.startsWith(':')) continue;
          
          if (line.startsWith('data: ')) {
            const dataStr = line.slice(6).trim();
            
            if (!dataStr || dataStr === '[DONE]') {
              if (callbacks.onDone) {
                callbacks.onDone();
                console.log('[Response]', url, { fullContent });
                return;
              }
              continue;
            }
            
            try {
              const data = JSON.parse(dataStr);
              console.log('[SSE] Parsed data:', data);
              
              if (data.type === 'start' && callbacks.onStart) {
                console.log('[SSE] Start event:', data.message);
                callbacks.onStart(data.message || '');
              } else if (data.type === 'chunk' && callbacks.onChunk && data.content) {
                fullContent += data.content;
                console.log('[SSE] Chunk received, length:', data.content.length, 'total:', fullContent.length);
                callbacks.onChunk(data.content);
              } else if (data.type === 'done' && callbacks.onDone) {
                console.log('[SSE] Done event, total length:', fullContent.length);
                callbacks.onDone();
                console.log('[Response]', url, { fullContent });
                return;
              } else if (data.type === 'error') {
                throw new Error(data.message || 'Unknown error');
              }
            } catch (e) {
              if (e instanceof Error) {
                if (e.message === 'Unexpected end of JSON input') {
                  continue;
                }
                if (e.message !== 'Unknown error') {
                  console.warn('Failed to parse SSE data:', dataStr, e);
                }
                if (callbacks.onError) {
                  callbacks.onError(e);
                  return;
                }
              }
            }
          }
        }
      }
      
      if (callbacks.onDone) {
        callbacks.onDone();
        console.log('[Response]', url, { fullContent });
      }
    } catch (error) {
      if (cancelled) return;
      if (error instanceof Error && error.name === 'AbortError') {
        return;
      }
      console.warn('[Error]', url, error);
      if (callbacks.onError) {
        callbacks.onError(error instanceof Error ? error : new Error(String(error)));
      }
    }
    // #endif
    
    // #ifndef H5
    // NOTE: Mobile platforms (App, Mini Program) do not support true streaming via onChunkReceived
    // The onChunkReceived callback is unreliable or not triggered on mobile platforms
    // Therefore, we use a fallback approach: wait for the complete response and parse it
    // This means mobile users will see the content appear all at once after the request completes
    // instead of seeing it stream in real-time like on H5
    
    const apiPath = getApiPathFromUrl(url);
    const baseUrl = getApiBaseUrl(apiPath);
    const token = getToken();
    
    let normalizedBaseUrl = baseUrl.endsWith('/') ? baseUrl.slice(0, -1) : baseUrl;
    let normalizedPath = url.startsWith('/') ? url : `/${url}`;
    
    if (normalizedBaseUrl.endsWith('/api') && normalizedPath.startsWith('/api')) {
      normalizedPath = normalizedPath.replace(/^\/api/, '');
    }
    
    const fullUrl = normalizedBaseUrl ? `${normalizedBaseUrl}${normalizedPath}` : normalizedPath;
    
    const parseSSEResponse = (responseText: string): string => {
      let parsedContent = '';
      const lines = responseText.split('\n');
      
      for (const line of lines) {
        if (!line.trim() || line.startsWith(':')) continue;
        
        if (line.startsWith('data: ')) {
          const lineDataStr = line.slice(6).trim();
          
          if (!lineDataStr || lineDataStr === '[DONE]') {
            continue;
          }
          
          try {
            const data = JSON.parse(lineDataStr);
            
            if (data.type === 'chunk' && data.content) {
              parsedContent += data.content;
            }
          } catch (e) {
            if (e instanceof Error && e.message !== 'Unexpected end of JSON input') {
              console.warn('[Mobile] Failed to parse SSE line:', lineDataStr, e);
            }
          }
        }
      }
      
      return parsedContent;
    };
    
    try {
      if (callbacks.onStart) {
        callbacks.onStart('');
      }
      
      const res = await new Promise<any>((resolve, reject) => {
        uni.request({
          url: fullUrl,
          method: 'GET',
          header: {
            'Authorization': token ? `Bearer ${token}` : '',
          },
          timeout: 120000,
          success: resolve,
          fail: reject,
        });
      });
      
      if (cancelled) return;
      
      if (res.statusCode === 200 && res.data) {
        let content = '';
        
        if (typeof res.data === 'string') {
          if (res.data.includes('data: ') && res.data.includes('\n')) {
            content = parseSSEResponse(res.data);
          } else {
            content = res.data;
          }
        } else if (res.data.raw_text) {
          if (typeof res.data.raw_text === 'string' && res.data.raw_text.includes('data: ') && res.data.raw_text.includes('\n')) {
            content = parseSSEResponse(res.data.raw_text);
          } else {
            content = res.data.raw_text;
          }
        } else if (res.data.content) {
          content = res.data.content;
        } else if (res.data.text) {
          content = res.data.text;
        } else if (res.data.data) {
          if (typeof res.data.data === 'string') {
            if (res.data.data.includes('data: ') && res.data.data.includes('\n')) {
              content = parseSSEResponse(res.data.data);
            } else {
              content = res.data.data;
            }
          } else {
            content = JSON.stringify(res.data.data);
          }
        }
        
        if (content && callbacks.onChunk) {
          callbacks.onChunk(content);
        }
        
        if (callbacks.onDone) {
          callbacks.onDone();
        }
        console.log('[Mobile] Request completed, content length:', content.length);
      } else {
        throw new Error(`Request failed with status ${res.statusCode}`);
      }
    } catch (error) {
      if (cancelled) return;
      console.warn('[Mobile] Request failed:', error);
      if (callbacks.onError) {
        callbacks.onError(error instanceof Error ? error : new Error(String(error)));
      }
    }
    // #endif
  };
  
  execute();
  
  return cancel;
}

export function getCareerPlanningInterviewPrepStream(
  jobId: number,
  callbacks: SSESteamCallbacks
): () => void {
  const url = `/api/ai/career-planning/${jobId}/interview-prep`;
  
  let cancelled = false;
  let abortController: AbortController | null = null;
  
  const cancel = () => {
    cancelled = true;
    if (abortController) {
      abortController.abort();
    }
  };
  
  const execute = async () => {
    console.log('[Params]', url, { jobId });
    // #ifdef H5
    try {
      const apiPath = getApiPathFromUrl(url);
      const baseUrl = getApiBaseUrl(apiPath);
      const token = getToken();
      
      const headers: Record<string, string> = {
        'Accept': 'text/event-stream',
      };
      if (token) {
        headers['Authorization'] = `Bearer ${token}`;
      }
      
      let normalizedBaseUrl = baseUrl.endsWith('/') ? baseUrl.slice(0, -1) : baseUrl;
      let normalizedPath = url.startsWith('/') ? url : `/${url}`;
      
      if (normalizedBaseUrl.endsWith('/api') && normalizedPath.startsWith('/api')) {
        normalizedPath = normalizedPath.replace(/^\/api/, '');
      }
      
      const fullUrl = normalizedBaseUrl ? `${normalizedBaseUrl}${normalizedPath}` : normalizedPath;
      
      abortController = new AbortController();
      
      const response = await fetch(fullUrl, {
        method: 'GET',
        headers,
        signal: abortController.signal,
      });
      
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      
      const reader = response.body?.getReader();
      if (!reader) {
        throw new Error('No response body reader available');
      }
      
      const decoder = new TextDecoder();
      let buffer = '';
      let fullContent = '';
      
      while (true) {
        if (cancelled) break;
        
        const { done, value } = await reader.read();
        if (done) break;
        
        buffer += decoder.decode(value, { stream: true });
        
        const lines = buffer.split('\n');
        buffer = lines.pop() || '';
        
        for (const line of lines) {
          if (!line.trim() || line.startsWith(':')) continue;
          
          if (line.startsWith('data: ')) {
            const dataStr = line.slice(6).trim();
            
            if (!dataStr || dataStr === '[DONE]') {
              if (callbacks.onDone) {
                callbacks.onDone();
                console.log('[Response]', url, { fullContent });
                return;
              }
              continue;
            }
            
            try {
              const data = JSON.parse(dataStr);
              console.log('[SSE] Parsed data:', data);
              
              if (data.type === 'start' && callbacks.onStart) {
                console.log('[SSE] Start event:', data.message);
                callbacks.onStart(data.message || '');
              } else if (data.type === 'chunk' && callbacks.onChunk && data.content) {
                fullContent += data.content;
                console.log('[SSE] Chunk received, length:', data.content.length, 'total:', fullContent.length);
                callbacks.onChunk(data.content);
              } else if (data.type === 'done' && callbacks.onDone) {
                console.log('[SSE] Done event, total length:', fullContent.length);
                callbacks.onDone();
                console.log('[Response]', url, { fullContent });
                return;
              } else if (data.type === 'error') {
                throw new Error(data.message || 'Unknown error');
              }
            } catch (e) {
              if (e instanceof Error) {
                if (e.message === 'Unexpected end of JSON input') {
                  continue;
                }
                if (e.message !== 'Unknown error') {
                  console.warn('Failed to parse SSE data:', dataStr, e);
                }
                if (callbacks.onError) {
                  callbacks.onError(e);
                  return;
                }
              }
            }
          }
        }
      }
      
      if (callbacks.onDone) {
        callbacks.onDone();
        console.log('[Response]', url, { fullContent });
      }
    } catch (error) {
      if (cancelled) return;
      if (error instanceof Error && error.name === 'AbortError') {
        return;
      }
      console.warn('[Error]', url, error);
      if (callbacks.onError) {
        callbacks.onError(error instanceof Error ? error : new Error(String(error)));
      }
    }
    // #endif
    
    // #ifndef H5
    // NOTE: Mobile platforms (App, Mini Program) do not support true streaming via onChunkReceived
    // The onChunkReceived callback is unreliable or not triggered on mobile platforms
    // Therefore, we use a fallback approach: wait for the complete response and parse it
    // This means mobile users will see the content appear all at once after the request completes
    // instead of seeing it stream in real-time like on H5
    
    const apiPath = getApiPathFromUrl(url);
    const baseUrl = getApiBaseUrl(apiPath);
    const token = getToken();
    
    let normalizedBaseUrl = baseUrl.endsWith('/') ? baseUrl.slice(0, -1) : baseUrl;
    let normalizedPath = url.startsWith('/') ? url : `/${url}`;
    
    if (normalizedBaseUrl.endsWith('/api') && normalizedPath.startsWith('/api')) {
      normalizedPath = normalizedPath.replace(/^\/api/, '');
    }
    
    const fullUrl = normalizedBaseUrl ? `${normalizedBaseUrl}${normalizedPath}` : normalizedPath;
    
    const parseSSEResponse = (responseText: string): string => {
      let parsedContent = '';
      const lines = responseText.split('\n');
      
      for (const line of lines) {
        if (!line.trim() || line.startsWith(':')) continue;
        
        if (line.startsWith('data: ')) {
          const lineDataStr = line.slice(6).trim();
          
          if (!lineDataStr || lineDataStr === '[DONE]') {
            continue;
          }
          
          try {
            const data = JSON.parse(lineDataStr);
            
            if (data.type === 'chunk' && data.content) {
              parsedContent += data.content;
            }
          } catch (e) {
            if (e instanceof Error && e.message !== 'Unexpected end of JSON input') {
              console.warn('[Mobile] Failed to parse SSE line:', lineDataStr, e);
            }
          }
        }
      }
      
      return parsedContent;
    };
    
    try {
      if (callbacks.onStart) {
        callbacks.onStart('');
      }
      
      const res = await new Promise<any>((resolve, reject) => {
        uni.request({
          url: fullUrl,
          method: 'GET',
          header: {
            'Authorization': token ? `Bearer ${token}` : '',
          },
          timeout: 120000,
          success: resolve,
          fail: reject,
        });
      });
      
      if (cancelled) return;
      
      if (res.statusCode === 200 && res.data) {
        let content = '';
        
        if (typeof res.data === 'string') {
          if (res.data.includes('data: ') && res.data.includes('\n')) {
            content = parseSSEResponse(res.data);
          } else {
            content = res.data;
          }
        } else if (res.data.content) {
          content = res.data.content;
        } else if (res.data.raw_text) {
          if (typeof res.data.raw_text === 'string' && res.data.raw_text.includes('data: ') && res.data.raw_text.includes('\n')) {
            content = parseSSEResponse(res.data.raw_text);
          } else {
            content = res.data.raw_text;
          }
        } else if (res.data.text) {
          content = res.data.text;
        } else if (res.data.data) {
          if (typeof res.data.data === 'string') {
            if (res.data.data.includes('data: ') && res.data.data.includes('\n')) {
              content = parseSSEResponse(res.data.data);
            } else {
              content = res.data.data;
            }
          } else {
            content = JSON.stringify(res.data.data);
          }
        }
        
        if (content && callbacks.onChunk) {
          callbacks.onChunk(content);
        }
        
        if (callbacks.onDone) {
          callbacks.onDone();
        }
        console.log('[Mobile] Request completed, content length:', content.length);
      } else {
        throw new Error(`Request failed with status ${res.statusCode}`);
      }
    } catch (error) {
      if (cancelled) return;
      console.warn('[Mobile] Request failed:', error);
      if (callbacks.onError) {
        callbacks.onError(error instanceof Error ? error : new Error(String(error)));
      }
    }
    // #endif
  };
  
  execute();
  
  return cancel;
}
