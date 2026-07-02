package com.example.refactor.feature.investor.controller;

// REST 컨트롤러 — 레거시 action 파라미터를 HTTP 메서드/경로로 대체한다.
import com.example.refactor.common.ApiResponse;
import com.example.refactor.feature.investor.dto.CreateInvestorRequest;
import com.example.refactor.feature.investor.model.Investor;
import com.example.refactor.feature.investor.service.InvestorService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/investors")
public class InvestorController {
    private final InvestorService service;

    public InvestorController(InvestorService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<?> list(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        if (page != null && size != null) {
            return new ApiResponse<>(service.listPage(name, page, size));
        }
        return new ApiResponse<>(service.list(name));
    }

    @GetMapping("/{id}")
    public ApiResponse<Investor> detail(@PathVariable Long id) {
        return new ApiResponse<>(service.detail(id));
    }

    @PostMapping
    public Map<String, Object> create(@RequestBody CreateInvestorRequest request) {
        // 프론트에서 재조회 흐름에 쓸 수 있도록 생성 ID를 함께 반환한다.
        Investor created = service.create(request);
        return Map.of("result", "OK", "investorId", created.investorId());
    }
}
