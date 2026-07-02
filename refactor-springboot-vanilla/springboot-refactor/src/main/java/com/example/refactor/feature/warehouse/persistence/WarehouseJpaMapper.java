package com.example.refactor.feature.warehouse.persistence;

import com.example.refactor.feature.warehouse.model.WarehouseIo;

/**
 * 도메인 {@link WarehouseIo} ↔ JPA {@link WarehouseJpaEntity} 변환.
 * DB 테이블 구조 변경은 이 계층에서만 흡수한다.
 */
public final class WarehouseJpaMapper {

    private WarehouseJpaMapper() {
    }

    public static WarehouseIo toDomain(WarehouseJpaEntity entity) {
        return new WarehouseIo(
                entity.getWarehouseIoId(),
                entity.getWarehouseName(),
                entity.getProductCode(),
                entity.getProductName(),
                entity.getProductCategory(),
                entity.getInQty(),
                entity.getOutQty(),
                entity.getCurrentStock(),
                entity.getClientName(),
                entity.getStatus()
        );
    }

    public static WarehouseJpaEntity toNewEntity(WarehouseIo item) {
        WarehouseJpaEntity entity = new WarehouseJpaEntity();
        if (item.warehouseIoId() != null) {
            entity.setWarehouseIoId(item.warehouseIoId());
        }
        entity.setWarehouseName(item.warehouseName());
        entity.setProductCode(item.productCode());
        entity.setProductName(item.productName());
        entity.setProductCategory(item.productCategory());
        entity.setInQty(item.inQty());
        entity.setOutQty(item.outQty());
        entity.setCurrentStock(item.currentStock());
        entity.setClientName(item.clientName());
        entity.setStatus(item.status());
        entity.setDeletedYn("N");
        return entity;
    }

    public static void merge(WarehouseJpaEntity entity, WarehouseIo item) {
        entity.setWarehouseName(item.warehouseName());
        entity.setProductCode(item.productCode());
        entity.setProductName(item.productName());
        entity.setProductCategory(item.productCategory());
        entity.setInQty(item.inQty());
        entity.setOutQty(item.outQty());
        entity.setCurrentStock(item.currentStock());
        entity.setClientName(item.clientName());
        entity.setStatus(item.status());
    }

    public static void applyField(WarehouseJpaEntity entity, String fieldName, Object value) {
        switch (fieldName) {
            case "warehouseName" -> entity.setWarehouseName(asString(value));
            case "productCode" -> entity.setProductCode(asString(value));
            case "productName" -> entity.setProductName(asString(value));
            case "productCategory" -> entity.setProductCategory(asString(value));
            case "inQty" -> entity.setInQty(asInt(value));
            case "outQty" -> entity.setOutQty(asInt(value));
            case "currentStock" -> entity.setCurrentStock(asInt(value));
            case "clientName" -> entity.setClientName(asString(value));
            case "status" -> entity.setStatus(asString(value));
            default -> throw new IllegalArgumentException("Unknown field: " + fieldName);
        }
    }

    private static String asString(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private static int asInt(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
