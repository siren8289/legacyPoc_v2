package com.example.refactor.feature.investor.repository;

// 외부 인프라 없이 Vue 샘플을 실행하기 위한 메모리 저장소.
import com.example.refactor.feature.investor.model.Investor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InvestorRepository {
    private final Map<Long, Investor> table = new LinkedHashMap<>();
    private final AtomicLong sequence = new AtomicLong(2000);

    public InvestorRepository() {
        save(new Investor(null, "김민수", "VIP", 850000000L, "글로벌인컴펀드", "최근 요청: 월간 리포트 이메일 발송"));
        save(new Investor(null, "이서연", "GOLD", 320000000L, "국내배당주랩", "최근 문의: 수익률 변동 사유"));
        save(new Investor(null, "박준호", "SILVER", 120000000L, "안정형채권플랜", "만기 도래 예정 상품 설명 필요"));
    }

    public List<Investor> findAll(String keyword) {
        String q = keyword == null ? "" : keyword.trim();
        List<Investor> rows = new ArrayList<>();
        for (Investor investor : table.values()) {
            if (q.isEmpty() || investor.investorName().contains(q)) {
                rows.add(investor);
            }
        }
        return rows;
    }

    public Investor findById(Long id) {
        return table.get(id);
    }

    public Investor save(Investor investor) {
        Long id = investor.investorId() == null ? sequence.incrementAndGet() : investor.investorId();
        Investor saved = new Investor(
                id,
                investor.investorName(),
                investor.investorGrade(),
                investor.totalAmount(),
                investor.lastProductName(),
                investor.screenMemo()
        );
        table.put(id, saved);
        return saved;
    }
}
