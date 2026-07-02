package com.example.refactor.feature.investor.model;

// 도메인 모델 — REST 응답 및 서비스 계층 간 데이터 전달에 사용한다.
public record Investor(
        Long investorId,
        String investorName,
        String investorGrade,
        Long totalAmount,
        String lastProductName,
        String screenMemo
) {
}
