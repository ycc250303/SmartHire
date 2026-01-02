import os
import yaml
from pathlib import Path
from typing import Dict, Any
from pydantic import BaseModel


class DatabaseConfig(BaseModel):
    host: str
    port: int
    username: str
    password: str
    database: str
    charset: str


class MilvusConfig(BaseModel):
    data_path: str
    collection_name: str


class JWTConfig(BaseModel):
    secret_key: str
    algorithm: str


class QwenConfig(BaseModel):
    api_key: str
    model: str
    base_url: str
    max_tokens: int = 2000
    temperature: float = 0.7
    top_p: float = 0.9
    timeout: int = 60
    enable_response_format: bool = True


class PromptsConfig(BaseModel):
    learning_plan: Dict[str, Any]
    interview_prep: Dict[str, Any]
    career_roadmap: Dict[str, Any] = {}
    learning_plan_structured: Dict[str, Any] = {}
    candidate_evaluation: Dict[str, Any] = {}
    candidate_recommendation: Dict[str, Any] = {}
    hr_interview_questions: Dict[str, Any] = {}


class MessagesConfig(BaseModel):
    stream: Dict[str, str]
    labels: Dict[str, str]
    education: Dict[str, str]
    experience: Dict[str, str]


class Config:
    def __init__(self, config_path: str = None):
        if config_path is None:
            config_path = os.path.join(
                Path(__file__).parent.parent.parent, "config.yaml"
            )
        
        with open(config_path, "r", encoding="utf-8") as f:
            config_data = yaml.safe_load(f)
        
        self.database = DatabaseConfig(**config_data["database"])
        self.milvus = MilvusConfig(**config_data["milvus"])
        self.jwt = JWTConfig(**config_data["jwt"])
        self.qwen = QwenConfig(**config_data["qwen"])
        self.prompts = PromptsConfig(**config_data["prompts"])
        self.messages = MessagesConfig(**config_data["messages"])
    
    def get_database_url(self) -> str:
        return (
            f"mysql+pymysql://{self.database.username}:{self.database.password}"
            f"@{self.database.host}:{self.database.port}/{self.database.database}"
            f"?charset={self.database.charset}"
        )


config = Config()

