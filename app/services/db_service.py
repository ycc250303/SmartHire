import logging
from typing import Optional, List, Dict, Any
from datetime import datetime
from dateutil.relativedelta import relativedelta
from sqlalchemy.orm import Session
from sqlalchemy import desc
from app.db.models import (
    JobInfo,
    JobSkillRequirement,
    JobSeeker,
    User,
    EducationExperience,
    Skill,
    WorkExperience,
    ProjectExperience,
    HrInfo,
    JobSeekerExpectation,
)

logger = logging.getLogger(__name__)


def get_job_info(db: Session, job_id: int) -> Optional[Dict[str, Any]]:
    logger.debug(f"[DBService] Fetching job info: jobId={job_id}")
    
    job = (
        db.query(JobInfo)
        .filter(
            JobInfo.id == job_id,
            JobInfo.status == 1,
            # TODO
            # JobInfo.audit_status == "approved",
            # JobInfo.company_audit_status == "approved",
        )
        .first()
    )
    
    if not job:
        logger.warning(f"[DBService] Job not found or not approved: jobId={job_id}")
        return None
    
    result = {
        "id": job.id,
        "job_title": job.job_title,
        "description": job.description or "",
        "responsibilities": job.responsibilities or "",
        "requirements": job.requirements or "",
        "education_required": job.education_required,
        "experience_required": job.experience_required,
        "salary_min": float(job.salary_min) if job.salary_min else None,
        "salary_max": float(job.salary_max) if job.salary_max else None,
        "city": job.city,
    }
    
    logger.debug(f"[DBService] Job info retrieved: jobTitle={result['job_title']}, city={result['city']}, eduRequired={result['education_required']}, expRequired={result['experience_required']}")
    
    return result


def get_job_skills(db: Session, job_id: int) -> List[str]:
    skills = (
        db.query(JobSkillRequirement.skill_name)
        .filter(JobSkillRequirement.job_id == job_id)
        .order_by(desc(JobSkillRequirement.is_required), JobSkillRequirement.id)
        .all()
    )
    return [skill[0] for skill in skills if skill[0]]


def get_job_skills_detailed(db: Session, job_id: int) -> List[Dict[str, Any]]:
    logger.debug(f"[DBService] Fetching job skills detailed: jobId={job_id}")
    
    skills = (
        db.query(JobSkillRequirement.skill_name, JobSkillRequirement.is_required)
        .filter(JobSkillRequirement.job_id == job_id)
        .order_by(desc(JobSkillRequirement.is_required), JobSkillRequirement.id)
        .all()
    )
    
    result = [
        {"name": skill[0], "is_required": bool(skill[1])}
        for skill in skills if skill[0]
    ]
    
    required_count = sum(1 for s in result if s["is_required"])
    logger.debug(f"[DBService] Job skills retrieved: total={len(result)}, required={required_count}, optional={len(result) - required_count}")
    
    return result


def get_seeker_info(db: Session, user_id: int) -> Optional[Dict[str, Any]]:
    logger.debug(f"[DBService] Fetching seeker info: userId={user_id}")
    
    seeker = (
        db.query(JobSeeker)
        .join(User, JobSeeker.user_id == User.id)
        .filter(User.id == user_id)
        .first()
    )
    
    if not seeker:
        logger.warning(f"[DBService] Seeker not found: userId={user_id}")
        return None
    
    result = {
        "id": seeker.id,
        "education": seeker.education,
        "current_city": seeker.current_city,
    }
    
    logger.debug(f"[DBService] Seeker info retrieved: jobSeekerId={result['id']}, education={result['education']}, city={result['current_city']}")
    
    return result


def get_seeker_education(db: Session, job_seeker_id: int) -> Optional[Dict[str, Any]]:
    education = (
        db.query(EducationExperience)
        .filter(EducationExperience.job_seeker_id == job_seeker_id)
        .order_by(desc(EducationExperience.education), desc(EducationExperience.end_year))
        .first()
    )
    
    if not education:
        return None
    
    return {
        "school_name": education.school_name or "",
        "major": education.major or "",
        "education": education.education,
    }


def get_seeker_skills(db: Session, job_seeker_id: int) -> List[Dict[str, Any]]:
    logger.debug(f"[DBService] Fetching seeker skills: jobSeekerId={job_seeker_id}")
    
    skills = (
        db.query(Skill)
        .filter(Skill.job_seeker_id == job_seeker_id)
        .order_by(desc(Skill.updated_at))
        .all()
    )
    
    result = [
        {"name": skill.skill_name, "level": skill.level}
        for skill in skills
        if skill.skill_name
    ]
    
    logger.debug(f"[DBService] Seeker skills retrieved: count={len(result)}")
    
    return result


def get_seeker_work_experience(
    db: Session, job_seeker_id: int
) -> List[Dict[str, Any]]:
    experiences = (
        db.query(WorkExperience)
        .filter(WorkExperience.job_seeker_id == job_seeker_id)
        .order_by(desc(WorkExperience.end_date), desc(WorkExperience.start_date))
        .all()
    )
    return [
        {
            "company_name": exp.company_name or "",
            "position": exp.position or "",
            "description": exp.description or "",
            "start_date": exp.start_date,
            "end_date": exp.end_date,
        }
        for exp in experiences
    ]


def calculate_work_years(work_experiences: List[Dict[str, Any]]) -> float:
    logger.debug(f"[DBService] Calculating work years: experienceCount={len(work_experiences)}")
    
    total_months = 0
    for exp in work_experiences:
        start_date = exp.get("start_date")
        end_date = exp.get("end_date") or datetime.now()
        
        if start_date:
            delta = relativedelta(end_date, start_date)
            months = delta.years * 12 + delta.months
            total_months += months
            logger.debug(f"[DBService] Work experience: {exp.get('company_name', '')} {exp.get('position', '')} - {months} months")
    
    result = round(total_months / 12, 1) if total_months > 0 else 0.0
    logger.debug(f"[DBService] Total work years calculated: {result} years ({total_months} months)")
    
    return result


def get_seeker_project_experience(
    db: Session, job_seeker_id: int
) -> List[Dict[str, Any]]:
    projects = (
        db.query(ProjectExperience)
        .filter(ProjectExperience.job_seeker_id == job_seeker_id)
        .order_by(desc(ProjectExperience.end_date), desc(ProjectExperience.start_date))
        .limit(3)
        .all()
    )
    return [
        {
            "project_name": proj.project_name or "",
            "description": proj.description or "",
            "responsibility": proj.responsibility or "",
        }
        for proj in projects
    ]


def get_candidate_info_by_id(db: Session, job_seeker_id: int) -> Optional[Dict[str, Any]]:
    """通过job_seeker_id获取候选人信息（用于HR端）"""
    logger.debug(f"[DBService] Fetching candidate info: jobSeekerId={job_seeker_id}")
    
    seeker = db.query(JobSeeker).filter(JobSeeker.id == job_seeker_id).first()
    
    if not seeker:
        logger.warning(f"[DBService] Candidate not found: jobSeekerId={job_seeker_id}")
        return None
    
    education_map = {0: "高中及以下", 1: "专科", 2: "本科", 3: "硕士", 4: "博士"}
    education_text = education_map.get(seeker.education, "未知")
    
    result = {
        "job_seeker_id": seeker.id,
        "real_name": seeker.real_name or "",
        "education": seeker.education,
        "education_text": education_text,
        "work_experience_year": seeker.work_experience_year or 0,
        "current_city": seeker.current_city or "",
    }
    
    logger.debug(f"[DBService] Candidate info retrieved: jobSeekerId={result['job_seeker_id']}, name={result['real_name']}, education={result['education_text']}")
    
    return result


def verify_hr_job_access(db: Session, hr_user_id: int, job_id: int) -> bool:
    """Verify if HR has permission to access the job position"""
    logger.debug(f"[DBService] Verifying HR job access: hrUserId={hr_user_id}, jobId={job_id}")
    
    hr_info = db.query(HrInfo).filter(HrInfo.user_id == hr_user_id).first()
    if not hr_info:
        logger.warning(f"[DBService] HR not found: userId={hr_user_id}")
        return False
    
    job = db.query(JobInfo).filter(JobInfo.id == job_id).first()
    if not job:
        logger.warning(f"[DBService] Job not found: jobId={job_id}")
        return False
    
    has_access = job.hr_id == hr_info.id
    
    logger.debug(
        f"[DBService] HR job access: {has_access} "
        f"(hrId={hr_info.id}, jobHrId={job.hr_id})"
    )
    
    return has_access


def get_seeker_expectation(db: Session, job_seeker_id: int) -> Optional[Dict[str, Any]]:
    logger.debug(f"[DBService] Fetching seeker expectation: jobSeekerId={job_seeker_id}")
    
    expectation = (
        db.query(JobSeekerExpectation)
        .filter(JobSeekerExpectation.job_seeker_id == job_seeker_id)
        .first()
    )
    
    if not expectation:
        logger.debug(f"[DBService] Seeker expectation not found: jobSeekerId={job_seeker_id}")
        return None
    
    result = {
        "expected_position": expectation.expected_position or "",
        "work_city": expectation.work_city or "",
        "salary_min": float(expectation.salary_min) if expectation.salary_min else None,
        "salary_max": float(expectation.salary_max) if expectation.salary_max else None,
    }
    
    logger.debug(f"[DBService] Seeker expectation retrieved: position={result['expected_position']}, city={result['work_city']}")
    
    return result


def get_seeker_full_profile(db: Session, user_id: int) -> Optional[Dict[str, Any]]:
    logger.debug(f"[DBService] Fetching seeker full profile: userId={user_id}")
    
    seeker_info = get_seeker_info(db, user_id)
    if not seeker_info:
        return None
    
    job_seeker_id = seeker_info["id"]
    
    education = get_seeker_education(db, job_seeker_id)
    skills = get_seeker_skills(db, job_seeker_id)
    work_experiences = get_seeker_work_experience(db, job_seeker_id)
    project_experiences = get_seeker_project_experience(db, job_seeker_id)
    expectation = get_seeker_expectation(db, job_seeker_id)
    
    work_years = calculate_work_years(work_experiences)
    
    education_map = {0: "高中及以下", 1: "专科", 2: "本科", 3: "硕士", 4: "博士"}
    education_text = education_map.get(seeker_info.get("education"), "未知")
    
    result = {
        "seeker_info": seeker_info,
        "education": education,
        "education_text": education_text,
        "skills": skills,
        "work_experiences": work_experiences,
        "project_experiences": project_experiences,
        "expectation": expectation,
        "work_years": work_years,
    }
    
    logger.debug(f"[DBService] Seeker full profile retrieved: education={education_text}, workYears={work_years}, skillsCount={len(skills)}, workExpCount={len(work_experiences)}, projectCount={len(project_experiences)}")
    
    return result

