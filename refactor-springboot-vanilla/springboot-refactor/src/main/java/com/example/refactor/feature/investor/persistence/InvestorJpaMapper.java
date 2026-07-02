package com.example.refactor.feature.investor.persistence;

import com.example.refactor.feature.investor.model.Investor;

/**
 * 도메인 {@link Investor} ↔ JPA {@link InvestorJpaEntity} 변환.
 */
public final class InvestorJpaMapper {

    private InvestorJpaMapper() {
    }

    public static Investor toDomain(InvestorJpaEntity entity) {
        return new Investor(
                entity.getInvestorId(),
                entity.getInvestorName(),
                entity.getInvestorGrade(),
                entity.getTotalAmount(),
                entity.getLastProductName(),
                entity.getScreenMemo()
        );
    }

    public static InvestorJpaEntity toNewEntity(Investor investor) {
        InvestorJpaEntity entity = new InvestorJpaEntity();
        if (investor.investorId() != null) {
            entity.setInvestorId(investor.investorId());
        }
        entity.setInvestorName(investor.investorName());
        entity.setInvestorGrade(investor.investorGrade());
        entity.setTotalAmount(investor.totalAmount());
        entity.setLastProductName(investor.lastProductName());
        entity.setScreenMemo(investor.screenMemo());
        return entity;
    }

    public static void merge(InvestorJpaEntity entity, Investor investor) {
        entity.setInvestorName(investor.investorName());
        entity.setInvestorGrade(investor.investorGrade());
        entity.setTotalAmount(investor.totalAmount());
        entity.setLastProductName(investor.lastProductName());
        entity.setScreenMemo(investor.screenMemo());
    }
}
