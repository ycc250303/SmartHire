import { http, getApiBaseUrl, getApiPathFromUrl, getToken } from '../http';

const AI_TIMEOUT_MS = 60000;

export interface CandidateMatchAnalysisResponse {
  match_analysis?: {
    overall_score?: number;
    skill_match?: number;
    education_match?: number;
    experience_qualified?: boolean;
  };
  gap_analysis?: {
    skills?: {
      required_missing?: string[];
      optional_missing?: string[];
      matched?: Array<{
        name: string;
        candidate_level?: number;
        is_required?: boolean;
      }>;
      match_rate?: number;
    };
    experience?: {
      candidate_years?: number;
      required_text?: string;
      is_qualified?: boolean;
      gap_years?: number;
    };
    education?: {
      candidate_text?: string;
      required_text?: string;
      is_qualified?: boolean;
      match_score?: number;
    };
  };
  candidate_info?: {
    job_seeker_id?: number;
    real_name?: string;
    education?: string;
    work_experience_year?: number;
    current_city?: string;
  };
  job_info?: {
    job_id?: number;
    job_title?: string;
    city?: string;
    education_required?: string;
    experience_required?: string;
  };
}

export interface CandidateEvaluationResponse {
  evaluation?: {
    overall_assessment?: string;
    strengths?: Array<{ category: string; description: string; evidence: string }>;
    weaknesses?: Array<{ category: string; description: string; impact: string }>;
    recommendation?: {
      level?: string;
      reason?: string;
      suggested_actions?: string[];
    };
    interview_focus?: string[];
  };
  candidate_summary?: Record<string, unknown>;
  job_summary?: Record<string, unknown>;
}

export interface CandidateRecommendationResponse {
  recommendation?: {
    summary?: string;
    key_points?: Array<{ point: string; description: string; evidence: string }>;
    concerns?: Array<{ concern: string; mitigation: string }>;
    recommendation_text?: string;
    suggested_next_steps?: string[];
  };
  match_score?: number;
  candidate_highlights?: Record<string, unknown>;
}

function buildJobQuery(jobId: number, forceRefresh?: boolean): string {
  const parts: string[] = [];
  parts.push(`job_id=${encodeURIComponent(String(jobId))}`);
  if (typeof forceRefresh === 'boolean') {
    parts.push(`force_refresh=${encodeURIComponent(String(forceRefresh))}`);
  }
  return parts.length ? `?${parts.join('&')}` : '';
}

export function getCandidateMatchAnalysis(jobSeekerId: number, jobId: number, forceRefresh?: boolean) {
  const url = `/api/ai/hr/candidate/${jobSeekerId}/analysis${buildJobQuery(jobId, forceRefresh)}`;
  return http<CandidateMatchAnalysisResponse>({ url, method: 'POST', timeout: AI_TIMEOUT_MS });
}

export function getCandidateEvaluation(jobSeekerId: number, jobId: number, forceRefresh?: boolean) {
  const url = `/api/ai/hr/candidate/${jobSeekerId}/evaluation${buildJobQuery(jobId, forceRefresh)}`;
  return http<CandidateEvaluationResponse>({ url, method: 'POST', timeout: AI_TIMEOUT_MS });
}

export function getCandidateRecommendation(jobSeekerId: number, jobId: number, forceRefresh?: boolean) {
  const url = `/api/ai/hr/candidate/${jobSeekerId}/recommendation${buildJobQuery(jobId, forceRefresh)}`;
  return http<CandidateRecommendationResponse>({ url, method: 'POST', timeout: AI_TIMEOUT_MS });
}

export type StreamCallbacks = {
  onStart?: (message: string) => void;
  onChunk?: (content: string) => void;
  onDone?: () => void;
  onError?: (error: Error) => void;
};

export function streamCandidateInterviewQuestions(
  jobSeekerId: number,
  jobId: number,
  callbacks: StreamCallbacks,
): () => void {
  let cancelled = false;
  const url = `/api/ai/hr/candidate/${jobSeekerId}/interview-questions${buildJobQuery(jobId)}`;
  const cancel = () => {
    cancelled = true;
  };

  const parseSSEResponse = (responseText: string): { message?: string; content: string } => {
    let fullContent = '';
    let startMessage = '';
    const lines = responseText.split('\n');

    for (const line of lines) {
      if (!line.trim() || line.startsWith(':')) continue;
      if (!line.startsWith('data: ')) continue;

      const lineDataStr = line.slice(6).trim();
      if (!lineDataStr || lineDataStr === '[DONE]') continue;

      try {
        const data = JSON.parse(lineDataStr) as any;
        if (data?.type === 'start' && typeof data.message === 'string') {
          startMessage = data.message;
        }
        if (data?.type === 'chunk' && typeof data.content === 'string') {
          fullContent += data.content;
        }
      } catch (e) {
        if (e instanceof Error && e.message !== 'Unexpected end of JSON input') {
          // ignore partial chunks
        }
      }
    }

    return { message: startMessage || undefined, content: fullContent };
  };

  const execute = async () => {
    // #ifdef H5
    try {
      const apiPath = getApiPathFromUrl(url);
      const baseUrl = getApiBaseUrl(apiPath);
      const token = getToken();

      let normalizedBaseUrl = baseUrl.endsWith('/') ? baseUrl.slice(0, -1) : baseUrl;
      let normalizedPath = url.startsWith('/') ? url : `/${url}`;
      if (normalizedBaseUrl.endsWith('/api') && normalizedPath.startsWith('/api')) {
        normalizedPath = normalizedPath.replace(/^\/api/, '');
      }
      const fullUrl = normalizedBaseUrl ? `${normalizedBaseUrl}${normalizedPath}` : normalizedPath;

      const controller = new AbortController();
      const signal = controller.signal;
      const abortOnCancel = setInterval(() => {
        if (cancelled) {
          controller.abort();
          clearInterval(abortOnCancel);
        }
      }, 120);

      const response = await fetch(fullUrl, {
        method: 'GET',
        headers: {
          Accept: 'text/event-stream',
          ...(token ? { Authorization: `Bearer ${token}` } : {}),
        },
        signal,
      });

      if (!response.ok) {
        throw new Error(`Request failed with status ${response.status}`);
      }

      if (callbacks.onStart) {
        callbacks.onStart('开始生成面试问题...');
      }

      const reader = response.body?.getReader();
      if (!reader) {
        throw new Error('Streaming not supported');
      }

      const decoder = new TextDecoder();
      let buffer = '';
      let fullContent = '';

      while (!cancelled) {
        const { done, value } = await reader.read();
        if (done) break;
        buffer += decoder.decode(value, { stream: true });

        const parts = buffer.split('\n\n');
        buffer = parts.pop() || '';

        for (const part of parts) {
          const lines = part.split('\n');
          for (const line of lines) {
            if (!line.startsWith('data:')) continue;
            const jsonStr = line.replace(/^data:\s*/, '').trim();
            if (!jsonStr) continue;

            let data: any;
            try {
              data = JSON.parse(jsonStr);
            } catch {
              continue;
            }

            if (data?.type === 'start') {
              if (callbacks.onStart) callbacks.onStart(data?.message || '开始生成面试问题...');
            } else if (data?.type === 'chunk' && typeof data.content === 'string') {
              fullContent += data.content;
              if (callbacks.onChunk) callbacks.onChunk(data.content);
            } else if (data?.type === 'done') {
              if (callbacks.onDone) callbacks.onDone();
              return;
            } else if (data?.type === 'error') {
              const err = new Error(String(data?.message || '生成失败'));
              if (callbacks.onError) callbacks.onError(err);
              return;
            }
          }
        }
      }

      if (cancelled) return;
      if (callbacks.onDone) callbacks.onDone();
      console.log('[Response]', url, { fullContentLength: fullContent.length });
    } catch (error) {
      if (cancelled) return;
      if (error instanceof Error && error.name === 'AbortError') return;
      if (callbacks.onError) callbacks.onError(error instanceof Error ? error : new Error(String(error)));
    }
    // #endif

    // #ifndef H5
    try {
      const apiPath = getApiPathFromUrl(url);
      const baseUrl = getApiBaseUrl(apiPath);
      const token = getToken();

      let normalizedBaseUrl = baseUrl.endsWith('/') ? baseUrl.slice(0, -1) : baseUrl;
      let normalizedPath = url.startsWith('/') ? url : `/${url}`;
      if (normalizedBaseUrl.endsWith('/api') && normalizedPath.startsWith('/api')) {
        normalizedPath = normalizedPath.replace(/^\/api/, '');
      }
      const fullUrl = normalizedBaseUrl ? `${normalizedBaseUrl}${normalizedPath}` : normalizedPath;

      if (callbacks.onStart) {
        callbacks.onStart('开始生成面试问题...');
      }

      const res = await new Promise<any>((resolve, reject) => {
        uni.request({
          url: fullUrl,
          method: 'GET',
          header: {
            Accept: 'text/event-stream',
            Authorization: token ? `Bearer ${token}` : '',
          },
          timeout: 120000,
          success: resolve,
          fail: reject,
        });
      });

      if (cancelled) return;

      if (res.statusCode === 200) {
        const rawText = typeof res.data === 'string' ? res.data : JSON.stringify(res.data ?? '');
        const parsed = parseSSEResponse(rawText);
        if (parsed.message && callbacks.onStart) callbacks.onStart(parsed.message);
        if (parsed.content && callbacks.onChunk) callbacks.onChunk(parsed.content);
        if (callbacks.onDone) callbacks.onDone();
      } else {
        throw new Error(`Request failed with status ${res.statusCode}`);
      }
    } catch (error) {
      if (cancelled) return;
      if (callbacks.onError) callbacks.onError(error instanceof Error ? error : new Error(String(error)));
    }
    // #endif
  };

  execute();
  return cancel;
}
