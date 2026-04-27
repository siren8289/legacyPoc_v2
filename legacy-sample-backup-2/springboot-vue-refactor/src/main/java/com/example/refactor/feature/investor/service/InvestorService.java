package com.example.refactor.feature.investor.service;

// 투자자 서비스: 기본값 처리 후 저장소 접근을 조정한다.
import com.example.refactor.feature.investor.dto.CreateInvestorRequest;
import com.example.refactor.feature.investor.model.Investor;
import com.example.refactor.feature.investor.repository.InvestorRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InvestorService {
    private final InvestorRepository repository;

    public InvestorService(InvestorRepository repository) {
        this.repository = repository;
    }

    public List<Investor> list(String name) {
        return repository.findAll(name);
    }

    public Investor detail(Long id) {
        return repository.findById(id);
    }

    public Investor create(CreateInvestorRequest request) {
        Investor investor = new Investor(
                null,
                safe(request.investorName()),
                safe(request.investorGrade()),
                request.totalAmount() == null ? 0L : request.totalAmount(),
                safe(request.lastProductName()),
                safe(request.screenMemo())
        );
        return repository.save(investor);
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
