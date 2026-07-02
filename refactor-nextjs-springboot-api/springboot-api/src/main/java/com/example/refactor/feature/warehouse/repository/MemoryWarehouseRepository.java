package com.example.refactor.feature.warehouse.repository;

import com.example.refactor.common.PageResult;
import com.example.refactor.feature.warehouse.model.WarehouseIo;
import com.example.refactor.feature.warehouse.model.WarehouseListItem;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Repository
@ConditionalOnProperty(name = "app.repository.type", havingValue = "memory", matchIfMissing = true)
public class MemoryWarehouseRepository implements WarehouseRepository {

    private final Map<Long, WarehouseIo> table = new LinkedHashMap<>();
    private final AtomicLong sequence = new AtomicLong(1000);

    public MemoryWarehouseRepository() {
        save(new WarehouseIo(null, "서울1창고", "P-100", "산업용 센서", "전자부품", 140, 30, 110, "한빛유통", "ACTIVE"));
        save(new WarehouseIo(null, "부산2창고", "P-220", "모터 모듈", "기계부품", 90, 12, 78, "남해물산", "ACTIVE"));
    }

    @Override
    public List<WarehouseIo> findAll() {
        return new ArrayList<>(table.values());
    }

    @Override
    public PageResult<WarehouseListItem> findActivePage(int page, int size) {
        int safeSize = Math.max(1, Math.min(size, 100));
        int safePage = Math.max(0, page);
        List<WarehouseListItem> all = table.values().stream()
                .sorted((a, b) -> Long.compare(b.warehouseIoId(), a.warehouseIoId()))
                .map(w -> new WarehouseListItem(
                        w.warehouseIoId(), w.warehouseName(), w.productName(), w.currentStock()))
                .toList();
        int from = safePage * safeSize;
        int to = Math.min(from + safeSize, all.size());
        List<WarehouseListItem> slice = from >= all.size() ? List.of() : all.subList(from, to);
        return PageResult.of(slice, all.size(), safePage, safeSize);
    }

    @Override
    public Optional<WarehouseIo> findById(Long id) {
        return Optional.ofNullable(table.get(id));
    }

    @Override
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

    @Override
    public WarehouseIo updateField(Long id, String fieldName, Object value) {
        WarehouseIo existing = table.get(id);
        if (existing == null) {
            return null;
        }
        WarehouseIo updated = applyField(existing, fieldName, value);
        table.put(id, updated);
        return updated;
    }

    @Override
    public void deleteById(Long id) {
        table.remove(id);
    }

    private WarehouseIo applyField(WarehouseIo row, String fieldName, Object value) {
        return switch (fieldName) {
            case "warehouseName" -> new WarehouseIo(row.warehouseIoId(), asString(value), row.productCode(),
                    row.productName(), row.productCategory(), row.inQty(), row.outQty(), row.currentStock(),
                    row.clientName(), row.status());
            case "productCode" -> new WarehouseIo(row.warehouseIoId(), row.warehouseName(), asString(value),
                    row.productName(), row.productCategory(), row.inQty(), row.outQty(), row.currentStock(),
                    row.clientName(), row.status());
            case "productName" -> new WarehouseIo(row.warehouseIoId(), row.warehouseName(), row.productCode(),
                    asString(value), row.productCategory(), row.inQty(), row.outQty(), row.currentStock(),
                    row.clientName(), row.status());
            case "productCategory" -> new WarehouseIo(row.warehouseIoId(), row.warehouseName(), row.productCode(),
                    row.productName(), asString(value), row.inQty(), row.outQty(), row.currentStock(),
                    row.clientName(), row.status());
            case "inQty" -> new WarehouseIo(row.warehouseIoId(), row.warehouseName(), row.productCode(),
                    row.productName(), row.productCategory(), asInt(value), row.outQty(), row.currentStock(),
                    row.clientName(), row.status());
            case "outQty" -> new WarehouseIo(row.warehouseIoId(), row.warehouseName(), row.productCode(),
                    row.productName(), row.productCategory(), row.inQty(), asInt(value), row.currentStock(),
                    row.clientName(), row.status());
            case "currentStock" -> new WarehouseIo(row.warehouseIoId(), row.warehouseName(), row.productCode(),
                    row.productName(), row.productCategory(), row.inQty(), row.outQty(), asInt(value),
                    row.clientName(), row.status());
            case "clientName" -> new WarehouseIo(row.warehouseIoId(), row.warehouseName(), row.productCode(),
                    row.productName(), row.productCategory(), row.inQty(), row.outQty(), row.currentStock(),
                    asString(value), row.status());
            case "status" -> new WarehouseIo(row.warehouseIoId(), row.warehouseName(), row.productCode(),
                    row.productName(), row.productCategory(), row.inQty(), row.outQty(), row.currentStock(),
                    row.clientName(), asString(value));
            default -> throw new IllegalArgumentException("Unknown field: " + fieldName);
        };
    }

    private String asString(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private int asInt(Object value) {
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
