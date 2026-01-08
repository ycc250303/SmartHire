import logging
from typing import List, Dict, Any, Optional
from app.services.db_service import calculate_work_years
from app.services.matching_service import calculate_education_match
from app.core.config import config

logger = logging.getLogger(__name__)


def _get_education_map() -> Dict[int, str]:
    """Get education level mapping from config"""
    return {
        0: config.messages.education.get("high_school", "High school or below"),
        1: config.messages.education.get("associate", "Associate degree"),
        2: config.messages.education.get("bachelor", "Bachelor"),
        3: config.messages.education.get("master", "Master"),
        4: config.messages.education.get("phd", "PhD"),
    }


def _get_experience_map() -> Dict[int, Dict[str, Any]]:
    """Get experience level mapping from config"""
    return {
        1: {"min": 1.0, "text": config.messages.experience.get("level_1", "1-3 years")},
        2: {"min": 3.0, "text": config.messages.experience.get("level_2", "3-5 years")},
        3: {"min": 5.0, "text": config.messages.experience.get("level_3", "5-10 years")},
        4: {"min": 10.0, "text": config.messages.experience.get("level_4", "10+ years")},
    }


def analyze_skill_gap(
    seeker_skills: List[Dict[str, Any]],
    job_skills: List[Dict[str, Any]]
) -> Dict[str, Any]:
    logger.debug(f"[GapAnalysis] Analyzing skill gap: seekerSkills={len(seeker_skills)}, jobSkills={len(job_skills)}")
    
    seeker_skill_names = {s.get("name", "").lower(): s.get("level") for s in seeker_skills if s.get("name")}
    
    required_missing = []
    optional_missing = []
    matched = []
    required_total = 0
    required_matched = 0
    
    for job_skill in job_skills:
        skill_name = job_skill.get("name", "")
        is_required = job_skill.get("is_required", False)
        skill_name_lower = skill_name.lower()
        
        if is_required:
            required_total += 1
        
        if skill_name_lower in seeker_skill_names:
            matched.append({
                "name": skill_name,
                "your_level": seeker_skill_names[skill_name_lower],
                "is_required": is_required
            })
            if is_required:
                required_matched += 1
        else:
            if is_required:
                required_missing.append(skill_name)
            else:
                optional_missing.append(skill_name)
    
    match_rate = required_matched / required_total if required_total > 0 else 1.0
    
    result = {
        "required_missing": required_missing,
        "optional_missing": optional_missing,
        "matched": matched,
        "match_rate": round(match_rate, 2)
    }
    
    logger.info(f"[GapAnalysis] Skill gap result: requiredTotal={required_total}, requiredMatched={required_matched}, requiredMissing={len(required_missing)}, optionalMissing={len(optional_missing)}, matched={len(matched)}, matchRate={result['match_rate']}")
    
    return result


def analyze_experience_gap(
    work_experiences: List[Dict[str, Any]],
    experience_required: Optional[int]
) -> Dict[str, Any]:
    logger.debug(f"[GapAnalysis] Analyzing experience gap: workExpCount={len(work_experiences)}, requiredLevel={experience_required}")
    
    your_years = calculate_work_years(work_experiences)
    logger.debug(f"[GapAnalysis] Calculated work years: {your_years}")
    
    unlimited_text = config.messages.experience.get("unlimited", "No requirement")
    unknown_text = config.messages.experience.get("unknown", "Unknown")
    
    if experience_required is None or experience_required == 0:
        result = {
            "your_years": your_years,
            "required_level": 0,
            "required_text": unlimited_text,
            "is_qualified": True,
            "gap_years": 0.0
        }
        logger.info(f"[GapAnalysis] Experience gap result: yourYears={your_years}, required={unlimited_text}, qualified=True")
        return result
    
    experience_map = _get_experience_map()
    
    if experience_required not in experience_map:
        result = {
            "your_years": your_years,
            "required_level": experience_required,
            "required_text": unknown_text,
            "is_qualified": False,
            "gap_years": 0.0
        }
        logger.warning(f"[GapAnalysis] Unknown experience level: {experience_required}")
        return result
    
    required_info = experience_map[experience_required]
    min_years = required_info["min"]
    is_qualified = your_years >= min_years
    gap_years = min_years - your_years
    
    result = {
        "your_years": your_years,
        "required_level": experience_required,
        "required_text": required_info["text"],
        "is_qualified": is_qualified,
        "gap_years": round(gap_years, 1)
    }
    
    logger.info(f"[GapAnalysis] Experience gap result: yourYears={your_years}, required={required_info['text']}, qualified={is_qualified}, gapYears={result['gap_years']}")
    
    return result


def analyze_education_gap(
    seeker_education: Optional[int],
    job_education_required: Optional[int]
) -> Dict[str, Any]:
    logger.debug(f"[GapAnalysis] Analyzing education gap: seekerLevel={seeker_education}, requiredLevel={job_education_required}")
    
    education_map = _get_education_map()
    unknown_text = config.messages.education.get("unknown", "Unknown")
    unlimited_text = config.messages.education.get("unlimited", "No requirement")
    
    if seeker_education is None:
        seeker_level = -1
        seeker_text = unknown_text
    else:
        seeker_level = seeker_education
        seeker_text = education_map.get(seeker_education, unknown_text)
    
    if job_education_required is None:
        required_level = -1
        required_text = unlimited_text
    else:
        required_level = job_education_required
        required_text = education_map.get(job_education_required, unknown_text)
    
    if required_level == -1:
        match_score = 1.0
        is_qualified = True
    elif seeker_level == -1:
        match_score = 0.0
        is_qualified = False
    else:
        seeker_education_str = education_map.get(seeker_level, "")
        match_score = calculate_education_match(seeker_education_str, required_level)
        is_qualified = seeker_level >= required_level
    
    result = {
        "your_level": seeker_level,
        "your_text": seeker_text,
        "required_level": required_level,
        "required_text": required_text,
        "is_qualified": is_qualified,
        "match_score": match_score
    }
    
    logger.info(f"[GapAnalysis] Education gap result: yourLevel={seeker_text}, requiredLevel={required_text}, qualified={is_qualified}, matchScore={match_score}")
    
    return result

