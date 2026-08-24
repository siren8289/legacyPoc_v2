"""입출고 요청/응답 DTO — Jackson camelCase 키와 동일 계약."""

from __future__ import annotations

from typing import Any, Optional

from pydantic import BaseModel, ConfigDict, Field

from app.features.warehouse.models import WarehouseIo, WarehouseListItem


class CreateWarehouseRequest(BaseModel):
    model_config = ConfigDict(extra="ignore")

    warehouseName: Optional[str] = None
    productCode: Optional[str] = None
    productName: Optional[str] = None
    productCategory: Optional[str] = None
    inQty: Optional[int] = None
    outQty: Optional[int] = None
    currentStock: Optional[int] = None
    clientName: Optional[str] = None
    status: Optional[str] = None


class UpdateWarehouseFieldRequest(BaseModel):
    """단일 필드 PATCH — 저장 후 해당 row만 반환해 클라이언트가 patch 한다 (FR-007)."""

    fieldName: str = Field(min_length=1)
    value: Any = None


class WarehouseIoResponse(BaseModel):
    warehouseIoId: int
    warehouseName: str
    productCode: str
    productName: str
    productCategory: str
    inQty: int
    outQty: int
    currentStock: int
    clientName: str
    status: str

    @classmethod
    def from_domain(cls, row: WarehouseIo) -> "WarehouseIoResponse":
        assert row.warehouse_io_id is not None
        return cls(
            warehouseIoId=row.warehouse_io_id,
            warehouseName=row.warehouse_name,
            productCode=row.product_code,
            productName=row.product_name,
            productCategory=row.product_category,
            inQty=row.in_qty,
            outQty=row.out_qty,
            currentStock=row.current_stock,
            clientName=row.client_name,
            status=row.status,
        )


class WarehouseListItemResponse(BaseModel):
    warehouseIoId: int
    warehouseName: str
    productName: str
    currentStock: int

    @classmethod
    def from_domain(cls, row: WarehouseListItem) -> "WarehouseListItemResponse":
        return cls(
            warehouseIoId=row.warehouse_io_id,
            warehouseName=row.warehouse_name,
            productName=row.product_name,
            currentStock=row.current_stock,
        )
