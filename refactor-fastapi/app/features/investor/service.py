from __future__ import annotations

from typing import List, Optional, Union

from app.common.api import PageResult
from app.common.exceptions import NotFoundError
from app.features.investor.dto import CreateInvestorRequest, InvestorListItemResponse, InvestorResponse
from app.features.investor.models import Investor
from app.features.investor.repository import InvestorRepository


def _safe(value: object) -> str:
    return "" if value is None else str(value)


class InvestorService:
    def __init__(self, repository: InvestorRepository) -> None:
        self._repository = repository

    def list(self, name: Optional[str]) -> List[InvestorResponse]:
        return [InvestorResponse.from_domain(row) for row in self._repository.find_all(name)]

    def list_page(self, name: Optional[str], page: int, size: int) -> PageResult[InvestorListItemResponse]:
        result = self._repository.find_page(name, page, size)
        return PageResult(
            items=[InvestorListItemResponse.from_domain(item) for item in result.items],
            totalCount=result.totalCount,
            page=result.page,
            size=result.size,
        )

    def list_or_page(
        self,
        name: Optional[str],
        page: Optional[int],
        size: Optional[int],
    ) -> Union[List[InvestorResponse], PageResult[InvestorListItemResponse]]:
        if page is not None and size is not None:
            return self.list_page(name, page, size)
        return self.list(name)

    def detail(self, investor_id: int) -> InvestorResponse:
        row = self._repository.find_by_id(investor_id)
        if row is None:
            raise NotFoundError("Investor not found")
        return InvestorResponse.from_domain(row)

    def create(self, request: CreateInvestorRequest) -> Investor:
        investor = Investor(
            investor_id=None,
            investor_name=_safe(request.investorName),
            investor_grade=_safe(request.investorGrade),
            total_amount=0 if request.totalAmount is None else request.totalAmount,
            last_product_name=_safe(request.lastProductName),
            screen_memo=_safe(request.screenMemo),
        )
        return self._repository.save(investor)
