"""동적 UPDATE 컬럼 화이트리스트 — Java `WarehouseIoColumn` enum 대응 (NFR-001)."""

from __future__ import annotations

from enum import Enum


class WarehouseIoColumn(str, Enum):
    warehouseName = "warehouseName"
    productCode = "productCode"
    productName = "productName"
    productCategory = "productCategory"
    inQty = "inQty"
    outQty = "outQty"
    currentStock = "currentStock"
    clientName = "clientName"
    status = "status"

    @classmethod
    def parse(cls, field_name: str) -> "WarehouseIoColumn":
        try:
            return cls(field_name)
        except ValueError as exc:
            raise ValueError(field_name) from exc

    @property
    def is_numeric(self) -> bool:
        return self in {self.inQty, self.outQty, self.currentStock}
