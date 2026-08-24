"""SQLAlchemy 투자자 저장소 — LIKE 검색은 바인딩 파라미터로만 수행한다."""

from __future__ import annotations

from typing import List, Optional

from sqlalchemy import func, select
from sqlalchemy.orm import Session, sessionmaker

from app.common.api import PageResult
from app.features.investor.models import Investor, InvestorListItem
from app.persistence.entities import InvestorEntity
from app.persistence.mappers import investor_to_domain, investor_to_new_entity, merge_investor


class SqlAlchemyInvestorRepository:
    def __init__(self, session_factory: sessionmaker[Session]) -> None:
        self._session_factory = session_factory

    def find_all(self, keyword: Optional[str]) -> List[Investor]:
        query = "" if keyword is None else keyword.strip()
        with self._session_factory() as session:
            stmt = select(InvestorEntity).order_by(InvestorEntity.investor_id.asc())
            if query:
                stmt = stmt.where(InvestorEntity.investor_name.like(f"%{query}%"))
            return [investor_to_domain(row) for row in session.scalars(stmt).all()]

    def find_page(self, keyword: Optional[str], page: int, size: int) -> PageResult[InvestorListItem]:
        query = "" if keyword is None else keyword.strip()
        safe_size = max(1, min(size, 100))
        safe_page = max(0, page)
        with self._session_factory() as session:
            filters = []
            if query:
                filters.append(InvestorEntity.investor_name.like(f"%{query}%"))
            total = session.scalar(
                select(func.count()).select_from(InvestorEntity).where(*filters) if filters
                else select(func.count()).select_from(InvestorEntity)
            ) or 0
            stmt = select(
                InvestorEntity.investor_id,
                InvestorEntity.investor_name,
                InvestorEntity.investor_grade,
                InvestorEntity.total_amount,
            ).order_by(InvestorEntity.investor_id.asc())
            if query:
                stmt = stmt.where(InvestorEntity.investor_name.like(f"%{query}%"))
            stmt = stmt.offset(safe_page * safe_size).limit(safe_size)
            items = [
                InvestorListItem(row.investor_id, row.investor_name, row.investor_grade, int(row.total_amount))
                for row in session.execute(stmt)
            ]
            return PageResult(items=items, totalCount=int(total), page=safe_page, size=safe_size)

    def find_by_id(self, investor_id: int) -> Optional[Investor]:
        with self._session_factory() as session:
            entity = session.get(InvestorEntity, investor_id)
            return investor_to_domain(entity) if entity else None

    def save(self, investor: Investor) -> Investor:
        with self._session_factory() as session:
            with session.begin():
                if investor.investor_id is None:
                    entity = investor_to_new_entity(investor)
                    session.add(entity)
                    session.flush()
                    return investor_to_domain(entity)
                entity = session.get(InvestorEntity, investor.investor_id)
                if entity is None:
                    entity = investor_to_new_entity(investor)
                    session.add(entity)
                    session.flush()
                else:
                    merge_investor(entity, investor)
                return investor_to_domain(entity)

    def delete_by_id(self, investor_id: int) -> bool:
        with self._session_factory() as session:
            with session.begin():
                entity = session.get(InvestorEntity, investor_id)
                if entity is None:
                    return False
                session.delete(entity)
                return True
