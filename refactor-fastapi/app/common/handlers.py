"""공통 오류 응답 — stack/SQL/secret 을 클라이언트에 노출하지 않는다 (FR-012, NFR-006)."""

from __future__ import annotations

import logging

from fastapi import FastAPI, Request
from fastapi.exceptions import RequestValidationError
from fastapi.responses import JSONResponse
from starlette.exceptions import HTTPException as StarletteHTTPException

from app.common.api import ErrorBody
from app.common.exceptions import DomainError

logger = logging.getLogger("legacypoc.fastapi")


def register_exception_handlers(app: FastAPI) -> None:
    @app.exception_handler(DomainError)
    async def domain_error_handler(_request: Request, exc: DomainError) -> JSONResponse:
        body = ErrorBody(errorCode=exc.error_code, message=exc.message)
        return JSONResponse(status_code=exc.status_code, content=body.model_dump())

    @app.exception_handler(RequestValidationError)
    async def validation_handler(_request: Request, exc: RequestValidationError) -> JSONResponse:
        fields = []
        for err in exc.errors():
            loc = ".".join(str(part) for part in err.get("loc", ()) if part != "body")
            if loc:
                fields.append(loc)
        body = ErrorBody(
            errorCode="INVALID_INPUT",
            message="Invalid request",
            details=fields,
        )
        return JSONResponse(status_code=400, content=body.model_dump())

    @app.exception_handler(StarletteHTTPException)
    async def http_exception_handler(_request: Request, exc: StarletteHTTPException) -> JSONResponse:
        error_code = "NOT_FOUND" if exc.status_code == 404 else "HTTP_ERROR"
        body = ErrorBody(errorCode=error_code, message="Request failed")
        return JSONResponse(status_code=exc.status_code, content=body.model_dump())

    @app.exception_handler(Exception)
    async def unhandled_handler(_request: Request, exc: Exception) -> JSONResponse:
        logger.exception("unhandled error: %s", type(exc).__name__)
        body = ErrorBody(errorCode="INTERNAL_ERROR", message="Unexpected error")
        return JSONResponse(status_code=500, content=body.model_dump())
