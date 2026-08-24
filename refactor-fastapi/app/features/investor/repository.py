from __future__ import annotations

from typing import List, Optional, Protocol

from app.common.api import PageResult
from app.features.investor.models import Investor, InvestorListItem


class InvestorRepository(Protocol):
    def save(self, investor: Investor) -> Investor: ...

    def find_by_id(self, investor_id: int) -> Optional[Investor]: ...

    def find_all(self, keyword: Optional[str]) -> List[Investor]: ...

    def find_page(self, keyword: Optional[str], page: int, size: int) -> PageResult[InvestorListItem]: ...

    def delete_by_id(self, investor_id: int) -> bool: ...
