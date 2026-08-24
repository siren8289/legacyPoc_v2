"""인메모리 입출고 저장소 — LinkedHashMap + AtomicLong 대응 (NFR-004)."""

from __future__ import annotations

from typing import Dict, List, Optional

from app.common.api import PageResult
from app.common.columns import WarehouseIoColumn
from app.features.warehouse.models import WarehouseIo, WarehouseListItem


def _as_string(value: object) -> str:
    return "" if value is None else str(value)


def _as_int(value: object) -> int:
    if value is None:
        return 0
    if isinstance(value, bool):
        return int(value)
    if isinstance(value, (int, float)):
        return int(value)
    try:
        return int(str(value))
    except ValueError:
        return 0


def apply_column(row: WarehouseIo, column: WarehouseIoColumn, value: object) -> WarehouseIo:
    updates = {
        "warehouse_name": row.warehouse_name,
        "product_code": row.product_code,
        "product_name": row.product_name,
        "product_category": row.product_category,
        "in_qty": row.in_qty,
        "out_qty": row.out_qty,
        "current_stock": row.current_stock,
        "client_name": row.client_name,
        "status": row.status,
    }
    field_map = {
        WarehouseIoColumn.warehouseName: ("warehouse_name", _as_string(value)),
        WarehouseIoColumn.productCode: ("product_code", _as_string(value)),
        WarehouseIoColumn.productName: ("product_name", _as_string(value)),
        WarehouseIoColumn.productCategory: ("product_category", _as_string(value)),
        WarehouseIoColumn.inQty: ("in_qty", _as_int(value)),
        WarehouseIoColumn.outQty: ("out_qty", _as_int(value)),
        WarehouseIoColumn.currentStock: ("current_stock", _as_int(value)),
        WarehouseIoColumn.clientName: ("client_name", _as_string(value)),
        WarehouseIoColumn.status: ("status", _as_string(value)),
    }
    key, parsed = field_map[column]
    updates[key] = parsed
    return WarehouseIo(warehouse_io_id=row.warehouse_io_id, **updates)


class MemoryWarehouseRepository:
    def __init__(self) -> None:
        self._table: Dict[int, WarehouseIo] = {}
        self._sequence = 1000
        self.save(WarehouseIo(None, "서울1창고", "P-100", "산업용 센서", "전자부품", 140, 30, 110, "한빛유통", "ACTIVE"))
        self.save(WarehouseIo(None, "부산2창고", "P-220", "모터 모듈", "기계부품", 90, 12, 78, "남해물산", "ACTIVE"))

    def find_all(self) -> List[WarehouseIo]:
        return list(self._table.values())

    def find_active_page(self, page: int, size: int) -> PageResult[WarehouseListItem]:
        safe_size = max(1, min(size, 100))
        safe_page = max(0, page)
        all_items = [
            WarehouseListItem(w.warehouse_io_id, w.warehouse_name, w.product_name, w.current_stock)
            for w in sorted(self._table.values(), key=lambda row: row.warehouse_io_id or 0, reverse=True)
        ]
        start = safe_page * safe_size
        slice_items = all_items[start:start + safe_size]
        return PageResult(items=slice_items, totalCount=len(all_items), page=safe_page, size=safe_size)

    def find_by_id(self, warehouse_io_id: int) -> Optional[WarehouseIo]:
        return self._table.get(warehouse_io_id)

    def save(self, item: WarehouseIo) -> WarehouseIo:
        warehouse_io_id = item.warehouse_io_id
        if warehouse_io_id is None:
            self._sequence += 1
            warehouse_io_id = self._sequence
        saved = WarehouseIo(
            warehouse_io_id,
            item.warehouse_name,
            item.product_code,
            item.product_name,
            item.product_category,
            item.in_qty,
            item.out_qty,
            item.current_stock,
            item.client_name,
            item.status,
        )
        self._table[warehouse_io_id] = saved
        return saved

    def update_field(self, warehouse_io_id: int, field_name: str, value: object) -> Optional[WarehouseIo]:
        existing = self._table.get(warehouse_io_id)
        if existing is None:
            return None
        column = WarehouseIoColumn.parse(field_name)
        updated = apply_column(existing, column, value)
        self._table[warehouse_io_id] = updated
        return updated

    def delete_by_id(self, warehouse_io_id: int) -> bool:
        return self._table.pop(warehouse_io_id, None) is not None
