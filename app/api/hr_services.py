import logging
import json
from typing import Dict, Any, Optional, AsyncGenerator
from datetime import datetime, timedelta
from fastapi import APIRouter, Depends, HTTPException, status, Query
from fastapi.responses import StreamingResponse
from sqlalchemy.orm import Session
from app.core.auth import get_current_hr_user
from app.core.config import config
from app.db.connection import get_db
from app.services.db_service import (
    get_job_info,
    get_job_skills_detailed,
    get_candidate_info_by_id,
    get_candidate_info_by_seeker_or_user,
    get_seeker_education,
    get_seeker_skills,
    get_seeker_work_experience,
    get_seeker_project_experience,
    verify_hr_job_access,
    calculate_work_years,
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


def get_cached_data(key: str) -> Optional[Dict[str, Any]]:
    if key in _cache:
        cached_data, timestamp = _cache[key]
        if datetime.now() - timestamp < timedelta(hours=24):
            return cached_data
        else:
            del _cache[key]
    return None


def set_cached_data(key: str, data: Dict[str, Any]):
    _cache[key] = (data, datetime.now())


def _prepare_candidate_analysis_data(
    db: Session,
    job_seeker_id: int,
    job_id: int,
    hr_user_id: int,
    seeker_user_id: Optional[int] = None,
) -> Dict[str, Any]:
    """准备候选人分析数据"""
    logger.info(f"[HRService] Preparing candidate analysis: jobSeekerId={job_seeker_id}, jobId={job_id}, hrUserId={hr_user_id}")
    
    # 验证HR权限
    if not verify_hr_job_access(db, hr_user_id, job_id):
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Access denied. You don't have permission to access this job."
        )
    
    # 获取岗位信息
    job_info = get_job_info(db, job_id)
    if not job_info:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Job {job_id} not found"
        )
    
    # 获取候选人信息（支持 job_seeker_id 或 user_id）
    candidate_info, resolved_job_seeker_id, candidate_user_id = get_candidate_info_by_seeker_or_user(
        db, job_seeker_id=job_seeker_id, user_id=seeker_user_id
    )
    if not candidate_info or not resolved_job_seeker_id:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Candidate not found"
        )
    
    # 获取详细数据
    job_skills_detailed = get_job_skills_detailed(db, job_id)
    seeker_education = get_seeker_education(db, resolved_job_seeker_id)
    seeker_skills = get_seeker_skills(db, resolved_job_seeker_id)
    seeker_work_experiences = get_seeker_work_experience(db, resolved_job_seeker_id)
    seeker_project_experiences = get_seeker_project_experience(db, resolved_job_seeker_id)
    
    # 计算匹配分数
    overall_score = calculate_single_match_score(db, candidate_user_id, job_id)
    
    job_skills_list = [s.get("name", "") for s in job_skills_detailed]
    skill_match = calculate_skill_match(seeker_skills, job_skills_list)
    
    seeker_edu_str = None
    if seeker_education:
        seeker_edu_level = seeker_education.get("education")
        if seeker_edu_level is not None:
            education_map = {0: "高中及以下", 1: "专科", 2: "本科", 3: "硕士", 4: "博士"}
            seeker_edu_str = education_map.get(seeker_edu_level, "")
    
    education_match = calculate_education_match(seeker_edu_str, job_info.get("education_required"))
    
    # 差距分析
    skill_gap = analyze_skill_gap(seeker_skills, job_skills_detailed)
    experience_gap = analyze_experience_gap(
        seeker_work_experiences,
        job_info.get("experience_required")
    )
    education_gap = analyze_education_gap(
        candidate_info.get("education"),
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
        "candidate_info": candidate_info,
        "job_info": job_info,
        "job_skills_detailed": job_skills_detailed,
        "seeker_skills": seeker_skills,
        "seeker_work_experiences": seeker_work_experiences,
        "seeker_project_experiences": seeker_project_experiences,
        "skill_gap": skill_gap,
        "candidate_user_id": candidate_user_id,
        "job_seeker_id": resolved_job_seeker_id,
    }


@router.post("/ai/hr/candidate/{job_seeker_id}/analysis")
def get_candidate_analysis(
    job_seeker_id: int,
    job_id: int = Query(..., description="岗位ID"),
    force_refresh: bool = Query(False, description="Force refresh cache"),
    user_id: Optional[int] = Query(None, alias="userId", description="候选人userId，可替代job_seeker_id"),
    hr_user_id: int = Depends(get_current_hr_user),
    db: Session = Depends(get_db),
):
    """候选人匹配分析"""
    logger.info(f"[HRService] Candidate analysis request: jobSeekerId={job_seeker_id}, jobId={job_id}, hrUserId={hr_user_id}")
    
    try:
        analysis_data = _prepare_candidate_analysis_data(
            db, job_seeker_id, job_id, hr_user_id, seeker_user_id=user_id
        )
        
        resolved_job_seeker_id = analysis_data.get("job_seeker_id", job_seeker_id)
        cache_key = f"hr_analysis:{hr_user_id}:{resolved_job_seeker_id}:{job_id}"

        if not force_refresh:
            cached = get_cached_data(cache_key)
            if cached:
                logger.info(f"[HRService] Analysis cache hit: jobSeekerId={resolved_job_seeker_id}, jobId={job_id}")
                return cached

        candidate_info = analysis_data["candidate_info"]
        response = {
            "match_analysis": analysis_data["match_analysis"],
            "gap_analysis": analysis_data["gap_analysis"],
            "candidate_info": {
                "job_seeker_id": candidate_info.get("job_seeker_id"),
                "user_id": candidate_info.get("user_id"),
                "real_name": candidate_info.get("real_name", ""),
                "education": candidate_info.get("education_text", ""),
                "work_experience_year": candidate_info.get("work_experience_year", 0),
                "current_city": candidate_info.get("current_city", "")
            },
            "job_info": {
                "job_id": analysis_data["job_info"]["id"],
                "job_title": analysis_data["job_info"]["job_title"],
                "city": analysis_data["job_info"]["city"],
                "education_required": analysis_data["job_info"]["education_required"],
                "experience_required": analysis_data["job_info"]["experience_required"],
            }
        }
        
        set_cached_data(cache_key, response)
        return response
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"[HRService] Analysis error: {e}", exc_info=True)
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Internal server error: {str(e)}"
        )


@router.post("/ai/hr/candidate/{job_seeker_id}/evaluation")
def get_candidate_evaluation(
    job_seeker_id: int,
    job_id: int = Query(..., description="岗位ID"),
    force_refresh: bool = Query(False, description="Force refresh cache"),
    user_id: Optional[int] = Query(None, alias="userId", description="候选人userId，可替代job_seeker_id"),
    hr_user_id: int = Depends(get_current_hr_user),
    db: Session = Depends(get_db),
):
    """候选人综合评估报告"""
    logger.info(f"[HRService] Candidate evaluation request: jobSeekerId={job_seeker_id}, jobId={job_id}, hrUserId={hr_user_id}")
    
    try:
        analysis_data = _prepare_candidate_analysis_data(
            db, job_seeker_id, job_id, hr_user_id, seeker_user_id=user_id
        )

        resolved_job_seeker_id = analysis_data.get("job_seeker_id", job_seeker_id)
        cache_key = f"hr_evaluation:{hr_user_id}:{resolved_job_seeker_id}:{job_id}"

        if not force_refresh:
            cached = get_cached_data(cache_key)
            if cached:
                logger.info(f"[HRService] Evaluation cache hit: jobSeekerId={resolved_job_seeker_id}, jobId={job_id}")
                return cached
        
        candidate_info = analysis_data["candidate_info"]
        job_info = analysis_data["job_info"]
        skill_gap = analysis_data["skill_gap"]
        seeker_skills = analysis_data["seeker_skills"]
        seeker_work_experiences = analysis_data["seeker_work_experiences"]
        seeker_project_experiences = analysis_data["seeker_project_experiences"]
        match_score = analysis_data["match_analysis"]["overall_score"]
        
        # 准备LLM输入数据
        skills_text = ", ".join([s.get("name", "") for s in seeker_skills]) if seeker_skills else "无"
        
        projects_text = ""
        for proj in seeker_project_experiences[:3]:
            projects_text += f"{proj.get('project_name', '')}: {proj.get('description', '')}\n"
        if not projects_text:
            projects_text = "无"
        
        work_text = ""
        for work in seeker_work_experiences[:2]:
            work_text += f"{work.get('company_name', '')} {work.get('position', '')}: {work.get('description', '')}\n"
        if not work_text:
            work_text = "无"
        
        required_skills = ", ".join([s.get("name", "") for s in analysis_data["job_skills_detailed"] if s.get("is_required")]) if analysis_data.get("job_skills_detailed") else "无"
        missing_skills = ", ".join(skill_gap.get("required_missing", [])) if skill_gap.get("required_missing") else "无"
        
        llm_service = LLMService()
        
        evaluation = None
        try:
            evaluation_data = llm_service.generate_candidate_evaluation(
                candidate_name=candidate_info.get("real_name", ""),
                education=candidate_info.get("education_text", ""),
                work_years=candidate_info.get("work_experience_year", 0),
                skills_text=skills_text,
                projects_text=projects_text,
                work_text=work_text,
                job_title=job_info.get("job_title", ""),
                job_requirements=job_info.get("requirements", ""),
                required_skills=required_skills,
                missing_skills=missing_skills,
                match_score=match_score
            )
            
            if evaluation_data:
                evaluation = evaluation_data
                logger.info(f"[HRService] Evaluation generated successfully")
            else:
                logger.warning(f"[HRService] Evaluation generation returned None")
        except Exception as e:
            logger.error(f"[HRService] LLM evaluation error: {e}", exc_info=True)
        
        candidate_summary = {
            "job_seeker_id": resolved_job_seeker_id,
            "user_id": candidate_info.get("user_id"),
            "real_name": candidate_info.get("real_name", ""),
            "education": candidate_info.get("education_text", ""),
            "work_experience_year": candidate_info.get("work_experience_year", 0),
            "skill_count": len(seeker_skills),
            "project_count": len(seeker_project_experiences),
            "work_experience_count": len(seeker_work_experiences)
        }
        
        job_summary = {
            "job_id": job_id,
            "job_title": job_info.get("job_title", ""),
            "required_skills_count": len([s for s in analysis_data.get("job_skills_detailed", []) if s.get("is_required")]),
            "experience_required": job_info.get("experience_required")
        }
        
        response = {
            "evaluation": evaluation,
            "candidate_summary": candidate_summary,
            "job_summary": job_summary
        }
        
        if evaluation:
            set_cached_data(cache_key, response)
        
        return response
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"[HRService] Evaluation error: {e}", exc_info=True)
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Internal server error: {str(e)}"
        )


@router.get("/ai/hr/candidate/{job_seeker_id}/interview-questions")
async def stream_interview_questions(
    job_seeker_id: int,
    job_id: int = Query(..., description="岗位ID"),
    user_id: Optional[int] = Query(None, alias="userId", description="候选人userId，可替代job_seeker_id"),
    hr_user_id: int = Depends(get_current_hr_user),
    db: Session = Depends(get_db),
):
    """面试问题生成（SSE流式输出）"""
    logger.info(f"[HRService] Interview questions stream request: jobSeekerId={job_seeker_id}, jobId={job_id}, hrUserId={hr_user_id}")
    
    try:
        analysis_data = _prepare_candidate_analysis_data(
            db, job_seeker_id, job_id, hr_user_id, seeker_user_id=user_id
        )
        
        candidate_info = analysis_data["candidate_info"]
        job_info = analysis_data["job_info"]
        seeker_skills = analysis_data["seeker_skills"]
        seeker_work_experiences = analysis_data["seeker_work_experiences"]
        seeker_project_experiences = analysis_data["seeker_project_experiences"]
        skill_gap = analysis_data["skill_gap"]
        resolved_job_seeker_id = analysis_data.get("job_seeker_id", job_seeker_id)
        
        # 准备LLM输入数据
        skills_text = ", ".join([s.get("name", "") for s in seeker_skills]) if seeker_skills else "无"
        
        projects_text = ""
        for proj in seeker_project_experiences[:3]:
            projects_text += f"{proj.get('project_name', '')}: {proj.get('description', '')}\n"
        if not projects_text:
            projects_text = "无"
        
        work_text = ""
        for work in seeker_work_experiences[:2]:
            work_text += f"{work.get('company_name', '')} {work.get('position', '')}: {work.get('description', '')}\n"
        if not work_text:
            work_text = "无"
        
        missing_skills = ", ".join(skill_gap.get("required_missing", [])) if skill_gap.get("required_missing") else "无"
        required_skills = ", ".join([s.get("name", "") for s in analysis_data.get("job_skills_detailed", []) if s.get("is_required")]) if analysis_data.get("job_skills_detailed") else "无"
        
        llm_service = LLMService()
        
        async def generate_stream() -> AsyncGenerator[str, None]:
            try:
                candidate_user_id = analysis_data.get("candidate_user_id")
                start_data = {
                    'type': 'start',
                    'message': "开始生成面试问题...",
                    'job_seeker_id': resolved_job_seeker_id,
                    'user_id': candidate_user_id
                }
                yield f"data: {json.dumps(start_data, ensure_ascii=False)}\n\n"
                
                async for chunk in llm_service.generate_hr_interview_questions_stream(
                    candidate_name=candidate_info.get("real_name", ""),
                    education=candidate_info.get("education_text", ""),
                    work_years=candidate_info.get("work_experience_year", 0),
                    skills_text=skills_text,
                    projects_text=projects_text,
                    work_text=work_text,
                    missing_skills=missing_skills,
                    job_title=job_info.get("job_title", ""),
                    job_requirements=job_info.get("requirements", ""),
                    required_skills=required_skills
                ):
                    if chunk:
                        yield f"data: {json.dumps({'type': 'chunk', 'content': chunk}, ensure_ascii=False)}\n\n"
                
                yield f"data: {json.dumps({'type': 'done'}, ensure_ascii=False)}\n\n"
            except Exception as e:
                logger.error(f"[HRService] Stream error: {e}", exc_info=True)
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
        logger.error(f"[HRService] Interview questions stream error: {e}", exc_info=True)
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Internal server error: {str(e)}"
        )


@router.post("/ai/hr/candidate/{job_seeker_id}/recommendation")
def get_candidate_recommendation(
    job_seeker_id: int,
    job_id: int = Query(..., description="岗位ID"),
    force_refresh: bool = Query(False, description="Force refresh cache"),
    user_id: Optional[int] = Query(None, alias="userId", description="候选人userId，可替代job_seeker_id"),
    hr_user_id: int = Depends(get_current_hr_user),
    db: Session = Depends(get_db),
):
    """候选人推荐理由"""
    logger.info(f"[HRService] Candidate recommendation request: jobSeekerId={job_seeker_id}, jobId={job_id}, hrUserId={hr_user_id}")
    
    try:
        analysis_data = _prepare_candidate_analysis_data(
            db, job_seeker_id, job_id, hr_user_id, seeker_user_id=user_id
        )

        resolved_job_seeker_id = analysis_data.get("job_seeker_id", job_seeker_id)
        cache_key = f"hr_recommendation:{hr_user_id}:{resolved_job_seeker_id}:{job_id}"

        if not force_refresh:
            cached = get_cached_data(cache_key)
            if cached:
                logger.info(f"[HRService] Recommendation cache hit: jobSeekerId={resolved_job_seeker_id}, jobId={job_id}")
                return cached
        
        candidate_info = analysis_data["candidate_info"]
        job_info = analysis_data["job_info"]
        seeker_skills = analysis_data["seeker_skills"]
        seeker_project_experiences = analysis_data["seeker_project_experiences"]
        skill_gap = analysis_data["skill_gap"]
        match_score = analysis_data["match_analysis"]["overall_score"]
        
        # 准备LLM输入数据
        key_skills = ", ".join([s.get("name", "") for s in seeker_skills[:5]]) if seeker_skills else "无"
        required_skills = ", ".join([s.get("name", "") for s in analysis_data.get("job_skills_detailed", []) if s.get("is_required")]) if analysis_data.get("job_skills_detailed") else "无"
        missing_skills = ", ".join(skill_gap.get("required_missing", [])) if skill_gap.get("required_missing") else "无"
        
        llm_service = LLMService()
        
        recommendation = None
        try:
            recommendation_data = llm_service.generate_candidate_recommendation(
                candidate_name=candidate_info.get("real_name", ""),
                education=candidate_info.get("education_text", ""),
                work_years=candidate_info.get("work_experience_year", 0),
                key_skills=key_skills,
                project_count=len(seeker_project_experiences),
                match_score=match_score,
                job_title=job_info.get("job_title", ""),
                required_skills=required_skills,
                missing_skills=missing_skills
            )
            
            if recommendation_data:
                recommendation = recommendation_data
                logger.info(f"[HRService] Recommendation generated successfully")
            else:
                logger.warning(f"[HRService] Recommendation generation returned None")
        except Exception as e:
            logger.error(f"[HRService] LLM recommendation error: {e}", exc_info=True)
        
        candidate_highlights = {
            "job_seeker_id": resolved_job_seeker_id,
            "user_id": candidate_info.get("user_id"),
            "work_experience": f"{candidate_info.get('work_experience_year', 0)}年工作经验",
            "key_skills": [s.get("name", "") for s in seeker_skills[:5]],
            "project_count": len(seeker_project_experiences),
            "education": candidate_info.get("education_text", "")
        }
        
        response = {
            "recommendation": recommendation,
            "match_score": match_score,
            "candidate_highlights": candidate_highlights
        }
        
        if recommendation:
            set_cached_data(cache_key, response)
        
        return response
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"[HRService] Recommendation error: {e}", exc_info=True)
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Internal server error: {str(e)}"
        )

