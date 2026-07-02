package com.example.legacy.dao;

import com.example.legacy.dto.WarehouseIoDto;

enum WarehouseIoColumn {
    WAREHOUSE_NAME {
        @Override void apply(WarehouseIoDto dto, Object value) { dto.setWarehouseName((String) value); }
    },
    PRODUCT_CODE {
        @Override void apply(WarehouseIoDto dto, Object value) { dto.setProductCode((String) value); }
    },
    PRODUCT_NAME {
        @Override void apply(WarehouseIoDto dto, Object value) { dto.setProductName((String) value); }
    },
    PRODUCT_CATEGORY {
        @Override void apply(WarehouseIoDto dto, Object value) { dto.setProductCategory((String) value); }
    },
    IN_QTY {
        @Override void apply(WarehouseIoDto dto, Object value) { dto.setInQty((Integer) value); }
    },
    OUT_QTY {
        @Override void apply(WarehouseIoDto dto, Object value) { dto.setOutQty((Integer) value); }
    },
    CURRENT_STOCK {
        @Override void apply(WarehouseIoDto dto, Object value) { dto.setCurrentStock((Integer) value); }
    },
    CLIENT_NAME {
        @Override void apply(WarehouseIoDto dto, Object value) { dto.setClientName((String) value); }
    },
    MANAGER_NAME {
        @Override void apply(WarehouseIoDto dto, Object value) { dto.setManagerName((String) value); }
    },
    MANAGER_PHONE {
        @Override void apply(WarehouseIoDto dto, Object value) { dto.setManagerPhone((String) value); }
    },
    IO_DATE {
        @Override void apply(WarehouseIoDto dto, Object value) { dto.setIoDate((String) value); }
    },
    STATUS {
        @Override void apply(WarehouseIoDto dto, Object value) { dto.setStatus((String) value); }
    },
    MEMO {
        @Override void apply(WarehouseIoDto dto, Object value) { dto.setMemo((String) value); }
    };

    abstract void apply(WarehouseIoDto dto, Object value);

    String sqlName() {
        return name();
    }
}
