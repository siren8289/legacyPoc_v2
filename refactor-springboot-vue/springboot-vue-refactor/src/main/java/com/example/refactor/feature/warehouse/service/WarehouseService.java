package com.example.refactor.feature.warehouse.service;

// 서비스 계층 — 입력 정규화·기본값 처리 후 저장소에 위임한다.
import com.example.refactor.common.PageResult;
import com.example.refactor.feature.warehouse.dto.CreateWarehouseRequest;
import com.example.refactor.feature.warehouse.model.WarehouseIo;
import com.example.refactor.feature.warehouse.model.WarehouseListItem;
import com.example.refactor.feature.warehouse.repository.WarehouseRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WarehouseService {
    private final WarehouseRepository repository;

    public WarehouseService(WarehouseRepository repository) {
        this.repository = repository;
    }

    public List<WarehouseIo> list() {
        return repository.findAll();
    }

    public PageResult<WarehouseListItem> listPage(int page, int size) {
        return repository.findActivePage(page, size);
    }

    public WarehouseIo detail(Long id) {
        return repository.findById(id).orElse(null);
    }

    public WarehouseIo create(CreateWarehouseRequest request) {
        // 레거시의 createEmptyRow + 필드별 update 흐름을 단일 POST로 대체한다.
        WarehouseIo row = new WarehouseIo(
                null,
                safe(request.warehouseName()),
                safe(request.productCode()),
                safe(request.productName()),
                safe(request.productCategory()),
                request.inQty() == null ? 0 : request.inQty(),
                request.outQty() == null ? 0 : request.outQty(),
                request.currentStock() == null ? 0 : request.currentStock(),
                safe(request.clientName()),
                safe(request.status())
        );
        return repository.save(row);
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
