"""인메모리 투자자 저장소 — PK Map + 이름 버킷 인덱스."""

from __future__ import annotations

from typing import Dict, List, Optional

from app.common.api import PageResult
from app.common.search_index import InvestorSearchIndex
from app.features.investor.models import Investor, InvestorListItem


class MemoryInvestorRepository:
    def __init__(self) -> None:
        self._table: Dict[int, Investor] = {}
        self._name_index = InvestorSearchIndex()
        self._sequence = 2000
        self.save(Investor(None, "김민수", "VIP", 850000000, "글로벌인컴펀드", "최근 요청: 월간 리포트 이메일 발송"))
        self.save(Investor(None, "이서연", "GOLD", 320000000, "국내배당주랩", "최근 문의: 수익률 변동 사유"))
        self.save(Investor(None, "박준호", "SILVER", 120000000, "안정형채권플랜", "만기 도래 예정 상품 설명 필요"))

    def find_all(self, keyword: Optional[str]) -> List[Investor]:
        query = "" if keyword is None else keyword.strip()
        candidates = self._name_index.candidate_ids(keyword)
        if candidates is None:
            return list(self._table.values())
        ids_to_scan = candidates if candidates else list(self._table.keys())
        rows = []
        for investor_id in ids_to_scan:
            investor = self._table.get(investor_id)
            if investor is not None and (not query or query in investor.investor_name):
                rows.append(investor)
        return rows

    def find_page(self, keyword: Optional[str], page: int, size: int) -> PageResult[InvestorListItem]:
        matched = self.find_all(keyword)
        safe_size = max(1, min(size, 100))
        safe_page = max(0, page)
        all_items = [
            InvestorListItem(i.investor_id, i.investor_name, i.investor_grade, i.total_amount)
            for i in matched
            if i.investor_id is not None
        ]
        start = safe_page * safe_size
        slice_items = all_items[start:start + safe_size]
        return PageResult(items=slice_items, totalCount=len(all_items), page=safe_page, size=safe_size)

    def find_by_id(self, investor_id: int) -> Optional[Investor]:
        return self._table.get(investor_id)

    def save(self, investor: Investor) -> Investor:
        investor_id = investor.investor_id
        if investor_id is None:
            self._sequence += 1
            investor_id = self._sequence
        else:
            existing = self._table.get(investor_id)
            if existing is not None:
                self._name_index.unregister(investor_id, existing.investor_name)
        saved = Investor(
            investor_id,
            investor.investor_name,
            investor.investor_grade,
            investor.total_amount,
            investor.last_product_name,
            investor.screen_memo,
        )
        self._table[investor_id] = saved
        self._name_index.register(investor_id, saved.investor_name)
        return saved

    def delete_by_id(self, investor_id: int) -> bool:
        existing = self._table.pop(investor_id, None)
        if existing is None:
            return False
        self._name_index.unregister(investor_id, existing.investor_name)
        return True
