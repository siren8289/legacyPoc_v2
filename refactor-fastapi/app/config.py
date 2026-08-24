"""런타임 설정 — Spring `application.yml` / profile 에 해당한다."""

from __future__ import annotations

from functools import lru_cache
from typing import Literal

from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    """memory 데모와 SQLAlchemy(JPA 대응)를 환경변수로 전환한다 (FR-013, NFR-004)."""

    model_config = SettingsConfigDict(env_prefix="APP_", extra="ignore")

    repository_type: Literal["memory", "sqlalchemy"] = "memory"
    database_url: str = (
        "mysql+pymysql://legacy_user:legacy_pass@localhost:3306/legacy_fastapi_refactor"
    )
    host: str = "0.0.0.0"
    port: int = 8084
    source_system: str = "refactor-fastapi"


@lru_cache
def get_settings() -> Settings:
    return Settings()
