from __future__ import annotations

from typing import Optional, Union

from fastapi import APIRouter, Depends, Query

from app.common.api import ApiResponse, CreateInvestorResponse, PageResult
from app.dependencies import get_investor_service
from app.features.investor.dto import CreateInvestorRequest, InvestorListItemResponse, InvestorResponse
from app.features.investor.service import InvestorService

router = APIRouter(prefix="/api/investors", tags=["investors"])


@router.get("", response_model=ApiResponse)
def list_investors(
    name: Optional[str] = Query(default=None),
    page: Optional[int] = Query(default=None),
    size: Optional[int] = Query(default=None),
    service: InvestorService = Depends(get_investor_service),
) -> ApiResponse[Union[list[InvestorResponse], PageResult[InvestorListItemResponse]]]:
    return ApiResponse(data=service.list_or_page(name, page, size))


@router.get("/{investor_id}", response_model=ApiResponse[InvestorResponse])
def detail_investor(
    investor_id: int,
    service: InvestorService = Depends(get_investor_service),
) -> ApiResponse[InvestorResponse]:
    return ApiResponse(data=service.detail(investor_id))


@router.post("", response_model=CreateInvestorResponse)
def create_investor(
    request: CreateInvestorRequest,
    service: InvestorService = Depends(get_investor_service),
) -> CreateInvestorResponse:
    created = service.create(request)
    assert created.investor_id is not None
    return CreateInvestorResponse(investorId=created.investor_id)
