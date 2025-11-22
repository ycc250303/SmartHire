import { http } from '../http';

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
  companyName: string;
  city: string;
  address: string;
  work_months_min: number;
  skills: string[];
  degrees: Degree;
  matchScore: number; 
}

export interface InternJobRecommendationsResponse {
  jobs: InternJobItem[];
}

/**
 * Get intern job recommendations
 * @param userId 
 */
export function getInternJobRecommendations(userId?: number | null): Promise<InternJobRecommendationsResponse> {
  const params: Record<string, any> = {};
  
  // Get userId in JWT by default
  if (userId !== undefined && userId !== null) {
    params.userId = userId;
  }
  
  return http<InternJobRecommendationsResponse>({
    url: '/api/seeker/job-recommendations/intern',
    method: 'GET',
    data: params,
  });
}

