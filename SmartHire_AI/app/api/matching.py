import logging
from typing import Dict, Any, List
from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from pydantic import BaseModel
from app.core.auth import get_current_user_id
from app.db.connection import get_db
from app.services.matching_service import (
    calculate_single_match_score,
    calculate_batch_match_scores,
)
from app.services.precompute_service import precompute_seeker_match_scores
from app.services.precompute_service import precompute_seeker_match_scores

router = APIRouter()
logger = logging.getLogger(__name__)


class CalculateMatchScoreRequest(BaseModel):
    jobId: int


class CalculateMatchScoreResponse(BaseModel):
    matchScore: int


class SkillInfo(BaseModel):
    name: str
    level: int = None


class WorkExperience(BaseModel):
    companyName: str = None
    position: str = None
    description: str = None


class ProjectExperience(BaseModel):
    projectName: str = None
    description: str = None
    responsibilities: str = None


class SeekerProfile(BaseModel):
    jobSeekerId: int = None
    major: str = None
    university: str = None
    highestEducation: str = None
    currentCity: str = None
    skills: List[SkillInfo] = []
    workExperiences: List[WorkExperience] = []
    projectExperiences: List[ProjectExperience] = []


class JobInfo(BaseModel):
    jobId: int
    jobTitle: str = None
    description: str = None
    responsibilities: str = None
    requirements: str = None
    skills: List[str] = []
    educationRequired: Any = None


class BatchMatchScoreRequest(BaseModel):
    seekerProfile: SeekerProfile
    jobs: List[JobInfo]


class MatchScoreDetail(BaseModel):
    skillMatch: float
    descriptionMatch: float
    educationMatch: float


class JobMatchScore(BaseModel):
    jobId: int
    matchScore: int
    similarity: float
    details: MatchScoreDetail


class BatchMatchScoreResponse(BaseModel):
    scores: List[JobMatchScore]


@router.post(
    "/recruitment/seeker/calculate-match-score",
    response_model=CalculateMatchScoreResponse,
)
def calculate_match_score_endpoint(
    request: CalculateMatchScoreRequest,
    user_id: int = Depends(get_current_user_id),
    db: Session = Depends(get_db),
):
    logger.info(f"Calculate match score: userId={user_id}, jobId={request.jobId}")
    try:
        match_score = calculate_single_match_score(db, user_id, request.jobId)
        logger.info(f"Match score calculated: userId={user_id}, jobId={request.jobId}, score={match_score}")
        return CalculateMatchScoreResponse(matchScore=match_score)
    except ValueError as e:
        logger.warning(f"Match score calculation failed: {e}")
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=str(e),
        )
    except Exception as e:
        logger.error(f"Match score calculation error: {e}", exc_info=True)
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Internal server error: {str(e)}",
        )


@router.post(
    "/recruitment/seeker/batch-calculate-match-scores",
    response_model=BatchMatchScoreResponse,
)
def batch_calculate_match_scores_endpoint(
    request: BatchMatchScoreRequest,
    user_id: int = Depends(get_current_user_id),
    db: Session = Depends(get_db),
):
    job_count = len(request.jobs)
    logger.info(f"Batch calculate match scores: userId={user_id}, jobCount={job_count}")
    try:
        seeker_profile_dict = request.seekerProfile.dict()
        jobs_dict = [job.dict() for job in request.jobs]
        
        scores = calculate_batch_match_scores(seeker_profile_dict, jobs_dict, user_id, db)
        
        logger.info(f"Batch match scores calculated: userId={user_id}, jobCount={job_count}, resultCount={len(scores)}")
        
        return BatchMatchScoreResponse(
            scores=[
                JobMatchScore(
                    jobId=score["jobId"],
                    matchScore=score["matchScore"],
                    similarity=score["similarity"],
                    details=MatchScoreDetail(
                        skillMatch=score["details"]["skillMatch"],
                        descriptionMatch=score["details"]["descriptionMatch"],
                        educationMatch=score["details"]["educationMatch"],
                    )
                )
                for score in scores
            ]
        )
    except Exception as e:
        logger.error(f"Batch match scores calculation error: {e}", exc_info=True)
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Internal server error: {str(e)}",
        )


@router.post("/recruitment/seeker/precompute-match-scores")
def precompute_match_scores_endpoint(
    user_id: int = Depends(get_current_user_id),
    db: Session = Depends(get_db),
):
    logger.info(f"Precomputing match scores for user {user_id}")
    try:
        count = precompute_seeker_match_scores(db, user_id)
        return {"message": f"Precomputed {count} match scores", "count": count}
    except Exception as e:
        logger.error(f"Precompute error: {e}", exc_info=True)
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Internal server error: {str(e)}",
        )
