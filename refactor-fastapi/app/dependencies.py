"""FastAPI Depends — Spring 생성자 DI에 해당한다."""

from __future__ import annotations

from fastapi import Request

from app.features.investor.service import InvestorService
from app.features.warehouse.service import WarehouseService


def get_warehouse_service(request: Request) -> WarehouseService:
    return WarehouseService(request.app.state.warehouse_repo)


def get_investor_service(request: Request) -> InvestorService:
    return InvestorService(request.app.state.investor_repo)
