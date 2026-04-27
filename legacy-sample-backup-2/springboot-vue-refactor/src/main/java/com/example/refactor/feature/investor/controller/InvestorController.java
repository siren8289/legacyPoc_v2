package com.example.refactor.feature.investor.controller;

// 뷰 화면에서 호출하는 투자자 엔드포인트 컨트롤러.
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
        // Vue 투자자 화면에서 키워드 검색 시 호출한다.
        return new ApiResponse<>(service.list(name));
    }

    @GetMapping("/{id}")
    public ApiResponse<Investor> detail(@PathVariable Long id) {
        return new ApiResponse<>(service.detail(id));
    }

    @PostMapping
    public Map<String, Object> create(@RequestBody CreateInvestorRequest request) {
        // Vue 폼 제출 처리에 필요한 최소 응답만 반환한다.
        Investor created = service.create(request);
        return Map.of("result", "OK", "investorId", created.investorId());
    }
}
