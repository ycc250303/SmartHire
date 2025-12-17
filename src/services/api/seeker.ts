import { http } from '../http';

// Education Experience
export interface EducationExperience {
  id: number;
  schoolName: string;
  major: string;
  education: number;
  startYear: string;
  endYear: string;
  isCurrent: number;
  createdAt: string;
  updatedAt: string;
}

export interface AddEducationExperienceParams {
  schoolName: string;
  major: string;
  education: number;
  startYear: string;
  endYear: string;
  isCurrent: number;
}

export interface UpdateEducationExperienceParams {
  schoolName?: string;
  major?: string;
  education?: number;
  startYear?: string;
  endYear?: string;
  isCurrent?: number;
}

/**
 * Add education experience
 */
export function addEducationExperience(params: AddEducationExperienceParams): Promise<EducationExperience> {
  const url = '/api/seeker/add-education-experience';
  console.log('[Params]', url, params);
  return http<EducationExperience>({
    url,
    method: 'POST',
    data: params,
  }).then(response => {
    console.log('[Response]', url, response);
    return response;
  });
}

/**
 * Get all education experiences
 */
export function getEducationExperiences(): Promise<EducationExperience[]> {
  const url = '/api/seeker/get-education-experiences';
  console.log('[Params]', url, null);
  return http<EducationExperience[]>({
    url,
    method: 'GET',
  }).then(response => {
    console.log('[Response]', url, response);
    return response;
  });
}

/**
 * Update education experience
 */
export function updateEducationExperience(id: number, params: UpdateEducationExperienceParams): Promise<EducationExperience> {
  const url = `/api/seeker/update-education-experience/${id}`;
  console.log('[Params]', url, { id, ...params });
  return http<EducationExperience>({
    url,
    method: 'PATCH',
    data: params,
  }).then(response => {
    console.log('[Response]', url, response);
    return response;
  });
}

/**
 * Delete education experience
 */
export function deleteEducationExperience(id: number): Promise<null> {
  const url = `/api/seeker/delete-education-experience/${id}`;
  console.log('[Params]', url, { id });
  return http<null>({
    url,
    method: 'DELETE',
  }).then(response => {
    console.log('[Response]', url, response);
    return response;
  });
}

// Project Experience
export interface ProjectExperience {
  id?: number;
  projectName: string;
  projectRole: string;
  description: string;
  responsibility: string;
  startMonth: string;
  endMonth: string;
}

export interface AddProjectExperienceParams {
  projectName: string;
  projectRole: string;
  description: string;
  responsibility: string;
  startMonth: string;
  endMonth: string;
}

export interface UpdateProjectExperienceParams {
  projectName?: string;
  projectRole?: string;
  description?: string;
  responsibility?: string;
  startMonth?: string;
  endMonth?: string;
}

/**
 * Add project experience
 */
export function addProjectExperience(params: AddProjectExperienceParams): Promise<ProjectExperience> {
  const url = '/api/seeker/add-project-experience';
  console.log('[Params]', url, params);
  return http<ProjectExperience>({
    url,
    method: 'POST',
    data: params,
  }).then(response => {
    console.log('[Response]', url, response);
    return response;
  });
}

/**
 * Get all project experiences
 */
export function getProjectExperiences(): Promise<ProjectExperience[]> {
  const url = '/api/seeker/get-project-experiences';
  console.log('[Params]', url, null);
  return http<ProjectExperience[]>({
    url,
    method: 'GET',
  }).then(response => {
    console.log('[Response]', url, response);
    return response;
  });
}

/**
 * Update project experience
 */
export function updateProjectExperience(id: number, params: UpdateProjectExperienceParams): Promise<ProjectExperience> {
  const url = `/api/seeker/update-project-experience/${id}`;
  console.log('[Params]', url, { id, ...params });
  return http<ProjectExperience>({
    url,
    method: 'PATCH',
    data: params,
  }).then(response => {
    console.log('[Response]', url, response);
    return response;
  });
}

/**
 * Delete project experience
 */
export function deleteProjectExperience(id: number): Promise<null> {
  const url = `/api/seeker/delete-project-experience/${id}`;
  console.log('[Params]', url, { id });
  return http<null>({
    url,
    method: 'DELETE',
  }).then(response => {
    console.log('[Response]', url, response);
    return response;
  });
}

// Work Experience
export interface WorkExperience {
  id: number;
  companyName: string;
  position: string;
  department: string;
  startMonth: string;
  endMonth: string;
  description: string;
  achievements: string;
  isInternship: number;
}

export interface AddWorkExperienceParams {
  companyName: string;
  position: string;
  department: string;
  startMonth: string;
  endMonth: string;
  description: string;
  achievements: string;
  isInternship: number;
}

export interface UpdateWorkExperienceParams {
  companyName?: string;
  position?: string;
  department?: string;
  startMonth?: string;
  endMonth?: string;
  description?: string;
  achievements?: string;
  isInternship?: number;
}

/**
 * Add work experience
 */
export function addWorkExperience(params: AddWorkExperienceParams): Promise<WorkExperience> {
  const url = '/api/seeker/add-work-experience';
  console.log('[Params]', url, params);
  return http<WorkExperience>({
    url,
    method: 'POST',
    data: params,
  }).then(response => {
    console.log('[Response]', url, response);
    return response;
  });
}

/**
 * Get all work experiences
 */
export function getWorkExperiences(): Promise<WorkExperience[]> {
  const url = '/api/seeker/get-work-experiences';
  console.log('[Params]', url, null);
  return http<WorkExperience[]>({
    url,
    method: 'GET',
  }).then(response => {
    console.log('[Response]', url, response);
    return response;
  });
}

/**
 * Update work experience
 */
export function updateWorkExperience(id: number, params: UpdateWorkExperienceParams): Promise<WorkExperience> {
  const url = `/api/seeker/update-work-experience/${id}`;
  console.log('[Params]', url, { id, ...params });
  return http<WorkExperience>({
    url,
    method: 'PATCH',
    data: params,
  }).then(response => {
    console.log('[Response]', url, response);
    return response;
  });
}

/**
 * Delete work experience
 */
export function deleteWorkExperience(id: number): Promise<null> {
  const url = `/api/seeker/delete-work-experience/${id}`;
  console.log('[Params]', url, { id });
  return http<null>({
    url,
    method: 'DELETE',
  }).then(response => {
    console.log('[Response]', url, response);
    return response;
  });
}

// Skill
export interface Skill {
  id: number;
  skillName: string;
  level: number;
}

export interface AddSkillParams {
  skillName: string;
  level: number;
}

export interface UpdateSkillParams {
  skillName?: string;
  level?: number;
}

/**
 * Add skill
 */
export function addSkill(params: AddSkillParams): Promise<Skill> {
  const url = '/api/seeker/add-skill';
  console.log('[Params]', url, params);
  return http<Skill>({
    url,
    method: 'POST',
    data: params,
  }).then(response => {
    console.log('[Response]', url, response);
    return response;
  });
}

/**
 * Get all skills
 */
export function getSkills(): Promise<Skill[]> {
  const url = '/api/seeker/get-skills';
  console.log('[Params]', url, null);
  return http<Skill[]>({
    url,
    method: 'GET',
  }).then(response => {
    console.log('[Response]', url, response);
    return response;
  });
}

/**
 * Update skill
 */
export function updateSkill(id: number, params: UpdateSkillParams): Promise<Skill> {
  const url = `/api/seeker/update-skill/${id}`;
  console.log('[Params]', url, { id, ...params });
  return http<Skill>({
    url,
    method: 'PATCH',
    data: params,
  }).then(response => {
    console.log('[Response]', url, response);
    return response;
  });
}

/**
 * Delete skill
 */
export function deleteSkill(id: number): Promise<null> {
  const url = `/api/seeker/delete-skill/${id}`;
  console.log('[Params]', url, { id });
  return http<null>({
    url,
    method: 'DELETE',
  }).then(response => {
    console.log('[Response]', url, response);
    return response;
  });
}

// Job Seeker Expectation
export interface JobSeekerExpectation {
  id: number;
  expectedPosition: string;
  expectedIndustry: string;
  workCity: string;
  salaryMin: number;
  salaryMax: number;
}

export interface AddJobSeekerExpectationParams {
  expectedPosition: string;
  expectedIndustry: string;
  workCity: string;
  salaryMin: number;
  salaryMax: number;
}

export interface UpdateJobSeekerExpectationParams {
  expectedPosition?: string;
  expectedIndustry?: string;
  workCity?: string;
  salaryMin?: number;
  salaryMax?: number;
}

/**
 * Add job seeker expectation
 */
export function addJobSeekerExpectation(params: AddJobSeekerExpectationParams): Promise<JobSeekerExpectation> {
  const url = '/api/seeker/add-job-seeker-expectation';
  console.log('[Params]', url, params);
  return http<JobSeekerExpectation>({
    url,
    method: 'POST',
    data: params,
  }).then(response => {
    console.log('[Response]', url, response);
    return response;
  });
}

/**
 * Get all job seeker expectations
 */
export function getJobSeekerExpectations(): Promise<JobSeekerExpectation[]> {
  const url = '/api/seeker/get-job-seeker-expectations';
  console.log('[Params]', url, null);
  return http<JobSeekerExpectation[]>({
    url,
    method: 'GET',
  }).then(response => {
    console.log('[Response]', url, response);
    return response;
  });
}

/**
 * Update job seeker expectation
 */
export function updateJobSeekerExpectation(id: number, params: UpdateJobSeekerExpectationParams): Promise<JobSeekerExpectation> {
  const url = `/api/seeker/update-job-seeker-expectation/${id}`;
  console.log('[Params]', url, { id, ...params });
  return http<JobSeekerExpectation>({
    url,
    method: 'PATCH',
    data: params,
  }).then(response => {
    console.log('[Response]', url, response);
    return response;
  });
}

/**
 * Delete job seeker expectation
 */
export function deleteJobSeekerExpectation(id: number): Promise<null> {
  const url = `/api/seeker/delete-job-seeker-expectation/${id}`;
  console.log('[Params]', url, { id });
  return http<null>({
    url,
    method: 'DELETE',
  }).then(response => {
    console.log('[Response]', url, response);
    return response;
  });
}

// Seeker Info
export interface SeekerInfo {
  id?: number;
  userId?: number;
  realName?: string | null;
  gender?: number;
  birthDate?: string | null;
  city?: string | null;
  bio?: string | null;
}

export interface RegisterSeekerParams {
  realName?: string | null;
  gender: number;
  birthDate?: string | null;
  city?: string | null;
  bio?: string | null;
}

/**
 * Register seeker information
 */
export function registerSeeker(params: RegisterSeekerParams): Promise<SeekerInfo> {
  const url = '/api/seeker/register-seeker';
  console.log('[Params]', url, params);
  return http<SeekerInfo>({
    url,
    method: 'POST',
    data: params,
  }).then(response => {
    console.log('[Response]', url, response);
    return response;
  });
}

/**
 * Get seeker information
 */
export function getSeekerInfo(): Promise<SeekerInfo> {
  const url = '/api/seeker/get-seeker-info';
  console.log('[Params]', url, null);
  return http<SeekerInfo>({
    url,
    method: 'GET',
  }).then(response => {
    console.log('[Response]', url, response);
    return response;
  });
}

/**
 * Update seeker basic information
 */
export function updateSeekerInfo(params: Partial<SeekerInfo>): Promise<SeekerInfo> {
  const url = '/api/seeker/update-seeker-info';
  console.log('[Params]', url, params);
  return http<SeekerInfo>({
    url,
    method: 'PATCH',
    data: params,
  }).then(response => {
    console.log('[Response]', url, response);
    return response;
  });
}

// Composite Resume Data
export interface CompositeResumeData {
  seekerInfo: SeekerInfo | null;
  educationExperiences: EducationExperience[];
  projectExperiences: ProjectExperience[];
  workExperiences: WorkExperience[];
  skills: Skill[];
  jobSeekerExpectations: JobSeekerExpectation[];
}

/**
 * Fetch all resume components and combine them
 */
export async function fetchCompositeResume(): Promise<CompositeResumeData> {
  try {
    const [
      seekerInfo,
      educationExperiences,
      projectExperiences,
      workExperiences,
      skills,
      jobSeekerExpectations
    ] = await Promise.allSettled([
      getSeekerInfo().catch(() => null),
      getEducationExperiences().catch(() => []),
      getProjectExperiences().catch(() => []),
      getWorkExperiences().catch(() => []),
      getSkills().catch(() => []),
      getJobSeekerExpectations().catch(() => [])
    ]);

    return {
      seekerInfo: seekerInfo.status === 'fulfilled' ? seekerInfo.value : null,
      educationExperiences: educationExperiences.status === 'fulfilled' ? educationExperiences.value : [],
      projectExperiences: projectExperiences.status === 'fulfilled' ? projectExperiences.value : [],
      workExperiences: workExperiences.status === 'fulfilled' ? workExperiences.value : [],
      skills: skills.status === 'fulfilled' ? skills.value : [],
      jobSeekerExpectations: jobSeekerExpectations.status === 'fulfilled' ? jobSeekerExpectations.value : []
    };
  } catch (error) {
    console.error('Failed to fetch composite resume:', error);
    throw error;
  }
}
