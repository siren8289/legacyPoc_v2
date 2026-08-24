from __future__ import annotations

from dataclasses import dataclass
from typing import Optional


@dataclass(frozen=True)
class WarehouseIo:
    """입출고 도메인 모델. SQLAlchemy 어노테이션 없음 (FR-010)."""

    warehouse_io_id: Optional[int]
    warehouse_name: str
    product_code: str
    product_name: str
    product_category: str
    in_qty: int
    out_qty: int
    current_stock: int
    client_name: str
    status: str


@dataclass(frozen=True)
class WarehouseListItem:
    """목록 projection — 필요한 4컬럼만 (NFR-002)."""

    warehouse_io_id: int
    warehouse_name: str
    product_name: str
    current_stock: int
