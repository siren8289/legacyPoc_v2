package com.example.refactor.feature.investor.model;

// 기능 중심 구조에서 투자자 도메인을 표현하는 모델.
public record Investor(
        Long investorId,
        String investorName,
        String investorGrade,
        Long totalAmount,
        String lastProductName,
        String screenMemo
) {
}
