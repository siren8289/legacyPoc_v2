"""입출고 REST — 레거시 action 파라미터를 HTTP 메서드/경로로 대체한다."""

from __future__ import annotations

from typing import Optional, Union

from fastapi import APIRouter, Depends, Query

from app.common.api import ApiResponse, CreateWarehouseResponse, OkResponse, PageResult
from app.dependencies import get_warehouse_service
from app.features.warehouse.dto import (
    CreateWarehouseRequest,
    UpdateWarehouseFieldRequest,
    WarehouseIoResponse,
    WarehouseListItemResponse,
)
from app.features.warehouse.service import WarehouseService

router = APIRouter(prefix="/api/warehouses", tags=["warehouses"])


@router.get("", response_model=ApiResponse)
def list_warehouses(
    page: Optional[int] = Query(default=None),
    size: Optional[int] = Query(default=None),
    service: WarehouseService = Depends(get_warehouse_service),
) -> ApiResponse[Union[list[WarehouseIoResponse], PageResult[WarehouseListItemResponse]]]:
    return ApiResponse(data=service.list_or_page(page, size))


@router.get("/{warehouse_io_id}", response_model=ApiResponse[WarehouseIoResponse])
def detail_warehouse(
    warehouse_io_id: int,
    service: WarehouseService = Depends(get_warehouse_service),
) -> ApiResponse[WarehouseIoResponse]:
    return ApiResponse(data=service.detail(warehouse_io_id))


@router.post("", response_model=CreateWarehouseResponse)
def create_warehouse(
    request: CreateWarehouseRequest,
    service: WarehouseService = Depends(get_warehouse_service),
) -> CreateWarehouseResponse:
    created = service.create(request)
    assert created.warehouse_io_id is not None
    return CreateWarehouseResponse(warehouseIoId=created.warehouse_io_id)


@router.patch("/{warehouse_io_id}", response_model=ApiResponse[WarehouseIoResponse])
def update_warehouse_field(
    warehouse_io_id: int,
    request: UpdateWarehouseFieldRequest,
    service: WarehouseService = Depends(get_warehouse_service),
) -> ApiResponse[WarehouseIoResponse]:
    return ApiResponse(data=service.update_field(warehouse_io_id, request.fieldName, request.value))


@router.delete("/{warehouse_io_id}", response_model=OkResponse)
def delete_warehouse(
    warehouse_io_id: int,
    service: WarehouseService = Depends(get_warehouse_service),
) -> OkResponse:
    service.delete(warehouse_io_id)
    return OkResponse()
