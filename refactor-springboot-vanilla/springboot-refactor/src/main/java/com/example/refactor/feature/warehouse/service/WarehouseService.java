package com.example.refactor.feature.warehouse.service;

import com.example.refactor.feature.warehouse.dto.CreateWarehouseRequest;
import com.example.refactor.feature.warehouse.model.WarehouseIo;
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

    public WarehouseIo detail(Long id) {
        return repository.findById(id);
    }

    public WarehouseIo create(CreateWarehouseRequest request) {
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
