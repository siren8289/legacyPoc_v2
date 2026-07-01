package com.example.refactor.feature.warehouse.dto;

// 입출고 등록 API 요청 DTO.
public record CreateWarehouseRequest(
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
