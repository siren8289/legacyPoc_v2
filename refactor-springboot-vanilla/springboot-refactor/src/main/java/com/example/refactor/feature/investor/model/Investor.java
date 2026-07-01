package com.example.refactor.feature.investor.model;

// 투자자 기능에서 controller/service/repository가 공통으로 사용하는 도메인 모델.
public record Investor(
        Long investorId,
        String investorName,
        String investorGrade,
        Long totalAmount,
        String lastProductName,
        String screenMemo
) {
}
