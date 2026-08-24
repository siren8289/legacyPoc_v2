from __future__ import annotations

from dataclasses import dataclass
from typing import Optional


@dataclass(frozen=True)
class Investor:
    """투자자 도메인 모델. persistence 어노테이션 없음 (FR-010)."""

    investor_id: Optional[int]
    investor_name: str
    investor_grade: str
    total_amount: int
    last_product_name: str
    screen_memo: str


@dataclass(frozen=True)
class InvestorListItem:
    investor_id: int
    investor_name: str
    investor_grade: str
    total_amount: int
