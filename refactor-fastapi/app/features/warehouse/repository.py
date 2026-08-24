"""WarehouseRepository port — Spring interface 와 동일 책임."""

from __future__ import annotations

from typing import List, Optional, Protocol

from app.common.api import PageResult
from app.features.warehouse.models import WarehouseIo, WarehouseListItem


class WarehouseRepository(Protocol):
    def save(self, warehouse_io: WarehouseIo) -> WarehouseIo: ...

    def find_by_id(self, warehouse_io_id: int) -> Optional[WarehouseIo]: ...

    def find_all(self) -> List[WarehouseIo]: ...

    def find_active_page(self, page: int, size: int) -> PageResult[WarehouseListItem]: ...

    def update_field(self, warehouse_io_id: int, field_name: str, value: object) -> Optional[WarehouseIo]: ...

    def delete_by_id(self, warehouse_io_id: int) -> bool: ...
