"""SQLAlchemy 입출고 저장소 — Entity 조회 후 항상 도메인으로 변환한다."""

from __future__ import annotations

from typing import List, Optional

from sqlalchemy import func, select
from sqlalchemy.orm import Session, sessionmaker

from app.common.api import PageResult
from app.common.columns import WarehouseIoColumn
from app.features.warehouse.models import WarehouseIo, WarehouseListItem
from app.persistence.entities import WarehouseEntity
from app.persistence.mappers import (
    apply_warehouse_field,
    mark_warehouse_deleted,
    merge_warehouse,
    warehouse_to_domain,
    warehouse_to_new_entity,
)


class SqlAlchemyWarehouseRepository:
    def __init__(self, session_factory: sessionmaker[Session]) -> None:
        self._session_factory = session_factory

    def find_all(self) -> List[WarehouseIo]:
        with self._session_factory() as session:
            rows = session.scalars(
                select(WarehouseEntity)
                .where(WarehouseEntity.deleted_yn == "N")
                .order_by(WarehouseEntity.warehouse_io_id.desc())
            ).all()
            return [warehouse_to_domain(row) for row in rows]

    def find_active_page(self, page: int, size: int) -> PageResult[WarehouseListItem]:
        safe_size = max(1, min(size, 100))
        safe_page = max(0, page)
        with self._session_factory() as session:
            total = session.scalar(
                select(func.count()).select_from(WarehouseEntity).where(WarehouseEntity.deleted_yn == "N")
            ) or 0
            stmt = (
                select(
                    WarehouseEntity.warehouse_io_id,
                    WarehouseEntity.warehouse_name,
                    WarehouseEntity.product_name,
                    WarehouseEntity.current_stock,
                )
                .where(WarehouseEntity.deleted_yn == "N")
                .order_by(WarehouseEntity.warehouse_io_id.desc())
                .offset(safe_page * safe_size)
                .limit(safe_size)
            )
            items = [
                WarehouseListItem(row.warehouse_io_id, row.warehouse_name, row.product_name, row.current_stock)
                for row in session.execute(stmt)
            ]
            return PageResult(items=items, totalCount=int(total), page=safe_page, size=safe_size)

    def find_by_id(self, warehouse_io_id: int) -> Optional[WarehouseIo]:
        with self._session_factory() as session:
            entity = session.scalar(
                select(WarehouseEntity).where(
                    WarehouseEntity.warehouse_io_id == warehouse_io_id,
                    WarehouseEntity.deleted_yn == "N",
                )
            )
            return warehouse_to_domain(entity) if entity else None

    def save(self, item: WarehouseIo) -> WarehouseIo:
        with self._session_factory() as session:
            with session.begin():
                if item.warehouse_io_id is None:
                    entity = warehouse_to_new_entity(item)
                    session.add(entity)
                    session.flush()
                    return warehouse_to_domain(entity)
                entity = session.scalar(
                    select(WarehouseEntity).where(
                        WarehouseEntity.warehouse_io_id == item.warehouse_io_id,
                        WarehouseEntity.deleted_yn == "N",
                    )
                )
                if entity is None:
                    entity = warehouse_to_new_entity(item)
                    session.add(entity)
                    session.flush()
                else:
                    merge_warehouse(entity, item)
                return warehouse_to_domain(entity)

    def update_field(self, warehouse_io_id: int, field_name: str, value: object) -> Optional[WarehouseIo]:
        column = WarehouseIoColumn.parse(field_name)
        with self._session_factory() as session:
            with session.begin():
                entity = session.scalar(
                    select(WarehouseEntity).where(
                        WarehouseEntity.warehouse_io_id == warehouse_io_id,
                        WarehouseEntity.deleted_yn == "N",
                    )
                )
                if entity is None:
                    return None
                apply_warehouse_field(entity, column, value)
                return warehouse_to_domain(entity)

    def delete_by_id(self, warehouse_io_id: int) -> bool:
        with self._session_factory() as session:
            with session.begin():
                entity = session.scalar(
                    select(WarehouseEntity).where(
                        WarehouseEntity.warehouse_io_id == warehouse_io_id,
                        WarehouseEntity.deleted_yn == "N",
                    )
                )
                if entity is None:
                    return False
                mark_warehouse_deleted(entity)
                return True
