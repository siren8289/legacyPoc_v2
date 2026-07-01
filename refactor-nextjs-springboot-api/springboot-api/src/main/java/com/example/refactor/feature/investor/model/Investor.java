package com.example.refactor.feature.investor.model;

// 투자자 기능 도메인 모델.
public record Investor(
        Long investorId,
        String investorName,
        String investorGrade,
        Long totalAmount,
        String lastProductName,
        String screenMemo
) {
}
