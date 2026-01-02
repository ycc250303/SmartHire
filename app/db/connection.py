import os
import logging
from sqlalchemy import create_engine, text
from sqlalchemy.orm import sessionmaker, Session
from sqlalchemy.pool import QueuePool
from pymilvus import MilvusClient
from app.core.config import config

logger = logging.getLogger(__name__)

engine = None
SessionLocal = None
milvus_client = None
milvus_connected = False


def init_mysql():
    global engine, SessionLocal
    try:
        database_url = config.get_database_url()
        engine = create_engine(
            database_url,
            poolclass=QueuePool,
            pool_size=5,
            max_overflow=10,
            pool_pre_ping=True,
            echo=False,
        )
        SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)
        logger.info("MySQL connection initialized successfully")
        return True
    except Exception as e:
        logger.error(f"Failed to initialize MySQL connection: {e}")
        return False


def test_mysql_connection():
    try:
        with engine.connect() as conn:
            result = conn.execute(text("SELECT 1"))
            result.fetchone()
        logger.info("MySQL connection test passed")
        return True
    except Exception as e:
        logger.error(f"MySQL connection test failed: {e}")
        return False


def init_milvus():
    global milvus_connected, milvus_client
    try:
        data_path = config.milvus.data_path
        if not os.path.exists(data_path):
            os.makedirs(data_path, exist_ok=True)
        
        db_file = os.path.join(data_path, "milvus.db")
        milvus_client = MilvusClient(db_file)
        
        milvus_connected = True
        logger.info("Milvus Lite connection initialized successfully")
        return True
    except Exception as e:
        logger.error(f"Failed to initialize Milvus connection: {e}")
        return False


def test_milvus_connection():
    global milvus_connected, milvus_client
    try:
        if not milvus_connected or not milvus_client:
            return False
        collections = milvus_client.list_collections()
        logger.info(f"Milvus connection test passed. Collections: {collections}")
        return True
    except Exception as e:
        logger.error(f"Milvus connection test failed: {e}")
        return False


def get_db() -> Session:
    if SessionLocal is None:
        raise RuntimeError("Database not initialized")
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()

