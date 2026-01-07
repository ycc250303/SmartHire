import logging
import json
from typing import Dict, Any, Optional
from datetime import datetime, timedelta
from fastapi import APIRouter, Depends, HTTPException, status, Body
from pydantic import BaseModel
from sqlalchemy.orm import Session
from app.core.auth import get_current_user_id
from app.db.connection import get_db
from app.services.db_service import get_seeker_full_profile
from app.services.llm_service import LLMService

router = APIRouter()
logger = logging.getLogger(__name__)

_cache: Dict[str, tuple] = {}


class CareerPathRequest(BaseModel):
    target_position: Optional[str] = None
    target_city: Optional[str] = None
    target_skills: Optional[str] = None


def get_cached_path(user_id: int, target_position: str) -> Optional[Dict[str, Any]]:
    key = f"{user_id}:{target_position}"
    if key in _cache:
        cached_data, timestamp = _cache[key]
        if datetime.now() - timestamp < timedelta(hours=24):
            return cached_data
        else:
            del _cache[key]
    return None


def set_cached_path(user_id: int, target_position: str, data: Dict[str, Any]):
    key = f"{user_id}:{target_position}"
    _cache[key] = (data, datetime.now())


def _prepare_profile_data(
    db: Session,
    user_id: int,
    request: CareerPathRequest
) -> Dict[str, Any]:
    logger.info(f"[SeekerCareerPath] Preparing profile data: userId={user_id}")
    
    profile = get_seeker_full_profile(db, user_id)
    if not profile:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Seeker profile for user {user_id} not found"
        )
    
    seeker_info = profile["seeker_info"]
    education = profile["education"]
    education_text = profile["education_text"]
    skills = profile["skills"]
    work_experiences = profile["work_experiences"]
    project_experiences = profile["project_experiences"]
    expectation = profile.get("expectation", {})
    work_years = profile["work_years"]
    
    target_position = request.target_position or expectation.get("expected_position", "")
    target_city = request.target_city or expectation.get("work_city", seeker_info.get("current_city", ""))
    target_skills = request.target_skills or ""
    
    if not target_position:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Target position is required"
        )
    
    skills_text = ", ".join([f"{s.get('name', '')}({s.get('level', 0)})" for s in skills]) if skills else "无"
    
    work_experiences_text = "\n".join([
        f"- {w.get('position', '')} @ {w.get('company_name', '')}: {w.get('description', '')[:100]}"
        for w in work_experiences[:3]
    ]) if work_experiences else "无"
    
    project_experiences_text = "\n".join([
        f"- {p.get('project_name', '')}: {p.get('description', '')[:100]}"
        for p in project_experiences[:3]
    ]) if project_experiences else "无"
    
    current_city = seeker_info.get("current_city", "")
    
    return {
        "education": education_text,
        "work_years": work_years,
        "current_city": current_city,
        "skills_text": skills_text,
        "work_experiences_text": work_experiences_text,
        "project_experiences_text": project_experiences_text,
        "target_position": target_position,
        "target_city": target_city,
        "target_skills": target_skills,
        "skills": skills,
        "work_experiences": work_experiences,
        "project_experiences": project_experiences,
    }


def _generate_fallback_response(profile_data: Dict[str, Any]) -> Dict[str, Any]:
    skills = profile_data.get("skills", [])
    work_years = profile_data.get("work_years", 0.0)
    education = profile_data.get("education", "")
    target_position = profile_data.get("target_position", "")
    
    current_level = f"{education}·{work_years}年经验"
    
    skill_names = [s.get("name", "") for s in skills if s.get("name")]
    target_skills_list = [s.strip() for s in profile_data.get("target_skills", "").split(",") if s.strip()]
    
    missing_skills = [s for s in target_skills_list if s not in skill_names]
    
    return {
        "profile_summary": {
            "current_level": current_level,
            "key_strengths": skill_names[:3] if skill_names else ["请完善个人技能信息"],
            "key_weaknesses": missing_skills[:3] if missing_skills else ["暂无"],
            "career_readiness_score": max(60, min(90, int(70 + len(skill_names) * 2)))
        },
        "gap_analysis": {
            "skill_gaps": [
                {
                    "skill_name": skill,
                    "priority": "高",
                    "gap_reason": f"目标职位{target_position}需要此技能"
                }
                for skill in missing_skills[:5]
            ],
            "experience_gaps": {
                "current_years": work_years,
                "target_years": work_years + 1.0,
                "gap_years": 1.0,
                "suggestion": "建议通过项目实践积累更多经验"
            },
            "education_gaps": {
                "current": education,
                "target": education,
                "is_qualified": True,
                "suggestion": "当前学历符合要求"
            }
        },
        "career_roadmap": {
            "overview": {
                "target_position": target_position,
                "current_level": current_level,
                "plan_duration_months": max(6, len(missing_skills) * 2),
                "milestones_count": min(5, max(3, len(missing_skills)))
            },
            "phases": [
                {
                    "phase": i + 1,
                    "title": f"学习{skill}",
                    "description": f"掌握{skill}技能，完成相关项目实践",
                    "duration_months": 2,
                    "key_skills": [skill],
                    "deliverables": [f"{skill}项目实践"]
                }
                for i, skill in enumerate(missing_skills[:5])
            ],
            "immediate_actions": [
                {
                    "title": "完善个人技能信息",
                    "description": "在个人中心补充更多技能标签",
                    "priority": "高"
                },
                {
                    "title": "制定学习计划",
                    "description": "针对缺失技能制定详细学习计划",
                    "priority": "中"
                }
            ]
        },
        "learning_plan": {
            "skills": [
                {
                    "skill_name": skill,
                    "priority": "高",
                    "reason": f"目标职位必备技能",
                    "learning_steps": [
                        f"第1-2周：学习{skill}基础知识",
                        f"第3-4周：完成{skill}实践项目",
                        f"第5-6周：深入学习{skill}进阶内容"
                    ],
                    "resources": [
                        {"name": f"{skill}官方文档", "type": "文档", "url": ""},
                        {"name": f"{skill}实战教程", "type": "视频", "url": ""}
                    ],
                    "estimated_weeks": 6,
                    "difficulty": "中等"
                }
                for skill in missing_skills[:5]
            ]
        },
        "interview_prep": {
            "project_tips": "请完善项目经历信息，使用STAR法则准备项目讲解",
            "possible_questions": "请根据目标职位准备常见技术问题和行为问题",
            "weakness_strategy": "坦诚说明技能差距，展示学习能力和改进计划",
            "strength_emphasis": "突出现有技能和项目经验，用数据支撑成果"
        }
    }


@router.post("/ai/seeker/career-path")
def get_seeker_career_path(
    request: CareerPathRequest = Body(...),
    force_refresh: bool = False,
    user_id: int = Depends(get_current_user_id),
    db: Session = Depends(get_db),
):
    start_time = datetime.now()
    logger.info(f"[SeekerCareerPath] ========== REQUEST RECEIVED ==========")
    logger.info(f"[SeekerCareerPath] Request: userId={user_id}, targetPosition={request.target_position}, forceRefresh={force_refresh}")
    
    try:
        profile_data = _prepare_profile_data(db, user_id, request)
        target_position = profile_data["target_position"]
        
        if not force_refresh:
            cached = get_cached_path(user_id, target_position)
            if cached:
                logger.info(f"[SeekerCareerPath] Cache hit: userId={user_id}, targetPosition={target_position}")
                return cached
        
        response = None
        llm_error = None
        
        try:
            llm_service = LLMService()
            
            logger.info(f"[SeekerCareerPath] Calling LLM for career path generation")
            llm_start = datetime.now()
            
            career_path_data = llm_service.generate_seeker_career_path(
                education=profile_data["education"],
                work_years=profile_data["work_years"],
                current_city=profile_data["current_city"],
                skills_text=profile_data["skills_text"],
                work_experiences_text=profile_data["work_experiences_text"],
                project_experiences_text=profile_data["project_experiences_text"],
                target_position=target_position,
                target_city=profile_data["target_city"],
                target_skills=profile_data["target_skills"]
            )
            
            llm_duration = (datetime.now() - llm_start).total_seconds()
            
            if career_path_data:
                logger.info(f"[SeekerCareerPath] LLM career path generated successfully: duration={llm_duration:.2f}s")
                response = career_path_data
            else:
                logger.warning(f"[SeekerCareerPath] LLM returned None, using fallback")
                llm_error = "LLM service temporarily unavailable"
                response = _generate_fallback_response(profile_data)
                
        except Exception as e:
            logger.error(f"[SeekerCareerPath] LLM generation error: {e}", exc_info=True)
            llm_error = "LLM service temporarily unavailable"
            response = _generate_fallback_response(profile_data)
        
        if llm_error:
            response["error"] = llm_error
        
        set_cached_path(user_id, target_position, response)
        
        total_duration = (datetime.now() - start_time).total_seconds()
        response_json = json.dumps(response, ensure_ascii=False)
        response_size = len(response_json)
        
        logger.info(f"[SeekerCareerPath] Request completed: duration={total_duration:.2f}s, responseSize={response_size} bytes, hasLLMError={llm_error is not None}")
        logger.info(f"[SeekerCareerPath] ========== RESPONSE SUMMARY ==========")
        
        return response
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"[SeekerCareerPath] Request error: {e}", exc_info=True)
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Internal server error: {str(e)}"
        )

