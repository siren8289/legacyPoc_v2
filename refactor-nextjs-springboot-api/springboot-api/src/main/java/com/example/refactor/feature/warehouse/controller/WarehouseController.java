package com.example.refactor.feature.warehouse.controller;

// REST 컨트롤러 — 레거시 action 파라미터를 HTTP 메서드/경로로 대체한다.
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
        // 입출고 목록 화면에서 테이블 렌더링을 위해 호출한다.
        return new ApiResponse<>(service.list());
    }

    @GetMapping("/{id}")
    public ApiResponse<WarehouseIo> detail(@PathVariable Long id) {
        return new ApiResponse<>(service.detail(id));
    }

    @PostMapping
    public Map<String, Object> create(@RequestBody CreateWarehouseRequest request) {
        // 프론트 "신규 등록" 폼에서 호출하는 생성 API.
        WarehouseIo created = service.create(request);
        return Map.of("result", "OK", "warehouseIoId", created.warehouseIoId());
    }
}
