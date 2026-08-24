"""투자자 이름 첫 글자 버킷 인덱스 (FR-012 / F-006). Java `InvestorSearchIndex` 와 동일 알고리즘."""

from __future__ import annotations

from typing import Dict, List, Optional


class InvestorSearchIndex:
    def __init__(self) -> None:
        self._by_first_char: Dict[str, List[int]] = {}

    def register(self, investor_id: int, name: str) -> None:
        if not name:
            return
        first = name.strip()[0].lower()
        self._by_first_char.setdefault(first, []).append(investor_id)

    def unregister(self, investor_id: int, name: str) -> None:
        if not name:
            return
        first = name.strip()[0].lower()
        ids = self._by_first_char.get(first)
        if not ids:
            return
        self._by_first_char[first] = [item for item in ids if item != investor_id]

    def candidate_ids(self, keyword: Optional[str]) -> Optional[List[int]]:
        if keyword is None or not keyword.strip():
            return None
        first = keyword.strip()[0].lower()
        return list(self._by_first_char.get(first, []))
