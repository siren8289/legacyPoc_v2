"""입출고 서비스 — 정규화·화이트리스트·트랜잭션 경계 (Controller는 HTTP만)."""

from __future__ import annotations

from typing import List, Union

from app.common.api import PageResult
from app.common.columns import WarehouseIoColumn
from app.common.exceptions import InvalidColumnError, NotFoundError
from app.features.warehouse.dto import (
    CreateWarehouseRequest,
    WarehouseListItemResponse,
    WarehouseIoResponse,
)
from app.features.warehouse.models import WarehouseIo
from app.features.warehouse.repository import WarehouseRepository


def _safe(value: object) -> str:
    return "" if value is None else str(value)


class WarehouseService:
    def __init__(self, repository: WarehouseRepository) -> None:
        self._repository = repository

    def list(self) -> List[WarehouseIoResponse]:
        return [WarehouseIoResponse.from_domain(row) for row in self._repository.find_all()]

    def list_page(self, page: int, size: int) -> PageResult[WarehouseListItemResponse]:
        result = self._repository.find_active_page(page, size)
        return PageResult(
            items=[WarehouseListItemResponse.from_domain(item) for item in result.items],
            totalCount=result.totalCount,
            page=result.page,
            size=result.size,
        )

    def list_or_page(self, page: int | None, size: int | None) -> Union[List[WarehouseIoResponse], PageResult[WarehouseListItemResponse]]:
        if page is not None and size is not None:
            return self.list_page(page, size)
        return self.list()

    def detail(self, warehouse_io_id: int) -> WarehouseIoResponse:
        row = self._repository.find_by_id(warehouse_io_id)
        if row is None:
            raise NotFoundError("Warehouse not found")
        return WarehouseIoResponse.from_domain(row)

    def create(self, request: CreateWarehouseRequest) -> WarehouseIo:
        row = WarehouseIo(
            warehouse_io_id=None,
            warehouse_name=_safe(request.warehouseName),
            product_code=_safe(request.productCode),
            product_name=_safe(request.productName),
            product_category=_safe(request.productCategory),
            in_qty=0 if request.inQty is None else request.inQty,
            out_qty=0 if request.outQty is None else request.outQty,
            current_stock=0 if request.currentStock is None else request.currentStock,
            client_name=_safe(request.clientName),
            status=_safe(request.status) if request.status else "DRAFT",
        )
        return self._repository.save(row)

    def update_field(self, warehouse_io_id: int, field_name: str, value: object) -> WarehouseIoResponse:
        try:
            WarehouseIoColumn.parse(field_name)
        except ValueError as exc:
            raise InvalidColumnError(field_name) from exc
        updated = self._repository.update_field(warehouse_io_id, field_name, value)
        if updated is None:
            raise NotFoundError("Warehouse not found")
        return WarehouseIoResponse.from_domain(updated)

    def delete(self, warehouse_io_id: int) -> None:
        if not self._repository.delete_by_id(warehouse_io_id):
            raise NotFoundError("Warehouse not found")
