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
  return http<EducationExperience>({
    url: '/api/seeker/add-education-experience',
    method: 'POST',
    data: params,
  });
}

/**
 * Get all education experiences
 */
export function getEducationExperiences(): Promise<EducationExperience[]> {
  return http<EducationExperience[]>({
    url: '/api/seeker/get-education-experiences',
    method: 'GET',
  });
}

/**
 * Update education experience
 */
export function updateEducationExperience(id: number, params: UpdateEducationExperienceParams): Promise<EducationExperience> {
  return http<EducationExperience>({
    url: `/api/seeker/update-education-experience/${id}`,
    method: 'PATCH',
    data: params,
  });
}

/**
 * Delete education experience
 */
export function deleteEducationExperience(id: number): Promise<null> {
  return http<null>({
    url: `/api/seeker/delete-education-experience/${id}`,
    method: 'DELETE',
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
  endMoneth: string;
}

export interface AddProjectExperienceParams {
  projectName: string;
  projectRole: string;
  description: string;
  responsibility: string;
  startMonth: string;
  endMoneth: string;
}

export interface UpdateProjectExperienceParams {
  projectName?: string;
  projectRole?: string;
  description?: string;
  responsibility?: string;
  startMonth?: string;
  endMoneth?: string;
}

/**
 * Add project experience
 */
export function addProjectExperience(params: AddProjectExperienceParams): Promise<ProjectExperience> {
  return http<ProjectExperience>({
    url: '/api/seeker/add-project-experience',
    method: 'POST',
    data: params,
  });
}

/**
 * Get all project experiences
 */
export function getProjectExperiences(): Promise<ProjectExperience[]> {
  return http<ProjectExperience[]>({
    url: '/api/seeker/get-project-experiences',
    method: 'GET',
  });
}

/**
 * Update project experience
 */
export function updateProjectExperience(id: number, params: UpdateProjectExperienceParams): Promise<ProjectExperience> {
  return http<ProjectExperience>({
    url: `/api/seeker/update-project-experience/${id}`,
    method: 'PATCH',
    data: params,
  });
}

/**
 * Delete project experience
 */
export function deleteProjectExperience(id: number): Promise<null> {
  return http<null>({
    url: `/api/seeker/delete-project-experience/${id}`,
    method: 'DELETE',
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
  return http<WorkExperience>({
    url: '/api/seeker/add-work-experience',
    method: 'POST',
    data: params,
  });
}

/**
 * Get all work experiences
 */
export function getWorkExperiences(): Promise<WorkExperience[]> {
  return http<WorkExperience[]>({
    url: '/api/seeker/get-work-experiences',
    method: 'GET',
  });
}

/**
 * Update work experience
 */
export function updateWorkExperience(id: number, params: UpdateWorkExperienceParams): Promise<WorkExperience> {
  return http<WorkExperience>({
    url: `/api/seeker/update-work-experience/${id}`,
    method: 'PATCH',
    data: params,
  });
}

/**
 * Delete work experience
 */
export function deleteWorkExperience(id: number): Promise<null> {
  return http<null>({
    url: `/api/seeker/delete-work-experience/${id}`,
    method: 'DELETE',
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
  return http<Skill>({
    url: '/api/seeker/add-skill',
    method: 'POST',
    data: params,
  });
}

/**
 * Get all skills
 */
export function getSkills(): Promise<Skill[]> {
  return http<Skill[]>({
    url: '/api/seeker/get-skills',
    method: 'GET',
  });
}

/**
 * Update skill
 */
export function updateSkill(id: number, params: UpdateSkillParams): Promise<Skill> {
  return http<Skill>({
    url: `/api/seeker/update-skill/${id}`,
    method: 'PATCH',
    data: params,
  });
}

/**
 * Delete skill
 */
export function deleteSkill(id: number): Promise<null> {
  return http<null>({
    url: `/api/seeker/delete-skill/${id}`,
    method: 'DELETE',
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
  return http<JobSeekerExpectation>({
    url: '/api/seeker/add-job-seeker-expectation',
    method: 'POST',
    data: params,
  });
}

/**
 * Get all job seeker expectations
 */
export function getJobSeekerExpectations(): Promise<JobSeekerExpectation[]> {
  return http<JobSeekerExpectation[]>({
    url: '/api/seeker/get-job-seeker-expectations',
    method: 'GET',
  });
}

/**
 * Update job seeker expectation
 */
export function updateJobSeekerExpectation(id: number, params: UpdateJobSeekerExpectationParams): Promise<JobSeekerExpectation> {
  return http<JobSeekerExpectation>({
    url: `/api/seeker/update-job-seeker-expectation/${id}`,
    method: 'PATCH',
    data: params,
  });
}

/**
 * Delete job seeker expectation
 */
export function deleteJobSeekerExpectation(id: number): Promise<null> {
  return http<null>({
    url: `/api/seeker/delete-job-seeker-expectation/${id}`,
    method: 'DELETE',
  });
}

// Resume
export interface Resume {
  id: number;
  jobSeekerId: number;
  resumeName: string;
  privacyLevel: number;
  fileUrl: string;
  completeness: number;
  createdAt: string;
  updatedAt: string;
}

export interface UploadResumeParams {
  file: File | string;
  resumeName: string;
  privacyLevel: number;
}

export interface UpdateResumeParams {
  resumeName?: string;
  privacyLevel?: number;
}

/**
 * Upload resume file
 */
export function uploadResume(params: UploadResumeParams): Promise<Resume> {
  return http<Resume>({
    url: '/api/seeker/upload-resume',
    method: 'POST',
    data: params,
  });
}

/**
 * Get all resumes
 */
export function getResumes(): Promise<Resume[]> {
  return http<Resume[]>({
    url: '/api/seeker/get-resumes',
    method: 'GET',
  });
}

/**
 * Update resume
 */
export function updateResume(id: number, params: UpdateResumeParams): Promise<Resume> {
  return http<Resume>({
    url: `/api/seeker/update-resume/${id}`,
    method: 'PATCH',
    data: params,
  });
}

/**
 * Delete resume
 */
export function deleteResume(id: number): Promise<null> {
  return http<null>({
    url: `/api/seeker/delete-resume/${id}`,
    method: 'DELETE',
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
  return http<SeekerInfo>({
    url: '/api/seeker/register-seeker',
    method: 'POST',
    data: params,
  });
}

/**
 * Get seeker information
 */
export function getSeekerInfo(): Promise<SeekerInfo> {
  return http<SeekerInfo>({
    url: '/api/seeker/get-seeker-info',
    method: 'GET',
  });
}

/**
 * Update seeker basic information
 */
export function updateSeekerInfo(params: Partial<SeekerInfo>): Promise<SeekerInfo> {
  return http<SeekerInfo>({
    url: '/api/seeker/update-seeker-info',
    method: 'PATCH',
    data: params,
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
