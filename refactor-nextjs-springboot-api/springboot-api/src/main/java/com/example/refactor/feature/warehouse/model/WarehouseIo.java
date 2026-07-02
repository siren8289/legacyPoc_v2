package com.example.refactor.feature.warehouse.model;

// 도메인 모델 — REST 응답 및 서비스 계층 간 데이터 전달에 사용한다.
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
