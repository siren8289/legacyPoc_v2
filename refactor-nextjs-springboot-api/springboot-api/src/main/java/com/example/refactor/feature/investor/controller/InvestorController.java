package com.example.refactor.feature.investor.controller;

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
    public ApiResponse<List<Investor>> list(@RequestParam(required = false) String name) {
        // 투자자 목록 조회 엔드포인트(이름 키워드 선택적 필터 지원).
        return new ApiResponse<>(service.list(name));
    }

    @GetMapping("/{id}")
    public ApiResponse<Investor> detail(@PathVariable Long id) {
        return new ApiResponse<>(service.detail(id));
    }

    @PostMapping
    public Map<String, Object> create(@RequestBody CreateInvestorRequest request) {
        // 프론트 폼 처리 단순화를 위해 결과와 생성 ID만 반환한다.
        Investor created = service.create(request);
        return Map.of("result", "OK", "investorId", created.investorId());
    }
}
