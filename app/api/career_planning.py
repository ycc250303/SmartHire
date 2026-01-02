import logging
import json
from typing import Dict, Any, Optional, AsyncGenerator
from datetime import datetime, timedelta
from fastapi import APIRouter, Depends, HTTPException, status, Query
from fastapi.responses import StreamingResponse
from sqlalchemy.orm import Session
from app.core.auth import get_current_user_id
from app.core.config import config
from app.db.connection import get_db
from app.services.db_service import (
    get_job_info,
    get_job_skills_detailed,
    get_seeker_info,
    get_seeker_education,
    get_seeker_skills,
    get_seeker_work_experience,
    get_seeker_project_experience,
)
from app.services.matching_service import (
    calculate_single_match_score,
    calculate_skill_match,
    calculate_education_match,
)
from app.services.gap_analysis_service import (
    analyze_skill_gap,
    analyze_experience_gap,
    analyze_education_gap,
)
from app.services.llm_service import LLMService

router = APIRouter()
logger = logging.getLogger(__name__)

_cache: Dict[str, tuple] = {}
_analysis_cache: Dict[str, tuple] = {}


def get_cached_plan(user_id: int, job_id: int) -> Optional[Dict[str, Any]]:
    key = f"{user_id}:{job_id}"
    if key in _cache:
        cached_data, timestamp = _cache[key]
        if datetime.now() - timestamp < timedelta(hours=24):
            return cached_data
        else:
            del _cache[key]
    return None


def set_cached_plan(user_id: int, job_id: int, data: Dict[str, Any]):
    key = f"{user_id}:{job_id}"
    _cache[key] = (data, datetime.now())


def get_cached_analysis(user_id: int, job_id: int) -> Optional[Dict[str, Any]]:
    key = f"{user_id}:{job_id}"
    if key in _analysis_cache:
        cached_data, timestamp = _analysis_cache[key]
        if datetime.now() - timestamp < timedelta(hours=24):
            return cached_data
        else:
            del _analysis_cache[key]
    return None


def set_cached_analysis(user_id: int, job_id: int, data: Dict[str, Any]):
    key = f"{user_id}:{job_id}"
    _analysis_cache[key] = (data, datetime.now())


def _prepare_analysis_data(
    db: Session,
    user_id: int,
    job_id: int
) -> Dict[str, Any]:
    logger.info(f"[CareerPlanning] Preparing analysis data: userId={user_id}, jobId={job_id}")
    
    job_info = get_job_info(db, job_id)
    if not job_info:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Job {job_id} not found"
        )
    
    seeker_info = get_seeker_info(db, user_id)
    if not seeker_info:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Seeker for user {user_id} not found"
        )
    
    job_seeker_id = seeker_info["id"]
    job_skills_detailed = get_job_skills_detailed(db, job_id)
    seeker_education = get_seeker_education(db, job_seeker_id)
    seeker_skills = get_seeker_skills(db, job_seeker_id)
    seeker_work_experiences = get_seeker_work_experience(db, job_seeker_id)
    seeker_project_experiences = get_seeker_project_experience(db, job_seeker_id)
    
    logger.info(f"[CareerPlanning] Calling calculate_single_match_score: userId={user_id}, jobId={job_id}")
    overall_score = calculate_single_match_score(db, user_id, job_id)
    logger.info(f"[CareerPlanning] Got overall_score={overall_score} for userId={user_id}, jobId={job_id}")
    
    job_skills_list = [s.get("name", "") for s in job_skills_detailed]
    skill_match = calculate_skill_match(seeker_skills, job_skills_list)
    
    seeker_edu_str = None
    if seeker_education:
        seeker_edu_level = seeker_education.get("education")
        if seeker_edu_level is not None:
            education_map = {0: "高中及以下", 1: "专科", 2: "本科", 3: "硕士", 4: "博士"}
            seeker_edu_str = education_map.get(seeker_edu_level, "")
    
    education_match = calculate_education_match(seeker_edu_str, job_info.get("education_required"))
    
    skill_gap = analyze_skill_gap(seeker_skills, job_skills_detailed)
    experience_gap = analyze_experience_gap(
        seeker_work_experiences,
        job_info.get("experience_required")
    )
    education_gap = analyze_education_gap(
        seeker_info.get("education"),
        job_info.get("education_required")
    )
    
    match_analysis = {
        "overall_score": overall_score,
        "skill_match": round(skill_match, 2),
        "education_match": round(education_match, 2),
        "experience_qualified": experience_gap.get("is_qualified", False)
    }
    
    gap_analysis = {
        "skills": skill_gap,
        "experience": experience_gap,
        "education": education_gap
    }
    
    return {
        "match_analysis": match_analysis,
        "gap_analysis": gap_analysis,
        "job_info": job_info,
        "seeker_info": seeker_info,
        "job_skills_detailed": job_skills_detailed,
        "seeker_skills": seeker_skills,
        "seeker_work_experiences": seeker_work_experiences,
        "seeker_project_experiences": seeker_project_experiences,
        "skill_gap": skill_gap
    }


@router.post("/ai/career-planning/{job_id}/analysis")
def get_career_analysis(
    job_id: int,
    force_refresh: bool = Query(False, description="Force refresh cache"),
    user_id: int = Depends(get_current_user_id),
    db: Session = Depends(get_db),
):
    start_time = datetime.now()
    logger.info(f"[CareerPlanning] ========== ANALYSIS ENDPOINT CALLED ==========")
    logger.info(f"[CareerPlanning] Analysis request: userId={user_id}, jobId={job_id}, forceRefresh={force_refresh}")
    logger.info(f"[CareerPlanning] Endpoint: POST /ai/career-planning/{job_id}/analysis")
    
    if not force_refresh:
        cached = get_cached_analysis(user_id, job_id)
        if cached:
            logger.info(f"[CareerPlanning] Analysis cache hit: userId={user_id}, jobId={job_id}")
            return cached
    
    try:
        analysis_data = _prepare_analysis_data(db, user_id, job_id)
        
        response = {
            "match_analysis": analysis_data["match_analysis"],
            "gap_analysis": analysis_data["gap_analysis"]
        }
        
        career_roadmap = None
        learning_plan_structured = None
        
        try:
            llm_service = LLMService()
            
            job_info = analysis_data["job_info"]
            seeker_info = analysis_data["seeker_info"]
            skill_gap = analysis_data["skill_gap"]
            job_skills_detailed = analysis_data["job_skills_detailed"]
            
            education_map = {0: "高中及以下", 1: "专科", 2: "本科", 3: "硕士", 4: "博士"}
            education_text = education_map.get(seeker_info.get("education"), "未知")
            work_years = seeker_info.get("work_experience_year", 0.0)
            
            required_missing = skill_gap.get("required_missing", [])
            optional_missing = skill_gap.get("optional_missing", [])
            matched = skill_gap.get("matched", [])
            all_job_skills = [s.get("name", "") for s in job_skills_detailed]
            
            skill_gaps_count = len(required_missing)
            plan_duration_days = max(skill_gaps_count * 30, 90)
            milestones_count = max(skill_gaps_count, 3)
            
            logger.info(f"[CareerPlanning] Generating career roadmap and structured learning plan")
            llm_start = datetime.now()
            
            roadmap_data = llm_service.generate_career_roadmap(
                job_title=job_info.get("job_title", ""),
                education=education_text,
                work_years=work_years,
                required_missing_skills=required_missing,
                optional_missing_skills=optional_missing,
                matched_skills=matched,
                all_job_skills=all_job_skills
            )
            
            llm_duration = (datetime.now() - llm_start).total_seconds()
            
            if roadmap_data:
                logger.info(f"[CareerPlanning] Career roadmap generated successfully: duration={llm_duration:.2f}s")
                
                current_level = f"{education_text}·{work_years}年经验"
                
                career_roadmap = {
                    "overview": {
                        "target_position": job_info.get("job_title", ""),
                        "current_level": current_level,
                        "skill_gaps_count": skill_gaps_count,
                        "plan_duration_days": plan_duration_days,
                        "milestones_count": milestones_count
                    },
                    "technology_stacks": roadmap_data.get("technology_stacks", []),
                    "phase_roadmap": roadmap_data.get("phase_roadmap", []),
                    "immediate_suggestions": roadmap_data.get("immediate_suggestions", [])
                }
                
                response["career_roadmap"] = career_roadmap
                logger.info(f"[CareerPlanning] Career roadmap added to response: techStacks={len(career_roadmap['technology_stacks'])}, phases={len(career_roadmap['phase_roadmap'])}, suggestions={len(career_roadmap['immediate_suggestions'])}")
            else:
                logger.warning(f"[CareerPlanning] Career roadmap generation returned None")
            
            if required_missing:
                logger.info(f"[CareerPlanning] Generating structured learning plan: requiredMissing={len(required_missing)}")
                llm_start = datetime.now()
                
                learning_plan_data = llm_service.generate_learning_plan_structured(
                    required_missing_skills=required_missing[:5],
                    optional_missing_skills=optional_missing,
                    matched_skills=matched,
                    job_description=job_info.get("description", "")
                )
                
                llm_duration = (datetime.now() - llm_start).total_seconds()
                
                if learning_plan_data:
                    logger.info(f"[CareerPlanning] Structured learning plan generated successfully: duration={llm_duration:.2f}s, skillsCount={len(learning_plan_data.get('skills', []))}")
                    learning_plan_structured = learning_plan_data
                    response["learning_plan_structured"] = learning_plan_structured
                else:
                    logger.warning(f"[CareerPlanning] Structured learning plan generation returned None")
            else:
                logger.info(f"[CareerPlanning] No missing skills, skipping structured learning plan generation")
                
        except Exception as e:
            logger.error(f"[CareerPlanning] LLM generation error: {e}", exc_info=True)
        
        set_cached_analysis(user_id, job_id, response)
        
        total_duration = (datetime.now() - start_time).total_seconds()
        response_json = json.dumps(response, ensure_ascii=False)
        response_size = len(response_json)
        
        logger.info(f"[CareerPlanning] Analysis completed: duration={total_duration:.2f}s, responseSize={response_size} bytes, hasCareerRoadmap={career_roadmap is not None}, hasLearningPlanStructured={learning_plan_structured is not None}")
        logger.info(f"[CareerPlanning] ========== FULL ANALYSIS RESPONSE ==========")
        logger.info(f"[CareerPlanning] {response_json}")
        logger.info(f"[CareerPlanning] ========== END ANALYSIS RESPONSE ==========")
        
        return response
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"[CareerPlanning] Analysis error: {e}", exc_info=True)
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Internal server error: {str(e)}"
        )


@router.get("/ai/career-planning/{job_id}/learning-plan")
async def stream_learning_plan(
    job_id: int,
    user_id: int = Depends(get_current_user_id),
    db: Session = Depends(get_db),
):
    logger.info(f"[CareerPlanning] Learning plan stream request: userId={user_id}, jobId={job_id}")
    
    try:
        analysis_data = _prepare_analysis_data(db, user_id, job_id)
        
        skill_gap = analysis_data["skill_gap"]
        required_missing = skill_gap.get("required_missing", [])
        optional_missing = skill_gap.get("optional_missing", [])
        matched = skill_gap.get("matched", [])
        job_info = analysis_data["job_info"]
        
        llm_service = LLMService()
        
        async def generate_stream() -> AsyncGenerator[str, None]:
            try:
                start_message = config.messages.stream.get("learning_plan_start", "Starting to generate learning plan...")
                yield f"data: {json.dumps({'type': 'start', 'message': start_message}, ensure_ascii=False)}\n\n"
                
                async for chunk in llm_service.generate_learning_plan_stream(
                    required_missing_skills=required_missing,
                    optional_missing_skills=optional_missing,
                    matched_skills=matched,
                    job_description=job_info.get("description", ""),
                    job_requirements=job_info.get("requirements", "")
                ):
                    if chunk:
                        yield f"data: {json.dumps({'type': 'chunk', 'content': chunk}, ensure_ascii=False)}\n\n"
                
                done_message = config.messages.stream.get("done", "Generation completed")
                yield f"data: {json.dumps({'type': 'done'}, ensure_ascii=False)}\n\n"
            except Exception as e:
                logger.error(f"[CareerPlanning] Stream error: {e}", exc_info=True)
                error_message = str(e)
                yield f"data: {json.dumps({'type': 'error', 'message': error_message}, ensure_ascii=False)}\n\n"
        
        return StreamingResponse(
            generate_stream(),
            media_type="text/event-stream",
            headers={
                "Cache-Control": "no-cache",
                "Connection": "keep-alive",
                "X-Accel-Buffering": "no"
            }
        )
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"[CareerPlanning] Learning plan stream error: {e}", exc_info=True)
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Internal server error: {str(e)}"
        )


@router.get("/ai/career-planning/{job_id}/interview-prep")
async def stream_interview_prep(
    job_id: int,
    user_id: int = Depends(get_current_user_id),
    db: Session = Depends(get_db),
):
    logger.info(f"[CareerPlanning] Interview prep stream request: userId={user_id}, jobId={job_id}")
    
    try:
        analysis_data = _prepare_analysis_data(db, user_id, job_id)
        
        skill_gap = analysis_data["skill_gap"]
        all_missing = skill_gap.get("required_missing", []) + skill_gap.get("optional_missing", [])
        job_info = analysis_data["job_info"]
        seeker_project_experiences = analysis_data["seeker_project_experiences"]
        seeker_work_experiences = analysis_data["seeker_work_experiences"]
        
        llm_service = LLMService()
        
        async def generate_stream() -> AsyncGenerator[str, None]:
            try:
                start_message = config.messages.stream.get("interview_prep_start", "Starting to generate interview preparation advice...")
                yield f"data: {json.dumps({'type': 'start', 'message': start_message}, ensure_ascii=False)}\n\n"
                
                async for chunk in llm_service.generate_interview_prep_stream(
                    job_title=job_info.get("job_title", ""),
                    job_requirements=job_info.get("requirements", ""),
                    project_experiences=seeker_project_experiences,
                    work_experiences=seeker_work_experiences,
                    missing_skills=all_missing
                ):
                    if chunk:
                        yield f"data: {json.dumps({'type': 'chunk', 'content': chunk}, ensure_ascii=False)}\n\n"
                
                done_message = config.messages.stream.get("done", "Generation completed")
                yield f"data: {json.dumps({'type': 'done'}, ensure_ascii=False)}\n\n"
            except Exception as e:
                logger.error(f"[CareerPlanning] Stream error: {e}", exc_info=True)
                error_message = str(e)
                yield f"data: {json.dumps({'type': 'error', 'message': error_message}, ensure_ascii=False)}\n\n"
        
        return StreamingResponse(
            generate_stream(),
            media_type="text/event-stream",
            headers={
                "Cache-Control": "no-cache",
                "Connection": "keep-alive",
                "X-Accel-Buffering": "no"
            }
        )
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"[CareerPlanning] Interview prep stream error: {e}", exc_info=True)
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Internal server error: {str(e)}"
        )


@router.post("/ai/career-planning/{job_id}")
def get_career_planning(
    job_id: int,
    force_refresh: bool = Query(False, description="Force refresh cache"),
    user_id: int = Depends(get_current_user_id),
    db: Session = Depends(get_db),
):
    start_time = datetime.now()
    logger.info(f"[CareerPlanning] ========== REQUEST RECEIVED ==========")
    logger.info(f"[CareerPlanning] Request started: userId={user_id}, jobId={job_id}, forceRefresh={force_refresh}")
    logger.info(f"[CareerPlanning] Endpoint: POST /ai/career-planning/{job_id}")
    
    if not force_refresh:
        cached = get_cached_plan(user_id, job_id)
        if cached:
            logger.info(f"[CareerPlanning] Cache hit: userId={user_id}, jobId={job_id}")
            return cached
    
    try:
        logger.info(f"[CareerPlanning] Fetching job info: jobId={job_id}")
        job_info = get_job_info(db, job_id)
        if not job_info:
            logger.warning(f"[CareerPlanning] Job not found: jobId={job_id}")
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=f"Job {job_id} not found"
            )
        logger.info(f"[CareerPlanning] Job info retrieved: jobTitle={job_info.get('job_title')}, city={job_info.get('city')}")
        
        logger.info(f"[CareerPlanning] Fetching seeker info: userId={user_id}")
        seeker_info = get_seeker_info(db, user_id)
        if not seeker_info:
            logger.warning(f"[CareerPlanning] Seeker not found: userId={user_id}")
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=f"Seeker for user {user_id} not found"
            )
        
        job_seeker_id = seeker_info["id"]
        logger.info(f"[CareerPlanning] Seeker info retrieved: jobSeekerId={job_seeker_id}, education={seeker_info.get('education')}, city={seeker_info.get('current_city')}")
        
        logger.info(f"[CareerPlanning] Fetching detailed data: jobSeekerId={job_seeker_id}")
        job_skills_detailed = get_job_skills_detailed(db, job_id)
        seeker_education = get_seeker_education(db, job_seeker_id)
        seeker_skills = get_seeker_skills(db, job_seeker_id)
        seeker_work_experiences = get_seeker_work_experience(db, job_seeker_id)
        seeker_project_experiences = get_seeker_project_experience(db, job_seeker_id)
        
        logger.info(f"[CareerPlanning] Data retrieved: jobSkills={len(job_skills_detailed)}, seekerSkills={len(seeker_skills)}, workExp={len(seeker_work_experiences)}, projectExp={len(seeker_project_experiences)}")
        
        logger.info(f"[CareerPlanning] Calculating match score: userId={user_id}, jobId={job_id}")
        overall_score = calculate_single_match_score(db, user_id, job_id)
        logger.info(f"[CareerPlanning] Match score calculated: overallScore={overall_score}")
        
        job_skills_list = [s.get("name", "") for s in job_skills_detailed]
        logger.debug(f"[CareerPlanning] Calculating skill match: seekerSkills={len(seeker_skills)}, jobSkills={len(job_skills_list)}")
        skill_match = calculate_skill_match(seeker_skills, job_skills_list)
        logger.info(f"[CareerPlanning] Skill match calculated: skillMatch={skill_match}")
        
        seeker_edu_str = None
        if seeker_education:
            seeker_edu_level = seeker_education.get("education")
            if seeker_edu_level is not None:
                education_map = {0: "高中及以下", 1: "专科", 2: "本科", 3: "硕士", 4: "博士"}
                seeker_edu_str = education_map.get(seeker_edu_level, "")
        
        logger.debug(f"[CareerPlanning] Calculating education match: seekerEdu={seeker_edu_str}, jobEduRequired={job_info.get('education_required')}")
        education_match = calculate_education_match(seeker_edu_str, job_info.get("education_required"))
        logger.info(f"[CareerPlanning] Education match calculated: educationMatch={education_match}")
        
        logger.info(f"[CareerPlanning] Starting gap analysis")
        skill_gap = analyze_skill_gap(seeker_skills, job_skills_detailed)
        experience_gap = analyze_experience_gap(
            seeker_work_experiences,
            job_info.get("experience_required")
        )
        education_gap = analyze_education_gap(
            seeker_info.get("education"),
            job_info.get("education_required")
        )
        
        logger.info(f"[CareerPlanning] Gap analysis completed: skillMatchRate={skill_gap.get('match_rate')}, requiredMissing={len(skill_gap.get('required_missing', []))}, experienceQualified={experience_gap.get('is_qualified')}, educationQualified={education_gap.get('is_qualified')}")
        
        match_analysis = {
            "overall_score": overall_score,
            "skill_match": round(skill_match, 2),
            "education_match": round(education_match, 2),
            "experience_qualified": experience_gap.get("is_qualified", False)
        }
        
        gap_analysis = {
            "skills": skill_gap,
            "experience": experience_gap,
            "education": education_gap
        }
        
        logger.info(f"[CareerPlanning] Match analysis prepared: overallScore={match_analysis['overall_score']}, skillMatch={match_analysis['skill_match']}, educationMatch={match_analysis['education_match']}, experienceQualified={match_analysis['experience_qualified']}")
        logger.info(f"[CareerPlanning] Gap analysis prepared: skillMatchRate={gap_analysis['skills'].get('match_rate')}, requiredMissing={len(gap_analysis['skills'].get('required_missing', []))}, optionalMissing={len(gap_analysis['skills'].get('optional_missing', []))}, matched={len(gap_analysis['skills'].get('matched', []))}")
        
        learning_plan = None
        interview_prep = None
        llm_error = None
        
        try:
            llm_service = LLMService()
            
            required_missing = skill_gap.get("required_missing", [])
            optional_missing = skill_gap.get("optional_missing", [])
            matched = skill_gap.get("matched", [])
            
            logger.info(f"[CareerPlanning] Calling LLM for learning plan: requiredMissing={len(required_missing)}, optionalMissing={len(optional_missing)}, matched={len(matched)}")
            llm_start = datetime.now()
            learning_plan_raw = llm_service.generate_learning_plan(
                required_missing_skills=required_missing,
                optional_missing_skills=optional_missing,
                matched_skills=matched,
                job_description=job_info.get("description", ""),
                job_requirements=job_info.get("requirements", "")
            )
            llm_duration = (datetime.now() - llm_start).total_seconds()
            
            if learning_plan_raw:
                logger.info(f"[CareerPlanning] ✅ LLM learning plan received: length={len(learning_plan_raw)}, duration={llm_duration:.2f}s")
                logger.info(f"[CareerPlanning] LLM learning plan content preview (first 200 chars): {learning_plan_raw[:200]}")
                learning_plan_parsed = llm_service.parse_learning_plan(learning_plan_raw)
                logger.info(f"[CareerPlanning] Learning plan parsed: skillsCount={len(learning_plan_parsed) if learning_plan_parsed else 0}")
                learning_plan = {
                    "skills": learning_plan_parsed if learning_plan_parsed else [],
                    "raw_text": learning_plan_raw
                }
                if not learning_plan_parsed:
                    logger.warning(f"[CareerPlanning] Failed to parse learning plan, returning raw text only")
            else:
                logger.warning(f"[CareerPlanning] LLM learning plan returned empty")
            
            all_missing = required_missing + optional_missing
            logger.info(f"[CareerPlanning] Calling LLM for interview prep: missingSkills={len(all_missing)}, projects={len(seeker_project_experiences)}, workExp={len(seeker_work_experiences)}")
            llm_start = datetime.now()
            interview_prep_raw = llm_service.generate_interview_prep(
                job_title=job_info.get("job_title", ""),
                job_requirements=job_info.get("requirements", ""),
                project_experiences=seeker_project_experiences,
                work_experiences=seeker_work_experiences,
                missing_skills=all_missing
            )
            llm_duration = (datetime.now() - llm_start).total_seconds()
            
            if interview_prep_raw:
                logger.info(f"[CareerPlanning] ✅ LLM interview prep received: length={len(interview_prep_raw)}, duration={llm_duration:.2f}s")
                logger.info(f"[CareerPlanning] LLM interview prep content preview (first 200 chars): {interview_prep_raw[:200]}")
                interview_prep_parsed = llm_service._parse_interview_prep(interview_prep_raw)
                logger.info(f"[CareerPlanning] Interview prep parsed: hasProjectTips={bool(interview_prep_parsed.get('project_tips'))}, hasQuestions={bool(interview_prep_parsed.get('possible_questions'))}")
                interview_prep = {
                    "project_tips": interview_prep_parsed.get("project_tips", ""),
                    "possible_questions": interview_prep_parsed.get("possible_questions", ""),
                    "weakness_strategy": interview_prep_parsed.get("weakness_strategy", ""),
                    "strength_emphasis": interview_prep_parsed.get("strength_emphasis", ""),
                    "raw_text": interview_prep_raw
                }
            else:
                logger.warning(f"[CareerPlanning] LLM interview prep returned empty")
                
        except Exception as e:
            logger.error(f"[CareerPlanning] LLM service error: {e}", exc_info=True)
            llm_error = "LLM service temporarily unavailable"
        
        response = {
            "match_analysis": match_analysis,
            "gap_analysis": gap_analysis,
            "learning_plan": learning_plan,
            "interview_prep": interview_prep
        }
        
        if llm_error:
            response["error"] = llm_error
        
        set_cached_plan(user_id, job_id, response)
        
        total_duration = (datetime.now() - start_time).total_seconds()
        
        import json
        response_json = json.dumps(response, ensure_ascii=False)
        response_size = len(response_json)
        
        logger.info(f"[CareerPlanning] ========== RESPONSE SUMMARY ==========")
        logger.info(f"[CareerPlanning] Request completed: userId={user_id}, jobId={job_id}, duration={total_duration:.2f}s, responseSize={response_size} bytes")
        logger.info(f"[CareerPlanning] Response structure: hasMatchAnalysis=True, hasGapAnalysis=True, hasLearningPlan={learning_plan is not None}, hasInterviewPrep={interview_prep is not None}, hasError={llm_error is not None}")
        logger.info(f"[CareerPlanning] ✅ MATCH ANALYSIS VALUES: overallScore={match_analysis.get('overall_score')}, skillMatch={match_analysis.get('skill_match')}, educationMatch={match_analysis.get('education_match')}, experienceQualified={match_analysis.get('experience_qualified')}")
        logger.info(f"[CareerPlanning] ✅ GAP ANALYSIS VALUES: skillMatchRate={gap_analysis['skills'].get('match_rate')}, requiredMissing={len(gap_analysis['skills'].get('required_missing', []))}, optionalMissing={len(gap_analysis['skills'].get('optional_missing', []))}, matched={len(gap_analysis['skills'].get('matched', []))}")
        logger.info(f"[CareerPlanning] ✅ EXPERIENCE GAP: yourYears={gap_analysis['experience'].get('your_years')}, requiredLevel={gap_analysis['experience'].get('required_level')}, isQualified={gap_analysis['experience'].get('is_qualified')}, gapYears={gap_analysis['experience'].get('gap_years')}")
        logger.info(f"[CareerPlanning] ✅ EDUCATION GAP: yourText={gap_analysis['education'].get('your_text')}, requiredText={gap_analysis['education'].get('required_text')}, isQualified={gap_analysis['education'].get('is_qualified')}, matchScore={gap_analysis['education'].get('match_score')}")
        logger.info(f"[CareerPlanning] ✅ LLM DATA: learningPlanSkills={len(learning_plan.get('skills', [])) if learning_plan else 0}, learningPlanRawLength={len(learning_plan.get('raw_text', '')) if learning_plan else 0}, interviewPrepRawLength={len(interview_prep.get('raw_text', '')) if interview_prep else 0}, llmError={llm_error}")
        logger.info(f"[CareerPlanning] ========== RETURNING RESPONSE ==========")
        logger.debug(f"[CareerPlanning] Full response JSON (first 1000 chars): {response_json[:1000]}")
        
        if llm_error and not learning_plan and not interview_prep:
            raise HTTPException(
                status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
                detail=llm_error
            )
        
        logger.info(f"[CareerPlanning] Returning response: statusCode=200, responseSize={response_size} bytes, overallScore={match_analysis.get('overall_score')}, skillMatch={match_analysis.get('skill_match')}")
        
        if not response or not isinstance(response, dict):
            logger.error(f"[CareerPlanning] CRITICAL: Response is empty or invalid: {response}")
            raise HTTPException(
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
                detail="Response generation failed"
            )
        
        if not response.get("match_analysis") or not response.get("gap_analysis"):
            logger.error(f"[CareerPlanning] CRITICAL: Response missing required fields: {list(response.keys())}")
            raise HTTPException(
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
                detail="Response missing required fields"
            )
        
        logger.info(f"[CareerPlanning] ========== FULL RESPONSE BODY ==========")
        logger.info(f"[CareerPlanning] {response_json}")
        logger.info(f"[CareerPlanning] ========== END RESPONSE BODY ==========")
        
        return response
        
    except HTTPException as e:
        logger.error(f"[CareerPlanning] HTTPException raised: status={e.status_code}, detail={e.detail}")
        raise
    except Exception as e:
        logger.error(f"[CareerPlanning] Unexpected error: {type(e).__name__}: {e}", exc_info=True)
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Internal server error: {str(e)}"
        )

