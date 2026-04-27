package com.example.refactor.feature.warehouse.controller;

// 입출고 기능 REST 엔드포인트를 제공하는 컨트롤러.
import com.example.refactor.common.ApiResponse;
import com.example.refactor.feature.warehouse.dto.CreateWarehouseRequest;
import com.example.refactor.feature.warehouse.model.WarehouseIo;
import com.example.refactor.feature.warehouse.service.WarehouseService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/warehouses")
public class WarehouseController {
    private final WarehouseService service;

    public WarehouseController(WarehouseService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<List<WarehouseIo>> list() {
        return new ApiResponse<>(service.list());
    }

    @GetMapping("/{id}")
    public ApiResponse<WarehouseIo> detail(@PathVariable Long id) {
        return new ApiResponse<>(service.detail(id));
    }

    @PostMapping
    public Map<String, Object> create(@RequestBody CreateWarehouseRequest request) {
        WarehouseIo created = service.create(request);
        return Map.of("result", "OK", "warehouseIoId", created.warehouseIoId());
    }
}
