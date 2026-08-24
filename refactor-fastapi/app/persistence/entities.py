"""SQLAlchemy persistence — 도메인 모델과 테이블 매핑을 분리한다 (FR-010)."""

from __future__ import annotations

from datetime import datetime
from typing import Optional

from sqlalchemy import CHAR, DateTime, Integer, String, func
from sqlalchemy.orm import DeclarativeBase, Mapped, mapped_column


class Base(DeclarativeBase):
    pass


class ChangeTrackedMixin:
    created_at: Mapped[datetime] = mapped_column(
        DateTime, nullable=False, default=func.now(), server_default=func.now()
    )
    updated_at: Mapped[datetime] = mapped_column(
        DateTime, nullable=False, default=func.now(), onupdate=func.now(), server_default=func.now()
    )
    deleted_at: Mapped[Optional[datetime]] = mapped_column(DateTime, nullable=True)
    source_system: Mapped[str] = mapped_column(String(50), nullable=False, default="refactor-fastapi")
    etl_batch_id: Mapped[Optional[str]] = mapped_column(String(50), nullable=True)


class WarehouseEntity(ChangeTrackedMixin, Base):
    __tablename__ = "tb_warehouse_io"

    # Integer PK: SQLite autoincrement 호환. MySQL 기존 BIGINT 컬럼과도 값 호환.
    warehouse_io_id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    warehouse_name: Mapped[str] = mapped_column(String(100), nullable=False, default="")
    product_code: Mapped[str] = mapped_column(String(40), nullable=False, default="")
    product_name: Mapped[str] = mapped_column(String(100), nullable=False, default="")
    product_category: Mapped[str] = mapped_column(String(60), nullable=False, default="")
    in_qty: Mapped[int] = mapped_column(Integer, nullable=False, default=0)
    out_qty: Mapped[int] = mapped_column(Integer, nullable=False, default=0)
    current_stock: Mapped[int] = mapped_column(Integer, nullable=False, default=0)
    client_name: Mapped[str] = mapped_column(String(100), nullable=False, default="")
    status: Mapped[str] = mapped_column(String(30), nullable=False, default="DRAFT")
    deleted_yn: Mapped[str] = mapped_column(CHAR(1), nullable=False, default="N")


class InvestorEntity(ChangeTrackedMixin, Base):
    __tablename__ = "tb_investor"

    investor_id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    investor_name: Mapped[str] = mapped_column(String(100), nullable=False)
    investor_grade: Mapped[str] = mapped_column(String(20), nullable=False, default="")
    total_amount: Mapped[int] = mapped_column(Integer, nullable=False, default=0)
    last_product_name: Mapped[str] = mapped_column(String(100), nullable=False, default="")
    screen_memo: Mapped[str] = mapped_column(String(500), nullable=False, default="")
