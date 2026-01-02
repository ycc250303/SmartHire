import logging
import numpy as np
from typing import List
from sentence_transformers import SentenceTransformer
from sklearn.metrics.pairwise import cosine_similarity
from app.core.config import config

logger = logging.getLogger(__name__)

_model = None


def get_model() -> SentenceTransformer:
    global _model
    if _model is None:
        model_path = "./data/models/text2vec-base-chinese"
        _model = SentenceTransformer(model_path)
        logger.info("Sentence transformer model loaded")
    return _model


def embed_text(text: str) -> List[float]:
    if not text or not text.strip():
        return [0.0] * 768
    
    model = get_model()
    embedding = model.encode(text, normalize_embeddings=True)
    return embedding.tolist()


def calculate_cosine_similarity(embedding1: List[float], embedding2: List[float]) -> float:
    vec1 = np.array(embedding1).reshape(1, -1)
    vec2 = np.array(embedding2).reshape(1, -1)
    similarity = cosine_similarity(vec1, vec2)[0][0]
    return float(similarity)


def build_seeker_text(
    major: str,
    university: str,
    skills: List[dict],
    work_experiences: List[dict] = None,
    project_experiences: List[dict] = None,
) -> str:
    parts = []
    
    if major:
        parts.append(f"专业: {major}")
    if university:
        parts.append(f"学校: {university}")
    
    if skills:
        skill_names = [s.get("name", "") for s in skills if s.get("name")]
        if skill_names:
            parts.append(f"技能: {', '.join(skill_names)}")
    
    if work_experiences:
        for exp in work_experiences[:2]:
            if exp.get("position"):
                parts.append(f"工作经历: {exp.get('position', '')}")
            if exp.get("description"):
                parts.append(exp.get("description", ""))
    
    if project_experiences:
        for proj in project_experiences[:2]:
            if proj.get("project_name"):
                parts.append(f"项目: {proj.get('project_name', '')}")
            if proj.get("description"):
                parts.append(proj.get("description", ""))
    
    return " ".join(parts)


def build_job_text(
    job_title: str,
    description: str,
    responsibilities: str,
    requirements: str,
    skills: List[str],
) -> str:
    parts = []
    
    if job_title:
        parts.append(f"职位: {job_title}")
    if description:
        parts.append(description)
    if responsibilities:
        parts.append(responsibilities)
    if requirements:
        parts.append(requirements)
    if skills:
        parts.append(f"技能要求: {', '.join(skills)}")
    
    return " ".join(parts)

