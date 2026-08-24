from __future__ import annotations

from typing import Optional

from pydantic import BaseModel, ConfigDict

from app.features.investor.models import Investor, InvestorListItem


class CreateInvestorRequest(BaseModel):
    model_config = ConfigDict(extra="ignore")

    investorName: Optional[str] = None
    investorGrade: Optional[str] = None
    totalAmount: Optional[int] = None
    lastProductName: Optional[str] = None
    screenMemo: Optional[str] = None


class InvestorResponse(BaseModel):
    investorId: int
    investorName: str
    investorGrade: str
    totalAmount: int
    lastProductName: str
    screenMemo: str

    @classmethod
    def from_domain(cls, row: Investor) -> "InvestorResponse":
        assert row.investor_id is not None
        return cls(
            investorId=row.investor_id,
            investorName=row.investor_name,
            investorGrade=row.investor_grade,
            totalAmount=row.total_amount,
            lastProductName=row.last_product_name,
            screenMemo=row.screen_memo,
        )


class InvestorListItemResponse(BaseModel):
    investorId: int
    investorName: str
    investorGrade: str
    totalAmount: int

    @classmethod
    def from_domain(cls, row: InvestorListItem) -> "InvestorListItemResponse":
        return cls(
            investorId=row.investor_id,
            investorName=row.investor_name,
            investorGrade=row.investor_grade,
            totalAmount=row.total_amount,
        )
