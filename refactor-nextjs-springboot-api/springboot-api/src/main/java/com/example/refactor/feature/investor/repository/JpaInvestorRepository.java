package com.example.refactor.feature.investor.repository;

import com.example.refactor.common.PageResult;
import com.example.refactor.feature.investor.model.Investor;
import com.example.refactor.feature.investor.model.InvestorListItem;
import com.example.refactor.feature.investor.persistence.InvestorJpaEntity;
import com.example.refactor.feature.investor.persistence.InvestorJpaMapper;
import com.example.refactor.feature.investor.persistence.SpringDataInvestorJpaRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA 저장소 — Entity 조회·저장 후 항상 {@link Investor} 도메인으로 변환해 반환한다.
 */
@Repository
@ConditionalOnProperty(name = "app.repository.type", havingValue = "jpa")
public class JpaInvestorRepository implements InvestorRepository {

    private final SpringDataInvestorJpaRepository jpa;

    public JpaInvestorRepository(SpringDataInvestorJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public List<Investor> findAll(String keyword) {
        String q = keyword == null ? "" : keyword.trim();
        List<InvestorJpaEntity> rows = q.isEmpty()
                ? jpa.findAllByOrderByInvestorIdAsc()
                : jpa.findByInvestorNameContainingOrderByInvestorIdAsc(q);
        return rows.stream().map(InvestorJpaMapper::toDomain).toList();
    }

    @Override
    public PageResult<InvestorListItem> findPage(String keyword, int page, int size) {
        String q = keyword == null ? "" : keyword.trim();
        int safeSize = Math.max(1, Math.min(size, 100));
        int safePage = Math.max(0, page);
        List<InvestorListItem> items = jpa.findListItems(q, PageRequest.of(safePage, safeSize));
        long total = jpa.countByKeyword(q);
        return PageResult.of(items, total, safePage, safeSize);
    }

    @Override
    public Optional<Investor> findById(Long id) {
        return jpa.findById(id).map(InvestorJpaMapper::toDomain);
    }

    @Override
    public Investor save(Investor investor) {
        InvestorJpaEntity entity = investor.investorId() == null
                ? InvestorJpaMapper.toNewEntity(investor)
                : jpa.findById(investor.investorId())
                        .map(existing -> {
                            InvestorJpaMapper.merge(existing, investor);
                            return existing;
                        })
                        .orElseGet(() -> InvestorJpaMapper.toNewEntity(investor));
        return InvestorJpaMapper.toDomain(jpa.save(entity));
    }

    @Override
    public void deleteById(Long id) {
        jpa.deleteById(id);
    }
}
