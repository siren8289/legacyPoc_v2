package com.example.refactor.feature.warehouse.repository;

// 빠른 데모와 리팩토링 비교를 위한 메모리 입출고 저장소.
import com.example.refactor.feature.warehouse.model.WarehouseIo;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class WarehouseRepository {
    private final Map<Long, WarehouseIo> table = new LinkedHashMap<>();
    private final AtomicLong sequence = new AtomicLong(1000);

    public WarehouseRepository() {
        save(new WarehouseIo(null, "서울1창고", "P-100", "산업용 센서", "전자부품", 140, 30, 110, "한빛유통", "ACTIVE"));
        save(new WarehouseIo(null, "부산2창고", "P-220", "모터 모듈", "기계부품", 90, 12, 78, "남해물산", "ACTIVE"));
    }

    public List<WarehouseIo> findAll() {
        return new ArrayList<>(table.values());
    }

    public WarehouseIo findById(Long id) {
        return table.get(id);
    }

    public WarehouseIo save(WarehouseIo item) {
        Long id = item.warehouseIoId() == null ? sequence.incrementAndGet() : item.warehouseIoId();
        WarehouseIo saved = new WarehouseIo(
                id,
                item.warehouseName(),
                item.productCode(),
                item.productName(),
                item.productCategory(),
                item.inQty(),
                item.outQty(),
                item.currentStock(),
                item.clientName(),
                item.status()
        );
        table.put(id, saved);
        return saved;
    }
}
