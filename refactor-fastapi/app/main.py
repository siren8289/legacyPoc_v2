"""FastAPI 진입점 — Router → Service → Repository port."""

from __future__ import annotations

from fastapi import FastAPI

from app.common.handlers import register_exception_handlers
from app.config import Settings, get_settings
from app.features.investor.memory_repository import MemoryInvestorRepository
from app.features.investor.router import router as investor_router
from app.features.investor.sqlalchemy_repository import SqlAlchemyInvestorRepository
from app.features.warehouse.memory_repository import MemoryWarehouseRepository
from app.features.warehouse.router import router as warehouse_router
from app.features.warehouse.sqlalchemy_repository import SqlAlchemyWarehouseRepository
from app.persistence.session import create_engine_from_url, create_session_factory


def _bind_repositories(app: FastAPI, settings: Settings) -> None:
    if settings.repository_type == "sqlalchemy":
        engine = create_engine_from_url(settings.database_url)
        session_factory = create_session_factory(engine)
        app.state.warehouse_repo = SqlAlchemyWarehouseRepository(session_factory)
        app.state.investor_repo = SqlAlchemyInvestorRepository(session_factory)
        return
    app.state.warehouse_repo = MemoryWarehouseRepository()
    app.state.investor_repo = MemoryInvestorRepository()


def create_app(settings: Settings | None = None) -> FastAPI:
    resolved = settings or get_settings()
    app = FastAPI(
        title="LegacyPoc FastAPI Refactor",
        version="1.0.0",
        description="Spring Boot refactor API parity — warehouse/investor CRUD, projection, paging.",
    )
    app.state.settings = resolved
    _bind_repositories(app, resolved)
    register_exception_handlers(app)
    app.include_router(warehouse_router)
    app.include_router(investor_router)

    @app.get("/health")
    def health() -> dict:
        return {"status": "OK", "repositoryType": resolved.repository_type}

    return app


app = create_app()
