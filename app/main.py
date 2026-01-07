import logging
from contextlib import asynccontextmanager
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.db.connection import (
    init_mysql,
    test_mysql_connection,
    init_milvus,
    test_milvus_connection,
)
from app.api.matching import router as matching_router
from app.api.career_planning import router as career_planning_router
from app.api.hr_services import router as hr_services_router
from app.api.seeker_career_path import router as seeker_career_path_router

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s - %(name)s - %(levelname)s - %(message)s",
)
logger = logging.getLogger(__name__)


@asynccontextmanager
async def lifespan(app: FastAPI):
    logger.info("Starting application...")
    
    if init_mysql():
        if test_mysql_connection():
            logger.info("✓ MySQL connection established successfully")
        else:
            logger.error("✗ MySQL connection test failed")
    else:
        logger.error("✗ MySQL initialization failed")
    
    if init_milvus():
        if test_milvus_connection():
            logger.info("✓ Milvus Lite connection established successfully")
        else:
            logger.error("✗ Milvus connection test failed")
    else:
        logger.error("✗ Milvus initialization failed")
    
    yield
    
    logger.info("Shutting down application...")


app = FastAPI(
    title="SmartHire AI Service",
    description="AI service for job matching and vector operations",
    lifespan=lifespan,
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=[
        "http://localhost:5173",
        "http://127.0.0.1:5173",
        "http://localhost:3000",
        "http://127.0.0.1:3000",
    ],
    allow_credentials=True,
    allow_methods=["GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"],
    allow_headers=[
        "Content-Type",
        "Authorization",
        "X-Requested-With",
        "Accept",
        "Origin",
        "Access-Control-Request-Method",
        "Access-Control-Request-Headers",
    ],
    expose_headers=["Authorization"],
    max_age=3600,
)

app.include_router(matching_router, prefix="/smarthire/api")
app.include_router(career_planning_router, prefix="/smarthire/api")
app.include_router(hr_services_router, prefix="/smarthire/api")
app.include_router(seeker_career_path_router, prefix="/smarthire/api")

logger.info("Registered routes:")
for route in app.routes:
    if hasattr(route, 'path') and 'career' in route.path.lower():
        logger.info(f"  {route.methods if hasattr(route, 'methods') else 'N/A'} {route.path}")


@app.get("/smarthire/api/health")
def health_check():
    return {"status": "ok"}
