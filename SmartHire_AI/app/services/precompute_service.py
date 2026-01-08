import logging
from typing import List
from sqlalchemy.orm import Session
from app.services.db_service import (
    get_job_info,
    get_job_skills,
    get_seeker_info,
    get_seeker_education,
    get_seeker_skills,
    get_seeker_work_experience,
    get_seeker_project_experience,
)
from app.services.vector_service import (
    embed_text,
    calculate_cosine_similarity,
    build_seeker_text,
    build_job_text,
)
from app.services.vector_db_service import store_match_score
from app.db.models import JobInfo, JobSeeker

logger = logging.getLogger(__name__)


def get_all_active_job_ids(db: Session) -> List[int]:
    try:
        jobs = (
            db.query(JobInfo.id)
            .filter(
                JobInfo.status == 1,
                JobInfo.audit_status == "approved",
                JobInfo.company_audit_status == "approved",
            )
            .all()
        )
        return [job[0] for job in jobs]
    except Exception as e:
        logger.error(f"Failed to get all active job IDs: {e}")
        return []


def precompute_seeker_match_scores(db: Session, user_id: int) -> int:
    seeker_info = get_seeker_info(db, user_id)
    if not seeker_info:
        logger.warning(f"Seeker not found for user {user_id}")
        return 0
    
    job_seeker_id = seeker_info["id"]
    
    seeker_education = get_seeker_education(db, job_seeker_id)
    seeker_skills = get_seeker_skills(db, job_seeker_id)
    seeker_work_experiences = get_seeker_work_experience(db, job_seeker_id)
    seeker_project_experiences = get_seeker_project_experience(db, job_seeker_id)
    
    major = seeker_education.get("major", "") if seeker_education else ""
    university = seeker_education.get("school_name", "") if seeker_education else ""
    
    seeker_text = build_seeker_text(
        major=major,
        university=university,
        skills=seeker_skills,
        work_experiences=seeker_work_experiences,
        project_experiences=seeker_project_experiences,
    )
    
    seeker_embedding = embed_text(seeker_text)
    
    job_ids = get_all_active_job_ids(db)
    logger.info(f"Precomputing match scores for user {user_id} with {len(job_ids)} jobs")
    
    computed_count = 0
    
    for job_id in job_ids:
        try:
            job_info = get_job_info(db, job_id)
            if not job_info:
                continue
            
            job_skills = get_job_skills(db, job_id)
            
            job_text = build_job_text(
                job_title=job_info["job_title"],
                description=job_info["description"],
                responsibilities=job_info["responsibilities"],
                requirements=job_info["requirements"],
                skills=job_skills,
            )
            
            job_embedding = embed_text(job_text)
            similarity = calculate_cosine_similarity(seeker_embedding, job_embedding)
            match_score = int(round(similarity * 100))
            match_score = max(0, min(100, match_score))
            
            seeker_entity = (
                db.query(JobSeeker)
                .filter(JobSeeker.user_id == user_id)
                .first()
            )
            
            job_entity = (
                db.query(JobInfo)
                .filter(JobInfo.id == job_id)
                .first()
            )
            
            seeker_updated_at = seeker_entity.updated_at if seeker_entity else None
            job_updated_at = job_entity.updated_at if job_entity else None
            
            store_match_score(
                seeker_id=user_id,
                job_id=job_id,
                similarity_score=similarity,
                match_score=match_score,
                seeker_updated_at=seeker_updated_at,
                job_updated_at=job_updated_at,
                seeker_vector=seeker_embedding,
                job_vector=job_embedding,
            )
            
            computed_count += 1
            
            if computed_count % 10 == 0:
                logger.info(f"Precomputed {computed_count}/{len(job_ids)} match scores for user {user_id}")
        
        except Exception as e:
            logger.error(f"Failed to precompute match score for user {user_id}, job {job_id}: {e}")
            continue
    
    logger.info(f"Precomputed {computed_count} match scores for user {user_id}")
    return computed_count

