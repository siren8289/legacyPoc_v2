package com.example.refactor.feature.investor.model;

/**
 * 투자자 도메인 모델.
 * <p>Service·Controller에서 사용한다. JPA/DB 어노테이션 없음.
 * DB 테이블 매핑은 persistence 패키지의 {@code InvestorJpaEntity}가 담당한다.
 */
public record Investor(
        Long investorId,
        String investorName,
        String investorGrade,
        Long totalAmount,
        String lastProductName,
        String screenMemo
) {
}
