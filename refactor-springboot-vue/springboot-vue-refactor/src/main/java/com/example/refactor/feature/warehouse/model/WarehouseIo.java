package com.example.refactor.feature.warehouse.model;

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
