package com.example.refactor.feature.warehouse.model;

// 입출고 기능의 조회/상세/등록에서 사용하는 도메인 모델.
public record WarehouseIo(
        Long warehouseIoId,
        String warehouseName,
        String productCode,
        String productName,
        String productCategory,
        Integer inQty,
        Integer outQty,
        Integer currentStock,
        String clientName,
        String status
) {
}
