"""공통 응답 래퍼 — Spring `ApiResponse` / `PageResult` 와 JSON 키를 맞춘다."""

from __future__ import annotations

from typing import Generic, List, TypeVar

from pydantic import BaseModel, ConfigDict, Field

T = TypeVar("T")


class ApiResponse(BaseModel, Generic[T]):
    data: T


class PageResult(BaseModel, Generic[T]):
    items: List[T]
    totalCount: int
    page: int
    size: int


class CreateWarehouseResponse(BaseModel):
    result: str = "OK"
    warehouseIoId: int


class CreateInvestorResponse(BaseModel):
    result: str = "OK"
    investorId: int


class OkResponse(BaseModel):
    result: str = "OK"


class ErrorBody(BaseModel):
    model_config = ConfigDict(extra="forbid")

    result: str = "FAIL"
    errorCode: str
    message: str
    details: List[str] = Field(default_factory=list)
