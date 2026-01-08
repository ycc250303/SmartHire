import logging
import time
import hashlib
from typing import List, Optional, Dict, Any
from datetime import datetime
from app.db.connection import milvus_client, milvus_connected
from app.core.config import config

logger = logging.getLogger(__name__)

MATCH_SCORES_COLLECTION = "match_scores"
EMBEDDING_DIM = 768


def ensure_collection():
    if not milvus_connected or not milvus_client:
        logger.error("Milvus not connected")
        return False
    
    try:
        if not milvus_client.has_collection(MATCH_SCORES_COLLECTION):
            milvus_client.create_collection(
                collection_name=MATCH_SCORES_COLLECTION,
                dimension=EMBEDDING_DIM,
                metric_type="COSINE",
                auto_id=True,
            )
            logger.info(f"Created collection: {MATCH_SCORES_COLLECTION}")
        
        return True
    except Exception as e:
        logger.error(f"Failed to ensure collection: {e}")
        return False


def _make_composite_key(seeker_id: int, job_id: int) -> str:
    return f"{seeker_id}_{job_id}"


def _hash_key(key: str) -> int:
    return int(hashlib.md5(key.encode()).hexdigest()[:15], 16)


def store_match_score(
    seeker_id: int,
    job_id: int,
    similarity_score: float,
    match_score: int,
    seeker_updated_at: datetime,
    job_updated_at: datetime,
    seeker_vector: List[float],
    job_vector: List[float],
) -> bool:
    if not milvus_connected or not milvus_client:
        return False
    
    try:
        ensure_collection()
        
        composite_key = _make_composite_key(seeker_id, job_id)
        key_hash = _hash_key(composite_key)
        
        existing = get_match_score(seeker_id, job_id)
        if existing:
            try:
                milvus_client.delete(
                    collection_name=MATCH_SCORES_COLLECTION,
                    filter=f'id == {key_hash}'
                )
            except:
                pass
        
        seeker_ts = int(seeker_updated_at.timestamp()) if seeker_updated_at else 0
        job_ts = int(job_updated_at.timestamp()) if job_updated_at else 0
        computed_ts = int(time.time())
        
        data = [{
            "id": key_hash,
            "vector": seeker_vector,
            "seeker_id": str(seeker_id),
            "job_id": str(job_id),
            "similarity": similarity_score,
            "match_score": match_score,
            "seeker_ts": seeker_ts,
            "job_ts": job_ts,
            "computed_ts": computed_ts,
        }]
        
        milvus_client.insert(
            collection_name=MATCH_SCORES_COLLECTION,
            data=data
        )
        
        logger.info(f"Stored match score: seeker={seeker_id}, job={job_id}, score={match_score}")
        return True
    except Exception as e:
        logger.error(f"Failed to store match score: {e}")
        return False


def get_match_score(seeker_id: int, job_id: int) -> Optional[Dict[str, Any]]:
    if not milvus_connected or not milvus_client:
        return None
    
    try:
        ensure_collection()
        
        composite_key = _make_composite_key(seeker_id, job_id)
        key_hash = _hash_key(composite_key)
        
        results = milvus_client.query(
            collection_name=MATCH_SCORES_COLLECTION,
            filter=f'id == {key_hash}',
            output_fields=[
                "similarity",
                "match_score",
                "seeker_id",
                "job_id",
                "seeker_ts",
                "job_ts",
                "computed_ts"
            ],
            limit=1
        )
        
        if results and len(results) > 0:
            result = results[0]
            return {
                "similarity_score": result.get("similarity", 0.0),
                "match_score": result.get("match_score", 0),
                "seeker_id": int(result.get("seeker_id", 0)),
                "job_id": int(result.get("job_id", 0)),
                "seeker_updated_at": result.get("seeker_ts", 0),
                "job_updated_at": result.get("job_ts", 0),
                "computed_at": result.get("computed_ts", 0),
            }
        
        return None
    except Exception as e:
        logger.error(f"Failed to get match score: {e}")
        return None


def batch_get_match_scores(seeker_id: int, job_ids: List[int]) -> Dict[int, Dict[str, Any]]:
    if not milvus_connected or not milvus_client:
        return {}
    
    try:
        ensure_collection()
        
        results_map = {}
        for job_id in job_ids:
            score_data = get_match_score(seeker_id, job_id)
            if score_data:
                results_map[job_id] = score_data
        
        return results_map
    except Exception as e:
        logger.error(f"Failed to batch get match scores: {e}")
        return {}


def check_and_update_if_needed(
    seeker_id: int,
    job_id: int,
    current_seeker_updated_at: datetime,
    current_job_updated_at: datetime,
) -> bool:
    cached_score = get_match_score(seeker_id, job_id)
    
    if not cached_score:
        return False
    
    cached_seeker_ts = cached_score.get("seeker_updated_at", 0)
    cached_job_ts = cached_score.get("job_updated_at", 0)
    
    current_seeker_ts = int(current_seeker_updated_at.timestamp()) if current_seeker_updated_at else 0
    current_job_ts = int(current_job_updated_at.timestamp()) if current_job_updated_at else 0
    
    if cached_seeker_ts < current_seeker_ts or cached_job_ts < current_job_ts:
        logger.info(
            f"Data updated detected: seeker={seeker_id}, job={job_id}, "
            f"seeker_ts: {cached_seeker_ts} -> {current_seeker_ts}, "
            f"job_ts: {cached_job_ts} -> {current_job_ts}"
        )
        return True
    
    return False


def delete_match_scores_for_seeker(seeker_id: int) -> bool:
    if not milvus_connected or not milvus_client:
        return False
    
    try:
        ensure_collection()
        
        results = milvus_client.query(
            collection_name=MATCH_SCORES_COLLECTION,
            filter=f'seeker_id == "{seeker_id}"',
            output_fields=["id"],
            limit=10000
        )
        
        if results:
            ids_to_delete = [r["id"] for r in results]
            for id_val in ids_to_delete:
                try:
                    milvus_client.delete(
                        collection_name=MATCH_SCORES_COLLECTION,
                        filter=f'id == {id_val}'
                    )
                except:
                    pass
            logger.info(f"Deleted {len(ids_to_delete)} match scores for seeker {seeker_id}")
        
        return True
    except Exception as e:
        logger.error(f"Failed to delete match scores for seeker: {e}")
        return False


def delete_match_scores_for_job(job_id: int) -> bool:
    if not milvus_connected or not milvus_client:
        return False
    
    try:
        ensure_collection()
        
        results = milvus_client.query(
            collection_name=MATCH_SCORES_COLLECTION,
            filter=f'job_id == "{job_id}"',
            output_fields=["id"],
            limit=10000
        )
        
        if results:
            ids_to_delete = [r["id"] for r in results]
            for id_val in ids_to_delete:
                try:
                    milvus_client.delete(
                        collection_name=MATCH_SCORES_COLLECTION,
                        filter=f'id == {id_val}'
                    )
                except:
                    pass
            logger.info(f"Deleted {len(ids_to_delete)} match scores for job {job_id}")
        
        return True
    except Exception as e:
        logger.error(f"Failed to delete match scores for job: {e}")
        return False
