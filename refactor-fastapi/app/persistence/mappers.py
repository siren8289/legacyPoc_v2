from __future__ import annotations

from datetime import datetime

from app.common.columns import WarehouseIoColumn
from app.features.investor.models import Investor
from app.features.warehouse.memory_repository import apply_column
from app.features.warehouse.models import WarehouseIo
from app.persistence.entities import InvestorEntity, WarehouseEntity


def warehouse_to_domain(entity: WarehouseEntity) -> WarehouseIo:
    return WarehouseIo(
        warehouse_io_id=entity.warehouse_io_id,
        warehouse_name=entity.warehouse_name,
        product_code=entity.product_code,
        product_name=entity.product_name,
        product_category=entity.product_category,
        in_qty=entity.in_qty,
        out_qty=entity.out_qty,
        current_stock=entity.current_stock,
        client_name=entity.client_name,
        status=entity.status,
    )


def warehouse_to_new_entity(item: WarehouseIo) -> WarehouseEntity:
    entity = WarehouseEntity(
        warehouse_name=item.warehouse_name,
        product_code=item.product_code,
        product_name=item.product_name,
        product_category=item.product_category,
        in_qty=item.in_qty,
        out_qty=item.out_qty,
        current_stock=item.current_stock,
        client_name=item.client_name,
        status=item.status,
        deleted_yn="N",
        source_system="refactor-fastapi",
    )
    if item.warehouse_io_id is not None:
        entity.warehouse_io_id = item.warehouse_io_id
    return entity


def merge_warehouse(entity: WarehouseEntity, item: WarehouseIo) -> None:
    entity.warehouse_name = item.warehouse_name
    entity.product_code = item.product_code
    entity.product_name = item.product_name
    entity.product_category = item.product_category
    entity.in_qty = item.in_qty
    entity.out_qty = item.out_qty
    entity.current_stock = item.current_stock
    entity.client_name = item.client_name
    entity.status = item.status


def apply_warehouse_field(entity: WarehouseEntity, column: WarehouseIoColumn, value: object) -> None:
    domain = apply_column(warehouse_to_domain(entity), column, value)
    merge_warehouse(entity, domain)


def investor_to_domain(entity: InvestorEntity) -> Investor:
    return Investor(
        investor_id=entity.investor_id,
        investor_name=entity.investor_name,
        investor_grade=entity.investor_grade,
        total_amount=int(entity.total_amount),
        last_product_name=entity.last_product_name,
        screen_memo=entity.screen_memo,
    )


def investor_to_new_entity(investor: Investor) -> InvestorEntity:
    entity = InvestorEntity(
        investor_name=investor.investor_name,
        investor_grade=investor.investor_grade,
        total_amount=investor.total_amount,
        last_product_name=investor.last_product_name,
        screen_memo=investor.screen_memo,
        source_system="refactor-fastapi",
    )
    if investor.investor_id is not None:
        entity.investor_id = investor.investor_id
    return entity


def merge_investor(entity: InvestorEntity, investor: Investor) -> None:
    entity.investor_name = investor.investor_name
    entity.investor_grade = investor.investor_grade
    entity.total_amount = investor.total_amount
    entity.last_product_name = investor.last_product_name
    entity.screen_memo = investor.screen_memo


def mark_warehouse_deleted(entity: WarehouseEntity) -> None:
    entity.deleted_yn = "Y"
    entity.deleted_at = datetime.utcnow()
