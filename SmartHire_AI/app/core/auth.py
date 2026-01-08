import jwt
import logging
from typing import Optional
from fastapi import Depends, HTTPException, status
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
from app.core.config import config

logger = logging.getLogger(__name__)
security = HTTPBearer()


def extract_token(credentials: HTTPAuthorizationCredentials) -> str:
    return credentials.credentials


def verify_token(token: str) -> dict:
    try:
        logger.info(f"Verifying token (first 50 chars): {token[:50]}...")
        logger.info(f"Token length: {len(token)}")
        logger.info(f"Using secret key: {config.jwt.secret_key}, algorithm: {config.jwt.algorithm}")
        
        # Try decoding without verification first to see the payload
        try:
            unverified = jwt.decode(token, options={"verify_signature": False})
            logger.info(f"Unverified payload: {unverified}")
        except Exception as e:
            logger.error(f"Cannot decode payload: {e}")
        
        payload = jwt.decode(
            token,
            config.jwt.secret_key,
            algorithms=[config.jwt.algorithm],
        )
        
        logger.info(f"Token decoded successfully: {payload}")
        
        if payload.get("type") != "access":
            logger.error(f"Invalid token type: {payload.get('type')}")
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Invalid token type",
            )
        
        claims = payload.get("claims")
        if not claims:
            logger.error(f"No claims in token payload: {payload}")
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Invalid token claims",
            )
        
        logger.info(f"Token verification successful, claims: {claims}")
        return claims
    except jwt.ExpiredSignatureError:
        logger.error("Token expired")
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Token expired",
        )
    except jwt.InvalidTokenError as e:
        logger.error(f"Token verification failed: {type(e).__name__}: {e}")
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid token",
        )


def get_current_user_id(
    credentials: HTTPAuthorizationCredentials = Depends(security),
) -> int:
    if credentials is None:
        logger.error("[Auth] No credentials provided")
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="No authorization credentials provided"
        )
    token = extract_token(credentials)
    if not token:
        logger.error("[Auth] Token is empty")
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Token is empty"
        )
    claims = verify_token(token)
    user_id = claims.get("id")
    
    if user_id is None:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="User ID not found in token",
        )
    
    if isinstance(user_id, int):
        return user_id
    elif isinstance(user_id, float):
        return int(user_id)
    else:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid user ID format",
        )


def get_current_hr_user(
    credentials: HTTPAuthorizationCredentials = Depends(security),
) -> int:
    """验证并返回HR用户ID"""
    user_id = get_current_user_id(credentials)
    token = extract_token(credentials)
    claims = verify_token(token)
    user_type = claims.get("userType")
    
    if user_type != 2:
        logger.error(f"[Auth] User {user_id} is not HR (userType={user_type})")
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Access denied. HR users only."
        )
    
    return user_id

