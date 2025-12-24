import { http } from '../http';
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

/**
 * Get intern job recommendations with field supplementation
 * @returns Intern job recommendations response with supplemented fields
 */
export async function getInternJobRecommendationsWithSupplement(userId?: number | null): Promise<InternJobRecommendationsResponse> {
  const response = await getInternJobRecommendations(userId);
  const supplementedJobs = await Promise.all(
    response.jobs.map(job => supplementJobItem(job))
  );
  return {
    jobs: supplementedJobs,
  };
}

/**
 * Get intern job recommendations
 * @returns Intern job recommendations response
 */
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

