package com.example.refactor.feature.investor.model;

/** 목록 화면용 projection — 이름 검색 결과에 필요한 컬럼만. */
public record InvestorListItem(
        Long investorId,
        String investorName,
        String investorGrade,
        Long totalAmount
) {
}
