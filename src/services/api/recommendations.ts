import { http, getApiPathFromUrl, getApiBaseUrl, getToken } from '../http';
import { getJobDetail, type JobDetail } from './recruitment';

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

  if (!needsSupplement) {
    return job;
  }

  try {
    const detail = await getJobDetail(job.jobId);
    const supplement = mapJobDetailToInternJobItem(detail);
    
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
    };
  } catch (error) {
    console.warn(`Failed to supplement job ${job.jobId}:`, error);
    return job;
  }
}

export async function getInternJobRecommendationsWithSupplement(userId?: number | null): Promise<InternJobRecommendationsResponse> {
  const response = await getInternJobRecommendations(userId);
  const supplementedJobs = await Promise.all(
    response.jobs.map(job => supplementJobItem(job))
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
  
  const url = '/api/seeker/job-recommendations/intern';
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
    timeout: 10000,
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
    uni.request({
      url: getApiBaseUrl(getApiPathFromUrl(url)) + url,
      method: 'GET',
      header: {
        'Authorization': `Bearer ${getToken()}`,
      },
      success: (res: any) => {
        if (res.statusCode === 200 && res.data) {
          if (callbacks.onStart) {
            callbacks.onStart('');
          }
          
          if (res.data.raw_text && callbacks.onChunk) {
            callbacks.onChunk(res.data.raw_text);
          }
          
          if (callbacks.onDone) {
            callbacks.onDone();
          }
          console.log('[Response]', url, res.data);
        } else {
          const error = new Error(`Request failed with status ${res.statusCode}`);
          if (callbacks.onError) {
            callbacks.onError(error);
          }
          console.warn('[Error]', url, error);
        }
      },
      fail: (err: any) => {
        const error = new Error(err.errMsg || 'Request failed');
        if (callbacks.onError) {
          callbacks.onError(error);
        }
        console.warn('[Error]', url, error);
      },
    });
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
    uni.request({
      url: getApiBaseUrl(getApiPathFromUrl(url)) + url,
      method: 'GET',
      header: {
        'Authorization': `Bearer ${getToken()}`,
      },
      success: (res: any) => {
        if (res.statusCode === 200 && res.data) {
          if (callbacks.onStart) {
            callbacks.onStart('');
          }
          
          if (res.data.raw_text && callbacks.onChunk) {
            callbacks.onChunk(res.data.raw_text);
          }
          
          if (callbacks.onDone) {
            callbacks.onDone();
          }
          console.log('[Response]', url, res.data);
        } else {
          const error = new Error(`Request failed with status ${res.statusCode}`);
          if (callbacks.onError) {
            callbacks.onError(error);
          }
          console.warn('[Error]', url, error);
        }
      },
      fail: (err: any) => {
        const error = new Error(err.errMsg || 'Request failed');
        if (callbacks.onError) {
          callbacks.onError(error);
        }
        console.warn('[Error]', url, error);
      },
    });
    // #endif
  };
  
  execute();
  
  return cancel;
}
