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
 * @returns Education experience data
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
 * @returns List of education experiences
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
 * @returns Updated education experience data
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
 * @returns Operation result
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
  endMonth?: string;
  achievement?: string;
  projectUrl?: string;
}

export interface AddProjectExperienceParams {
  projectName: string;
  projectRole: string;
  description: string;
  responsibility: string;
  startMonth: string;
  endMonth?: string;
  achievement?: string;
  projectUrl?: string;
}

export interface UpdateProjectExperienceParams {
  projectName?: string;
  projectRole?: string;
  description?: string;
  responsibility?: string;
  startMonth?: string;
  endMonth?: string;
  achievement?: string;
  projectUrl?: string;
}

/**
 * Add project experience
 * @returns Project experience data
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
 * @returns List of project experiences
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
 * @returns Updated project experience data
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
 * @returns Operation result
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
 * @returns Work experience data
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
 * @returns List of work experiences
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
 * @returns Updated work experience data
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
 * @returns Operation result
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
 * @returns Skill data
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
 * @returns List of skills
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
 * @returns Updated skill data
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
 * @returns Operation result
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
 * @returns Job seeker expectation data
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
 * @returns List of job seeker expectations
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
 * @returns Updated job seeker expectation data
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
 * @returns Operation result
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
  realName: string;
  birthDate: string;
  currentCity: string;
  education?: number;
  jobStatus: number;
  graduationYear?: string;
  selfIntroduction?: string;
}

export interface RegisterSeekerParams {
  realName: string;
  birthDate: string;
  currentCity: string;
  jobStatus: number;
}

/**
 * Register seeker information
 * @returns Seeker information data
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
 * @returns Seeker information data
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
 * @returns Operation result
 */
export function updateSeekerInfo(params: { realName: string; birthDate: string; currentCity: string; jobStatus: number }): Promise<null> {
  const url = '/api/seeker/update-seeker-info';
  console.log('[Params]', url, params);
  return http<null>({
    url,
    method: 'PATCH',
    data: params,
  }).then(response => {
    console.log('[Response]', url, response);
    return response;
  });
}

/**
 * Get online resume
 * @returns Online resume data
 */
export function getOnlineResume(userId?: number): Promise<any> {
  const queryString = userId !== undefined ? `userId=${userId}` : '';
  const url = `/api/seeker/online-resume${queryString ? `?${queryString}` : ''}`;
  console.log('[Params]', url, { userId });
  return http<any>({
    url,
    method: 'GET',
  }).then(response => {
    console.log('[Response]', url, response);
    return response;
  });
}

// Get all seeker cards
export interface SeekerCard {
  username: string;
  graduationYear: string;
  age: number;
  highestEducation: string;
  major: string;
  jobStatus: number;
  workExperienceYear: number;
  internshipExperience: boolean;
  university?: string;
  city?: string;
  avatarUrl?: string;
}

/**
 * Get all seeker cards
 * @returns List of seeker cards
 */
export function getSeekerCards(): Promise<SeekerCard[]> {
  const url = '/api/seeker/cards';
  console.log('[Params]', url, null);
  return http<SeekerCard[]>({
    url,
    method: 'GET',
  }).then(response => {
    console.log('[Response]', url, response);
    return response;
  });
}

// Filter seekers
export interface FilterSeekersParams {
  city?: string;
  education?: number;
  salaryMin?: number;
  salaryMax?: number;
  isInternship?: number;
  jobStatus?: number;
  hasInternship?: number;
}

/**
 * Filter seekers
 * @returns Filtered list of seeker cards
 */
export function filterSeekers(params: FilterSeekersParams): Promise<SeekerCard[]> {
  const queryParams: string[] = [];
  if (params.city) queryParams.push(`city=${encodeURIComponent(params.city)}`);
  if (params.education !== undefined) queryParams.push(`education=${params.education}`);
  if (params.salaryMin !== undefined) queryParams.push(`salaryMin=${params.salaryMin}`);
  if (params.salaryMax !== undefined) queryParams.push(`salaryMax=${params.salaryMax}`);
  if (params.isInternship !== undefined) queryParams.push(`isInternship=${params.isInternship}`);
  if (params.jobStatus !== undefined) queryParams.push(`jobStatus=${params.jobStatus}`);
  if (params.hasInternship !== undefined) queryParams.push(`hasInternship=${params.hasInternship}`);
  const queryString = queryParams.join('&');
  const url = `/api/seeker/cards/filter${queryString ? `?${queryString}` : ''}`;
  console.log('[Params]', url, params);
  return http<SeekerCard[]>({
    url,
    method: 'GET',
  }).then(response => {
    console.log('[Response]', url, response);
    return response;
  });
}

/**
 * Delete seeker information
 * @returns Operation result
 */
export function deleteSeeker(): Promise<null> {
  const url = '/api/seeker/delete-seeker';
  console.log('[Params]', url, null);
  return http<null>({
    url,
    method: 'DELETE',
  }).then(response => {
    console.log('[Response]', url, response);
    return response;
  });
}

/**
 * Favorite job
 * @returns Operation result
 */
export function favoriteJob(jobId: number): Promise<null> {
  const url = `/api/seeker/favorite-job/${jobId}`;
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
 * Unfavorite job
 * @returns Operation result
 */
export function unfavoriteJob(jobId: number): Promise<null> {
  const url = `/api/seeker/favorite-job/${jobId}`;
  console.log('[Params]', url, { jobId });
  return http<null>({
    url,
    method: 'DELETE',
  }).then(response => {
    console.log('[Response]', url, response);
    return response;
  });
}

// Get favorite jobs list
export interface FavoriteJob {
  favoriteId: number;
  jobId: number;
  createdAt: string;
  jobTitle: string;
  salaryMin: number;
  salaryMax: number;
  address: string;
  companyName: string;
  companyScale: number;
  financingStage: number;
  hrName: string;
  hrAvatarUrl: string;
  educationRequired: number;
}

/**
 * Get favorite jobs list
 * @returns List of favorite jobs
 */
export function getFavoriteJobs(): Promise<FavoriteJob[]> {
  const url = '/api/seeker/favorite-jobs';
  console.log('[Params]', url, null);
  return http<FavoriteJob[]>({
    url,
    method: 'GET',
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
 * @returns Composite resume data
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
