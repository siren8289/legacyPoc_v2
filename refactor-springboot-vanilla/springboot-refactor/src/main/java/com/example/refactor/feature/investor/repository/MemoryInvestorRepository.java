package com.example.refactor.feature.investor.repository;

import com.example.refactor.common.InvestorSearchIndex;
import com.example.refactor.common.PageResult;
import com.example.refactor.feature.investor.model.Investor;
import com.example.refactor.feature.investor.model.InvestorListItem;
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
public class MemoryInvestorRepository implements InvestorRepository {

    private final Map<Long, Investor> table = new LinkedHashMap<>();
    private final InvestorSearchIndex nameIndex = new InvestorSearchIndex();
    private final AtomicLong sequence = new AtomicLong(2000);

    public MemoryInvestorRepository() {
        save(new Investor(null, "김민수", "VIP", 850000000L, "글로벌인컴펀드", "최근 요청: 월간 리포트 이메일 발송"));
        save(new Investor(null, "이서연", "GOLD", 320000000L, "국내배당주랩", "최근 문의: 수익률 변동 사유"));
        save(new Investor(null, "박준호", "SILVER", 120000000L, "안정형채권플랜", "만기 도래 예정 상품 설명 필요"));
    }

    @Override
    public List<Investor> findAll(String keyword) {
        String q = keyword == null ? "" : keyword.trim();
        List<Long> candidates = nameIndex.candidateIds(keyword);
        List<Investor> rows = new ArrayList<>();

        if (candidates == null) {
            return new ArrayList<>(table.values());
        }

        Iterable<Long> idsToScan = candidates.isEmpty() ? table.keySet() : candidates;
        for (Long id : idsToScan) {
            Investor investor = table.get(id);
            if (investor != null && (q.isEmpty() || investor.investorName().contains(q))) {
                rows.add(investor);
            }
        }
        return rows;
    }

    @Override
    public PageResult<InvestorListItem> findPage(String keyword, int page, int size) {
        List<Investor> matched = findAll(keyword);
        int safeSize = Math.max(1, Math.min(size, 100));
        int safePage = Math.max(0, page);
        List<InvestorListItem> all = matched.stream()
                .map(i -> new InvestorListItem(
                        i.investorId(), i.investorName(), i.investorGrade(), i.totalAmount()))
                .toList();
        int from = safePage * safeSize;
        int to = Math.min(from + safeSize, all.size());
        List<InvestorListItem> slice = from >= all.size() ? List.of() : all.subList(from, to);
        return PageResult.of(slice, all.size(), safePage, safeSize);
    }

    @Override
    public Optional<Investor> findById(Long id) {
        return Optional.ofNullable(table.get(id));
    }

    @Override
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
        nameIndex.register(id, saved.investorName());
        return saved;
    }

    @Override
    public void deleteById(Long id) {
        table.remove(id);
    }
}
