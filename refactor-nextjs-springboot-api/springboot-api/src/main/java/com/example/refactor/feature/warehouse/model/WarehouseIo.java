package com.example.refactor.feature.warehouse.model;

/**
 * 입출고 도메인 모델.
 * <p>Service·Controller에서 사용한다. JPA/DB 어노테이션 없음.
 * DB 테이블 매핑은 persistence 패키지의 {@code WarehouseJpaEntity}가 담당한다.
 */
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
