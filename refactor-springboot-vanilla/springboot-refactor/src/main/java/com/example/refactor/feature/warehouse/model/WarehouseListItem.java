package com.example.refactor.feature.warehouse.model;

/**
 * 목록 화면용 projection — 필요한 컬럼만 조회한다 (SELECT * 지양).
 */
public record WarehouseListItem(
        Long warehouseIoId,
        String warehouseName,
        String productName,
        Integer currentStock
) {
}
